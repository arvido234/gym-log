package fm.mrc.gymlog;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends BaseActivity {

    public static final String PREFS_NAME = "GymLogPrefs";
    public static final String KEY_SHOW_SUBTITLE = "show_subtitle";
    public static final String KEY_LANGUAGE = "language";
    
    public static final String KEY_FIELD_SETS = "field_sets";
    public static final String KEY_FIELD_REPS = "field_reps";
    public static final String KEY_FIELD_WEIGHT = "field_weight";
    public static final String KEY_FIELD_RPE = "field_rpe";
    public static final String KEY_SHOW_1RM = "show_1rm";
    public static final String KEY_ENABLE_NOTES = "enable_notes";
    public static final String KEY_SHOW_INLINE_NOTE = "show_inline_note";
    public static final String KEY_GLASS_MODE = "glass_mode";
    public static final String KEY_SHOW_STREAK = "show_streak";
    public static final String KEY_STREAK_FREQUENCY = "streak_frequency";
    public static final String KEY_ENABLE_STREAK_NOTES = "enable_streak_notes";

    public static final String KEY_THEME = "app_theme";
    public static final String KEY_BACKGROUND_TYPE = "bg_type"; // "charcoal", "black", "midnight", "sunrise", "custom"
    public static final String KEY_BACKGROUND_BLACK = "bg_black"; // Legacy/Fallback
    
    private android.widget.Button btnPickImage;
    private RadioButton radioBgGradientMidnight, radioBgGradientSunrise, radioBgCustom;
    private Switch switchShowSubtitle, switchEnableNotes, switchShowInlineNote, switchGlassMode;
    private RadioButton radioGerman, radioEnglish;
    private RadioButton radioThemeBlue, radioThemeRed, radioThemeGreen, radioThemeOrange, radioThemePurple;
    private RadioButton radioThemePink, radioThemeCyan, radioThemeYellow, radioThemeTeal;
    private RadioButton radioBgCharcoal, radioBgBlack;
    private Switch switchSets, switchReps, switchWeight, switchRPE, switchShow1RM, switchShowStreak, switchEnableStreakNotes;
    private Switch switchEnableRestTimer;

    public static final String KEY_ENABLE_REST_TIMER = "enable_rest_timer";
    public static final String KEY_DEFAULT_REST_SECONDS = "default_rest_seconds";

    private final androidx.activity.result.ActivityResultLauncher<android.content.Intent> pickImageLauncher = registerForActivityResult(
            new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    android.net.Uri selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        saveCustomImage(selectedImageUri);
                        radioBgCustom.setChecked(true);
                        android.widget.Toast.makeText(this, "Bild gespeichert", android.widget.Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private final androidx.activity.result.ActivityResultLauncher<android.content.Intent> importDataLauncher = registerForActivityResult(
            new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    android.net.Uri uri = result.getData().getData();
                    if (uri != null) {
                        importData(uri);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        switchShowSubtitle = findViewById(R.id.switch_show_subtitle);
        switchEnableNotes = findViewById(R.id.switch_show_note_button);
        switchShowInlineNote = findViewById(R.id.switch_show_note_preview);
        switchGlassMode = findViewById(R.id.switch_glass_mode);
        
        switchSets = findViewById(R.id.switch_field_sets);
        switchReps = findViewById(R.id.switch_field_reps);
        switchWeight = findViewById(R.id.switch_field_weight);
        switchRPE = findViewById(R.id.switch_field_rpe);
        switchShow1RM = findViewById(R.id.switch_show_1rm);
        
        // Correct initialization
        switchShowStreak = findViewById(R.id.switch_show_streak);
        switchEnableStreakNotes = findViewById(R.id.switch_enable_streak_notes);
        switchEnableRestTimer = findViewById(R.id.switch_enable_rest_timer);

        RadioGroup languageGroup = findViewById(R.id.radio_group_language);
        radioGerman = findViewById(R.id.radio_german);
        radioEnglish = findViewById(R.id.radio_english);
        Button saveButton = findViewById(R.id.button_save_settings);
        
        radioThemeBlue = findViewById(R.id.radio_theme_blue);
        radioThemeRed = findViewById(R.id.radio_theme_red);
        radioThemeGreen = findViewById(R.id.radio_theme_green);
        radioThemeOrange = findViewById(R.id.radio_theme_orange);
        radioThemePurple = findViewById(R.id.radio_theme_purple);
        radioThemePink = findViewById(R.id.radio_theme_pink);
        radioThemeCyan = findViewById(R.id.radio_theme_cyan);
        radioThemeYellow = findViewById(R.id.radio_theme_yellow);
        radioThemeTeal = findViewById(R.id.radio_theme_teal);
        
        radioBgCharcoal = findViewById(R.id.radio_bg_charcoal);
        radioBgBlack = findViewById(R.id.radio_bg_black);
        radioBgGradientMidnight = findViewById(R.id.radio_bg_gradient_midnight);
        radioBgGradientSunrise = findViewById(R.id.radio_bg_gradient_sunrise);
        radioBgCustom = findViewById(R.id.radio_bg_custom);
        btnPickImage = findViewById(R.id.button_pick_image);

        // Load saved state
        switchShowSubtitle.setChecked(prefs.getBoolean(KEY_SHOW_SUBTITLE, false));
        switchEnableNotes.setChecked(prefs.getBoolean(KEY_ENABLE_NOTES, true));
        switchShowInlineNote.setChecked(prefs.getBoolean(KEY_SHOW_INLINE_NOTE, false));
        switchGlassMode.setChecked(prefs.getBoolean(KEY_GLASS_MODE, false));
        
        switchSets.setChecked(prefs.getBoolean(KEY_FIELD_SETS, true));
        switchReps.setChecked(prefs.getBoolean(KEY_FIELD_REPS, true));
        switchWeight.setChecked(prefs.getBoolean(KEY_FIELD_WEIGHT, true));
        switchRPE.setChecked(prefs.getBoolean(KEY_FIELD_RPE, true));
        switchShow1RM.setChecked(prefs.getBoolean(KEY_SHOW_1RM, true));
        switchShowStreak.setChecked(prefs.getBoolean(KEY_SHOW_STREAK, true));
        switchEnableStreakNotes.setChecked(prefs.getBoolean(KEY_ENABLE_STREAK_NOTES, true));
        switchEnableRestTimer.setChecked(prefs.getBoolean(KEY_ENABLE_REST_TIMER, true));
        
        android.widget.EditText editDefaultRest = findViewById(R.id.edit_default_rest_seconds);
        if (editDefaultRest != null) {
            editDefaultRest.setText(String.valueOf(prefs.getInt(KEY_DEFAULT_REST_SECONDS, 90)));
        }
        
        String lang = prefs.getString(KEY_LANGUAGE, "de");
        if ("en".equals(lang)) {
            radioEnglish.setChecked(true);
        } else {
            radioGerman.setChecked(true);
        }
        
        // Theme Config
        String theme = prefs.getString(KEY_THEME, "blue");
        switch (theme) {
            case "red": radioThemeRed.setChecked(true); break;
            case "green": radioThemeGreen.setChecked(true); break;
            case "orange": radioThemeOrange.setChecked(true); break;
            case "purple": radioThemePurple.setChecked(true); break;
            case "pink": radioThemePink.setChecked(true); break;
            case "cyan": radioThemeCyan.setChecked(true); break;
            case "yellow": radioThemeYellow.setChecked(true); break;
            case "teal": radioThemeTeal.setChecked(true); break;
            default: radioThemeBlue.setChecked(true); break;
        }

        // Background Config
        // Default legacy fallback: if KEY_BACKGROUND_BLACK was true -> "black", else "charcoal"
        String defaultBg = prefs.getBoolean(KEY_BACKGROUND_BLACK, false) ? "black" : "charcoal";
        String bgType = prefs.getString(KEY_BACKGROUND_TYPE, defaultBg);
        
        switch (bgType) {
            case "black": radioBgBlack.setChecked(true); break;
            case "midnight": radioBgGradientMidnight.setChecked(true); break;
            case "sunrise": radioBgGradientSunrise.setChecked(true); break;
            case "custom": radioBgCustom.setChecked(true); break;
            default: radioBgCharcoal.setChecked(true); break;
        }

        // Manual handling for nested radio button (custom image)
        radioBgCustom.setOnClickListener(v -> {
            radioBgCustom.setChecked(true);
            radioBgCharcoal.setChecked(false);
            radioBgBlack.setChecked(false);
            radioBgGradientMidnight.setChecked(false);
            radioBgGradientSunrise.setChecked(false);
            // Clear parent group check
            ((RadioGroup)findViewById(R.id.radio_group_background)).clearCheck();
            // Re-check custom because clearCheck might have cleared it if it was somehow linked
            radioBgCustom.setChecked(true);
        });

        // Frequency Spinner
        android.widget.Spinner spinnerFreq = findViewById(R.id.spinner_streak_freq);
        Integer[] freqs = {1, 2, 3, 4, 5, 6, 7};
        android.widget.ArrayAdapter<Integer> freqAdapter = new android.widget.ArrayAdapter<>(this, android.R.layout.simple_spinner_item, freqs);
        freqAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFreq.setAdapter(freqAdapter);
        
        int savedFreq = prefs.getInt(KEY_STREAK_FREQUENCY, 1);
        // Index is freq-1 (1->0, 2->1)
        if (savedFreq >= 1 && savedFreq <= 7) spinnerFreq.setSelection(savedFreq - 1);
        
        btnPickImage.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(android.content.Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            pickImageLauncher.launch(intent);
        });

        // Export Button Logic (JSON)
        android.widget.Button exportButton = new android.widget.Button(this);
        exportButton.setText(R.string.btn_export_json);
        exportButton.setOnClickListener(v -> exportData());
        
        // Export CSV Button
        android.widget.Button exportCsvButton = new android.widget.Button(this);
        exportCsvButton.setText("Export CSV");
        exportCsvButton.setOnClickListener(v -> exportCsvData());

        // Reset App Button
        android.widget.Button resetButton = new android.widget.Button(this);
        resetButton.setText(R.string.action_reset_app);
        resetButton.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
        resetButton.setTextColor(getResources().getColor(android.R.color.white));
        resetButton.setPadding(0, 16, 0, 16);
        
        android.widget.LinearLayout rootLayout = (android.widget.LinearLayout) saveButton.getParent();
        rootLayout.addView(exportButton, rootLayout.indexOfChild(saveButton));
        
        // Import Button
        android.widget.Button importButton = new android.widget.Button(this);
        importButton.setText(R.string.btn_import_json);
        importButton.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(android.content.Intent.CATEGORY_OPENABLE);
            intent.setType("application/json");
            importDataLauncher.launch(intent);
        });
        rootLayout.addView(importButton, rootLayout.indexOfChild(saveButton));

        rootLayout.addView(exportCsvButton, rootLayout.indexOfChild(saveButton));
        // Add reset below save
        rootLayout.addView(resetButton); // Add at the end

        resetButton.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(R.string.dialog_reset_title)
                .setMessage(R.string.dialog_reset_message)
                .setPositiveButton(R.string.btn_reset_confirm, (dialog, which) -> {
                    new Thread(() -> {
                        try {
                            fm.mrc.gymlog.data.AppDatabase db = fm.mrc.gymlog.data.AppDatabase.getInstance(this);
                            db.clearAllTables();
                            
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.clear();
                            editor.commit();
                            
                             java.io.File file = new java.io.File(getFilesDir(), "custom_bg.jpg");
                             if (file.exists()) file.delete();
                             
                            runOnUiThread(() -> {
                                android.widget.Toast.makeText(this, "App Reset Complete", android.widget.Toast.LENGTH_SHORT).show();
                                android.content.Intent i = getBaseContext().getPackageManager()
                                    .getLaunchIntentForPackage(getBaseContext().getPackageName());
                                if (i != null) {
                                    i.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP | android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(i);
                                    Runtime.getRuntime().exit(0);
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }).start();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
        });

        saveButton.setOnClickListener(v -> {
            // ... (save logic)
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(KEY_SHOW_SUBTITLE, switchShowSubtitle.isChecked());
            editor.putBoolean(KEY_ENABLE_NOTES, switchEnableNotes.isChecked());
            editor.putBoolean(KEY_SHOW_INLINE_NOTE, switchShowInlineNote.isChecked());
            editor.putBoolean(KEY_GLASS_MODE, switchGlassMode.isChecked());
            
            editor.putBoolean(KEY_FIELD_SETS, switchSets.isChecked());
            editor.putBoolean(KEY_FIELD_REPS, switchReps.isChecked());
            editor.putBoolean(KEY_FIELD_WEIGHT, switchWeight.isChecked());
            editor.putBoolean(KEY_FIELD_RPE, switchRPE.isChecked());
            editor.putBoolean(KEY_SHOW_1RM, switchShow1RM.isChecked());
            editor.putBoolean(KEY_SHOW_STREAK, switchShowStreak.isChecked());
            editor.putBoolean(KEY_ENABLE_STREAK_NOTES, switchEnableStreakNotes.isChecked());
            
            String selectedLang = radioEnglish.isChecked() ? "en" : "de";
            editor.putString(KEY_LANGUAGE, selectedLang);
            
            String selectedTheme = "blue";
            if (radioThemeRed.isChecked()) selectedTheme = "red";
            else if (radioThemeGreen.isChecked()) selectedTheme = "green";
            else if (radioThemeOrange.isChecked()) selectedTheme = "orange";
            else if (radioThemePurple.isChecked()) selectedTheme = "purple";
            else if (radioThemePink.isChecked()) selectedTheme = "pink";
            else if (radioThemeCyan.isChecked()) selectedTheme = "cyan";
            else if (radioThemeYellow.isChecked()) selectedTheme = "yellow";
            else if (radioThemeTeal.isChecked()) selectedTheme = "teal";
            editor.putString(KEY_THEME, selectedTheme);

            // Determine Background
            String selectedBg = "charcoal";
            if (radioBgBlack.isChecked()) selectedBg = "black";
            else if (radioBgGradientMidnight.isChecked()) selectedBg = "midnight";
            else if (radioBgGradientSunrise.isChecked()) selectedBg = "sunrise";
            else if (radioBgCustom.isChecked()) selectedBg = "custom";
            
            editor.putString(KEY_BACKGROUND_TYPE, selectedBg);
            editor.putBoolean(KEY_BACKGROUND_BLACK, "black".equals(selectedBg));
            
            android.widget.Spinner sFreq = findViewById(R.id.spinner_streak_freq);
            if (sFreq != null && sFreq.getSelectedItem() != null) {
                editor.putInt(KEY_STREAK_FREQUENCY, (Integer)sFreq.getSelectedItem());
            }

            // Rest Timer
            editor.putBoolean(KEY_ENABLE_REST_TIMER, switchEnableRestTimer.isChecked());
            
            android.widget.EditText editDefaultRestSave = findViewById(R.id.edit_default_rest_seconds);
            if (editDefaultRestSave != null) {
                int defSeconds = 90;
                try {
                    String s = editDefaultRestSave.getText().toString();
                    if (!s.isEmpty()) defSeconds = Integer.parseInt(s);
                } catch (Exception e) {}
                editor.putInt(KEY_DEFAULT_REST_SECONDS, defSeconds);
            }

            editor.apply();

            android.content.Intent i = getBaseContext().getPackageManager()
                    .getLaunchIntentForPackage(getBaseContext().getPackageName());
            if (i != null) {
                i.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP | android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
            finish();
        });
    }

    private void exportData() {
        // ... (JSON export logic as before)
         new Thread(() -> {
            try {
                fm.mrc.gymlog.data.AppDatabase db = fm.mrc.gymlog.data.AppDatabase.getInstance(this);
                java.util.List<fm.mrc.gymlog.data.Exercise> exercises = db.exerciseDao().getAllExercisesSync();
                java.util.List<fm.mrc.gymlog.data.LogEntry> logs = db.logEntryDao().getAllLogEntries();
                
                StringBuilder json = new StringBuilder();
                json.append("{ \"exercises\": [");
                for (int i = 0; i < exercises.size(); i++) {
                    fm.mrc.gymlog.data.Exercise e = exercises.get(i);
                    json.append(String.format("{\"id\":%d, \"name\":\"%s\", \"muscle\":\"%s\", \"day\":\"%s\"}", 
                        e.exerciseId, e.name.replace("\"", "\\\""), 
                        (e.muscleGroup != null ? e.muscleGroup : ""), 
                        (e.day != null ? e.day : "")));
                    if (i < exercises.size() - 1) json.append(",");
                }
                json.append("], \"logs\": [");
                for (int i = 0; i < logs.size(); i++) {
                    fm.mrc.gymlog.data.LogEntry l = logs.get(i);
                    json.append(String.format("{\"exId\":%d, \"sets\":%d, \"reps\":%d, \"weight\":%.1f, \"ts\":%d}", 
                        l.exerciseId, l.sets, l.reps, l.weight, l.timestamp));
                    if (i < logs.size() - 1) json.append(",");
                }
                json.append("] }");
                
                String filename = "gymlog_backup_" + System.currentTimeMillis() + ".json";
                java.io.File path = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS);
                java.io.File file = new java.io.File(path, filename);
                
                java.io.FileOutputStream fos = new java.io.FileOutputStream(file);
                fos.write(json.toString().getBytes());
                fos.close();
                
                runOnUiThread(() -> android.widget.Toast.makeText(this, getString(R.string.msg_export_success, filename), android.widget.Toast.LENGTH_LONG).show());
                
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> android.widget.Toast.makeText(this, getString(R.string.msg_export_failed) + ": " + e.getMessage(), android.widget.Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
    
    private void exportCsvData() {
        new Thread(() -> {
            try {
                fm.mrc.gymlog.data.AppDatabase db = fm.mrc.gymlog.data.AppDatabase.getInstance(this);
                java.util.List<fm.mrc.gymlog.data.Exercise> exercises = db.exerciseDao().getAllExercisesSync();
                java.util.List<fm.mrc.gymlog.data.LogEntry> logs = db.logEntryDao().getAllLogEntries();
                
                // Map ID -> Name
                java.util.Map<Long, String> exMap = new java.util.HashMap<>();
                for (fm.mrc.gymlog.data.Exercise e : exercises) exMap.put(e.exerciseId, e.name);
                
                StringBuilder csv = new StringBuilder();
                csv.append("Date,Time,Exercise,Sets,Reps,Weight,RPE\n");
                
                java.text.SimpleDateFormat sdfDate = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
                java.text.SimpleDateFormat sdfTime = new java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault());
                
                for (fm.mrc.gymlog.data.LogEntry l : logs) {
                    String date = sdfDate.format(l.timestamp);
                    String time = sdfTime.format(l.timestamp);
                    String name = exMap.getOrDefault(l.exerciseId, "Unknown");
                    // Escape CSV
                    if (name.contains(",")) name = "\"" + name + "\"";
                    
                    csv.append(String.format(java.util.Locale.US, "%s,%s,%s,%d,%d,%.1f,%d\n", 
                        date, time, name, l.sets, l.reps, l.weight, l.rpe));
                }
                
                String filename = "gymlog_export_" + System.currentTimeMillis() + ".csv";
                java.io.File path = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS);
                java.io.File file = new java.io.File(path, filename);
                
                java.io.FileOutputStream fos = new java.io.FileOutputStream(file);
                fos.write(csv.toString().getBytes());
                fos.close();
                
                runOnUiThread(() -> android.widget.Toast.makeText(this, "CSV Export saved to Downloads/" + filename, android.widget.Toast.LENGTH_LONG).show());
                
            } catch (Exception e) {
                 e.printStackTrace();
                 runOnUiThread(() -> android.widget.Toast.makeText(this, "CSV Export failed: " + e.getMessage(), android.widget.Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void importData(android.net.Uri uri) {
        new Thread(() -> {
            try {
                // 1. Read File
                java.io.InputStream is = getContentResolver().openInputStream(uri);
                java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(is));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();
                is.close();

                String jsonString = sb.toString();
                org.json.JSONObject root = new org.json.JSONObject(jsonString);
                
                org.json.JSONArray exercisesArray = root.optJSONArray("exercises");
                org.json.JSONArray logsArray = root.optJSONArray("logs");

                fm.mrc.gymlog.data.AppDatabase db = fm.mrc.gymlog.data.AppDatabase.getInstance(this);

                if (exercisesArray != null) {
                    for (int i = 0; i < exercisesArray.length(); i++) {
                        org.json.JSONObject eObj = exercisesArray.getJSONObject(i);
                        fm.mrc.gymlog.data.Exercise exercise = new fm.mrc.gymlog.data.Exercise(
                            eObj.getString("name"),
                            eObj.optString("muscle", ""),
                            eObj.optString("day", "")
                        );
                        exercise.exerciseId = eObj.getLong("id"); // Force ID
                        
                        try {
                            db.exerciseDao().insert(exercise); 
                        } catch (android.database.sqlite.SQLiteConstraintException ex) {
                            // ID exists, update it
                            db.exerciseDao().update(exercise);
                        }
                    }
                }
                
                if (logsArray != null) {
                    for (int i = 0; i < logsArray.length(); i++) {
                        org.json.JSONObject lObj = logsArray.getJSONObject(i);
                        fm.mrc.gymlog.data.LogEntry entry = new fm.mrc.gymlog.data.LogEntry(
                            lObj.getLong("exId"), 
                            lObj.getLong("ts"),
                            lObj.getInt("sets"),
                            lObj.getInt("reps"),
                            lObj.getDouble("weight"),
                            lObj.optInt("rpe", 0)
                        );
                        
                        db.logEntryDao().insert(entry);
                    }
                }

                runOnUiThread(() -> {
                    android.widget.Toast.makeText(this, R.string.msg_import_success, android.widget.Toast.LENGTH_SHORT).show();
                    // Restart to refresh UI
                    android.content.Intent i = getBaseContext().getPackageManager()
                        .getLaunchIntentForPackage(getBaseContext().getPackageName());
                    if (i != null) {
                        i.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP | android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(i);
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> android.widget.Toast.makeText(this, getString(R.string.msg_import_failed, e.getMessage()), android.widget.Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private void saveCustomImage(android.net.Uri uri) {
        try {
            java.io.InputStream inputStream = getContentResolver().openInputStream(uri);
            java.io.File file = new java.io.File(getFilesDir(), "custom_bg.jpg");
            java.io.FileOutputStream outputStream = new java.io.FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.close();
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            android.widget.Toast.makeText(this, "Fehler beim Speichern des Bildes", android.widget.Toast.LENGTH_SHORT).show();
        }
    }
}
