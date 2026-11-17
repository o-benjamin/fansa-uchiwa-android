package com.fansauchiwa.data.infra

import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.firebase.installations.FirebaseInstallations
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import java.io.IOException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class FirebaseInstallationsRemoteSourceTest {

    private val firebaseInstallations: FirebaseInstallations = mockk()
    private val source = FirebaseInstallationsRemoteSource(firebaseInstallations)

    @Before
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.w(any(), any<String>(), any()) } returns 0
    }

    @After
    fun tearDown() {
        unmockkStatic(Log::class)
    }

    @Test
    fun getInstallationIdStream_idAvailable_emitsId() = runTest {
        every { firebaseInstallations.id } returns Tasks.forResult("test-installation-id")

        assertEquals("test-installation-id", source.getInstallationIdStream().first())
    }

    @Test
    fun getInstallationIdStream_taskFails_emitsNull() = runTest {
        every { firebaseInstallations.id } returns Tasks.forException(IOException("offline"))

        assertNull(source.getInstallationIdStream().first())
    }
}
