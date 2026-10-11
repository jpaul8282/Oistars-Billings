package com.example

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.HttpMetric
import com.google.firebase.perf.metrics.Trace

/**
 * Main application class for Oistars Billings.
 * Initializes Firebase, Firebase Crashlytics, and Firebase Performance Monitoring
 * to track runtime errors, HTTP network request latency, and application startup duration.
 */
class OistarsApplication : Application() {

    private var appStartupTrace: Trace? = null

    override fun onCreate() {
        super.onCreate()
        initializeFirebase()
    }

    private fun initializeFirebase() {
        try {
            // Ensure FirebaseApp is initialized
            val app = FirebaseApp.initializeApp(this)
            if (app != null) {
                // Initialize Firebase Performance Monitoring
                initializePerformanceMonitoring()

                // Initialize Firebase Crashlytics
                initializeCrashlytics()

                Log.i(TAG, "Firebase core services initialized successfully.")
            } else {
                Log.w(TAG, "FirebaseApp returned null during initialization.")
            }
        } catch (e: Exception) {
            // Avoid failing app startup if Google Services configuration is missing in local dev
            Log.w(TAG, "Firebase initialization deferred or skipped: ${e.message}")
        }
    }

    private fun initializePerformanceMonitoring() {
        try {
            val perf = FirebasePerformance.getInstance()
            // Enable telemetry collection in production builds
            perf.isPerformanceCollectionEnabled = !BuildConfig.DEBUG

            // Start custom application startup latency trace
            appStartupTrace = perf.newTrace("app_startup_latency").apply {
                start()
                putAttribute("build_type", BuildConfig.BUILD_TYPE)
                putAttribute("version_name", BuildConfig.VERSION_NAME)
            }

            Log.i(TAG, "Firebase Performance Monitoring initialized (collection: ${!BuildConfig.DEBUG}).")

            // End startup trace once initialization sequence completes
            appStartupTrace?.stop()
        } catch (e: Exception) {
            Log.w(TAG, "Firebase Performance initialization deferred: ${e.message}")
        }
    }

    private fun initializeCrashlytics() {
        try {
            val crashlytics = FirebaseCrashlytics.getInstance()
            crashlytics.isCrashlyticsCollectionEnabled = !BuildConfig.DEBUG

            crashlytics.setCustomKey("environment", if (BuildConfig.DEBUG) "debug" else "production")
            crashlytics.setCustomKey("version_code", BuildConfig.VERSION_CODE)
            crashlytics.setCustomKey("version_name", BuildConfig.VERSION_NAME)
            crashlytics.setCustomKey("build_type", BuildConfig.BUILD_TYPE)
            crashlytics.log("OistarsApplication initialized: Performance and Crashlytics active.")

            Log.i(TAG, "Firebase Crashlytics active (collection: ${!BuildConfig.DEBUG}).")
        } catch (e: Exception) {
            Log.w(TAG, "Firebase Crashlytics deferred: ${e.message}")
        }
    }

    companion object {
        private const val TAG = "OistarsApplication"

        /**
         * Begins a named performance trace to measure latency of critical workflows.
         */
        fun startTrace(traceName: String): Trace? {
            return try {
                FirebasePerformance.getInstance().newTrace(traceName).apply { start() }
            } catch (e: Exception) {
                Log.w(TAG, "Unable to start trace '$traceName': ${e.message}")
                null
            }
        }

        /**
         * Creates an HttpMetric instance to monitor external REST/network requests manually.
         */
        fun newHttpMetric(url: String, httpMethod: String): HttpMetric? {
            return try {
                FirebasePerformance.getInstance().newHttpMetric(url, httpMethod)
            } catch (e: Exception) {
                Log.w(TAG, "Unable to create HttpMetric for '$url': ${e.message}")
                null
            }
        }

        /**
         * Safely logs a non-fatal exception to Crashlytics if available,
         * while also logging it to Android Logcat.
         */
        fun recordNonFatal(throwable: Throwable, message: String? = null) {
            try {
                if (message != null) {
                    Log.e(TAG, message, throwable)
                    FirebaseCrashlytics.getInstance().log(message)
                } else {
                    Log.e(TAG, "Non-fatal exception encountered", throwable)
                }
                FirebaseCrashlytics.getInstance().recordException(throwable)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to record non-fatal to Crashlytics: ${e.message}")
            }
        }

        /**
         * Sets user identifier in Crashlytics for user-specific issue triage.
         */
        fun setUserId(userId: String) {
            try {
                FirebaseCrashlytics.getInstance().setUserId(userId)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to set Crashlytics user ID: ${e.message}")
            }
        }

        /**
         * Adds custom diagnostic breadcrumb to Crashlytics.
         */
        fun logBreadcrumb(breadcrumb: String) {
            try {
                FirebaseCrashlytics.getInstance().log(breadcrumb)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to log Crashlytics breadcrumb: ${e.message}")
            }
        }
    }
}
