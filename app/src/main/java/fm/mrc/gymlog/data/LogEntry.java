package fm.mrc.gymlog.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
import androidx.room.Index;

import static androidx.room.ForeignKey.CASCADE;

@Entity(tableName = "log_entries",
        indices = {@Index(value = {"exerciseId"})}, // Index hinzugefügt (behebt Warnung)
        foreignKeys = @ForeignKey(entity = Exercise.class,
                parentColumns = "exerciseId",
                childColumns = "exerciseId",
                onDelete = CASCADE))
public class LogEntry {
    @PrimaryKey(autoGenerate = true)
    public long logEntryId;

    @ColumnInfo(name = "exerciseId")
    public long exerciseId;

    @ColumnInfo(name = "timestamp")
    public long timestamp;

    @ColumnInfo(name = "sets")
    public int sets;

    @ColumnInfo(name = "reps")
    public int reps;

    @ColumnInfo(name = "weight")
    public double weight;

    @ColumnInfo(name = "rpe")
    public int rpe;

    public LogEntry(long exerciseId, long timestamp, int sets, int reps, double weight, int rpe) {
        this.exerciseId = exerciseId;
        this.timestamp = timestamp;
        this.sets = sets;
        this.reps = reps;
        this.weight = weight;
        this.rpe = rpe;
    }
}