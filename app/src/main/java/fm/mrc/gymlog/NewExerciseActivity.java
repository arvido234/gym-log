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
        
        // Muscle Group Spinner
        android.widget.Spinner muscleSpinner = new android.widget.Spinner(this);
        // Create layout params to insert it into the layout
        android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 16, 0, 16);
        muscleSpinner.setLayoutParams(params);
        
        // Find the container to add the spinner (Assuming LinearLayout is the root)
        android.view.ViewGroup container = (android.view.ViewGroup) ((android.view.ViewGroup) findViewById(android.R.id.content)).getChildAt(0);
        // Insert before save button (assuming it's the last child, index count-1)
        container.addView(muscleSpinner, container.getChildCount() - 1);
        
        // Add label
        android.widget.TextView muscleLabel = new android.widget.TextView(this);
        muscleLabel.setText(R.string.label_muscle_group);
        muscleLabel.setTextAppearance(android.R.style.TextAppearance_Material_Body1);
        container.addView(muscleLabel, container.getChildCount() - 2); // Before spinner

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

            saveExercise(name, description, selectedDay, selectedMuscle);
        });
    }

    private void saveExercise(String name, String description, String day, String muscleGroup) {
        new Thread(() -> {
            try {
                android.util.Log.d("NewExercise", "Starting saveExercise: " + name + " on " + day);
                fm.mrc.gymlog.data.AppDatabase db = fm.mrc.gymlog.data.AppDatabase.getInstance(this);
                fm.mrc.gymlog.data.Exercise newExercise = new fm.mrc.gymlog.data.Exercise(name, description, day);
                newExercise.muscleGroup = muscleGroup; // Set manually
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