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
}
