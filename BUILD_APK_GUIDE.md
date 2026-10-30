# Build APK Guide - Android Studio

## Why Build with Android Studio?

The command-line build is failing due to network connectivity issues (cannot reach Google's Maven repository). **Android Studio handles network issues better** and has built-in caching that makes builds more reliable.

## Steps to Build APK

### 1. Open Project in Android Studio

1. Launch Android Studio
2. Click **"Open"** or **"File → Open"**
3. Navigate to: `C:\Users\DBTECH AFRICA\Desktop\waiter order android apk\waiter-orders`
4. Click **"OK"**
5. Wait for Gradle sync to complete (this may take a few minutes on first open)

### 2. Wait for Gradle Sync

- Android Studio will automatically sync the project
- Look at the bottom progress bar
- Wait until you see "Gradle sync finished"
- If you see errors, click "Try Again"

### 3. Build Debug APK

#### Option A: Build APK Directly

1. Click **"Build"** in the top menu
2. Select **"Build Bundle(s) / APK(s)"**
3. Select **"Build APK(s)"**
4. Wait for the build to complete (this may take 5-10 minutes)
5. When complete, you'll see a notification: "BUILD SUCCESSFUL"
6. Click **"locate"** in the notification

#### Option B: Build from Run Configuration

1. Click the **"Build"** dropdown menu (left of the green play button)
2. Select **"Build APK"**
3. Wait for build to complete
4. Click the notification to locate the APK

### 4. Locate the APK

The APK will be located at:
```
C:\Users\DBTECH AFRICA\Desktop\waiter order android apk\waiter-orders\app\build\outputs\apk\debug\app-debug.apk
```

Or use the "locate" button in the build notification.

### 5. Install on Your Phone

#### Method A: USB Connection

1. Enable **USB Debugging** on your phone:
   - Go to Settings → About Phone
   - Tap "Build Number" 7 times to enable Developer Options
   - Go to Settings → Developer Options
   - Enable "USB Debugging"
2. Connect phone to PC via USB
3. In Android Studio, click the green **play button** (Run)
4. Select your phone from the device list
5. App will install and launch automatically

#### Method B: Manual Installation

1. Copy the APK file to your phone (via USB, email, or cloud storage)
2. On your phone, navigate to the APK file
3. Tap to install
4. You may need to enable "Install from Unknown Sources" in Settings
5. Grant permissions if prompted

---

## Troubleshooting

### Gradle Sync Fails

**Issue:** Gradle sync fails with network errors

**Solution:**
1. Check your internet connection
2. Try using mobile hotspot instead of WiFi
3. In Android Studio: File → Invalidate Caches → Invalidate and Restart
4. Try again

### Build Fails with Dependency Errors

**Issue:** Cannot download dependencies

**Solution:**
1. Open `gradle.properties` in Android Studio
2. Add this line:
   ```properties
   systemProp.http.proxyHost=your-proxy-host
   systemProp.http.proxyPort=your-proxy-port
   ```
3. Or configure proxy in Android Studio Settings

### Build Fails with SDK Errors

**Issue:** SDK location not found

**Solution:**
1. File → Settings → Appearance & Behavior → System Settings → Android SDK
2. Verify SDK location is correct
3. Click "Edit" to install missing components

### APK Won't Install

**Issue:** "App not installed" or parsing error

**Solution:**
1. Uninstall any existing version of the app
2. Clear phone storage if low
3. Ensure Android version is 7.0 (API 24) or higher
4. Try building a release APK instead (see below)

---

## Build Release APK (Optional)

For a production-ready APK:

1. Open `app/build.gradle.kts`
2. Remove or comment out line 50:
   ```kotlin
   // signingConfig = signingConfigs.getByName("release")
   ```
3. Build → Build Bundle(s) / APK(s) → Build APK(s)
4. Select "release" variant from build variants (bottom left)
5. Build again

Note: Release APK requires signing configuration which may need keystore setup.

---

## Quick Checklist

- [ ] Open project in Android Studio
- [ ] Wait for Gradle sync to complete
- [ ] Build → Build Bundle(s) / APK(s) → Build APK(s)
- [ ] Wait for build to complete
- [ ] Locate APK in notification
- [ ] Copy APK to phone or install via USB
- [ ] Enable "Install from Unknown Sources" if needed
- [ ] Install and test the app

---

## What to Test After Installation

1. **Authentication:**
   - Should see login screen
   - Click "Sign in with Google"
   - Authenticate with your Google account
   - Should proceed to main app

2. **Create Order:**
   - Go to "New Order" tab
   - Add items to cart
   - Submit order
   - Check Firebase Console → Firestore → Data
   - Verify order appears in `orders` collection

3. **Menu Management:**
   - Open Settings → Manage Menu Items
   - Add/edit/delete menu items
   - Verify syncs to Firestore

4. **Sign Out:**
   - Open Settings
   - Click "Sign Out"
   - Should return to login screen

---

## Current Firebase Configuration

- **Project:** waiter-ordering
- **Database:** Firestore (nam5 - South Africa)
- **Authentication:** Google Sign-In enabled
- **OAuth Client ID:** Configured
- **Status:** Ready to use ✅

If you encounter any issues, check the error messages in Android Studio's Build tab for detailed information.
