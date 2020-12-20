package com.novyr.callfilter.ui.rulelist;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.novyr.callfilter.R;
import com.novyr.callfilter.viewmodel.RuleViewModel;

public class RuleListActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_rule_list);

        final RecyclerView ruleList = findViewById(R.id.rule_list);
        final TextView emptyView = findViewById(R.id.empty_view);

        final RuleViewModel ruleViewModel = new ViewModelProvider(this).get(RuleViewModel.class);
        final RuleListViewModel ruleListViewModel = new RuleListViewModel(this, ruleViewModel);

        final RuleListAdapter adapter = new RuleListAdapter(ruleListViewModel);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new RuleListAdapterTouchHelper(adapter));
        itemTouchHelper.attachToRecyclerView(ruleList);

        final RuleViewHolderFactory holderFactory = new RuleViewHolderFactory(
                this,
                ruleListViewModel,
                itemTouchHelper::startDrag
        );

        // TODO Figure out a better way to handle this.  It is not attached to the constructor since
        //      it needs to access itemTouchHolder which did not exist yet. This whole class
        //      structure is a mess that needs to be cleaned up.
        adapter.setViewHolderFactory(holderFactory);

        ruleList.setAdapter(adapter);
        ruleList.setLayoutManager(new LinearLayoutManager(this));

        FloatingActionButton fab = findViewById(R.id.add_button);
        fab.setOnClickListener(view -> ruleListViewModel.showEditDialog());

        ruleViewModel.highestOrder().observe(
                this,
                order -> ruleListViewModel.setNextOrder(order + 2)
        );

        ruleViewModel.findAll().observe(this, entities -> {
            adapter.setEntities(entities);

            if (adapter.getItemCount() > 0) {
                ruleList.setVisibility(View.VISIBLE);
                emptyView.setVisibility(View.GONE);
            } else {
                ruleList.setVisibility(View.GONE);
                emptyView.setVisibility(View.VISIBLE);
            }
        });
    }
}
