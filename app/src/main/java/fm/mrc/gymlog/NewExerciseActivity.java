package fm.mrc.gymlog;

import android.os.Bundle;

public class NewExerciseActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_exercise);

        android.widget.EditText nameEditText = findViewById(R.id.edit_text_exercise_name);
        android.widget.EditText descriptionEditText = findViewById(R.id.edit_text_exercise_description);
        android.widget.Spinner daySpinner = findViewById(R.id.spinner_day);
        android.widget.Spinner muscleSpinner = findViewById(R.id.spinner_muscle);
        
        // Muscle Group Spinner Setup
        String[] muscleGroups = {
            getString(R.string.muscle_chest), getString(R.string.muscle_back), 
            getString(R.string.muscle_legs), getString(R.string.muscle_shoulders), 
            getString(R.string.muscle_arms), getString(R.string.muscle_core), 
            getString(R.string.muscle_other)
        };
        // DB Keys
        String[] muscleKeys = {"Chest", "Back", "Legs", "Shoulders", "Arms", "Core", "Other"};

        android.widget.ArrayAdapter<String> muscleAdapter = new android.widget.ArrayAdapter<>(this, android.R.layout.simple_spinner_item, muscleGroups);
        muscleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        muscleSpinner.setAdapter(muscleAdapter);
        
        android.widget.Button saveButton = findViewById(R.id.button_save_exercise);

        // Spinner Setup
        String[] days = new String[]{"Montag", "Dienstag", "Mittwoch", "Donnerstag", "Freitag", "Samstag", "Sonntag"};
        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(this, android.R.layout.simple_spinner_item, days);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        daySpinner.setAdapter(adapter);

        // Initialize Text Fields
        android.widget.EditText restInput = findViewById(R.id.edit_text_rest_seconds);
        if (restInput != null) {
            android.content.SharedPreferences prefs = getSharedPreferences(SettingsActivity.PREFS_NAME, MODE_PRIVATE);
            int defSeconds = prefs.getInt(SettingsActivity.KEY_DEFAULT_REST_SECONDS, 90);
            restInput.setText(String.valueOf(defSeconds));
        }

        saveButton.setOnClickListener(v -> {
            String name = nameEditText.getText().toString().trim();
            if (name.isEmpty()) {
                nameEditText.setError("Name darf nicht leer sein");
                return;
            }
            String description = descriptionEditText.getText().toString().trim();
            String selectedDay = (String) daySpinner.getSelectedItem();
            
            int musclePos = muscleSpinner.getSelectedItemPosition();
            String selectedMuscle = (musclePos >= 0 && musclePos < muscleKeys.length) ? muscleKeys[musclePos] : "Other";
            
            // Rest Timer
            android.widget.EditText restInputSave = findViewById(R.id.edit_text_rest_seconds);
            int restSeconds = 90;
            if (restInputSave != null) {
                try {
                    String r = restInputSave.getText().toString().trim();
                    if (!r.isEmpty()) restSeconds = Integer.parseInt(r);
                } catch (Exception e) {}
            }

            saveExercise(name, description, selectedDay, selectedMuscle, restSeconds);
        });
    }

    private void saveExercise(String name, String description, String day, String muscleGroup, int restSeconds) {
        new Thread(() -> {
            try {
                android.util.Log.d("NewExercise", "Starting saveExercise: " + name + " on " + day);
                fm.mrc.gymlog.data.AppDatabase db = fm.mrc.gymlog.data.AppDatabase.getInstance(this);
                fm.mrc.gymlog.data.Exercise newExercise = new fm.mrc.gymlog.data.Exercise(name, description, day);
                newExercise.muscleGroup = muscleGroup; 
                newExercise.restTimerSeconds = restSeconds;
                android.util.Log.d("NewExercise", "Inserting exercise...");
                db.exerciseDao().insert(newExercise);
                android.util.Log.d("NewExercise", "Insert successful.");
                runOnUiThread(this::finish);
            } catch (Exception e) {
                android.util.Log.e("NewExercise", "Error saving exercise", e);
                runOnUiThread(() -> android.widget.Toast.makeText(this, "Fehler: " + e.getMessage(), android.widget.Toast.LENGTH_LONG).show());
            }
        }).start();
    }
}