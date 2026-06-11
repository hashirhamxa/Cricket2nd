package bicodes.cricket.liveapp.util;

public class Constants {
    public static final String BASE_URL = "https://api.cricapi.com/v1/";
    public static final String API_KEY = "c6f5c57f-d8ab-4748-96fa-73e86cc37003"; // Replace with real key
    
    // Endpoints
    public static final String CURRENT_MATCHES = "currentMatches";
    public static final String RECENT_MATCHES_SCORE_ENDPOINT = "cricScore";
    public static final String SERIES = "series";
    public static final String SERIES_INFO = "series_info";
    public static final String MATCH_LIST = "matches";
    public static final String SERIES_LIST = "series";
    
    // Database
    public static final String DATABASE_NAME = "cricket_db";

    // Intent Extras
    public static final String EXTRA_MATCH_ID = "extra_match_id";
    
    // Cache expiry (optional)
    public static final long CACHE_EXPIRY_MS = 10 * 60 * 1000; // 10 minutes
}
