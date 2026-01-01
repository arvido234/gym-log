package fm.mrc.gymlog.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Query;
import androidx.room.Insert;
import androidx.room.Update;
import java.util.List;

import fm.mrc.gymlog.ExerciseWithLogEntries;
import fm.mrc.gymlog.data.Exercise;
import fm.mrc.gymlog.data.LogEntry;
import fm.mrc.gymlog.data.ExerciseWithLastLog;
import androidx.room.RoomWarnings;

@Dao
public interface ExerciseDao {
    @Insert
    long insert(Exercise exercise);

    @Update
    void update(Exercise exercise);

    @Delete
    void delete(Exercise exercise);

    @Query("SELECT * FROM exercises ORDER BY name ASC")
    LiveData<List<Exercise>> getAllExercises();

    @Query("SELECT * FROM exercises ORDER BY name ASC")
    List<Exercise> getAllExercisesSync();

    @Query("SELECT * FROM exercises WHERE exerciseId = :exerciseId")
    LiveData<Exercise> getExerciseById(long exerciseId);

    // SQL-Abfrage korrigiert: Führt einen LEFT JOIN aus, um nur den aktuellsten
    // Log-Eintrag zu jeder Übung abzurufen. (Behebt den Room Query Fehler)
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("SELECT e.*, " +
            "t1.timestamp AS lastLogTimestamp, " +
            "t1.sets AS lastLogSets, " +
            "t1.reps AS lastLogReps, " +
            "t1.weight AS lastLogWeight " +
            "FROM exercises e " +
            "LEFT JOIN log_entries t1 ON t1.logEntryId = ( " +
            "SELECT t2.logEntryId FROM log_entries t2 " +
            "WHERE t2.exerciseId = e.exerciseId " +
            "ORDER BY t2.timestamp DESC LIMIT 1" +
            ") " +
            "ORDER BY e.name ASC")
    LiveData<List<ExerciseWithLastLog>> getAllExercisesWithLastLog();

    @Query("SELECT * FROM log_entries WHERE exerciseId = :exerciseId ORDER BY timestamp DESC")
    LiveData<List<LogEntry>> getLogEntriesForExercise(long exerciseId);
}