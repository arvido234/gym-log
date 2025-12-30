package fm.mrc.gymlog;

import androidx.room.Embedded;
import androidx.room.Relation;
import androidx.room.Junction;
import java.util.List;

import fm.mrc.gymlog.data.WorkoutDay;
import fm.mrc.gymlog.data.Exercise;
import fm.mrc.gymlog.data.DayExerciseCrossRef; // Wichtig: Paket korrigiert

public class WorkoutDayWithExercises {
    @Embedded
    public WorkoutDay workoutDay;

    @Relation(
            parentColumn = "dayId",
            entity = Exercise.class,
            entityColumn = "exerciseId",
            associateBy = @Junction(value = DayExerciseCrossRef.class, // Wichtig: Symbol gefunden
                    parentColumn = "dayId",
                    entityColumn = "exerciseId")
    )
    public List<Exercise> exercises; // Wichtig: Symbol gefunden

    public List<Exercise> getExercises() { return exercises; }
}