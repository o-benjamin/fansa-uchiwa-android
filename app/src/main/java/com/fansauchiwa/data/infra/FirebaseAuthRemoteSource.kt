package com.fansauchiwa.data.infra

import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject
import kotlinx.coroutines.tasks.await

class FirebaseAuthRemoteSource @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : AuthDataSource {

    override fun getCurrentUserId(): String? = firebaseAuth.currentUser?.uid

    override suspend fun signInAnonymously(): String {
        val user = firebaseAuth.signInAnonymously().await().user
        return checkNotNull(user) { "匿名ログインが成功したのに利用者がいない" }.uid
    }
}
