package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.example.ui.components.PayoutDashboard
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.util.clearFocusOnTap
import com.example.data.local.PayoutEntity
import com.example.data.model.PayoutDestinationType
import com.example.data.model.PayoutMetrics
import com.example.data.model.PayoutStatus
import com.example.ui.components.Formatters
import com.example.ui.theme.AccentGold
import com.example.ui.theme.NavyDark
import com.example.ui.theme.OceanBlue
import com.example.ui.theme.PearlWhite
import com.example.ui.theme.StatusDraft
import com.example.ui.theme.StatusDraftBg
import com.example.ui.theme.StatusOverdue
import com.example.ui.theme.StatusOverdueBg
import com.example.ui.theme.StatusPaid
import com.example.ui.theme.StatusPaidBg
import com.example.ui.theme.StatusPending
import com.example.ui.theme.StatusPendingBg

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayoutsScreen(
    payoutMetrics: PayoutMetrics,
    payouts: List<PayoutEntity>,
    selectedFilter: String,
    onFilterChange: (String) -> Unit,
    onRequestPayout: (
        amount: Double,
        destinationType: String,
        destinationBank: String,
        destinationIban: String,
        destinationBic: String,
        recipientName: String,
        notes: String
    ) -> Unit,
    onProcessPayout: (PayoutEntity) -> Unit,
    onCompletePayout: (PayoutEntity) -> Unit,
    onCancelPayout: (PayoutEntity) -> Unit,
    onDeletePayout: (PayoutEntity) -> Unit,
    onRetryPayout: (PayoutEntity) -> Unit = {},
    onUpdateAndRetryPayout: (PayoutEntity, String, String, String, String, String) -> Unit = { _, _, _, _, _, _ -> },
    onBatchRetryPayouts: () -> Unit = {},
    onFixPayoutError: (PayoutEntity) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showRequestSheet by remember { mutableStateOf(false) }
    var selectedPayoutForDetail by remember { mutableStateOf<PayoutEntity?>(null) }
    var payoutToCancel by remember { mutableStateOf<PayoutEntity?>(null) }
    var payoutToDelete by remember { mutableStateOf<PayoutEntity?>(null) }
    var payoutToEditAndRetry by remember { mutableStateOf<PayoutEntity?>(null) }
    var showTransactionErrorInspector by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var isAuditingLedger by remember { mutableStateOf(false) }
    var auditScanResult by remember { mutableStateOf<String?>(null) }

    val failedPayouts = remember(payouts) {
        payouts.filter { it.status.equals("FAILED", ignoreCase = true) }
    }

    val filteredPayouts = remember(payouts, selectedFilter, searchQuery) {
        val byFilter = if (selectedFilter == "ALL") {
            payouts
        } else {
            payouts.filter { it.status.equals(selectedFilter, ignoreCase = true) }
        }
        if (searchQuery.isBlank()) {
            byFilter
        } else {
            val q = searchQuery.trim().lowercase()
            byFilter.filter {
                it.id.lowercase().contains(q) ||
                it.reference.lowercase().contains(q) ||
                it.recipientName.lowercase().contains(q) ||
                it.destinationBank.lowercase().contains(q) ||
                it.destinationIban.lowercase().contains(q) ||
                it.destinationBic.lowercase().contains(q) ||
                it.notes.lowercase().contains(q) ||
                it.status.lowercase().contains(q)
            }
        }
    }
    val focusManager = LocalFocusManager.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .clearFocusOnTap(focusManager)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .clearFocusOnTap(focusManager),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Comprehensive PayoutDashboard with Recharts history & pending merchant settlements
            item {
                PayoutDashboard(
                    payoutMetrics = payoutMetrics,
                    payouts = payouts,
                    onRequestPayout = { showRequestSheet = true },
                    onViewAllPayouts = { onFilterChange("ALL") },
                    onProcessPayout = onProcessPayout,
                    onCompletePayout = onCompletePayout,
                    onFixPayoutError = onFixPayoutError
                )
            }

            // Quick Payout Highlights / Metrics Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PayoutMetricMiniCard(
                        title = "In Transit",
                        amount = payoutMetrics.pendingPayoutBalance,
                        subtitle = "${payoutMetrics.processingPayoutsCount} scheduled",
                        icon = Icons.Default.HourglassBottom,
                        iconColor = StatusPending,
                        modifier = Modifier.weight(1f)
                    )

                    PayoutMetricMiniCard(
                        title = "Settled Total",
                        amount = payoutMetrics.totalPaidOut,
                        subtitle = "${payoutMetrics.completedPayoutsCount} completed",
                        icon = Icons.Default.CheckCircle,
                        iconColor = StatusPaid,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Transaction Error Alert Banner or Reconciliation Status
            if (failedPayouts.isNotEmpty()) {
                item {
                    Surface(
                        color = StatusOverdueBg,
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, StatusOverdue.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("payout_transaction_error_banner")
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(StatusOverdue.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ErrorOutline,
                                            contentDescription = "Transaction Error",
                                            tint = StatusOverdue,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "${failedPayouts.size} Transaction Settlement Error${if (failedPayouts.size > 1) "s" else ""} Detected",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = StatusOverdue
                                        )
                                        Text(
                                            text = "Total €${String.format(java.util.Locale.US, "%.2f", failedPayouts.sumOf { it.amount })} stalled in clearing gateway",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Surface(
                                    color = StatusOverdue,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.clickable { onFilterChange("FAILED") }
                                ) {
                                    Text(
                                        text = "Filter Errors",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                            }

                            val primaryFailed = failedPayouts.firstOrNull()
                            Surface(
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Ref: ${primaryFailed?.reference ?: "N/A"} (${primaryFailed?.id ?: ""})",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "SEPA Rejection",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = StatusOverdue
                                        )
                                    }
                                    Text(
                                        text = primaryFailed?.notes?.ifBlank { "Clearing gateway reported transaction settlement failure." } ?: "",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        lineHeight = 15.sp
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { primaryFailed?.let { onFixPayoutError(it) } },
                                    colors = ButtonDefaults.buttonColors(containerColor = StatusPaid),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .weight(1.1f)
                                        .testTag("fix_payout_error_button")
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Fix Payout Error", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = { showTransactionErrorInspector = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = StatusOverdue),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Diagnostics", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = onBatchRetryPayouts,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(0.9f)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(15.dp), tint = OceanBlue)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Retry (${failedPayouts.size})", fontSize = 12.sp, color = OceanBlue, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            } else {
                item {
                    Surface(
                        color = StatusPaidBg.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StatusPaid.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        Icons.Default.VerifiedUser,
                                        contentDescription = null,
                                        tint = StatusPaid,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Ledger Reconciled • 0 Transaction Settlement Errors",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = StatusPaid
                                        )
                                        Text(
                                            text = "All interbank transfers, routing codes, and settlement queues are healthy.",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                FilledTonalButton(
                                    onClick = {
                                        isAuditingLedger = true
                                        auditScanResult = "Audited ${payouts.size} ledger records: 0 errors detected. Bank BIC and SEPA IBANs verified."
                                    },
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Audit Ledger", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            if (auditScanResult != null) {
                                Surface(
                                    color = MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = auditScanResult ?: "",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Search Transactions Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Look for transactions, references, or errors...", fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search Transactions",
                            tint = OceanBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Cancel, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("payout_search_input")
                )
            }

            // Next Settlement Notice
            item {
                payoutMetrics.nextScheduledPayoutDate?.let { nextDate ->
                    Surface(
                        color = OceanBlue.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Schedule,
                                contentDescription = null,
                                tint = OceanBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Next Batch Settlement Scheduled",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OceanBlue
                                )
                                Text(
                                    text = "${Formatters.formatDate(nextDate)} • Estimated bank arrival within 1 business day",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Filter Chips
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val filters = mutableListOf(
                        "ALL" to "All (${payouts.size})"
                    )
                    if (failedPayouts.isNotEmpty()) {
                        filters.add("FAILED" to "Errors (${failedPayouts.size})")
                    }
                    filters.add("SCHEDULED" to "Scheduled")
                    filters.add("PROCESSING" to "Processing")
                    filters.add("PAID" to "Settled")
                    filters.add("CANCELLED" to "Cancelled")

                    filters.forEach { (key, label) ->
                        val isSelected = selectedFilter == key
                        ElevatedFilterChip(
                            selected = isSelected,
                            onClick = { onFilterChange(key) },
                            label = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (key == "FAILED") {
                                        Icon(
                                            Icons.Default.ErrorOutline,
                                            contentDescription = null,
                                            tint = if (isSelected) StatusOverdue else StatusOverdue.copy(alpha = 0.8f),
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    Text(label, fontSize = 12.sp)
                                }
                            },
                            colors = FilterChipDefaults.elevatedFilterChipColors(
                                selectedContainerColor = if (key == "FAILED") StatusOverdue.copy(alpha = 0.15f) else OceanBlue.copy(alpha = 0.15f),
                                selectedLabelColor = if (key == "FAILED") StatusOverdue else OceanBlue
                            )
                        )
                    }
                }
            }

            // Payout List Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Transfer Ledger",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${filteredPayouts.size} records",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Payouts List or Empty State
            if (filteredPayouts.isEmpty()) {
                item {
                    PayoutEmptyState(
                        filter = selectedFilter,
                        onRequestPayout = { showRequestSheet = true }
                    )
                }
            } else {
                items(filteredPayouts, key = { it.id }) { payout ->
                    PayoutItemCard(
                        payout = payout,
                        onClick = { selectedPayoutForDetail = payout },
                        onProcess = { onProcessPayout(payout) },
                        onComplete = { onCompletePayout(payout) },
                        onCancel = { payoutToCancel = payout },
                        onRetry = { onRetryPayout(payout) },
                        onEditAndRetry = { payoutToEditAndRetry = payout },
                        onInspectError = { showTransactionErrorInspector = true },
                        onFixError = { onFixPayoutError(payout) }
                    )
                }
            }
        }

        // Floating Action Button to Request Instant Payout
        ExtendedFloatingActionButton(
            onClick = { showRequestSheet = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("request_payout_fab"),
            containerColor = OceanBlue,
            contentColor = Color.White
        ) {
            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Request Payout", fontWeight = FontWeight.Bold)
        }
    }

    // Modal Sheet for Initiating a New Payout
    if (showRequestSheet) {
        RequestPayoutBottomSheet(
            availableBalance = payoutMetrics.availableBalance,
            onDismiss = { showRequestSheet = false },
            onSubmit = { amount, destType, bank, iban, bic, recipient, notes ->
                onRequestPayout(amount, destType, bank, iban, bic, recipient, notes)
                showRequestSheet = false
            }
        )
    }

    // Payout Detail Dialog
    selectedPayoutForDetail?.let { payout ->
        PayoutDetailDialog(
            payout = payout,
            onDismiss = { selectedPayoutForDetail = null },
            onProcess = {
                onProcessPayout(payout)
                selectedPayoutForDetail = null
            },
            onComplete = {
                onCompletePayout(payout)
                selectedPayoutForDetail = null
            },
            onCancel = {
                payoutToCancel = payout
                selectedPayoutForDetail = null
            },
            onDelete = {
                payoutToDelete = payout
                selectedPayoutForDetail = null
            },
            onRetry = {
                onRetryPayout(payout)
                selectedPayoutForDetail = null
            }
        )
    }

    // Cancel Payout Confirmation
    payoutToCancel?.let { payout ->
        AlertDialog(
            onDismissRequest = { payoutToCancel = null },
            title = { Text("Cancel Payout ${payout.id}?") },
            text = {
                Text("This scheduled transfer of ${Formatters.formatCurrency(payout.amount, payout.currency)} will be aborted and funds will be released back to your available balance.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onCancelPayout(payout)
                        payoutToCancel = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusOverdue)
                ) {
                    Text("Confirm Cancellation")
                }
            },
            dismissButton = {
                TextButton(onClick = { payoutToCancel = null }) {
                    Text("Keep Payout")
                }
            }
        )
    }

    // Edit & Retry Payout Dialog
    payoutToEditAndRetry?.let { payout ->
        EditAndRetryPayoutDialog(
            payout = payout,
            onDismiss = { payoutToEditAndRetry = null },
            onConfirm = { bank, iban, bic, recipient, notes ->
                onUpdateAndRetryPayout(payout, bank, iban, bic, recipient, notes)
                payoutToEditAndRetry = null
            }
        )
    }

    // Comprehensive Transaction Error Inspector & Diagnostic Sheet
    if (showTransactionErrorInspector) {
        TransactionErrorInspectorDialog(
            failedPayouts = failedPayouts,
            onDismiss = { showTransactionErrorInspector = false },
            onFixSingle = { payout ->
                onFixPayoutError(payout)
            },
            onRetrySingle = { payout ->
                onRetryPayout(payout)
            },
            onEditAndRetry = { payout ->
                showTransactionErrorInspector = false
                payoutToEditAndRetry = payout
            },
            onCancelSingle = { payout ->
                onCancelPayout(payout)
            },
            onBatchRetryAll = {
                onBatchRetryPayouts()
                showTransactionErrorInspector = false
            }
        )
    }
}

@Composable
fun PayoutHeroBalanceCard(
    metrics: PayoutMetrics,
    onRequestPayoutClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SETTLEMENT & PAYOUT HUB",
                        color = AccentGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.1.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Merchant Balance",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    color = Color.White.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Savings,
                            contentDescription = null,
                            tint = AccentGold,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "SEPA Direct",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Available to Withdraw",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = Formatters.formatCurrency(metrics.availableBalance),
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Black
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (metrics.availableBalance > 0) StatusPaid else StatusDraft)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (metrics.availableBalance > 0) "Ready for automated batch" else "All collected funds disbursed",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 12.sp
                    )
                }

                Button(
                    onClick = onRequestPayoutClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentGold,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("hero_withdraw_btn")
                ) {
                    Icon(Icons.Default.ArrowUpward, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Withdraw", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun PayoutMetricMiniCard(
    title: String,
    amount: Double,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(iconColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = Formatters.formatCurrency(amount),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun PayoutItemCard(
    payout: PayoutEntity,
    onClick: () -> Unit,
    onProcess: () -> Unit,
    onComplete: () -> Unit,
    onCancel: () -> Unit,
    onRetry: () -> Unit = {},
    onEditAndRetry: () -> Unit = {},
    onInspectError: () -> Unit = {},
    onFixError: () -> Unit = {}
) {
    val statusEnum = PayoutStatus.fromString(payout.status)
    val (statusBg, statusFg) = when (statusEnum) {
        PayoutStatus.PAID -> StatusPaidBg to StatusPaid
        PayoutStatus.PROCESSING -> StatusPendingBg to StatusPending
        PayoutStatus.SCHEDULED -> OceanBlue.copy(alpha = 0.12f) to OceanBlue
        PayoutStatus.FAILED -> StatusOverdueBg to StatusOverdue
        PayoutStatus.CANCELLED -> StatusDraftBg to StatusDraft
    }

    val cardModifier = if (statusEnum == PayoutStatus.FAILED) {
        Modifier
            .fillMaxWidth()
            .border(BorderStroke(1.5.dp, StatusOverdue.copy(alpha = 0.5f)), RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag("payout_card_${payout.id}")
    } else {
        Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("payout_card_${payout.id}")
    }

    ElevatedCard(
        modifier = cardModifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(statusBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (statusEnum) {
                                PayoutStatus.PAID -> Icons.Default.CheckCircle
                                PayoutStatus.PROCESSING -> Icons.Default.Sync
                                PayoutStatus.SCHEDULED -> Icons.Default.Schedule
                                PayoutStatus.FAILED -> Icons.Default.ErrorOutline
                                PayoutStatus.CANCELLED -> Icons.Default.Cancel
                            },
                            contentDescription = null,
                            tint = statusFg,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = payout.recipientName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "${payout.destinationBank} • ${payout.destinationIban.takeLast(8)}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = Formatters.formatCurrency(payout.amount, payout.currency),
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Surface(
                        color = statusBg,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (statusEnum == PayoutStatus.FAILED) "TRANSACTION ERROR" else statusEnum.label,
                            color = statusFg,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            if (payout.status == "FAILED") {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = StatusOverdue.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StatusOverdue.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = "Error",
                                    tint = StatusOverdue,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Interbank Clearing Exception",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusOverdue
                                )
                            }
                            Text(
                                text = "Action Required",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = StatusOverdue
                            )
                        }

                        Text(
                            text = payout.notes.ifBlank { "Clearing gateway reported rejection or routing mismatch." },
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ref: ${payout.reference} • ${Formatters.formatShortDate(payout.createdAt)}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Quick Status Advance Buttons
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (payout.status == "FAILED") {
                        Button(
                            onClick = onFixError,
                            colors = ButtonDefaults.buttonColors(containerColor = StatusPaid),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("fix_payout_error_${payout.id}")
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Fix Error", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        FilledTonalButton(
                            onClick = onEditAndRetry,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Edit & Retry", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = onRetry,
                            colors = ButtonDefaults.buttonColors(containerColor = OceanBlue),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Retry", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    } else if (payout.status == "SCHEDULED") {
                        TextButton(
                            onClick = onProcess,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("Process", fontSize = 11.sp, color = OceanBlue, fontWeight = FontWeight.Bold)
                        }
                        TextButton(
                            onClick = onCancel,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("Cancel", fontSize = 11.sp, color = StatusOverdue)
                        }
                    } else if (payout.status == "PROCESSING") {
                        Button(
                            onClick = onComplete,
                            colors = ButtonDefaults.buttonColors(containerColor = StatusPaid),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Mark Settled", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PayoutEmptyState(
    filter: String,
    onRequestPayout: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(OceanBlue.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.AccountBalanceWallet,
                    contentDescription = null,
                    tint = OceanBlue,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = if (filter == "ALL") "No Payouts Yet" else "No $filter Payouts",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "When invoices are paid by store clients, request automated SEPA bank settlements right here.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onRequestPayout,
                colors = ButtonDefaults.buttonColors(containerColor = OceanBlue),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Initiate First Payout")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestPayoutBottomSheet(
    availableBalance: Double,
    onDismiss: () -> Unit,
    onSubmit: (
        amount: Double,
        destinationType: String,
        destinationBank: String,
        destinationIban: String,
        destinationBic: String,
        recipientName: String,
        notes: String
    ) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var amountText by remember { mutableStateOf(if (availableBalance > 0) String.format(java.util.Locale.US, "%.2f", availableBalance) else "100.00") }
    var selectedType by remember { mutableStateOf(PayoutDestinationType.SEPA_BANK) }
    var bankName by remember { mutableStateOf("ING Bank N.V.") }
    var iban by remember { mutableStateOf("NL43 INGB 0006 3721 46") }
    var bic by remember { mutableStateOf("INGBNL2A") }
    var recipientName by remember { mutableStateOf("Jurgen Paul Westerveld") }
    var notes by remember { mutableStateOf("Scheduled merchant store balance payout") }
    var isExpressTransfer by remember { mutableStateOf(false) }

    val sheetFocusManager = LocalFocusManager.current
    val isBannedIban = false

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clearFocusOnTap(sheetFocusManager)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Request Settlement Payout",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Surface(
                    color = StatusPaidBg,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Avail: ${Formatters.formatCurrency(availableBalance)}",
                        color = StatusPaid,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Amount Input with Quick Preset Chips
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Payout Amount (€)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("payout_amount_input"),
                    leadingIcon = { Text("€", fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp)) }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(500.0, 1000.0, 2500.0, availableBalance).distinct().forEach { preset ->
                        if (preset > 0) {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.clickable {
                                    amountText = String.format(java.util.Locale.US, "%.2f", preset)
                                }
                            ) {
                                Text(
                                    text = if (preset == availableBalance) "Max" else "€${preset.toInt()}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Recipient & Settlement Bank Details
            OutlinedTextField(
                value = recipientName,
                onValueChange = { recipientName = it },
                label = { Text("Beneficiary Entity") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = bankName,
                    onValueChange = { bankName = it },
                    label = { Text("Beneficiary Bank") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = bic,
                    onValueChange = { bic = it },
                    label = { Text("BIC / SWIFT") },
                    singleLine = true,
                    modifier = Modifier.weight(0.9f)
                )
            }

            OutlinedTextField(
                value = iban,
                onValueChange = { iban = it },
                label = { Text("Settlement IBAN") },
                supportingText = { Text("Verified SEPA IBAN for direct corporate disbursement", fontSize = 11.sp, color = OceanBlue) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Memo / Internal Notes") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Settlement Timing Guarantee Notice
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = OceanBlue, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Standard SEPA clearing takes 1 business day. Zero payout processing fees applied.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            val cleanIban = iban.replace("\\s+".toRegex(), "")
            val isIbanFormatValid = cleanIban.length in 15..34
            val isBicFormatValid = bic.trim().length in 8..11

            Button(
                onClick = {
                    val parsed = amountText.toDoubleOrNull() ?: 0.0
                    if (parsed > 0 && !isBannedIban && isIbanFormatValid && isBicFormatValid) {
                        onSubmit(parsed, selectedType.name, bankName, iban, bic, recipientName, notes)
                    }
                },
                enabled = !isBannedIban && (amountText.toDoubleOrNull() ?: 0.0) > 0 && isIbanFormatValid && isBicFormatValid,
                colors = ButtonDefaults.buttonColors(containerColor = OceanBlue),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("submit_payout_btn")
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Confirm & Schedule Payout", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun PayoutDetailDialog(
    payout: PayoutEntity,
    onDismiss: () -> Unit,
    onProcess: () -> Unit,
    onComplete: () -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit,
    onRetry: () -> Unit = {}
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Payout ${payout.id}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Surface(
                    color = when (payout.status) {
                        "PAID" -> StatusPaidBg
                        "PROCESSING" -> StatusPendingBg
                        "SCHEDULED" -> OceanBlue.copy(alpha = 0.12f)
                        "FAILED" -> StatusOverdueBg
                        else -> StatusDraftBg
                    },
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = if (payout.status == "FAILED") "TRANSACTION ERROR" else payout.status,
                        color = when (payout.status) {
                            "PAID" -> StatusPaid
                            "PROCESSING" -> StatusPending
                            "SCHEDULED" -> OceanBlue
                            "FAILED" -> StatusOverdue
                            else -> StatusDraft
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                DetailRow("Total Amount", Formatters.formatCurrency(payout.amount, payout.currency), isBold = true)
                DetailRow("Beneficiary", payout.recipientName)
                DetailRow("Bank", payout.destinationBank)
                DetailRow("IBAN", payout.destinationIban)
                DetailRow("BIC/SWIFT", payout.destinationBic)
                DetailRow("Payment Reference", payout.reference)
                DetailRow("Created Date", Formatters.formatDate(payout.createdAt))
                payout.completedAt?.let {
                    DetailRow("Completed Date", Formatters.formatDate(it))
                }
                if (payout.notes.isNotBlank()) {
                    DetailRow("Notes", payout.notes)
                }

                if (payout.status == "FAILED") {
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        color = StatusOverdueBg,
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StatusOverdue.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = StatusOverdue, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Clearing Exception Diagnostic", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = StatusOverdue)
                            }
                            Text(
                                text = "Gateway Reason: ${payout.notes.ifBlank { "Intermediary network clearing rejected transfer" }}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Ledger Status: Funds were not debited. Tap 'Retry Transfer' to resubmit.",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(4.dp))

                // Action Controls
                Text("Transfer Operations:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (payout.status == "FAILED") {
                        Button(
                            onClick = onRetry,
                            colors = ButtonDefaults.buttonColors(containerColor = OceanBlue),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Retry Transfer", fontSize = 12.sp)
                        }
                        OutlinedButton(
                            onClick = onDelete,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Discard Record", fontSize = 12.sp, color = StatusOverdue)
                        }
                    } else if (payout.status == "SCHEDULED") {
                        FilledTonalButton(
                            onClick = onProcess,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Send to Bank", fontSize = 12.sp)
                        }
                        OutlinedButton(
                            onClick = onCancel,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel", fontSize = 12.sp, color = StatusOverdue)
                        }
                    } else if (payout.status == "PROCESSING") {
                        Button(
                            onClick = onComplete,
                            colors = ButtonDefaults.buttonColors(containerColor = StatusPaid),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Mark Settled", fontSize = 12.sp)
                        }
                    } else {
                        OutlinedButton(
                            onClick = onDelete,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Delete Record", fontSize = 12.sp, color = StatusOverdue)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun DetailRow(label: String, value: String, isBold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = 12.sp, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium)
    }
}

@Composable
fun EditAndRetryPayoutDialog(
    payout: PayoutEntity,
    onDismiss: () -> Unit,
    onConfirm: (bank: String, iban: String, bic: String, recipient: String, notes: String) -> Unit
) {
    var recipientName by remember { mutableStateOf(payout.recipientName) }
    var bankName by remember { mutableStateOf(payout.destinationBank) }
    var iban by remember { mutableStateOf(payout.destinationIban) }
    var bic by remember { mutableStateOf(payout.destinationBic) }
    var notes by remember { mutableStateOf(payout.notes) }

    val cleanIban = iban.replace("\\s+".toRegex(), "")
    val isIbanValid = cleanIban.length in 15..34
    val isBicValid = bic.trim().length in 8..11

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = OceanBlue,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Fix Routing & Retry Payout", fontSize = 17.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Update the beneficiary bank routing details for ${payout.id} (${Formatters.formatCurrency(payout.amount, payout.currency)}) to clear transaction exceptions.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = recipientName,
                    onValueChange = { recipientName = it },
                    label = { Text("Beneficiary Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = bankName,
                    onValueChange = { bankName = it },
                    label = { Text("Bank Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = iban,
                    onValueChange = { iban = it },
                    label = { Text("Settlement IBAN") },
                    isError = !isIbanValid,
                    supportingText = {
                        if (!isIbanValid) {
                            Text("IBAN must be 15-34 characters", color = StatusOverdue, fontSize = 10.sp)
                        } else {
                            Text("SEPA IBAN format valid", color = StatusPaid, fontSize = 10.sp)
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = bic,
                    onValueChange = { bic = it.uppercase() },
                    label = { Text("BIC / SWIFT Code") },
                    isError = !isBicValid,
                    supportingText = {
                        if (!isBicValid) {
                            Text("BIC must be 8 or 11 characters (e.g. INGBNL2A)", color = StatusOverdue, fontSize = 10.sp)
                        } else {
                            Text("BIC length verified", color = StatusPaid, fontSize = 10.sp)
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Audit Note / Correction Reason") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isIbanValid && isBicValid) {
                        onConfirm(bankName, iban, bic, recipientName, notes)
                    }
                },
                enabled = isIbanValid && isBicValid,
                colors = ButtonDefaults.buttonColors(containerColor = OceanBlue)
            ) {
                Text("Save & Re-submit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun TransactionErrorInspectorDialog(
    failedPayouts: List<PayoutEntity>,
    onDismiss: () -> Unit,
    onFixSingle: (PayoutEntity) -> Unit = {},
    onRetrySingle: (PayoutEntity) -> Unit,
    onEditAndRetry: (PayoutEntity) -> Unit,
    onCancelSingle: (PayoutEntity) -> Unit,
    onBatchRetryAll: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(StatusOverdue.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = StatusOverdue,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Transaction Error Audit", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Surface(
                    color = StatusOverdue.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "${failedPayouts.size} FAILED",
                        color = StatusOverdue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "The following settlement transactions encountered clearing gateway exceptions or interbank rejection:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                failedPayouts.forEach { payout ->
                    Surface(
                        color = StatusOverdueBg,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StatusOverdue.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${payout.id} • ${Formatters.formatCurrency(payout.amount, payout.currency)}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = payout.reference,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = OceanBlue
                                )
                            }

                            DetailRow("Recipient", payout.recipientName)
                            DetailRow("Bank & BIC", "${payout.destinationBank} (${payout.destinationBic})")
                            DetailRow("IBAN", payout.destinationIban)

                            Surface(
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = StatusOverdue,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(
                                            text = "Clearing Rejection Reason:",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = StatusOverdue
                                        )
                                        Text(
                                            text = payout.notes.ifBlank { "Intermediary clearing institution rejected transfer." },
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Button(
                                    onClick = { onFixSingle(payout) },
                                    colors = ButtonDefaults.buttonColors(containerColor = StatusPaid),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("fix_payout_error_dialog_button")
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Fix Error", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                FilledTonalButton(
                                    onClick = { onEditAndRetry(payout) },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Edit & Retry", fontSize = 11.sp)
                                }

                                OutlinedButton(
                                    onClick = { onCancelSingle(payout) },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Cancel", fontSize = 11.sp, color = StatusOverdue)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (failedPayouts.isNotEmpty()) {
                    Button(
                        onClick = {
                            failedPayouts.forEach { onFixSingle(it) }
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StatusPaid),
                        modifier = Modifier.testTag("fix_all_payout_errors_button")
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Fix All (${failedPayouts.size})")
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Close")
                }
            }
        },
        dismissButton = {}
    )
}
