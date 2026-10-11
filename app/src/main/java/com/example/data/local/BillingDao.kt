package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ClientDao {
    @Query("SELECT * FROM clients ORDER BY name ASC")
    fun getAllClients(): Flow<List<ClientEntity>>

    @Query("SELECT * FROM clients WHERE id = :clientId LIMIT 1")
    suspend fun getClientById(clientId: String): ClientEntity?

    @Query("SELECT * FROM clients WHERE name LIKE '%' || :query || '%' OR email LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchClients(query: String): Flow<List<ClientEntity>>

    @Query("SELECT COUNT(*) FROM clients")
    fun getClientCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClient(client: ClientEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClients(clients: List<ClientEntity>)

    @Update
    suspend fun updateClient(client: ClientEntity)

    @Delete
    suspend fun deleteClient(client: ClientEntity)

    @Query("DELETE FROM clients WHERE id = :clientId")
    suspend fun deleteClientById(clientId: String)
}

@Dao
interface InvoiceDao {
    @Query("SELECT * FROM invoices ORDER BY issueDate DESC, createdAt DESC")
    fun getAllInvoices(): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices WHERE customerId = :customerId OR clientId = :customerId ORDER BY issueDate DESC")
    fun getInvoicesForCustomer(customerId: String): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices WHERE clientId = :clientId OR customerId = :clientId ORDER BY issueDate DESC")
    fun getInvoicesForClient(clientId: String): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices WHERE id = :invoiceId LIMIT 1")
    suspend fun getInvoiceById(invoiceId: String): InvoiceEntity?

    @Query("SELECT * FROM invoices WHERE status = :status ORDER BY issueDate DESC")
    fun getInvoicesByStatus(status: String): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices WHERE dueDate <= :dueDate ORDER BY dueDate ASC")
    fun getInvoicesDueBefore(dueDate: Long): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices WHERE dueDate BETWEEN :startDate AND :endDate ORDER BY dueDate ASC")
    fun getInvoicesDueBetween(startDate: Long, endDate: Long): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices WHERE (status = 'OVERDUE' OR (status = 'PENDING' AND dueDate < :currentTime AND amountPaid < totalAmount)) ORDER BY dueDate ASC")
    fun getOverdueInvoices(currentTime: Long = System.currentTimeMillis()): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices WHERE status != 'PAID' ORDER BY dueDate ASC")
    fun getUnpaidInvoices(): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices WHERE (amount BETWEEN :minAmount AND :maxAmount) OR (totalAmount BETWEEN :minAmount AND :maxAmount) ORDER BY amount DESC")
    fun getInvoicesByAmountRange(minAmount: Double, maxAmount: Double): Flow<List<InvoiceEntity>>

    @Query("SELECT SUM(CASE WHEN totalAmount > 0 THEN totalAmount ELSE amount END) FROM invoices")
    fun getTotalBilled(): Flow<Double?>

    @Query("SELECT SUM(amountPaid) FROM invoices")
    fun getTotalCollected(): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: InvoiceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoices(invoices: List<InvoiceEntity>)

    @Update
    suspend fun updateInvoice(invoice: InvoiceEntity)

    @Delete
    suspend fun deleteInvoice(invoice: InvoiceEntity)

    @Query("DELETE FROM invoices WHERE id = :invoiceId")
    suspend fun deleteInvoiceById(invoiceId: String)

    @Query("UPDATE invoices SET status = :status, paidAt = :paidAt, amountPaid = :amountPaid WHERE id = :invoiceId")
    suspend fun updateInvoicePaymentStatus(invoiceId: String, status: String, paidAt: Long?, amountPaid: Double)
}

@Dao
interface SubscriptionDao {
    @Query("SELECT * FROM subscriptions ORDER BY nextBillingDate ASC")
    fun getAllSubscriptions(): Flow<List<SubscriptionEntity>>

    @Query("SELECT * FROM subscriptions WHERE clientId = :clientId")
    fun getSubscriptionsForClient(clientId: String): Flow<List<SubscriptionEntity>>

    @Query("SELECT * FROM subscriptions WHERE id = :subscriptionId LIMIT 1")
    suspend fun getSubscriptionById(subscriptionId: String): SubscriptionEntity?

    @Query("SELECT * FROM subscriptions WHERE status = :status ORDER BY nextBillingDate ASC")
    fun getSubscriptionsByStatus(status: String): Flow<List<SubscriptionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubscription(subscription: SubscriptionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubscriptions(subscriptions: List<SubscriptionEntity>)

    @Update
    suspend fun updateSubscription(subscription: SubscriptionEntity)

    @Delete
    suspend fun deleteSubscription(subscription: SubscriptionEntity)

    @Query("DELETE FROM subscriptions WHERE id = :subscriptionId")
    suspend fun deleteSubscriptionById(subscriptionId: String)

    @Query("UPDATE subscriptions SET status = :status WHERE id = :subscriptionId")
    suspend fun updateSubscriptionStatus(subscriptionId: String, status: String)
}

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payments ORDER BY paymentDate DESC")
    fun getAllPayments(): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE invoiceId = :invoiceId ORDER BY paymentDate DESC")
    fun getPaymentsForInvoice(invoiceId: String): Flow<List<PaymentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayments(payments: List<PaymentEntity>)

    @Delete
    suspend fun deletePayment(payment: PaymentEntity)
}

@Dao
interface PayoutDao {
    @Query("SELECT * FROM payouts ORDER BY createdAt DESC")
    fun getAllPayouts(): Flow<List<PayoutEntity>>

    @Query("SELECT * FROM payouts WHERE id = :payoutId LIMIT 1")
    suspend fun getPayoutById(payoutId: String): PayoutEntity?

    @Query("SELECT * FROM payouts WHERE status = :status ORDER BY createdAt DESC")
    fun getPayoutsByStatus(status: String): Flow<List<PayoutEntity>>

    @Query("SELECT SUM(amount) FROM payouts WHERE status = 'PAID'")
    fun getTotalPaidOut(): Flow<Double?>

    @Query("SELECT SUM(amount) FROM payouts WHERE status IN ('SCHEDULED', 'PROCESSING')")
    fun getTotalPendingPayout(): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayout(payout: PayoutEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayouts(payouts: List<PayoutEntity>)

    @Update
    suspend fun updatePayout(payout: PayoutEntity)

    @Delete
    suspend fun deletePayout(payout: PayoutEntity)

    @Query("DELETE FROM payouts WHERE id = :payoutId")
    suspend fun deletePayoutById(payoutId: String)

    @Query("UPDATE payouts SET status = :status, completedAt = :completedAt WHERE id = :payoutId")
    suspend fun updatePayoutStatus(payoutId: String, status: String, completedAt: Long?)
}
