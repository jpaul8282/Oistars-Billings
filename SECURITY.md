# Security Policy & Vulnerability Disclosure

## 1. OpenSSF & Security Commitment

Oistars Billings is dedicated to delivering a secure financial billing and merchant ledger platform. We adhere to the **Open Source Security Foundation (OpenSSF)** best practices, **NIST SP 800-218** (Secure Software Development Framework), and **SLSA Level 3** supply chain principles.

---

## 2. Supported Versions

We provide active security patches and vulnerability remediation for the following release branches:

| Version | Supported | Minimum Android API | Target Android API |
| :--- | :--- | :--- | :--- |
| **1.1.x (Current)** | :white_check_mark: Yes | Android 26 (Oreo) | Android 34 (UpsideDownCake) |
| **1.0.x** | :white_check_mark: Critical Only | Android 26 (Oreo) | Android 34 (UpsideDownCake) |
| **< 1.0.0** | :x: No | Deprecated | Deprecated |

---

## 3. Reporting a Vulnerability

If you identify a security issue, vulnerability, or unexpected cryptographic behavior:

1. **Do NOT disclose publicly**: Please refrain from opening public issues, forums, or pull requests regarding unpatched vulnerabilities.
2. **Send an encrypted report**: Contact our security team at `security@oistars.example.com` or via direct contact through your Google AI Studio project administrators.
3. **Include the following information**:
   - Description of the vulnerability and its potential impact.
   - Exact steps or minimal proof of concept (PoC) to reproduce the vulnerability.
   - Device model, Android OS version, and application build number (`versionCode: 2`, `versionName: 1.1`).
   - Any suggested mitigations or patches.

---

## 4. Response Timeline & SLA

- **Acknowledgment**: Within 24–48 hours of receipt.
- **Triage & Severity Assessment**: Within 72 hours.
- **Fix & Patch Deployment**: Critical issues resolved within 7 calendar days; non-critical within 30 days.
- **Public Advisory / CVE**: Issued in coordination with the reporter after an updated release has been published to Google Play.

---

## 5. Security Architecture Highlights

- **Local Storage Isolation**: Database files (`oistars_billing_database`) are stored in internal app-private storage (`/data/data/com.aistudio.oistarsbillings.kxpobq/databases/`) with Unix file mode 600.
- **Zero Insecure Reflection**: All Room entities, DAOs, and UI state models are strictly typed and verified by the Kotlin compiler.
- **R8 / ProGuard Protection**: Code shrinking, dead code elimination, and symbol obfuscation prevent reverse-engineering and tampering.
- **Least-Privilege Permissions**: The app does not request SMS, contacts, location, camera, audio, or broad external storage permissions.

---

## 6. Safe Harbor

Any activities conducted in good faith according to this policy will be considered authorized conduct, and we will not initiate legal action against you for accidental, good-faith violations of this policy.
