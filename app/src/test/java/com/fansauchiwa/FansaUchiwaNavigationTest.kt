package com.fansauchiwa

import com.fansauchiwa.data.DecorationColors
import org.junit.Assert.assertEquals
import org.junit.Test

class FansaUchiwaNavigationTest {

    @Test
    fun editDestinationCreateRoute_encodesNameTemplateQueryArguments() {
        val route = EditDestination.createRoute(
            EditScreenInputArg(
                uchiwaId = "uchiwa-1",
                templateId = "template_1",
                templateMainColor = DecorationColors.PINK,
                lastName = "佐藤",
                firstName1 = "勝",
                firstName2 = "利 太",
                honorific = "くん&ちゃん"
            )
        )

        assertEquals(
            "edit?uchiwaId=uchiwa-1&templateId=template_1&templateMainColor=PINK&lastName=%E4%BD%90%E8%97%A4&firstName1=%E5%8B%9D&firstName2=%E5%88%A9%20%E5%A4%AA&honorific=%E3%81%8F%E3%82%93%26%E3%81%A1%E3%82%83%E3%82%93",
            route
        )
    }
}
