package fm.mrc.gymlog.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "workout_days")
public class WorkoutDay {
    @PrimaryKey(autoGenerate = true)
    public long dayId;

    @ColumnInfo(name = "date")
    public long date;

    @ColumnInfo(name = "name")
    public String name;

    public WorkoutDay(long date, String name) {
        this.date = date;
        this.name = name;
    }
}