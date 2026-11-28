package com.fansauchiwa.data.repository

import android.util.Log
import com.fansauchiwa.data.InAppMessageTarget
import com.fansauchiwa.data.infra.InAppMessagingDataSource
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class InAppMessagingRepositoryImplTest {

    private lateinit var dataSource: InAppMessagingDataSource
    private lateinit var repository: InAppMessagingRepositoryImpl

    @Before
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.w(any(), any<String>(), any()) } returns 0
        dataSource = mockk(relaxed = true)
        repository = InAppMessagingRepositoryImpl(dataSource)
    }

    @After
    fun tearDown() {
        unmockkStatic(Log::class)
    }

    @Test
    fun getClickedTargetStream_mapsActionUrlsToTargets() = runTest {
        every { dataSource.getClickedActionUrlStream() } returns
            flowOf("fansauchiwa://store", "https://example.com", "fansauchiwa://unknown")

        assertEquals(
            listOf(InAppMessageTarget.STORE, InAppMessageTarget.WEB, InAppMessageTarget.OTHER),
            repository.getClickedTargetStream().toList()
        )
    }

    @Test
    fun getClickedTargetStream_dataSourceFails_completesWithoutThrowing() = runTest {
        every { dataSource.getClickedActionUrlStream() } returns flow {
            emit("fansauchiwa://home")
            throw IllegalStateException("FirebaseApp is not initialized")
        }

        assertEquals(listOf(InAppMessageTarget.HOME), repository.getClickedTargetStream().toList())
    }

    @Test
    fun getImpressionStream_dataSourceFails_completesWithoutThrowing() = runTest {
        every { dataSource.getImpressionStream() } returns flow { throw IllegalStateException("boom") }

        assertEquals(emptyList<Unit>(), repository.getImpressionStream().toList())
    }

    @Test
    fun setMessagesSuppressed_dataSourceFails_doesNotThrow() {
        every { dataSource.setMessagesSuppressed(any()) } throws IllegalStateException("boom")

        repository.setMessagesSuppressed(true)

        verify(exactly = 1) { dataSource.setMessagesSuppressed(true) }
    }
}
