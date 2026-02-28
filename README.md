# Call Filter

An Android app to reject calls from numbers matching various conditions.

<img src=".docs/rule_list.png" alt="Rule list" width="250" height="444"/> <img src=".docs/rule_edit.png" alt="Rule edit" width="250" height="444"/> <img src=".docs/log_list.png" alt="Log list" width="250" height="444"/>

## Build

Use [Android Studio](https://developer.android.com/studio)

## Releases

Releases are automated via GitHub Actions. Pushing a tag matching `v<major>.<minor>.<patch>` (e.g. `v1.2.3`) runs unit tests, builds a signed release APK, and publishes it as a [GitHub Release](https://github.com/erik-perri/android-call-filter/releases).

Build provenance is attested so you can verify an APK was built from this repository with the [GitHub CLI](https://github.com/cli/cli) tool:

```bash
gh attestation verify call-filter-<version>.apk -R erik-perri/android-call-filter
```

## Issues

 * On Android versions before Q (API v29) the app does not always get notified about a call to reject it before the ringer can start.  This is fixed in Q with the [CallScreeningService](https://developer.android.com/reference/android/telecom/CallScreeningService.html) API.
 * On Android versions before Lollipop (API v21) the app must run as a system app to block calls.

## Testing

Unit tests run without an emulator:

```bash
./gradlew testDebugUnitTest
```

Instrumented tests use [Gradle Managed Devices](https://developer.android.com/studio/test/managed-devices) to automatically provision emulators. Because the test libraries require a higher minSdk than the app itself, pass `-PtestMinSdk=26`:

```bash
# Run on a single device (e.g. API 30)
./gradlew pixel2Api30DebugAndroidTest -PtestMinSdk=26

# Run on all configured devices (API 26, 28, 30)
./gradlew allDevicesDebugAndroidTest -PtestMinSdk=26
```

To avoid passing the property every time, add `testMinSdk=21` to your local `gradle.properties` (this file is not committed).

You can also run against a connected emulator or device directly:

```bash
./gradlew connectedDebugAndroidTest -PtestMinSdk=21
```

## License

[MIT](https://opensource.org/licenses/MIT)
