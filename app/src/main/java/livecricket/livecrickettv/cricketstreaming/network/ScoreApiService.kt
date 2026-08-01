package livecricket.livecrickettv.cricketstreaming.network

import livecricket.livecrickettv.cricketstreaming.models.CurrentMatchesResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ScoreApiService {
    @GET("currentMatches")
    suspend fun getCurrentMatches(
        @Query("apikey") apiKey: String,
        @Query("offset") offset: Int = 0
    ): CurrentMatchesResponse
}
