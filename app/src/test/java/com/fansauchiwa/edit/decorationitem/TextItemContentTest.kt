package com.fansauchiwa.edit.decorationitem

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TextItemContentTest {

    @Test
    fun supportsPukuPukuTextEffect_whenSdkIsBelowAndroid13_returnsFalse() {
        assertFalse(supportsPukuPukuTextEffect(32))
    }

    @Test
    fun supportsPukuPukuTextEffect_whenSdkIsAndroid13OrAbove_returnsTrue() {
        assertTrue(supportsPukuPukuTextEffect(33))
    }
}
