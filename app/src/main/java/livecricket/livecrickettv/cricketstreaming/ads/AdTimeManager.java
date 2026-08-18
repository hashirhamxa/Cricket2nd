package livecricket.livecrickettv.cricketstreaming.ads;

import android.content.Context;
import android.content.SharedPreferences;

public class AdTimeManager {
    private static final String sp_name = "AdTimerPrefs";
    private static final String KEY_LAST_AD_TIME = "lastAdShownTime";
    private static final String KEY_AD_INTERVAL = "adIntervalTime";
    private static final long DEFAULT_INTERVAL = 5000; // fixed 5 seconds
    private final SharedPreferences sharedPreferences;

    public AdTimeManager(Context context) {
        sharedPreferences = context.getSharedPreferences(sp_name, Context.MODE_PRIVATE);
    }

    // Save the last ad shown time
    public void setLastAdShownTime(long time) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(KEY_LAST_AD_TIME, time);
        editor.apply();
    }

    // Get the last ad shown time
    public long getLastAdShownTime() {
        return sharedPreferences.getLong(KEY_LAST_AD_TIME, 0); // Default to 0 if not set
    }

    // Save the ad interval time from server (in seconds)
    public void setAdIntervalInSeconds(int seconds) {
        long intervalMs = (seconds > 0) ? (seconds * 1000L) : DEFAULT_INTERVAL;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(KEY_AD_INTERVAL, intervalMs);
        editor.apply();
    }

    // Get the ad interval time (defaults to 5000ms)
    public long getAdIntervalTime() {
        long interval = sharedPreferences.getLong(KEY_AD_INTERVAL, DEFAULT_INTERVAL);
        return interval > 0 ? interval : DEFAULT_INTERVAL;
    }

    // Check if enough time has passed to show another ad
    public boolean canShowAd() {
        long currentTime = System.currentTimeMillis();
        return (currentTime - getLastAdShownTime()) >= getAdIntervalTime();
    }

    // Get remaining wait time in seconds
    public long getRemainingTime() {
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - getLastAdShownTime();
        long remaining = getAdIntervalTime() - elapsedTime;
        return remaining > 0 ? remaining / 1000 : 0; // never return negative
    }
}
