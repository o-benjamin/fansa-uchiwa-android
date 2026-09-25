package com.fansauchiwa.analytics

import com.fansauchiwa.data.Decoration
import com.fansauchiwa.data.extractUchiwaIdFromImagePath
import com.fansauchiwa.data.repository.CrashReportingRepository
import com.fansauchiwa.data.repository.LocalDatabaseRepository
import com.fansauchiwa.data.repository.MasterpieceRepository
import com.fansauchiwa.edit.FontFamilies
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * フォント選びが「迷い」か「楽しみ」かを見分けるための計測（#242）で、編集セッションの状態を持つ。
 *
 * 編集画面（EditViewModel）とPreview画面（UchiwaPreviewViewModel）の両方から呼ばれるため、
 * 画面をまたいで値を持てるようアプリ内で1つ（[Singleton]）にしている。
 * 値はメモリにだけ持ち、設定（DataStore）・DB・ナビゲーション引数には入れない。
 * そのため、プロセスが再生成されると失われる（その場合、tap_preview_export には #242 のパラメータを付けない）。
 *
 * 計測をやめるときに消すもの：
 * - このクラス、[FontSessionAnalyticsSnapshot]、[FontSessionAnalyticsParams]、FontSessionAnalyticsBuckets.kt
 * - EditViewModel の呼び出し（init の startSession、updateFont の onFontSwitched、
 *   fontSessionParamsForDiscardEvent、finishFontSessionForPreview）
 * - EditScreen の呼び出し（保存後の finishFontSessionForPreview、破棄ダイアログで
 *   tap_edit_back_dialog のパラメータに fontSessionParamsForDiscardEvent を足しているところ）
 * - UchiwaPreviewViewModel の logExportEvent で exportParams を付けているところ
 * - テスト：FontSessionTrackerTest、FontSessionAnalyticsBucketsTest、EditViewModelTest と
 *   UchiwaPreviewSaveTest のフォント計測のテスト
 */
@Singleton
class FontSessionTracker internal constructor(
    private val localDatabaseRepository: LocalDatabaseRepository,
    private val masterpieceRepository: MasterpieceRepository,
    private val crashReportingRepository: CrashReportingRepository,
    private val currentTimeMillis: () -> Long
) {
    // コンストラクタが2つあるのは、テストでは時刻を差し替えたいが、Hilt には関数型の引数を
    // 渡す登録が無いため。アプリでは下の @Inject のもの（現在時刻を使う）が使われる
    @Inject
    constructor(
        localDatabaseRepository: LocalDatabaseRepository,
        masterpieceRepository: MasterpieceRepository,
        crashReportingRepository: CrashReportingRepository
    ) : this(
        localDatabaseRepository,
        masterpieceRepository,
        crashReportingRepository,
        System::currentTimeMillis
    )

    private var fontSwitchCount = 0

    // フォントを最後に切り替えたテキスト装飾のID（フォント自体ではなくIDを持つのは、
    // その装飾が削除された場合に古いフォントを参照し続けないようにするため。
    // 最終的なフォントは参照時に現在の decorations から解決する）
    private var lastSwitchedDecorationId: String? = null
    private var editStartTimeMillis = currentTimeMillis()
    private var snapshotForPreview: FontSessionAnalyticsSnapshot? = null

    /** 編集画面を開いたときに呼ぶ。前の編集セッションの値を捨てて数え直す */
    fun startSession() {
        resetCounting()
        snapshotForPreview = null
    }

    /** テキスト装飾のフォントを切り替えたときに呼ぶ */
    fun onFontSwitched(decorationId: String) {
        fontSwitchCount++
        lastSwitchedDecorationId = decorationId
    }

    /**
     * 破棄（tap_edit_back_dialog の action=delete）のログに付けるパラメータを返す。
     * 保存側にだけ付けると、諦めた人のデータが丸ごと欠けるため（選択バイアス）。
     */
    fun paramsForDiscardEvent(): Map<String, Any> = baseFontSessionParams(
        switchCount = fontSwitchCount,
        elapsedMillis = currentTimeMillis() - editStartTimeMillis
    )

    /**
     * 保存してPreview画面へ進むときに呼ぶ。この時点の値を tap_preview_export 用に取っておき、
     * 編集画面に戻って続けて編集する場合に備えてセッションを数え直す。
     *
     * 最終的なフォントは、このセッションで最後に切り替えたテキスト装飾の現在のフォント。
     * その装飾が既に削除されていれば、最初のテキスト装飾の（テンプレートまたは初期値の）
     * フォントにする。テキスト装飾が無ければ null。
     *
     * @param decorations 保存したうちわの装飾
     */
    fun finishSessionForPreview(decorations: List<Decoration>) {
        val textDecorations = decorations.filterIsInstance<Decoration.Text>()
        val finalFont = textDecorations.find { it.id == lastSwitchedDecorationId }?.font
            ?: textDecorations.firstOrNull()?.font
        snapshotForPreview = FontSessionAnalyticsSnapshot(
            fontSwitchCount = fontSwitchCount,
            finalFont = finalFont,
            editStartTimeMillis = editStartTimeMillis
        )
        resetCounting()
    }

    private fun resetCounting() {
        fontSwitchCount = 0
        lastSwitchedDecorationId = null
        editStartTimeMillis = currentTimeMillis()
    }

    /**
     * tap_preview_export に付けるパラメータを返す。
     * [finishSessionForPreview] を通っていない（プロセスが再生成された等）ときは空。
     *
     * font_same_as_last は、保存済みのうちわのうち今回のうちわの直前に保存したもの
     * （ホーム画面の並びと同じく、うちわ画像の更新日時が新しい順で今回のうちわを除いた先頭）の
     * テキスト装飾に、今回の最終的なフォントが使われているかどうか。直前のうちわが無ければ "false"。
     * 直前のうちわを読めなかった（装飾のデータが壊れている等）ときは、保存の操作を止めないよう
     * 例外を Crashlytics に記録して、font_same_as_last だけ付けずに返す。
     *
     * @param currentUchiwaId Preview画面で表示しているうちわのID
     */
    suspend fun exportParams(currentUchiwaId: String?): Map<String, Any> {
        val snapshot = snapshotForPreview ?: return emptyMap()
        val baseParams = baseFontSessionParams(
            switchCount = snapshot.fontSwitchCount,
            elapsedMillis = currentTimeMillis() - snapshot.editStartTimeMillis
        )
        val finalFont = snapshot.finalFont ?: return baseParams
        val paramsWithRank = baseParams +
            (FontSessionAnalyticsParams.FINAL_FONT_RANK_BUCKET to finalFontRankBucket(finalFont))
        val previousFonts = try {
            fontsOfPreviousUchiwa(currentUchiwaId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            crashReportingRepository.recordException(e)
            return paramsWithRank
        }

        return paramsWithRank +
            (FontSessionAnalyticsParams.FONT_SAME_AS_LAST to (finalFont in previousFonts).toString())
    }

    private suspend fun fontsOfPreviousUchiwa(currentUchiwaId: String?): Set<FontFamilies> {
        // うちわ画像のファイル一覧を読むため、メインスレッドを止めないよう IO で行う
        val previousUchiwaId = withContext(Dispatchers.IO) {
            masterpieceRepository.loadAllMasterpieces()
                .map(::extractUchiwaIdFromImagePath)
                .firstOrNull { it != currentUchiwaId }
        } ?: return emptySet()
        val previousUchiwa = localDatabaseRepository.getUchiwa(previousUchiwaId) ?: return emptySet()
        return previousUchiwa.decorations
            .filterIsInstance<Decoration.Text>()
            .map { it.font }
            .toSet()
    }
}
