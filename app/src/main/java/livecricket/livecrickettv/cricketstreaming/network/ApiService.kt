package livecricket.livecrickettv.cricketstreaming.network

import livecricket.livecrickettv.cricketstreaming.models.ApiResponse
import retrofit2.http.GET
import retrofit2.http.Header

interface ApiService {
    @GET("items/apps?fields=*,ads.*,streaming.*,streaming.streaming_id.*,streaming.streaming_id.tournaments.*,streaming.streaming_id.tournaments.tournaments_id.*,streaming.streaming_id.tournaments.tournaments_id.events.*,streaming.streaming_id.tournaments.tournaments_id.events.events_id.*,streaming.streaming_id.tournaments.tournaments_id.events.events_id.highligths.*,streaming.streaming_id.tournaments.tournaments_id.events.events_id.highligths.highlights_id.*,streaming.streaming_id.tournaments.tournaments_id.events.events_id.links.*,streaming.streaming_id.tournaments.tournaments_id.events.events_id.links.links_id.*&filter%5B_and%5D%5B0%5D%5Bid%5D%5B_eq%5D=16")
    suspend fun getAppConfig(
        @Header("Authorization") token: String
    ): ApiResponse
}
