package com.fansauchiwa.data.repository

import androidx.compose.ui.graphics.Color
import com.fansauchiwa.data.Decoration
import com.fansauchiwa.data.SavedUchiwa
import com.fansauchiwa.data.TEMPLATE_BASE_DENSITY
import com.fansauchiwa.data.Template
import com.fansauchiwa.data.TemplateCandidates
import com.fansauchiwa.data.infra.DisplayDensityDataSource
import com.fansauchiwa.data.templateList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
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
            backgroundColor = Color.Black,
            // 飾りがないので、密度で変わる値を持たせて、候補も伸ばされたことを比べられるようにする
            overallBorderWidth = 4f
        )
    )

    private fun densitySource(density: Float = TEMPLATE_BASE_DENSITY) = object : DisplayDensityDataSource {
        override fun getDensityStream(): Flow<Float> = flowOf(density)
    }

    @Test
    fun getTemplates_withoutCandidates_returnsTemplateListOnly() = runTest {
        val repository = DefaultTemplateRepository(Optional.empty(), densitySource())

        assertEquals(templateList, repository.getTemplates())
    }

    @Test
    fun getTemplates_withCandidates_appendsCandidatesAfterTemplateList() = runTest {
        val repository = DefaultTemplateRepository(Optional.of(TemplateCandidates(listOf(candidate))), densitySource())

        assertEquals(templateList + candidate, repository.getTemplates())
    }

    @Test
    fun getTemplates_higherDensity_returnsTemplatesAndCandidatesScaledToDensity() = runTest {
        val repository = DefaultTemplateRepository(Optional.of(TemplateCandidates(listOf(candidate))), densitySource(3.5f))

        val templates = repository.getTemplates()
        val originalText = templateList.first().savedUchiwa.decorations.first() as Decoration.Text
        val scaledText = templates.first().savedUchiwa.decorations.first() as Decoration.Text

        assertEquals((templateList + candidate).map { it.id }, templates.map { it.id })
        // 3.5 / 2.625 = 4/3
        assertEquals(originalText.offset.x * 4f / 3f, scaledText.offset.x, DELTA)
        assertEquals(originalText.offset.y * 4f / 3f, scaledText.offset.y, DELTA)
        assertEquals(4f * 4f / 3f, templates.last().savedUchiwa.overallBorderWidth, DELTA)
    }

    @Test
    fun getTemplateById_withCandidates_findsCandidate() = runTest {
        val repository = DefaultTemplateRepository(Optional.of(TemplateCandidates(listOf(candidate))), densitySource())

        assertEquals(candidate, repository.getTemplateById("candidate_1"))
    }

    @Test
    fun getTemplateById_higherDensity_returnsTemplateScaledToDensity() = runTest {
        val repository = DefaultTemplateRepository(Optional.empty(), densitySource(3.5f))
        val original = templateList.first()

        val scaled = requireNotNull(repository.getTemplateById(original.id))

        // 3.5 / 2.625 = 4/3
        assertEquals(original.savedUchiwa.overallBorderWidth * 4f / 3f, scaled.savedUchiwa.overallBorderWidth, DELTA)
    }

    @Test
    fun getTemplateById_withoutCandidates_doesNotFindCandidate() = runTest {
        val repository = DefaultTemplateRepository(Optional.empty(), densitySource())

        assertNull(repository.getTemplateById("candidate_1"))
    }

    private companion object {
        const val DELTA = 0.0001f
    }
}
