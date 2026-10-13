package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.PayoutEntity
import com.example.data.repository.BillingRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PayoutTransactionErrorTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: BillingRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = BillingRepository(database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `detect failed transaction and verify clearing error details`() = runBlocking {
        val failedPayout = PayoutEntity(
            id = "POUT-TEST-ERR",
            amount = 350.0,
            fee = 0.0,
            netAmount = 350.0,
            currency = "EUR",
            status = "FAILED",
            destinationBank = "ING Bank N.V.",
            destinationIban = "NL43 INGB 0006 3721 46",
            destinationBic = "INGBNL2A",
            recipientName = "Oistars International B.V.",
            reference = "PO-ERR-001",
            notes = "Transaction rejected by clearing gateway: BIC mismatch with intermediary institution."
        )

        repository.createPayout(failedPayout)

        val retrieved = repository.getPayoutById("POUT-TEST-ERR")
        assertNotNull(retrieved)
        assertEquals("FAILED", retrieved?.status)
        assertTrue(retrieved?.notes?.contains("BIC mismatch") == true)
    }

    @Test
    fun `fix routing details and retry failed payout transaction`() = runBlocking {
        val failedPayout = PayoutEntity(
            id = "POUT-TEST-RETRY",
            amount = 500.0,
            fee = 0.0,
            netAmount = 500.0,
            currency = "EUR",
            status = "FAILED",
            destinationBank = "Old Bank",
            destinationIban = "NL43 INGB 0006 3721 46",
            destinationBic = "OLDBIC2A",
            recipientName = "Beneficiary Entity",
            reference = "PO-ERR-002",
            notes = "Gateway failure"
        )
        repository.createPayout(failedPayout)

        // Correct routing and re-queue
        val corrected = failedPayout.copy(
            destinationBank = "ING Bank N.V.",
            destinationBic = "INGBNL2A",
            status = "SCHEDULED",
            notes = "Corrected routing details and re-queued for settlement"
        )
        repository.updatePayout(corrected)

        val updated = repository.getPayoutById("POUT-TEST-RETRY")
        assertNotNull(updated)
        assertEquals("SCHEDULED", updated?.status)
        assertEquals("INGBNL2A", updated?.destinationBic)
        assertEquals("ING Bank N.V.", updated?.destinationBank)
    }

    @Test
    fun `batch recover all failed payouts`() = runBlocking {
        val p1 = PayoutEntity(
            id = "POUT-F1",
            amount = 100.0,
            status = "FAILED",
            notes = "Timeout error"
        )
        val p2 = PayoutEntity(
            id = "POUT-F2",
            amount = 200.0,
            status = "FAILED",
            notes = "Routing unreachable"
        )
        repository.createPayout(p1)
        repository.createPayout(p2)

        val payoutsBefore = repository.allPayouts.first()
        val failedBefore = payoutsBefore.filter { it.status == "FAILED" }
        assertEquals(2, failedBefore.size)

        // Batch retry
        failedBefore.forEach {
            repository.updatePayoutStatus(it.id, "SCHEDULED", null)
        }

        val payoutsAfter = repository.allPayouts.first()
        val failedAfter = payoutsAfter.filter { it.status == "FAILED" }
        val scheduledAfter = payoutsAfter.filter { it.status == "SCHEDULED" }
        assertEquals(0, failedAfter.size)
        assertEquals(2, scheduledAfter.size)
    }

    @Test
    fun `fix payout error and mark as paid settlement`() = runBlocking {
        val failed = PayoutEntity(
            id = "POUT-TEST-FIX",
            amount = 350.0,
            status = "FAILED",
            notes = "BIC mismatch"
        )
        repository.createPayout(failed)

        val resolved = failed.copy(
            destinationBic = "INGBNL2A",
            status = "PAID",
            notes = "Error resolved: Intermediary BIC verified with clearing gateway. Settled successfully.",
            completedAt = System.currentTimeMillis()
        )
        repository.updatePayout(resolved)

        val retrieved = repository.getPayoutById("POUT-TEST-FIX")
        assertNotNull(retrieved)
        assertEquals("PAID", retrieved?.status)
        assertEquals("INGBNL2A", retrieved?.destinationBic)
        assertNotNull(retrieved?.completedAt)
    }
}
