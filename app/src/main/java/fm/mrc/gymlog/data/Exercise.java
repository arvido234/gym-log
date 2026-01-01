package fm.mrc.gymlog.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "exercises")
public class Exercise {
    @PrimaryKey(autoGenerate = true)
    public long exerciseId;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "description")
    public String description;

    @ColumnInfo(name = "day")
    public String day;

    @ColumnInfo(name = "sort_order")
    public int sortOrder;

    @ColumnInfo(name = "notes")
    public String notes;

    @ColumnInfo(name = "muscle_group")
    public String muscleGroup;

    @ColumnInfo(name = "rest_timer_seconds")
    public int restTimerSeconds;

    public Exercise(String name, String description, String day) {
        this.name = name;
        this.description = description;
        this.day = day;
        this.sortOrder = 0;
        this.notes = "";
        this.muscleGroup = "";
        this.restTimerSeconds = 90; // Default 90 seconds
    }
}