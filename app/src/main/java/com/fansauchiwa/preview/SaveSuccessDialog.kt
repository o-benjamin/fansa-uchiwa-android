package com.fansauchiwa.preview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fansauchiwa.R
import com.fansauchiwa.data.AffiliateLink
import com.fansauchiwa.ui.theme.FansaUchiwaTheme

/**
 * 保存が成功したときのダイアログ
 *
 * 保存した直後の人にも共有の機会を出す（#244）。リンクがあれば、うちわの材料のリンクも出す（#311）。
 * リンクを押してもダイアログは閉じない（Amazon から戻ってきたあとに、共有や OK を押せるように）。
 *
 * @param affiliateLinks うちわの材料のリンク。空なら欄ごと出さない
 * @param onAffiliateLinkClick リンクを押したとき。開く処理と計測は呼び出し側（UchiwaPreviewScreen）が行う
 * @param onConfirm 「OK」
 * @param onShare 「SNSでシェア」
 * @param onDismissRequest ダイアログの外を押した・戻るを押したとき
 */
@Composable
fun SaveSuccessDialog(
    affiliateLinks: List<AffiliateLink>,
    onAffiliateLinkClick: (AffiliateLink) -> Unit,
    onConfirm: () -> Unit,
    onShare: () -> Unit,
    onDismissRequest: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(text = stringResource(R.string.save_success_title))
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text(text = stringResource(R.string.save_success_message))
                if (affiliateLinks.isNotEmpty()) {
                    AffiliateLinksSection(
                        links = affiliateLinks,
                        onLinkClick = onAffiliateLinkClick
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = stringResource(R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onShare) {
                Text(text = stringResource(R.string.save_success_share))
            }
        }
    )
}

@Preview
@Composable
private fun SaveSuccessDialogPreview() {
    FansaUchiwaTheme {
        SaveSuccessDialog(
            affiliateLinks = emptyList(),
            onAffiliateLinkClick = {},
            onConfirm = {},
            onShare = {},
            onDismissRequest = {}
        )
    }
}

@Preview
@Composable
private fun SaveSuccessDialogWithAffiliateLinksPreview() {
    FansaUchiwaTheme {
        SaveSuccessDialog(
            affiliateLinks = listOf(
                AffiliateLink(id = "jumbo_uchiwa", label = "ジャンボうちわ", url = "https://www.amazon.co.jp/"),
                AffiliateLink(id = "mirror_sheet", label = "ミラーシート", url = "https://www.amazon.co.jp/")
            ),
            onAffiliateLinkClick = {},
            onConfirm = {},
            onShare = {},
            onDismissRequest = {}
        )
    }
}
