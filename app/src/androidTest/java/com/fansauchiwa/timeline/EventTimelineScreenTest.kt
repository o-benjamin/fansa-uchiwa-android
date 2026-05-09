package com.fansauchiwa.timeline

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toColorLong
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.fansauchiwa.R
import com.fansauchiwa.data.Decoration
import com.fansauchiwa.data.source.EventEntity
import com.fansauchiwa.data.source.EventWithUchiwas
import com.fansauchiwa.data.source.FansaUchiwaEntity
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
                    isSelectionMode = true,
                    onBack = {},
                    onLinkEvent = {},
                    onSaveEvent = { _, _, _, _ -> },
                    onDeleteEvent = {}
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
                    isSelectionMode = false,
                    onBack = {},
                    onLinkEvent = {},
                    onSaveEvent = { _, _, _, _ -> },
                    onDeleteEvent = {}
                )
            }
        }

        composeTestRule.onNodeWithContentDescription(moreOptions).performClick()

        composeTestRule.onNodeWithText(edit).assertIsDisplayed()
        composeTestRule.onNodeWithText(delete).assertIsDisplayed()
    }

    private fun sampleEvents(): List<EventWithUchiwas> {
        return listOf(
            EventWithUchiwas(
                event = EventEntity(
                    id = "event-1",
                    name = "東京ドーム",
                    eventDateEpochDay = LocalDate.now().plusDays(3).toEpochDay()
                ),
                uchiwas = listOf(sampleUchiwa("uchiwa-1"))
            ),
            EventWithUchiwas(
                event = EventEntity(
                    id = "event-2",
                    name = "大阪城ホール",
                    eventDateEpochDay = LocalDate.now().plusDays(8).toEpochDay()
                ),
                uchiwas = listOf(sampleUchiwa("uchiwa-2"))
            )
        )
    }

    private fun sampleUchiwa(id: String): FansaUchiwaEntity {
        return FansaUchiwaEntity(
            id = id,
            decorations = emptyList<Decoration>(),
            uchiwaColorValue = Color.White.toColorLong(),
            backgroundColorValue = Color.White.toColorLong()
        )
    }
}
