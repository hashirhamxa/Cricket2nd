package livecricket.livecrickettv.cricketstreaming.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class SeriesResponse {
    @SerializedName("data")
    private List<Series> data;

    @SerializedName("status")
    private String status;

    @SerializedName("reason")
    private String reason;

    public List<Series> getData() { return data; }
    public void setData(List<Series> data) { this.data = data; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
