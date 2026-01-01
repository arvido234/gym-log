package fm.mrc.gymlog.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import java.util.List;
import fm.mrc.gymlog.MainActivity;
import fm.mrc.gymlog.R;
import fm.mrc.gymlog.data.AppDatabase;
import fm.mrc.gymlog.SettingsActivity;
import fm.mrc.gymlog.util.StreakCalculator;

public class StreakWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // Update all widgets
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        // Run database query in background
        new Thread(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(context);
                List<Long> logDates = db.logEntryDao().getDistinctTimestamps();
                List<Long> checkDates = db.streakCheckInDao().getAllCheckInTimestamps();
                
                android.content.SharedPreferences prefs = context.getSharedPreferences(SettingsActivity.PREFS_NAME, Context.MODE_PRIVATE);
                int targetFreq = prefs.getInt(SettingsActivity.KEY_STREAK_FREQUENCY, 1);
                
                int streak = StreakCalculator.calculateWeeklyStreak(logDates, checkDates, targetFreq);

                // Prepare view
                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_streak);
                views.setTextViewText(R.id.widget_streak_count, String.valueOf(streak));
                views.setTextViewText(R.id.widget_streak_label, context.getString(R.string.text_streak_message, streak).replace(streak + " ", ""));
                
                // Fallback label logic simply "Weeks" or "Wochen" if string is complex, but for now standard:
                views.setTextViewText(R.id.widget_streak_label, "WEEK STREAK");

                // Click handler -> Open App
                Intent intent = new Intent(context, MainActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                views.setOnClickPendingIntent(R.id.widget_streak_count, pendingIntent);
                views.setOnClickPendingIntent(R.id.widget_streak_label, pendingIntent);
                
                // Update
                appWidgetManager.updateAppWidget(appWidgetId, views);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
