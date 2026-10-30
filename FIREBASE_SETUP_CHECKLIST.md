# Firebase Setup Checklist for Waiter Order App

## Pre-Setup

- [ ] Create Google account (if not exists)
- [ ] Sign up for Firebase at https://console.firebase.google.com/
- [ ] Install Android Studio with latest SDK
- [ ] Ensure app package name: `com.aistudio.waiterorder.vrtx`

## Firebase Project Setup

### 1. Create Firebase Project
- [ ] Go to [Firebase Console](https://console.firebase.google.com/)
- [ ] Click "Add project"
- [ ] Project name: `devstephenke-sys/Waiter-Ordering`
- [ ] Enable Google Analytics (optional for now)
- [ ] Create project

### 2. Add Android App
- [ ] In Firebase Console, click Android icon
- [ ] Package name: `com.aistudio.waiterorder.vrtx`
- [ ] App nickname: `Waiter Orders`
- [ ] Debug signing certificate: use default (skip for now)
- [ ] Register app
- [ ] Download `google-services.json`
- [ ] Place `google-services.json` in `app/` folder
- [ ] Copy Google Services plugin code (already in `build.gradle.kts`)

### 3. Enable Authentication
- [ ] Go to Build → Authentication
- [ ] Click "Get Started"
- [ ] Sign-in method tab
- [ ] Enable Google
- [ ] Project support email: Add your email
- [ ] Enable Email/Password (optional)
- [ ] Save

### 4. Enable Firestore Database
- [ ] Go to Build → Firestore Database
- [ ] Click "Create Database"
- [ ] Select location: `europe-west3` (closest to Kenya) or your region
- [ ] Start in Test Mode
- [ ] Enable

### 5. Configure Firestore Collections

#### Create these collections manually in Firestore Console:

**menuItems** collection:
- [ ] Add sample menu item
  - name: "Grilled Chicken & Chips"
  - category: "Meals"
  - price: 550
  - emoji: "🍗"

**orders** collection:
- [ ] Leave empty (app will populate)

**dailySettlements** collection:
- [ ] Leave empty (app will populate)

### 6. Set Firestore Security Rules

The project includes a `firestore.rules` file with security rules. Deploy them:

- [ ] Open `firestore.rules` file in project root
- [ ] Copy the content
- [ ] Go to Firebase Console → Firestore → Rules tab
- [ ] Paste the rules
- [ ] Click "Publish"

**Rules Summary:**
- Orders: Authenticated users only
- Menu Items: Public read, authenticated write
- Daily Settlements: Authenticated users only

### 7. Get OAuth Client ID

- [ ] Go to Firebase Console → Project Settings
- [ ] General tab
- [ ] Scroll to "Your apps" section
- [ ] Under Android app, find "OAuth 2.0 Client ID"
- [ ] Copy the Web Client ID (starts with numbers)
- [ ] Update `AuthRepository.kt` line 21: `setServerClientId("YOUR_WEB_CLIENT_ID")`

### 8. Firebase Console Settings

- [ ] Go to Project Settings → General
- [ ] Note down:
  - Project ID
  - API Key
  - App ID

## Android App Configuration

### 9. Update Dependencies

In `app/build.gradle.kts`:
- [ ] Ensure these are uncommented (already done):
  ```kotlin
  implementation(libs.firebase.auth)
  implementation(libs.androidx.credentials)
  implementation(libs.androidx.credentials.play.services)
  implementation(libs.googleid)
  implementation(libs.firebase.appcheck.recaptcha)
  ```

### 10. Verify google-services.json

- [ ] File exists in `app/google-services.json`
- [ ] File has correct package name: `com.aistudio.waiterorder.vrtx`
- [ ] File contains API keys

### 11. Update AuthRepository

In `app/src/main/java/com/example/data/auth/AuthRepository.kt`:
- [ ] Line 21: Replace `YOUR_WEB_CLIENT_ID` with actual OAuth Client ID from step 7

### 12. Test Configuration

- [ ] Clean project: `Build → Clean Project`
- [ ] Rebuild project: `Build → Rebuild Project`
- [ ] Sync Gradle: `File → Sync Project with Gradle Files`

## Testing

### 13. Deploy Firestore Security Rules

- [ ] Go to Firebase Console → Firestore → Rules tab
- [ ] Copy content from `firestore.rules` file in project root
- [ ] Paste into Rules editor
- [ ] Click "Publish"

### 14. Run on Emulator

- [ ] Create Android emulator (API 24+)
- [ ] Add Google account to emulator
- [ ] Run app
- [ ] Should see login screen
- [ ] Click "Sign in with Google"
- [ ] Select Google account
- [ ] Should authenticate and show main app

### 15. Verify Firestore Sync

- [ ] Create an order in app
- [ ] Go to Firebase Console → Firestore → Data tab
- [ ] Check `orders` collection
- [ ] Verify order appears with all data
- [ ] Check menu items sync (if modified)
- [ ] Check daily settlements (after closing day)

### 16. Test Sign Out

- [ ] Open Settings dialog
- [ ] Click "Sign Out"
- [ ] Should return to login screen

## Production Checklist

### 16. App Signing

- [ ] Generate release keystore:
  ```bash
  keytool -genkey -v -keystore my-upload-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias upload
  ```
- [ ] Place keystore in project root
- [ ] Set environment variables:
  ```bash
  KEYSTORE_PATH=path/to/my-upload-key.jks
  STORE_PASSWORD=your-store-password
  KEY_PASSWORD=your-key-password
  ```

### 17. Firestore Production Rules

Change rules to production mode:
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /orders/{orderId} {
      allow read, write: if request.auth != null
        && request.auth.token.email_verified == true;
    }

    match /menuItems/{itemId} {
      allow read: if true;
      allow write: if request.auth != null
        && request.auth.token.email_verified == true;
    }

    match /dailySettlements/{settlementId} {
      allow read, write: if request.auth != null
        && request.auth.token.email_verified == true;
    }
  }
}
```

### 18. App Distribution

- [ ] Build release APK:
  ```bash
  ./gradlew assembleRelease
  ```
- [ ] Test release APK on physical device
- [ ] Upload to Google Play Console
- [ ] Complete Play Store listing

## Troubleshooting

### Common Issues

**"google-services.json not found"**
- Ensure file is in `app/` folder (not project root)
- Check file name exactly: `google-services.json`

**"Google Sign-In fails"**
- Verify OAuth Client ID is correct
- Check Firebase Console → Authentication → Sign-in method
- Ensure Google Sign-In is enabled
- Check emulator has Google Play Services

**"Firestore permission denied"**
- Check security rules
- Verify user is authenticated
- Check collection names match exactly

**"App crashes on launch"**
- Check `google-services.json` is valid JSON
- Verify package name matches
- Check build logs for errors

## Additional Resources

- [Firebase Documentation](https://firebase.google.com/docs)
- [Firebase Android Quickstart](https://github.com/firebase/quickstart-android)
- [Google Sign-In for Android](https://developers.google.com/identity/sign-in/android)
- [Firestore Security Rules](https://firebase.google.com/docs/firestore/security/rules)
