package com.fansauchiwa.di

import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import org.junit.Assert.assertSame
import org.junit.Test

/**
 * debug 用の provider が選ばれていることを確かめる。release には debug 用の provider がないので、debug のユニットテスト（`src/testDebug`）にだけ置く
 */
class AppCheckProviderModuleTest {

    @Test
    fun provideAppCheckProviderFactory_debugBuild_returnsDebugProvider() {
        assertSame(
            DebugAppCheckProviderFactory.getInstance(),
            AppCheckProviderModule.provideAppCheckProviderFactory()
        )
    }
}
