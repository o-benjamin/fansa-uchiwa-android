package com.fansauchiwa.data.repository

import android.util.Log
import com.fansauchiwa.data.AffiliateLink
import com.fansauchiwa.data.AffiliateLinksParser
import com.fansauchiwa.data.infra.RemoteConfigDataSource
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first

/**
 * 保存完了のダイアログに出す、うちわの材料のアフィリエイトのリンク（#311）
 *
 * リンクは Remote Config で配る。空のリストなら、リンクの欄ごと出さない
 */
interface AffiliateRepository {
    fun getAffiliateLinksStream(): Flow<List<AffiliateLink>>

    /**
     * Remote Config から読み直す。例外は外に出さず、読めなければ空のリストを流す（保存の流れを止めないため）
     */
    suspend fun fetchAffiliateLinks()
}

class AffiliateRepositoryImpl @Inject constructor(
    private val remoteConfigDataSource: RemoteConfigDataSource,
    private val crashReportingRepository: CrashReportingRepository
) : AffiliateRepository {

    private val affiliateLinks = MutableSharedFlow<List<AffiliateLink>>(replay = 1)

    // 画面を開くたびに読むので、同じ誤りを全員が毎回送らないよう1プロセスに1回だけ記録する
    private var hasRecordedParseError = false

    override fun getAffiliateLinksStream(): Flow<List<AffiliateLink>> = affiliateLinks.asSharedFlow()

    override suspend fun fetchAffiliateLinks() {
        val links = try {
            AffiliateLinksParser.parse(remoteConfigDataSource.getAffiliateMaterialsJsonStream().first())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Remote Config に置いた JSON の誤り。全員の端末で起きるので、Crashlytics で気づけるようにする
            Log.w(TAG, "affiliate_materials を読めない", e)
            if (!hasRecordedParseError) {
                hasRecordedParseError = true
                crashReportingRepository.recordException(e)
            }
            emptyList()
        }
        affiliateLinks.emit(links)
    }

    private companion object {
        const val TAG = "Affiliate"
    }
}
