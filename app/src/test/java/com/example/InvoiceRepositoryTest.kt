package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.InvoiceEntity
import com.example.data.model.InvoiceStatus
import com.example.data.repository.InvoiceRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class InvoiceRepositoryTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: InvoiceRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = InvoiceRepository(database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `insert and retrieve invoice by id`() = runBlocking {
        val invoice = InvoiceEntity(
            id = "INV-TEST-001",
            customerId = "CUST-999",
            clientName = "Acme Global B.V.",
            amount = 1450.0,
            status = InvoiceStatus.PENDING.name,
            dueDate = System.currentTimeMillis() + 86400000L
        )

        repository.insertInvoice(invoice)

        val retrieved = repository.getInvoiceById("INV-TEST-001")
        assertNotNull(retrieved)
        assertEquals("INV-TEST-001", retrieved?.id)
        assertEquals("CUST-999", retrieved?.customerId)
        assertEquals(1450.0, retrieved?.amount ?: 0.0, 0.01)
        assertEquals(InvoiceStatus.PENDING.name, retrieved?.status)
    }

    @Test
    fun `query invoices by customer id`() = runBlocking {
        val inv1 = InvoiceEntity(
            id = "INV-CUST-1",
            customerId = "CUST-ALPHA",
            amount = 500.0,
            status = InvoiceStatus.PENDING.name
        )
        val inv2 = InvoiceEntity(
            id = "INV-CUST-2",
            customerId = "CUST-ALPHA",
            amount = 750.0,
            status = InvoiceStatus.PAID.name
        )
        val inv3 = InvoiceEntity(
            id = "INV-CUST-3",
            customerId = "CUST-BETA",
            amount = 1200.0,
            status = InvoiceStatus.PENDING.name
        )

        repository.insertInvoices(listOf(inv1, inv2, inv3))

        val alphaInvoices = repository.getInvoicesForCustomer("CUST-ALPHA").first()
        assertEquals(2, alphaInvoices.size)

        val betaInvoices = repository.getInvoicesForCustomer("CUST-BETA").first()
        assertEquals(1, betaInvoices.size)
        assertEquals("INV-CUST-3", betaInvoices.first().id)
    }

    @Test
    fun `mark invoice as paid updates status and payment amount`() = runBlocking {
        val invoice = InvoiceEntity(
            id = "INV-PAY-01",
            customerId = "CUST-SETTLE",
            amount = 2300.0,
            status = InvoiceStatus.PENDING.name
        )

        repository.insertInvoice(invoice)
        repository.markInvoiceAsPaid(invoice)

        val updated = repository.getInvoiceById("INV-PAY-01")
        assertNotNull(updated)
        assertEquals(InvoiceStatus.PAID.name, updated?.status)
        assertEquals(2300.0, updated?.amountPaid ?: 0.0, 0.01)
        assertNotNull(updated?.paidAt)
    }

    @Test
    fun `delete invoice removes record from repository`() = runBlocking {
        val invoice = InvoiceEntity(
            id = "INV-DEL-01",
            customerId = "CUST-TEMP",
            amount = 100.0,
            status = InvoiceStatus.DRAFT.name
        )

        repository.insertInvoice(invoice)
        assertNotNull(repository.getInvoiceById("INV-DEL-01"))

        repository.deleteInvoice(invoice)
        assertNull(repository.getInvoiceById("INV-DEL-01"))
    }
}
