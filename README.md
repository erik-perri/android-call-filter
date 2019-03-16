# Call Filter

This is a basic app that will silence the ringer and hang up on calls that are either not in your contacts or from private numbers.

**This project is abandoned and should not be used.  The code has not been updated or maintained since ~2015.**


## Issues

If you ignore my warning about not using it beware of these issues:

 * It stopped working when Google started requiring the system-only `MODIFY_PHONE_STATE` permission to access the private telephony API (somewhere around API Level 28?).
 * Instant run must be disabled in Android Studio for debugging, it is not compatible with the version of [Sugar ORM](https://github.com/chennaione/sugar) used.
 * The app does not always get notified about the call before the ringer can start, so there is sometimes a second of ringer before it hangs up.
