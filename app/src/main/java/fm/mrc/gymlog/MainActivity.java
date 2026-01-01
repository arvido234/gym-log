package fm.mrc.gymlog;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import fm.mrc.gymlog.data.AppDatabase;
import fm.mrc.gymlog.data.Exercise;
import fm.mrc.gymlog.data.ExerciseDao;
import fm.mrc.gymlog.data.ExerciseWithLastLog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class MainActivity extends BaseActivity implements ExerciseAdapter.AdapterCallbacks {

    private ExerciseAdapter adapter;
    private AppDatabase db;
    private RecyclerView recyclerView;

    private TextView streakTextView;

    private android.widget.FrameLayout layoutRestTimer;
    private android.widget.ProgressBar progressRestTimer;
    private android.widget.TextView textRestTimer;
    private android.widget.ImageButton btnCancelTimer;
    private android.os.CountDownTimer restTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        streakTextView = findViewById(R.id.text_streak_indicator);
        
        // Rest Timer UI
        layoutRestTimer = findViewById(R.id.layout_rest_timer);
        progressRestTimer = findViewById(R.id.progress_rest_timer);
        textRestTimer = findViewById(R.id.text_rest_timer);
        btnCancelTimer = findViewById(R.id.btn_cancel_timer);
        
        if (btnCancelTimer != null) {
            btnCancelTimer.setOnClickListener(v -> cancelRestTimer());
        }

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        
        // ... (rest of onCreate remains existing, just ensure duplicates aren't created by replacement range if I replace huge chunk. I will try to target specific methods or merge properly)
        
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
                // android.util.Log.d("MainActivity", "Exercises loaded: " + (exercisesWithLog != null ? exercisesWithLog.size() : "null"));
                updateList(exercisesWithLog);
            } catch (Exception e) {
                // android.util.Log.e("MainActivity", "Error updating list", e);
            }
        });
        
        // Swipe to Delete logic (keeping existing)
        androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback(0, androidx.recyclerview.widget.ItemTouchHelper.LEFT) {
            @Override
            public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                 if (viewHolder instanceof ExerciseAdapter.HeaderViewHolder) return 0;
                 return super.getSwipeDirs(recyclerView, viewHolder);
            }

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                int position = viewHolder.getAdapterPosition();
                Object item = adapter.getItem(position);
                
                if (item instanceof ExerciseWithLastLog) {
                    Exercise exercise = ((ExerciseWithLastLog) item).getExercise();
                    new Thread(() -> db.exerciseDao().delete(exercise)).start();
                    
                    com.google.android.material.snackbar.Snackbar snackbar = com.google.android.material.snackbar.Snackbar
                        .make(recyclerView, exercise.name + " gelöscht", com.google.android.material.snackbar.Snackbar.LENGTH_LONG);
                    
                    snackbar.setAction("Rückgängig", view -> {
                        new Thread(() -> db.exerciseDao().insert(exercise)).start();
                    });
                    
                    snackbar.setActionTextColor(getResources().getColor(R.color.gym_primary));
                    snackbar.show();
                }
            }
            
            @Override
            public void onChildDraw(android.graphics.Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                 if (viewHolder instanceof ExerciseAdapter.ExerciseViewHolder) {
                     View itemView = viewHolder.itemView;
                     android.graphics.Paint p = new android.graphics.Paint();
                     if (dX < 0) { // Swiping Left
                         p.setColor(android.graphics.Color.parseColor("#B00020")); 
                         c.drawRect((float) itemView.getRight() + dX, (float) itemView.getTop(),
                                 (float) itemView.getRight(), (float) itemView.getBottom(), p);
                         android.graphics.drawable.Drawable icon = androidx.core.content.ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_delete_forever);
                         if (icon == null) icon = androidx.core.content.ContextCompat.getDrawable(MainActivity.this, android.R.drawable.ic_menu_delete);
                         
                         if (icon != null) {
                             icon.setTint(android.graphics.Color.WHITE);
                             int margin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
                             int top = itemView.getTop() + (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
                             int bottom = top + icon.getIntrinsicHeight();
                             int iconLeft = itemView.getRight() - margin - icon.getIntrinsicWidth();
                             int iconRight = itemView.getRight() - margin;
                             if (-dX > margin) {
                                 icon.setBounds(iconLeft, top, iconRight, bottom);
                                 icon.draw(c);
                             }
                         }
                     }
                 }
                 super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };

        androidx.recyclerview.widget.ItemTouchHelper itemTouchHelper = new androidx.recyclerview.widget.ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        Button historyButton = findViewById(R.id.button_history);
        if (historyButton != null) {
            historyButton.setOnClickListener(v -> {
                // startActivity(new Intent(MainActivity.this, HistoryActivity.class)); 
            });
        }

        FloatingActionButton fab = findViewById(R.id.fab_add_exercise);
        if (fab != null) {
            fab.setOnClickListener(v -> {
                startActivity(new Intent(MainActivity.this, NewExerciseActivity.class));
            });
        }
        
        checkStreak();
    }
    
    private void startRestTimer(int seconds) {
        cancelRestTimer(); // Stop existing
        if (seconds <= 0) return;
        
        if (layoutRestTimer != null) layoutRestTimer.setVisibility(View.VISIBLE);
        if (progressRestTimer != null) {
            progressRestTimer.setMax(seconds * 100);
            progressRestTimer.setProgress(0);
        }
        
        final int totalMillis = seconds * 1000;
        
        restTimer = new android.os.CountDownTimer(totalMillis, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                int progress = (int) (totalMillis - millisUntilFinished);
                if (progressRestTimer != null) {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                        progressRestTimer.setProgress(progress / 10, true);
                    } else {
                        progressRestTimer.setProgress(progress / 10);
                    }
                }
                if (textRestTimer != null) {
                    textRestTimer.setText(getString(R.string.rest_timer_running, millisUntilFinished / 1000));
                }
            }

            @Override
            public void onFinish() {
                if (textRestTimer != null) textRestTimer.setText(R.string.rest_timer_finished);
                if (progressRestTimer != null) progressRestTimer.setProgress(progressRestTimer.getMax());
                
                // Vibrate
                android.os.Vibrator v = (android.os.Vibrator) getSystemService(android.content.Context.VIBRATOR_SERVICE);
                if (v != null) {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        v.vibrate(android.os.VibrationEffect.createOneShot(500, android.os.VibrationEffect.DEFAULT_AMPLITUDE));
                    } else {
                        v.vibrate(500);
                    }
                }
                
                // Hide after delay? Or keep until dismissed?
                // For now keep visible saying "Finished"
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                     if (layoutRestTimer != null) layoutRestTimer.setVisibility(View.GONE);
                }, 5000);
            }
        };
        restTimer.start();
    }
    
    private void cancelRestTimer() {
        if (restTimer != null) {
            restTimer.cancel();
            restTimer = null;
        }
        if (layoutRestTimer != null) layoutRestTimer.setVisibility(View.GONE);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelRestTimer();
    }

    // ... (keep logic for onResume, checkStreak, menus, updateList, etc.)
    
    // Replacing showEditExerciseDialog and showAddLogDialog:
    
    private void showEditExerciseDialog(Exercise exercise) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.edit) + ": " + exercise.name);

        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(32, 32, 32, 32);

        // Name
        final android.widget.TextView nameLabel = new android.widget.TextView(this);
        nameLabel.setText(getString(R.string.label_name));
        layout.addView(nameLabel);

        final android.widget.EditText nameInput = new android.widget.EditText(this);
        nameInput.setText(exercise.name);
        layout.addView(nameInput);

        // Subtitle
        final android.widget.TextView subtitleLabel = new android.widget.TextView(this);
        subtitleLabel.setText(getString(R.string.label_subtitle));
        layout.addView(subtitleLabel);

        final android.widget.EditText subtitleInput = new android.widget.EditText(this);
        subtitleInput.setText(exercise.description);
        subtitleInput.setHint(getString(R.string.hint_subtitle));
        layout.addView(subtitleInput);

        // Day Spinner
        final android.widget.TextView dayLabel = new android.widget.TextView(this);
        dayLabel.setText(getString(R.string.label_day));
        layout.addView(dayLabel);

        final android.widget.Spinner daySpinner = new android.widget.Spinner(this);
        final String[] dbDays = {"Montag", "Dienstag", "Mittwoch", "Donnerstag", "Freitag", "Samstag", "Sonntag", "Ohne Tag"};
        final String[] displayDays = {
            getString(R.string.day_monday), getString(R.string.day_tuesday), getString(R.string.day_wednesday),
            getString(R.string.day_thursday), getString(R.string.day_friday), getString(R.string.day_saturday),
            getString(R.string.day_sunday), getString(R.string.day_unknown)
        };
        
        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(this, android.R.layout.simple_spinner_item, displayDays);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        daySpinner.setAdapter(adapter);

        String currentDay = exercise.day != null ? exercise.day : "Ohne Tag";
        int selectionIndex = 7;
        for (int i = 0; i < dbDays.length; i++) {
            if (dbDays[i].equals(currentDay)) {
                selectionIndex = i;
                break;
            }
        }
        daySpinner.setSelection(selectionIndex);
        layout.addView(daySpinner);

        // Muscle Spinner
        final android.widget.TextView muscleLabel = new android.widget.TextView(this);
        muscleLabel.setText(getString(R.string.label_muscle_group));
        muscleLabel.setPadding(0, 16, 0, 0);
        layout.addView(muscleLabel);

        final android.widget.Spinner muscleSpinner = new android.widget.Spinner(this);
        String[] muscleGroups = {
            getString(R.string.muscle_chest), getString(R.string.muscle_back), 
            getString(R.string.muscle_legs), getString(R.string.muscle_shoulders), 
            getString(R.string.muscle_arms), getString(R.string.muscle_core), 
            getString(R.string.muscle_other)
        };
        String[] muscleKeys = {"Chest", "Back", "Legs", "Shoulders", "Arms", "Core", "Other"};

        android.widget.ArrayAdapter<String> muscleAdapter = new android.widget.ArrayAdapter<>(this, android.R.layout.simple_spinner_item, muscleGroups);
        muscleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        muscleSpinner.setAdapter(muscleAdapter);
        
        String currentMuscle = exercise.muscleGroup != null ? exercise.muscleGroup : "Other";
        int muscleSelectionIndex = 6;
        for (int i = 0; i < muscleKeys.length; i++) {
            if (muscleKeys[i].equals(currentMuscle)) {
                muscleSelectionIndex = i;
                break;
            }
        }
        muscleSpinner.setSelection(muscleSelectionIndex);
        layout.addView(muscleSpinner);
        
        // Rest Timer
        final android.widget.TextView restLabel = new android.widget.TextView(this);
        restLabel.setText(getString(R.string.label_rest_seconds));
        restLabel.setPadding(0, 16, 0, 0);
        layout.addView(restLabel);

        final android.widget.EditText restInput = new android.widget.EditText(this);
        restInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        restInput.setText(String.valueOf(exercise.restTimerSeconds > 0 ? exercise.restTimerSeconds : 90));
        layout.addView(restInput);


        builder.setView(layout);

        builder.setPositiveButton(getString(R.string.save), (dialog, which) -> {
            String newName = nameInput.getText().toString().trim();
            if (newName.isEmpty()) {
                android.widget.Toast.makeText(this, getString(R.string.error_name_empty), android.widget.Toast.LENGTH_SHORT).show();
                return;
            }
            
            String newSubtitle = subtitleInput.getText().toString().trim();
            int selectedPos = daySpinner.getSelectedItemPosition();
            String newDay = dbDays[selectedPos]; 

            int musclePos = muscleSpinner.getSelectedItemPosition();
            String newMuscle = (musclePos >= 0 && musclePos < muscleKeys.length) ? muscleKeys[musclePos] : "Other";
            
            int newRest = 90;
            try {
                newRest = Integer.parseInt(restInput.getText().toString().trim());
            } catch (Exception e) {}

            exercise.name = newName;
            exercise.description = newSubtitle;
            exercise.day = newDay;
            exercise.muscleGroup = newMuscle;
            exercise.restTimerSeconds = newRest;

            new Thread(() -> db.exerciseDao().update(exercise)).start();
            android.widget.Toast.makeText(this, getString(R.string.msg_saved), android.widget.Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void showAddLogDialog(Exercise exercise) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.dialog_log_title, exercise.name));

        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(32, 32, 32, 32);
        
        android.content.SharedPreferences prefs = getSharedPreferences(SettingsActivity.PREFS_NAME, MODE_PRIVATE);
        boolean showSets = prefs.getBoolean(SettingsActivity.KEY_FIELD_SETS, true);
        boolean showReps = prefs.getBoolean(SettingsActivity.KEY_FIELD_REPS, true);
        boolean showWeight = prefs.getBoolean(SettingsActivity.KEY_FIELD_WEIGHT, true);
        boolean showRPE = prefs.getBoolean(SettingsActivity.KEY_FIELD_RPE, true);

        final android.widget.EditText setsInput = new android.widget.EditText(this);
        setsInput.setHint(getString(R.string.hint_sets));
        setsInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        if (!showSets) setsInput.setVisibility(View.GONE);
        layout.addView(setsInput);

        final android.widget.EditText repsInput = new android.widget.EditText(this);
        repsInput.setHint(getString(R.string.hint_reps));
        repsInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        if (!showReps) repsInput.setVisibility(View.GONE);
        layout.addView(repsInput);

        final android.widget.EditText weightInput = new android.widget.EditText(this);
        weightInput.setHint(getString(R.string.hint_weight));
        weightInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        if (!showWeight) weightInput.setVisibility(View.GONE);
        layout.addView(weightInput);

        final android.widget.EditText rpeInput = new android.widget.EditText(this);
        rpeInput.setHint(getString(R.string.hint_rpe));
        rpeInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        if (!showRPE) rpeInput.setVisibility(View.GONE);
        layout.addView(rpeInput);
        
        // 1RM Estimate linked
        final android.widget.TextView rmLabel = new android.widget.TextView(this);
        boolean show1RM = prefs.getBoolean(SettingsActivity.KEY_SHOW_1RM, true);
        if (show1RM) {
            rmLabel.setText(getString(R.string.est_1rm, "-"));
            rmLabel.setGravity(android.view.Gravity.CENTER_HORIZONTAL);
            rmLabel.setPadding(0, 24, 0, 0);
            rmLabel.setTextSize(14);
            rmLabel.setTextColor(getResources().getColor(android.R.color.darker_gray));
            layout.addView(rmLabel);

            android.text.TextWatcher rmWatcher = new android.text.TextWatcher() {
                 @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                 @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                 @Override public void afterTextChanged(android.text.Editable s) {
                     try {
                         String wStr = weightInput.getText().toString();
                         String rStr = repsInput.getText().toString();
                         if (!wStr.isEmpty() && !rStr.isEmpty()) {
                             double w = Double.parseDouble(wStr);
                             int r = Integer.parseInt(rStr);
                             if (r > 0) {
                                 double rm = w * (1.0 + (double)r / 30.0);
                                 rmLabel.setText(getString(R.string.est_1rm, String.format(java.util.Locale.getDefault(), "%.1f", rm)));
                             }
                         } else rmLabel.setText(getString(R.string.est_1rm, "-"));
                     } catch(Exception e){}
                 }
            };
            weightInput.addTextChangedListener(rmWatcher);
            repsInput.addTextChangedListener(rmWatcher);
        }

        builder.setView(layout);
        
        final java.util.concurrent.atomic.AtomicReference<fm.mrc.gymlog.data.LogEntry> lastLogRef = new java.util.concurrent.atomic.AtomicReference<>();
        
        new Thread(() -> {
            fm.mrc.gymlog.data.LogEntry lastEntry = db.logEntryDao().getLatestLogEntry(exercise.exerciseId);
            if (lastEntry != null) {
                lastLogRef.set(lastEntry);
                runOnUiThread(() -> {
                    if (showSets) setsInput.setHint(String.valueOf(lastEntry.sets));
                    if (showReps) repsInput.setHint(String.valueOf(lastEntry.reps));
                    if (showWeight) weightInput.setHint(String.valueOf(lastEntry.weight));
                    if (showRPE) rpeInput.setHint(String.valueOf(lastEntry.rpe));
                });
            }
        }).start();

        builder.setPositiveButton(getString(R.string.save), (dialog, which) -> {
            try {
                String setsStr = setsInput.getText().toString();
                String repsStr = repsInput.getText().toString();
                String weightStr = weightInput.getText().toString();
                String rpeStr = rpeInput.getText().toString();
                
                fm.mrc.gymlog.data.LogEntry last = lastLogRef.get();
                int sets = 0;
                if (showSets) {
                    if (!setsStr.isEmpty()) sets = Integer.parseInt(setsStr);
                    else if (last != null) sets = last.sets;
                }
                int reps = 0;
                if (showReps) {
                    if (!repsStr.isEmpty()) reps = Integer.parseInt(repsStr);
                    else if (last != null) reps = last.reps;
                }
                double weight = 0.0;
                if (showWeight) {
                    if (!weightStr.isEmpty()) weight = Double.parseDouble(weightStr);
                    else if (last != null) weight = last.weight;
                }
                int rpe = 0;
                if (showRPE) {
                    if (!rpeStr.isEmpty()) rpe = Integer.parseInt(rpeStr);
                    else if (last != null) rpe = last.rpe;
                }

                boolean invalid = false;
                if (showSets && sets <= 0) invalid = true;
                if (showReps && reps <= 0) invalid = true;
                
                if (invalid) {
                    android.widget.Toast.makeText(this, getString(R.string.req_fields), android.widget.Toast.LENGTH_SHORT).show();
                    return;
                }

                fm.mrc.gymlog.data.LogEntry entry = new fm.mrc.gymlog.data.LogEntry(
                        exercise.exerciseId, System.currentTimeMillis(), sets, reps, weight, rpe);
                
                new Thread(() -> db.logEntryDao().insert(entry)).start();
                android.widget.Toast.makeText(this, getString(R.string.msg_saved), android.widget.Toast.LENGTH_SHORT).show();

                // Trigger Streak Check
                // (Already done in onResume)
                
                // --- TRIGGER REST TIMER ---
                boolean restEnabled = prefs.getBoolean(SettingsActivity.KEY_ENABLE_REST_TIMER, true);
                if (restEnabled && exercise.restTimerSeconds > 0) {
                     startRestTimer(exercise.restTimerSeconds);
                }

            } catch (NumberFormatException e) {
                android.widget.Toast.makeText(this, getString(R.string.error_invalid_input), android.widget.Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.cancel());
        builder.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkStreak();
        android.content.SharedPreferences prefs = getSharedPreferences(SettingsActivity.PREFS_NAME, MODE_PRIVATE);
        boolean showSubtitle = prefs.getBoolean(SettingsActivity.KEY_SHOW_SUBTITLE, false);
        boolean showSets = prefs.getBoolean(SettingsActivity.KEY_FIELD_SETS, true);
        boolean showReps = prefs.getBoolean(SettingsActivity.KEY_FIELD_REPS, true);
        boolean showWeight = prefs.getBoolean(SettingsActivity.KEY_FIELD_WEIGHT, true);
        boolean enableNotes = prefs.getBoolean(SettingsActivity.KEY_ENABLE_NOTES, true);
        boolean showInlineNote = prefs.getBoolean(SettingsActivity.KEY_SHOW_INLINE_NOTE, false);
        boolean glassMode = prefs.getBoolean(SettingsActivity.KEY_GLASS_MODE, false);
        if (adapter != null) {
            adapter.setShowSubtitle(showSubtitle);
            adapter.setFieldVisibility(showSets, showReps, showWeight);
            adapter.setNoteSettings(enableNotes, showInlineNote);
            adapter.setGlassMode(glassMode);
        }
    }
    
    private void checkStreak() {
        android.content.SharedPreferences prefs = getSharedPreferences(SettingsActivity.PREFS_NAME, MODE_PRIVATE);
        boolean showStreak = prefs.getBoolean(SettingsActivity.KEY_SHOW_STREAK, true);
        int targetFreq = prefs.getInt(SettingsActivity.KEY_STREAK_FREQUENCY, 1);
        if (!showStreak) {
            if (streakTextView != null) streakTextView.setVisibility(android.view.View.GONE);
            return;
        }
        streakTextView.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, StreakActivity.class)));
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            java.util.List<Long> logDates = db.logEntryDao().getDistinctTimestamps();
            java.util.List<Long> checkDates = null;
            try {
                checkDates = db.streakCheckInDao().getAllCheckInTimestamps();
            } catch (Exception e) { }
            int streak = fm.mrc.gymlog.util.StreakCalculator.calculateWeeklyStreak(logDates, checkDates, targetFreq);
            runOnUiThread(() -> {
                if (streakTextView == null) return;
                streakTextView.setVisibility(android.view.View.VISIBLE);
                if (streak > 0) {
                    streakTextView.setText(getString(R.string.text_streak_message, streak));
                } else {
                     streakTextView.setText(R.string.text_streak_none);
                }
            });
        }).start();
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
        } else if (item.getItemId() == R.id.action_statistics) {
            startActivity(new Intent(this, MuscleStatsActivity.class));
            return true;
        } else if (item.getItemId() == R.id.action_share) {
            shareWorkoutSummary();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    private void shareWorkoutSummary() {
        new Thread(() -> {
            // Start of today
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
            cal.set(java.util.Calendar.MINUTE, 0);
            cal.set(java.util.Calendar.SECOND, 0);
            cal.set(java.util.Calendar.MILLISECOND, 0);
            long startOfDay = cal.getTimeInMillis();
            
            AppDatabase db = AppDatabase.getInstance(this);
            // This query is perfect: Grouped by muscle, sums sets and volume.
            java.util.List<fm.mrc.gymlog.data.MuscleStats> stats = db.logEntryDao().getWeeklyMuscleStats(startOfDay);
            
            if (stats == null || stats.isEmpty()) {
                runOnUiThread(() -> android.widget.Toast.makeText(this, "Kein Training heute gefunden!", android.widget.Toast.LENGTH_SHORT).show());
                return;
            }
            
            // Calculate Totals
            int totalSets = 0;
            double totalVolume = 0;
            for (fm.mrc.gymlog.data.MuscleStats s : stats) {
                totalSets += s.totalSets;
                totalVolume += s.totalVolume;
            }
            
            final int finalSets = totalSets;
            final double finalVolume = totalVolume;
            
            runOnUiThread(() -> {
                generateAndShareImage(stats, finalSets, finalVolume);
            });
        }).start();
    }

    private void generateAndShareImage(java.util.List<fm.mrc.gymlog.data.MuscleStats> stats, int totalSets, double totalVolume) {
        // Inflate Layout
        android.view.LayoutInflater inflater = android.view.LayoutInflater.from(this);
        android.view.View view = inflater.inflate(R.layout.layout_share_summary, null);
        
        // Setup Date
        TextView dateParams = view.findViewById(R.id.share_date);
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd. MMM yyyy", java.util.Locale.getDefault());
        dateParams.setText(sdf.format(new java.util.Date()).toUpperCase());
        
        // Setup Stats
        android.widget.LinearLayout statsContainer = view.findViewById(R.id.share_stats_container);
        
        // Add Total Summary Big
        addShareStatLine(statsContainer, "GESAMT", String.format(java.util.Locale.GERMAN, "%.0f kg", totalVolume), 60, true);
        addShareStatLine(statsContainer, "", totalSets + " Sätze", 40, false);
        
        // Spacer
        android.view.View spacer = new android.view.View(this);
        spacer.setLayoutParams(new android.widget.LinearLayout.LayoutParams(100, 48));
        statsContainer.addView(spacer);

        // Add Top Muscles (limit to 4 to fit)
        int count = 0;
        for (fm.mrc.gymlog.data.MuscleStats s : stats) {
            if (count >= 5) break; 
            String mName = translateMuscleGroup(s.muscleGroup);
            addShareStatLine(statsContainer, mName.toUpperCase(), String.format(java.util.Locale.GERMAN, "%d Sätze • %.0f kg", s.totalSets, s.totalVolume), 40, false);
            count++;
        }
        
        // Set Background based on settings
        android.content.SharedPreferences prefs = getSharedPreferences(SettingsActivity.PREFS_NAME, MODE_PRIVATE);
        String bgType = prefs.getString(SettingsActivity.KEY_BACKGROUND_TYPE, "charcoal");
        int bgResId = R.drawable.bg_gradient_midnight; // Default nice Look
        switch (bgType) {
            case "sunrise": bgResId = R.drawable.bg_gradient_sunrise; break;
            case "black": 
                view.setBackgroundColor(android.graphics.Color.BLACK); 
                bgResId = 0;
                break;
            case "charcoal": 
            default:
                // Keep default or set specific
                break;
        }
        if (bgResId != 0) {
            view.setBackgroundResource(bgResId);
        }
        
        // Measure and Layout
        int width = 1080;
        int height = 1350; // 4:5 ratio
        view.measure(android.view.View.MeasureSpec.makeMeasureSpec(width, android.view.View.MeasureSpec.EXACTLY),
                     android.view.View.MeasureSpec.makeMeasureSpec(height, android.view.View.MeasureSpec.EXACTLY));
        view.layout(0, 0, width, height);
        
        // Draw to Bitmap
        android.graphics.Bitmap bitmap = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888);
        android.graphics.Canvas canvas = new android.graphics.Canvas(bitmap);
        view.draw(canvas);
        
        // Save and Share
        shareBitmap(bitmap);
    }

    private void addShareStatLine(android.widget.LinearLayout container, String left, String right, int textSizePx, boolean bold) {
        android.widget.LinearLayout line = new android.widget.LinearLayout(this);
        line.setOrientation(android.widget.LinearLayout.HORIZONTAL);
        line.setLayoutParams(new android.widget.LinearLayout.LayoutParams(android.widget.LinearLayout.LayoutParams.MATCH_PARENT, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT));
        line.setPadding(0, 8, 0, 8);
        
        TextView tLeft = new TextView(this);
        tLeft.setText(left);
        tLeft.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, textSizePx);
        tLeft.setTextColor(android.graphics.Color.WHITE);
        if (bold) tLeft.setTypeface(null, android.graphics.Typeface.BOLD);
        tLeft.setLayoutParams(new android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        tLeft.setGravity(android.view.Gravity.START);
        
        TextView tRight = new TextView(this);
        tRight.setText(right);
        tRight.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, textSizePx);
        tRight.setTextColor(android.graphics.Color.LTGRAY);
        if (bold) {
            tRight.setTextColor(android.graphics.Color.WHITE);
            tRight.setTypeface(null, android.graphics.Typeface.BOLD);
        }
        tRight.setLayoutParams(new android.widget.LinearLayout.LayoutParams(android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT));
        tRight.setGravity(android.view.Gravity.END);
        
        line.addView(tLeft);
        line.addView(tRight);
        container.addView(line);
    }

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
    
    private void shareBitmap(android.graphics.Bitmap bitmap) {
        try {
            java.io.File cachePath = new java.io.File(getCacheDir(), "images");
            cachePath.mkdirs();
            java.io.File stream = new java.io.File(cachePath, "workout_share.png"); // Overwrite same file to save space
            java.io.FileOutputStream streamOut = new java.io.FileOutputStream(stream);
            bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, streamOut);
            streamOut.close();
            
            android.net.Uri contentUri = androidx.core.content.FileProvider.getUriForFile(this, "fm.mrc.gymlog.fileprovider", stream);
            
            if (contentUri != null) {
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                shareIntent.setDataAndType(contentUri, getContentResolver().getType(contentUri));
                shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                startActivity(Intent.createChooser(shareIntent, "Teilen mit..."));
            }
        } catch (java.io.IOException e) {
            e.printStackTrace();
            android.widget.Toast.makeText(this, "Fehler beim Erstellen des Bildes", android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    private void updateList(List<ExerciseWithLastLog> exercises) {
        if (exercises == null) return;
        java.util.Map<String, Integer> dayOrder = new java.util.HashMap<>();
        dayOrder.put("Montag", 1);
        dayOrder.put("Dienstag", 2);
        dayOrder.put("Mittwoch", 3);
        dayOrder.put("Donnerstag", 4);
        dayOrder.put("Freitag", 5);
        dayOrder.put("Samstag", 6);
        dayOrder.put("Sonntag", 7);
        java.util.Collections.sort(exercises, (e1, e2) -> {
            String d1 = e1.getExercise().day != null ? e1.getExercise().day : "";
            String d2 = e2.getExercise().day != null ? e2.getExercise().day : "";
            int o1 = dayOrder.getOrDefault(d1, 99);
            int o2 = dayOrder.getOrDefault(d2, 99);
            int orderResult = Integer.compare(o1, o2);
            if (orderResult == 0) {
                 return Integer.compare(e1.getExercise().sortOrder, e2.getExercise().sortOrder);
            }
            return orderResult;
        });
        java.util.List<Object> items = new java.util.ArrayList<>();
        String currentDay = null;
        for (ExerciseWithLastLog item : exercises) {
            String dayKey = item.getExercise().day;
            if (dayKey == null || dayKey.isEmpty()) dayKey = "Ohne Tag";
            if (!dayKey.equals(currentDay)) {
                items.add(getDayDisplayString(dayKey));
                currentDay = dayKey;
            }
            items.add(item);
        }
        adapter.setItems(items);
    }

    private String getDayDisplayString(String dayKey) {
        switch (dayKey) {
            case "Montag": return getString(R.string.day_monday);
            case "Dienstag": return getString(R.string.day_tuesday);
            case "Mittwoch": return getString(R.string.day_wednesday);
            case "Donnerstag": return getString(R.string.day_thursday);
            case "Freitag": return getString(R.string.day_friday);
            case "Samstag": return getString(R.string.day_saturday);
            case "Sonntag": return getString(R.string.day_sunday);
            default: return getString(R.string.day_unknown);
        }
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

    @Override
    public void onEditExercise(Exercise exercise) {
        showEditExerciseDialog(exercise);
    }

    @Override
    public void onNoteClick(Exercise exercise) {
        showNoteDialog(exercise);
    }

    private void showNoteDialog(Exercise exercise) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.edit_note) + ": " + exercise.name);
        final android.widget.EditText input = new android.widget.EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        input.setMinLines(3);
        input.setGravity(android.view.Gravity.TOP | android.view.Gravity.START);
        input.setText(exercise.notes != null ? exercise.notes : "");
        input.setHint(getString(R.string.hint_note));
        android.widget.FrameLayout container = new android.widget.FrameLayout(this);
        android.widget.FrameLayout.LayoutParams params = new android.widget.FrameLayout.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = 50;
        params.rightMargin = 50;
        input.setLayoutParams(params);
        container.addView(input);
        builder.setView(container);
        builder.setPositiveButton(getString(R.string.save), (dialog, which) -> {
            exercise.notes = input.getText().toString().trim();
            new Thread(() -> db.exerciseDao().update(exercise)).start();
            android.widget.Toast.makeText(this, getString(R.string.msg_saved), android.widget.Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.cancel());
        builder.show();
    }
}