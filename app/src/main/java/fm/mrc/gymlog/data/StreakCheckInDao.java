package fm.mrc.gymlog.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface StreakCheckInDao {
    @Insert
    void insert(StreakCheckIn checkIn);

    @Delete
    void delete(StreakCheckIn checkIn);

    @Query("SELECT timestamp FROM streak_checkins ORDER BY timestamp DESC")
    List<Long> getAllCheckInTimestamps();
    
    // Delete by timestamp range or exact match might be useful, but for now simple insert/delete
    @Query("DELETE FROM streak_checkins WHERE timestamp = :ts")
    void deleteByTimestamp(long ts);
}
