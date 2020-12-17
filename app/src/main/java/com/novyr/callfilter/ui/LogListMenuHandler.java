package com.novyr.callfilter.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.ContactsContract;
import android.view.ContextMenu;
import android.view.MenuItem;

import androidx.annotation.NonNull;

import com.novyr.callfilter.ContactFinder;
import com.novyr.callfilter.R;
import com.novyr.callfilter.db.entity.LogEntity;
import com.novyr.callfilter.db.entity.WhitelistEntity;
import com.novyr.callfilter.viewmodel.LogViewModel;
import com.novyr.callfilter.viewmodel.WhitelistViewModel;

import java.util.List;

class LogListMenuHandler {
    private final ContactFinder mContactFinder;
    private final LogViewModel mLogViewModel;
    private final WhitelistViewModel mWhitelistViewModel;
    private final Activity mActivity;
    private List<WhitelistEntity> mWhitelistEntities;

    LogListMenuHandler(
            Activity activity,
            ContactFinder contactFinder,
            LogViewModel logViewModel,
            WhitelistViewModel whitelistViewModel
    ) {
        mActivity = activity;
        mContactFinder = contactFinder;
        mLogViewModel = logViewModel;
        mWhitelistViewModel = whitelistViewModel;
    }

    void setWhitelistEntities(List<WhitelistEntity> entities) {
        mWhitelistEntities = entities;
    }

    void createMenu(final ContextMenu menu, final LogEntity entity) {
        final String number = entity.getNumber();
        final MenuItem.OnMenuItemClickListener listener = new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int itemId = menuItem.getItemId();
                if (itemId == R.id.log_context_contacts_open) {
                    openInContacts(number);
                    return true;
                } else if (itemId == R.id.log_context_whitelist_add) {
                    if (number != null) {
                        addToWhitelist(number);
                    }
                    return true;
                } else if (itemId == R.id.log_context_whitelist_remove) {
                    if (number != null) {
                        removeFromWhitelist(number);
                    }
                    return true;
                } else if (itemId == R.id.log_context_log_remove) {
                    removeLog(entity);
                    return true;
                }
                return false;
            }
        };

        mActivity.getMenuInflater().inflate(R.menu.menu_log_context, menu);

        boolean numberHasContact = hasContact(number);
        boolean canAddToWhitelist = !numberHasContact && number != null && !isWhitelisted(number);
        boolean canRemoveFromWhitelist = number != null && isWhitelisted(number);

        menu.findItem(R.id.log_context_contacts_open)
                .setVisible(numberHasContact)
                .setOnMenuItemClickListener(listener);

        menu.findItem(R.id.log_context_whitelist_add)
                .setVisible(canAddToWhitelist)
                .setOnMenuItemClickListener(listener);

        menu.findItem(R.id.log_context_whitelist_remove)
                .setVisible(canRemoveFromWhitelist)
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

    private boolean isWhitelisted(String number) {
        WhitelistEntity entity = findEntity(number);

        return entity != null;
    }

    private void addToWhitelist(String number) {
        WhitelistEntity entity = findEntity(number);
        if (entity == null) {
            mWhitelistViewModel.insert(new WhitelistEntity(number));
        }
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

    private void removeFromWhitelist(@NonNull String number) {
        WhitelistEntity entity = findEntity(number);
        if (entity != null) {
            mWhitelistViewModel.delete(entity);
        }
    }

    private WhitelistEntity findEntity(String number) {
        if (number == null || mWhitelistEntities == null || mWhitelistEntities.size() == 0) {
            return null;
        }

        for (WhitelistEntity entity : mWhitelistEntities) {
            if (number.equals(entity.getNumber())) {
                return entity;
            }
        }

        return null;
    }
}
