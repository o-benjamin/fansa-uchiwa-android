package com.fansauchiwa.settings

sealed interface SettingsAction {
    class ToggleHapticFeedback(val enabled: Boolean) : SettingsAction
}
