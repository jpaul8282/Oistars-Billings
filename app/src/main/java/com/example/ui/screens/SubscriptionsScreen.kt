package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ClientEntity
import com.example.data.local.SubscriptionEntity
import com.example.data.model.BillingInterval
import com.example.data.model.SubscriptionStatus
import com.example.ui.components.Formatters
import com.example.ui.theme.NavyDark
import com.example.ui.theme.OceanBlue
import com.example.ui.theme.StatusDraft
import com.example.ui.theme.StatusDraftBg
import com.example.ui.theme.StatusOverdue
import com.example.ui.theme.StatusPaid
import com.example.ui.theme.StatusPaidBg
import com.example.ui.theme.StatusPending
import com.example.ui.theme.StatusPendingBg

enum class SubscriptionFilter(val label: String) {
    ALL("All"),
    ACTIVE("Active"),
    PAUSED("Paused")
}

@Composable
fun SubscriptionsScreen(
    subscriptions: List<SubscriptionEntity>,
    clients: List<ClientEntity>,
    onSaveSubscription: (
        id: String?,
        clientId: String,
        clientName: String,
        planName: String,
        amount: Double,
        currency: String,
        interval: String,
        status: String,
        notes: String
    ) -> Unit,
    onDeleteSubscription: (SubscriptionEntity) -> Unit,
    onGenerateInvoice: (SubscriptionEntity) -> Unit,
    onToggleSubscription: (SubscriptionEntity, Boolean) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingSub by remember { mutableStateOf<SubscriptionEntity?>(null) }
    var subToDelete by remember { mutableStateOf<SubscriptionEntity?>(null) }
    var selectedFilter by remember { mutableStateOf(SubscriptionFilter.ALL) }
    var searchQuery by remember { mutableStateOf("") }

    // Financial Metrics
    val activeSubscriptions = subscriptions.filter { it.subscriptionStatus == SubscriptionStatus.ACTIVE }
    val pausedSubscriptions = subscriptions.filter { it.subscriptionStatus == SubscriptionStatus.PAUSED }

    val monthlyRecurringRevenue = activeSubscriptions.fold(0.0) { acc, sub ->
        acc + when (sub.billingInterval) {
            BillingInterval.MONTHLY -> sub.amount
            BillingInterval.QUARTERLY -> sub.amount / 3.0
            BillingInterval.ANNUALLY -> sub.amount / 12.0
        }
    }

    val filteredSubscriptions = subscriptions.filter { sub ->
        val matchesFilter = when (selectedFilter) {
            SubscriptionFilter.ALL -> true
            SubscriptionFilter.ACTIVE -> sub.subscriptionStatus == SubscriptionStatus.ACTIVE
            SubscriptionFilter.PAUSED -> sub.subscriptionStatus != SubscriptionStatus.ACTIVE
        }
        val matchesSearch = searchQuery.isBlank() ||
                sub.clientName.contains(searchQuery, ignoreCase = true) ||
                sub.planName.contains(searchQuery, ignoreCase = true)
        matchesFilter && matchesSearch
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Metrics Header Banner
            SubscriptionMetricsHeader(
                activeCount = activeSubscriptions.size,
                pausedCount = pausedSubscriptions.size,
                monthlyRecurringRevenue = monthlyRecurringRevenue,
                totalCount = subscriptions.size
            )

            // Search & Filter Row
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by client or plan name...", fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear search",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("subscription_search_input")
                )

                // Filter Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SubscriptionFilter.entries.forEach { filter ->
                        val count = when (filter) {
                            SubscriptionFilter.ALL -> subscriptions.size
                            SubscriptionFilter.ACTIVE -> activeSubscriptions.size
                            SubscriptionFilter.PAUSED -> pausedSubscriptions.size
                        }
                        FilterChip(
                            selected = selectedFilter == filter,
                            onClick = { selectedFilter = filter },
                            label = { Text("${filter.label} ($count)", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = OceanBlue.copy(alpha = 0.15f),
                                selectedLabelColor = OceanBlue
                            ),
                            modifier = Modifier.testTag("filter_chip_${filter.name.lowercase()}")
                        )
                    }
                }
            }

            // Subscription List / Empty State
            if (filteredSubscriptions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Autorenew,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = if (subscriptions.isEmpty()) "No recurring subscriptions configured" else "No matching subscriptions found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (subscriptions.isEmpty())
                                "Set up retainers and recurring billing schedules for your merchant clients."
                            else
                                "Try adjusting your search terms or filter selection.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (subscriptions.isEmpty()) {
                            Button(
                                onClick = { showAddDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = OceanBlue),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("empty_add_subscription_btn")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Add Recurring Retainer")
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("subscription_list"),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredSubscriptions, key = { it.id }) { sub ->
                        SubscriptionItemCard(
                            subscription = sub,
                            onToggleActivity = { isActive ->
                                onToggleSubscription(sub, isActive)
                            },
                            onGenerateInvoice = { onGenerateInvoice(sub) },
                            onEdit = { editingSub = sub },
                            onDelete = { subToDelete = sub }
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = OceanBlue,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_subscription_fab")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Retainer")
                Spacer(modifier = Modifier.width(6.dp))
                Text("New Retainer", fontWeight = FontWeight.Bold)
            }
        }
    }

    // Add / Edit Dialog
    if (showAddDialog || editingSub != null) {
        SubscriptionFormDialog(
            subscription = editingSub,
            clients = clients,
            onDismiss = {
                showAddDialog = false
                editingSub = null
            },
            onSave = { id, cId, cName, plan, amt, curr, interval, status, notes ->
                onSaveSubscription(id, cId, cName, plan, amt, curr, interval, status, notes)
                showAddDialog = false
                editingSub = null
            }
        )
    }

    // Delete Confirmation Dialog
    subToDelete?.let { sub ->
        AlertDialog(
            onDismissRequest = { subToDelete = null },
            title = { Text("Delete Subscription?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Are you sure you want to delete the retainer '${sub.planName}' for ${sub.clientName}? Historical invoices created from this plan will remain untouched.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteSubscription(sub)
                        subToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusOverdue)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { subToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SubscriptionMetricsHeader(
    activeCount: Int,
    pausedCount: Int,
    monthlyRecurringRevenue: Double,
    totalCount: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(OceanBlue.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = OceanBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Recurring Retainers",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Automated subscription contracts",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    color = StatusPaidBg,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "$activeCount of $totalCount Active",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = StatusPaid,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Estimated MRR",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = Formatters.formatCurrency(monthlyRecurringRevenue, "EUR"),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = OceanBlue
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Paused Retainers",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$pausedCount",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (pausedCount > 0) StatusPending else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun SubscriptionItemCard(
    subscription: SubscriptionEntity,
    onToggleActivity: (Boolean) -> Unit,
    onGenerateInvoice: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isActive = subscription.subscriptionStatus == SubscriptionStatus.ACTIVE
    val now = System.currentTimeMillis()
    val daysUntilBilling = ((subscription.nextBillingDate - now) / (1000 * 60 * 60 * 24)).toInt()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("subscription_card_${subscription.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Client, Plan & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = subscription.clientName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = subscription.planName,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Status Badge with icon
                Surface(
                    color = when (subscription.subscriptionStatus) {
                        SubscriptionStatus.ACTIVE -> StatusPaidBg
                        SubscriptionStatus.PAUSED -> StatusPendingBg
                        SubscriptionStatus.CANCELLED -> StatusDraftBg
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = when (subscription.subscriptionStatus) {
                                SubscriptionStatus.ACTIVE -> Icons.Default.CheckCircle
                                SubscriptionStatus.PAUSED -> Icons.Default.PauseCircle
                                SubscriptionStatus.CANCELLED -> Icons.Default.Autorenew
                            },
                            contentDescription = null,
                            tint = when (subscription.subscriptionStatus) {
                                SubscriptionStatus.ACTIVE -> StatusPaid
                                SubscriptionStatus.PAUSED -> StatusPending
                                SubscriptionStatus.CANCELLED -> StatusDraft
                            },
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = subscription.subscriptionStatus.label,
                            color = when (subscription.subscriptionStatus) {
                                SubscriptionStatus.ACTIVE -> StatusPaid
                                SubscriptionStatus.PAUSED -> StatusPending
                                SubscriptionStatus.CANCELLED -> StatusDraft
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Pricing & Next Billing Details Box
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
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
                            text = "Rate / Cycle",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${Formatters.formatCurrency(subscription.amount, subscription.currency)} / ${subscription.billingInterval.label.lowercase()}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = OceanBlue
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Next Billing",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = Formatters.formatDate(subscription.nextBillingDate),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = when {
                                daysUntilBilling < 0 -> "Overdue for invoice"
                                daysUntilBilling == 0 -> "Billed Today"
                                daysUntilBilling == 1 -> "Tomorrow"
                                else -> "In $daysUntilBilling days"
                            },
                            fontSize = 10.sp,
                            color = if (daysUntilBilling <= 0) StatusPending else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Interactive Activity Toggle & Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Subscription Activity Toggle Switch
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Switch(
                        checked = isActive,
                        onCheckedChange = onToggleActivity,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = StatusPaid,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier.testTag("toggle_subscription_switch_${subscription.id}")
                    )
                    Text(
                        text = if (isActive) "Active" else "Paused",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isActive) StatusPaid else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Action Buttons: Bill Now, Edit, Delete
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Button(
                        onClick = onGenerateInvoice,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OceanBlue),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("bill_now_btn_${subscription.id}")
                    ) {
                        Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Bill Now", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("edit_subscription_btn_${subscription.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Subscription",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("delete_subscription_btn_${subscription.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Subscription",
                            tint = StatusOverdue,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionFormDialog(
    subscription: SubscriptionEntity?,
    clients: List<ClientEntity>,
    onDismiss: () -> Unit,
    onSave: (
        id: String?,
        clientId: String,
        clientName: String,
        planName: String,
        amount: Double,
        currency: String,
        interval: String,
        status: String,
        notes: String
    ) -> Unit
) {
    var selectedClientId by remember { mutableStateOf(subscription?.clientId ?: clients.firstOrNull()?.id ?: "") }
    var selectedClientName by remember { mutableStateOf(subscription?.clientName ?: clients.firstOrNull()?.name ?: "") }
    var planName by remember { mutableStateOf(subscription?.planName ?: "Managed E-Commerce Retainer") }
    var amountText by remember { mutableStateOf(subscription?.amount?.toString() ?: "500.00") }
    var selectedInterval by remember { mutableStateOf(subscription?.interval ?: BillingInterval.MONTHLY.name) }
    var isActiveStatus by remember { mutableStateOf(subscription?.status != SubscriptionStatus.PAUSED.name) }
    var notes by remember { mutableStateOf(subscription?.notes ?: "") }

    var clientDropdownExpanded by remember { mutableStateOf(false) }
    var intervalDropdownExpanded by remember { mutableStateOf(false) }

    val intervals = BillingInterval.entries.map { it.name to it.label }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (subscription == null) "New Recurring Retainer" else "Edit Retainer Plan",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Client Selector
                ExposedDropdownMenuBox(
                    expanded = clientDropdownExpanded,
                    onExpandedChange = { clientDropdownExpanded = !clientDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedClientName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Client Store") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = clientDropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = clientDropdownExpanded,
                        onDismissRequest = { clientDropdownExpanded = false }
                    ) {
                        clients.forEach { c ->
                            DropdownMenuItem(
                                text = { Text(c.name) },
                                onClick = {
                                    selectedClientId = c.id
                                    selectedClientName = c.name
                                    clientDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = planName,
                    onValueChange = { planName = it },
                    label = { Text("Plan / Retainer Title *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text("Amount (€)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    ExposedDropdownMenuBox(
                        expanded = intervalDropdownExpanded,
                        onExpandedChange = { intervalDropdownExpanded = !intervalDropdownExpanded },
                        modifier = Modifier.weight(1.2f)
                    ) {
                        OutlinedTextField(
                            value = intervals.firstOrNull { it.first == selectedInterval }?.second ?: "Monthly",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Billing Cycle") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = intervalDropdownExpanded) },
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        )
                        ExposedDropdownMenu(
                            expanded = intervalDropdownExpanded,
                            onDismissRequest = { intervalDropdownExpanded = false }
                        ) {
                            intervals.forEach { (key, label) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        selectedInterval = key
                                        intervalDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Initial Status Switch in Form
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Active Schedule", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text("Include in recurring billing cycles", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = isActiveStatus,
                        onCheckedChange = { isActiveStatus = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = StatusPaid)
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / SLA Terms") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 500.0
                    val finalStatus = if (isActiveStatus) SubscriptionStatus.ACTIVE.name else SubscriptionStatus.PAUSED.name
                    onSave(
                        subscription?.id,
                        selectedClientId,
                        selectedClientName,
                        planName,
                        amt,
                        "EUR",
                        selectedInterval,
                        finalStatus,
                        notes
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = OceanBlue),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Save Retainer", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
