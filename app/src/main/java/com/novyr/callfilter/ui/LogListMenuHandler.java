package com.novyr.callfilter.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.ContactsContract;

import androidx.annotation.NonNull;

import com.novyr.callfilter.ContactFinder;
import com.novyr.callfilter.db.entity.LogEntity;
import com.novyr.callfilter.db.entity.WhitelistEntity;
import com.novyr.callfilter.viewmodel.LogViewModel;
import com.novyr.callfilter.viewmodel.WhitelistViewModel;

import java.util.List;

class LogListMenuHandler {
    private final ContactFinder mContactFinder;
    private final Context mContext;
    private final LogViewModel mLogViewModel;
    private final WhitelistViewModel mWhitelistViewModel;

    LogListMenuHandler(Context context, LogViewModel logViewModel, WhitelistViewModel whitelistViewModel) {
        mContext = context;
        mContactFinder = new ContactFinder(context);
        mLogViewModel = logViewModel;
        mWhitelistViewModel = whitelistViewModel;
    }

    boolean hasContact(final String number) {
        try {
            String contactName = mContactFinder.findContactName(number);

            return contactName != null;
        } catch (Exception ignored) {
        }
        return false;
    }

    void removeLog(LogEntity log) {
        mLogViewModel.delete(log);
    }

    boolean isWhitelisted(String number) {
        WhitelistEntity entity = findEntity(number);

        return entity != null;
    }

    void addToWhitelist(@NonNull String number) {
        WhitelistEntity entity = findEntity(number);
        if (entity == null) {
            mWhitelistViewModel.insert(new WhitelistEntity(number));
        }
    }

    void openInContacts(final String number) {
        String contactId = mContactFinder.findContactId(number);
        if (contactId == null) {
            return;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, contactId);
        intent.setData(uri);
        mContext.startActivity(intent);
    }

    void removeFromWhitelist(@NonNull String number) {
        WhitelistEntity entity = findEntity(number);
        if (entity != null) {
            mWhitelistViewModel.delete(entity);
        }
    }

    private WhitelistEntity findEntity(String number) {
        if (number == null) {
            return null;
        }

        List<WhitelistEntity> list = mWhitelistViewModel.getCurrentEntities();
        if (list == null) {
            return null;
        }

        for (WhitelistEntity entity : list) {
            if (number.equals(entity.getNumber())) {
                return entity;
            }
        }

        return null;
    }
}
