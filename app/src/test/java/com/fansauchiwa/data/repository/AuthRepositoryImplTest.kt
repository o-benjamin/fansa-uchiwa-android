package com.fansauchiwa.data.repository

import com.fansauchiwa.data.infra.AppCheckDataSource
import com.fansauchiwa.data.infra.AuthDataSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.io.IOException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AuthRepositoryImplTest {

    private lateinit var authDataSource: AuthDataSource
    private lateinit var appCheckDataSource: AppCheckDataSource
    private lateinit var repository: AuthRepositoryImpl

    @Before
    fun setUp() {
        authDataSource = mockk()
        appCheckDataSource = mockk(relaxed = true)
        repository = AuthRepositoryImpl(authDataSource, appCheckDataSource)
    }

    @Test
    fun prepareForServerCall_alreadySignedIn_returnsCurrentUserIdWithoutSigningIn() = runTest {
        every { authDataSource.getCurrentUserId() } returns "uid-existing"

        assertEquals("uid-existing", repository.prepareForServerCall())
        coVerify(exactly = 0) { authDataSource.signInAnonymously() }
    }

    @Test
    fun prepareForServerCall_notSignedIn_signsInAnonymouslyAndReturnsNewUserId() = runTest {
        every { authDataSource.getCurrentUserId() } returns null
        coEvery { authDataSource.signInAnonymously() } returns "uid-new"

        assertEquals("uid-new", repository.prepareForServerCall())
    }

    @Test
    fun prepareForServerCall_notSignedIn_installsAppCheckBeforeSigningIn() = runTest {
        every { authDataSource.getCurrentUserId() } returns null
        coEvery { authDataSource.signInAnonymously() } returns "uid-new"

        repository.prepareForServerCall()

        coVerifyOrder {
            appCheckDataSource.installProviderIfNeeded()
            authDataSource.signInAnonymously()
        }
    }

    @Test
    fun prepareForServerCall_alreadySignedIn_stillInstallsAppCheck() = runTest {
        every { authDataSource.getCurrentUserId() } returns "uid-existing"

        repository.prepareForServerCall()

        verify(exactly = 1) { appCheckDataSource.installProviderIfNeeded() }
    }

    @Test
    fun prepareForServerCall_signInFails_throwsAndSucceedsOnRetry() = runTest {
        every { authDataSource.getCurrentUserId() } returns null
        coEvery { authDataSource.signInAnonymously() } throws IOException("offline") andThen "uid-new"

        val failure = runCatching { repository.prepareForServerCall() }.exceptionOrNull()

        assertTrue(failure is IOException)
        assertEquals("uid-new", repository.prepareForServerCall())
    }

    @Test
    fun prepareForServerCall_calledConcurrently_signsInOnlyOnce() = runTest {
        var currentUserId: String? = null
        val signInStarted = CompletableDeferred<Unit>()
        val finishSignIn = CompletableDeferred<Unit>()
        every { authDataSource.getCurrentUserId() } answers { currentUserId }
        coEvery { authDataSource.signInAnonymously() } coAnswers {
            signInStarted.complete(Unit)
            finishSignIn.await()
            "uid-new".also { currentUserId = it }
        }

        val first = async { repository.prepareForServerCall() }
        val second = async { repository.prepareForServerCall() }
        signInStarted.await()
        finishSignIn.complete(Unit)

        assertEquals(listOf("uid-new", "uid-new"), listOf(first, second).awaitAll())
        coVerify(exactly = 1) { authDataSource.signInAnonymously() }
    }
}
