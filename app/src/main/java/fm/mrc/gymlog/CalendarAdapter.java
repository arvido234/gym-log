package fm.mrc.gymlog;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Calendar;
import java.util.List;
import java.util.Set;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.DayViewHolder> {

    private final List<Long> days;
    private final Set<String> logDays;
    private final Set<String> checkInDays;
    private final OnDayClickListener listener;
    private int selectedPosition = -1;
    
    private java.util.Map<String, Double> volumeMap;
    private double maxVolume = 1.0;

    public interface OnDayClickListener {
        void onDayClick(long timestamp);
    }

    public CalendarAdapter(List<Long> days, Set<String> logDays, Set<String> checkInDays, OnDayClickListener listener) {
        this.days = days;
        this.logDays = logDays;
        this.checkInDays = checkInDays;
        this.listener = listener;
    }
    
    public void setVolumeData(java.util.Map<String, Double> volumeMap, double maxVolume) {
        this.volumeMap = volumeMap;
        this.maxVolume = maxVolume > 0 ? maxVolume : 1.0;
        notifyDataSetChanged();
    }

    @Override
    public DayViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_calendar_day, parent, false);
        return new DayViewHolder(v);
    }

    @Override
    public void onBindViewHolder(DayViewHolder holder, int position) {
        Long ts = days.get(position);
        
        if (ts == 0) {
            holder.textDay.setText("");
            holder.iconStatus.setVisibility(View.GONE);
            holder.itemView.setOnClickListener(null);
            holder.itemView.setBackground(null);
            return;
        }

        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(ts);
        holder.textDay.setText(String.valueOf(c.get(Calendar.DAY_OF_MONTH)));

        String key = c.get(Calendar.YEAR) + "-" + c.get(Calendar.MONTH) + "-" + c.get(Calendar.DAY_OF_MONTH);
        
        // Reset state
        holder.iconStatus.setVisibility(View.GONE);
        holder.textDay.setTypeface(null, android.graphics.Typeface.NORMAL);
        holder.textDay.setTextColor(holder.itemView.getContext().getColor(android.R.color.tab_indicator_text)); // Default text color assumption
        holder.itemView.setBackgroundResource(android.R.color.transparent);

        // Heatmap Logic for Log Days
        if (volumeMap != null && volumeMap.containsKey(key)) {
            double vol = volumeMap.get(key);
            float ratio = (float) (vol / maxVolume);
            // Cap ratio
            if (ratio > 1.0f) ratio = 1.0f;
            if (ratio < 0.1f) ratio = 0.1f; // Minimum visibility if it exists

            // Calculate color intensity (Green base #4CAF50 is standard, simpler is to tint the drawable)
            // Or we can use the Primary Color of volume?
            // Let's use a solid shape and change alpha
            
            android.graphics.drawable.Drawable heatmapBg = holder.itemView.getContext().getDrawable(R.drawable.bg_heatmap_day).mutate();
            // Alpha 0-255. Min 50, Max 255.
            int alpha = 50 + (int)(205 * ratio);
            heatmapBg.setAlpha(alpha);
            
            // If selected, we might want to override later, or blend? 
            // Current selection logic overrides background. Let's keep selection as dominant.
            holder.itemView.setBackground(heatmapBg);
            holder.textDay.setTextColor(android.graphics.Color.WHITE); // Better contrast on color
        }

        // Manual Check-In (Priority over heatmap visual or addition?)
        // If manual check-in, we just show the check icon. 
        if (checkInDays.contains(key)) {
            holder.iconStatus.setImageResource(R.drawable.ic_check_circle);
            holder.iconStatus.setVisibility(View.VISIBLE);
        } else if (logDays.contains(key)) {
             // For logs, we now have heatmap. Do we still want the dot?
             // Maybe remove dot if we have heatmap, or keep it as indicator of specific event.
             // User asked for "Heatmap ... similar to GitHub". GitHub just colors the square.
             // So we minimize the dot if we have background color, OR we keep dot for "Low Volume" consistency?
             // Let's hide the dot if we have volume map, to look cleaner.
             if (volumeMap == null) {
                 holder.iconStatus.setImageResource(R.drawable.ic_workout_dot);
                 holder.iconStatus.setVisibility(View.VISIBLE);
             }
        }

        // Selection (Must be last to override)
        if (position == selectedPosition) {
             holder.itemView.setBackgroundResource(R.drawable.bg_selected_day); 
             holder.textDay.setTypeface(null, android.graphics.Typeface.BOLD);
             holder.textDay.setTextColor(android.graphics.Color.WHITE);
        }

        holder.itemView.setOnClickListener(v -> {
            int old = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(old);
            notifyItemChanged(selectedPosition);
            listener.onDayClick(ts);
        });
    }

    @Override
    public int getItemCount() {
        return days.size();
    }
    
    public void setSelectedTimestamp(long ts) {
        for (int i=0; i<days.size(); i++) {
            if (days.get(i) == ts) {
                int old = selectedPosition;
                selectedPosition = i;
                notifyItemChanged(old);
                notifyItemChanged(selectedPosition);
                return;
            }
        }
    }

    static class DayViewHolder extends RecyclerView.ViewHolder {
        TextView textDay;
        ImageView iconStatus;

        DayViewHolder(View itemView) {
            super(itemView);
            textDay = itemView.findViewById(R.id.text_day);
            iconStatus = itemView.findViewById(R.id.icon_status);
        }
    }
}
