# Android build

GYMBOW is packaged for Android with Capacitor 8.

- Application ID: `com.ali.asghar.gymbow`
- Minimum Android API: 24 (Android 7.0), as required by Capacitor 8
- Camera permission: enabled for Movement Lab pose tracking
- Web assets: bundled locally in the APK; pose model files are downloaded on first use and require internet access

## Local build

```bash
npm install
npm run build
npx cap add android
node scripts/patch-android-manifest.mjs
npx cap sync android
cd android
./gradlew assembleDebug
```

The debug APK is generated at `android/app/build/outputs/apk/debug/app-debug.apk`.
