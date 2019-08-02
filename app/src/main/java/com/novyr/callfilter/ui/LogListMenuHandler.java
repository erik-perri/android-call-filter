package com.novyr.callfilter.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.ContactsContract;
import android.view.ContextMenu;
import android.view.MenuItem;

import com.novyr.callfilter.ContactFinder;
import com.novyr.callfilter.R;
import com.novyr.callfilter.db.entity.LogEntity;
import com.novyr.callfilter.db.entity.WhitelistEntity;
import com.novyr.callfilter.viewmodel.LogViewModel;
import com.novyr.callfilter.viewmodel.WhitelistViewModel;

import java.util.List;

import androidx.annotation.NonNull;

import static android.view.Menu.NONE;

class LogListMenuHandler {
    private final static int MENU_CONTACTS_OPEN = 1;
    private final static int MENU_WHITELIST_ADD = 2;
    private final static int MENU_WHITELIST_REMOVE = 3;
    private final static int MENU_LOG_REMOVE = 4;

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

    void createMenu(final ContextMenu menu, final LogEntity entity) {
        final String number = entity.getNumber();
        final MenuItem.OnMenuItemClickListener listener = new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case MENU_CONTACTS_OPEN:
                        openInContacts(number);
                        return true;
                    case MENU_WHITELIST_ADD:
                        if (number != null) {
                            addToWhitelist(number);
                        }
                        return true;
                    case MENU_WHITELIST_REMOVE:
                        if (number != null) {
                            removeFromWhitelist(number);
                        }
                        return true;
                    case MENU_LOG_REMOVE:
                        removeLog(entity);
                        return true;
                }
                return false;
            }
        };
        int order = 0;

        if (number != null) {
            if (hasContact(number)) {
                menu.add(NONE, MENU_CONTACTS_OPEN, order++, R.string.context_menu_open_contacts)
                        .setOnMenuItemClickListener(listener);

                if (isWhitelisted(number)) {
                    menu.add(NONE, MENU_WHITELIST_REMOVE, order++, R.string.context_menu_whitelist_remove)
                            .setOnMenuItemClickListener(listener);
                }
            } else {
                if (isWhitelisted(number)) {
                    menu.add(NONE, MENU_WHITELIST_REMOVE, order++, R.string.context_menu_whitelist_remove)
                            .setOnMenuItemClickListener(listener);
                } else {
                    menu.add(NONE, MENU_WHITELIST_ADD, order++, R.string.context_menu_whitelist_add)
                            .setOnMenuItemClickListener(listener);
                }
            }
        }

        menu.add(NONE, MENU_LOG_REMOVE, order, R.string.context_menu_log_remove)
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
        mContext.startActivity(intent);
    }

    private void removeFromWhitelist(@NonNull String number) {
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
