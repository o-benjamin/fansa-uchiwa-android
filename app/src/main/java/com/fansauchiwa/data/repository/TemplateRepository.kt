package com.fansauchiwa.data.repository

import com.fansauchiwa.data.Template
import com.fansauchiwa.data.templateList
import javax.inject.Inject

interface TemplateRepository {
    suspend fun getTemplates(): List<Template>
    suspend fun getTemplateById(id: String): Template?
}

class DefaultTemplateRepository @Inject constructor() : TemplateRepository {

    override suspend fun getTemplates(): List<Template> {
        return templateList
    }

    override suspend fun getTemplateById(id: String): Template? {
        return templateList.find { it.id == id }
    }
}

