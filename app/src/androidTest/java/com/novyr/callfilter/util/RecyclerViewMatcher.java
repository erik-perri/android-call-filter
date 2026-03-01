package com.novyr.callfilter.util;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.matcher.BoundedMatcher;

import org.hamcrest.Description;
import org.hamcrest.Matcher;

/**
 * Matches a view within a RecyclerView item at a given position.
 *
 * Usage: onView(withRecyclerView(R.id.rule_list).atPositionOnView(0, R.id.rule_action_allow))
 */
public class RecyclerViewMatcher {
    private final int recyclerViewId;

    public RecyclerViewMatcher(int recyclerViewId) {
        this.recyclerViewId = recyclerViewId;
    }

    public static RecyclerViewMatcher withRecyclerView(int recyclerViewId) {
        return new RecyclerViewMatcher(recyclerViewId);
    }

    public Matcher<View> atPositionOnView(final int position, final int targetViewId) {
        return new BoundedMatcher<View, View>(View.class) {
            @Override
            public void describeTo(Description description) {
                description.appendText("has item at position " + position
                        + " with view id " + targetViewId);
            }

            @Override
            protected boolean matchesSafely(View view) {
                View rootView = view.getRootView();
                RecyclerView recyclerView = rootView.findViewById(recyclerViewId);
                if (recyclerView == null) {
                    return false;
                }

                RecyclerView.ViewHolder holder =
                        recyclerView.findViewHolderForAdapterPosition(position);
                if (holder == null) {
                    return false;
                }

                View targetView = holder.itemView.findViewById(targetViewId);
                return view == targetView;
            }
        };
    }
}
