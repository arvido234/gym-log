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
            saveExercise(name, description, selectedDay);
        });
    }

    private void saveExercise(String name, String description, String day) {
        new Thread(() -> {
            try {
                android.util.Log.d("NewExercise", "Starting saveExercise: " + name + " on " + day);
                fm.mrc.gymlog.data.AppDatabase db = fm.mrc.gymlog.data.AppDatabase.getInstance(this);
                fm.mrc.gymlog.data.Exercise newExercise = new fm.mrc.gymlog.data.Exercise(name, description, day);
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