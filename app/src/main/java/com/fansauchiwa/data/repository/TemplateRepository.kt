package com.fansauchiwa.data.repository

import com.fansauchiwa.data.Template
import com.fansauchiwa.data.TemplateCandidates
import com.fansauchiwa.data.infra.DisplayDensityDataSource
import com.fansauchiwa.data.scaledToDensity
import com.fansauchiwa.data.templateList
import kotlinx.coroutines.flow.first
import java.util.Optional
import javax.inject.Inject

/** テンプレートは、端末の密度に合わせて位置と縁の太さを直して返す（[scaledToDensity]） */
interface TemplateRepository {
    suspend fun getTemplates(): List<Template>
    suspend fun getTemplateById(id: String): Template?
}

class DefaultTemplateRepository @Inject constructor(
    candidates: Optional<TemplateCandidates>,
    private val displayDensityDataSource: DisplayDensityDataSource
) : TemplateRepository {

    // 候補は既存のテンプレートの後ろに足す。既存の表示と順番は変えない
    private val templates = templateList + candidates.map { it.templates }.orElse(emptyList())

    override suspend fun getTemplates(): List<Template> {
        val density = displayDensityDataSource.getDensityStream().first()
        return templates.map { it.scaledToDensity(density) }
    }

    override suspend fun getTemplateById(id: String): Template? {
        val density = displayDensityDataSource.getDensityStream().first()
        return templates.find { it.id == id }?.scaledToDensity(density)
    }
}
