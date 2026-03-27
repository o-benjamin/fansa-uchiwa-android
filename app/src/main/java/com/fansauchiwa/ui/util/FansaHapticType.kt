package com.fansauchiwa.ui.util

/**
 * アプリ内で使用する触覚フィードバックの種類を定義する独自Enum。
 *
 * Compose標準の [androidx.compose.ui.hapticfeedback.HapticFeedbackType] を直接使用せず、
 * このEnumを介することで将来的な実装変更（独自Vibrator実装への切り替えなど）に対応できる。
 */
enum class FansaHapticType {
    CONFIRM,
    CONTEXT_CLICK,
    GESTURE_END,
    GESTURE_THRESHOLD_ACTIVATE,
    KEYBOARD_TAP,
    LONG_PRESS,
    REJECT,
    SEGMENT_FREQUENT_TICK,
    SEGMENT_TICK,
    TEXT_HANDLE_MOVE,
    TOGGLE_OFF,
    TOGGLE_ON,
    VIRTUAL_KEY,
}

