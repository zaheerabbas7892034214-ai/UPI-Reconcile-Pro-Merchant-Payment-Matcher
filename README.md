# UPI Reconcile Pro – Merchant Payment Matcher

**Production-Ready Android Application** for reconciling UPI payment statements with sales invoices.

## 📱 App Overview

**UPI Reconcile Pro** is a complete MVVM Android app built with Jetpack Compose and Material 3 that helps merchants reconcile their UPI payment statements with sales registers. It automatically matches transactions, detects discrepancies, and generates detailed reports.

### Key Features

✅ **CSV Import** - Import UPI statements and sales registers via Storage Access Framework  
✅ **Smart Matching** - Automatic reconciliation with configurable date tolerance  
✅ **Discrepancy Detection** - Identify missing payments, duplicates, and mismatches  
✅ **Professional Reports** - Export to Excel (multi-sheet) and PDF  
✅ **Subscription Model** - ₹1,499/year via Google Play Billing  
✅ **Security** - App lock with PIN and biometric authentication  
✅ **Material 3 Design** - Modern, beautiful UI with animations  

## 🏗️ Architecture

- **Pattern**: MVVM (Model-View-ViewModel)
- **UI**: Jetpack Compose + Material 3
- **Database**: Room (SQLite)
- **Async**: Kotlin Coroutines + Flow
- **Billing**: Google Play Billing Library v6+
- **Navigation**: Jetpack Navigation Compose

## 📋 Requirements

- **Android Studio**: Flamingo (2022.2.1) or later
- **Gradle**: 8.2+
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)
- **Kotlin**: 1.9.22
- **Java**: 17

## 🛠️ Tech Stack

### Core
- Kotlin 1.9.22
- Android Gradle Plugin 8.2.2
- Jetpack Compose BOM 2024.02.00
- Material 3
- Compose Navigation 2.7.6

### Database & Storage
- Room 2.6.1 with KSP
- DataStore Preferences 1.0.0

### Business Logic
- Kotlin CSV JVM 1.9.3 (CSV parsing)
- Apache POI 5.2.5 (Excel export)
- iText 7.2.5 (PDF generation)

### Android Services
- Google Play Billing 6.1.0
- Biometric 1.1.0
- Lifecycle ViewModel Compose 2.7.0

## 📂 Project Structure

```
app/src/main/
├── java/com/zaheer/upireconcilepro/
│   ├── UPIReconcileApplication.kt          # Application class
│   ├── MainActivity.kt                      # Main activity
│   │
│   ├── ui/
│   │   ├── theme/                          # Material 3 theme
│   │   │   ├── Color.kt
│   │   │   ├── Type.kt
│   │   │   └── Theme.kt
│   │   │
│   │   ├── screens/                        # 11 Compose screens
│   │   │   ├── SplashScreen.kt
│   │   │   ├── HomeScreen.kt
│   │   │   ├── UploadScreen.kt
│   │   │   ├── ProcessingScreen.kt
│   │   │   ├── DashboardScreen.kt
│   │   │   ├── MatchedListScreen.kt
│   │   │   ├── UnmatchedListScreen.kt
│   │   │   ├── ExportScreen.kt
│   │   │   ├── PaywallScreen.kt
│   │   │   ├── SettingsScreen.kt
│   │   │   └── AppLockScreen.kt
│   │   │
│   │   ├── components/                     # Reusable UI components
│   │   │   ├── MetricCard.kt
│   │   │   ├── RiskScoreGauge.kt
│   │   │   ├── ProFeatureBadge.kt
│   │   │   ├── LoadingDialog.kt
│   │   │   ├── ErrorDialog.kt
│   │   │   └── EmptyStateView.kt
│   │   │
│   │   └── navigation/
│   │       ├── NavGraph.kt                 # Old navigation (kept for reference)
│   │       └── AppNavigation.kt            # Active navigation
│   │
│   ├── viewmodel/                          # 7 ViewModels
│   │   ├── HomeViewModel.kt
│   │   ├── UploadViewModel.kt
│   │   ├── DashboardViewModel.kt
│   │   ├── MatchedViewModel.kt
│   │   ├── UnmatchedViewModel.kt
│   │   ├── SettingsViewModel.kt
│   │   └── PaywallViewModel.kt
│   │
│   ├── data/
│   │   ├── database/
│   │   │   ├── AppDatabase.kt              # Room database
│   │   │   ├── entity/                     # 4 entities
│   │   │   │   ├── ReconciliationSessionEntity.kt
│   │   │   │   ├── MatchedItemEntity.kt
│   │   │   │   ├── UnmatchedItemEntity.kt
│   │   │   │   └── EntitlementEntity.kt
│   │   │   └── dao/                        # 4 DAOs
│   │   │       ├── ReconciliationSessionDao.kt
│   │   │       ├── MatchedItemDao.kt
│   │   │       ├── UnmatchedItemDao.kt
│   │   │       └── EntitlementDao.kt
│   │   │
│   │   ├── repository/                     # 3 repositories
│   │   │   ├── ReconciliationRepository.kt
│   │   │   ├── FileRepository.kt
│   │   │   └── EntitlementRepository.kt
│   │   │
│   │   └── model/                          # Data models
│   │       ├── CSVData.kt
│   │       ├── ColumnMapping.kt
│   │       ├── ReconciliationResult.kt
│   │       ├── ReconciliationMetrics.kt
│   │       └── MatchType.kt
│   │
│   ├── util/                               # Utility classes
│   │   ├── CSVParser.kt
│   │   ├── ColumnDetector.kt
│   │   ├── ReconciliationEngine.kt
│   │   ├── ExcelExporter.kt
│   │   ├── PDFExporter.kt
│   │   └── BiometricHelper.kt
│   │
│   └── billing/
│       └── BillingManager.kt               # Google Play Billing v6+
│
└── res/
    ├── values/
    │   ├── strings.xml
    │   ├── colors.xml
    │   └── themes.xml
    ├── xml/
    │   ├── file_paths.xml
    │   ├── backup_rules.xml
    │   └── data_extraction_rules.xml
    └── mipmap-*/
        └── ic_launcher.xml
```

## 🚀 Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/zaheerabbas7892034214-ai/UPI-Reconcile-Pro-Merchant-Payment-Matcher.git
cd UPI-Reconcile-Pro-Merchant-Payment-Matcher
```

### 2. Open in Android Studio

1. Open Android Studio
2. Select **File > Open**
3. Navigate to the cloned directory
4. Wait for Gradle sync to complete

### 3. Build the Project

```bash
# Clean build
./gradlew clean

# Build debug APK
./gradlew assembleDebug

# Build release AAB (for Play Store)
./gradlew bundleRelease
```

### 4. Run on Emulator/Device

1. Create/start an Android emulator (API 24+) or connect a device
2. Click **Run** (▶️) in Android Studio
3. Or use command line:

```bash
./gradlew installDebug
```

## 💰 Google Play Billing Setup

### Configure In-App Subscription

1. **Create App in Google Play Console**
   - Go to [Google Play Console](https://play.google.com/console)
   - Create new app or select existing

2. **Create Subscription Product**
   - Navigate to **Monetization > Products > Subscriptions**
   - Click **Create subscription**
   - Product ID: `upireconcile_pro_yearly`
   - Name: UPI Reconcile Pro - Yearly
   - Description: Full access to all PRO features

3. **Create Base Plan**
   - Base plan ID: `yearly_base`
   - Billing period: 1 Year
   - Price: ₹1,499 (India)
   - Add prices for other countries if needed

4. **Activate Subscription**
   - Complete all required fields
   - Activate the subscription

### License Testing

1. **Add License Testers**
   - Go to **Setup > License testing**
   - Add test Gmail accounts
   - Set license response to **RESPOND_NORMALLY**

2. **Test Purchase Flow**
   - Install app on test device with test account signed in
   - Go through purchase flow
   - Test account will not be charged
   - Verify PRO features unlock

### Internal Testing

1. **Create Internal Test Track**
   - Go to **Testing > Internal testing**
   - Create new release
   - Upload AAB file
   - Add test users (up to 100)

2. **Share Test Link**
   - Copy opt-in URL
   - Share with testers
   - Testers join via link and download from Play Store

## 📱 Sample CSV Formats

### UPI Statement CSV

```csv
Date,Transaction ID,Merchant,Amount,Status
01/01/2024,UTR12345678,ABC Traders,5000.00,Success
02/01/2024,UTR12345679,XYZ Store,2500.50,Success
03/01/2024,UTR12345680,DEF Suppliers,7500.00,Success
```

**Supported Headers:**
- Date: `Date`, `Transaction Date`, `Txn Date`
- Reference: `Transaction ID`, `UTR`, `Reference`, `ID`, `Txn ID`
- Merchant: `Merchant`, `Payee`, `Vendor`, `To`
- Amount: `Amount`, `Credit`, `Debit`, `Value`, `₹`

### Sales Register CSV

```csv
Invoice Date,Invoice No,Customer,Amount,Payment Status
01/01/2024,INV-001,ABC Traders,5000.00,Paid
02/01/2024,INV-002,XYZ Store,2500.50,Paid
04/01/2024,INV-003,GHI Enterprises,3000.00,Pending
```

**Supported Headers:**
- Date: `Invoice Date`, `Date`, `Billing Date`
- Reference: `Invoice No`, `Invoice Number`, `Bill No`, `Reference`
- Customer: `Customer`, `Client`, `Party Name`
- Amount: `Amount`, `Total`, `Bill Amount`, `₹`

## 🔐 Security

### ProGuard Configuration

The app includes comprehensive ProGuard rules for release builds:

```properties
# Keep Room, Billing, POI, iText classes
# Optimize bytecode while maintaining functionality
# See proguard-rules.pro for details
```

### App Lock

- 4-digit PIN
- Biometric authentication (fingerprint/face)
- Auto-lock on app background

### Data Protection

- All financial data stored locally in encrypted Room database
- No cloud sync (user privacy first)
- FileProvider for secure file sharing

## 🏷️ FREE vs PRO Features

### FREE Tier
- ✅ Upload 1 UPI statement CSV
- ✅ Upload 1 sales register CSV
- ✅ Preview first 20 matched results
- ✅ Basic reconciliation
- ❌ No export
- ❌ No history
- ❌ Limited to 3 sessions

### PRO Tier (₹1,499/year)
- ✅ Unlimited file uploads
- ✅ Full reconciliation results
- ✅ Mismatch & duplicate detection
- ✅ Export Excel + PDF
- ✅ Save reconciliation history
- ✅ App lock (PIN + Biometric)
- ✅ Priority support

## 🧪 Testing

### Unit Tests (TODO)

```bash
./gradlew test
```

### Instrumentation Tests (TODO)

```bash
./gradlew connectedAndroidTest
```

### Manual Testing Checklist

- [ ] App launches without crash
- [ ] Splash screen displays for 2 seconds
- [ ] Upload CSV files via file picker
- [ ] CSV parsing succeeds with sample data
- [ ] Column auto-detection works
- [ ] Reconciliation completes successfully
- [ ] Dashboard shows correct metrics
- [ ] FREE tier shows 20-item limit
- [ ] PRO features locked for free users
- [ ] Paywall screen displays correctly
- [ ] Purchase flow launches (test mode)
- [ ] Export Excel generates valid file
- [ ] Export PDF generates valid file
- [ ] App lock PIN works
- [ ] Biometric authentication works
- [ ] Settings persist correctly

## 📦 Building Release

### Generate Keystore

```bash
keytool -genkey -v -keystore upi-reconcile-pro.keystore \
  -alias upi-reconcile-pro -keyalg RSA -keysize 2048 -validity 10000
```

### Sign AAB

Add to `local.properties`:

```properties
RELEASE_STORE_FILE=../upi-reconcile-pro.keystore
RELEASE_STORE_PASSWORD=your_keystore_password
RELEASE_KEY_ALIAS=upi-reconcile-pro
RELEASE_KEY_PASSWORD=your_key_password
```

Build signed AAB:

```bash
./gradlew bundleRelease
```

Output: `app/build/outputs/bundle/release/app-release.aab`

## 🐛 Known Issues

- None currently reported

## 📄 License

```
Copyright 2024 Zaheer Abbas

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

## 🔗 Links

- **Privacy Policy**: [To be added]
- **Terms of Service**: [To be added]
- **Support Email**: [To be added]
- **Google Play Store**: [Coming soon]

## 👥 Contributing

Contributions are welcome! Please follow these guidelines:

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

## 📞 Support

For issues or questions:
- Open an issue on GitHub
- Contact: [To be added]

---

**Built with ❤️ using Jetpack Compose & Material 3**
