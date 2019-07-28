package com.novyr.callfilter.ui;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.provider.ContactsContract;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.novyr.callfilter.ContactHelper;
import com.novyr.callfilter.R;
import com.novyr.callfilter.db.entity.LogEntity;
import com.novyr.callfilter.db.entity.WhitelistEntity;
import com.novyr.callfilter.viewmodel.LogViewModel;

import java.util.List;

import androidx.annotation.NonNull;

class LogListMenuHandler implements View.OnCreateContextMenuListener, MenuItem.OnMenuItemClickListener {
    private final ContactHelper mContactHelper;
    private final Context mContext;
    private final LogViewModel mLogViewModel;
    private final LogViewHolder mHolder;

    LogListMenuHandler(Context context, LogViewHolder holder) {
        mContext = context;
        mContactHelper = new ContactHelper(context);
        mHolder = holder;
        mLogViewModel = new LogViewModel((Application) context.getApplicationContext());
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        LogEntity log = mHolder.getEntity();
        if (log == null) {
            return;
        }

        int index = 1, order = 0;

        if (log.getNumber() != null) {
            String contactName = null;
            try {
                contactName = mContactHelper.findContactName(log.getNumber());
            } catch (Exception ignored) {
            }

            if (contactName != null) {
                menu.add(0, index++, order++, R.string.context_menu_open_contacts).setOnMenuItemClickListener(this);

                if (isWhitelisted(log.getNumber())) {
                    menu.add(0, index++, order++, R.string.context_menu_whitelist_remove).setOnMenuItemClickListener(this);
                }
            } else {
                if (isWhitelisted(log.getNumber())) {
                    menu.add(0, index++, order++, R.string.context_menu_whitelist_remove).setOnMenuItemClickListener(this);
                } else {
                    menu.add(0, index++, order++, R.string.context_menu_whitelist_add).setOnMenuItemClickListener(this);
                }
            }
        }

        menu.add(0, index, order, R.string.context_menu_log_remove).setOnMenuItemClickListener(this);
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        LogEntity log = mHolder.getEntity();
        if (log == null) {
            return false;
        }

        String contextItemSelected = menuItem.getTitle().toString();
        String toastText = null;

        Resources resources = mContext.getResources();

        // Open in contacts
        if (contextItemSelected.equals(resources.getString(R.string.context_menu_open_contacts))) {
            try {
                String contactId = mContactHelper.findContactId(log.getNumber());
                if (contactId == null) {
                    toastText = "Failed to find contact";
                } else {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, contactId);
                    intent.setData(uri);
                    mContext.startActivity(intent);
                }
            } catch (Exception ignored) {
                toastText = "Error while trying to find contact";
            }
        }

        // Remove from log
        else if (contextItemSelected.equals(resources.getString(R.string.context_menu_log_remove))) {
            mLogViewModel.delete(log);
        }

        // Add to whitelist
        else if (contextItemSelected.equals(resources.getString(R.string.context_menu_whitelist_add))) {
            if (log.getNumber() != null) {
                addToWhitelist(log.getNumber());
            }
        }

        // Remove from whitelist
        else if (contextItemSelected.equals(resources.getString(R.string.context_menu_whitelist_remove))) {
            if (log.getNumber() != null) {
                removeFromWhitelist(log.getNumber());
            }
        }

        // Unhandled
        else {
            return false;
        }

        if (toastText != null) {
            Toast.makeText(mContext, toastText, Toast.LENGTH_SHORT).show();
        }

        return true;
    }

    private boolean isWhitelisted(String number) {
        WhitelistEntity entity = findEntity(number);

        return entity != null;
    }

    private void addToWhitelist(@NonNull String number) {
        WhitelistEntity entity = findEntity(number);
        if (entity == null) {
            mHolder.getWhitelistViewModel().insert(new WhitelistEntity(number));
        }
    }

    private void removeFromWhitelist(@NonNull String number) {
        WhitelistEntity entity = findEntity(number);
        if (entity != null) {
            mHolder.getWhitelistViewModel().delete(entity);
        }
    }

    private WhitelistEntity findEntity(String number) {
        if (number == null) {
            return null;
        }

        if (mHolder.getWhitelistViewModel() == null) {
            return null;
        }

        List<WhitelistEntity> list = mHolder.getWhitelistViewModel().getCurrentEntities();
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
