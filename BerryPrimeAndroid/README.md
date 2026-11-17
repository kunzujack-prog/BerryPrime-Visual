# Berry Prime Vissual – Android app project

This is an Android Studio project that packages the existing Berry Prime Vissual billing system in an Android WebView. It keeps the original invoices, receipts, payments, income/expenses, packages, events, reports, settings, payment-method controls and local browser storage.

## Build the APK
1. Install Android Studio (current stable) and Android SDK Platform 35.
2. Open this folder (`BerryPrimeAndroid`) in Android Studio.
3. Allow Gradle to sync and install any requested SDK/build tools.
4. Select **Build → Build Bundle(s) / APK(s) → Build APK(s)**.
5. Android Studio creates the debug APK under `app/build/outputs/apk/debug/app-debug.apk`. For a distributable release, use **Build → Generate Signed Bundle / APK** and sign the APK with your own keystore.

## Offline and cloud data
- Local data is stored in the app's private WebView storage and remains available offline.
- Cloud backup/restore uses your own Firebase Realtime Database project. The app does not include a shared developer Firebase account or any third-party credentials.
- In Firebase: create a project, add a Web app, enable **Authentication → Email/Password**, create a sign-in user, and create a **Realtime Database**. Copy the Web API key and database URL into Settings → Cloud backup & sync.
- Suggested Realtime Database rules (authenticated users can access only their own data):

```json
{
  "rules": {
    "users": {
      "$uid": {
        ".read": "auth != null && auth.uid === $uid",
        ".write": "auth != null && auth.uid === $uid"
      }
    }
  }
}
```

- Save the cloud settings, enter the Firebase account password, then choose **Back up this phone to cloud** or **Restore from cloud**. Cloud sync is manual; local saving remains automatic.
- Never share your Firebase password or API credentials publicly. The password is not saved by the app.

## Import existing business records
The source HTML uploaded for this Android build has `bpv-data` set to `null`; it does not contain an embedded data backup. If the old browser installation contains invoices/payments, open that old system on the device where the records exist, go to **Settings → Save copy with data**, then move that HTML backup to the Android phone. In the Android app, open **Settings → Import an existing data backup** and select it. Import replaces the current local data, so back up first.

## Notes
- Minimum Android version: Android 10 (API 29).
- The app requires internet for Firebase sync and for the existing online PDF/PNG export libraries. Core record keeping works offline.
- This workspace does not contain Android SDK/Gradle build tools, so an APK cannot be compiled or verified here. The supplied project is ready to open and build in Android Studio.

## Build without a PC (GitHub Actions)
1. Create a free GitHub repository and upload everything in this folder (keep the .github folder).
2. Open the Actions tab, select Build APK, and wait for it to finish (about 3-5 minutes).
3. Open the finished run and download BerryPrimeVissual-APK under Artifacts. Unzip it and install app-debug.apk on your phone (allow install from unknown apps).
