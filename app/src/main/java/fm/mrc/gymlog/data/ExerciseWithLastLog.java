package fm.mrc.gymlog.data;

import androidx.room.Embedded;
import androidx.room.ColumnInfo;
import java.util.List;

// Hilfsklasse für die Room-Relation: Eine Übung und ihr letzter Log-Eintrag.
// Die Felder des Log-Eintrags werden direkt über die SQL-Abfrage in ExerciseDao
// mit Präfixen befüllt, da eine Room-Relation hierfür nicht optimal ist.
public class ExerciseWithLastLog {
    @Embedded
    public Exercise exercise;

    // Felder, die durch die JOIN-Abfrage in ExerciseDao befüllt werden:
    @ColumnInfo(name = "lastLogTimestamp")
    public Long lastLogTimestamp;

    @ColumnInfo(name = "lastLogSets")
    public Integer lastLogSets;

    @ColumnInfo(name = "lastLogReps")
    public Integer lastLogReps;

    @ColumnInfo(name = "lastLogWeight")
    public Double lastLogWeight;

    public Exercise getExercise() { return exercise; }

    // Hilfsmethode, um die Log-Daten als LogEntry-Objekt zurückzugeben
    public LogEntry getLastLogEntry() {
        if (lastLogTimestamp == null) {
            return null;
        }
        return new LogEntry(
                exercise.exerciseId,
                lastLogTimestamp,
                lastLogSets != null ? lastLogSets : 0,
                lastLogReps != null ? lastLogReps : 0,
                lastLogWeight != null ? lastLogWeight : 0.0,
                0);
    }
}