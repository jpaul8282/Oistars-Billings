package com.example.ui.components

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ClientEntity
import com.example.data.local.InvoiceEntity
import com.example.data.local.SubscriptionEntity
import com.example.data.model.BillingInterval
import com.example.data.model.InvoiceStatus
import com.example.data.model.SubscriptionStatus
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

/**
 * Reusable ClientProfile Component:
 * Displays:
 * 1. Client details (store identity, VAT ID, contact details, address, payment terms)
 * 2. Total billing history (total billed, total collected, overdue balance, invoice list)
 * 3. Current subscription status (active retainer, plan name, recurring fee, renewal date, bill now action)
 */
@Composable
fun ClientProfile(
    client: ClientEntity,
    invoices: List<InvoiceEntity>,
    subscriptions: List<SubscriptionEntity> = emptyList(),
    onEditClient: (() -> Unit)? = null,
    onDeleteClient: (() -> Unit)? = null,
    onCreateInvoice: (() -> Unit)? = null,
    onInvoiceClick: ((InvoiceEntity) -> Unit)? = null,
    onGenerateInvoiceFromSubscription: ((SubscriptionEntity) -> Unit)? = null,
    onManageSubscription: ((SubscriptionEntity) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Overview & Details, 1: Billing History, 2: Subscriptions

    // Aggregate Client Billing History Metrics
    val clientInvoices = remember(invoices, client.id) {
        invoices.filter { it.clientId == client.id }.sortedByDescending { it.issueDate }
    }

    val totalBilled = remember(clientInvoices) {
        clientInvoices.sumOf { it.totalAmount }
    }
    val totalPaid = remember(clientInvoices) {
        clientInvoices.filter { it.invoiceStatus == InvoiceStatus.PAID }.sumOf { it.totalAmount }
    }
    val totalOutstanding = remember(clientInvoices) {
        clientInvoices.sumOf { it.balanceDue }
    }
    val paidCount = remember(clientInvoices) {
        clientInvoices.count { it.invoiceStatus == InvoiceStatus.PAID }
    }
    val pendingCount = remember(clientInvoices) {
        clientInvoices.count { it.invoiceStatus == InvoiceStatus.PENDING }
    }
    val overdueCount = remember(clientInvoices) {
        clientInvoices.count { it.invoiceStatus == InvoiceStatus.OVERDUE }
    }

    // Client Subscriptions
    val clientSubscriptions = remember(subscriptions, client.id) {
        subscriptions.filter { it.clientId == client.id }
    }
    val activeSubscription = remember(clientSubscriptions) {
        clientSubscriptions.firstOrNull { it.subscriptionStatus == SubscriptionStatus.ACTIVE }
            ?: clientSubscriptions.firstOrNull()
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("client_profile_container")
    ) {
        // --- Header Section: Client Identity Banner ---
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar Initials Circle
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(NavyDark),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = client.name.take(2).uppercase(),
                            color = AccentGold,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = client.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = client.id,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )

                            if (client.taxNumber.isNotBlank()) {
                                Text(
                                    text = " • VAT: ${client.taxNumber}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Ledger Health Pill
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            when {
                                overdueCount > 0 -> {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = StatusOverdueBg
                                    ) {
                                        Text(
                                            text = "$overdueCount Overdue Invoices",
                                            color = StatusOverdue,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                pendingCount > 0 -> {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = StatusPendingBg
                                    ) {
                                        Text(
                                            text = "$pendingCount Awaiting Payment",
                                            color = StatusPending,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                clientInvoices.isNotEmpty() -> {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = StatusPaidBg
                                    ) {
                                        Text(
                                            text = "All Invoices Settled",
                                            color = StatusPaid,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                else -> {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        Text(
                                            text = "New Store",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            // Active subscription indicator badge
                            if (activeSubscription != null && activeSubscription.subscriptionStatus == SubscriptionStatus.ACTIVE) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = OceanBlue.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = "Active Retainer",
                                        color = OceanBlue,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Quick Action Button Row
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (onCreateInvoice != null) {
                        Button(
                            onClick = onCreateInvoice,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("client_profile_new_invoice_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = OceanBlue),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New Invoice", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    if (onEditClient != null) {
                        OutlinedButton(
                            onClick = onEditClient,
                            modifier = Modifier.testTag("client_profile_edit_btn"),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Edit", fontSize = 13.sp)
                        }
                    }

                    if (onDeleteClient != null) {
                        OutlinedButton(
                            onClick = onDeleteClient,
                            modifier = Modifier.testTag("client_profile_delete_btn"),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusOverdue),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- Navigation Tabs ---
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            edgePadding = 0.dp,
            containerColor = Color.Transparent,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Details", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) },
                icon = { Icon(Icons.Default.Business, contentDescription = null, modifier = Modifier.size(16.dp)) },
                modifier = Modifier.testTag("client_profile_tab_details")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Billing History (${clientInvoices.size})", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) },
                icon = { Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(16.dp)) },
                modifier = Modifier.testTag("client_profile_tab_history")
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Subscription", fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) },
                icon = { Icon(Icons.Default.Autorenew, contentDescription = null, modifier = Modifier.size(16.dp)) },
                modifier = Modifier.testTag("client_profile_tab_subscription")
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- Tab Content ---
        when (selectedTab) {
            0 -> {
                // Section 1: Client Details
                ClientDetailsSection(client = client)
            }
            1 -> {
                // Section 2: Total Billing History
                ClientBillingHistorySection(
                    client = client,
                    invoices = clientInvoices,
                    totalBilled = totalBilled,
                    totalPaid = totalPaid,
                    totalOutstanding = totalOutstanding,
                    paidCount = paidCount,
                    pendingCount = pendingCount,
                    overdueCount = overdueCount,
                    onInvoiceClick = onInvoiceClick
                )
            }
            2 -> {
                // Section 3: Current Subscription Status
                ClientSubscriptionSection(
                    client = client,
                    subscriptions = clientSubscriptions,
                    onGenerateInvoice = onGenerateInvoiceFromSubscription,
                    onManageSubscription = onManageSubscription
                )
            }
        }
    }
}

/**
 * Client Details Section
 */
@Composable
private fun ClientDetailsSection(client: ClientEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Contact & Corporate Profile",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            HorizontalDivider()

            if (client.contactPerson.isNotBlank()) {
                ProfileInfoRow(
                    icon = Icons.Default.Business,
                    label = "Contact Person",
                    value = client.contactPerson
                )
            }

            if (client.email.isNotBlank()) {
                ProfileInfoRow(
                    icon = Icons.Default.Email,
                    label = "Billing Email",
                    value = client.email
                )
            }

            if (client.phone.isNotBlank()) {
                ProfileInfoRow(
                    icon = Icons.Default.Call,
                    label = "Phone",
                    value = client.phone
                )
            }

            if (client.taxNumber.isNotBlank()) {
                ProfileInfoRow(
                    icon = Icons.Default.VerifiedUser,
                    label = "VAT / Tax Registration",
                    value = client.taxNumber
                )
            }

            if (client.address.isNotBlank() || client.country.isNotBlank()) {
                ProfileInfoRow(
                    icon = Icons.Default.LocationOn,
                    label = "Billing Address",
                    value = listOf(client.address, client.country).filter { it.isNotBlank() }.joinToString(", ")
                )
            }

            ProfileInfoRow(
                icon = Icons.Default.Schedule,
                label = "Payment Terms",
                value = "Net ${client.paymentTermsDays} Days (Currency: ${client.currency})"
            )

            if (client.notes.isNotBlank()) {
                ProfileInfoRow(
                    icon = Icons.Default.Notes,
                    label = "Internal Notes",
                    value = client.notes
                )
            }
        }
    }
}

/**
 * Client Billing History Section with KPIs and Invoice List
 */
@Composable
private fun ClientBillingHistorySection(
    client: ClientEntity,
    invoices: List<InvoiceEntity>,
    totalBilled: Double,
    totalPaid: Double,
    totalOutstanding: Double,
    paidCount: Int,
    pendingCount: Int,
    overdueCount: Int,
    onInvoiceClick: ((InvoiceEntity) -> Unit)?
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // High-level financial KPIs
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Total Billing Ledger",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BillingKpiBox(
                        title = "Total Billed",
                        amount = totalBilled,
                        currency = client.currency,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    BillingKpiBox(
                        title = "Collected (Paid)",
                        amount = totalPaid,
                        currency = client.currency,
                        color = StatusPaid,
                        modifier = Modifier.weight(1f)
                    )
                    BillingKpiBox(
                        title = "Outstanding",
                        amount = totalOutstanding,
                        currency = client.currency,
                        color = if (overdueCount > 0) StatusOverdue else StatusPending,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Breakdown Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatusCountPill(label = "Paid", count = paidCount, color = StatusPaid, bg = StatusPaidBg)
                    StatusCountPill(label = "Pending", count = pendingCount, color = StatusPending, bg = StatusPendingBg)
                    StatusCountPill(label = "Overdue", count = overdueCount, color = StatusOverdue, bg = StatusOverdueBg)
                }
            }
        }

        // Invoice List
        Text(
            text = "Invoices History (${invoices.size})",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )

        if (invoices.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Receipt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No invoices issued for ${client.name} yet.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            invoices.forEach { invoice ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = onInvoiceClick != null) { onInvoiceClick?.invoke(invoice) }
                        .testTag("client_profile_invoice_${invoice.id}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = invoice.id,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                StatusBadge(status = invoice.invoiceStatus)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Issued: ${Formatters.formatShortDate(invoice.issueDate)} • Due: ${Formatters.formatShortDate(invoice.dueDate)}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (invoice.balanceDue > 0.0 && invoice.invoiceStatus != InvoiceStatus.PAID) {
                                Text(
                                    text = "Balance Due: ${Formatters.formatCurrency(invoice.balanceDue, invoice.currency)}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (invoice.invoiceStatus == InvoiceStatus.OVERDUE) StatusOverdue else StatusPending
                                )
                            }
                        }

                        Text(
                            text = Formatters.formatCurrency(invoice.totalAmount, invoice.currency),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Current Subscription Status Section
 */
@Composable
private fun ClientSubscriptionSection(
    client: ClientEntity,
    subscriptions: List<SubscriptionEntity>,
    onGenerateInvoice: ((SubscriptionEntity) -> Unit)?,
    onManageSubscription: ((SubscriptionEntity) -> Unit)?
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (subscriptions.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(OceanBlue.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Autorenew,
                            contentDescription = null,
                            tint = OceanBlue,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "No Active Subscription Retainer",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "${client.name} is currently billed per single invoice.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            subscriptions.forEach { sub ->
                val isActive = sub.subscriptionStatus == SubscriptionStatus.ACTIVE

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = sub.planName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Plan ID: ${sub.id}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Subscription Status Badge
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isActive) StatusPaidBg else StatusDraftBg
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(if (isActive) StatusPaid else StatusDraft)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = sub.subscriptionStatus.name,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isActive) StatusPaid else StatusDraft
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Amount & Frequency Strip
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Recurring Amount",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${Formatters.formatCurrency(sub.amount, sub.currency)} / ${sub.billingInterval.label.lowercase()}",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = OceanBlue
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Next Billing Cycle",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = Formatters.formatDate(sub.nextBillingDate),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        if (sub.notes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Notes: ${sub.notes}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Action Buttons: Bill Now
                        if (onGenerateInvoice != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Button(
                                    onClick = { onGenerateInvoice(sub) },
                                    modifier = Modifier.testTag("client_profile_bill_sub_btn"),
                                    colors = ButtonDefaults.buttonColors(containerColor = OceanBlue),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Bill Now (Generate Invoice)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileInfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = OceanBlue,
            modifier = Modifier
                .size(18.dp)
                .padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = label,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun BillingKpiBox(
    title: String,
    amount: Double,
    currency: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(text = title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = Formatters.formatCurrency(amount, currency),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun StatusCountPill(
    label: String,
    count: Int,
    color: Color,
    bg: Color
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bg
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = "$label: $count",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

/**
 * Dialog wrapper for the reusable ClientProfile component
 */
@Composable
fun ClientProfileDialog(
    client: ClientEntity,
    invoices: List<InvoiceEntity>,
    subscriptions: List<SubscriptionEntity> = emptyList(),
    onDismiss: () -> Unit,
    onEditClient: (() -> Unit)? = null,
    onDeleteClient: (() -> Unit)? = null,
    onCreateInvoice: (() -> Unit)? = null,
    onInvoiceClick: ((InvoiceEntity) -> Unit)? = null,
    onGenerateInvoiceFromSubscription: ((SubscriptionEntity) -> Unit)? = null
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = null,
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                ClientProfile(
                    client = client,
                    invoices = invoices,
                    subscriptions = subscriptions,
                    onEditClient = onEditClient,
                    onDeleteClient = onDeleteClient,
                    onCreateInvoice = onCreateInvoice,
                    onInvoiceClick = onInvoiceClick,
                    onGenerateInvoiceFromSubscription = onGenerateInvoiceFromSubscription
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
