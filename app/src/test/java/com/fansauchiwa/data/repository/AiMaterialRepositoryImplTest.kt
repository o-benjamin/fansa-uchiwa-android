package com.fansauchiwa.data.repository

import com.fansauchiwa.data.AiMaterialPayment
import com.fansauchiwa.data.AiMaterialRejectReason
import com.fansauchiwa.data.AiMaterialRejectedException
import com.fansauchiwa.data.GeneratedAiMaterial
import com.fansauchiwa.data.infra.AiMaterialDataSource
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
    private lateinit var dataSource: AiMaterialDataSource
    private lateinit var repository: AiMaterialRepositoryImpl

    private val material = GeneratedAiMaterial("aW1n", "image/png", AiMaterialPayment.FREE, 0)

    @Before
    fun setUp() {
        authRepository = mockk()
        dataSource = mockk()
        coEvery { authRepository.prepareForServerCall() } returns "uid"
        repository = AiMaterialRepositoryImpl(authRepository, dataSource)
    }

    @Test
    fun generate_success_preparesAuthBeforeCallingServer() = runTest {
        coEvery { dataSource.generateMaterial("星", null) } returns material

        assertEquals(material, repository.generate("星", null).getOrThrow())
        coVerifyOrder {
            authRepository.prepareForServerCall()
            dataSource.generateMaterial("星", null)
        }
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
