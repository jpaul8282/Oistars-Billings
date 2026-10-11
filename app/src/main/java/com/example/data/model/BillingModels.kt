package com.example.data.model

import org.json.JSONArray
import org.json.JSONObject

enum class InvoiceStatus(val label: String) {
    PAID("Paid"),
    PENDING("Pending"),
    OVERDUE("Overdue"),
    DRAFT("Draft");

    companion object {
        fun fromString(value: String): InvoiceStatus {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: PENDING
        }
    }
}

enum class BillingInterval(val label: String, val months: Int) {
    MONTHLY("Monthly", 1),
    QUARTERLY("Quarterly", 3),
    ANNUALLY("Annually", 12);

    companion object {
        fun fromString(value: String): BillingInterval {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: MONTHLY
        }
    }
}

enum class SubscriptionStatus(val label: String) {
    ACTIVE("Active"),
    PAUSED("Paused"),
    CANCELLED("Cancelled");

    companion object {
        fun fromString(value: String): SubscriptionStatus {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: ACTIVE
        }
    }
}

data class InvoiceItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val description: String,
    val quantity: Double = 1.0,
    val unitPrice: Double = 0.0,
    val taxRate: Double = 21.0, // Default 21% EU VAT
    val discountPercent: Double = 0.0
) {
    val subtotal: Double
        get() = quantity * unitPrice

    val discountAmount: Double
        get() = subtotal * (discountPercent / 100.0)

    val netAmount: Double
        get() = subtotal - discountAmount

    val taxAmount: Double
        get() = netAmount * (taxRate / 100.0)

    val totalAmount: Double
        get() = netAmount + taxAmount
}

object InvoiceItemSerializer {
    fun toJson(items: List<InvoiceItem>): String {
        val array = JSONArray()
        for (item in items) {
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("description", item.description)
            obj.put("quantity", item.quantity)
            obj.put("unitPrice", item.unitPrice)
            obj.put("taxRate", item.taxRate)
            obj.put("discountPercent", item.discountPercent)
            array.put(obj)
        }
        return array.toString()
    }

    fun fromJson(json: String?): List<InvoiceItem> {
        if (json.isNullOrBlank()) return emptyList()
        val list = mutableListOf<InvoiceItem>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    InvoiceItem(
                        id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                        description = obj.optString("description", ""),
                        quantity = obj.optDouble("quantity", 1.0),
                        unitPrice = obj.optDouble("unitPrice", 0.0),
                        taxRate = obj.optDouble("taxRate", 21.0),
                        discountPercent = obj.optDouble("discountPercent", 0.0)
                    )
                )
            }
        } catch (e: Exception) {
            // fallback safe list
        }
        return list
    }
}

enum class ClientActivityType(val label: String) {
    INVOICE_SENT("Invoice Sent"),
    PAYMENT_RECEIVED("Payment Received"),
    PAYMENT_OVERDUE("Payment Overdue"),
    INVOICE_DRAFT("Draft Created")
}

data class ClientActivityItem(
    val id: String,
    val clientId: String,
    val clientName: String,
    val invoiceId: String,
    val type: ClientActivityType,
    val title: String,
    val description: String,
    val timestamp: Long,
    val amount: Double,
    val currency: String = "EUR",
    val paymentMethod: String? = null
)

data class MonthlyRevenueTrend(
    val monthKey: String,
    val monthLabel: String,
    val monthFullLabel: String,
    val year: Int,
    val monthIndex: Int,
    val billedAmount: Double,
    val collectedAmount: Double,
    val outstandingAmount: Double,
    val invoiceCount: Int,
    val paidInvoiceCount: Int
) {
    val collectionRatePercent: Int
        get() = if (billedAmount > 0) ((collectedAmount / billedAmount) * 100).toInt().coerceIn(0, 100) else 0
}

data class ClientLedgerEntry(
    val id: String = "",
    val clientId: String = "",
    val clientName: String = "",
    val entryType: String = "INVOICE",
    val referenceId: String = "",
    val amount: Double = 0.0,
    val currency: String = "EUR",
    val balanceAfter: Double = 0.0,
    val status: String = "SETTLED",
    val description: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class ClientLedgerSummary(
    val clientId: String = "",
    val clientName: String = "",
    val totalBilled: Double = 0.0,
    val totalCollected: Double = 0.0,
    val outstandingBalance: Double = 0.0,
    val currency: String = "EUR",
    val lastUpdated: Long = System.currentTimeMillis(),
    val entriesCount: Int = 0
)

enum class PayoutStatus(val label: String) {
    PAID("Paid"),
    PROCESSING("Processing"),
    SCHEDULED("Scheduled"),
    FAILED("Failed"),
    CANCELLED("Cancelled");

    companion object {
        fun fromString(value: String): PayoutStatus {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: SCHEDULED
        }
    }
}

enum class PayoutDestinationType(val label: String) {
    SEPA_BANK("SEPA Bank Transfer"),
    SWIFT_INTERNATIONAL("SWIFT International"),
    STRIPE_CONNECT("Stripe Connect"),
    PAYPAL("PayPal Business");

    companion object {
        fun fromString(value: String): PayoutDestinationType {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: SEPA_BANK
        }
    }
}

data class PayoutMetrics(
    val availableBalance: Double = 0.0,
    val pendingPayoutBalance: Double = 0.0,
    val totalPaidOut: Double = 0.0,
    val totalPayoutsCount: Int = 0,
    val completedPayoutsCount: Int = 0,
    val processingPayoutsCount: Int = 0,
    val failedPayoutsCount: Int = 0,
    val nextScheduledPayoutDate: Long? = null,
    val estimatedFeeSaved: Double = 0.0
)

