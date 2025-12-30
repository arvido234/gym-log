package fm.mrc.gymlog;

import androidx.room.Embedded;
import androidx.room.Relation;
import java.util.List;

import fm.mrc.gymlog.data.Exercise;
import fm.mrc.gymlog.data.LogEntry; // <-- Hinzugefügt/Korrigiert

// Hilfsklasse für die Room-Relation: Eine Übung und alle zugehörigen Log-Einträge
public class ExerciseWithLogEntries {
    @Embedded
    public Exercise exercise;

    @Relation(
            parentColumn = "exerciseId",
            entityColumn = "exerciseId"
    )
    public List<LogEntry> logEntries;

    public Exercise getExercise() { return exercise; }
    public List<LogEntry> getLogEntries() { return logEntries; }
}