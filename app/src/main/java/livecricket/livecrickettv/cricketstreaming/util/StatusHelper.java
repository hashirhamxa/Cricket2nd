package livecricket.livecrickettv.cricketstreaming.util;

import android.widget.TextView;
import androidx.core.content.ContextCompat;
import livecricket.livecrickettv.cricketstreaming.R;

public class StatusHelper {

    public static void setStatusBadge(TextView textView, String status) {
        if (status == null) return;
        
        String lowerStatus = status.toLowerCase();
        textView.setText(status.toUpperCase());
        
        int colorResId;
        if (lowerStatus.contains("live") || lowerStatus.contains("progress")) {
            colorResId = R.color.status_live;
        } else if (lowerStatus.contains("upcoming") || lowerStatus.contains("scheduled")) {
            colorResId = R.color.status_upcoming;
        } else {
            colorResId = R.color.status_finished;
        }
        
        textView.setBackgroundResource(R.drawable.badge_background);
        textView.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                ContextCompat.getColor(textView.getContext(), colorResId)
        ));
    }
}
