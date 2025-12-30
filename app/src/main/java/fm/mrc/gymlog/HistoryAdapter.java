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

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        LogEntry currentItem = logEntryList.get(position);

        holder.dateTextView.setText(dateFormat.format(new Date(currentItem.timestamp)));

        String details = String.format(Locale.getDefault(), "%d Sätze x %d Wdh. @ %.1f kg (RPE %d)",
                currentItem.sets, currentItem.reps, currentItem.weight, currentItem.rpe);
        holder.detailsTextView.setText(details);

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