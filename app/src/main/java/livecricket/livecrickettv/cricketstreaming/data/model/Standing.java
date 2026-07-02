package livecricket.livecrickettv.cricketstreaming.data.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

@Entity(tableName = "standings")
public class Standing {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private String seriesId;

    @SerializedName("teamname")
    private String teamName;

    @SerializedName("m")
    private int played;

    @SerializedName("w")
    private int won;

    @SerializedName("l")
    private int lost;

    @SerializedName("t")
    private int tied;

    @SerializedName("nr")
    private int noResult;

    @SerializedName("pts")
    private int points;

    @SerializedName("nrr")
    private double nrr;

    private int position;

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getSeriesId() { return seriesId; }
    public void setSeriesId(String seriesId) { this.seriesId = seriesId; }

    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }

    public int getPlayed() { return played; }
    public void setPlayed(int played) { this.played = played; }

    public int getWon() { return won; }
    public void setWon(int won) { this.won = won; }

    public int getLost() { return lost; }
    public void setLost(int lost) { this.lost = lost; }

    public int getTied() { return tied; }
    public void setTied(int tied) { this.tied = tied; }

    public int getNoResult() { return noResult; }
    public void setNoResult(int noResult) { this.noResult = noResult; }

    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }

    public double getNrr() { return nrr; }
    public void setNrr(double nrr) { this.nrr = nrr; }

    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Standing standing = (Standing) o;
        return played == standing.played &&
                won == standing.won &&
                lost == standing.lost &&
                tied == standing.tied &&
                noResult == standing.noResult &&
                points == standing.points &&
                Double.compare(standing.nrr, nrr) == 0 &&
                position == standing.position &&
                java.util.Objects.equals(seriesId, standing.seriesId) &&
                java.util.Objects.equals(teamName, standing.teamName);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(seriesId, teamName, played, won, lost, tied, noResult, points, nrr, position);
    }
}
