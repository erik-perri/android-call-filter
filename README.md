# Call Filter

This is a basic app that will silence the ringer and hang up on calls that are either not in your contacts or from private numbers.

**This has only been tested on a phone running 6.0, any newer versions have only been tested in an emulator.**


## Issues

 * Instant run must be disabled in Android Studio for debugging, it is not compatible with the version of [Sugar ORM](https://github.com/chennaione/sugar) used.
 * The app does not always get notified about the call before the ringer can start, so there is sometimes a second of ringer before it hangs up.
 * The app has a whitelist to allow numbers not in the contacts but has not way to view it.
