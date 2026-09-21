package com.fansauchiwa

import com.fansauchiwa.data.DecorationColors
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FansaUchiwaNavigationTest {

    @Test
    fun editDestinationCreateRoute_encodesSerializableInputArg() {
        val inputArg = EditScreenInputArg(
            uchiwaId = "uchiwa-1",
            templateId = "template_1",
            templateMainColor = DecorationColors.PINK,
            lastName = "佐藤",
            firstName1 = "勝",
            firstName2 = "利 太",
            honorific = "くん&ちゃん"
        )
        val route = EditDestination.createRoute(inputArg)
        assertTrue(route.startsWith("edit?inputArg="))

        val encodedArg = route.substringAfter("inputArg=")
        assertEquals(inputArg, EditScreenInputArg.fromRouteArgument(encodedArg))
    }

    // region フォント計測（#242）

    @Test
    fun previewDestinationCreateRoute_withFinalFontName_includesAllArgs() {
        val route = PreviewDestination.createRoute(
            imagePath = "/data/masterpiece.png",
            fontSwitchCount = 12,
            finalFontName = "KEI_FONT",
            editStartTimeMillis = 1_700_000_000_000L
        )

        assertTrue(route.startsWith("preview//data/masterpiece.png?"))
        assertTrue(route.contains("fontSwitchCount=12"))
        assertTrue(route.contains("finalFontName=KEI_FONT"))
        assertTrue(route.contains("editStartTimeMillis=1700000000000"))
    }

    @Test
    fun previewDestinationCreateRoute_finalFontNameNull_omitsFinalFontNameArg() {
        val route = PreviewDestination.createRoute(
            imagePath = "/data/masterpiece.png",
            fontSwitchCount = 0,
            finalFontName = null,
            editStartTimeMillis = 0L
        )

        assertTrue(route.contains("fontSwitchCount=0"))
        assertTrue(!route.contains("finalFontName="))
    }
}
