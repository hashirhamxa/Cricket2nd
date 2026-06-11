package bicodes.cricket.liveapp.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class SeriesInfoResponse {
    @SerializedName("data")
    private Data data;

    @SerializedName("status")
    private String status;

    @SerializedName("reason")
    private String reason;

    public Data getData() { return data; }
    public void setData(Data data) { this.data = data; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public static class Data {
        @SerializedName("pointsTable")
        private List<Standing> pointsTable;

        public List<Standing> getPointsTable() { return pointsTable; }
        public void setPointsTable(List<Standing> pointsTable) { this.pointsTable = pointsTable; }
    }
}
