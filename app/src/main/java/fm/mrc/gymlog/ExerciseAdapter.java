package fm.mrc.gymlog;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import fm.mrc.gymlog.data.Exercise;
import fm.mrc.gymlog.data.ExerciseWithLastLog;
import java.util.List;
import java.util.Locale;

public class ExerciseAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    // Wir nutzen Object, da die Liste Strings (Header) und ExerciseWithLastLog (Items) enthalten kann
    private List<Object> items;
    private final AdapterCallbacks callbacks;

    public interface AdapterCallbacks {
        void onDeleteExercise(Exercise exercise, int position);
        void onEditExercise(Exercise exercise);
        void onExerciseClick(Exercise exercise);
        void onQuickLogClick(Exercise exercise);
        void onHistoryClick(Exercise exercise);
    }

    public ExerciseAdapter(AdapterCallbacks callbacks) {
        this.callbacks = callbacks;
    }

    public void setItems(List<Object> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (items.get(position) instanceof String) {
            return TYPE_HEADER;
        }
        return TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_exercise, parent, false);
            return new ExerciseViewHolder(view);
        }
    }

    private boolean showSubtitle = false;

    public void setShowSubtitle(boolean show) {
        this.showSubtitle = show;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            String title = (String) items.get(position);
            ((HeaderViewHolder) holder).titleTextView.setText(title);
        } else if (holder instanceof ExerciseViewHolder) {
            ExerciseWithLastLog currentItem = (ExerciseWithLastLog) items.get(position);
            Exercise exercise = currentItem.getExercise();
            ExerciseViewHolder exHolder = (ExerciseViewHolder) holder;

            exHolder.nameTextView.setText(exercise.name);
            
            if (showSubtitle && exercise.description != null && !exercise.description.isEmpty()) {
                exHolder.descriptionTextView.setText(exercise.description);
                exHolder.descriptionTextView.setVisibility(View.VISIBLE);
            } else {
                exHolder.descriptionTextView.setVisibility(View.GONE);
            }

            if (currentItem.lastLogWeight != null && currentItem.lastLogWeight > 0) {
                String lastLog = String.format(Locale.getDefault(),
                        "%d x %d @ %.1f kg",
                        currentItem.lastLogSets != null ? currentItem.lastLogSets : 0,
                        currentItem.lastLogReps != null ? currentItem.lastLogReps : 0,
                        currentItem.lastLogWeight);
                exHolder.lastLogTextView.setText(lastLog);
            } else {
                exHolder.lastLogTextView.setText("Noch keine Logs"); // Should localize this too later
            }

            exHolder.optionsButton.setOnClickListener(v -> {
                android.widget.PopupMenu popup = new android.widget.PopupMenu(v.getContext(), v);
                popup.getMenu().add(0, 1, 0, v.getContext().getString(R.string.edit));
                popup.getMenu().add(0, 2, 0, v.getContext().getString(R.string.delete));
                
                popup.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == 1) {
                         if (callbacks != null) callbacks.onEditExercise(exercise);
                         return true;
                    } else if (item.getItemId() == 2) {
                         if (callbacks != null) callbacks.onDeleteExercise(exercise, position);
                         return true;
                    }
                    return false;
                });
                popup.show();
            });

            exHolder.quickLogButton.setOnClickListener(v -> {
                if (callbacks != null) callbacks.onQuickLogClick(exercise);
            });

            exHolder.historyButton.setOnClickListener(v -> {
                if (callbacks != null) callbacks.onHistoryClick(exercise);
            });
            
            // Item click also opens history
             exHolder.itemView.setOnClickListener(v -> {
                if (callbacks != null) callbacks.onExerciseClick(exercise);
            });
        }
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    public static class HeaderViewHolder extends RecyclerView.ViewHolder {
        public TextView titleTextView;
        public HeaderViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.text_view_header_title);
        }
    }

    public static class ExerciseViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTextView;
        public TextView descriptionTextView;
        public TextView lastLogTextView;
        public ImageButton optionsButton;
        public ImageButton quickLogButton;
        public ImageButton historyButton;

        public ExerciseViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.text_view_exercise_name);
            descriptionTextView = itemView.findViewById(R.id.text_view_exercise_description);
            lastLogTextView = itemView.findViewById(R.id.text_view_last_log);
            optionsButton = itemView.findViewById(R.id.button_options);
            quickLogButton = itemView.findViewById(R.id.button_quick_log);
            historyButton = itemView.findViewById(R.id.button_history);
        }
    }
}