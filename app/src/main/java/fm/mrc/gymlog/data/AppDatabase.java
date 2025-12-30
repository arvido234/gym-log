package fm.mrc.gymlog.data;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

// ... (alle anderen Imports)

@Database(entities = {Exercise.class, LogEntry.class, WorkoutDay.class, DayExerciseCrossRef.class}, version = 5, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase instance;

    public abstract ExerciseDao exerciseDao();
    public abstract LogEntryDao logEntryDao();
    public abstract WorkoutDayDao workoutDayDao();

    // Die Methode, die in MainActivity aufgerufen werden muss (Behebt Fehler 4)
    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "gymlog_database")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}