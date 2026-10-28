package com.fansauchiwa.preview

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fansauchiwa.R
import com.fansauchiwa.data.AffiliateLink
import com.fansauchiwa.ui.theme.FansaUchiwaTheme

/**
 * うちわの材料のアフィリエイトのリンク（#311）
 *
 * 「PR」の表示とアソシエイトの表示文は、ステマ規制と Amazon アソシエイトの規約で必要なので、リンクと必ず一緒に出す。
 * Amazon の商品画像・価格は規約上そのままは出せないため、文字だけにしている。
 *
 * @param links 出すリンク（空にしないこと。空のときは呼び出し側でこの欄ごと出さない）
 */
@Composable
fun AffiliateLinksSection(
    links: List<AffiliateLink>,
    onLinkClick: (AffiliateLink) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.affiliate_pr_label),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                    .padding(horizontal = 4.dp)
            )
            Text(
                text = stringResource(R.string.affiliate_section_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }
        links.forEach { link ->
            OutlinedButton(
                onClick = { onLinkClick(link) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(R.string.affiliate_link_label, link.label))
            }
        }
        Text(
            text = stringResource(R.string.affiliate_disclosure),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AffiliateLinksSectionPreview() {
    FansaUchiwaTheme {
        AffiliateLinksSection(
            links = listOf(
                AffiliateLink(id = "jumbo_uchiwa", label = "ジャンボうちわ", url = "https://www.amazon.co.jp/"),
                AffiliateLink(id = "mirror_sheet", label = "ミラーシート", url = "https://www.amazon.co.jp/")
            ),
            onLinkClick = {}
        )
    }
}
