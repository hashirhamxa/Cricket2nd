package bicodes.cricket.liveapp.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateTimeUtils {

    public static String formatMatchDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return "";
        
        // CricAPI often returns dates in yyyy-MM-dd format
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat outputFormat = new SimpleDateFormat("EEE, MMM dd", Locale.getDefault());
        
        try {
            Date date = inputFormat.parse(dateStr);
            if (date != null) {
                return outputFormat.format(date);
            }
        } catch (ParseException e) {
            // Try another common format yyyy-MM-dd'T'HH:mm:ss
            try {
                SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date date = isoFormat.parse(dateStr);
                if (date != null) {
                    return outputFormat.format(date);
                }
            } catch (ParseException e2) {
                return dateStr;
            }
        }
        return dateStr;
    }

    public static String getHeaderDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty() || dateStr.equals("Unknown")) return "Upcoming Matches";
        
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            Date date = inputFormat.parse(dateStr);
            if (date == null) return dateStr;

            SimpleDateFormat compareFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
            String target = compareFormat.format(date);
            
            Date now = new Date();
            String today = compareFormat.format(now);
            
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTime(now);
            
            cal.add(java.util.Calendar.DATE, -1);
            String yesterday = compareFormat.format(cal.getTime());
            
            cal.setTime(now);
            cal.add(java.util.Calendar.DATE, 1);
            String tomorrow = compareFormat.format(cal.getTime());

            if (target.equals(today)) return "Today";
            if (target.equals(yesterday)) return "Yesterday";
            if (target.equals(tomorrow)) return "Tomorrow";

            return new SimpleDateFormat("EEEE, MMM dd", Locale.getDefault()).format(date);
        } catch (ParseException e) {
            // Handle ISO format if needed
            try {
                SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                Date date = isoFormat.parse(dateStr);
                if (date != null) return getHeaderDate(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date));
            } catch (Exception e2) {
                return dateStr;
            }
        }
        return dateStr;
    }
}
