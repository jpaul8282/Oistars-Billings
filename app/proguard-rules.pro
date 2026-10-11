# ==============================================================================
# Oistars Billings - Production ProGuard & R8 Optimization Rules
# ==============================================================================

# ------------------------------------------------------------------------------
# 1. Stack Trace, Reflection & Annotation Preservation
# ------------------------------------------------------------------------------
-keepattributes SourceFile,LineNumberTable
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod,Exceptions
-renamesourcefileattribute SourceFile

# ------------------------------------------------------------------------------
# 2. Firebase Dependencies (Firestore, Realtime DB, Auth, Storage, Messaging)
# ------------------------------------------------------------------------------
# Firebase Core & Component Registrars
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**
-keep class com.google.firebase.components.** { *; }
-keep class com.google.firebase.provider.** { *; }
-keep class * implements com.google.firebase.components.ComponentRegistrar

# Keep Google Play Tasks used by Firebase asynchronous calls
-keep class com.google.android.gms.tasks.** { *; }
-dontwarn com.google.android.gms.tasks.**

# Firebase Firestore Core SDK & Serialization Types
-keep class com.google.firebase.firestore.** { *; }
-dontwarn com.google.firebase.firestore.**
-keep class com.google.firebase.Timestamp { *; }
-keep class com.google.firebase.firestore.Blob { *; }
-keep class com.google.firebase.firestore.GeoPoint { *; }
-keep class com.google.firebase.firestore.DocumentReference { *; }
-keep class com.google.firebase.firestore.FieldValue { *; }
-keep class com.google.firebase.firestore.FieldPath { *; }
-keep class com.google.firebase.firestore.ListenerRegistration { *; }
-keep interface com.google.firebase.firestore.EventListener { *; }

# Firebase Firestore & Realtime Database Model Serialization
# Preserves POJO / Kotlin data classes and their property annotations
-keepclassmembers class * {
    @com.google.firebase.firestore.PropertyName <fields>;
    @com.google.firebase.firestore.PropertyName <methods>;
    @com.google.firebase.firestore.Exclude <fields>;
    @com.google.firebase.firestore.Exclude <methods>;
    @com.google.firebase.firestore.ServerTimestamp <fields>;
    @com.google.firebase.firestore.ServerTimestamp <methods>;
    @com.google.firebase.firestore.DocumentId <fields>;
    @com.google.firebase.firestore.DocumentId <methods>;
    @com.google.firebase.firestore.IgnoreExtraProperties <fields>;
    @com.google.firebase.firestore.IgnoreExtraProperties <methods>;
}
-keepclassmembers class * {
    @com.google.firebase.database.PropertyName <fields>;
    @com.google.firebase.database.PropertyName <methods>;
    @com.google.firebase.database.Exclude <fields>;
    @com.google.firebase.database.Exclude <methods>;
    @com.google.firebase.database.IgnoreExtraProperties <fields>;
}

# Preserve Firestore data models getters, setters, and default public constructors
-keep class com.example.data.model.** { *; }
-keepclassmembers class com.example.data.model.** {
    public <init>(...);
    public <init>();
    *** get*();
    *** set*(...);
    *** is*();
    <fields>;
}
-keepclassmembers class * {
    public <init>();
}

# Firebase Auth & Internal Security API
-keep class com.google.firebase.auth.** { *; }
-keep class com.google.android.gms.internal.firebase-auth-api.** { *; }
-dontwarn com.google.firebase.auth.**

# Firebase Messaging (FCM) & Remote Notifications
-keep class com.google.firebase.messaging.** { *; }
-dontwarn com.google.firebase.messaging.**

# Firebase Storage & App Check
-keep class com.google.firebase.storage.** { *; }
-keep class com.google.firebase.appcheck.** { *; }

# Firebase Crashlytics (Exception reporting & Stack Trace preservation)
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception
-keep class com.google.firebase.crashlytics.** { *; }
-dontwarn com.google.firebase.crashlytics.**
-keepclassmembers class com.google.firebase.crashlytics.** { *; }

# Firebase Performance Monitoring (Network request timing & latency traces)
-keep class com.google.firebase.perf.** { *; }
-dontwarn com.google.firebase.perf.**
-keepclassmembers class com.google.firebase.perf.** { *; }
-keepclassmembers class * {
    @com.google.firebase.perf.metrics.AddTrace <methods>;
}

# ------------------------------------------------------------------------------
# 3. Retrofit, OkHttp & Okio (REST Networking & Serialization)
# ------------------------------------------------------------------------------
# Retrofit 2 rules
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-keepclassmembers,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-keep class * extends retrofit2.Converter$Factory { *; }
-keep class * extends retrofit2.CallAdapter$Factory { *; }

# Preserve type parameters for Retrofit parameterized responses (e.g. Call<T>, Response<List<T>>)
-keepattributes Signature
-keepattributes Exceptions

# OkHttp 3 rules
-dontwarn okhttp3.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.internal.platform.**
-keepclassmembers class okhttp3.internal.publicsuffix.PublicSuffixDatabase {
    public java.lang.String findMatchingRule(java.lang.String[]);
}

# Okio rules
-dontwarn okio.**
-keep class okio.** { *; }
-keep interface okio.** { *; }

# JSON Deserializers (Gson, Moshi, Kotlinx Serialization)
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
    @com.google.gson.annotations.Expose <fields>;
}
-keep class com.google.gson.** { *; }
-dontwarn com.google.gson.**

# Moshi (JSON Serialization & Deserialization)
-keep class com.squareup.moshi.** { *; }
-dontwarn com.squareup.moshi.**

# Keep Moshi annotations and annotated members
-keep @interface com.squareup.moshi.* { *; }
-keep @com.squareup.moshi.JsonQualifier @interface * { *; }
-keepclassmembers class * {
    @com.squareup.moshi.Json <fields>;
    @com.squareup.moshi.Json <methods>;
    @com.squareup.moshi.FromJson *;
    @com.squareup.moshi.ToJson *;
}

# Keep Moshi @JsonClass annotated classes, companion objects, and members
-keep @com.squareup.moshi.JsonClass class * { *; }
-keepclassmembers @com.squareup.moshi.JsonClass class * {
    <fields>;
    <methods>;
}

# Keep KSP-generated Moshi JSON Adapters and constructors
-keep class *JsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
    public <init>(com.squareup.moshi.Moshi, java.lang.reflect.Type[]);
    public <init>(...);
    *;
}
-keep class **.*JsonAdapter { *; }
-keep class com.squareup.moshi.internal.NullSafeJsonAdapter { *; }
-keep class com.squareup.moshi.adapters.** { *; }

# Prevent Kotlin metadata stripping essential for Moshi Kotlin reflection
-keep class kotlin.Metadata { *; }
-keepclassmembers class kotlin.Metadata { *; }
-dontwarn kotlin.reflect.jvm.internal.**
-keep class kotlin.reflect.jvm.internal.** { *; }

# ------------------------------------------------------------------------------
# 4. Kotlin Runtime, Metadata & Coroutines
# ------------------------------------------------------------------------------
-dontwarn kotlin.**
-keep class kotlin.Metadata { *; }
-keepclassmembers class kotlin.Metadata { *; }

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.**

# ------------------------------------------------------------------------------
# 5. Jetpack Compose & Material 3
# ------------------------------------------------------------------------------
-keep class androidx.compose.runtime.** { *; }
-keepclassmembers class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.** { *; }
-keep class androidx.compose.material3.** { *; }

# Preserve custom Composables and State models
-keepclassmembers class * {
    @androidx.compose.runtime.Composable *;
    @androidx.compose.runtime.ReadOnlyComposable *;
    @androidx.compose.runtime.Stable *;
    @androidx.compose.runtime.Immutable *;
}

# ------------------------------------------------------------------------------
# 6. Room Database & Local SQLite Persistence
# ------------------------------------------------------------------------------
# Keep all data models, entities, and DAOs
-keep class com.example.data.** { *; }
-keepclassmembers class com.example.data.** { *; }

# Keep Room Database implementation classes generated by KSP
-keep class * extends androidx.room.RoomDatabase {
    public <init>();
    *;
}
-keep class * extends androidx.room.EntityDeletionOrUpdateAdapter { *; }
-keep class * extends androidx.room.EntityInsertionAdapter { *; }
-keep class * extends androidx.room.SharedSQLiteStatement { *; }

-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-keep @androidx.room.Database class * { *; }
-keep @androidx.room.TypeConverter class * { *; }
-keep @androidx.room.TypeConverters class * { *; }

-keepclassmembers class * {
    @androidx.room.TypeConverter *;
}

-dontwarn androidx.room.paging.**

# ------------------------------------------------------------------------------
# 7. AndroidX Architecture Components (ViewModel, Lifecycle, SavedState)
# ------------------------------------------------------------------------------
-keep class * extends androidx.lifecycle.ViewModel {
    public <init>(...);
}
-keep class * extends androidx.lifecycle.AndroidViewModel {
    public <init>(...);
}
-keepclassmembers class androidx.lifecycle.LifecycleObserver {
    @androidx.lifecycle.OnLifecycleEvent *;
}

# ------------------------------------------------------------------------------
# 8. Kotlinx Serialization & JSON Mapping
# ------------------------------------------------------------------------------
-keepattributes *Annotation*,InnerClasses
-dontwarn kotlinx.serialization.**
-keepclassmembers class kotlinx.serialization.** {
    *** Companion;
}
-keepclasseswithmembers class * {
    @kotlinx.serialization.Serializable *;
}
-keepclasseswithmembers class * {
    @kotlinx.serialization.Serializer *;
}
-keepclassmembers class * {
    *** Companion;
}
-keepclassmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}

# ------------------------------------------------------------------------------
# 9. Security, Cryptography & Google Play Integrity
# ------------------------------------------------------------------------------
-keepclassmembers class * extends java.security.Provider {
    *;
}
-keep class javax.crypto.** { *; }
-keep interface javax.crypto.** { *; }
-dontwarn javax.crypto.**

# Google Play Services & FileProvider
-keep class androidx.core.content.FileProvider { *; }
-keep public class com.google.android.gms.common.** { *; }
-dontwarn com.google.android.gms.**

# ------------------------------------------------------------------------------
# 10. General Cleanup & Optimization Flags
# ------------------------------------------------------------------------------
-dontpreverify
-repackageclasses ''
-allowaccessmodification
-mergeinterfacesaggressively

# Ignore benign warnings from external libraries
-dontwarn sun.misc.Unsafe
-dontwarn java.lang.invoke.**
