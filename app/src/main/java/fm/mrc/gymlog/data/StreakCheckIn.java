package fm.mrc.gymlog.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "streak_checkins")
public class StreakCheckIn {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public long timestamp;
    public String note;

    public StreakCheckIn(long timestamp) {
        this.timestamp = timestamp;
    }
}
