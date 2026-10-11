package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.InvoiceEntity
import com.example.data.local.PayoutEntity
import com.example.data.model.InvoiceStatus
import com.example.data.model.PayoutMetrics
import com.example.data.model.PayoutStatus
import com.example.ui.theme.AccentGold
import com.example.ui.theme.NavyDark
import com.example.ui.theme.OceanBlue
import com.example.ui.theme.StatusDraft
import com.example.ui.theme.StatusDraftBg
import com.example.ui.theme.StatusOverdue
import com.example.ui.theme.StatusOverdueBg
import com.example.ui.theme.StatusPaid
import com.example.ui.theme.StatusPaidBg
import com.example.ui.theme.StatusPending
import com.example.ui.theme.StatusPendingBg
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.max

data class PayoutHistoryBucket(
    val periodLabel: String,
    val periodFullLabel: String,
    val timestamp: Long,
    val completedAmount: Double,
    val pendingAmount: Double,
    val payoutCount: Int,
    val completedCount: Int
) {
    val totalAmount: Double
        get() = completedAmount + pendingAmount
}

enum class PayoutChartType {
    AREA_FLOW,
    BAR_COLUMNS
}

/**
 * PayoutDashboard composable that:
 * 1. Aggregates pending merchant settlements & available earnings to be paid out.
 * 2. Visualizes payout history using interactive Recharts-style Canvas charts (Area & Bar).
 * 3. Displays a comprehensive summary of total earnings, in-flight settlements, and quick action controls.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayoutDashboard(
    payoutMetrics: PayoutMetrics,
    payouts: List<PayoutEntity>,
    pendingInvoices: List<InvoiceEntity> = emptyList(),
    onRequestPayout: () -> Unit = {},
    onViewAllPayouts: () -> Unit = {},
    onProcessPayout: (PayoutEntity) -> Unit = {},
    onCompletePayout: (PayoutEntity) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var chartType by remember { mutableStateOf(PayoutChartType.AREA_FLOW) }
    var selectedTimeframeMonths by remember { mutableIntStateOf(6) } // 3, 6, or 12 months

    // Aggregate monthly buckets for Recharts visualization
    val historyBuckets = remember(payouts, selectedTimeframeMonths) {
        generatePayoutHistoryBuckets(payouts, selectedTimeframeMonths)
    }

    var selectedBucketIndex by remember(historyBuckets) {
        mutableStateOf<Int?>(if (historyBuckets.isNotEmpty()) historyBuckets.lastIndex else null)
    }

    // Pending settlements: payouts currently in SCHEDULED or PROCESSING
    val pendingPayouts = remember(payouts) {
        payouts.filter {
            it.status.equals("SCHEDULED", ignoreCase = true) ||
            it.status.equals("PROCESSING", ignoreCase = true)
        }.sortedBy { it.scheduledDate }
    }

    // Total gross earnings ready to be disbursed
    val totalAvailableEarnings = payoutMetrics.availableBalance
    val totalInTransit = payoutMetrics.pendingPayoutBalance
    val totalPaidOut = payoutMetrics.totalPaidOut

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("payout_dashboard_container"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Dashboard Header & Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(OceanBlue.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = "Payout Dashboard",
                            tint = OceanBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Merchant Payout Dashboard",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Settlement clearance, earnings & Recharts telemetry",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    color = StatusPaidBg,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(StatusPaid)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Active SEPA Hub",
                            color = StatusPaid,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Summary KPI Strip: Total Earnings to be Paid Out vs In Transit vs Settled
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Available to be paid out
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Ready to Pay Out",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = Formatters.formatCurrency(totalAvailableEarnings),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Available balance",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // In-Flight Pending
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = StatusPendingBg.copy(alpha = 0.5f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Pending Transit",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = Formatters.formatCurrency(totalInTransit),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = StatusPending
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${payoutMetrics.processingPayoutsCount} in transit",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Settled Total
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = StatusPaidBg.copy(alpha = 0.5f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Total Settled",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = Formatters.formatCurrency(totalPaidOut),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = StatusPaid
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${payoutMetrics.completedPayoutsCount} disbursements",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Interactive Recharts Visualization Card
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Chart Controls Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Payout History (Recharts)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = OceanBlue.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "Interactive",
                                        color = OceanBlue,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Drag or tap points for month breakdowns",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Chart Type Switcher & Timeframe selector
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Area vs Bar Toggle
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surface
                            ) {
                                Row(modifier = Modifier.padding(2.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (chartType == PayoutChartType.AREA_FLOW) OceanBlue.copy(alpha = 0.15f) else Color.Transparent)
                                            .clickable { chartType = PayoutChartType.AREA_FLOW }
                                            .padding(horizontal = 6.dp, vertical = 4.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.ShowChart,
                                            contentDescription = "Area Flow",
                                            tint = if (chartType == PayoutChartType.AREA_FLOW) OceanBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (chartType == PayoutChartType.BAR_COLUMNS) OceanBlue.copy(alpha = 0.15f) else Color.Transparent)
                                            .clickable { chartType = PayoutChartType.BAR_COLUMNS }
                                            .padding(horizontal = 6.dp, vertical = 4.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.BarChart,
                                            contentDescription = "Bar Columns",
                                            tint = if (chartType == PayoutChartType.BAR_COLUMNS) OceanBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }

                            // Timeframe: 3M, 6M, 12M
                            listOf(3, 6, 12).forEach { months ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (selectedTimeframeMonths == months) OceanBlue else MaterialTheme.colorScheme.surface,
                                    modifier = Modifier.clickable { selectedTimeframeMonths = months }
                                ) {
                                    Text(
                                        text = "${months}M",
                                        fontSize = 11.sp,
                                        fontWeight = if (selectedTimeframeMonths == months) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedTimeframeMonths == months) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Selected Hover / Active Tooltip Bar
                    val activeBucket = selectedBucketIndex?.let { historyBuckets.getOrNull(it) }
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = activeBucket?.periodFullLabel ?: "Select Month",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${activeBucket?.payoutCount ?: 0} transfers (${activeBucket?.completedCount ?: 0} settled)",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Settled",
                                        fontSize = 10.sp,
                                        color = StatusPaid,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = Formatters.formatCurrency(activeBucket?.completedAmount ?: 0.0),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = StatusPaid
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Pending",
                                        fontSize = 10.sp,
                                        color = StatusPending,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = Formatters.formatCurrency(activeBucket?.pendingAmount ?: 0.0),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = StatusPending
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Canvas Visualizer Viewport
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    ) {
                        if (historyBuckets.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No payout data available", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        } else {
                            PayoutRechartsCanvas(
                                buckets = historyBuckets,
                                chartType = chartType,
                                selectedIndex = selectedBucketIndex,
                                onSelectIndex = { selectedBucketIndex = it }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Pending Merchant Settlements Queue Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Pending Merchant Settlements",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = if (pendingPayouts.isNotEmpty()) StatusPendingBg else StatusDraftBg,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "${pendingPayouts.size}",
                            color = if (pendingPayouts.isNotEmpty()) StatusPending else StatusDraft,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                TextButton(
                    onClick = onViewAllPayouts,
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("View All Ledger", fontSize = 12.sp, color = OceanBlue)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (pendingPayouts.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = StatusPaid,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "All Settlements Cleared",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "No payouts are currently awaiting bank batch processing.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    pendingPayouts.take(3).forEach { payout ->
                        PendingSettlementItemRow(
                            payout = payout,
                            onProcess = { onProcessPayout(payout) },
                            onComplete = { onCompletePayout(payout) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Bar: Request Withdrawal & View Full History
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onRequestPayout,
                    colors = ButtonDefaults.buttonColors(containerColor = OceanBlue),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1.2f)
                        .height(44.dp)
                        .testTag("dashboard_request_payout_btn")
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Request Payout", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                OutlinedButton(
                    onClick = onViewAllPayouts,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("dashboard_view_all_payouts_btn")
                ) {
                    Icon(Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("All Payouts", fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun PendingSettlementItemRow(
    payout: PayoutEntity,
    onProcess: () -> Unit,
    onComplete: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (payout.status == "PROCESSING") StatusPendingBg else OceanBlue.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (payout.status == "PROCESSING") Icons.Default.Sync else Icons.Default.Schedule,
                        contentDescription = null,
                        tint = if (payout.status == "PROCESSING") StatusPending else OceanBlue,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = payout.recipientName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "${payout.destinationBank} • Due: ${Formatters.formatShortDate(payout.scheduledDate)}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = Formatters.formatCurrency(payout.amount, payout.currency),
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                if (payout.status == "SCHEDULED") {
                    TextButton(
                        onClick = onProcess,
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                    ) {
                        Text("Process", fontSize = 11.sp, color = OceanBlue, fontWeight = FontWeight.Bold)
                    }
                } else if (payout.status == "PROCESSING") {
                    Button(
                        onClick = onComplete,
                        colors = ButtonDefaults.buttonColors(containerColor = StatusPaid),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("Settle", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Custom Canvas chart that visualizes payout telemetry in Recharts aesthetic.
 * Supports smooth Bézier area chart with gradients, as well as grouped/stacked columns.
 */
@Composable
private fun PayoutRechartsCanvas(
    buckets: List<PayoutHistoryBucket>,
    chartType: PayoutChartType,
    selectedIndex: Int?,
    onSelectIndex: (Int) -> Unit
) {
    val textMeasurer = rememberTextMeasurer()
    val settledColor = StatusPaid
    val pendingColor = StatusPending
    val gridLineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    val maxAmount = remember(buckets) {
        val maxVal = buckets.maxOfOrNull { it.totalAmount } ?: 1000.0
        if (maxVal <= 0.0) 1000.0 else maxVal * 1.2
    }

    androidx.compose.foundation.Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(buckets) {
                detectTapGestures { offset ->
                    val pointCount = buckets.size
                    if (pointCount > 0) {
                        val leftPad = 70f
                        val rightPad = 30f
                        val availW = size.width - leftPad - rightPad
                        val step = availW / (pointCount - 1).coerceAtLeast(1)
                        val closestIdx = ((offset.x - leftPad + (step / 2f)) / step)
                            .toInt()
                            .coerceIn(0, pointCount - 1)
                        onSelectIndex(closestIdx)
                    }
                }
            }
            .pointerInput(buckets) {
                detectDragGestures { change, _ ->
                    val pointCount = buckets.size
                    if (pointCount > 0) {
                        val leftPad = 70f
                        val rightPad = 30f
                        val availW = size.width - leftPad - rightPad
                        val step = availW / (pointCount - 1).coerceAtLeast(1)
                        val closestIdx = ((change.position.x - leftPad + (step / 2f)) / step)
                            .toInt()
                            .coerceIn(0, pointCount - 1)
                        onSelectIndex(closestIdx)
                    }
                }
            }
    ) {
        val w = size.width
        val h = size.height

        val leftPad = 70f
        val rightPad = 30f
        val topPad = 20f
        val bottomPad = 36f

        val plotW = w - leftPad - rightPad
        val plotH = h - topPad - bottomPad

        // 1. Horizontal Reference Grid Lines (Recharts CartesianGrid)
        val gridSteps = 3
        for (i in 0..gridSteps) {
            val ratio = i / gridSteps.toFloat()
            val y = topPad + plotH * (1f - ratio)
            val valAtGrid = maxAmount * ratio

            // Grid line
            drawLine(
                color = gridLineColor,
                start = Offset(leftPad, y),
                end = Offset(w - rightPad, y),
                strokeWidth = 1f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
            )

            // Y-Axis currency label
            val textLayout = textMeasurer.measure(
                text = "€${(valAtGrid / 1000).toInt()}k",
                style = TextStyle(fontSize = 10.sp, color = onSurfaceVariant)
            )
            drawText(
                textLayoutResult = textLayout,
                topLeft = Offset(10f, y - textLayout.size.height / 2f)
            )
        }

        val count = buckets.size
        if (count == 0) return@Canvas

        val stepX = if (count > 1) plotW / (count - 1) else plotW / 2f

        // 2. Render Chart based on chartType
        if (chartType == PayoutChartType.AREA_FLOW) {
            // Compute points for settled and total amount
            val settledPoints = mutableListOf<Offset>()
            val totalPoints = mutableListOf<Offset>()

            for (i in buckets.indices) {
                val bucket = buckets[i]
                val x = leftPad + i * stepX
                val ySettled = topPad + plotH * (1f - (bucket.completedAmount / maxAmount).toFloat().coerceIn(0f, 1f))
                val yTotal = topPad + plotH * (1f - (bucket.totalAmount / maxAmount).toFloat().coerceIn(0f, 1f))
                settledPoints.add(Offset(x, ySettled))
                totalPoints.add(Offset(x, yTotal))
            }

            // Draw Area Fill for Settled (Gradient)
            val settledPath = Path().apply {
                moveTo(settledPoints.first().x, topPad + plotH)
                for (pt in settledPoints) {
                    lineTo(pt.x, pt.y)
                }
                lineTo(settledPoints.last().x, topPad + plotH)
                close()
            }

            drawPath(
                path = settledPath,
                brush = Brush.verticalGradient(
                    colors = listOf(settledColor.copy(alpha = 0.35f), settledColor.copy(alpha = 0.02f)),
                    startY = topPad,
                    endY = topPad + plotH
                )
            )

            // Draw Settled Stroke Line
            val settledStrokePath = Path().apply {
                moveTo(settledPoints.first().x, settledPoints.first().y)
                for (pt in settledPoints) {
                    lineTo(pt.x, pt.y)
                }
            }
            drawPath(
                path = settledStrokePath,
                color = settledColor,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )

            // Draw Total / In-Flight Stroke Line (Dashed)
            val totalStrokePath = Path().apply {
                moveTo(totalPoints.first().x, totalPoints.first().y)
                for (pt in totalPoints) {
                    lineTo(pt.x, pt.y)
                }
            }
            drawPath(
                path = totalStrokePath,
                color = pendingColor,
                style = Stroke(
                    width = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f),
                    cap = StrokeCap.Round
                )
            )

            // Draw Point Knots
            for (i in buckets.indices) {
                val pt = settledPoints[i]
                val isSelected = selectedIndex == i

                drawCircle(
                    color = Color.White,
                    radius = if (isSelected) 6.dp.toPx() else 3.5.dp.toPx(),
                    center = pt
                )
                drawCircle(
                    color = settledColor,
                    radius = if (isSelected) 4.5.dp.toPx() else 2.5.dp.toPx(),
                    center = pt
                )
            }
        } else {
            // BAR_COLUMNS: Render paired stacked bars
            val barW = (stepX * 0.45f).coerceIn(12f, 32f)

            for (i in buckets.indices) {
                val bucket = buckets[i]
                val x = leftPad + i * stepX - (barW / 2f)
                val settledH = plotH * (bucket.completedAmount / maxAmount).toFloat().coerceIn(0f, 1f)
                val pendingH = plotH * (bucket.pendingAmount / maxAmount).toFloat().coerceIn(0f, 1f)

                val isSelected = selectedIndex == i

                // Settled bottom bar
                if (settledH > 0f) {
                    drawRoundRect(
                        color = if (isSelected) settledColor else settledColor.copy(alpha = 0.85f),
                        topLeft = Offset(x, topPad + plotH - settledH),
                        size = Size(barW, settledH),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )
                }

                // Pending stacked top bar
                if (pendingH > 0f) {
                    drawRoundRect(
                        color = if (isSelected) pendingColor else pendingColor.copy(alpha = 0.75f),
                        topLeft = Offset(x, topPad + plotH - settledH - pendingH),
                        size = Size(barW, pendingH),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )
                }
            }
        }

        // 3. Active Indicator Guide Line
        selectedIndex?.let { idx ->
            if (idx in buckets.indices) {
                val x = leftPad + idx * stepX
                drawLine(
                    color = OceanBlue.copy(alpha = 0.7f),
                    start = Offset(x, topPad),
                    end = Offset(x, topPad + plotH),
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                )
            }
        }

        // 4. X-Axis Month Labels
        for (i in buckets.indices) {
            val x = leftPad + i * stepX
            val bucket = buckets[i]
            val isSelected = selectedIndex == i

            val textLayout = textMeasurer.measure(
                text = bucket.periodLabel,
                style = TextStyle(
                    fontSize = 10.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) OceanBlue else onSurfaceVariant
                )
            )

            drawText(
                textLayoutResult = textLayout,
                topLeft = Offset(x - (textLayout.size.width / 2f), topPad + plotH + 8f)
            )
        }
    }
}

/**
 * Utility to group payouts into historical monthly buckets for visualization.
 */
private fun generatePayoutHistoryBuckets(
    payouts: List<PayoutEntity>,
    monthsCount: Int
): List<PayoutHistoryBucket> {
    val monthFmt = SimpleDateFormat("MMM", Locale.US)
    val fullFmt = SimpleDateFormat("MMMM yyyy", Locale.US)

    val buckets = mutableListOf<PayoutHistoryBucket>()

    for (i in (monthsCount - 1) downTo 0) {
        val cal = Calendar.getInstance().apply {
            add(Calendar.MONTH, -i)
        }

        val startCal = (cal.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startMs = startCal.timeInMillis

        val endCal = (cal.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        val endMs = endCal.timeInMillis

        // Payouts falling in this month
        val periodPayouts = payouts.filter {
            val date = if (it.completedAt != null && it.completedAt > 0) it.completedAt else it.createdAt
            date in startMs..endMs
        }

        val completed = periodPayouts.filter { it.status.equals("PAID", ignoreCase = true) }
        val pending = periodPayouts.filter {
            it.status.equals("SCHEDULED", ignoreCase = true) || it.status.equals("PROCESSING", ignoreCase = true)
        }

        buckets.add(
            PayoutHistoryBucket(
                periodLabel = monthFmt.format(cal.time),
                periodFullLabel = fullFmt.format(cal.time),
                timestamp = startMs,
                completedAmount = completed.sumOf { it.amount },
                pendingAmount = pending.sumOf { it.amount },
                payoutCount = periodPayouts.size,
                completedCount = completed.size
            )
        )
    }

    return buckets
}
