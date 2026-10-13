package com.example

import com.example.data.local.ClientEntity
import com.example.data.local.InvoiceEntity
import com.example.data.local.SubscriptionEntity
import com.example.data.model.BillingInterval
import com.example.data.model.InvoiceStatus
import com.example.data.model.SubscriptionStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ClientProfileTest {

    @Test
    fun `test billing history aggregates total billed paid and outstanding correctly`() {
        val client = ClientEntity(
            id = "CLI-9001",
            name = "North Sea Logistics B.V.",
            contactPerson = "Lars van Dijk",
            email = "finance@northsealogistics.nl"
        )

        val invoices = listOf(
            InvoiceEntity(
                id = "INV-001",
                clientId = "CLI-9001",
                customerId = "CLI-9001",
                clientName = client.name,
                clientEmail = client.email,
                status = InvoiceStatus.PAID.name,
                totalAmount = 2500.0,
                amount = 2500.0,
                amountPaid = 2500.0
            ),
            InvoiceEntity(
                id = "INV-002",
                clientId = "CLI-9001",
                customerId = "CLI-9001",
                clientName = client.name,
                clientEmail = client.email,
                status = InvoiceStatus.PENDING.name,
                totalAmount = 1500.0,
                amount = 1500.0,
                amountPaid = 500.0 // partial paid: balance 1000.0
            ),
            InvoiceEntity(
                id = "INV-003",
                clientId = "CLI-9001",
                customerId = "CLI-9001",
                clientName = client.name,
                clientEmail = client.email,
                status = InvoiceStatus.OVERDUE.name,
                totalAmount = 800.0,
                amount = 800.0,
                amountPaid = 0.0
            ),
            InvoiceEntity(
                id = "INV-999",
                clientId = "OTHER-CLI",
                customerId = "OTHER-CLI",
                status = InvoiceStatus.PAID.name,
                totalAmount = 9999.0
            )
        )

        val clientInvoices = invoices.filter { it.clientId == client.id }
        assertEquals(3, clientInvoices.size)

        val totalBilled = clientInvoices.sumOf { it.totalAmount }
        assertEquals(4800.0, totalBilled, 0.01)

        val totalPaid = clientInvoices.filter { it.invoiceStatus == InvoiceStatus.PAID }.sumOf { it.totalAmount }
        assertEquals(2500.0, totalPaid, 0.01)

        val totalOutstanding = clientInvoices.sumOf { it.balanceDue }
        assertEquals(1800.0, totalOutstanding, 0.01) // 1000 + 800

        val paidCount = clientInvoices.count { it.invoiceStatus == InvoiceStatus.PAID }
        assertEquals(1, paidCount)

        val pendingCount = clientInvoices.count { it.invoiceStatus == InvoiceStatus.PENDING }
        assertEquals(1, pendingCount)

        val overdueCount = clientInvoices.count { it.invoiceStatus == InvoiceStatus.OVERDUE }
        assertEquals(1, overdueCount)
    }

    @Test
    fun `test client subscription status resolution`() {
        val client = ClientEntity(
            id = "CLI-7001",
            name = "Amsterdam Tech Studio"
        )

        val subscriptions = listOf(
            SubscriptionEntity(
                id = "SUB-101",
                clientId = "CLI-7001",
                clientName = client.name,
                planName = "Enterprise SLA Retainer",
                amount = 3500.0,
                interval = BillingInterval.MONTHLY.name,
                status = SubscriptionStatus.ACTIVE.name
            ),
            SubscriptionEntity(
                id = "SUB-202",
                clientId = "OTHER-CLI",
                clientName = "Other Client",
                planName = "Basic Care",
                amount = 500.0
            )
        )

        val clientSubs = subscriptions.filter { it.clientId == client.id }
        assertEquals(1, clientSubs.size)

        val activeSub = clientSubs.firstOrNull { it.subscriptionStatus == SubscriptionStatus.ACTIVE }
        assertNotNull(activeSub)
        assertEquals("Enterprise SLA Retainer", activeSub?.planName)
        assertEquals(3500.0, activeSub?.amount ?: 0.0, 0.01)
        assertEquals(BillingInterval.MONTHLY, activeSub?.billingInterval)
    }
}
