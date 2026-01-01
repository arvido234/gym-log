package fm.mrc.gymlog;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Button;
import java.util.List;
import fm.mrc.gymlog.data.AppDatabase;
import fm.mrc.gymlog.data.MuscleStats;

public class MuscleStatsActivity extends BaseActivity {

    private List<MuscleStats> currentStats;
    private boolean showVolume = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_muscle_stats);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar_stats);
        toolbar.setTitle(getString(R.string.title_muscle_stats));
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
        
        android.widget.RadioGroup radioGroup = findViewById(R.id.radio_group_stats_type);
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            showVolume = (checkedId == R.id.radio_volume);
            renderStats();
        });

        new Thread(() -> {
            // Last 7 days
            long startTime = System.currentTimeMillis() - (7L * 24 * 3600 * 1000);
            AppDatabase db = AppDatabase.getInstance(this);
            currentStats = db.logEntryDao().getWeeklyMuscleStats(startTime);

            runOnUiThread(this::renderStats);
        }).start();
    }
    
    private void renderStats() {
        LinearLayout container = findViewById(R.id.stats_container);
        container.removeAllViews();
        
        if (currentStats == null || currentStats.isEmpty()) {
            LinearLayout emptyLayout = new LinearLayout(this);
            emptyLayout.setOrientation(LinearLayout.VERTICAL);
            emptyLayout.setGravity(android.view.Gravity.CENTER);
            emptyLayout.setPadding(32, 64, 32, 32);

            TextView title = new TextView(this);
            title.setText(getString(R.string.stats_empty));
            title.setTextSize(18);
            title.setTypeface(null, android.graphics.Typeface.BOLD);
            title.setGravity(android.view.Gravity.CENTER);
            title.setTextColor(getThemeColor(androidx.appcompat.R.attr.colorPrimary));
            
            TextView motivation = new TextView(this);
            motivation.setText(getString(R.string.stats_empty_motivation));
            motivation.setGravity(android.view.Gravity.CENTER);
            motivation.setPadding(0, 16, 0, 32);

            Button btnStart = new Button(this);
            btnStart.setText(getString(R.string.btn_start_training));
            btnStart.setOnClickListener(v -> finish()); // Go back to Main to log

            emptyLayout.addView(title);
            emptyLayout.addView(motivation);
            emptyLayout.addView(btnStart);
            
            container.addView(emptyLayout);
            return;
        }

        double maxVal = 0;
        for (MuscleStats s : currentStats) {
             double val = showVolume ? s.totalVolume : s.totalSets;
             if (val > maxVal) maxVal = val;
        }
        
        // Sorting? The query sorts by Sets already. 
        // Ideally we re-sort if showing volume, but preserving original order (by sets) might be acceptable or we sort here.
        // Let's sort manually if showing volume, otherwise use query order.
        List<MuscleStats> displayList = new java.util.ArrayList<>(currentStats);
        if (showVolume) {
            java.util.Collections.sort(displayList, (a, b) -> Double.compare(b.totalVolume, a.totalVolume));
        }

        for (MuscleStats s : displayList) {
            double val = showVolume ? s.totalVolume : s.totalSets;
            
            // Label
            TextView label = new TextView(this);
            String muscleName = translateMuscleGroup(s.muscleGroup);
            
            if (showVolume) {
                label.setText(getString(R.string.stats_volume_format, muscleName, val));
            } else {
                label.setText(getString(R.string.stats_sets_format, muscleName, (int)val));
            }
            
            label.setTextSize(16);
            label.setPadding(0, 16, 0, 8);
            container.addView(label);

            // Progress Bar
            ProgressBar pb = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
            pb.setMax(100); 
            pb.setProgress(maxVal > 0 ? (int)((val / maxVal) * 100) : 0);
            
            pb.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            // Tint bar with primary color
            pb.getProgressDrawable().setColorFilter(
                getThemeColor(androidx.appcompat.R.attr.colorPrimary), 
                android.graphics.PorterDuff.Mode.SRC_IN);
            
            container.addView(pb);
        }
    }
    
    // Helper to get color from attr
    private int getThemeColor(int attr) {
        android.util.TypedValue typedValue = new android.util.TypedValue();
        getTheme().resolveAttribute(attr, typedValue, true);
        return typedValue.data;
    }

    // Helper to map DB Keys back to localized strings (since we stored keys like "Chest", "Back")
    private String translateMuscleGroup(String key) {
        if (key == null) return getString(R.string.muscle_other);
        switch (key) {
            case "Chest": return getString(R.string.muscle_chest);
            case "Back": return getString(R.string.muscle_back);
            case "Legs": return getString(R.string.muscle_legs);
            case "Shoulders": return getString(R.string.muscle_shoulders);
            case "Arms": return getString(R.string.muscle_arms);
            case "Core": return getString(R.string.muscle_core);
            case "Other": return getString(R.string.muscle_other);
            default: return key;
        }
    }
}
