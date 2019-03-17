package com.novyr.callfilter.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.Toast;

import com.novyr.callfilter.CallFilterApplication;
import com.novyr.callfilter.CallReceiver;
import com.novyr.callfilter.R;
import com.novyr.callfilter.adapters.LogEntryAdapter;
import com.novyr.callfilter.managers.PermissionManager;
import com.novyr.callfilter.models.Contact;
import com.novyr.callfilter.models.LogEntry;
import com.novyr.callfilter.models.WhitelistEntry;

public class LogViewerActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener, AbsListView.OnScrollListener {
    private static final String TAG = LogViewerActivity.class.getName();
    public static final String BROADCAST_REFRESH = "com.novyr.callfilter.refresh";

    private SwipeRefreshLayout mRefreshLayout;
    private Snackbar mPermissionNotice;
    private ListView mLogList;
    private BroadcastReceiver mRefreshLogViewReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent bufferIntent) {
            refreshFromDatabase();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_viewer);

        PreferenceManager.setDefaultValues(this, R.xml.settings, false);

        mRefreshLayout = findViewById(R.id.refresh_layout);
        mRefreshLayout.setOnRefreshListener(this);

        mLogList = findViewById(R.id.log_list);
        mLogList.setEmptyView(findViewById(R.id.empty));
        mLogList.setOnScrollListener(this);

        registerForContextMenu(mLogList);
        handlePermissionCheck();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    protected void onResume() {
        super.onResume();

        refreshFromDatabase();
        showPermissionWarning();
        registerReceiver(mRefreshLogViewReceiver, new IntentFilter(BROADCAST_REFRESH));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (mPermissionNotice != null) {
            PermissionManager manager = new PermissionManager(this);
            if (manager.hasRequiredPermissions()) {
                mPermissionNotice.dismiss();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(mRefreshLogViewReceiver);
    }

    @Override
    public void onRefresh() {
        refreshFromDatabase();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_log_viewer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.action_refresh:
                if (mRefreshLayout != null) {
                    mRefreshLayout.setRefreshing(true);
                }
                refreshFromDatabase();
                return true;
            case R.id.action_clear_log:
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.dialog_clear_logs_title))
                        .setMessage(getString(R.string.dialog_clear_logs_message))
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                LogEntry.deleteAll(LogEntry.class);
                                if (mRefreshLayout != null) {
                                    mRefreshLayout.setRefreshing(true);
                                }
                                refreshFromDatabase();
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .show();
                return true;
            /*case R.id.action_check_permissions:
                handlePermissionCheck();
                showPermissionWarning();
                return true;*/
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        int topRowVerticalPosition = (mLogList == null || mLogList.getChildCount() == 0) ? 0 : mLogList.getChildAt(0).getTop();
        mRefreshLayout.setEnabled(firstVisibleItem == 0 && topRowVerticalPosition >= 0);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        ListView.AdapterContextMenuInfo info = (ListView.AdapterContextMenuInfo) menuInfo;
        LogEntryAdapter adapter = (LogEntryAdapter) mLogList.getAdapter();

        if (info == null || adapter == null) {
            return;
        }

        LogEntry entry = adapter.getItem(info.position);

        int index = 1, order = 0;

        if (entry != null && entry.number != null) {
            if (CallFilterApplication.isNumberInContacts(this.getApplicationContext(), entry.number)) {
                menu.add(0, index++, order++, R.string.context_menu_open_contacts);

                if (CallFilterApplication.isNumberInWhitelist(entry.number)) {
                    menu.add(0, index++, order++, R.string.context_menu_whitelist_remove);
                }
            } else {
                if (CallFilterApplication.isNumberInWhitelist(entry.number)) {
                    menu.add(0, index++, order++, R.string.context_menu_whitelist_remove);
                } else {
                    menu.add(0, index++, order++, R.string.context_menu_whitelist_add);
                }
            }
        }

        //noinspection UnusedAssignment
        menu.add(0, index++, order++, R.string.context_menu_log_remove);

        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        super.onContextItemSelected(item);

        String contextItemSelected = item.getTitle().toString();
        ListView.AdapterContextMenuInfo info = (ListView.AdapterContextMenuInfo) item.getMenuInfo();
        LogEntryAdapter adapter = (LogEntryAdapter) mLogList.getAdapter();
        LogEntry logEntry = adapter.getItem(info.position);
        if (logEntry == null) {
            return false;
        }

        String message = "";
        String formattedNumber = CallFilterApplication.formatNumber(this, logEntry.number);

        if (contextItemSelected.equals(getString(R.string.context_menu_open_contacts))) {
            Contact contact = CallFilterApplication.getContactInfo(this, logEntry.number);
            if (contact == null) {
                message = "Failed to find contact";
            } else {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, contact.id);
                intent.setData(uri);
                startActivity(intent);
            }
        } else if (contextItemSelected.equals(getString(R.string.context_menu_whitelist_remove))) {
            message = String.format("Failed to remove %s from whitelist", formattedNumber);
            if (WhitelistEntry.deleteAll(WhitelistEntry.class, "number = ?", logEntry.number) > 0) {
                message = String.format("Removed %s from whitelist", formattedNumber);
            }
        } else if (contextItemSelected.equals(getString(R.string.context_menu_whitelist_add))) {
            WhitelistEntry whitelistEntry = new WhitelistEntry(logEntry.number);
            if (whitelistEntry.save() > 0) {
                message = String.format("Added %s to whitelist", formattedNumber);
            }
        } else if (contextItemSelected.equals(getString(R.string.context_menu_log_remove))) {
            if (logEntry.delete()) {
                message = "Deleted log entry";
            } else {
                message = "Failed to delete log entry";
            }
            refreshFromDatabase();
        }

        if (message.length() > 0) {
            Toast.makeText(LogViewerActivity.this, message, Toast.LENGTH_SHORT).show();
        }

        return true;
    }

    private void handlePermissionCheck() {
        final PermissionManager manager = new PermissionManager(this);

        if (manager.shouldRequestPermissions()) {
            manager.requestPermissions();
        }
    }

    private void refreshFromDatabase() {
        ListView list = findViewById(R.id.log_list);
        list.setAdapter(new LogEntryAdapter(this));
        if (mRefreshLayout != null) {
            // Delay so it is obvious the list was actually refreshed (sometimes it is too fast on my phone)
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    mRefreshLayout.setRefreshing(false);
                }
            }, 500);
        }
    }

    private void showPermissionWarning() {
        final PermissionManager manager = new PermissionManager(this);

        if (!manager.hasRequiredPermissions()) {
            View parentLayout = findViewById(R.id.log_list);
            mPermissionNotice = Snackbar.make(parentLayout, R.string.warning_permissions, Snackbar.LENGTH_INDEFINITE).setAction(R.string.warning_action_retry, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (manager.shouldRequestPermissions(true)) {
                        manager.requestPermissions();
                    }
                }
            });
            mPermissionNotice.show();
        }
    }
}
