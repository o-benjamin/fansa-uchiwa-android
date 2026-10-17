package com.fansauchiwa.di

import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import org.junit.Assert.assertSame
import org.junit.Test

/**
 * ユニットテストは debug ビルドで動くので、debug 用の provider が選ばれていることを確かめる
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
