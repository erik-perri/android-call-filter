package com.novyr.callfilter.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.novyr.callfilter.R;
import com.novyr.callfilter.db.entity.LogEntity;
import com.novyr.callfilter.db.entity.WhitelistEntity;
import com.novyr.callfilter.formatter.LogDateFormatter;
import com.novyr.callfilter.formatter.LogMessageFormatter;
import com.novyr.callfilter.managers.PermissionManager;
import com.novyr.callfilter.managers.permission.CallScreeningRoleChecker;
import com.novyr.callfilter.viewmodel.LogViewModel;
import com.novyr.callfilter.viewmodel.WhitelistViewModel;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends AppCompatActivity {
    private RecyclerView mLogList;
    private LogViewModel mLogViewModel;

    private Snackbar mPermissionNotice;
    private PermissionManager mPermissionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_log_viewer);

        PreferenceManager.setDefaultValues(this, R.xml.settings, false);

        mPermissionManager = new PermissionManager();
        mLogList = findViewById(R.id.log_list);

        final WhitelistViewModel whitelistViewModel = ViewModelProviders.of(this).get(WhitelistViewModel.class);
        final LogListAdapter adapter = new LogListAdapter(
                this,
                whitelistViewModel,
                new LogMessageFormatter(getApplicationContext()),
                new LogDateFormatter()
        );

        mLogList.setAdapter(adapter);
        mLogList.setLayoutManager(new LinearLayoutManager(this));

        final TextView emptyView = findViewById(R.id.empty_view);

        mLogViewModel = ViewModelProviders.of(this).get(LogViewModel.class);
        mLogViewModel.findAll().observe(this, new Observer<List<LogEntity>>() {
            @Override
            public void onChanged(@Nullable final List<LogEntity> entities) {
                adapter.setEntities(entities);

                if (adapter.getItemCount() > 0) {
                    mLogList.setVisibility(View.VISIBLE);
                    emptyView.setVisibility(View.GONE);
                } else {
                    mLogList.setVisibility(View.GONE);
                    emptyView.setVisibility(View.VISIBLE);
                }
            }
        });

        whitelistViewModel.findAll().observe(this, new Observer<List<WhitelistEntity>>() {
            @Override
            public void onChanged(@Nullable final List<WhitelistEntity> entities) {
                // TODO Figure out a better way to make the entities available to LogListMenuHandler
                whitelistViewModel.setCurrentEntities(entities);
            }
        });

        registerForContextMenu(mLogList);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (!mPermissionManager.hasAccess(this)) {
            mPermissionManager.requestAccess(this, false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        showPermissionWarning();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        showPermissionWarning();
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CallScreeningRoleChecker.CALL_SCREENING_REQUEST) {
            showPermissionWarning();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_log_viewer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.action_clear_log:
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.dialog_clear_logs_title))
                        .setMessage(getString(R.string.dialog_clear_logs_message))
                        .setIconAttribute(android.R.attr.alertDialogIcon)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                mLogViewModel.clear();
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .show();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showPermissionWarning() {
        if (mPermissionManager.hasAccess(this)) {
            if (mPermissionNotice != null) {
                mPermissionNotice.dismiss();
                mPermissionNotice = null;
            }
            return;
        }

        final Activity self = this;
        mPermissionNotice = Snackbar.make(mLogList, R.string.warning_permissions, Snackbar.LENGTH_INDEFINITE).setAction(R.string.warning_action_retry, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPermissionManager.requestAccess(self, true);
            }
        });
        mPermissionNotice.show();
    }
}
