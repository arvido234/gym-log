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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        Switch subtitleSwitch = findViewById(R.id.switch_show_subtitle);
        
        Switch switchSets = findViewById(R.id.switch_field_sets);
        Switch switchReps = findViewById(R.id.switch_field_reps);
        Switch switchWeight = findViewById(R.id.switch_field_weight);
        Switch switchRPE = findViewById(R.id.switch_field_rpe);

        RadioGroup languageGroup = findViewById(R.id.radio_group_language);
        RadioButton radioGerman = findViewById(R.id.radio_german);
        RadioButton radioEnglish = findViewById(R.id.radio_english);
        Button saveButton = findViewById(R.id.button_save_settings);

        // Load saved state
        subtitleSwitch.setChecked(prefs.getBoolean(KEY_SHOW_SUBTITLE, false));
        
        switchSets.setChecked(prefs.getBoolean(KEY_FIELD_SETS, true));
        switchReps.setChecked(prefs.getBoolean(KEY_FIELD_REPS, true));
        switchWeight.setChecked(prefs.getBoolean(KEY_FIELD_WEIGHT, true));
        switchRPE.setChecked(prefs.getBoolean(KEY_FIELD_RPE, true));
        
        String lang = prefs.getString(KEY_LANGUAGE, "de");
        if ("en".equals(lang)) {
            radioEnglish.setChecked(true);
        } else {
            radioGerman.setChecked(true);
        }

        saveButton.setOnClickListener(v -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(KEY_SHOW_SUBTITLE, subtitleSwitch.isChecked());
            
            editor.putBoolean(KEY_FIELD_SETS, switchSets.isChecked());
            editor.putBoolean(KEY_FIELD_REPS, switchReps.isChecked());
            editor.putBoolean(KEY_FIELD_WEIGHT, switchWeight.isChecked());
            editor.putBoolean(KEY_FIELD_RPE, switchRPE.isChecked());
            
            String selectedLang = radioEnglish.isChecked() ? "en" : "de";
            editor.putString(KEY_LANGUAGE, selectedLang);
            
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
}
