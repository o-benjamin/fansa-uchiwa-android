package com.fansauchiwa.data.infra

import com.google.firebase.inappmessaging.FirebaseInAppMessaging
import com.google.firebase.inappmessaging.FirebaseInAppMessagingClickListener
import com.google.firebase.inappmessaging.FirebaseInAppMessagingImpressionListener
import javax.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class FirebaseInAppMessagingRemoteSource @Inject constructor() : InAppMessagingDataSource {

    private val inAppMessaging by lazy { FirebaseInAppMessaging.getInstance() }

    override fun getImpressionStream(): Flow<Unit> = callbackFlow {
        val listener = FirebaseInAppMessagingImpressionListener { trySend(Unit) }
        inAppMessaging.addImpressionListener(listener)
        awaitClose { inAppMessaging.removeImpressionListener(listener) }
    }

    override fun getClickedActionUrlStream(): Flow<String> = callbackFlow {
        val listener = FirebaseInAppMessagingClickListener { _, action -> trySend(action.actionUrl.orEmpty()) }
        inAppMessaging.addClickListener(listener)
        awaitClose { inAppMessaging.removeClickListener(listener) }
    }

    override fun setMessagesSuppressed(suppressed: Boolean) {
        inAppMessaging.setMessagesSuppressed(suppressed)
    }
}
