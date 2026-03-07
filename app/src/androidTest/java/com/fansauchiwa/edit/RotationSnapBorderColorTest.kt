package com.fansauchiwa.edit

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.fansauchiwa.data.Decoration
import com.fansauchiwa.ui.theme.FansaUchiwaTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * 回転スナップに応じたボーダー色の切り替えを検証するUIテスト。
 *
 * - 回転角度が0, 90, 180, 270度（スナップポイント）のとき、ボーダー色は `primary`
 * - 回転角度がスナップポイント外（例: 45度）のとき、ボーダー色は `tertiary`
 *
 * ※ テストファーストで記述しているため、`BorderColorKey` や `getSelectionBorderColor` など
 *   未実装の要素を参照しています。実装後にテストが通るようになります。
 */
@RunWith(AndroidJUnit4::class)
class RotationSnapBorderColorTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    companion object {
        private const val TEXT_ITEM_TEST_TAG = "TextItemBorder"

        private fun createTextDecoration(rotation: Float): Decoration.Text =
            Decoration.Text(
                text = "テスト",
                id = "test-id",
                offset = Offset.Zero,
                rotation = rotation,
                scale = 1f,
                color = Color(0xFFFFFFFF),
                strokeColor = Color(0xFFFF0000),
                strokeWidth = 30f,
                width = 900,
                font = FontFamilies.ZEN_MARU_GOTHIC
            )

        private fun hasBorderColor(expectedColor: Color): SemanticsMatcher =
            SemanticsMatcher.expectValue(BorderColorKey, expectedColor)
    }

    // ---- スナップポイント（0, 90, 180, 270度）: primary ----

    @Test
    fun textItem_rotation0_borderColorIsPrimary() {
        var primaryColor = Color.Unspecified
        composeTestRule.setContent {
            FansaUchiwaTheme {
                primaryColor = MaterialTheme.colorScheme.primary
                TextItemWithBorderSemantics(
                    decoration = createTextDecoration(rotation = 0f),
                    isSelected = true
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEXT_ITEM_TEST_TAG)
            .assert(hasBorderColor(primaryColor))
    }

    @Test
    fun textItem_rotation90_borderColorIsPrimary() {
        var primaryColor = Color.Unspecified
        composeTestRule.setContent {
            FansaUchiwaTheme {
                primaryColor = MaterialTheme.colorScheme.primary
                TextItemWithBorderSemantics(
                    decoration = createTextDecoration(rotation = 90f),
                    isSelected = true
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEXT_ITEM_TEST_TAG)
            .assert(hasBorderColor(primaryColor))
    }

    @Test
    fun textItem_rotation180_borderColorIsPrimary() {
        var primaryColor = Color.Unspecified
        composeTestRule.setContent {
            FansaUchiwaTheme {
                primaryColor = MaterialTheme.colorScheme.primary
                TextItemWithBorderSemantics(
                    decoration = createTextDecoration(rotation = 180f),
                    isSelected = true
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEXT_ITEM_TEST_TAG)
            .assert(hasBorderColor(primaryColor))
    }

    @Test
    fun textItem_rotation270_borderColorIsPrimary() {
        var primaryColor = Color.Unspecified
        composeTestRule.setContent {
            FansaUchiwaTheme {
                primaryColor = MaterialTheme.colorScheme.primary
                TextItemWithBorderSemantics(
                    decoration = createTextDecoration(rotation = 270f),
                    isSelected = true
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEXT_ITEM_TEST_TAG)
            .assert(hasBorderColor(primaryColor))
    }

    // ---- スナップポイント外（45度）: tertiary ----

    @Test
    fun textItem_rotation45_borderColorIsTertiary() {
        var tertiaryColor = Color.Unspecified
        composeTestRule.setContent {
            FansaUchiwaTheme {
                tertiaryColor = MaterialTheme.colorScheme.secondary
                TextItemWithBorderSemantics(
                    decoration = createTextDecoration(rotation = 45f),
                    isSelected = true
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEXT_ITEM_TEST_TAG)
            .assert(hasBorderColor(tertiaryColor))
    }

    // ---- 非選択状態ではボーダーが表示されない ----

    @Test
    fun textItem_notSelected_borderNodeDoesNotExist() {
        composeTestRule.setContent {
            FansaUchiwaTheme {
                TextItemWithBorderSemantics(
                    decoration = createTextDecoration(rotation = 0f),
                    isSelected = false
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEXT_ITEM_TEST_TAG)
            .assertDoesNotExist()
    }

    // ---- 負の角度のスナップポイント（-90度 = 270度相当）: primary ----

    @Test
    fun textItem_rotationNegative90_borderColorIsPrimary() {
        var primaryColor = Color.Unspecified
        composeTestRule.setContent {
            FansaUchiwaTheme {
                primaryColor = MaterialTheme.colorScheme.primary
                TextItemWithBorderSemantics(
                    decoration = createTextDecoration(rotation = -90f),
                    isSelected = true
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEXT_ITEM_TEST_TAG)
            .assert(hasBorderColor(primaryColor))
    }

    // ---- 360度超のスナップポイント（360度 = 0度相当）: primary ----

    @Test
    fun textItem_rotation360_borderColorIsPrimary() {
        var primaryColor = Color.Unspecified
        composeTestRule.setContent {
            FansaUchiwaTheme {
                primaryColor = MaterialTheme.colorScheme.primary
                TextItemWithBorderSemantics(
                    decoration = createTextDecoration(rotation = 360f),
                    isSelected = true
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEXT_ITEM_TEST_TAG)
            .assert(hasBorderColor(primaryColor))
    }

    // ---- 360度超の非スナップポイント（405度 = 45度相当）: tertiary ----

    @Test
    fun textItem_rotation405_borderColorIsTertiary() {
        var tertiaryColor = Color.Unspecified
        composeTestRule.setContent {
            FansaUchiwaTheme {
                tertiaryColor = MaterialTheme.colorScheme.secondary
                TextItemWithBorderSemantics(
                    decoration = createTextDecoration(rotation = 405f),
                    isSelected = true
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEXT_ITEM_TEST_TAG)
            .assert(hasBorderColor(tertiaryColor))
    }
}

