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
}