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

package com.team980.thunderscout.home;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.team980.thunderscout.BuildConfig;
import com.team980.thunderscout.MainActivity;
import com.team980.thunderscout.R;
import com.team980.thunderscout.backend.AccountScope;
import com.team980.thunderscout.backend.StorageWrapper;
import com.team980.thunderscout.schema.ScoutData;
import com.team980.thunderscout.schema.enumeration.AllianceStation;
import com.team980.thunderscout.scouting_flow.ScoutingFlowActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class HomeFragment extends Fragment implements View.OnClickListener, SharedPreferences.OnSharedPreferenceChangeListener, View.OnLongClickListener {

    private FloatingActionButton scoutButton;

    private TaskUpdateReceiver receiver;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);

        //TODO implement ongoing tasks cards IN view XML
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MainActivity activity = (MainActivity) getActivity();

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle("ThunderScout");
        activity.setSupportActionBar(toolbar);

        setHasOptionsMenu(true);

        DrawerLayout drawer = getActivity().findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                activity, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        scoutButton = view.findViewById(R.id.fab_scout);
        scoutButton.setOnClickListener(this);
        if (BuildConfig.DEBUG) {
            scoutButton.setOnLongClickListener(this);
        }

        Boolean matchScout = PreferenceManager.getDefaultSharedPreferences(getContext())
                .getBoolean(getResources().getString(R.string.pref_enable_match_scouting), true);

        if (matchScout) {
            scoutButton.setVisibility(View.VISIBLE);
        } else {
            scoutButton.setVisibility(View.GONE);
        }

        //receiver = new TaskUpdateReceiver();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        PreferenceManager.getDefaultSharedPreferences(getContext()).registerOnSharedPreferenceChangeListener(this);

        //context.registerReceiver(receiver, new IntentFilter(TaskUpdateReceiver.ACTION_UPDATE_ONGOING_TASK));
    }

    @Override
    public void onDetach() {
        super.onDetach();

        PreferenceManager.getDefaultSharedPreferences(getContext()).unregisterOnSharedPreferenceChangeListener(this);

        //getContext().unregisterReceiver(receiver);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.fab_scout) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

            if (!prefs.getBoolean(getResources().getString(R.string.pref_enable_match_scouting), true)) {
                return;
            }

            Intent scoutIntent = new Intent(getContext(), ScoutingFlowActivity.class);
            startActivity(scoutIntent);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_home, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getResources().getString(R.string.pref_enable_match_scouting))) {
            Boolean matchScout = sharedPreferences.getBoolean(getResources().getString(R.string.pref_enable_match_scouting), false);

            if (matchScout) {
                scoutButton.setVisibility(View.VISIBLE);
            } else {
                scoutButton.setVisibility(View.GONE);
            }
        }
    }

    public boolean onLongClick(View view) {
        if (!BuildConfig.DEBUG) {
            return true;
        }

        ArrayList<ScoutData> debug = new ArrayList<>();
        Random random = new Random();
        for (int i = 1; i < 101; i++) {
            for (int j = 0; j < 6; j++) {
                ScoutData data = new ScoutData();
                data.setTeam(String.valueOf(random.nextInt(70)));
                data.setMatchNumber(i);
                data.setSource(Build.MANUFACTURER + " " + Build.MODEL);

                data.setAllianceStation(AllianceStation.values()[j]);

                debug.add(data);
            }
        }
        AccountScope.getStorageWrapper(AccountScope.LOCAL, getContext()).writeData(debug, new StorageWrapper.StorageListener() {
            @Override
            public void onDataWrite(@Nullable List<ScoutData> dataWritten) {
                Toast.makeText(getContext(), "Written " + dataWritten.size(), Toast.LENGTH_SHORT).show();
            }
        });
        Toast.makeText(getContext(), "Started " + debug.size(), Toast.LENGTH_SHORT).show();
        return true;
    }

    public static class TaskUpdateReceiver extends BroadcastReceiver {

        public static final String ACTION_UPDATE_ONGOING_TASK = "com.team980.thunderscout.ACTION_UPDATE_ONGOING_TASK";
        public static final String KEY_TASK_ID = "task_id";
        public static final String KEY_UPDATE_TYPE = "update_type";


        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() == ACTION_UPDATE_ONGOING_TASK) {
                UpdateType updateType = (UpdateType) intent.getSerializableExtra(KEY_UPDATE_TYPE);

                Log.d("Receiver", "Task is" + updateType.name());
            }
        }


        public enum UpdateType {
            STARTING,
            IN_PROGRESS,
            FINISHED,
            ERROR
        }

    }

}
