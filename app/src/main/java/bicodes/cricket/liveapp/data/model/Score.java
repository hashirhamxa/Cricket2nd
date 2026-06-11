package bicodes.cricket.liveapp.data.model;

import com.google.gson.annotations.SerializedName;

public class Score {
    @SerializedName("r")
    private int runs;
    
    @SerializedName("w")
    private int wickets;
    
    @SerializedName("o")
    private double overs;
    
    @SerializedName("inning")
    private String inning;

    // Getters and Setters
    public int getRuns() { return runs; }
    public void setRuns(int runs) { this.runs = runs; }

    public int getWickets() { return wickets; }
    public void setWickets(int wickets) { this.wickets = wickets; }

    public double getOvers() { return overs; }
    public void setOvers(double overs) { this.overs = overs; }

    public String getInning() { return inning; }
    public void setInning(String inning) { this.inning = inning; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Score score = (Score) o;
        return runs == score.runs &&
                wickets == score.wickets &&
                Double.compare(score.overs, overs) == 0 &&
                java.util.Objects.equals(inning, score.inning);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(runs, wickets, overs, inning);
    }
}
