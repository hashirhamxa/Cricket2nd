package livecricket.livecrickettv.cricketstreaming.data.api;

import livecricket.livecrickettv.cricketstreaming.data.model.CurrentMatchesResponse;
import livecricket.livecrickettv.cricketstreaming.data.model.SeriesInfoResponse;
import livecricket.livecrickettv.cricketstreaming.data.model.SeriesResponse;
import livecricket.livecrickettv.cricketstreaming.util.Constants;

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
