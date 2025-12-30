package fm.mrc.gymlog;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import fm.mrc.gymlog.data.AppDatabase;
import fm.mrc.gymlog.data.LogEntry;
import fm.mrc.gymlog.data.LogEntryDao;

public class HistoryActivity extends AppCompatActivity implements HistoryAdapter.LogCallbacks {

    private HistoryAdapter historyAdapter;
    private List<LogEntry> allLogEntries = new ArrayList<>();
    private LogEntryDao logEntryDao;
    private long exerciseId;
    private String exerciseName;
    private LineChart chart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        exerciseId = getIntent().getLongExtra("exerciseId", -1);
        exerciseName = getIntent().getStringExtra("exerciseName");
        
        if (exerciseId == -1) {
            finish();
            return;
        }

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar_history);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        chart = findViewById(R.id.chart_history);
        setupChart();

        RecyclerView recyclerView = findViewById(R.id.recycler_view_history);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        historyAdapter = new HistoryAdapter(allLogEntries, this);
        recyclerView.setAdapter(historyAdapter);

        logEntryDao = AppDatabase.getInstance(this).logEntryDao();

        logEntryDao.getLogEntriesByExerciseId(exerciseId).observe(this, logEntries -> {
            allLogEntries = logEntries;
            historyAdapter.setLogEntries(logEntries);
            updateChart(logEntries);
        });

        FloatingActionButton fab = findViewById(R.id.fab_add_log);
        fab.setOnClickListener(v -> showAddLogDialog());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void setupChart() {
        chart.getDescription().setEnabled(false);
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setPinchZoom(true);
        chart.getLegend().setTextColor(Color.WHITE);
        
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setValueFormatter(new ValueFormatter() {
            private final SimpleDateFormat mFormat = new SimpleDateFormat("dd.MM", Locale.getDefault());
            @Override
            public String getFormattedValue(float value) {
                return mFormat.format(new Date((long) value));
            }
        });
        
        chart.getAxisLeft().setTextColor(Color.WHITE);
        chart.getAxisRight().setEnabled(false);
    }

    private void updateChart(List<LogEntry> logEntries) {
        if (logEntries == null || logEntries.isEmpty()) {
            chart.clear();
            return;
        }

        List<Entry> entries = new ArrayList<>();
        // Reversing list for chart chronologically (Adapter shows newest first)
        List<LogEntry> chartData = new ArrayList<>(logEntries);
        Collections.reverse(chartData);

        for (int i = 0; i < chartData.size(); i++) {
            LogEntry entry = chartData.get(i);
            entries.add(new Entry(i, (float) entry.weight));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Gewicht (kg)");
        int electricBlue = Color.parseColor("#2979FF");
        dataSet.setColor(electricBlue);
        dataSet.setValueTextColor(Color.WHITE); 
        dataSet.setCircleColor(electricBlue);
        dataSet.setCircleHoleColor(Color.BLACK);
        dataSet.setLineWidth(3f);
        dataSet.setCircleRadius(5f);
        dataSet.setDrawValues(false); // Clean look, values on tap usually better, or keep true if preferred

        // Update X-Axis formatter to map index -> date
        XAxis xAxis = chart.getXAxis();
        xAxis.setGranularity(1f); // Interval 1
        xAxis.setValueFormatter(new ValueFormatter() {
            private final SimpleDateFormat mFormat = new SimpleDateFormat("dd.MM HH:mm", Locale.getDefault());
            @Override
            public String getFormattedValue(float value) {
                int index = Math.round(value);
                if (index >= 0 && index < chartData.size()) {
                    return mFormat.format(new Date(chartData.get(index).timestamp));
                }
                return "";
            }
        });
        
        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        chart.invalidate();
    }

    private void showAddLogDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Neuer Eintrag: " + (exerciseName != null ? exerciseName : ""));

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 32, 32, 32);
        
        android.content.SharedPreferences prefs = getSharedPreferences(SettingsActivity.PREFS_NAME, MODE_PRIVATE);
        boolean showSets = prefs.getBoolean(SettingsActivity.KEY_FIELD_SETS, true);
        boolean showReps = prefs.getBoolean(SettingsActivity.KEY_FIELD_REPS, true);
        boolean showWeight = prefs.getBoolean(SettingsActivity.KEY_FIELD_WEIGHT, true);
        boolean showRPE = prefs.getBoolean(SettingsActivity.KEY_FIELD_RPE, true);

        final EditText setsInput = new EditText(this);
        setsInput.setHint("Sätze");
        setsInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        if (!showSets) setsInput.setVisibility(android.view.View.GONE);
        layout.addView(setsInput);

        final EditText repsInput = new EditText(this);
        repsInput.setHint("Wiederholungen");
        repsInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        if (!showReps) repsInput.setVisibility(android.view.View.GONE);
        layout.addView(repsInput);

        final EditText weightInput = new EditText(this);
        weightInput.setHint("Gewicht (kg)");
        weightInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        if (!showWeight) weightInput.setVisibility(android.view.View.GONE);
        layout.addView(weightInput);

        final EditText rpeInput = new EditText(this);
        rpeInput.setHint("RPE (1-10)");
        rpeInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        if (!showRPE) rpeInput.setVisibility(android.view.View.GONE);
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
                    Toast.makeText(HistoryActivity.this, "Bitte angezeigte Felder ausfüllen", Toast.LENGTH_SHORT).show();
                    return;
                }

                int sets = (showSets && !setsStr.isEmpty()) ? Integer.parseInt(setsStr) : 0;
                int reps = (showReps && !repsStr.isEmpty()) ? Integer.parseInt(repsStr) : 0;
                double weight = (showWeight && !weightStr.isEmpty()) ? Double.parseDouble(weightStr) : 0.0;
                int rpe = (showRPE && !rpeStr.isEmpty()) ? Integer.parseInt(rpeStr) : 0;

                addLogEntry(new LogEntry(exerciseId, System.currentTimeMillis(), sets, reps, weight, rpe));

            } catch (NumberFormatException e) {
                Toast.makeText(HistoryActivity.this, "Ungültige Eingabe", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Abbrechen", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    public void addLogEntry(LogEntry logEntry) {
        new Thread(() -> logEntryDao.insert(logEntry)).start();
    }

    @Override
    public void onLogEntryDeleted(LogEntry logEntry) {
        new Thread(() -> logEntryDao.delete(logEntry)).start();
    }
}