package fm.mrc.gymlog;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.util.List;
import fm.mrc.gymlog.data.AppDatabase;
import fm.mrc.gymlog.data.MuscleStats;

public class MuscleStatsActivity extends BaseActivity {

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

        LinearLayout container = findViewById(R.id.stats_container);

        new Thread(() -> {
            // Last 7 days
            long startTime = System.currentTimeMillis() - (7L * 24 * 3600 * 1000);
            AppDatabase db = AppDatabase.getInstance(this);
            List<MuscleStats> stats = db.logEntryDao().getWeeklyMuscleStats(startTime);

            runOnUiThread(() -> {
                if (stats == null || stats.isEmpty()) {
                    TextView emptyView = new TextView(this);
                    emptyView.setText(getString(R.string.stats_empty));
                    emptyView.setPadding(0, 32, 0, 0);
                    container.addView(emptyView);
                    return;
                }

                // Find max sets for scaling progress bars
                int maxSets = 0;
                for (MuscleStats s : stats) {
                     if (s.totalSets > maxSets) maxSets = s.totalSets;
                }

                for (MuscleStats s : stats) {
                    // Label
                    TextView label = new TextView(this);
                    String muscleName = translateMuscleGroup(s.muscleGroup);
                    label.setText(getString(R.string.stats_sets_format, muscleName, s.totalSets));
                    label.setTextSize(16);
                    label.setPadding(0, 16, 0, 8);
                    container.addView(label);

                    // Progress Bar
                    ProgressBar pb = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
                    pb.setMax(maxSets); // Scale relative to max
                    pb.setProgress(s.totalSets);
                    pb.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, 
                            LinearLayout.LayoutParams.WRAP_CONTENT));
                    // Tint bar with primary color
                    pb.getProgressDrawable().setColorFilter(
                        getThemeColor(androidx.appcompat.R.attr.colorPrimary), 
                        android.graphics.PorterDuff.Mode.SRC_IN);
                    
                    container.addView(pb);
                }
            });
        }).start();
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
