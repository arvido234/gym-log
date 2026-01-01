package fm.mrc.gymlog.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface LogEntryDao {
    @Insert
    long insert(LogEntry logEntry);

    @Update
    void update(LogEntry logEntry);

    @Delete
    void delete(LogEntry logEntry);

    @Query("SELECT * FROM log_entries WHERE exerciseId = :exerciseId ORDER BY timestamp DESC")
    LiveData<List<LogEntry>> getLogEntriesByExerciseId(long exerciseId);

    @Query("SELECT * FROM log_entries")
    List<LogEntry> getAllLogEntries();

    @Query("SELECT e.muscle_group as muscleGroup, SUM(l.sets) as totalSets " +
           "FROM log_entries l " +
           "JOIN exercises e ON l.exerciseId = e.exerciseId " +
           "WHERE l.timestamp >= :startTime " +
           "GROUP BY e.muscle_group " +
           "ORDER BY totalSets DESC")
    List<MuscleStats> getWeeklyMuscleStats(long startTime);
    @Query("SELECT DISTINCT timestamp FROM log_entries ORDER BY timestamp DESC")
    List<Long> getDistinctTimestamps();
}