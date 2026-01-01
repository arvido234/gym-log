package fm.mrc.gymlog.data;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

// ... (alle anderen Imports)

@Database(entities = {Exercise.class, LogEntry.class, WorkoutDay.class, DayExerciseCrossRef.class, StreakCheckIn.class}, version = 10, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase instance;

    public abstract ExerciseDao exerciseDao();
    public abstract LogEntryDao logEntryDao();
    public abstract WorkoutDayDao workoutDayDao();
    public abstract StreakCheckInDao streakCheckInDao();

    static final androidx.room.migration.Migration MIGRATION_9_10 = new androidx.room.migration.Migration(9, 10) {
        @Override
        public void migrate(@androidx.annotation.NonNull androidx.sqlite.db.SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE exercises ADD COLUMN rest_timer_seconds INTEGER NOT NULL DEFAULT 90");
        }
    };

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "gymlog_database")
                    .addMigrations(MIGRATION_9_10) // Add migration
                    .fallbackToDestructiveMigration() // Keep fallback just in case
                    .build();
        }
        return instance;
    }
}