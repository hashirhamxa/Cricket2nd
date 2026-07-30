package livecricket.livecrickettv.cricketstreaming.Database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface AppDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApp(app: AppEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAds(ads: List<AdEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStreaming(streaming: List<StreamingEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTournaments(tournaments: List<TournamentEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<EventEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHighlights(highlights: List<HighlightEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLinks(links: List<LinkEntity>)

    @Transaction
    suspend fun clearAndInsert(
        app: AppEntity,
        ads: List<AdEntity>,
        streaming: List<StreamingEntity>,
        tournaments: List<TournamentEntity>,
        events: List<EventEntity>,
        highlights: List<HighlightEntity>,
        links: List<LinkEntity>
    ) {
        deleteAll()
        insertApp(app)
        insertAds(ads)
        insertStreaming(streaming)
        insertTournaments(tournaments)
        insertEvents(events)
        insertHighlights(highlights)
        insertLinks(links)
    }

    @Query("DELETE FROM apps")
    suspend fun deleteAllApps()

    @Query("DELETE FROM ads")
    suspend fun deleteAllAds()

    @Query("DELETE FROM streaming")
    suspend fun deleteAllStreaming()

    @Query("DELETE FROM tournaments")
    suspend fun deleteAllTournaments()

    @Query("DELETE FROM events")
    suspend fun deleteAllEvents()

    @Query("DELETE FROM highlights")
    suspend fun deleteAllHighlights()

    @Query("DELETE FROM links")
    suspend fun deleteAllLinks()

    @Query("SELECT * FROM ads")
    suspend fun getAllAds(): List<AdEntity>

    @Transaction
    suspend fun deleteAll() {
        deleteAllApps()
        deleteAllAds()
        deleteAllStreaming()
        deleteAllTournaments()
        deleteAllEvents()
        deleteAllHighlights()
        deleteAllLinks()
    }
}
