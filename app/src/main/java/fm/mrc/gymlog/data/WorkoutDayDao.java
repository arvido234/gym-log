package fm.mrc.gymlog.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

import fm.mrc.gymlog.WorkoutDayWithExercises;

@Dao
public interface WorkoutDayDao {
    @Insert
    long insert(WorkoutDay workoutDay);

    @Delete
    void delete(WorkoutDay workoutDay);

    @Insert
    void insertCrossRef(DayExerciseCrossRef crossRef);

    @Transaction
    @Query("SELECT * FROM workout_days WHERE dayId = :dayId")
    LiveData<WorkoutDayWithExercises> getDayWithExercises(long dayId);
}