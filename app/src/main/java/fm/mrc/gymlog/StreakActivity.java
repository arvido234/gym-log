package fm.mrc.gymlog;

import android.os.Bundle;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.content.SharedPreferences;

import java.util.Calendar;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;

import fm.mrc.gymlog.data.AppDatabase;
import fm.mrc.gymlog.data.StreakCheckIn;

public class StreakActivity extends BaseActivity {

    private androidx.recyclerview.widget.RecyclerView recyclerCalendar;
    private TextView textCurrentStreak, textMonthTitle, textSelectedDate, textDateStatus;
    private LinearLayout panelDateAction;
    private Button btnToggleCheckin, btnPrevMonth, btnNextMonth;
    private android.widget.EditText editNote;
    private TextView textHistoryDebug;

    private long selectedDateTimestamp = 0;
    private AppDatabase db;
    private Calendar currentMonthCal;

    // Cache
    private Set<String> logDays = new HashSet<>(); // "yyyy-M-d"
    private Set<String> checkInDays = new HashSet<>(); // "yyyy-M-d"
    private Map<String, String> checkInNotes = new HashMap<>(); // "yyyy-M-d" -> note
    private int targetFrequency = 1;
    private boolean enableNotes = true;
    
    // Watcher reference to remove listener temporarily
    private android.text.TextWatcher textWatcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_streak);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar_streak);
        toolbar.setTitle("Streak Calendar");
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        db = AppDatabase.getInstance(this);

        SharedPreferences prefs = getSharedPreferences(SettingsActivity.PREFS_NAME, MODE_PRIVATE);
        targetFrequency = prefs.getInt(SettingsActivity.KEY_STREAK_FREQUENCY, 1);
        enableNotes = prefs.getBoolean(SettingsActivity.KEY_ENABLE_STREAK_NOTES, true);

        // Init views
        recyclerCalendar = findViewById(R.id.recycler_calendar);
        recyclerCalendar.setLayoutManager(new androidx.recyclerview.widget.GridLayoutManager(this, 7));
        
        textCurrentStreak = findViewById(R.id.text_current_streak_large);
        textMonthTitle = findViewById(R.id.text_month_title);
        panelDateAction = findViewById(R.id.panel_date_action);
        textSelectedDate = findViewById(R.id.text_selected_date);
        textDateStatus = findViewById(R.id.text_date_status);
        editNote = findViewById(R.id.edit_note);
        btnToggleCheckin = findViewById(R.id.btn_toggle_checkin);
        btnPrevMonth = findViewById(R.id.btn_prev_month);
        btnNextMonth = findViewById(R.id.btn_next_month);
        textHistoryDebug = findViewById(R.id.text_history_debug);
        
        if (enableNotes) {
            editNote.setVisibility(android.view.View.VISIBLE);
        } else {
            editNote.setVisibility(android.view.View.GONE);
        }

        currentMonthCal = Calendar.getInstance();
        currentMonthCal.set(Calendar.DAY_OF_MONTH, 1);

        btnPrevMonth.setOnClickListener(v -> {
            currentMonthCal.add(Calendar.MONTH, -1);
            setupCalendar();
        });
        btnNextMonth.setOnClickListener(v -> {
            currentMonthCal.add(Calendar.MONTH, 1);
            setupCalendar();
        });

        btnToggleCheckin.setOnClickListener(v -> toggleCheckIn());
        
        textWatcher = new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(android.text.Editable s) {
                // If we have a checkin at this date, update it
                if (selectedDateTimestamp != 0) {
                     String key = getDateKey(selectedDateTimestamp);
                     if (checkInDays.contains(key)) {
                         checkInNotes.put(key, s.toString()); // Update local cache instantly
                         saveNoteToDb(selectedDateTimestamp, s.toString());
                     }
                }
            }
        };

        editNote.addTextChangedListener(textWatcher);

        refreshData();
    }
    
    private void saveNoteToDb(long ts, String note) {
        new Thread(() -> {
            // Delete old, insert new with note
            db.streakCheckInDao().deleteByTimestamp(ts);
            StreakCheckIn s = new StreakCheckIn(ts);
            s.note = note;
            db.streakCheckInDao().insert(s);
            // Dont refresh full data here to avoid flicker, just background update
        }).start();
    }

    private Map<String, Double> volumeMap = new HashMap<>();
    private double maxVolume = 0;

    private void refreshData() {
        new Thread(() -> {
            List<Long> logTs = db.logEntryDao().getDistinctTimestamps();
            List<StreakCheckIn> checkIns = db.streakCheckInDao().getAll();
            List<fm.mrc.gymlog.data.LogEntry> allLogs = db.logEntryDao().getAllLogEntries(); // Fetch all logs for volume

            logDays.clear();
            checkInDays.clear();
            checkInNotes.clear();
            volumeMap.clear();
            maxVolume = 0;

            for (Long ts : logTs) logDays.add(getDateKey(ts));
            for (StreakCheckIn s : checkIns) {
                String key = getDateKey(s.timestamp);
                checkInDays.add(key);
                if (s.note != null) {
                    checkInNotes.put(key, s.note);
                }
            }
            
            // Calculate Volume for Heatmap
            for (fm.mrc.gymlog.data.LogEntry entry : allLogs) {
                String key = getDateKey(entry.timestamp);
                // Volume = sets * reps * weight.
                // If weight 0, use 1? Or just Sets * Reps?
                // Let's use strict weight volume. If weight is 0 (bodyweight), assume 1kg for visualization or user Sets * Reps?
                // Standard Volume Load = Sets * Reps * Weight.
                double vol = entry.sets * entry.reps * (entry.weight > 0 ? entry.weight : 1.0); 
                volumeMap.put(key, volumeMap.getOrDefault(key, 0.0) + vol);
            }
            
            // Find Max Volume
            for (Double v : volumeMap.values()) {
                if (v > maxVolume) maxVolume = v;
            }
            
            // For calculation, we still need just timestamps.
            List<Long> checkTs = new java.util.ArrayList<>();
            for (StreakCheckIn s : checkIns) checkTs.add(s.timestamp);

            int streak = fm.mrc.gymlog.util.StreakCalculator.calculateWeeklyStreak(logTs, checkTs, targetFrequency);

            runOnUiThread(() -> {
                textCurrentStreak.setText(getString(R.string.text_streak_message, streak));
                setupCalendar();
                // Update panel if selected
                if (selectedDateTimestamp != 0) updateSelectedDatePanel(selectedDateTimestamp);
            });
        }).start();
    }

    private void setupCalendar() {
        // ... (existing code for month title)
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMMM yyyy", java.util.Locale.getDefault());
        textMonthTitle.setText(sdf.format(currentMonthCal.getTime()));

        List<Long> days = new ArrayList<>();
        Calendar c = (Calendar) currentMonthCal.clone();
        c.set(Calendar.DAY_OF_MONTH, 1);
        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK); 
        int offset = dayOfWeek - Calendar.MONDAY;
        if (offset < 0) offset += 7;
        
        for (int i = 0; i < offset; i++) days.add(0L);
        
        int maxDay = c.getActualMaximum(Calendar.DAY_OF_MONTH);
        for (int i = 1; i <= maxDay; i++) {
            days.add(c.getTimeInMillis());
            c.add(Calendar.DAY_OF_MONTH, 1);
        }

        CalendarAdapter adapter = new CalendarAdapter(days, logDays, checkInDays, this::onDateSelected);
        adapter.setVolumeData(volumeMap, maxVolume); // Pass volume data
        adapter.setSelectedTimestamp(selectedDateTimestamp); 
        recyclerCalendar.setAdapter(adapter);
    }
    
    private void onDateSelected(long ts) {
        selectedDateTimestamp = ts;
        updateSelectedDatePanel(ts);
    }

    private String getDateKey(long ts) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(ts);
        return c.get(Calendar.YEAR) + "-" + c.get(Calendar.MONTH) + "-" + c.get(Calendar.DAY_OF_MONTH);
    }

    private void updateSelectedDatePanel(long ts) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(ts);
        
        panelDateAction.setVisibility(android.view.View.VISIBLE);
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        
        textSelectedDate.setText(String.format("%d-%02d-%02d", year, month + 1, day));
        String key = year + "-" + month + "-" + day;

        boolean isLogged = logDays.contains(key);
        boolean isChecked = checkInDays.contains(key);
        
        // Check for future date
        Calendar now = Calendar.getInstance();
        // Reset "now" to midnight of tomorrow to strictly compare days? 
        // Or clearer: if (selectedYear > nowYear) || ...
        
        boolean isFuture = false;
        if (year > now.get(Calendar.YEAR)) isFuture = true;
        else if (year == now.get(Calendar.YEAR)) {
            if (month > now.get(Calendar.MONTH)) isFuture = true;
            else if (month == now.get(Calendar.MONTH)) {
                if (day > now.get(Calendar.DAY_OF_MONTH)) isFuture = true;
            }
        }
        
        // Temporarily remove watcher regarding to update loop
        if (textWatcher != null) editNote.removeTextChangedListener(textWatcher);
        
        if (isFuture) {
            textDateStatus.setText("Status: Future Date");
            btnToggleCheckin.setEnabled(false);
            btnToggleCheckin.setText("Cannot Check-in in Future");
            editNote.setVisibility(android.view.View.GONE);
        } else if (isLogged) {
            textDateStatus.setText("Status: Workout Logged (App)");
            btnToggleCheckin.setEnabled(false);
            btnToggleCheckin.setText("Already Logged");
            editNote.setVisibility(android.view.View.GONE); // Cant note on logs yet
            editNote.setText("");
        } else if (isChecked) {
            textDateStatus.setText("Status: Manual Check-in");
            btnToggleCheckin.setEnabled(true);
            btnToggleCheckin.setText("Remove Check-in");
            if (enableNotes) {
                editNote.setVisibility(android.view.View.VISIBLE);
                String note = checkInNotes.get(key);
                editNote.setText(note != null ? note : "");
            }
        } else {
            textDateStatus.setText("Status: No Activity");
            btnToggleCheckin.setEnabled(true);
            btnToggleCheckin.setText("Mark as Trained");
             if (enableNotes) {
                editNote.setVisibility(android.view.View.VISIBLE);
                editNote.setText("");
            }
        }
        
        if (textWatcher != null) editNote.addTextChangedListener(textWatcher);
    }

    private void toggleCheckIn() {
        if (selectedDateTimestamp == 0) return;
        String key = getDateKey(selectedDateTimestamp);

        if (checkInDays.contains(key)) {
            // Remove - Robustly using date range for that day
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(selectedDateTimestamp);
            c.set(Calendar.HOUR_OF_DAY, 0);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);
            long min = c.getTimeInMillis();
            c.set(Calendar.HOUR_OF_DAY, 23);
            c.set(Calendar.MINUTE, 59);
            c.set(Calendar.SECOND, 59);
            c.set(Calendar.MILLISECOND, 999);
            long max = c.getTimeInMillis();

            new Thread(() -> {
                db.streakCheckInDao().deleteByDateRange(min, max);
                runOnUiThread(this::refreshData);
                // Trigger Widget Update
                sendWidgetUpdate();
            }).start();
        } else {
            // Add with note
            new Thread(() -> {
                // Ensure insert is normalized to what we expect, or just use selectedDateTimestamp which we hope is midnight
                StreakCheckIn s = new StreakCheckIn(selectedDateTimestamp);
                s.note = editNote.getText().toString();
                db.streakCheckInDao().insert(s);
                runOnUiThread(this::refreshData);
                sendWidgetUpdate();
            }).start();
        }
    }

    private void sendWidgetUpdate() {
        android.content.Intent intent = new android.content.Intent(this, fm.mrc.gymlog.widget.StreakWidget.class);
        intent.setAction(android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int[] ids = android.appwidget.AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(
                new android.content.ComponentName(getApplication(), fm.mrc.gymlog.widget.StreakWidget.class));
        intent.putExtra(android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        sendBroadcast(intent);
    }
}
