/*
 * MIT License
 *
 * Copyright (c) 2016 - 2017 Luke Myers (FRC Team 980 ThunderBots)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.team980.thunderscout.analytics.rankings;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SearchView;

import com.team980.thunderscout.MainActivity;
import com.team980.thunderscout.R;
import com.team980.thunderscout.backend.AccountScope;
import com.team980.thunderscout.csv.ExportActivity;
import com.team980.thunderscout.csv.ImportActivity;

public class RankingsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, DialogInterface.OnClickListener, SearchView.OnQueryTextListener, View.OnClickListener, SearchView.OnCloseListener {

    private Toolbar toolbar;
    private DrawerLayout drawer;
    private ActionBarDrawerToggle toggle;

    private RecyclerView dataView;
    private RankingsAdapter adapter;
    private SwipeRefreshLayout swipeContainer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_rankings, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MainActivity activity = (MainActivity) getActivity();

        toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle("Rankings");
        activity.setSupportActionBar(toolbar);

        drawer = getActivity().findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                activity, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        setHasOptionsMenu(true);

        dataView = view.findViewById(R.id.dataView);

        // use a linear layout manager
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        dataView.setLayoutManager(mLayoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(dataView.getContext(),
                LinearLayout.VERTICAL);
        dataView.addItemDecoration(dividerItemDecoration);

        // specify an adapter
        adapter = new RankingsAdapter(this);
        dataView.setAdapter(adapter);

        swipeContainer = view.findViewById(R.id.swipeContainer);

        swipeContainer.setOnRefreshListener(this);
        swipeContainer.setEnabled(false);

        swipeContainer.setColorSchemeResources(R.color.accent);
        swipeContainer.setProgressBackgroundColorSchemeResource(R.color.cardview_dark_background);

        AccountScope.getStorageWrapper(AccountScope.LOCAL, getContext()).queryData(adapter);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_rank_tools, menu);

        SearchView searchView = (SearchView) toolbar.getMenu().findItem(R.id.action_search).getActionView();
        searchView.setOnSearchClickListener(this);
        searchView.setOnQueryTextListener(this);
        searchView.setOnCloseListener(this);
        searchView.setQueryHint("Search for team...");
        searchView.setInputType(InputType.TYPE_CLASS_NUMBER);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //Default mode
        if (id == R.id.action_refresh) {
            swipeContainer.setRefreshing(true);
            onRefresh();
        }

        if (id == R.id.action_sort) {
            AlertDialog sortDialog;

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Sort teams by...");

            builder.setSingleChoiceItems(TeamComparator.getFormattedList(), adapter.getCurrentSortMode().ordinal(),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int item) {
                            TeamComparator sortMode = TeamComparator.values()[item];

                            adapter.sort(sortMode);

                            dialog.dismiss();
                        }
                    });

            sortDialog = builder.create();
            sortDialog.show();
        }

        if (id == R.id.action_import) {
            Intent importIntent = new Intent(getContext(), ImportActivity.class);
            startActivity(importIntent);
            return true;
        }

        if (id == R.id.action_export) {
            Intent exportIntent = new Intent(getContext(), ExportActivity.class);
            startActivity(exportIntent);
            return true;
        }

        if (id == R.id.action_delete_all && adapter.getItemCount() > 0) {
            new AlertDialog.Builder(getContext())
                    .setTitle("Delete all data from this account?")
                    .setPositiveButton("Delete", this)
                    .setNegativeButton("Cancel", null).show();
            return true;
        }

        return false;
    }

    @Override
    public void onClick(View view) {
        //if (view.getId() == R.id.action_search) {
        //swipeContainer.setEnabled(false);
        //}
    }

    @Override
    public void onRefresh() { //SwipeRefreshLayout
        AccountScope.getStorageWrapper(AccountScope.LOCAL, getContext()).queryData(adapter);
    }

    public SwipeRefreshLayout getSwipeRefreshLayout() {
        return swipeContainer;
    }

    @Override //Deletion dialog
    public void onClick(DialogInterface dialog, int which) { //TODO modular account scopes - CLOUD should prompt for password
        AccountScope.getStorageWrapper(AccountScope.LOCAL, getContext()).clearAllData(adapter);
    }

    @Override
    public boolean onQueryTextChange(String query) {
        adapter.filterByTeam(query);
        dataView.scrollToPosition(0);
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onClose() { //SearchView
        adapter.filterByTeam("");

        //swipeContainer.setEnabled(true);
        return false;
    }
}
