package com.fansauchiwa.home

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.mapSaver

data class NameTemplateDialogState(
    val templateId: String? = null,
    val lastName: String = "",
    val firstName: String = "",
    val honorific: String = "",
    val showFirstNameRequiredError: Boolean = false
) {
    companion object {
        val Saver: Saver<NameTemplateDialogState, Any> = mapSaver(
            save = {
                mapOf(
                    "templateId" to it.templateId,
                    "lastName" to it.lastName,
                    "firstName" to it.firstName,
                    "honorific" to it.honorific,
                    "showFirstNameRequiredError" to it.showFirstNameRequiredError
                )
            },
            restore = {
                NameTemplateDialogState(
                    templateId = it["templateId"] as String?,
                    lastName = it["lastName"] as? String ?: "",
                    firstName = it["firstName"] as? String ?: "",
                    honorific = it["honorific"] as? String ?: "",
                    showFirstNameRequiredError = it["showFirstNameRequiredError"] as? Boolean ?: false
                )
            }
        )
    }
}
