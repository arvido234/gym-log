package fm.mrc.gymlog.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;



@Dao
public interface DayDao {

    // Fügt einen Wochentag hinzu, ignoriert ihn, wenn er schon existiert
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertDay(WorkoutDay day);

    // Ruft alle Wochentage ab (zum Anzeigen in einer Liste)
    @Query("SELECT * FROM workout_day ORDER BY dayId ASC")
    List<WorkoutDay> getAllDays();
}