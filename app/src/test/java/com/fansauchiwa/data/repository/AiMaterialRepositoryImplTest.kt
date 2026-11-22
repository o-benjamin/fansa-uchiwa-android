package com.fansauchiwa.data.repository

import android.net.Uri
import com.fansauchiwa.data.AiMaterialFaceCheckException
import com.fansauchiwa.data.AiMaterialImage
import com.fansauchiwa.data.AiMaterialPayment
import com.fansauchiwa.data.AiMaterialRejectReason
import com.fansauchiwa.data.AiMaterialRejectedException
import com.fansauchiwa.data.AiMaterialReportReason
import com.fansauchiwa.data.AiMaterialSourceImage
import com.fansauchiwa.data.AiMaterialUnconfirmedException
import com.fansauchiwa.data.BackgroundRemovalException
import com.fansauchiwa.data.BackgroundRemovalFailureReason
import com.fansauchiwa.data.GeneratedAiMaterial
import com.fansauchiwa.data.infra.AiMaterialDataSource
import com.fansauchiwa.data.infra.AiMaterialImageDataSource
import com.fansauchiwa.data.infra.RemoteConfigDataSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AiMaterialRepositoryImplTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var imageProcessingRepository: ImageProcessingRepository
    private lateinit var dataSource: AiMaterialDataSource
    private lateinit var imageDataSource: AiMaterialImageDataSource
    private lateinit var crashReportingRepository: CrashReportingRepository
    private lateinit var remoteConfigDataSource: RemoteConfigDataSource
    private lateinit var repository: AiMaterialRepositoryImpl

    private val sourceUri = mockk<Uri>()
    private val generatedUri = mockk<Uri>()
    private val cutOutUri = mockk<Uri>()
    private val encodedSource = AiMaterialSourceImage("image/jpeg", "c3Jj")
    private val material = GeneratedAiMaterial("aW1n", "image/png", AiMaterialPayment.TICKET, 4, MODEL)

    @Before
    fun setUp() {
        authRepository = mockk()
        imageProcessingRepository = mockk()
        dataSource = mockk()
        imageDataSource = mockk()
        crashReportingRepository = mockk(relaxed = true)
        remoteConfigDataSource = mockk()
        coEvery { authRepository.prepareForServerCall() } returns "uid"
        coEvery { imageDataSource.encodeSourceImage(sourceUri) } returns encodedSource
        coEvery { imageDataSource.writeGeneratedImage(material) } returns generatedUri
        coEvery { imageProcessingRepository.removeBackground(generatedUri) } returns
            Result.success(cutOutUri)
        repository = AiMaterialRepositoryImpl(
            authRepository,
            imageProcessingRepository,
            dataSource,
            imageDataSource,
            crashReportingRepository,
            remoteConfigDataSource
        )
    }

    @Test
    fun generate_success_preparesAuthThenReturnsCutOutImage() = runTest {
        coEvery { dataSource.generateMaterial("星", null, REQUEST_ID) } returns material

        assertEquals(
            AiMaterialImage(cutOutUri, true, AiMaterialPayment.TICKET, 4, MODEL),
            repository.generate("星", null, REQUEST_ID).getOrThrow()
        )
        coVerifyOrder {
            authRepository.prepareForServerCall()
            dataSource.generateMaterial("星", null, REQUEST_ID)
        }
        coVerify(exactly = 0) { imageProcessingRepository.containsFace(any()) }
    }

    @Test
    fun generate_sourceWithoutFace_sendsEncodedImage() = runTest {
        coEvery { imageProcessingRepository.containsFace(sourceUri) } returns Result.success(false)
        coEvery { dataSource.generateMaterial("星", encodedSource, REQUEST_ID) } returns material

        assertTrue(repository.generate("星", sourceUri, REQUEST_ID).isSuccess)
        coVerify(exactly = 1) { dataSource.generateMaterial("星", encodedSource, REQUEST_ID) }
    }

    @Test
    fun generate_sourceWithFace_rejectsWithoutCallingServer() = runTest {
        coEvery { imageProcessingRepository.containsFace(sourceUri) } returns Result.success(true)

        val error = repository.generate("星", sourceUri, REQUEST_ID).exceptionOrNull()

        assertEquals(
            AiMaterialRejectReason.FACE_IN_SOURCE,
            (error as AiMaterialRejectedException).reason
        )
        coVerify(exactly = 0) { dataSource.generateMaterial(any(), any(), any()) }
    }

    @Test
    fun generate_faceCheckFails_returnsFaceCheckFailureWithoutCallingServer() = runTest {
        val cause = IOException("module")
        coEvery { imageProcessingRepository.containsFace(sourceUri) } returns Result.failure(cause)

        val error = repository.generate("星", sourceUri, REQUEST_ID).exceptionOrNull()

        assertTrue(error is AiMaterialFaceCheckException)
        assertEquals(cause, error?.cause)
        coVerify(exactly = 0) { dataSource.generateMaterial(any(), any(), any()) }
    }

    @Test
    fun generate_backgroundRemovalFails_returnsGeneratedImageAndRecordsCause() = runTest {
        val cause = BackgroundRemovalException(BackgroundRemovalFailureReason.NO_SUBJECT)
        coEvery { dataSource.generateMaterial("星", null, REQUEST_ID) } returns material
        coEvery { imageProcessingRepository.removeBackground(generatedUri) } returns
            Result.failure(cause)

        assertEquals(
            AiMaterialImage(generatedUri, false, AiMaterialPayment.TICKET, 4, MODEL),
            repository.generate("星", null, REQUEST_ID).getOrThrow()
        )
        verify(exactly = 1) { crashReportingRepository.recordException(cause) }
    }

    @Test
    fun generate_rejected_returnsFailureWithReason() = runTest {
        coEvery { dataSource.generateMaterial(any(), any(), any()) } throws
            AiMaterialRejectedException(AiMaterialRejectReason.FREE_DAILY_LIMIT)

        val error = repository.generate("星", null, REQUEST_ID).exceptionOrNull()

        assertEquals(
            AiMaterialRejectReason.FREE_DAILY_LIMIT,
            (error as AiMaterialRejectedException).reason
        )
    }

    @Test
    fun generate_authFails_returnsFailureWithoutCallingServer() = runTest {
        coEvery { authRepository.prepareForServerCall() } throws IOException("offline")

        assertTrue(repository.generate("星", null, REQUEST_ID).exceptionOrNull() is IOException)
        coVerify(exactly = 0) { dataSource.generateMaterial(any(), any(), any()) }
    }

    @Test
    fun generate_serverCallFails_returnsUnconfirmedWithCause() = runTest {
        // サーバーが作り終えて回数を減らしたあとに通信が切れたのかもしれない
        val cause = IOException("offline")
        coEvery { dataSource.generateMaterial(any(), any(), any()) } throws cause

        val error = repository.generate("星", null, REQUEST_ID).exceptionOrNull()

        assertTrue(error is AiMaterialUnconfirmedException)
        assertEquals(cause, error?.cause)
    }

    @Test
    fun generate_writeFails_returnsUnconfirmedWithoutCuttingOut() = runTest {
        val cause = IOException("disk full")
        coEvery { dataSource.generateMaterial("星", null, REQUEST_ID) } returns material
        coEvery { imageDataSource.writeGeneratedImage(material) } throws cause

        val error = repository.generate("星", null, REQUEST_ID).exceptionOrNull()

        assertTrue(error is AiMaterialUnconfirmedException)
        assertEquals(cause, error?.cause)
        coVerify(exactly = 0) { imageProcessingRepository.removeBackground(any()) }
    }

    @Test
    fun generate_taskCancelledWhileCallerActive_returnsUnconfirmed() = runTest {
        // Play 開発者サービスや Firebase の Task が取り消されたとき。画面を「作っています」のまま止めない
        coEvery { dataSource.generateMaterial(any(), any(), any()) } throws CancellationException("task")

        val error = repository.generate("星", null, REQUEST_ID).exceptionOrNull()

        assertTrue(error is AiMaterialUnconfirmedException)
        assertTrue(error?.cause is CancellationException)
    }

    @Test
    fun generate_callerCancelled_rethrows() = runTest {
        coEvery { dataSource.generateMaterial(any(), any(), any()) } coAnswers { awaitCancellation() }
        var result: Result<AiMaterialImage>? = null

        val job = launch { result = repository.generate("星", null, REQUEST_ID) }
        runCurrent()
        job.cancelAndJoin()

        assertTrue(job.isCancelled)
        assertNull(result)
    }

    @Test
    fun isOnDevice_returnsWhetherImageFileExists() = runTest {
        val image = AiMaterialImage(cutOutUri, true, AiMaterialPayment.TICKET, 4, MODEL)
        coEvery { imageDataSource.exists(cutOutUri) } returns false

        assertEquals(false, repository.isOnDevice(image))
    }

    @Test
    fun report_success_preparesAuthThenSendsReport() = runTest {
        coEvery { dataSource.reportMaterial(any(), any(), any()) } returns Unit

        assertTrue(repository.report("星", AiMaterialReportReason.RIGHTS, MODEL).isSuccess)
        coVerifyOrder {
            authRepository.prepareForServerCall()
            dataSource.reportMaterial("星", AiMaterialReportReason.RIGHTS, MODEL)
        }
    }

    @Test
    fun report_serverFails_returnsFailure() = runTest {
        coEvery { dataSource.reportMaterial(any(), any(), any()) } throws IOException("offline")

        assertTrue(
            repository.report("星", AiMaterialReportReason.OTHER, MODEL).exceptionOrNull() is IOException
        )
    }

    @Test
    fun report_authFails_returnsFailureWithoutCallingServer() = runTest {
        coEvery { authRepository.prepareForServerCall() } throws IOException("offline")

        assertTrue(repository.report("星", AiMaterialReportReason.OTHER, MODEL).isFailure)
        coVerify(exactly = 0) { dataSource.reportMaterial(any(), any(), any()) }
    }

    @Test
    fun getEntryEnabledStream_returnsRemoteConfigValue() = runTest {
        every { remoteConfigDataSource.getAiMaterialEnabledStream() } returns flowOf(true)

        assertTrue(repository.getEntryEnabledStream().first())
    }

    private companion object {
        const val MODEL = "gemini-3.1-flash-lite-image"
        const val REQUEST_ID = "request-1"
    }
}
