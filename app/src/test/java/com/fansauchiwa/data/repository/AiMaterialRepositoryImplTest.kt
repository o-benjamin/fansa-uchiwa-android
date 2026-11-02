package com.fansauchiwa.data.repository

import android.net.Uri
import com.fansauchiwa.data.AiMaterialImage
import com.fansauchiwa.data.AiMaterialPayment
import com.fansauchiwa.data.AiMaterialRejectReason
import com.fansauchiwa.data.AiMaterialRejectedException
import com.fansauchiwa.data.AiMaterialSourceImage
import com.fansauchiwa.data.BackgroundRemovalException
import com.fansauchiwa.data.BackgroundRemovalFailureReason
import com.fansauchiwa.data.GeneratedAiMaterial
import com.fansauchiwa.data.infra.AiMaterialDataSource
import com.fansauchiwa.data.infra.AiMaterialImageDataSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.mockk
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AiMaterialRepositoryImplTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var imageProcessingRepository: ImageProcessingRepository
    private lateinit var dataSource: AiMaterialDataSource
    private lateinit var imageDataSource: AiMaterialImageDataSource
    private lateinit var repository: AiMaterialRepositoryImpl

    private val sourceUri = mockk<Uri>()
    private val generatedUri = mockk<Uri>()
    private val cutOutUri = mockk<Uri>()
    private val encodedSource = AiMaterialSourceImage("image/jpeg", "c3Jj")
    private val material = GeneratedAiMaterial("aW1n", "image/png", AiMaterialPayment.TICKET, 4)

    @Before
    fun setUp() {
        authRepository = mockk()
        imageProcessingRepository = mockk()
        dataSource = mockk()
        imageDataSource = mockk()
        coEvery { authRepository.prepareForServerCall() } returns "uid"
        coEvery { imageDataSource.encodeSourceImage(sourceUri) } returns encodedSource
        coEvery { imageDataSource.writeGeneratedImage(material) } returns generatedUri
        coEvery { imageProcessingRepository.removeBackground(generatedUri) } returns
            Result.success(cutOutUri)
        repository = AiMaterialRepositoryImpl(
            authRepository,
            imageProcessingRepository,
            dataSource,
            imageDataSource
        )
    }

    @Test
    fun generate_success_preparesAuthThenReturnsCutOutImage() = runTest {
        coEvery { dataSource.generateMaterial("星", null) } returns material

        assertEquals(
            AiMaterialImage(cutOutUri, true, AiMaterialPayment.TICKET, 4),
            repository.generate("星", null).getOrThrow()
        )
        coVerifyOrder {
            authRepository.prepareForServerCall()
            dataSource.generateMaterial("星", null)
        }
        coVerify(exactly = 0) { imageProcessingRepository.containsFace(any()) }
    }

    @Test
    fun generate_sourceWithoutFace_sendsEncodedImage() = runTest {
        coEvery { imageProcessingRepository.containsFace(sourceUri) } returns Result.success(false)
        coEvery { dataSource.generateMaterial("星", encodedSource) } returns material

        assertTrue(repository.generate("星", sourceUri).isSuccess)
        coVerify(exactly = 1) { dataSource.generateMaterial("星", encodedSource) }
    }

    @Test
    fun generate_sourceWithFace_rejectsWithoutCallingServer() = runTest {
        coEvery { imageProcessingRepository.containsFace(sourceUri) } returns Result.success(true)

        val error = repository.generate("星", sourceUri).exceptionOrNull()

        assertEquals(
            AiMaterialRejectReason.FACE_IN_SOURCE,
            (error as AiMaterialRejectedException).reason
        )
        coVerify(exactly = 0) { dataSource.generateMaterial(any(), any()) }
    }

    @Test
    fun generate_faceCheckFails_returnsFailureWithoutCallingServer() = runTest {
        coEvery { imageProcessingRepository.containsFace(sourceUri) } returns
            Result.failure(IOException("module"))

        assertTrue(repository.generate("星", sourceUri).exceptionOrNull() is IOException)
        coVerify(exactly = 0) { dataSource.generateMaterial(any(), any()) }
    }

    @Test
    fun generate_backgroundRemovalFails_returnsGeneratedImage() = runTest {
        coEvery { dataSource.generateMaterial("星", null) } returns material
        coEvery { imageProcessingRepository.removeBackground(generatedUri) } returns
            Result.failure(BackgroundRemovalException(BackgroundRemovalFailureReason.NO_SUBJECT))

        assertEquals(
            AiMaterialImage(generatedUri, false, AiMaterialPayment.TICKET, 4),
            repository.generate("星", null).getOrThrow()
        )
    }

    @Test
    fun generate_rejected_returnsFailureWithReason() = runTest {
        coEvery { dataSource.generateMaterial(any(), any()) } throws
            AiMaterialRejectedException(AiMaterialRejectReason.FREE_DAILY_LIMIT)

        val error = repository.generate("星", null).exceptionOrNull()

        assertEquals(
            AiMaterialRejectReason.FREE_DAILY_LIMIT,
            (error as AiMaterialRejectedException).reason
        )
    }

    @Test
    fun generate_authFails_returnsFailureWithoutCallingServer() = runTest {
        coEvery { authRepository.prepareForServerCall() } throws IOException("offline")

        assertTrue(repository.generate("星", null).exceptionOrNull() is IOException)
        coVerify(exactly = 0) { dataSource.generateMaterial(any(), any()) }
    }

    @Test(expected = CancellationException::class)
    fun generate_cancelled_rethrows() = runTest {
        coEvery { dataSource.generateMaterial(any(), any()) } throws CancellationException("cancel")

        repository.generate("星", null)
    }
}
