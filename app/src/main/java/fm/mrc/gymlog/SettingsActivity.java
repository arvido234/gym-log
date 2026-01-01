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

    public static final String KEY_THEME = "app_theme";
    public static final String KEY_BACKGROUND_TYPE = "bg_type"; // "charcoal", "black", "midnight", "sunrise", "custom"
    public static final String KEY_BACKGROUND_BLACK = "bg_black"; // Legacy/Fallback
    
    private android.widget.Button btnPickImage;
    private RadioButton radioBgGradientMidnight, radioBgGradientSunrise, radioBgCustom;
    private Switch switchShowSubtitle, switchEnableNotes, switchShowInlineNote, switchGlassMode;
    private RadioButton radioGerman, radioEnglish;
    private RadioButton radioThemeBlue, radioThemeRed, radioThemeGreen, radioThemeOrange, radioThemePurple;
    private RadioButton radioBgCharcoal, radioBgBlack;
    private Switch switchSets, switchReps, switchWeight, switchRPE, switchShow1RM, switchShowStreak;

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
        switchShowStreak = findViewById(R.id.switch_show_streak);

        RadioGroup languageGroup = findViewById(R.id.radio_group_language);
        radioGerman = findViewById(R.id.radio_german);
        radioEnglish = findViewById(R.id.radio_english);
        Button saveButton = findViewById(R.id.button_save_settings);
        
        radioThemeBlue = findViewById(R.id.radio_theme_blue);
        radioThemeRed = findViewById(R.id.radio_theme_red);
        radioThemeGreen = findViewById(R.id.radio_theme_green);
        radioThemeOrange = findViewById(R.id.radio_theme_orange);
        radioThemePurple = findViewById(R.id.radio_theme_purple);
        
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

        // Add listener to main group to uncheck custom if another is selected
        // ...
        
        // Frequency Spinner
        android.widget.Spinner spinnerFreq = findViewById(R.id.spinner_streak_freq);
        Integer[] freqs = {1, 2, 3, 4, 5, 6, 7};
        android.widget.ArrayAdapter<Integer> freqAdapter = new android.widget.ArrayAdapter<>(this, android.R.layout.simple_spinner_item, freqs);
        freqAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFreq.setAdapter(freqAdapter);
        
        int savedFreq = prefs.getInt(KEY_STREAK_FREQUENCY, 1);
        // Index is freq-1 (1->0, 2->1)
        if (savedFreq >= 1 && savedFreq <= 7) spinnerFreq.setSelection(savedFreq - 1);
        
        // ... in saveOnClickListener
        
        // Removed dangling line

        btnPickImage.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(android.content.Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            pickImageLauncher.launch(intent);
        });

        // Export Button Logic
        android.widget.Button exportButton = new android.widget.Button(this);
        exportButton.setText(R.string.btn_export_json);
        exportButton.setOnClickListener(v -> exportData());
        
        // Add to layout before Save button (Dynamically find correct index or just append to linear layout before save button)
        android.widget.LinearLayout rootLayout = (android.widget.LinearLayout) saveButton.getParent();
        rootLayout.addView(exportButton, rootLayout.indexOfChild(saveButton));

        saveButton.setOnClickListener(v -> {
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
            
            String selectedLang = radioEnglish.isChecked() ? "en" : "de";
            editor.putString(KEY_LANGUAGE, selectedLang);
            
            String selectedTheme = "blue";
            if (radioThemeRed.isChecked()) selectedTheme = "red";
            else if (radioThemeGreen.isChecked()) selectedTheme = "green";
            else if (radioThemeOrange.isChecked()) selectedTheme = "orange";
            else if (radioThemePurple.isChecked()) selectedTheme = "purple";
            editor.putString(KEY_THEME, selectedTheme);

            // Determine Background
            String selectedBg = "charcoal";
            if (radioBgBlack.isChecked()) selectedBg = "black";
            else if (radioBgGradientMidnight.isChecked()) selectedBg = "midnight";
            else if (radioBgGradientSunrise.isChecked()) selectedBg = "sunrise";
            else if (radioBgCustom.isChecked()) selectedBg = "custom";
            
            editor.putString(KEY_BACKGROUND_TYPE, selectedBg);
            // Sync legacy key just in case
            editor.putBoolean(KEY_BACKGROUND_BLACK, "black".equals(selectedBg));
            
            android.widget.Spinner sFreq = findViewById(R.id.spinner_streak_freq);
            if (sFreq != null && sFreq.getSelectedItem() != null) {
                editor.putInt(KEY_STREAK_FREQUENCY, (Integer)sFreq.getSelectedItem());
            }

            editor.apply();

            // Restart to apply changes
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
        new Thread(() -> {
            try {
                fm.mrc.gymlog.data.AppDatabase db = fm.mrc.gymlog.data.AppDatabase.getInstance(this);
                java.util.List<fm.mrc.gymlog.data.Exercise> exercises = db.exerciseDao().getAllExercisesSync();
                java.util.List<fm.mrc.gymlog.data.LogEntry> logs = db.logEntryDao().getAllLogEntries();
                
                // Simple manually constructed JSON
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
                
                // Save to Downloads
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
