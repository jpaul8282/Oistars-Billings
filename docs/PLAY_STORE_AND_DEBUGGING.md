# Google Play Store Symbols, Release Signing & Debugging Guide

## 1. Google Play Console Identifiers & Rules

- **Package Identifier**: `com.aistudio.oistarsbillings.kxpobq` *(MANDATORY: Must not be altered)*
- **Namespace**: `com.example`
- **Version Code**: `2` *(Must be strictly greater than version 1 for Play Console release)*
- **Version Name**: `1.1`
- **Target SDK**: `34` (Android 14)
- **Minimum SDK**: `26` (Android 8.0 Oreo)

---

## 2. Google Play Store Symbols & UI Components

The application renders vector and card components representing Google Play Store certification:

1. **`GooglePlayStoreSymbol` (`ProductionBadges.kt`)**:
   - 4-color faceted polygon drawn with Jetpack Compose Canvas paths.
   - Exact chromatic accuracy (Play Teal `#01875F`, Cyan `#00C3FF`, Red `#FF3333`, Yellow `#FFD400`).
2. **`GooglePlayStoreBadgeCard`**:
   - Store rating: **PEGI 3 / Everyone**.
   - Play Protect Shield: Verified malware-free hermetic build.
   - Category: **Finance / Merchant Ledger**.
   - Interactive dialog detailing package registration, Play Integrity, and local data isolation.

---

## 3. Production Release & Signing Workflow

### Build Commands:
```bash
# Debug APK Build
gradle assembleDebug

# Release Android App Bundle (AAB)
gradle bundleRelease
```

### Keystore Configuration (`app/build.gradle.kts`):
The release signing configuration uses environment variables when provided, falling back gracefully:
- `KEYSTORE_PATH`: Absolute path to upload keystore (.jks)
- `STORE_PASSWORD`: Keystore master password
- `KEY_ALIAS`: Key alias name (e.g. `upload`)
- `KEY_PASSWORD`: Key password

---

## 4. Debugging & Troubleshooting

### 4.1 R8 / ProGuard Optimization & De-obfuscation
When testing with release minification enabled (`isMinifyEnabled = true`):
1. **Mapping File**: Generated at `app/build/outputs/mapping/release/mapping.txt`.
2. **Seeds File**: Generated at `app/build/outputs/mapping/release/seeds.txt` (shows which classes and methods were preserved).
3. **Usage File**: Generated at `app/build/outputs/mapping/release/usage.txt` (shows stripped dead code).

To restore an obfuscated crash stack trace:
```bash
retrace app/build/outputs/mapping/release/mapping.txt stacktrace.txt
```

### 4.2 Room Database SQLite Debugging
If entity schema changes occur, Room verifies schemas through KSP:
- Database version: `1`
- Export Schema: `false` (in `AppDatabase.kt`)
- All database operations execute on background dispatcher `Dispatchers.IO` via Kotlin Flows.

### 4.3 Logcat Tagging & Filter Guide
- UI State & ViewModels: `tag:BillingViewModel`
- Room Transactions: `tag:Room`
- Touch & Defocus: `tag:FocusUtils`
- Compose Recomposition: `tag:Compose`
