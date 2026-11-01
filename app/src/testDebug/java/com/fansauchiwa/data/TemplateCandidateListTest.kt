package com.fansauchiwa.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TemplateCandidateListTest {

    @Test
    fun templateCandidateList_ids_doNotCollideWithTemplateList() {
        val ids = (templateList + templateCandidateList).map { it.id }

        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun templateCandidateList_templates_areNotNameTemplates() {
        assertFalse(templateCandidateList.any { it.isNameInputPlaceholderEnabled })
    }

    // 保存データから候補を見分ける template_lab.py pull が、この id の形に頼っている
    @Test
    fun templateCandidateList_decorationIds_startWithTemplateId() {
        templateCandidateList.forEach { template ->
            assertTrue(template.savedUchiwa.decorations.all { it.id.startsWith("${template.id}_") })
        }
    }
}
