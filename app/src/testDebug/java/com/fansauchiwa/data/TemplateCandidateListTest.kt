package com.fansauchiwa.data

import com.fansauchiwa.ui.StickerAsset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TemplateCandidateListTest {

    @Test
    fun templateCandidateList_ids_areUniqueIncludingTemplateList() {
        val ids = (templateList + templateCandidateList).map { it.id }

        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun templateCandidateList_templates_areNotNameTemplates() {
        assertFalse(templateCandidateList.any { it.isNameInputPlaceholderEnabled })
    }

    // 保存データでどの候補から作ったかを見分けるのにこの形を使う（.agents/template-candidates.md）
    @Test
    fun templateCandidateList_ids_matchCandidateFormat() {
        templateCandidateList.forEach { template ->
            assertTrue(template.id.matches(Regex("candidate_\\d+")))
            assertTrue(template.savedUchiwa.decorations.all { it.id.matches(Regex("${template.id}_\\d+")) })
        }
    }

    @Test
    fun templateCandidateList_decorationIds_areUnique() {
        val ids = templateCandidateList.flatMap { template -> template.savedUchiwa.decorations.map { it.id } }

        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun templateCandidateList_stickers_existInStickerAssets() {
        val knownLabels = StickerAsset.entries.map { it.type }
        val labels = templateCandidateList
            .flatMap { it.savedUchiwa.decorations }
            .filterIsInstance<Decoration.Sticker>()
            .map { it.label }

        assertTrue(knownLabels.containsAll(labels))
    }
}
