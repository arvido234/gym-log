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
        void onNoteClick(Exercise exercise);
    }

    public ExerciseAdapter(AdapterCallbacks callbacks) {
        this.callbacks = callbacks;
    }

    public void setItems(List<Object> items) {
        this.items = items;
        notifyDataSetChanged();
    }
    
    public Object getItem(int position) {
        if (items != null && position >= 0 && position < items.size()) {
            return items.get(position);
        }
        return null;
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

    private boolean showSets = true;
    private boolean showReps = true;
    private boolean showWeight = true;

    public void setFieldVisibility(boolean showSets, boolean showReps, boolean showWeight) {
        this.showSets = showSets;
        this.showReps = showReps;
        this.showWeight = showWeight;
        notifyDataSetChanged();
    }

    private boolean showNotesFeature = true;
    private boolean showInlineNote = false;
    private boolean glassMode = false;

    public void setNoteSettings(boolean showNotesFeature, boolean showInlineNote) {
        this.showNotesFeature = showNotesFeature;
        this.showInlineNote = showInlineNote;
        notifyDataSetChanged();
    }
    
    public void setGlassMode(boolean glassMode) {
        this.glassMode = glassMode;
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

            // Glass Mode Logic
            if (glassMode) {
                // Semi-transparent surface (Dark Glass)
                ((androidx.cardview.widget.CardView) exHolder.itemView).setCardBackgroundColor(android.graphics.Color.parseColor("#44222222"));
                ((androidx.cardview.widget.CardView) exHolder.itemView).setCardElevation(0);
            } else {
                // Default Surface
                 ((androidx.cardview.widget.CardView) exHolder.itemView).setCardBackgroundColor(exHolder.itemView.getContext().getResources().getColor(R.color.gym_surface));
            }

            exHolder.nameTextView.setText(exercise.name);
            
            if (showSubtitle && exercise.description != null && !exercise.description.isEmpty()) {
                exHolder.descriptionTextView.setText(exercise.description);
                exHolder.descriptionTextView.setVisibility(View.VISIBLE);
            } else {
                exHolder.descriptionTextView.setVisibility(View.GONE);
            }
            
            // Inline Note Logic
            if (showInlineNote && exercise.notes != null && !exercise.notes.isEmpty()) {
                exHolder.noteTextView.setText(exercise.notes);
                exHolder.noteTextView.setVisibility(View.VISIBLE);
            } else {
                exHolder.noteTextView.setVisibility(View.GONE);
            }

            if (currentItem.lastLogWeight != null) { 
                StringBuilder sb = new StringBuilder();
                int sets = currentItem.lastLogSets != null ? currentItem.lastLogSets : 0;
                int reps = currentItem.lastLogReps != null ? currentItem.lastLogReps : 0;
                double weight = currentItem.lastLogWeight;

                boolean hasSets = showSets && sets > 0;
                boolean hasReps = showReps && reps > 0;
                boolean hasWeight = showWeight && weight > 0;

                if (hasSets) {
                    sb.append(sets);
                }

                if (hasSets && hasReps) {
                    sb.append(" x ");
                }
                
                if (hasReps) {
                    sb.append(reps);
                }
                
                if (hasWeight) {
                    if (sb.length() > 0) sb.append(" @ ");
                    sb.append(String.format(Locale.getDefault(), "%.1f kg", weight));
                }
                
                if (sb.length() == 0) {
                     // If all values are 0 or hidden (e.g. just a log entry with 0s), show Saved
                     exHolder.lastLogTextView.setText(R.string.msg_saved);
                } else {
                     exHolder.lastLogTextView.setText(sb.toString());
                }
            } else {
                exHolder.lastLogTextView.setText("Noch keine Logs"); 
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
            

            // Note button Logic
            if (showNotesFeature) {
                exHolder.noteButton.setVisibility(View.VISIBLE);
                if (exercise.notes != null && !exercise.notes.isEmpty()) {
                     // Highlight if note exists
                     exHolder.noteButton.setColorFilter(android.graphics.Color.WHITE); 
                } else {
                     // Dim/Normal if empty
                     exHolder.noteButton.setColorFilter(androidx.core.content.ContextCompat.getColor(exHolder.itemView.getContext(), R.color.text_secondary)); 
                }
    
                exHolder.noteButton.setOnClickListener(v -> {
                    if (callbacks != null) callbacks.onNoteClick(exercise);
                });
            } else {
                exHolder.noteButton.setVisibility(View.GONE);
            }

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
        public TextView noteTextView;
        public TextView lastLogTextView;
        public ImageButton optionsButton;
        public ImageButton quickLogButton;
        public ImageButton historyButton;
        public ImageButton noteButton;

        public ExerciseViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.text_view_exercise_name);
            descriptionTextView = itemView.findViewById(R.id.text_view_exercise_description);
            noteTextView = itemView.findViewById(R.id.text_view_exercise_note);
            lastLogTextView = itemView.findViewById(R.id.text_view_last_log);
            optionsButton = itemView.findViewById(R.id.button_options);
            quickLogButton = itemView.findViewById(R.id.button_quick_log);
            historyButton = itemView.findViewById(R.id.button_history);
            noteButton = itemView.findViewById(R.id.button_note);
        }
    }
}