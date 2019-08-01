package com.novyr.callfilter.ui;

import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.widget.TextView;

import com.novyr.callfilter.R;
import com.novyr.callfilter.db.entity.LogEntity;

import androidx.recyclerview.widget.RecyclerView;

class LogViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
    final TextView mMessageView;
    final TextView mCreatedView;
    private LogEntity mEntity;
    private final LogListMenuHandler mMenuHandler;

    LogViewHolder(View itemView, LogListMenuHandler menuHandler) {
        super(itemView);

        mMenuHandler = menuHandler;
        mMessageView = itemView.findViewById(R.id.message);
        mCreatedView = itemView.findViewById(R.id.created);

        itemView.setOnCreateContextMenuListener(this);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        final LogEntity log = getEntity();
        if (log == null) {
            return;
        }

        int index = 1, order = 0;

        if (log.getNumber() != null) {
            final OnMenuItemClickListener removeFromWhitelistHandler = new OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(final MenuItem menuItem) {
                    mMenuHandler.removeFromWhitelist(log.getNumber());
                    return true;
                }
            };

            if (mMenuHandler.hasContact(log.getNumber())) {
                menu.add(0, index++, order++, R.string.context_menu_open_contacts).setOnMenuItemClickListener(
                        new OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(final MenuItem menuItem) {
                                mMenuHandler.openInContacts(log.getNumber());
                                return true;
                            }
                        });

                if (mMenuHandler.isWhitelisted(log.getNumber())) {
                    menu.add(0, index++, order++, R.string.context_menu_whitelist_remove)
                            .setOnMenuItemClickListener(removeFromWhitelistHandler);
                }
            } else {
                if (mMenuHandler.isWhitelisted(log.getNumber())) {
                    menu.add(0, index++, order++, R.string.context_menu_whitelist_remove).setOnMenuItemClickListener(
                            new OnMenuItemClickListener() {
                                @Override
                                public boolean onMenuItemClick(final MenuItem menuItem) {
                                    mMenuHandler.addToWhitelist(log.getNumber());
                                    return true;
                                }
                            }
                    );
                } else {
                    menu.add(0, index++, order++, R.string.context_menu_whitelist_add)
                            .setOnMenuItemClickListener(removeFromWhitelistHandler);
                }
            }
        }

        menu.add(0, index, order, R.string.context_menu_log_remove).setOnMenuItemClickListener(
                new OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(final MenuItem menuItem) {
                        mMenuHandler.removeLog(log);
                        return true;
                    }
                }
        );
    }

    LogEntity getEntity() {
        return mEntity;
    }

    void setEntity(LogEntity entity) {
        mEntity = entity;
    }
}
