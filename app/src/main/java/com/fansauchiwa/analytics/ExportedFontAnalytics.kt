package com.fansauchiwa.analytics

import com.fansauchiwa.data.Decoration
import javax.inject.Inject

/**
 * ギャラリーへ保存できたうちわで使われているフォントを、export_uchiwa_font のイベントにする（#288）。
 *
 * フォントの選択（select_edit_text_font）は、選んだあと消したフォントも数える。
 * 保存したうちわに残ったフォントを数えて、使われないフォントを削る判断に使う。
 *
 * 保存するたびに送る（同じうちわを2回保存すると2回送る）。割合はイベント数ではなく人数で出し、
 * 分母は tap_preview_export の人数にする。
 */
class ExportedFontAnalytics @Inject constructor(
    private val uchiwaReader: AnalyticsUchiwaReader
) {
    /**
     * @param uchiwaId Preview画面で表示しているうちわのID
     * @return うちわのテキストが使っているフォントごとに1つずつのイベント（同じフォントは1つにまとめる）。
     *   テキストが無いとき・うちわを読めなかったとき（[AnalyticsUchiwaReader.readOrNull] が null）は空
     */
    suspend fun fontEvents(uchiwaId: String?): List<AnalyticsEvent> {
        val uchiwa = uchiwaReader.readOrNull(uchiwaId) ?: return emptyList()
        return uchiwa.decorations
            .filterIsInstance<Decoration.Text>()
            .map { it.font }
            .distinct()
            .map { font ->
                AnalyticsEvent(
                    AnalyticsActions.EXPORT_UCHIWA_FONT,
                    mapOf(FontFamilyParams.PARAM_FONT_FAMILY to font.name)
                )
            }
    }
}
