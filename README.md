# Call Filter

A basic app to reject calls from numbers not in your contacts or from private numbers.

## Issues

 * On Android versions before Q (API v29) the app does not always get notified about a call to reject it before the ringer can start.  This is fixed in Q with the [CallScreeningService](https://developer.android.com/reference/android/telecom/CallScreeningService.html) API.
 * On Android versions before Lollipop (API v21) the app must run as a system app to block calls.
 * The app allows you to whitelist numbers not in the contact list but has no way to view the whitelist.

## License

[MIT](https://opensource.org/licenses/MIT)
