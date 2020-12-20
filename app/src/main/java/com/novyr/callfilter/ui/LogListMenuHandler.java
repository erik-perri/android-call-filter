package com.novyr.callfilter.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.ContactsContract;
import android.view.ContextMenu;
import android.view.MenuItem;

import com.novyr.callfilter.ContactFinder;
import com.novyr.callfilter.R;
import com.novyr.callfilter.db.entity.LogEntity;
import com.novyr.callfilter.viewmodel.LogViewModel;

class LogListMenuHandler {
    private final ContactFinder mContactFinder;
    private final LogViewModel mLogViewModel;
    private final Activity mActivity;

    LogListMenuHandler(
            Activity activity,
            ContactFinder contactFinder,
            LogViewModel logViewModel
    ) {
        mActivity = activity;
        mContactFinder = contactFinder;
        mLogViewModel = logViewModel;
    }

    void createMenu(Context context, final ContextMenu menu, final LogEntity entity) {
        final String number = entity.getNumber();
        final MenuItem.OnMenuItemClickListener listener = menuItem -> {
            int itemId = menuItem.getItemId();
            if (itemId == R.id.log_context_contacts_open) {
                openInContacts(number);
                return true;
            } else if (itemId == R.id.log_context_contact_create) {
                createContact(context, number);
                return true;
            } else if (itemId == R.id.log_context_log_remove) {
                removeLog(entity);
                return true;
            }
            return false;
        };

        mActivity.getMenuInflater().inflate(R.menu.menu_log_context, menu);

        boolean numberHasContact = hasContact(number);

        menu.findItem(R.id.log_context_contacts_open)
            .setVisible(numberHasContact)
            .setOnMenuItemClickListener(listener);

        menu.findItem(R.id.log_context_contact_create)
            .setVisible(!numberHasContact)
            .setOnMenuItemClickListener(listener);

        menu.findItem(R.id.log_context_log_remove)
            .setVisible(true)
            .setOnMenuItemClickListener(listener);
    }

    private boolean hasContact(final String number) {
        try {
            String contactName = mContactFinder.findContactName(number);

            return contactName != null;
        } catch (Exception ignored) {
        }
        return false;
    }

    private void removeLog(LogEntity entity) {
        mLogViewModel.delete(entity);
    }

    private void openInContacts(final String number) {
        String contactId = mContactFinder.findContactId(number);
        if (contactId == null) {
            return;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, contactId);
        intent.setData(uri);
        mActivity.startActivity(intent);
    }

    private void createContact(Context context, String number) {
        Intent intent = new Intent(Intent.ACTION_INSERT);
        intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
        intent.putExtra(ContactsContract.Intents.Insert.PHONE, number);
        context.startActivity(intent);
    }
}
