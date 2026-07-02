package livecricket.livecrickettv.cricketstreaming.data.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;
import livecricket.livecrickettv.cricketstreaming.util.Converters;

import java.util.List;

@Entity(tableName = "matches")
public class Match {
    @PrimaryKey
    @NonNull
    @SerializedName("id")
    private String id = "";

    @SerializedName("name")
    private String name;

    @SerializedName("title")
    private String title;

    @SerializedName("matchType")
    private String matchType;

    @SerializedName("status")
    private String status;

    @SerializedName("t1")
    private String team1;

    @SerializedName("t2")
    private String team2;

    @SerializedName("venue")
    private String venue;

    @SerializedName("date")
    private String date;

    @SerializedName("dateTimeGMT")
    private String dateTimeGMT;

    @SerializedName("matchStarted")
    private boolean matchStarted;

    @SerializedName("matchEnded")
    private boolean matchEnded;

    @SerializedName("teams")
    private List<String> teams;

    @SerializedName("score")
    private List<Score> score;

    private long lastUpdated;

    // Getters and Setters
    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }

    public String getName() { 
        if (name != null && !name.isEmpty()) return name;
        if (team1 != null && team2 != null) return team1 + " vs " + team2;
        return title;
    }
    public void setName(String name) { this.name = name; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMatchType() { return matchType; }
    public void setMatchType(String matchType) { this.matchType = matchType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getTeam1() { return team1; }
    public void setTeam1(String team1) { this.team1 = team1; }

    public String getTeam2() { return team2; }
    public void setTeam2(String team2) { this.team2 = team2; }

    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }

    public String getDate() { 
        if (date != null && !date.isEmpty()) return date;
        if (dateTimeGMT != null && !dateTimeGMT.isEmpty()) {
            if (dateTimeGMT.contains("T")) {
                this.date = dateTimeGMT.split("T")[0];
                return date;
            }
            this.date = dateTimeGMT;
            return date;
        }
        return null;
    }
    public void setDate(String date) { this.date = date; }

    public String getDateTimeGMT() { return dateTimeGMT; }
    public void setDateTimeGMT(String dateTimeGMT) { this.dateTimeGMT = dateTimeGMT; }

    public boolean isMatchStarted() { return matchStarted; }
    public void setMatchStarted(boolean matchStarted) { this.matchStarted = matchStarted; }

    public boolean isMatchEnded() { return matchEnded; }
    public void setMatchEnded(boolean matchEnded) { this.matchEnded = matchEnded; }

    public List<String> getTeams() { return teams; }
    public void setTeams(List<String> teams) { this.teams = teams; }

    public List<Score> getScore() { return score; }
    public void setScore(List<Score> score) { this.score = score; }

    public long getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(long lastUpdated) { this.lastUpdated = lastUpdated; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Match match = (Match) o;
        return id.equals(match.id) &&
                matchStarted == match.matchStarted &&
                matchEnded == match.matchEnded &&
                lastUpdated == match.lastUpdated &&
                java.util.Objects.equals(name, match.name) &&
                java.util.Objects.equals(status, match.status) &&
                java.util.Objects.equals(score, match.score);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(id, name, status, matchStarted, matchEnded, score, lastUpdated);
    }
}
