package com.fansauchiwa.preview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.fansauchiwa.ui.util.LocalHapticFeedbackEnabled
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SaveResultHapticEffectTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val performed = mutableListOf<HapticFeedbackType>()

    // 端末を振動させる代わりに、鳴らされた種類を記録する
    private val recordingHapticFeedback = object : HapticFeedback {
        override fun performHapticFeedback(hapticFeedbackType: HapticFeedbackType) {
            performed += hapticFeedbackType
        }
    }

    @Composable
    private fun WithRecordingHaptic(hapticEnabled: Boolean, content: @Composable () -> Unit) {
        CompositionLocalProvider(
            LocalHapticFeedback provides recordingHapticFeedback,
            LocalHapticFeedbackEnabled provides hapticEnabled,
            content = content
        )
    }

    @Test
    fun saveResultHapticEffect_saveSucceeded_performsConfirmOnce() {
        composeTestRule.setContent {
            WithRecordingHaptic(hapticEnabled = true) { SaveResultHapticEffect(saveSuccess = true) }
        }
        composeTestRule.waitForIdle()

        assertEquals(listOf(HapticFeedbackType.Confirm), performed)
    }

    @Test
    fun saveResultHapticEffect_saveFailed_performsRejectOnce() {
        composeTestRule.setContent {
            WithRecordingHaptic(hapticEnabled = true) { SaveResultHapticEffect(saveSuccess = false) }
        }
        composeTestRule.waitForIdle()

        assertEquals(listOf(HapticFeedbackType.Reject), performed)
    }

    @Test
    fun saveResultHapticEffect_noResult_performsNothing() {
        composeTestRule.setContent {
            WithRecordingHaptic(hapticEnabled = true) { SaveResultHapticEffect(saveSuccess = null) }
        }
        composeTestRule.waitForIdle()

        assertEquals(emptyList<HapticFeedbackType>(), performed)
    }

    @Test
    fun saveResultHapticEffect_hapticDisabledInSettings_performsNothing() {
        composeTestRule.setContent {
            WithRecordingHaptic(hapticEnabled = false) { SaveResultHapticEffect(saveSuccess = true) }
        }
        composeTestRule.waitForIdle()

        assertEquals(emptyList<HapticFeedbackType>(), performed)
    }

    @Test
    fun saveResultHapticEffect_recreatedWithSameResult_doesNotPerformAgain() {
        val restorationTester = StateRestorationTester(composeTestRule)
        restorationTester.setContent {
            WithRecordingHaptic(hapticEnabled = true) { SaveResultHapticEffect(saveSuccess = true) }
        }
        composeTestRule.waitForIdle()

        // ダークモードの切り替えなどで画面が作り直されたときと同じく、保存した状態から作り直す
        restorationTester.emulateSavedInstanceStateRestore()
        composeTestRule.waitForIdle()

        assertEquals(listOf(HapticFeedbackType.Confirm), performed)
    }

    @Test
    fun saveResultHapticEffect_resultClearedThenSavedAgain_performsAgain() {
        var saveSuccess by mutableStateOf<Boolean?>(true)
        composeTestRule.setContent {
            WithRecordingHaptic(hapticEnabled = true) { SaveResultHapticEffect(saveSuccess = saveSuccess) }
        }
        composeTestRule.waitForIdle()

        composeTestRule.runOnIdle { saveSuccess = null }
        composeTestRule.runOnIdle { saveSuccess = false }
        composeTestRule.waitForIdle()

        assertEquals(listOf(HapticFeedbackType.Confirm, HapticFeedbackType.Reject), performed)
    }

    @Test
    fun saveResultHapticEffect_screenNotResumed_waitsUntilResumed() {
        // 報酬獲得時の保存は広告の表示中に終わる。広告が前面にある間は、この画面は RESUMED ではない
        val lifecycleOwner = object : LifecycleOwner {
            val registry = LifecycleRegistry(this)
            override val lifecycle: Lifecycle get() = registry
        }
        composeTestRule.runOnUiThread { lifecycleOwner.registry.currentState = Lifecycle.State.STARTED }
        composeTestRule.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
                WithRecordingHaptic(hapticEnabled = true) { SaveResultHapticEffect(saveSuccess = true) }
            }
        }
        composeTestRule.waitForIdle()
        assertEquals(emptyList<HapticFeedbackType>(), performed)

        composeTestRule.runOnUiThread { lifecycleOwner.registry.currentState = Lifecycle.State.RESUMED }
        composeTestRule.waitForIdle()

        assertEquals(listOf(HapticFeedbackType.Confirm), performed)
    }
}
