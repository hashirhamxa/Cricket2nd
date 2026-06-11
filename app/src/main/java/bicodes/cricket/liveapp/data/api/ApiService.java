package bicodes.cricket.liveapp.data.api;

import bicodes.cricket.liveapp.data.model.CurrentMatchesResponse;
import bicodes.cricket.liveapp.data.model.SeriesInfoResponse;
import bicodes.cricket.liveapp.data.model.SeriesResponse;
import bicodes.cricket.liveapp.util.Constants;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {
    @GET(Constants.CURRENT_MATCHES)
    Call<CurrentMatchesResponse> getCurrentMatches(
            @Query("apikey") String apiKey,
            @Query("offset") int offset
    );

    @GET(Constants.RECENT_MATCHES_SCORE_ENDPOINT)
    Call<CurrentMatchesResponse> getRecentMatchesScore(
            @Query("apikey") String apiKey
    );

    @GET(Constants.MATCH_LIST)
    Call<CurrentMatchesResponse> getMatchList(
            @Query("apikey") String apiKey,
            @Query("offset") int offset
    );

    @GET(Constants.SERIES)
    Call<SeriesResponse> getSeriesList(
            @Query("apikey") String apiKey,
            @Query("offset") int offset
    );

    @GET(Constants.SERIES_INFO)
    Call<SeriesInfoResponse> getSeriesInfo(
            @Query("apikey") String apiKey,
            @Query("id") String seriesId
    );
}
