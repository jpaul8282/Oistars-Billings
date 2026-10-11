package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.util.clearFocusOnTap
import com.example.data.local.ClientEntity
import com.example.data.local.InvoiceEntity
import com.example.data.model.InvoiceItem
import com.example.data.model.InvoiceStatus
import com.example.ui.components.ExportPdfDialog
import com.example.ui.components.Formatters
import com.example.ui.components.SearchBar
import com.example.ui.components.StatusBadge
import com.example.ui.theme.AccentGold
import com.example.ui.theme.NavyDark
import com.example.ui.theme.OceanBlue
import com.example.ui.theme.StatusDraft
import com.example.ui.theme.StatusOverdue
import com.example.ui.theme.StatusPaid
import com.example.ui.theme.StatusPending

enum class InvoiceSortOption(val title: String) {
    DATE_DESC("Date (Newest)"),
    DATE_ASC("Date (Oldest)"),
    DUE_DATE("Due Date"),
    AMOUNT_DESC("Amount (High to Low)"),
    AMOUNT_ASC("Amount (Low to High)")
}

data class FilterOption(
    val key: String,
    val label: String,
    val icon: ImageVector,
    val count: Int,
    val badgeColor: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoicesScreen(
    invoices: List<InvoiceEntity>,
    clients: List<ClientEntity>,
    selectedFilter: String,
    searchQuery: String,
    onFilterChange: (String) -> Unit,
    onSearchChange: (String) -> Unit,
    onMarkAsPaid: (InvoiceEntity) -> Unit,
    onRecordPayment: (InvoiceEntity, Double, String, String, String) -> Unit,
    onDeleteInvoice: (InvoiceEntity) -> Unit,
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
    var showCreateSheet by remember { mutableStateOf(false) }
    var selectedInvoiceForDetail by remember { mutableStateOf<InvoiceEntity?>(null) }
    var currentSortOption by remember { mutableStateOf(InvoiceSortOption.DATE_DESC) }
    var showSortMenu by remember { mutableStateOf(false) }

    val pendingCount = invoices.count { it.invoiceStatus == InvoiceStatus.PENDING }
    val paidCount = invoices.count { it.invoiceStatus == InvoiceStatus.PAID }
    val overdueCount = invoices.count { it.invoiceStatus == InvoiceStatus.OVERDUE }
    val draftCount = invoices.count { it.invoiceStatus == InvoiceStatus.DRAFT }

    val filterOptions = listOf(
        FilterOption("ALL", "All", Icons.Default.ReceiptLong, invoices.size, OceanBlue),
        FilterOption("PENDING", "Pending", Icons.Default.HourglassEmpty, pendingCount, StatusPending),
        FilterOption("PAID", "Paid", Icons.Default.CheckCircle, paidCount, StatusPaid),
        FilterOption("OVERDUE", "Overdue", Icons.Default.Warning, overdueCount, StatusOverdue),
        FilterOption("DRAFT", "Draft", Icons.Default.EditNote, draftCount, StatusDraft)
    )

    // Filter and sort invoices
    val filteredInvoices = remember(invoices, selectedFilter, searchQuery, currentSortOption) {
        val filtered = invoices.filter { invoice ->
            val matchesFilter = when (selectedFilter) {
                "ALL" -> true
                "PENDING" -> invoice.invoiceStatus == InvoiceStatus.PENDING
                "PAID" -> invoice.invoiceStatus == InvoiceStatus.PAID
                "OVERDUE" -> invoice.invoiceStatus == InvoiceStatus.OVERDUE
                "DRAFT" -> invoice.invoiceStatus == InvoiceStatus.DRAFT
                else -> true
            }

            val matchesSearch = if (searchQuery.isBlank()) true else {
                invoice.id.contains(searchQuery, ignoreCase = true) ||
                invoice.clientName.contains(searchQuery, ignoreCase = true) ||
                invoice.notes.contains(searchQuery, ignoreCase = true) ||
                invoice.items.any { it.description.contains(searchQuery, ignoreCase = true) }
            }

            matchesFilter && matchesSearch
        }

        when (currentSortOption) {
            InvoiceSortOption.DATE_DESC -> filtered.sortedByDescending { it.issueDate }
            InvoiceSortOption.DATE_ASC -> filtered.sortedBy { it.issueDate }
            InvoiceSortOption.DUE_DATE -> filtered.sortedBy { it.dueDate }
            InvoiceSortOption.AMOUNT_DESC -> filtered.sortedByDescending { it.totalAmount }
            InvoiceSortOption.AMOUNT_ASC -> filtered.sortedBy { it.totalAmount }
        }
    }

    val totalFilteredAmount = remember(filteredInvoices) {
        filteredInvoices.sumOf { it.totalAmount }
    }
    val totalFilteredBalance = remember(filteredInvoices) {
        filteredInvoices.sumOf { it.balanceDue }
    }
    val focusManager = LocalFocusManager.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .clearFocusOnTap(focusManager)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clearFocusOnTap(focusManager)
        ) {
            // Search Input Row
            PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 4.dp).let {
                Box(modifier = Modifier.padding(it)) {
                    SearchBar(
                        query = searchQuery,
                        onQueryChange = onSearchChange,
                        placeholder = "Search by invoice # or client name..."
                    )
                }
            }

            // Material 3 Filter Chips Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                filterOptions.forEach { option ->
                    val isSelected = selectedFilter == option.key

                    ElevatedFilterChip(
                        selected = isSelected,
                        onClick = { onFilterChange(option.key) },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = option.label,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = CircleShape,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else option.badgeColor.copy(alpha = 0.2f),
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "${option.count}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else option.badgeColor
                                        )
                                    }
                                }
                            }
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = if (isSelected) Icons.Default.Check else option.icon,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else option.badgeColor
                            )
                        },
                        colors = FilterChipDefaults.elevatedFilterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.testTag("filter_chip_${option.key.lowercase()}")
                    )
                }
            }

            // Summary Info & Sort Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${filteredInvoices.size} ${if (filteredInvoices.size == 1) "invoice" else "invoices"} • ${Formatters.formatCurrency(totalFilteredAmount)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (totalFilteredBalance > 0.0 && selectedFilter != "PAID") {
                        Text(
                            text = "${Formatters.formatCurrency(totalFilteredBalance)} balance unpaid",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 11.sp
                        )
                    }
                }

                // Sort Action
                Box {
                    OutlinedButton(
                        onClick = { showSortMenu = true },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("sort_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sort,
                            contentDescription = "Sort invoices",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = currentSortOption.title.substringBefore(" ("),
                            fontSize = 12.sp
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        InvoiceSortOption.entries.forEach { option ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = option.title,
                                        fontWeight = if (currentSortOption == option) FontWeight.Bold else FontWeight.Normal,
                                        color = if (currentSortOption == option) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                onClick = {
                                    currentSortOption = option
                                    showSortMenu = false
                                },
                                leadingIcon = {
                                    if (currentSortOption == option) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // Overdue Attention Banner if not in OVERDUE filter and overdue invoices exist
            if (selectedFilter != "OVERDUE" && overdueCount > 0) {
                Surface(
                    color = StatusOverdue.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clickable { onFilterChange("OVERDUE") }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = StatusOverdue,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "$overdueCount overdue ${if (overdueCount == 1) "invoice requires" else "invoices require"} attention",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = StatusOverdue
                            )
                        }
                        Text(
                            text = "View >",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = StatusOverdue
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Invoice List or Empty State
            if (filteredInvoices.isEmpty()) {
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
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(72.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.ReceiptLong,
                                    contentDescription = null,
                                    modifier = Modifier.size(36.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Text(
                            text = when {
                                searchQuery.isNotBlank() -> "No invoices found for '$searchQuery'"
                                selectedFilter == "OVERDUE" -> "No overdue invoices! All payments are on track."
                                selectedFilter == "PENDING" -> "No pending invoices awaiting payment."
                                selectedFilter == "PAID" -> "No paid invoices recorded yet."
                                selectedFilter == "DRAFT" -> "No draft invoices."
                                else -> "No invoices found in database"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        Text(
                            text = if (searchQuery.isNotBlank() || selectedFilter != "ALL") {
                                "Try changing filters or clear your search term."
                            } else {
                                "Create your first professional invoice to start tracking payments."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (selectedFilter != "ALL" || searchQuery.isNotBlank()) {
                                OutlinedButton(
                                    onClick = {
                                        onFilterChange("ALL")
                                        onSearchChange("")
                                    }
                                ) {
                                    Text("Reset Filters")
                                }
                            }

                            TextButton(onClick = { showCreateSheet = true }) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("New Invoice")
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredInvoices, key = { it.id }) { invoice ->
                        InvoiceListItemCard(
                            invoice = invoice,
                            onClick = { selectedInvoiceForDetail = invoice },
                            onMarkAsPaid = { onMarkAsPaid(invoice) }
                        )
                    }
                }
            }
        }

        // Material 3 Extended Floating Action Button
        ExtendedFloatingActionButton(
            onClick = { showCreateSheet = true },
            containerColor = OceanBlue,
            contentColor = Color.White,
            icon = { Icon(Icons.Default.Add, contentDescription = "Create Invoice") },
            text = { Text("New Invoice", fontWeight = FontWeight.Bold) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("create_invoice_fab")
        )
    }

    if (showCreateSheet) {
        CreateInvoiceSheet(
            clients = clients,
            onDismiss = { showCreateSheet = false },
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

@Composable
fun InvoiceListItemCard(
    invoice: InvoiceEntity,
    onClick: () -> Unit,
    onMarkAsPaid: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showExportPdf by remember { mutableStateOf(false) }

    val isOverdue = invoice.invoiceStatus == InvoiceStatus.OVERDUE
    val isPending = invoice.invoiceStatus == InvoiceStatus.PENDING
    val isPaid = invoice.invoiceStatus == InvoiceStatus.PAID
    val isDraft = invoice.invoiceStatus == InvoiceStatus.DRAFT

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("invoice_card_${invoice.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Row: Client Avatar Initial + Invoice ID + Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Initial Avatar
                    Surface(
                        shape = CircleShape,
                        color = when (invoice.invoiceStatus) {
                            InvoiceStatus.PAID -> StatusPaid.copy(alpha = 0.15f)
                            InvoiceStatus.OVERDUE -> StatusOverdue.copy(alpha = 0.15f)
                            InvoiceStatus.PENDING -> StatusPending.copy(alpha = 0.15f)
                            InvoiceStatus.DRAFT -> StatusDraft.copy(alpha = 0.15f)
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = invoice.clientName.firstOrNull()?.uppercase() ?: "C",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = when (invoice.invoiceStatus) {
                                    InvoiceStatus.PAID -> StatusPaid
                                    InvoiceStatus.OVERDUE -> StatusOverdue
                                    InvoiceStatus.PENDING -> StatusPending
                                    InvoiceStatus.DRAFT -> StatusDraft
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = invoice.id,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Issued: ${Formatters.formatShortDate(invoice.issueDate)}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                StatusBadge(status = invoice.invoiceStatus)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Client Name & Summary of Items
            Text(
                text = invoice.clientName,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            val firstItem = invoice.items.firstOrNull()?.description
            if (!firstItem.isNullOrBlank()) {
                Text(
                    text = if (invoice.items.size > 1) "$firstItem (+${invoice.items.size - 1} other item${if (invoice.items.size > 2) "s" else ""})" else firstItem,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Payment Progress Bar if partially paid
            if (invoice.amountPaid > 0.0 && invoice.amountPaid < invoice.totalAmount) {
                Spacer(modifier = Modifier.height(8.dp))
                val progress = (invoice.amountPaid / invoice.totalAmount).toFloat().coerceIn(0f, 1f)
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Paid: ${Formatters.formatCurrency(invoice.amountPaid, invoice.currency)} (${(progress * 100).toInt()}%)",
                            fontSize = 10.sp,
                            color = StatusPaid,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Remaining: ${Formatters.formatCurrency(invoice.balanceDue, invoice.currency)}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = StatusPaid,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom Row: Total Amount & Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = Formatters.formatCurrency(invoice.totalAmount, invoice.currency),
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Due date descriptor with status styling
                    when {
                        isPaid -> {
                            Text(
                                text = "Settled in full",
                                fontSize = 11.sp,
                                color = StatusPaid,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        isOverdue -> {
                            val daysOverdue = ((System.currentTimeMillis() - invoice.dueDate) / (1000 * 60 * 60 * 24)).coerceAtLeast(1)
                            Text(
                                text = "Overdue by $daysOverdue day${if (daysOverdue > 1) "s" else ""}",
                                fontSize = 11.sp,
                                color = StatusOverdue,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        isPending -> {
                            val daysUntilDue = ((invoice.dueDate - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)).coerceAtLeast(0)
                            Text(
                                text = if (daysUntilDue == 0L) "Due today" else "Due in $daysUntilDue days",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        isDraft -> {
                            Text(
                                text = "Unsent draft",
                                fontSize = 11.sp,
                                color = StatusDraft
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Export PDF icon button (touch target >= 48dp)
                    IconButton(
                        onClick = { showExportPdf = true },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("invoice_card_pdf_btn_${invoice.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = "Export PDF for ${invoice.id}",
                            tint = OceanBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Quick Mark Paid button
                    if (isPending || isOverdue) {
                        OutlinedButton(
                            onClick = onMarkAsPaid,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("invoice_card_mark_paid_${invoice.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = StatusPaid,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Mark Paid",
                                fontSize = 12.sp,
                                color = StatusPaid,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.ArrowForwardIos,
                        contentDescription = "Open details",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier
                            .size(14.dp)
                            .padding(start = 2.dp)
                    )
                }
            }
        }
    }

    if (showExportPdf) {
        ExportPdfDialog(
            invoice = invoice,
            onDismissRequest = { showExportPdf = false }
        )
    }
}

