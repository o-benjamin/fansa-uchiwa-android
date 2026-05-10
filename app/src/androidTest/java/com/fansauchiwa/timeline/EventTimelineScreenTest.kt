package com.fansauchiwa.timeline

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.fansauchiwa.R
import com.fansauchiwa.ui.theme.FansaUchiwaTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class EventTimelineScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun context() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun selectionMode_中央カードに対応したFabを表示する() {
        composeTestRule.setContent {
            FansaUchiwaTheme {
                EventTimelineContent(
                    events = sampleEvents(),
                    availableUchiwas = sampleEvents().flatMap { it.linkedUchiwas },
                    isSelectionMode = true,
                    currentUchiwaId = "uchiwa-1",
                    onBack = {},
                    onLinkEvent = {},
                    onSaveEvent = { _, _, _, _, _ -> },
                    onDeleteEvent = {},
                    onSendDebugReminder = {},
                    onUpdateEventThumbnail = { _, _ -> }
                )
            }
        }

        composeTestRule.onNodeWithText("東京ドームに持っていく").assertIsDisplayed()
    }

    @Test
    fun overflowMenu_編集と削除を表示する() {
        val moreOptions = context().getString(R.string.more_options)
        val edit = context().getString(R.string.edit)
        val delete = context().getString(R.string.delete)

        composeTestRule.setContent {
            FansaUchiwaTheme {
                EventTimelineContent(
                    events = sampleEvents(),
                    availableUchiwas = sampleEvents().flatMap { it.linkedUchiwas },
                    isSelectionMode = false,
                    currentUchiwaId = null,
                    onBack = {},
                    onLinkEvent = {},
                    onSaveEvent = { _, _, _, _, _ -> },
                    onDeleteEvent = {},
                    onSendDebugReminder = {},
                    onUpdateEventThumbnail = { _, _ -> }
                )
            }
        }

        composeTestRule.onNodeWithContentDescription(moreOptions).performClick()

        composeTestRule.onNodeWithText(edit).assertIsDisplayed()
        composeTestRule.onNodeWithText(delete).assertIsDisplayed()
    }

    private fun sampleEvents(): List<EventTimelineEventUiModel> {
        return listOf(
            EventTimelineEventUiModel(
                id = "event-1",
                name = "東京ドーム",
                eventDate = LocalDate.now().plusDays(3),
                remindEnabled = true,
                linkedUchiwas = listOf(sampleUchiwa("uchiwa-1"))
            ),
            EventTimelineEventUiModel(
                id = "event-2",
                name = "大阪城ホール",
                eventDate = LocalDate.now().plusDays(8),
                remindEnabled = true,
                linkedUchiwas = listOf(sampleUchiwa("uchiwa-2"))
            )
        )
    }

    private fun sampleUchiwa(id: String): EventTimelineUchiwaUiModel {
        return EventTimelineUchiwaUiModel(
            id = id,
            imagePath = null
        )
    }
}
