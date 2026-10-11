package com.example.data.repository

import android.util.Log
import com.example.data.model.ClientLedgerEntry
import com.example.data.model.ClientLedgerSummary
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Basic Data Repository to handle storing and retrieving client ledger data in Cloud Firestore.
 */
class ClientLedgerFirestoreRepository(
    private val firestoreProvider: () -> FirebaseFirestore? = {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.w("ClientLedgerRepo", "FirebaseFirestore not initialized or unavailable: ${e.message}")
            null
        }
    }
) {
    private val firestore: FirebaseFirestore?
        get() = firestoreProvider()

    val isAvailable: Boolean
        get() = firestore != null

    /**
     * Store or update a client ledger entry in the "client_ledgers" collection.
     */
    suspend fun saveLedgerEntry(entry: ClientLedgerEntry): Result<String> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore is not initialized"))
        return try {
            val collection = db.collection("client_ledgers")
            val docRef = if (entry.id.isNotBlank()) {
                collection.document(entry.id)
            } else {
                collection.document()
            }
            val recordToSave = if (entry.id.isBlank()) entry.copy(id = docRef.id) else entry
            docRef.set(recordToSave).awaitTask()
            Result.success(recordToSave.id)
        } catch (e: Exception) {
            Log.e("ClientLedgerRepo", "Error saving ledger entry", e)
            Result.failure(e)
        }
    }

    /**
     * Retrieve real-time ledger entries for a specific client ordered by timestamp descending.
     */
    fun getClientLedgerEntries(clientId: String): Flow<List<ClientLedgerEntry>> {
        val db = firestore ?: return flowOf(emptyList())
        return callbackFlow {
            val registration = db.collection("client_ledgers")
                .whereEqualTo("clientId", clientId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("ClientLedgerRepo", "Listen failed for client $clientId", error)
                        close(error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val entries = snapshot.toObjects(ClientLedgerEntry::class.java)
                        trySend(entries)
                    }
                }
            awaitClose { registration.remove() }
        }
    }

    /**
     * Retrieve real-time ledger entries across all clients ordered by timestamp descending.
     */
    fun getAllLedgerEntries(): Flow<List<ClientLedgerEntry>> {
        val db = firestore ?: return flowOf(emptyList())
        return callbackFlow {
            val registration = db.collection("client_ledgers")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("ClientLedgerRepo", "Listen failed for all ledger entries", error)
                        close(error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val entries = snapshot.toObjects(ClientLedgerEntry::class.java)
                        trySend(entries)
                    }
                }
            awaitClose { registration.remove() }
        }
    }

    /**
     * Store or update aggregate client ledger summary in "client_ledger_summaries" collection.
     */
    suspend fun saveClientSummary(summary: ClientLedgerSummary): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore is not initialized"))
        return try {
            db.collection("client_ledger_summaries")
                .document(summary.clientId)
                .set(summary)
                .awaitTask()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("ClientLedgerRepo", "Error saving client summary", e)
            Result.failure(e)
        }
    }

    /**
     * Real-time stream of a client's aggregate ledger balance and summary.
     */
    fun getClientSummary(clientId: String): Flow<ClientLedgerSummary?> {
        val db = firestore ?: return flowOf(null)
        return callbackFlow {
            val registration = db.collection("client_ledger_summaries")
                .document(clientId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("ClientLedgerRepo", "Listen failed for summary $clientId", error)
                        close(error)
                        return@addSnapshotListener
                    }
                    val summary = snapshot?.toObject(ClientLedgerSummary::class.java)
                    trySend(summary)
                }
            awaitClose { registration.remove() }
        }
    }

    /**
     * One-time fetch of ledger entries for a client.
     */
    suspend fun fetchClientLedgerOnce(clientId: String): Result<List<ClientLedgerEntry>> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore is not initialized"))
        return try {
            val snapshot = db.collection("client_ledgers")
                .whereEqualTo("clientId", clientId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .awaitTask()
            val entries = snapshot.toObjects(ClientLedgerEntry::class.java)
            Result.success(entries)
        } catch (e: Exception) {
            Log.e("ClientLedgerRepo", "Error fetching client ledger", e)
            Result.failure(e)
        }
    }

    /**
     * Delete a ledger entry by ID.
     */
    suspend fun deleteLedgerEntry(entryId: String): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore is not initialized"))
        return try {
            db.collection("client_ledgers")
                .document(entryId)
                .delete()
                .awaitTask()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("ClientLedgerRepo", "Error deleting ledger entry", e)
            Result.failure(e)
        }
    }
}

/**
 * Suspend extension converting Google Play Tasks to cancellable Coroutines.
 */
internal suspend fun <T> Task<T>.awaitTask(): T = suspendCancellableCoroutine { continuation ->
    addOnSuccessListener { result ->
        if (continuation.isActive) continuation.resume(result)
    }
    addOnFailureListener { exception ->
        if (continuation.isActive) continuation.resumeWithException(exception)
    }
    addOnCanceledListener {
        if (continuation.isActive) continuation.cancel()
    }
}
