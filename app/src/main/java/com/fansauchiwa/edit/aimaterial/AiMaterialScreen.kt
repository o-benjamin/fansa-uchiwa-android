package com.fansauchiwa.edit.aimaterial

import android.app.Activity
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.fansauchiwa.R
import com.fansauchiwa.data.AiMaterialImage
import com.fansauchiwa.data.AiMaterialPayment
import com.fansauchiwa.data.AiMaterialRejectReason
import com.fansauchiwa.data.AiMaterialReportReason
import com.fansauchiwa.data.GrantedTickets
import com.fansauchiwa.data.TicketProduct
import com.fansauchiwa.ui.rememberTransparencyGridBrush
import com.fansauchiwa.ui.theme.FansaUchiwaTheme
import com.fansauchiwa.ui.util.FansaHapticType
import com.fansauchiwa.ui.util.rememberFansaHapticManager
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

// 回数券の購入の案内からリンクする。ページは `web/public/tokushoho.html`（Firebase Hosting）で、入口を開ける前に出す
private const val COMMERCIAL_TRANSACTIONS_URL = "https://fansauchiwa-578d22ff.web.app/tokushoho.html"

private val TicketExpiryFormatter =
    DateTimeFormatter.ofPattern("yyyy/MM/dd", Locale.JAPAN).withZone(ZoneId.systemDefault())

@Composable
fun AiMaterialScreen(
    onConfirm: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: AiMaterialViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.logScreenView()
        viewModel.grantUnconsumedPurchasesOnOpen()
    }

    LaunchedEffect(Unit) {
        viewModel.confirmEvent.collect { imageUri ->
            onConfirm(imageUri)
        }
    }

    val pickMedia =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) viewModel.updateSourceImage(uri)
        }

    AiMaterialScreenContent(
        uiState = uiState,
        onBack = onBack,
        onPromptChange = viewModel::updatePrompt,
        onPickSourceImage = {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        },
        onRemoveSourceImage = { viewModel.updateSourceImage(null) },
        onGenerate = viewModel::generate,
        onPurchase = { (context as? Activity)?.let(viewModel::purchase) },
        onDiscardResult = viewModel::discardResult,
        onConfirm = viewModel::confirm,
        onReport = viewModel::report
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AiMaterialScreenContent(
    uiState: AiMaterialUiState,
    onBack: () -> Unit,
    onPromptChange: (String) -> Unit,
    onPickSourceImage: () -> Unit,
    onRemoveSourceImage: () -> Unit,
    onGenerate: () -> Unit,
    onPurchase: () -> Unit,
    onDiscardResult: () -> Unit,
    onConfirm: () -> Unit,
    onReport: (AiMaterialReportReason) -> Unit
) {
    val generation = uiState.generation
    val isGenerating = generation is AiMaterialGeneration.Generating
    val isShowingResult = generation is AiMaterialGeneration.Succeeded
    var showLeaveResultDialog by remember { mutableStateOf(false) }
    val onBackClick = { if (isShowingResult) showLeaveResultDialog = true else onBack() }

    // 生成中に戻ると、回数だけ減って絵が残らない
    BackHandler(enabled = isGenerating) {}
    // 結果を見ている間に戻ると、回数を使った素材を置かずに捨てることになるので確かめる
    BackHandler(enabled = isShowingResult) { showLeaveResultDialog = true }

    if (showLeaveResultDialog) {
        AiMaterialLeaveResultDialog(
            onLeave = {
                showLeaveResultDialog = false
                onBack()
            },
            onDismiss = { showLeaveResultDialog = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.ai_material_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick, enabled = !isGenerating) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (generation is AiMaterialGeneration.Succeeded) {
                AiMaterialResult(
                    image = generation.image,
                    reportStatus = uiState.reportStatus,
                    onDiscardResult = onDiscardResult,
                    onConfirm = onConfirm,
                    onReport = onReport
                )
            } else {
                AiMaterialInput(
                    uiState = uiState,
                    isGenerating = isGenerating,
                    onPromptChange = onPromptChange,
                    onPickSourceImage = onPickSourceImage,
                    onRemoveSourceImage = onRemoveSourceImage,
                    onGenerate = onGenerate,
                    onPurchase = onPurchase
                )
            }
        }
    }
}

@Composable
private fun AiMaterialInput(
    uiState: AiMaterialUiState,
    isGenerating: Boolean,
    onPromptChange: (String) -> Unit,
    onPickSourceImage: () -> Unit,
    onRemoveSourceImage: () -> Unit,
    onGenerate: () -> Unit,
    onPurchase: () -> Unit
) {
    val hapticManager = rememberFansaHapticManager()

    OutlinedTextField(
        value = uiState.prompt,
        onValueChange = onPromptChange,
        modifier = Modifier.fillMaxWidth(),
        enabled = !isGenerating,
        label = { Text(text = stringResource(R.string.ai_material_prompt_label)) },
        placeholder = { Text(text = stringResource(R.string.ai_material_prompt_placeholder)) },
        isError = uiState.isPromptTooLong,
        supportingText = {
            Text(
                text = stringResource(
                    R.string.ai_material_prompt_counter,
                    uiState.promptLength,
                    AI_MATERIAL_MAX_PROMPT_LENGTH
                )
            )
        },
        minLines = 3
    )

    AiMaterialSourceImage(
        sourceImageUri = uiState.sourceImageUri,
        enabled = !isGenerating,
        onPick = onPickSourceImage,
        onRemove = onRemoveSourceImage
    )

    uiState.generation.messageResId()?.let { messageResId ->
        Text(
            text = stringResource(messageResId),
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium
        )
    }

    if (uiState.purchase != AiMaterialPurchase.Closed) {
        AiMaterialPurchasePanel(purchase = uiState.purchase, onPurchase = onPurchase)
    }

    uiState.addedTickets?.let { addedTickets ->
        Text(
            text = stringResource(
                R.string.ai_material_tickets_added,
                addedTickets.ticketsAdded,
                TicketExpiryFormatter.format(addedTickets.expiresAt)
            ),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.bodyMedium
        )
    }

    if (isGenerating) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp))
            Text(text = stringResource(R.string.ai_material_generating))
        }
    }

    Button(
        onClick = {
            hapticManager.perform(FansaHapticType.CONFIRM)
            onGenerate()
        },
        modifier = Modifier.fillMaxWidth(),
        enabled = uiState.canGenerate
    ) {
        Text(text = stringResource(R.string.ai_material_generate))
    }

    if (uiState.ticketsRemaining > 0) {
        Text(
            text = stringResource(R.string.ai_material_tickets_remaining, uiState.ticketsRemaining),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun AiMaterialPurchasePanel(
    purchase: AiMaterialPurchase,
    onPurchase: () -> Unit
) {
    val uriHandler = LocalUriHandler.current

    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.ai_material_purchase_title),
                style = MaterialTheme.typography.titleMedium
            )
            when (purchase) {
                AiMaterialPurchase.Closed -> Unit
                AiMaterialPurchase.Preparing -> Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Text(text = stringResource(R.string.ai_material_purchase_preparing))
                }
                AiMaterialPurchase.Unavailable -> Text(
                    text = stringResource(R.string.ai_material_purchase_unavailable),
                    style = MaterialTheme.typography.bodyMedium
                )
                is AiMaterialPurchase.Ready -> AiMaterialPurchaseOffer(
                    product = purchase.product,
                    problem = purchase.problem,
                    enabled = true,
                    onPurchase = onPurchase
                )
                // Play の購入の画面を出している間とサーバーが確かめている間は、二度押しで2回買わせない
                is AiMaterialPurchase.Purchasing -> AiMaterialPurchaseOffer(
                    product = purchase.product,
                    problem = null,
                    enabled = false,
                    onPurchase = onPurchase
                )
            }
            TextButton(onClick = { uriHandler.openUri(COMMERCIAL_TRANSACTIONS_URL) }) {
                Text(text = stringResource(R.string.ai_material_purchase_legal))
            }
        }
    }
}

@Composable
private fun AiMaterialPurchaseOffer(
    product: TicketProduct,
    problem: AiMaterialPurchaseProblem?,
    enabled: Boolean,
    onPurchase: () -> Unit
) {
    val hapticManager = rememberFansaHapticManager()

    Text(
        text = stringResource(
            R.string.ai_material_purchase_product,
            AI_MATERIAL_TICKETS_PER_PURCHASE,
            product.formattedPrice
        ),
        style = MaterialTheme.typography.bodyLarge
    )
    // アプリを消すと回数券が使えなくなることは、買う前に知らせる（#392 の (a)）
    listOf(
        stringResource(R.string.ai_material_purchase_note_valid, AI_MATERIAL_TICKET_VALID_MONTHS),
        stringResource(R.string.ai_material_purchase_note_refund),
        stringResource(R.string.ai_material_purchase_note_reinstall)
    ).forEach { note ->
        Text(text = note, style = MaterialTheme.typography.bodySmall)
    }
    Button(
        onClick = {
            hapticManager.perform(FansaHapticType.CONFIRM)
            onPurchase()
        },
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled
    ) {
        Text(text = stringResource(R.string.ai_material_purchase_buy, product.formattedPrice))
    }
    problem?.let {
        Text(
            text = stringResource(it.messageResId()),
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@StringRes
private fun AiMaterialPurchaseProblem.messageResId(): Int = when (this) {
    AiMaterialPurchaseProblem.PENDING -> R.string.ai_material_purchase_pending
    AiMaterialPurchaseProblem.REJECTED -> R.string.ai_material_purchase_rejected
    AiMaterialPurchaseProblem.PLAY_ERROR -> R.string.ai_material_purchase_play_error
    AiMaterialPurchaseProblem.FAILED -> R.string.ai_material_purchase_failed
}

@Composable
private fun AiMaterialSourceImage(
    sourceImageUri: Uri?,
    enabled: Boolean,
    onPick: () -> Unit,
    onRemove: () -> Unit
) {
    if (sourceImageUri == null) {
        OutlinedButton(
            onClick = onPick,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled
        ) {
            Text(text = stringResource(R.string.ai_material_source_image_pick))
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AsyncImage(
                model = sourceImageUri,
                contentDescription = stringResource(R.string.ai_material_source_image),
                modifier = Modifier.size(96.dp)
            )
            OutlinedButton(onClick = onRemove, enabled = enabled) {
                Text(text = stringResource(R.string.ai_material_source_image_remove))
            }
        }
    }
    Text(
        text = stringResource(R.string.ai_material_source_image_note),
        style = MaterialTheme.typography.bodySmall
    )
}

@Composable
private fun AiMaterialResult(
    image: AiMaterialImage,
    reportStatus: AiMaterialReportStatus,
    onDiscardResult: () -> Unit,
    onConfirm: () -> Unit,
    onReport: (AiMaterialReportReason) -> Unit
) {
    val hapticManager = rememberFansaHapticManager()
    var showReportDialog by remember { mutableStateOf(false) }

    if (showReportDialog) {
        AiMaterialReportDialog(
            initialReason = null,
            onSend = { reason ->
                showReportDialog = false
                onReport(reason)
            },
            onDismiss = { showReportDialog = false }
        )
    }

    AsyncImage(
        model = image.uri,
        contentDescription = stringResource(R.string.ai_material_result),
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .background(rememberTransparencyGridBrush())
    )

    if (!image.backgroundRemoved) {
        Text(
            text = stringResource(R.string.ai_material_background_not_removed),
            style = MaterialTheme.typography.bodyMedium
        )
    }

    Button(
        onClick = {
            hapticManager.perform(FansaHapticType.CONFIRM)
            onConfirm()
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = stringResource(R.string.ai_material_confirm))
    }
    OutlinedButton(onClick = onDiscardResult, modifier = Modifier.fillMaxWidth()) {
        Text(text = stringResource(R.string.ai_material_discard))
    }

    AiMaterialReportSection(
        reportStatus = reportStatus,
        onClick = { showReportDialog = true }
    )
}

// Google Play の AI 生成コンテンツのポリシーで、不快な生成物をアプリの中から報告できることが要る
@Composable
private fun AiMaterialReportSection(
    reportStatus: AiMaterialReportStatus,
    onClick: () -> Unit
) {
    when (reportStatus) {
        AiMaterialReportStatus.SENT -> Text(
            text = stringResource(R.string.ai_material_report_sent),
            style = MaterialTheme.typography.bodyMedium
        )
        AiMaterialReportStatus.LIMIT_REACHED -> Text(
            text = stringResource(R.string.ai_material_report_limit_reached),
            style = MaterialTheme.typography.bodyMedium
        )
        AiMaterialReportStatus.NOT_SENT,
        AiMaterialReportStatus.SENDING,
        AiMaterialReportStatus.FAILED -> {
            // 送っている間は押せない（同じ素材を2回送らない）
            TextButton(
                onClick = onClick,
                enabled = reportStatus != AiMaterialReportStatus.SENDING,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(R.string.ai_material_report))
            }
            if (reportStatus == AiMaterialReportStatus.FAILED) {
                Text(
                    text = stringResource(R.string.ai_material_report_failed),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun AiMaterialReportDialog(
    initialReason: AiMaterialReportReason?,
    onSend: (AiMaterialReportReason) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedReason by remember { mutableStateOf(initialReason) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.ai_material_report)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = stringResource(R.string.ai_material_report_message))
                Column(modifier = Modifier.selectableGroup()) {
                    AiMaterialReportReason.entries.forEach { reason ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = reason == selectedReason,
                                    onClick = { selectedReason = reason },
                                    role = Role.RadioButton
                                )
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            RadioButton(selected = reason == selectedReason, onClick = null)
                            Text(text = stringResource(reason.labelResId()))
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { selectedReason?.let(onSend) },
                enabled = selectedReason != null
            ) {
                Text(text = stringResource(R.string.ai_material_report_send))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.cancel))
            }
        }
    )
}

@StringRes
private fun AiMaterialReportReason.labelResId(): Int = when (this) {
    AiMaterialReportReason.INAPPROPRIATE -> R.string.ai_material_report_reason_inappropriate
    AiMaterialReportReason.REAL_PERSON -> R.string.ai_material_report_reason_real_person
    AiMaterialReportReason.RIGHTS -> R.string.ai_material_report_reason_rights
    AiMaterialReportReason.OTHER -> R.string.ai_material_report_reason_other
}

@Composable
private fun AiMaterialLeaveResultDialog(
    onLeave: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.ai_material_leave_result_title)) },
        text = { Text(text = stringResource(R.string.ai_material_leave_result_message)) },
        confirmButton = {
            TextButton(onClick = onLeave) {
                Text(text = stringResource(R.string.ai_material_leave_result_leave))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.cancel))
            }
        }
    )
}

@StringRes
private fun AiMaterialGeneration.messageResId(): Int? = when (this) {
    is AiMaterialGeneration.Rejected -> when (reason) {
        AiMaterialRejectReason.INVALID_PROMPT -> R.string.ai_material_error_invalid_prompt
        AiMaterialRejectReason.INVALID_IMAGE -> R.string.ai_material_error_invalid_image
        AiMaterialRejectReason.FACE_IN_SOURCE -> R.string.ai_material_error_face_in_source
        AiMaterialRejectReason.BLOCKED_NAME,
        AiMaterialRejectReason.MODERATION,
        AiMaterialRejectReason.SAFETY_BLOCKED -> R.string.ai_material_error_blocked
        AiMaterialRejectReason.NO_CREDITS -> R.string.ai_material_error_no_credits
        AiMaterialRejectReason.FREE_DAILY_LIMIT -> R.string.ai_material_error_free_daily_limit
        AiMaterialRejectReason.TOTAL_DAILY_LIMIT -> R.string.ai_material_error_total_daily_limit
        AiMaterialRejectReason.GENERATION_FAILED -> R.string.ai_material_error_generation_failed
        AiMaterialRejectReason.DISABLED,
        AiMaterialRejectReason.UNAUTHENTICATED -> R.string.ai_material_error_unavailable
    }
    AiMaterialGeneration.Failed -> R.string.ai_material_error_failed
    AiMaterialGeneration.Idle,
    AiMaterialGeneration.Generating,
    is AiMaterialGeneration.Succeeded -> null
}

@Preview(showBackground = true)
@Composable
private fun AiMaterialScreenIdlePreview() {
    AiMaterialScreenPreview(AiMaterialUiState(prompt = "笑顔の星"))
}

@Preview(showBackground = true)
@Composable
private fun AiMaterialScreenGeneratingPreview() {
    AiMaterialScreenPreview(
        AiMaterialUiState(prompt = "笑顔の星", generation = AiMaterialGeneration.Generating)
    )
}

@Preview(showBackground = true)
@Composable
private fun AiMaterialScreenRejectedPreview() {
    AiMaterialScreenPreview(
        AiMaterialUiState(
            prompt = "笑顔の星",
            generation = AiMaterialGeneration.Rejected(AiMaterialRejectReason.FREE_DAILY_LIMIT)
        )
    )
}

@Preview(showBackground = true)
@Composable
private fun AiMaterialScreenPurchasePreparingPreview() {
    AiMaterialScreenPreview(
        AiMaterialUiState(
            prompt = "笑顔の星",
            generation = AiMaterialGeneration.Rejected(AiMaterialRejectReason.NO_CREDITS),
            purchase = AiMaterialPurchase.Preparing
        )
    )
}

@Preview(showBackground = true)
@Composable
private fun AiMaterialScreenPurchaseReadyPreview() {
    AiMaterialScreenPreview(
        AiMaterialUiState(
            prompt = "笑顔の星",
            generation = AiMaterialGeneration.Rejected(AiMaterialRejectReason.NO_CREDITS),
            purchase = AiMaterialPurchase.Ready(PREVIEW_PRODUCT, problem = null)
        )
    )
}

@Preview(showBackground = true)
@Composable
private fun AiMaterialScreenPurchasePendingPreview() {
    AiMaterialScreenPreview(
        AiMaterialUiState(
            prompt = "笑顔の星",
            purchase = AiMaterialPurchase.Ready(PREVIEW_PRODUCT, AiMaterialPurchaseProblem.PENDING)
        )
    )
}

@Preview(showBackground = true)
@Composable
private fun AiMaterialScreenPurchasingPreview() {
    AiMaterialScreenPreview(
        AiMaterialUiState(
            prompt = "笑顔の星",
            purchase = AiMaterialPurchase.Purchasing(PREVIEW_PRODUCT)
        )
    )
}

@Preview(showBackground = true)
@Composable
private fun AiMaterialScreenPurchaseUnavailablePreview() {
    AiMaterialScreenPreview(
        AiMaterialUiState(
            prompt = "笑顔の星",
            generation = AiMaterialGeneration.Rejected(AiMaterialRejectReason.FREE_DAILY_LIMIT),
            purchase = AiMaterialPurchase.Unavailable
        )
    )
}

@Preview(showBackground = true)
@Composable
private fun AiMaterialScreenTicketsAddedPreview() {
    AiMaterialScreenPreview(
        AiMaterialUiState(
            prompt = "笑顔の星",
            ticketsRemaining = 5,
            addedTickets = GrantedTickets(
                ticketsAdded = 5,
                ticketsRemaining = 5,
                expiresAt = Instant.parse("2027-05-12T00:00:00Z"),
                alreadyGranted = false
            )
        )
    )
}

@Preview(showBackground = true)
@Composable
private fun AiMaterialScreenSucceededPreview() {
    AiMaterialScreenPreview(
        AiMaterialUiState(
            prompt = "笑顔の星",
            generation = AiMaterialGeneration.Succeeded(PREVIEW_IMAGE.copy(backgroundRemoved = false))
        )
    )
}

@Preview(showBackground = true)
@Composable
private fun AiMaterialScreenReportSendingPreview() {
    AiMaterialScreenPreview(
        AiMaterialUiState(
            prompt = "笑顔の星",
            generation = AiMaterialGeneration.Succeeded(PREVIEW_IMAGE),
            reportStatus = AiMaterialReportStatus.SENDING
        )
    )
}

@Preview(showBackground = true)
@Composable
private fun AiMaterialScreenReportSentPreview() {
    AiMaterialScreenPreview(
        AiMaterialUiState(
            prompt = "笑顔の星",
            generation = AiMaterialGeneration.Succeeded(PREVIEW_IMAGE),
            reportStatus = AiMaterialReportStatus.SENT
        )
    )
}

@Preview(showBackground = true)
@Composable
private fun AiMaterialScreenReportFailedPreview() {
    AiMaterialScreenPreview(
        AiMaterialUiState(
            prompt = "笑顔の星",
            generation = AiMaterialGeneration.Succeeded(PREVIEW_IMAGE),
            reportStatus = AiMaterialReportStatus.FAILED
        )
    )
}

@Preview(showBackground = true)
@Composable
private fun AiMaterialScreenReportLimitReachedPreview() {
    AiMaterialScreenPreview(
        AiMaterialUiState(
            prompt = "笑顔の星",
            generation = AiMaterialGeneration.Succeeded(PREVIEW_IMAGE),
            reportStatus = AiMaterialReportStatus.LIMIT_REACHED
        )
    )
}

@Preview
@Composable
private fun AiMaterialReportDialogPreview() {
    FansaUchiwaTheme {
        AiMaterialReportDialog(initialReason = null, onSend = {}, onDismiss = {})
    }
}

@Preview
@Composable
private fun AiMaterialReportDialogSelectedPreview() {
    FansaUchiwaTheme {
        AiMaterialReportDialog(
            initialReason = AiMaterialReportReason.REAL_PERSON,
            onSend = {},
            onDismiss = {}
        )
    }
}

@Composable
private fun AiMaterialScreenPreview(uiState: AiMaterialUiState) {
    FansaUchiwaTheme {
        AiMaterialScreenContent(
            uiState = uiState,
            onBack = {},
            onPromptChange = {},
            onPickSourceImage = {},
            onRemoveSourceImage = {},
            onGenerate = {},
            onPurchase = {},
            onDiscardResult = {},
            onConfirm = {},
            onReport = {}
        )
    }
}

private val PREVIEW_PRODUCT = TicketProduct(formattedPrice = "¥160")

private val PREVIEW_IMAGE = AiMaterialImage(
    uri = Uri.EMPTY,
    backgroundRemoved = true,
    payment = AiMaterialPayment.FREE,
    ticketsRemaining = 0,
    model = "gemini-3.1-flash-lite-image"
)
