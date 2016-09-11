package com.team980.thunderscout;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.team980.thunderscout.data.TeamWrapper;
import com.team980.thunderscout.data.task.DatabaseClearTask;
import com.team980.thunderscout.data.task.DatabaseReadTask;
import com.team980.thunderscout.info.DataViewAdapter;
import com.team980.thunderscout.match.ScoutingFlowActivity;
import com.team980.thunderscout.preferences.SettingsActivity;

import java.util.ArrayList;

import static com.team980.thunderscout.data.TeamWrapper.TeamComparator.SORT_TEAM_NUMBER;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener, DialogInterface.OnClickListener,
        PopupMenu.OnMenuItemClickListener, SearchView.OnQueryTextListener {

    private RecyclerView dataView;
    private DataViewAdapter adapter;

    private SwipeRefreshLayout swipeContainer;

    private BroadcastReceiver refreshReceiver;

    public static final String ACTION_REFRESH_VIEW_PAGER = "com.team980.thunderscout.REFRESH_VIEW_PAGER";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("ThunderScout");

        dataView = (RecyclerView) findViewById(R.id.dataView);

        // use a linear layout manager
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        dataView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        adapter = new DataViewAdapter(this, new ArrayList<TeamWrapper>());
        dataView.setAdapter(adapter);

        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);

        swipeContainer.setOnRefreshListener(this);

        swipeContainer.setColorSchemeResources(R.color.primary);
        swipeContainer.setProgressBackgroundColorSchemeResource(R.color.cardview_dark_background);

        DatabaseReadTask query = new DatabaseReadTask(adapter, this, swipeContainer);
        query.execute();

        refreshReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                DatabaseReadTask query = new DatabaseReadTask(adapter, MainActivity.this, swipeContainer);
                query.execute();
            }
        };
    }

    @Override
    protected void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(refreshReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();

        LocalBroadcastManager.getInstance(this).registerReceiver(refreshReceiver, new IntentFilter(ACTION_REFRESH_VIEW_PAGER));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        final MenuItem item = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(this);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_delete && adapter.getItemCount() > 0) {
            new AlertDialog.Builder(this)
                    .setTitle("Are you sure?")
                    .setMessage("This will delete all scout data in your local database and the data cannot be recovered!")
                    .setIcon(R.drawable.ic_warning_white_24dp)
                    .setPositiveButton(android.R.string.yes, this)
                    .setNegativeButton(android.R.string.no, null).show();
        }

        if (id == R.id.action_sort) {
            PopupMenu popup = new PopupMenu(this, findViewById(R.id.action_sort));
            popup.setOnMenuItemClickListener(this);

            MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(R.menu.sort_modes, popup.getMenu());
            popup.show();
        }

        if (id == R.id.action_filter) {
            Toast.makeText(this, "Coming in a future update!", Toast.LENGTH_SHORT).show();
        }

        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) { //TODO this doesn't do what's advertised
        super.onSaveInstanceState(savedInstanceState);
        adapter.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        adapter.onRestoreInstanceState(savedInstanceState);
    }

    /**
     * Called when the scout button is pressed.
     */
    public void onButtonPressed(View v) {
        if (v.getId() == R.id.button_scout) {
            Intent scoutIntent = new Intent(this, ScoutingFlowActivity.class);
            startActivity(scoutIntent);
        }
    }

    /**
     * SwipeRefreshLayout
     */
    @Override
    public void onRefresh() {
        DatabaseReadTask query = new DatabaseReadTask(adapter, this, swipeContainer);
        query.execute();
    }

    /**
     * Alert dialog shown for deletion prompt
     */
    @Override
    public void onClick(DialogInterface dialog, int whichButton) {
        DatabaseClearTask clearTask = new DatabaseClearTask(adapter, this);
        clearTask.execute();
    }

    /**
     * Popup menu for sorting mode selection
     */
    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sort_team_number:
                adapter.sort(SORT_TEAM_NUMBER);
                return true;
            default:
                return false;
        }
    }

    /**
     * SearchView
     */
    @Override
    public boolean onQueryTextChange(String query) {
        return false;
    }

    /**
     * SearchView
     */
    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }
}
