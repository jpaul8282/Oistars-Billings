package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ClientEntity
import com.example.data.local.InvoiceEntity
import com.example.data.local.PayoutEntity
import com.example.data.local.SubscriptionEntity
import com.example.data.model.InvoiceItem
import com.example.data.model.InvoiceStatus
import com.example.data.model.MonthlyRevenueTrend
import com.example.data.model.PayoutMetrics
import com.example.ui.components.Formatters
import com.example.ui.components.MetricStatCard
import com.example.ui.components.MonthlyRevenueTrendsChart
import com.example.ui.components.PayoutDashboard
import com.example.ui.theme.AccentGold
import com.example.ui.theme.NavyDark
import com.example.ui.theme.OceanBlue
import com.example.ui.theme.StatusOverdue
import com.example.ui.theme.StatusOverdueBg
import com.example.ui.theme.StatusPaid
import com.example.ui.theme.StatusPaidBg
import com.example.ui.theme.StatusPending
import com.example.ui.theme.StatusPendingBg
import com.example.ui.viewmodel.FinancialMetrics

@Composable
fun DashboardScreen(
    metrics: FinancialMetrics,
    payoutMetrics: PayoutMetrics = PayoutMetrics(),
    payouts: List<PayoutEntity> = emptyList(),
    monthlyRevenueTrends: List<MonthlyRevenueTrend>,
    selectedTrendPeriodMonths: Int,
    onTrendPeriodChange: (Int) -> Unit,
    recentInvoices: List<InvoiceEntity>,
    activeSubscriptions: List<SubscriptionEntity>,
    clients: List<ClientEntity>,
    onNavigateToInvoices: () -> Unit,
    onNavigateToClients: () -> Unit,
    onNavigateToSubscriptions: () -> Unit,
    onNavigateToPayouts: () -> Unit = {},
    onRequestPayout: () -> Unit = onNavigateToPayouts,
    onProcessPayout: (PayoutEntity) -> Unit = {},
    onCompletePayout: (PayoutEntity) -> Unit = {},
    onFixPayoutError: (PayoutEntity) -> Unit = {},
    onMarkAsPaid: (InvoiceEntity) -> Unit,
    onRecordPayment: (InvoiceEntity, Double, String, String, String) -> Unit,
    onDeleteInvoice: (InvoiceEntity) -> Unit,
    onGenerateFromSubscription: (SubscriptionEntity) -> Unit,
    onSaveInvoice: (
        clientId: String,
        clientName: String,
        clientEmail: String,
        items: List<InvoiceItem>,
        paymentTerms: String,
        notes: String,
        status: String,
        currency: String
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    var showCreateInvoiceSheet by remember { mutableStateOf(false) }
    var selectedInvoiceForDetail by remember { mutableStateOf<InvoiceEntity?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Platform Hero Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
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
                            text = "OISTARS BILLINGS PLATFORM",
                            color = AccentGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.2.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "E-Commerce Billing Hub",
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
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(StatusPaid)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${metrics.collectionRatePercent}% Collected",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "Total Invoiced Revenue",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 13.sp
                        )
                        Text(
                            text = Formatters.formatCurrency(metrics.totalInvoiced),
                            color = Color.White,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = { showCreateInvoiceSheet = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentGold,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("dashboard_new_invoice_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New Invoice", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Cash Flow Visual Health Bar
        val total = (metrics.totalPaid + metrics.totalOutstanding).coerceAtLeast(1.0)
        val paidFraction = (metrics.totalPaid / total).toFloat().coerceIn(0f, 1f)
        val overdueFraction = (metrics.totalOverdue / total).toFloat().coerceIn(0f, 1f)
        val pendingFraction = ((metrics.totalOutstanding - metrics.totalOverdue).coerceAtLeast(0.0) / total).toFloat().coerceIn(0f, 1f)

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Cash Flow Health",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Multi-color Progress bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    if (paidFraction > 0f) {
                        Box(
                            modifier = Modifier
                                .weight(paidFraction.coerceAtLeast(0.01f))
                                .height(10.dp)
                                .background(StatusPaid)
                        )
                    }
                    if (pendingFraction > 0f) {
                        Box(
                            modifier = Modifier
                                .weight(pendingFraction.coerceAtLeast(0.01f))
                                .height(10.dp)
                                .background(StatusPending)
                        )
                    }
                    if (overdueFraction > 0f) {
                        Box(
                            modifier = Modifier
                                .weight(overdueFraction.coerceAtLeast(0.01f))
                                .height(10.dp)
                                .background(StatusOverdue)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(StatusPaid))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Paid: ${Formatters.formatCurrency(metrics.totalPaid)}", fontSize = 11.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(StatusPending))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Pending: ${Formatters.formatCurrency(metrics.totalOutstanding - metrics.totalOverdue)}", fontSize = 11.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(StatusOverdue))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Overdue: ${Formatters.formatCurrency(metrics.totalOverdue)}", fontSize = 11.sp, color = StatusOverdue)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Monthly Revenue Trends Chart (Recharts-style Visualizer from Room database)
        MonthlyRevenueTrendsChart(
            trends = monthlyRevenueTrends,
            selectedPeriodMonths = selectedTrendPeriodMonths,
            onPeriodChange = onTrendPeriodChange
        )

        Spacer(modifier = Modifier.height(16.dp))

        // PayoutDashboard Component: Aggregates pending merchant settlements & Recharts visualization
        PayoutDashboard(
            payoutMetrics = payoutMetrics,
            payouts = payouts,
            pendingInvoices = recentInvoices.filter { it.invoiceStatus != InvoiceStatus.PAID },
            onRequestPayout = onRequestPayout,
            onViewAllPayouts = onNavigateToPayouts,
            onProcessPayout = onProcessPayout,
            onCompletePayout = onCompletePayout,
            onFixPayoutError = onFixPayoutError
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Metric Stat Cards Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricStatCard(
                title = "Collected (Paid)",
                amount = metrics.totalPaid,
                icon = Icons.Default.CheckCircle,
                iconColor = StatusPaid,
                subtitle = "${metrics.paidInvoicesCount} invoices paid",
                modifier = Modifier.weight(1f)
            )

            MetricStatCard(
                title = "Outstanding",
                amount = metrics.totalOutstanding,
                icon = Icons.Default.HourglassEmpty,
                iconColor = StatusPending,
                subtitle = "${metrics.pendingInvoicesCount + metrics.overdueInvoicesCount} awaiting payment",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricStatCard(
                title = "Monthly MRR",
                amount = metrics.monthlyRecurringRevenue,
                icon = Icons.Default.Autorenew,
                iconColor = OceanBlue,
                subtitle = "${metrics.activeSubscriptionsCount} active retainers",
                modifier = Modifier.weight(1f)
            )

            MetricStatCard(
                title = "Overdue Balances",
                amount = metrics.totalOverdue,
                icon = Icons.Default.Warning,
                iconColor = StatusOverdue,
                subtitle = "${metrics.overdueInvoicesCount} invoices past due",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Quick Navigation Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilledTonalButton(
                onClick = onNavigateToInvoices,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Invoices", fontSize = 12.sp)
            }

            FilledTonalButton(
                onClick = onNavigateToPayouts,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(16.dp), tint = OceanBlue)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Payouts", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = OceanBlue)
            }

            FilledTonalButton(
                onClick = onNavigateToSubscriptions,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Autorenew, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Retainers", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Active Retainers Quick Generation Section
        if (activeSubscriptions.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recurring Retainers",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onNavigateToSubscriptions) {
                    Text("View All", fontSize = 12.sp)
                }
            }

            activeSubscriptions.take(2).forEach { sub ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(sub.clientName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(sub.planName, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = "Next: ${Formatters.formatShortDate(sub.nextBillingDate)} • ${Formatters.formatCurrency(sub.amount, sub.currency)} / ${sub.billingInterval.label.lowercase()}",
                                fontSize = 11.sp,
                                color = OceanBlue
                            )
                        }

                        OutlinedButton(
                            onClick = { onGenerateFromSubscription(sub) },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Bill Now", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Recent Invoices Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Invoices",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = onNavigateToInvoices) {
                Text("See All (${recentInvoices.size})", fontSize = 12.sp)
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            recentInvoices.take(4).forEach { invoice ->
                InvoiceListItemCard(
                    invoice = invoice,
                    onClick = { selectedInvoiceForDetail = invoice },
                    onMarkAsPaid = { onMarkAsPaid(invoice) }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Recent Payout Disbursements Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Payout Disbursements",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = onNavigateToPayouts) {
                Text(
                    text = if (payouts.isNotEmpty()) "Manage Payouts (${payouts.size})" else "Manage Payouts",
                    fontSize = 12.sp,
                    color = OceanBlue
                )
            }
        }

        val failedPayouts = payouts.filter { it.status.equals("FAILED", ignoreCase = true) }
        if (failedPayouts.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                color = StatusOverdueBg,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, StatusOverdue.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_payout_error_banner")
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
                        Icon(
                            Icons.Default.ErrorOutline,
                            contentDescription = "Payout Error",
                            tint = StatusOverdue,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "${failedPayouts.size} Payout Settlement Error Detected",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = StatusOverdue
                            )
                            Text(
                                text = "${failedPayouts.first().id} • ${failedPayouts.first().notes.take(45)}...",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Button(
                        onClick = { onFixPayoutError(failedPayouts.first()) },
                        colors = ButtonDefaults.buttonColors(containerColor = StatusPaid),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("fix_payout_error_button")
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Fix Error", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        if (payouts.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onRequestPayout() },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(OceanBlue.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = OceanBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "No Payout Disbursements Yet",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Request your first merchant settlement payout to transfer earnings to your bank.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(
                        onClick = onRequestPayout,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("Disburse", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                payouts.take(4).forEach { payout ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToPayouts() }
                            .testTag("dashboard_payout_item_${payout.id}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = payout.reference.ifBlank { payout.id },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    val isSuccess = payout.status.equals("PAID", true) || payout.status.equals("COMPLETED", true)
                                    val isFailed = payout.status.equals("FAILED", true)
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = when {
                                            isSuccess -> StatusPaidBg
                                            isFailed -> StatusOverdueBg
                                            else -> StatusPendingBg
                                        }
                                    ) {
                                        Text(
                                            text = payout.status.uppercase(),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = when {
                                                isSuccess -> StatusPaid
                                                isFailed -> StatusOverdue
                                                else -> StatusPending
                                            },
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    if (isFailed) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            color = StatusPaid,
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier
                                                .clickable { onFixPayoutError(payout) }
                                                .testTag("fix_payout_error_${payout.id}")
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp))
                                                Spacer(modifier = Modifier.width(2.dp))
                                                Text("Fix", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${payout.destinationBank} • ${payout.recipientName.ifBlank { "Merchant Account" }}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (payout.destinationIban.isNotBlank()) {
                                    Text(
                                        text = "IBAN: ${payout.destinationIban.take(4)}••••${payout.destinationIban.takeLast(4)}",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = Formatters.formatCurrency(payout.amount, payout.currency),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OceanBlue
                                )
                                Text(
                                    text = Formatters.formatDate(payout.scheduledDate),
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(60.dp))
    }

    if (showCreateInvoiceSheet) {
        CreateInvoiceSheet(
            clients = clients,
            onDismiss = { showCreateInvoiceSheet = false },
            onSaveInvoice = { clientId, clientName, clientEmail, items, paymentTerms, notes, status, currency ->
                onSaveInvoice(clientId, clientName, clientEmail, items, paymentTerms, notes, status, currency)
            }
        )
    }

    selectedInvoiceForDetail?.let { inv ->
        InvoiceDetailSheet(
            invoice = inv,
            onDismiss = { selectedInvoiceForDetail = null },
            onMarkAsPaid = {
                onMarkAsPaid(it)
                selectedInvoiceForDetail = null
            },
            onRecordPayment = { invoice, amount, method, ref, notes ->
                onRecordPayment(invoice, amount, method, ref, notes)
                selectedInvoiceForDetail = null
            },
            onDelete = {
                onDeleteInvoice(it)
                selectedInvoiceForDetail = null
            }
        )
    }
}
