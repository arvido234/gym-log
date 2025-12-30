package fm.mrc.gymlog;

import androidx.room.Embedded;
import androidx.room.Relation;
import androidx.room.Junction;
import java.util.List;

import fm.mrc.gymlog.data.WorkoutDay;
import fm.mrc.gymlog.data.Exercise; // Import korrigiert (behebt Symbol-Fehler)
import fm.mrc.gymlog.data.DayExerciseCrossRef; // Import korrigiert (behebt Symbol-Fehler)

public class DayWithExercises {
    @Embedded
    public WorkoutDay workoutDay;

    @Relation(
            parentColumn = "dayId",
            entity = Exercise.class,
            entityColumn = "exerciseId",
            associateBy = @Junction(value = DayExerciseCrossRef.class,
                    parentColumn = "dayId",
                    entityColumn = "exerciseId")
    )
    public List<Exercise> exercises;

    public List<Exercise> getExercises() { return exercises; }
}