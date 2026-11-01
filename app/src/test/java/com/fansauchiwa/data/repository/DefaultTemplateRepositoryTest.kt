package com.fansauchiwa.data.repository

import androidx.compose.ui.graphics.Color
import com.fansauchiwa.data.SavedUchiwa
import com.fansauchiwa.data.Template
import com.fansauchiwa.data.TemplateCandidates
import com.fansauchiwa.data.templateList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Optional

class DefaultTemplateRepositoryTest {

    private val candidate = Template(
        id = "candidate_1",
        savedUchiwa = SavedUchiwa(
            decorations = emptyList(),
            uchiwaColor = Color.Black,
            backgroundColor = Color.Black
        )
    )

    @Test
    fun getTemplates_withoutCandidates_returnsTemplateListOnly() = runTest {
        val repository = DefaultTemplateRepository(Optional.empty())

        assertEquals(templateList, repository.getTemplates())
    }

    @Test
    fun getTemplates_withCandidates_appendsCandidatesAfterTemplateList() = runTest {
        val repository = DefaultTemplateRepository(Optional.of(TemplateCandidates(listOf(candidate))))

        assertEquals(templateList + candidate, repository.getTemplates())
    }

    @Test
    fun getTemplateById_withCandidates_findsCandidate() = runTest {
        val repository = DefaultTemplateRepository(Optional.of(TemplateCandidates(listOf(candidate))))

        assertEquals(candidate, repository.getTemplateById("candidate_1"))
    }

    @Test
    fun getTemplateById_withoutCandidates_doesNotFindCandidate() = runTest {
        val repository = DefaultTemplateRepository(Optional.empty())

        assertNull(repository.getTemplateById("candidate_1"))
    }
}
