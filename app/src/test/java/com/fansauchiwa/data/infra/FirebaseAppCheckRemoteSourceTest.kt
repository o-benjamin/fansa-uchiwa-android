package com.fansauchiwa.data.infra

import com.google.firebase.appcheck.AppCheckProviderFactory
import com.google.firebase.appcheck.FirebaseAppCheck
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class FirebaseAppCheckRemoteSourceTest {

    private lateinit var firebaseAppCheck: FirebaseAppCheck
    private lateinit var providerFactory: AppCheckProviderFactory
    private lateinit var source: FirebaseAppCheckRemoteSource

    @Before
    fun setUp() {
        firebaseAppCheck = mockk(relaxed = true)
        providerFactory = mockk()
        source = FirebaseAppCheckRemoteSource(firebaseAppCheck, providerFactory)
    }

    @Test
    fun installProviderIfNeeded_firstCall_installsFactoryWithoutAutoRefresh() {
        source.installProviderIfNeeded()

        verify(exactly = 1) { firebaseAppCheck.installAppCheckProviderFactory(providerFactory, false) }
    }

    @Test
    fun installProviderIfNeeded_calledTwice_installsOnlyOnce() {
        source.installProviderIfNeeded()
        source.installProviderIfNeeded()

        verify(exactly = 1) { firebaseAppCheck.installAppCheckProviderFactory(any(), any()) }
    }

    @Test
    fun installProviderIfNeeded_firstInstallFails_installsAgainOnNextCall() {
        var calls = 0
        every { firebaseAppCheck.installAppCheckProviderFactory(any(), any()) } answers {
            calls++
            if (calls == 1) throw IllegalStateException("install failed")
        }

        runCatching { source.installProviderIfNeeded() }
        source.installProviderIfNeeded()

        verify(exactly = 2) { firebaseAppCheck.installAppCheckProviderFactory(providerFactory, false) }
    }
}
