package fm.mrc.gymlog.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;

@Dao
public interface StreakCheckInDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(StreakCheckIn checkIn);

    @Query("DELETE FROM streak_checkins WHERE id = :id")
    void delete(long id);

    @Query("DELETE FROM streak_checkins WHERE timestamp = :timestamp")
    void deleteByTimestamp(long timestamp);

    @Query("SELECT * FROM streak_checkins")
    List<StreakCheckIn> getAll();

    @Query("SELECT timestamp FROM streak_checkins")
    List<Long> getAllCheckInTimestamps();
}
