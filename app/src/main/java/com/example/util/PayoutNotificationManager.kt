package com.example.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.data.local.PayoutEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Manages local notifications for merchant payout lifecycle events.
 * Alerts users when a payout is processed or when its status transitions to 'completed'.
 */
object PayoutNotificationManager {
    private const val TAG = "PayoutNotificationMgr"
    const val CHANNEL_ID = "merchant_payout_alerts"
    const val CHANNEL_NAME = "Merchant Payout Alerts"
    const val CHANNEL_DESCRIPTION = "Alerts for merchant payout disbursements, processing status, and completed settlements."

    const val NOTIFICATION_ID_BASE_PROCESSED = 10000
    const val NOTIFICATION_ID_BASE_COMPLETED = 20000

    /**
     * Initializes the notification channel for Android 8.0+ (Oreo).
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.createNotificationChannel(channel)
            Log.d(TAG, "Merchant Payout notification channel registered.")
        }
    }

    /**
     * Checks if notification permissions are granted on the current device.
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }

    /**
     * Dispatches a high-priority local notification when a merchant payout begins processing.
     */
    fun notifyPayoutProcessed(context: Context, payout: PayoutEntity): Boolean {
        createNotificationChannel(context)
        val formattedAmount = String.format(Locale.US, "%.2f", payout.amount)
        val title = "Merchant Payout Processing • ${payout.id}"
        val shortContent = "Payout ${payout.id} (€$formattedAmount) is now processing to ${payout.destinationBank}."
        val bigText = buildString {
            append("Merchant disbursement of €$formattedAmount is now processing.\n")
            append("• Payout ID: ${payout.id}\n")
            append("• Beneficiary: ${payout.recipientName}\n")
            append("• Destination Bank: ${payout.destinationBank}\n")
            append("• Account: ${payout.destinationIban}\n")
            append("• Status: PROCESSING (SEPA Gateway)")
        }

        val notificationId = NOTIFICATION_ID_BASE_PROCESSED + (payout.id.hashCode() and 0x7FFF)
        return postNotification(
            context = context,
            notificationId = notificationId,
            title = title,
            content = shortContent,
            bigText = bigText,
            subText = "Processing",
            payoutId = payout.id
        )
    }

    /**
     * Dispatches a local notification when a merchant payout is completed / paid.
     */
    fun notifyPayoutCompleted(context: Context, payout: PayoutEntity): Boolean {
        createNotificationChannel(context)
        val formattedAmount = String.format(Locale.US, "%.2f", payout.amount)
        val title = "Merchant Payout Completed • ${payout.id}"
        val shortContent = "Payout ${payout.id} (€$formattedAmount) completed and settled to ${payout.destinationBank}."
        val timestamp = SimpleDateFormat("HH:mm, dd MMM yyyy", Locale.US).format(Date(payout.completedAt ?: System.currentTimeMillis()))
        val bigText = buildString {
            append("Merchant disbursement of €$formattedAmount has been successfully settled.\n")
            append("• Payout ID: ${payout.id}\n")
            append("• Beneficiary: ${payout.recipientName}\n")
            append("• Destination Bank: ${payout.destinationBank}\n")
            append("• Account: ${payout.destinationIban}\n")
            append("• Settled At: $timestamp\n")
            append("• Status: COMPLETED / PAID")
        }

        val notificationId = NOTIFICATION_ID_BASE_COMPLETED + (payout.id.hashCode() and 0x7FFF)
        return postNotification(
            context = context,
            notificationId = notificationId,
            title = title,
            content = shortContent,
            bigText = bigText,
            subText = "Completed",
            payoutId = payout.id
        )
    }

    /**
     * Sends a test notification to verify the notification system pipeline.
     */
    fun sendTestNotification(context: Context, isCompleted: Boolean): Boolean {
        val dummyPayout = PayoutEntity(
            id = "POUT-TEST-${System.currentTimeMillis() % 10000}",
            amount = 1450.00,
            fee = 0.0,
            netAmount = 1450.00,
            currency = "EUR",
            status = if (isCompleted) "PAID" else "PROCESSING",
            destinationBank = "ING Bank N.V.",
            destinationIban = "NL43 INGB 0006 3721 46",
            destinationBic = "INGBNL2A",
            recipientName = "Oistars International B.V.",
            reference = "PO-TEST-${System.currentTimeMillis() % 1000}",
            notes = "Test notification for merchant settlement pipeline",
            completedAt = if (isCompleted) System.currentTimeMillis() else null
        )
        return if (isCompleted) {
            notifyPayoutCompleted(context, dummyPayout)
        } else {
            notifyPayoutProcessed(context, dummyPayout)
        }
    }

    /**
     * Builds and displays the local notification.
     */
    fun postNotification(
        context: Context,
        notificationId: Int,
        title: String,
        content: String,
        bigText: String,
        subText: String,
        payoutId: String? = null
    ): Boolean {
        if (!hasNotificationPermission(context)) {
            Log.w(TAG, "Cannot post notification: POST_NOTIFICATIONS permission not granted.")
            return false
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("NAVIGATE_TO", "PAYOUTS")
            if (payoutId != null) {
                putExtra("SELECTED_PAYOUT_ID", payoutId)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(content)
            .setSubText(subText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        return try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
            Log.i(TAG, "Notification posted successfully: $title (ID: $notificationId)")
            true
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException while posting notification: ${e.message}")
            false
        } catch (e: Exception) {
            Log.e(TAG, "Failed to post notification: ${e.message}")
            false
        }
    }
}
