/*
 * MIT License
 *
 * Copyright (c) 2016 - 2018 Luke Myers (FRC Team 980 ThunderBots)
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

package com.team980.thunderscout.analytics.matches.breakdown;

import android.app.ActivityManager;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import com.team980.thunderscout.R;
import com.team980.thunderscout.analytics.matches.MatchPointEstimator;
import com.team980.thunderscout.schema.ScoutData;
import com.team980.thunderscout.schema.enumeration.FuelDumpAmount;

import java.text.SimpleDateFormat;

public class MatchInfoActivity extends AppCompatActivity {

    public static final String EXTRA_SCOUT_DATA = "com.team980.thunderscout.SCOUT_DATA";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_match);

        Intent launchIntent = getIntent();
        ScoutData data = (ScoutData) launchIntent.getSerializableExtra(EXTRA_SCOUT_DATA);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Match Info: Team " + data.getTeam());
        getSupportActionBar().setSubtitle("Qualification Match " + data.getMatchNumber());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
            ActivityManager.TaskDescription current = activityManager.getAppTasks().get(0).getTaskInfo().taskDescription;
            ActivityManager.TaskDescription taskDesc = new ActivityManager.TaskDescription("Match Info: Team " + data.getTeam(),
                    current.getIcon(), getResources().getColor(data.getAllianceStation().getColor().getColorPrimary()));
            setTaskDescription(taskDesc);
        }

        toolbar.setBackground(new ColorDrawable(getResources().getColor(data.getAllianceStation().getColor().getColorPrimary())));
        findViewById(R.id.app_bar_layout).setBackground(new ColorDrawable(getResources().getColor(data.getAllianceStation().getColor().getColorPrimary())));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(data.getAllianceStation().getColor().getColorPrimaryDark()));
        }

        // Init
        TextView date = findViewById(R.id.info_match_date);
        date.setText(SimpleDateFormat.getDateTimeInstance().format(data.getDate()));

        TextView source = findViewById(R.id.info_match_source);
        source.setText(data.getSource());

        // Auto
        TextView mobility = findViewById(R.id.info_match_autoMobilityPoints);
        mobility.setText(MatchPointEstimator.getBaselinePoints(data) + " pts");

        TextView autoLowGoalCount = findViewById(R.id.info_match_autoLowGoalCount);
        autoLowGoalCount.setText((data.getAutonomous().getLowGoalDumpAmount().getMinimumAmount()
                + data.getAutonomous().getLowGoalDumpAmount().getMaximumAmount()) / 2 + "");

        TextView autoHighGoalCount = findViewById(R.id.info_match_autoHighGoalCount);
        autoHighGoalCount.setText(data.getAutonomous().getHighGoals() + "");

        TextView autoMissedGoalCount = findViewById(R.id.info_match_autoMissedGoalCount);
        autoMissedGoalCount.setText(data.getAutonomous().getMissedHighGoals() + "");

        TextView autoFuelPoints = findViewById(R.id.info_match_autoFuelPoints);
        autoFuelPoints.setText(MatchPointEstimator.getAutoFuelPoints(data) + " pts");

        TextView autoGearDeliveryCount = findViewById(R.id.info_match_autoGearDeliveryCount);
        autoGearDeliveryCount.setText(data.getAutonomous().getGearsDelivered() + "");

        TextView autoGearDropCount = findViewById(R.id.info_match_autoGearDropCount);
        autoGearDropCount.setText(data.getAutonomous().getGearsDropped() + "");

        TextView autoRotorPoints = findViewById(R.id.info_match_autoRotorPoints);
        autoRotorPoints.setText(MatchPointEstimator.getAutoRotorPoints(data) + " pts");

        // Teleop
        TextView teleopLowGoalCount = findViewById(R.id.info_match_teleopLowGoalCount);
        int low = 0; //ugh I hate FuelDumpAmount soooo much
        for (FuelDumpAmount dumpAmount : data.getTeleop().getLowGoalDumps()) {
            low += ((dumpAmount.getMinimumAmount() + dumpAmount.getMaximumAmount()) / 2);
        }
        teleopLowGoalCount.setText(low + "");

        TextView teleopHighGoalCount = findViewById(R.id.info_match_teleopHighGoalCount);
        teleopHighGoalCount.setText(data.getTeleop().getHighGoals() + "");

        TextView teleopMissedGoalCount = findViewById(R.id.info_match_teleopMissedGoalCount);
        teleopMissedGoalCount.setText(data.getTeleop().getMissedHighGoals() + "");

        TextView teleopFuelPoints = findViewById(R.id.info_match_teleopFuelPoints);
        teleopFuelPoints.setText(MatchPointEstimator.getTeleopFuelPoints(data) + " pts");

        TextView teleopGearDeliveryCount = findViewById(R.id.info_match_teleopGearDeliveryCount);
        teleopGearDeliveryCount.setText(data.getTeleop().getGearsDelivered() + "");

        TextView teleopGearDropCount = findViewById(R.id.info_match_teleopGearDropCount);
        teleopGearDropCount.setText(data.getTeleop().getGearsDropped() + "");

        TextView teleopRotorPoints = findViewById(R.id.info_match_teleopRotorPoints);
        teleopRotorPoints.setText(MatchPointEstimator.getTeleopRotorPoints(data) + " pts");

        TextView climb = findViewById(R.id.info_match_teleopClimbPoints);
        climb.setText(MatchPointEstimator.getClimbingPoints(data) + " pts");

        // Summary
        TextView rankingPoints = findViewById(R.id.info_match_rankingPoints);
        rankingPoints.setText(MatchPointEstimator.getRankingPoints(data) + " pts");

        TextView total = findViewById(R.id.info_match_totalPoints);
        total.setText(MatchPointEstimator.getPointContribution(data) + " pts");

        TextView troubleWith = findViewById(R.id.info_match_troubleWith);
        if (data.getTroubleWith() != null && !data.getTroubleWith().isEmpty()) {
            troubleWith.setText(data.getTroubleWith());
        } else {
            troubleWith.setText("N/A");
        }

        TextView comments = findViewById(R.id.info_match_comments);
        if (data.getComments() != null && !data.getComments().isEmpty()) {
            comments.setText(data.getComments());
        } else {
            comments.setText("N/A");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            supportFinishAfterTransition();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
