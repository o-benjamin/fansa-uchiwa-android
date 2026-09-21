package com.fansauchiwa.ui.notification

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import com.fansauchiwa.data.repository.EventRepository
import com.fansauchiwa.data.source.EventEntity
import com.fansauchiwa.data.source.EventWithUchiwas
import com.fansauchiwa.data.source.FansaUchiwaEntity
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class UchiwaReminderWorkerTest {

    private class FakeEventRepository(
        private val events: List<EventWithUchiwas>
    ) : EventRepository {
        private val eventsFlow = MutableSharedFlow<List<EventWithUchiwas>>(replay = 1)
        var fetchCount = 0

        // 本物の EventRepositoryImpl と同じく、fetchEvents() するまで値を流さない
        override fun getEventsStream(): Flow<List<EventWithUchiwas>> = eventsFlow

        override suspend fun fetchEvents() {
            fetchCount++
            eventsFlow.emit(events)
        }

        override suspend fun upsertEvent(event: EventEntity) = Unit

        override suspend fun deleteEvent(eventId: String) = Unit

        override suspend fun linkUchiwaToEvent(eventId: String, uchiwaId: String) = Unit

        override suspend fun replaceEventUchiwas(eventId: String, uchiwaIds: List<String>) = Unit

        override suspend fun updateEventThumbnail(eventId: String, thumbnailImagePath: String?) =
            Unit
    }

    private class FakeWorkerFactory(
        private val eventRepository: EventRepository
    ) : WorkerFactory() {
        override fun createWorker(
            appContext: Context,
            workerClassName: String,
            workerParameters: WorkerParameters
        ): ListenableWorker = UchiwaReminderWorker(
            appContext,
            workerParameters,
            eventRepository,
            mockk(relaxed = true)
        )
    }

    private fun context(): Context = InstrumentationRegistry.getInstrumentation().targetContext

    private fun buildWorker(eventRepository: EventRepository): UchiwaReminderWorker =
        TestListenableWorkerBuilder<UchiwaReminderWorker>(context())
            .setWorkerFactory(FakeWorkerFactory(eventRepository))
            .build()

    private fun event(daysFromToday: Long) = EventWithUchiwas(
        event = EventEntity(
            id = "event-$daysFromToday",
            name = "event",
            eventDateEpochDay = LocalDate.now().plusDays(daysFromToday).toEpochDay(),
            remindEnabled = true
        ),
        uchiwas = listOf(
            FansaUchiwaEntity(
                id = "uchiwa-1",
                decorations = emptyList(),
                uchiwaColorValue = 0L,
                backgroundColorValue = 0L,
                overallBorderColorValue = 0L,
                overallBorderWidth = 0f,
                isOverallBorderPuffyEnabled = false
            )
        )
    )

    @Test
    fun doWork_NoEvents_FetchesOnceAndReturnsSuccess() {
        val repository = FakeEventRepository(events = emptyList())

        val result = runBlocking { withTimeout(5_000) { buildWorker(repository).doWork() } }

        assertEquals(ListenableWorker.Result.success(), result)
        assertEquals(1, repository.fetchCount)
    }

    @Test
    fun doWork_EventOutsideReminderRange_ReturnsSuccess() {
        val repository = FakeEventRepository(events = listOf(event(daysFromToday = 30)))

        val result = runBlocking { withTimeout(5_000) { buildWorker(repository).doWork() } }

        assertEquals(ListenableWorker.Result.success(), result)
        assertEquals(1, repository.fetchCount)
    }

    @Test
    fun doWork_EventWithinReminderRange_ReturnsSuccess() {
        val repository = FakeEventRepository(events = listOf(event(daysFromToday = 3)))

        val result = runBlocking { withTimeout(5_000) { buildWorker(repository).doWork() } }

        assertEquals(ListenableWorker.Result.success(), result)
        assertEquals(1, repository.fetchCount)
    }
}
