package fm.mrc.gymlog.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

public class StreakCalculator {

    /**
     * Calculates the weekly streak based on workout logs and manual check-ins.
     * The streak increments only if the user has trained/checked-in on at least 'targetFrequency' unique days in a week.
     *
     * @param logTimestamps      List of timestamps from LogEntry
     * @param checkInTimestamps  List of timestamps from StreakCheckIn
     * @param targetFrequency    Number of days per week required to keep the streak (e.g. 1, 3, 5)
     * @return current streak in weeks
     */
    public static int calculateWeeklyStreak(List<Long> logTimestamps, List<Long> checkInTimestamps, int targetFrequency) {
        List<Long> allTimestamps = new ArrayList<>();
        if (logTimestamps != null) allTimestamps.addAll(logTimestamps);
        if (checkInTimestamps != null) allTimestamps.addAll(checkInTimestamps);

        if (allTimestamps.isEmpty()) {
            return 0;
        }

        // Map: Key = "Year-Week", Value = Set of unique "DayOfYear" (or full date string) to count distinct days
        Map<String, Set<Integer>> weeklyActivity = new HashMap<>();

        Calendar cal = Calendar.getInstance(TimeZone.getDefault());
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        cal.setMinimalDaysInFirstWeek(4); 

        for (Long ts : allTimestamps) {
            cal.setTimeInMillis(ts);
            int year = cal.get(Calendar.YEAR);
            int week = cal.get(Calendar.WEEK_OF_YEAR);
            int dayOfYear = cal.get(Calendar.DAY_OF_YEAR);

            // Year adjustment for ISO weeks
            if (cal.get(Calendar.MONTH) == Calendar.DECEMBER && week == 1) {
                year++;
            } else if (cal.get(Calendar.MONTH) == Calendar.JANUARY && week >= 52) {
                year--;
            }
            
            String weekKey = year + "-" + week;
            
            if (!weeklyActivity.containsKey(weekKey)) {
                weeklyActivity.put(weekKey, new HashSet<>());
            }
            weeklyActivity.get(weekKey).add(year * 1000 + dayOfYear); // Unique Day Identifier
        }

        // --- Iteration logic ---
        cal.setTimeInMillis(System.currentTimeMillis());
        int currentYear = cal.get(Calendar.YEAR);
        int currentWeek = cal.get(Calendar.WEEK_OF_YEAR);
        
        // Adjustment for current date
        if (cal.get(Calendar.MONTH) == Calendar.DECEMBER && currentWeek == 1) {
            currentYear++;
        } else if (cal.get(Calendar.MONTH) == Calendar.JANUARY && currentWeek >= 52) {
            currentYear--;
        }

        int streak = 0;
        String currentKey = currentYear + "-" + currentWeek;
        
        boolean currentWeekMet = false;
        if (weeklyActivity.containsKey(currentKey)) {
            if (weeklyActivity.get(currentKey).size() >= targetFrequency) {
                streak++;
                currentWeekMet = true;
            }
        }

        // If current week is NOT met, we check if the streak is still "alive" from last week.
        // If last week met target, then streak is valid (just doesn't include current week yet).
        // If last week failed, and current week failed, streak is 0.
        
        // Helper to get previous week key
        int checkYear = currentYear;
        int checkWeek = currentWeek;

        // Start checking from Previous Week
        // If current week WAS met, we just continue back.
        // If current week was NOT met, we still continue back, and if prev week is good, we count it.
        
        // Loop backwards
        boolean streakDiffers = false; // To handle the gap
        
        // We always loop back one by one
        while (true) {
            // Move back 1 week
            // Simple logic: modify cal object to stay consistent with Calendar logic
            cal.add(Calendar.WEEK_OF_YEAR, -1);
            int y = cal.get(Calendar.YEAR);
            int w = cal.get(Calendar.WEEK_OF_YEAR);
             if (cal.get(Calendar.MONTH) == Calendar.DECEMBER && w == 1) {
                y++;
            } else if (cal.get(Calendar.MONTH) == Calendar.JANUARY && w >= 52) {
                y--;
            }
            
            String key = y + "-" + w;
            int count = 0;
            if (weeklyActivity.containsKey(key)) {
                count = weeklyActivity.get(key).size();
            }

            if (count >= targetFrequency) {
                streak++;
            } else {
                // Determine break condition
                // If we are just checking the week immediately before current...
                // And current was NOT met..
                // And this one is NOT met -> Streak 0.
                
                // If current WAS met, and this one NOT met -> Streak ends (is just 1).
                break;
            }

            if (streak > 5000) break; 
        }

        return streak;
    }
}
