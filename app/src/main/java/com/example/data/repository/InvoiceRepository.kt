package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.InvoiceDao
import com.example.data.local.InvoiceEntity
import com.example.data.model.InvoiceStatus
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Interface abstracting data operations for the Room [InvoiceEntity],
 * guaranteeing a clean separation of concerns between Room SQLite persistence
 * and UI-layer ViewModels.
 */
interface IInvoiceRepository {
    val allInvoices: Flow<List<InvoiceEntity>>
    val totalBilled: Flow<Double?>
    val totalCollected: Flow<Double?>

    fun getInvoicesForCustomer(customerId: String): Flow<List<InvoiceEntity>>
    fun getInvoicesForClient(clientId: String): Flow<List<InvoiceEntity>>
    fun getInvoicesByStatus(status: String): Flow<List<InvoiceEntity>>
    fun getInvoicesByStatus(status: InvoiceStatus): Flow<List<InvoiceEntity>>
    fun getOverdueInvoices(currentTime: Long = System.currentTimeMillis()): Flow<List<InvoiceEntity>>
    fun getUnpaidInvoices(): Flow<List<InvoiceEntity>>
    fun getInvoicesDueBefore(dueDate: Long): Flow<List<InvoiceEntity>>
    fun getInvoicesDueBetween(startDate: Long, endDate: Long): Flow<List<InvoiceEntity>>
    fun getInvoicesByAmountRange(minAmount: Double, maxAmount: Double): Flow<List<InvoiceEntity>>

    suspend fun getInvoiceById(id: String): InvoiceEntity?
    suspend fun insertInvoice(invoice: InvoiceEntity)
    suspend fun insertInvoices(invoices: List<InvoiceEntity>)
    suspend fun updateInvoice(invoice: InvoiceEntity)
    suspend fun deleteInvoice(invoice: InvoiceEntity)
    suspend fun deleteInvoiceById(id: String)
    suspend fun markInvoiceAsPaid(invoice: InvoiceEntity)
    suspend fun updateInvoicePaymentStatus(invoiceId: String, status: String, paidAt: Long?, amountPaid: Double)
}

/**
 * Concrete implementation of [IInvoiceRepository] that interacts directly with Room's [InvoiceDao].
 * Ensures non-blocking execution by dispatching write and lookup queries to an IO coroutine dispatcher.
 */
class InvoiceRepository(
    private val invoiceDao: InvoiceDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : IInvoiceRepository {

    /**
     * Convenience secondary constructor allowing instantiation from an [AppDatabase] instance.
     */
    constructor(database: AppDatabase, ioDispatcher: CoroutineDispatcher = Dispatchers.IO) : this(
        invoiceDao = database.invoiceDao(),
        ioDispatcher = ioDispatcher
    )

    override val allInvoices: Flow<List<InvoiceEntity>> = invoiceDao.getAllInvoices()

    override val totalBilled: Flow<Double?> = invoiceDao.getTotalBilled()

    override val totalCollected: Flow<Double?> = invoiceDao.getTotalCollected()

    override fun getInvoicesForCustomer(customerId: String): Flow<List<InvoiceEntity>> =
        invoiceDao.getInvoicesForCustomer(customerId)

    override fun getInvoicesForClient(clientId: String): Flow<List<InvoiceEntity>> =
        invoiceDao.getInvoicesForClient(clientId)

    override fun getInvoicesByStatus(status: String): Flow<List<InvoiceEntity>> =
        invoiceDao.getInvoicesByStatus(status)

    override fun getInvoicesByStatus(status: InvoiceStatus): Flow<List<InvoiceEntity>> =
        invoiceDao.getInvoicesByStatus(status.name)

    override fun getOverdueInvoices(currentTime: Long): Flow<List<InvoiceEntity>> =
        invoiceDao.getOverdueInvoices(currentTime)

    override fun getUnpaidInvoices(): Flow<List<InvoiceEntity>> =
        invoiceDao.getUnpaidInvoices()

    override fun getInvoicesDueBefore(dueDate: Long): Flow<List<InvoiceEntity>> =
        invoiceDao.getInvoicesDueBefore(dueDate)

    override fun getInvoicesDueBetween(startDate: Long, endDate: Long): Flow<List<InvoiceEntity>> =
        invoiceDao.getInvoicesDueBetween(startDate, endDate)

    override fun getInvoicesByAmountRange(minAmount: Double, maxAmount: Double): Flow<List<InvoiceEntity>> =
        invoiceDao.getInvoicesByAmountRange(minAmount, maxAmount)

    override suspend fun getInvoiceById(id: String): InvoiceEntity? = withContext(ioDispatcher) {
        invoiceDao.getInvoiceById(id)
    }

    override suspend fun insertInvoice(invoice: InvoiceEntity) = withContext(ioDispatcher) {
        invoiceDao.insertInvoice(invoice)
    }

    override suspend fun insertInvoices(invoices: List<InvoiceEntity>) = withContext(ioDispatcher) {
        invoiceDao.insertInvoices(invoices)
    }

    override suspend fun updateInvoice(invoice: InvoiceEntity) = withContext(ioDispatcher) {
        invoiceDao.updateInvoice(invoice)
    }

    override suspend fun deleteInvoice(invoice: InvoiceEntity) = withContext(ioDispatcher) {
        invoiceDao.deleteInvoice(invoice)
    }

    override suspend fun deleteInvoiceById(id: String) = withContext(ioDispatcher) {
        invoiceDao.deleteInvoiceById(id)
    }

    override suspend fun markInvoiceAsPaid(invoice: InvoiceEntity) = withContext(ioDispatcher) {
        val now = System.currentTimeMillis()
        val effectiveTotal = if (invoice.totalAmount > 0.0) invoice.totalAmount else invoice.amount
        invoiceDao.updateInvoicePaymentStatus(
            invoiceId = invoice.id,
            status = InvoiceStatus.PAID.name,
            paidAt = now,
            amountPaid = effectiveTotal
        )
    }

    override suspend fun updateInvoicePaymentStatus(
        invoiceId: String,
        status: String,
        paidAt: Long?,
        amountPaid: Double
    ) = withContext(ioDispatcher) {
        invoiceDao.updateInvoicePaymentStatus(invoiceId, status, paidAt, amountPaid)
    }
}
