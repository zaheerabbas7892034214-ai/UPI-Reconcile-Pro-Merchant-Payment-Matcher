# Quick Start Guide - UPI Reconcile Pro

## Prerequisites

- **Android Studio**: Flamingo (2022.2.1) or later
- **JDK**: 17 or later
- **Android SDK**: API 24-34
- **Git**: For cloning the repository

## 5-Minute Setup

### Step 1: Clone & Open (1 min)
```bash
git clone https://github.com/zaheerabbas7892034214-ai/UPI-Reconcile-Pro-Merchant-Payment-Matcher.git
cd UPI-Reconcile-Pro-Merchant-Payment-Matcher
```

Open Android Studio → File → Open → Select project folder

### Step 2: Sync Gradle (2 min)
Wait for Gradle sync to complete. Android Studio will:
- Download dependencies
- Configure build tools
- Index project files

### Step 3: Run (2 min)
1. Connect Android device or start emulator (API 24+)
2. Click Run ▶️ button
3. Select target device
4. App installs and launches!

## Sample Data for Testing

### Sample UPI Statement (upi_statement.csv)
```csv
Date,Transaction ID,Merchant,Amount,Status
01/02/2024,UTR12345678,ABC Traders,5000.00,Success
02/02/2024,UTR12345679,XYZ Store,2500.50,Success
03/02/2024,UTR12345680,DEF Suppliers,7500.00,Success
04/02/2024,UTR12345681,ABC Traders,3000.00,Success
05/02/2024,UTR12345682,GHI Enterprises,4500.50,Success
```

### Sample Sales Register (sales_register.csv)
```csv
Invoice Date,Invoice No,Customer,Amount,Payment Status
01/02/2024,INV-001,ABC Traders,5000.00,Paid
02/02/2024,INV-002,XYZ Store,2500.50,Paid
04/02/2024,INV-003,GHI Enterprises,3000.00,Pending
05/02/2024,INV-004,ABC Traders,4500.50,Paid
06/02/2024,INV-005,JKL Company,6000.00,Pending
```

Save these as CSV files and use them to test the app!

## Testing the App

### 1. First Launch
- Splash screen appears for 2 seconds
- Home screen loads

### 2. Upload Files
- Click "Start Reconciliation"
- Select UPI statement CSV
- Select sales register CSV
- Click "Proceed"

### 3. View Results
- Processing screen shows progress
- Dashboard displays metrics
- Click "View Matched" to see matches
- FREE users see first 20 results

### 4. Test PRO Features (License Testing)
1. Go to Settings
2. Click "Upgrade to PRO"
3. Use test account (add in Play Console)
4. Complete purchase flow
5. Verify PRO features unlock

## Google Play Billing Test Setup

### Quick Test Configuration
1. **Play Console** → Your App → Setup → License testing
2. Add your Gmail address
3. Set response: **RESPOND_NORMALLY**
4. Build & install app
5. Sign in with test account
6. Test purchase (won't be charged)

## Common Issues & Solutions

### Issue: Gradle sync fails
**Solution**: Check internet connection, update Android Studio

### Issue: Build errors with dependencies
**Solution**: File → Invalidate Caches → Invalidate and Restart

### Issue: App crashes on launch
**Solution**: Check logcat for errors, verify API level 24+

### Issue: Billing not working
**Solution**: 
- Add test account in Play Console
- Use real device (not emulator)
- Sign in with test account
- Check subscription is active in Play Console

## App Navigation Flow

```
Home → Upload → Processing → Dashboard
                                 ↓
                    ┌────────────┴────────────┐
                    ↓                         ↓
              Matched List            Unmatched List
                    ↓                         ↓
                    └────────┬────────────────┘
                             ↓
                        Export Screen
```

## Key Features to Test

- [ ] CSV file upload via SAF
- [ ] Column auto-detection
- [ ] Manual column mapping (if auto fails)
- [ ] Reconciliation accuracy
- [ ] Dashboard metrics
- [ ] Risk score gauge
- [ ] Matched items list
- [ ] FREE 20-item limit
- [ ] Unmatched items (PRO)
- [ ] Export Excel (PRO)
- [ ] Export PDF (PRO)
- [ ] App lock with PIN
- [ ] Biometric authentication
- [ ] Settings persistence
- [ ] Subscription purchase
- [ ] Purchase restoration

## Development Tips

### Debug Mode
- Enable debug logging in `BuildConfig`
- Check logcat for detailed logs
- Use Android Studio profiler

### Testing Billing
- Use `com.android.test.purchased` for immediate success
- Use `android.test.canceled` for immediate cancel
- Use real test accounts for actual flow

### Database Inspection
Android Studio → View → Tool Windows → App Inspection → Database Inspector

### UI Preview
Each screen has `@Preview` annotations. Open file and click "Split" or "Design" view.

## File Locations

### Sample CSV Files
Save in: `Downloads/` or `Documents/`

### Exported Files
Location: App's external files directory
Access: Via Android's built-in file manager

### Database
Location: `/data/data/com.zaheer.upireconcilepro/databases/`
Access: Device Explorer in Android Studio (rooted device only)

## Build Commands

```bash
# Debug APK
./gradlew assembleDebug

# Debug install
./gradlew installDebug

# Run tests
./gradlew test

# Clean build
./gradlew clean build

# Release AAB (requires signing config)
./gradlew bundleRelease
```

## Pro Tips

1. **Use Android Device** - Emulator may have issues with file picker and biometric
2. **Test with Real Data** - Use actual CSV exports from your bank/software
3. **Check Column Headers** - Auto-detection works best with standard headers
4. **Date Format** - Supports DD/MM/YYYY, DD-MM-YYYY, YYYY-MM-DD, etc.
5. **Amount Format** - Handles ₹ symbol, commas, decimals automatically

## Getting Help

### Resources
- **README.md** - Comprehensive documentation
- **IMPLEMENTATION_SUMMARY.md** - Technical details
- **Code Comments** - Inline documentation

### Support
- GitHub Issues: Report bugs
- Stack Overflow: Technical questions
- Android Developers: Official docs

## Next Steps After Setup

1. ✅ Verify app runs without crashes
2. ✅ Test with sample CSV files
3. ✅ Configure Google Play Billing
4. ✅ Add license testers
5. ✅ Test purchase flow
6. ✅ Verify PRO features
7. ✅ Generate signed AAB
8. ✅ Submit to Play Store

---

**Ready to start?** Open Android Studio and let's build! 🚀
