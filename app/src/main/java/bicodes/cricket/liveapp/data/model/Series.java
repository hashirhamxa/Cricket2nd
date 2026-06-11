package bicodes.cricket.liveapp.data.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

@Entity(tableName = "series")
public class Series {
    @PrimaryKey
    @NonNull
    @SerializedName("id")
    private String id = "";

    @SerializedName("name")
    private String name;

    @SerializedName("startDate")
    private String startDate;

    @SerializedName("endDate")
    private String endDate;

    @SerializedName("matches")
    private int matchesCount;

    // Getters and Setters
    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public int getMatchesCount() { return matchesCount; }
    public void setMatchesCount(int matchesCount) { this.matchesCount = matchesCount; }

    @Override
    public String toString() {
        return name;
    }
}
