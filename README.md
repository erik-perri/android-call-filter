# Call Filter

A basic app to reject calls from numbers not in your contacts or from private numbers.

**This has only been tested on a phone running 6.0.1, any newer versions have only been tested in an emulator.**


## Issues

 * Instant run must be disabled in Android Studio for debugging, it is not compatible with the version of [Sugar ORM](https://github.com/chennaione/sugar) used.
 * The app does not always get notified about a call to reject it before the ringer can start.  This should be fixed in Q with the [CallScreeningService](https://developer.android.com/reference/android/telecom/CallScreeningService.html) API.
 * The app allows you to whitelist numbers not in the contact list but has no way to view the whitelist.
