package com.novyr.callfilter;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberUtils;
import android.util.Log;

import com.novyr.callfilter.models.Contact;
import com.novyr.callfilter.models.WhitelistEntry;
import com.orm.SugarApp;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class CallFilterApplication extends SugarApp {
    private static final String TAG = CallFilterApplication.class.getName();
    private static final HashMap<String, Contact> mContacts = new HashMap<>();

    public static boolean shouldBlockCall(Context context, String number) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        // Private
        if (number == null) {
            // We don't need to check the number against the contacts or whitelist if it is private
            return sharedPref.getBoolean("block_private", false);
        }

        // Contacts
        boolean blockUnknown = sharedPref.getBoolean("block_unknown", false);

        return blockUnknown && !isNumberInContacts(context, number) && !isNumberInWhitelist(number);
    }

    public static boolean isNumberInWhitelist(String number) {
        List<WhitelistEntry> entries = WhitelistEntry.find(WhitelistEntry.class, "number = ?", number);

        return entries.size() > 0;
    }

    public static boolean isNumberInContacts(Context context, String number) {
        if (number == null) {
            return false;
        }

        try {
            Contact contact = getContactInfo(context, number);
            return (contact != null);
        } catch (Exception e) {
            // If we failed to check we to return true to prevent blocking anything
            return true;
        }
    }

    public static Contact getContactInfo(Context context, String number) {
        if (mContacts.containsKey(number)) {
            return mContacts.get(number);
        }

        Uri lookupUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
        String[] mPhoneNumberProjection = {ContactsContract.PhoneLookup._ID, ContactsContract.PhoneLookup.NUMBER, ContactsContract.PhoneLookup.DISPLAY_NAME};
        Cursor cursor = context.getContentResolver().query(lookupUri, mPhoneNumberProjection, null, null, null);

        if (cursor == null) {
            throw new InternalError("Failed to lookup number in contacts");
        }

        try {
            if (cursor.moveToFirst()) {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup.DISPLAY_NAME));
                String id = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup._ID));
                mContacts.put(number, new Contact(name, id));
                return mContacts.get(number);
            }
        } catch (Exception e) {
            Log.d(TAG, "Failed to find check phone number against contacts", e);
        } finally {
            cursor.close();
        }

        return null;
    }

    @SuppressWarnings("deprecation")
    public static String formatNumber(Context context, String number) {
        if (number == null) {
            return "Unknown";
        }

        Contact contact = null;
        if (context != null) {
            contact = CallFilterApplication.getContactInfo(context, number);
        }

        if (contact != null) {
            return contact.name;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return PhoneNumberUtils.formatNumber(number, Locale.getDefault().getCountry());
        } else {
            return PhoneNumberUtils.formatNumber(number);
        }
    }
}
