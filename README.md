# Call Filter

An Android app to reject calls from numbers matching various conditions.

<img src=".docs/rule_list.png" alt="Rule list" width="250" height="444"/> <img src=".docs/rule_edit.png" alt="Rule edit" width="250" height="444"/> <img src=".docs/log_list.png" alt="Log list" width="250" height="444"/>

## Build

Use [Android Studio](https://developer.android.com/studio)

## Issues

 * On Android versions before Q (API v29) the app does not always get notified about a call to reject it before the ringer can start.  This is fixed in Q with the [CallScreeningService](https://developer.android.com/reference/android/telecom/CallScreeningService.html) API.
 * On Android versions before Lollipop (API v21) the app must run as a system app to block calls.

## License

[MIT](https://opensource.org/licenses/MIT)
