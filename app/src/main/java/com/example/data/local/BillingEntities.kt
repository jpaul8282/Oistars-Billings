package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.model.BillingInterval
import com.example.data.model.InvoiceItem
import com.example.data.model.InvoiceItemSerializer
import com.example.data.model.InvoiceStatus
import com.example.data.model.SubscriptionStatus

@Entity(
    tableName = "clients",
    indices = [
        Index(value = ["name"]),
        Index(value = ["email"])
    ]
)
data class ClientEntity(
    @PrimaryKey
    val id: String = "CLI-${System.currentTimeMillis() % 100000}",
    val name: String,
    val contactPerson: String = "",
    val email: String = "",
    val phone: String = "",
    val taxNumber: String = "", // VAT / Tax ID
    val address: String = "",
    val country: String = "Netherlands",
    val currency: String = "EUR",
    val paymentTermsDays: Int = 14,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "invoices",
    indices = [
        Index(value = ["clientId"]),
        Index(value = ["customerId"]),
        Index(value = ["status"]),
        Index(value = ["dueDate"])
    ]
)
data class InvoiceEntity(
    @PrimaryKey
    val id: String = "INV-${(System.currentTimeMillis() % 100000).toString().padStart(5, '0')}",
    val clientId: String = "",
    val customerId: String = clientId,
    val clientName: String = "",
    val clientEmail: String = "",
    val status: String = InvoiceStatus.PENDING.name, // PAID, PENDING, OVERDUE, DRAFT
    val issueDate: Long = System.currentTimeMillis(),
    val dueDate: Long = System.currentTimeMillis() + (14L * 24 * 60 * 60 * 1000),
    val currency: String = "EUR",
    val itemsJson: String = "[]",
    val subtotal: Double = 0.0,
    val taxTotal: Double = 0.0,
    val discountTotal: Double = 0.0,
    val totalAmount: Double = 0.0,
    val amount: Double = totalAmount,
    val amountPaid: Double = 0.0,
    val paymentTerms: String = "Net 14 Days",
    val notes: String = "",
    val paidAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    @androidx.room.Ignore
    constructor(
        id: String = "INV-${(System.currentTimeMillis() % 100000).toString().padStart(5, '0')}",
        customerId: String,
        amount: Double,
        status: String = InvoiceStatus.PENDING.name,
        dueDate: Long = System.currentTimeMillis() + (14L * 24 * 60 * 60 * 1000)
    ) : this(
        id = id,
        clientId = customerId,
        customerId = customerId,
        clientName = "",
        clientEmail = "",
        status = status,
        issueDate = System.currentTimeMillis(),
        dueDate = dueDate,
        currency = "EUR",
        itemsJson = "[]",
        subtotal = amount,
        taxTotal = 0.0,
        discountTotal = 0.0,
        totalAmount = amount,
        amount = amount,
        amountPaid = 0.0,
        paymentTerms = "Net 14 Days",
        notes = "",
        paidAt = null,
        createdAt = System.currentTimeMillis()
    )

    val items: List<InvoiceItem>
        get() = InvoiceItemSerializer.fromJson(itemsJson)

    val invoiceStatus: InvoiceStatus
        get() {
            val s = InvoiceStatus.fromString(status)
            val effectiveTotal = if (totalAmount > 0.0) totalAmount else amount
            if (s == InvoiceStatus.PENDING && dueDate < System.currentTimeMillis() && amountPaid < effectiveTotal) {
                return InvoiceStatus.OVERDUE
            }
            return s
        }

    val balanceDue: Double
        get() {
            val effectiveTotal = if (totalAmount > 0.0) totalAmount else amount
            return (effectiveTotal - amountPaid).coerceAtLeast(0.0)
        }
}

/**
 * Typealias providing standard 'Invoice' naming for managing invoices in Room.
 */
typealias Invoice = InvoiceEntity

@Entity(
    tableName = "subscriptions",
    indices = [
        Index(value = ["clientId"]),
        Index(value = ["status"]),
        Index(value = ["nextBillingDate"])
    ]
)
data class SubscriptionEntity(
    @PrimaryKey
    val id: String = "SUB-${System.currentTimeMillis() % 100000}",
    val clientId: String,
    val clientName: String,
    val planName: String,
    val amount: Double,
    val currency: String = "EUR",
    val interval: String = BillingInterval.MONTHLY.name,
    val status: String = SubscriptionStatus.ACTIVE.name,
    val nextBillingDate: Long = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000),
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    val subscriptionStatus: SubscriptionStatus
        get() = SubscriptionStatus.fromString(status)

    val billingInterval: BillingInterval
        get() = BillingInterval.fromString(interval)
}

@Entity(
    tableName = "payments",
    indices = [
        Index(value = ["invoiceId"]),
        Index(value = ["paymentDate"])
    ]
)
data class PaymentEntity(
    @PrimaryKey
    val id: String = "PAY-${System.currentTimeMillis() % 100000}",
    val invoiceId: String,
    val clientName: String,
    val amount: Double,
    val currency: String = "EUR",
    val paymentMethod: String = "Bank Transfer (SEPA)", // iDEAL, Credit Card, Bank Transfer, Stripe, PayPal
    val transactionRef: String = "",
    val paymentDate: Long = System.currentTimeMillis(),
    val notes: String = ""
)

@Entity(
    tableName = "payouts",
    indices = [
        Index(value = ["status"]),
        Index(value = ["createdAt"]),
        Index(value = ["destinationIban"])
    ]
)
data class PayoutEntity(
    @PrimaryKey
    val id: String = "POUT-${(System.currentTimeMillis() % 100000).toString().padStart(5, '0')}",
    val amount: Double,
    val fee: Double = 0.0,
    val netAmount: Double = amount - fee,
    val currency: String = "EUR",
    val status: String = "SCHEDULED", // SCHEDULED, PROCESSING, PAID, FAILED, CANCELLED
    val destinationType: String = "SEPA_BANK", // SEPA_BANK, SWIFT_INTERNATIONAL, STRIPE_CONNECT, PAYPAL
    val destinationBank: String = "ING Bank N.V.",
    val destinationIban: String = "NL43 INGB 0006 3721 46",
    val destinationBic: String = "INGBNL2A",
    val recipientName: String = "Oistars International B.V.",
    val reference: String = "PAYOUT-${System.currentTimeMillis() % 1000000}",
    val notes: String = "",
    val scheduledDate: Long = System.currentTimeMillis() + (24L * 60 * 60 * 1000),
    val completedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
