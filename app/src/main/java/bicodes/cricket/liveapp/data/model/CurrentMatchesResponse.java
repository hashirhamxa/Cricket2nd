package bicodes.cricket.liveapp.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CurrentMatchesResponse {
    @SerializedName("data")
    private List<Match> data;

    @SerializedName("status")
    private String status;

    @SerializedName("reason")
    private String reason;

    @SerializedName("apikey")
    private String apikey;

    // Getters and Setters
    public List<Match> getData() { return data; }
    public void setData(List<Match> data) { this.data = data; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getApikey() { return apikey; }
    public void setApikey(String apikey) { this.apikey = apikey; }
}
