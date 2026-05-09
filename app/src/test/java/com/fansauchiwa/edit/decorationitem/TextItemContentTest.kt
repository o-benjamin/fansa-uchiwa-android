package com.fansauchiwa.edit.decorationitem

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TextItemContentTest {

    @Test
    fun supportsPukuPukuEffect_whenSdkIsBelowAndroid13_returnsFalse() {
        assertFalse(supportsPukuPukuEffect(32))
    }

    @Test
    fun supportsPukuPukuEffect_whenSdkIsAndroid13OrAbove_returnsTrue() {
        assertTrue(supportsPukuPukuEffect(33))
    }
}
