package com.fansauchiwa.data.repository

import com.fansauchiwa.data.Template
import com.fansauchiwa.data.TemplateCandidates
import com.fansauchiwa.data.templateList
import java.util.Optional
import javax.inject.Inject

interface TemplateRepository {
    suspend fun getTemplates(): List<Template>
    suspend fun getTemplateById(id: String): Template?
}

class DefaultTemplateRepository @Inject constructor(
    candidates: Optional<TemplateCandidates>
) : TemplateRepository {

    // 候補は既存のテンプレートの後ろに足す。既存の表示と順番は変えない
    private val templates = templateList + candidates.map { it.templates }.orElse(emptyList())

    override suspend fun getTemplates(): List<Template> {
        return templates
    }

    override suspend fun getTemplateById(id: String): Template? {
        return templates.find { it.id == id }
    }
}
