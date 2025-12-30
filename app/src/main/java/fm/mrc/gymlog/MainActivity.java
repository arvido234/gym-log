package fm.mrc.gymlog;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import fm.mrc.gymlog.data.AppDatabase;
import fm.mrc.gymlog.data.Exercise;
import fm.mrc.gymlog.data.ExerciseDao;
import fm.mrc.gymlog.data.ExerciseWithLastLog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class MainActivity extends AppCompatActivity implements ExerciseAdapter.AdapterCallbacks {

    private ExerciseAdapter adapter;
    private AppDatabase db;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);

        // Datenbank-Initialisierung
        db = AppDatabase.getInstance(this);
        ExerciseDao exerciseDao = db.exerciseDao();

        // RecyclerView-Setup
        recyclerView = findViewById(R.id.recycler_view_exercises);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Adapter-Instanziierung
        adapter = new ExerciseAdapter(this);
        recyclerView.setAdapter(adapter);

        // LiveData Observer: Daten aus der DB laden
        exerciseDao.getAllExercisesWithLastLog().observe(this, exercisesWithLog -> {
            try {
                android.util.Log.d("MainActivity", "Exercises loaded: " + (exercisesWithLog != null ? exercisesWithLog.size() : "null"));
                updateList(exercisesWithLog);
            } catch (Exception e) {
                android.util.Log.e("MainActivity", "Error updating list", e);
            }
        });

        // History Button (falls vorhanden)
        Button historyButton = findViewById(R.id.button_history);
        if (historyButton != null) {
            historyButton.setOnClickListener(v -> {
                // startActivity(new Intent(MainActivity.this, HistoryActivity.class)); // Global history not implemented yet
            });
        }

        // FloatingActionButton für "Neue Übung hinzufügen"
        FloatingActionButton fab = findViewById(R.id.fab_add_exercise);
        if (fab != null) {
            fab.setOnClickListener(v -> {
                startActivity(new Intent(MainActivity.this, NewExerciseActivity.class));
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Load settings and update adapter
        android.content.SharedPreferences prefs = getSharedPreferences(SettingsActivity.PREFS_NAME, MODE_PRIVATE);
        boolean showSubtitle = prefs.getBoolean(SettingsActivity.KEY_SHOW_SUBTITLE, false);
        if (adapter != null) {
            adapter.setShowSubtitle(showSubtitle);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@androidx.annotation.NonNull android.view.MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateList(List<ExerciseWithLastLog> exercises) {
        if (exercises == null) return;

        // Custom sorting for days
        java.util.Map<String, Integer> dayOrder = new java.util.HashMap<>();
        dayOrder.put("Montag", 1);
        dayOrder.put("Dienstag", 2);
        dayOrder.put("Mittwoch", 3);
        dayOrder.put("Donnerstag", 4);
        dayOrder.put("Freitag", 5);
        dayOrder.put("Samstag", 6);
        dayOrder.put("Sonntag", 7);

        // Sort
        java.util.Collections.sort(exercises, (e1, e2) -> {
            String d1 = e1.getExercise().day != null ? e1.getExercise().day : "";
            String d2 = e2.getExercise().day != null ? e2.getExercise().day : "";
            int o1 = dayOrder.getOrDefault(d1, 99);
            int o2 = dayOrder.getOrDefault(d2, 99);
            return Integer.compare(o1, o2);
        });

        // Build list with headers
        java.util.List<Object> items = new java.util.ArrayList<>();
        String currentDay = null;

        for (ExerciseWithLastLog item : exercises) {
            String day = item.getExercise().day;
            if (day == null || day.isEmpty()) day = "Ohne Tag";

            if (!day.equals(currentDay)) {
                items.add(day);
                currentDay = day;
            }
            items.add(item);
        }

        adapter.setItems(items);
    }

    @Override
    public void onDeleteExercise(Exercise exercise, int position) {
        new Thread(() -> db.exerciseDao().delete(exercise)).start();
    }

    @Override
    public void onExerciseClick(Exercise exercise) {
        openHistory(exercise);
    }

    @Override
    public void onHistoryClick(Exercise exercise) {
        openHistory(exercise);
    }

    private void openHistory(Exercise exercise) {
        Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
        intent.putExtra("exerciseId", exercise.exerciseId);
        intent.putExtra("exerciseName", exercise.name);
        startActivity(intent);
    }

    @Override
    public void onQuickLogClick(Exercise exercise) {
        showAddLogDialog(exercise);
    }

    private void showAddLogDialog(Exercise exercise) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Loggen: " + exercise.name);

        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(32, 32, 32, 32);
        
        android.content.SharedPreferences prefs = getSharedPreferences(SettingsActivity.PREFS_NAME, MODE_PRIVATE);
        boolean showSets = prefs.getBoolean(SettingsActivity.KEY_FIELD_SETS, true);
        boolean showReps = prefs.getBoolean(SettingsActivity.KEY_FIELD_REPS, true);
        boolean showWeight = prefs.getBoolean(SettingsActivity.KEY_FIELD_WEIGHT, true);
        boolean showRPE = prefs.getBoolean(SettingsActivity.KEY_FIELD_RPE, true);

        final android.widget.EditText setsInput = new android.widget.EditText(this);
        setsInput.setHint("Sätze");
        setsInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        if (!showSets) setsInput.setVisibility(View.GONE);
        layout.addView(setsInput);

        final android.widget.EditText repsInput = new android.widget.EditText(this);
        repsInput.setHint("Wiederholungen");
        repsInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        if (!showReps) repsInput.setVisibility(View.GONE);
        layout.addView(repsInput);

        final android.widget.EditText weightInput = new android.widget.EditText(this);
        weightInput.setHint("Gewicht (kg)");
        weightInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        if (!showWeight) weightInput.setVisibility(View.GONE);
        layout.addView(weightInput);

        final android.widget.EditText rpeInput = new android.widget.EditText(this);
        rpeInput.setHint("RPE (1-10)");
        rpeInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        if (!showRPE) rpeInput.setVisibility(View.GONE);
        layout.addView(rpeInput);

        builder.setView(layout);

        builder.setPositiveButton("Speichern", (dialog, which) -> {
            try {
                String setsStr = setsInput.getText().toString();
                String repsStr = repsInput.getText().toString();
                String weightStr = weightInput.getText().toString();
                String rpeStr = rpeInput.getText().toString();

                if ((showSets && setsStr.isEmpty()) || 
                    (showReps && repsStr.isEmpty()) || 
                    (showWeight && weightStr.isEmpty())) {
                    android.widget.Toast.makeText(this, "Bitte angezeigte Felder ausfüllen", android.widget.Toast.LENGTH_SHORT).show();
                    return;
                }

                int sets = (showSets && !setsStr.isEmpty()) ? Integer.parseInt(setsStr) : 0;
                int reps = (showReps && !repsStr.isEmpty()) ? Integer.parseInt(repsStr) : 0;
                double weight = (showWeight && !weightStr.isEmpty()) ? Double.parseDouble(weightStr) : 0.0;
                int rpe = (showRPE && !rpeStr.isEmpty()) ? Integer.parseInt(rpeStr) : 0;

                fm.mrc.gymlog.data.LogEntry entry = new fm.mrc.gymlog.data.LogEntry(
                        exercise.exerciseId, System.currentTimeMillis(), sets, reps, weight, rpe);
                
                new Thread(() -> db.logEntryDao().insert(entry)).start();
                 android.widget.Toast.makeText(this, "Gespeichert", android.widget.Toast.LENGTH_SHORT).show();

            } catch (NumberFormatException e) {
                android.widget.Toast.makeText(this, "Ungültige Eingabe", android.widget.Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Abbrechen", (dialog, which) -> dialog.cancel());
        builder.show();
    }
}