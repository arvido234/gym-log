package fm.mrc.gymlog.data;

public class MuscleStats {
    public String muscleGroup;
    public int totalSets;
    public double totalVolume;

    public MuscleStats(String muscleGroup, int totalSets, double totalVolume) {
        this.muscleGroup = muscleGroup;
        this.totalSets = totalSets;
        this.totalVolume = totalVolume;
    }
}
