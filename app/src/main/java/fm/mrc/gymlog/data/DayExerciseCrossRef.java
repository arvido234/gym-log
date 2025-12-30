package fm.mrc.gymlog.data;

import androidx.room.Entity;
import androidx.room.Index; // Import für Index

@Entity(primaryKeys = {"dayId", "exerciseId"},
        indices = {@Index(value = {"exerciseId"})}) // <-- Warnung behoben
public class DayExerciseCrossRef {
    public long dayId;
    public long exerciseId;

    public DayExerciseCrossRef(long dayId, long exerciseId) {
        this.dayId = dayId;
        this.exerciseId = exerciseId;
    }
}