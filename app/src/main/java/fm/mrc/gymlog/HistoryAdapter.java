package fm.mrc.gymlog;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import android.widget.ImageButton;

import fm.mrc.gymlog.data.LogEntry; // <-- Hinzugefügt/Korrigiert

// Adapter für die RecyclerView in der HistoryActivity, die LogEntries anzeigt
public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private List<LogEntry> logEntryList;
    private final LogCallbacks callbacks;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());

    // Schnittstelle für Callbacks (muss LogEntry kennen)
    public interface LogCallbacks {
        void onLogEntryDeleted(LogEntry logEntry); // LogEntry Symbol gefunden
    }

    // Konstruktoren und Setter (müssen LogEntry kennen)
    public HistoryAdapter(List<LogEntry> logEntries) { // LogEntry Symbol gefunden
        this.logEntryList = logEntries;
        this.callbacks = null; // Wenn keine Callbacks benötigt werden
    }

    public HistoryAdapter(List<LogEntry> logEntries, LogCallbacks callbacks) { // LogEntry Symbol gefunden
        this.logEntryList = logEntries;
        this.callbacks = callbacks;
    }

    public void setLogEntries(List<LogEntry> newLogEntries) { // LogEntry Symbol gefunden
        this.logEntryList = newLogEntries;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_log_entry, parent, false);
        return new HistoryViewHolder(view);
    }

    private boolean showSets = true;
    private boolean showReps = true;
    private boolean showWeight = true;
    private boolean showRPE = true;

    public void setFieldVisibility(boolean showSets, boolean showReps, boolean showWeight, boolean showRPE) {
        this.showSets = showSets;
        this.showReps = showReps;
        this.showWeight = showWeight;
        this.showRPE = showRPE;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        LogEntry currentItem = logEntryList.get(position);

        holder.dateTextView.setText(dateFormat.format(new Date(currentItem.timestamp)));

        StringBuilder sb = new StringBuilder();
        
        boolean hasSets = showSets && currentItem.sets > 0;
        boolean hasReps = showReps && currentItem.reps > 0;
        boolean hasWeight = showWeight && currentItem.weight > 0;
        boolean hasRPE = showRPE && currentItem.rpe > 0;

        if (hasSets) {
            sb.append(currentItem.sets);
        }

        if (hasSets && hasReps) {
            sb.append(" x ");
        }

        if (hasReps) {
            sb.append(currentItem.reps).append(" Wdh.");
        }
        
        if (hasWeight) {
            if (sb.length() > 0) sb.append(" @ ");
            sb.append(String.format(Locale.getDefault(), "%.1f kg", currentItem.weight));
        }
        
        if (hasRPE) {
            if (sb.length() > 0) sb.append("  (RPE ");
            else sb.append("RPE ");
            sb.append(currentItem.rpe).append(")");
        }
        
        if (sb.length() == 0) {
             holder.detailsTextView.setText("Gespeichert");
        } else {
             holder.detailsTextView.setText(sb.toString());
        }

        holder.deleteButton.setOnClickListener(v -> {
            if (callbacks != null) {
                callbacks.onLogEntryDeleted(currentItem);
            }
        });
    }

    @Override
    public int getItemCount() {
        return logEntryList.size();
    }

    public static class HistoryViewHolder extends RecyclerView.ViewHolder {
        public TextView dateTextView;
        public TextView detailsTextView;
        public ImageButton deleteButton;

        public HistoryViewHolder(View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.text_view_log_date);
            detailsTextView = itemView.findViewById(R.id.text_view_log_details);
            deleteButton = itemView.findViewById(R.id.button_delete_log);
        }
    }
}