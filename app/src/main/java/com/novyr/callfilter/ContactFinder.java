package com.novyr.callfilter;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import androidx.core.content.ContextCompat;

import com.novyr.callfilter.model.Contact;

import java.util.HashMap;

public class ContactFinder {
    private final HashMap<String, ContactModel> mContacts = new HashMap<>();
    private final ContentResolver mContentResolver;
    private final Context mContext;

    public ContactFinder(Context context) {
        mContext = context;
        mContentResolver = context.getContentResolver();
    }

    public String findContactName(String number) throws InternalError {
        Contact contact = findContact(number);

        return contact != null ? contact.getName() : null;
    }

    public String findContactId(String number) throws InternalError {
        Contact contact = findContact(number);

        return contact != null ? contact.getId() : null;
    }

    private Contact findContact(String number) throws InternalError {
        if (mContacts.containsKey(number)) {
            return mContacts.get(number);
        }

        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            throw new InternalError("Unable to lookup contacts due to permissions");
        }

        Uri lookupUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
        String[] phoneNumberProjection = {ContactsContract.PhoneLookup._ID, ContactsContract.PhoneLookup.NUMBER, ContactsContract.PhoneLookup.DISPLAY_NAME};
        Cursor cursor = mContentResolver.query(lookupUri, phoneNumberProjection, null, null, null);
        if (cursor == null) {
            throw new InternalError("Failed to query content resolver");
        }

        try {
            if (cursor.moveToFirst()) {
                String id = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup._ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup.DISPLAY_NAME));

                mContacts.put(number, new ContactModel(id, name));

                return mContacts.get(number);
            }
        } catch (Exception e) {
            throw new InternalError(String.format("Error while querying contacts %s", e.getMessage()));
        } finally {
            cursor.close();
        }

        return null;
    }

    private static class ContactModel implements Contact {
        private final String mId;
        private final String mName;

        ContactModel(String id, String name) {
            this.mId = id;
            this.mName = name;
        }

        @Override
        public String getId() {
            return mId;
        }

        @Override
        public String getName() {
            return mName;
        }
    }
}
