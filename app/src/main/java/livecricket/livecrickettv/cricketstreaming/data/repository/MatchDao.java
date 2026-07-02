package livecricket.livecrickettv.cricketstreaming.data.repository;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import livecricket.livecrickettv.cricketstreaming.data.model.Match;

import java.util.List;

@Dao
public interface MatchDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMatches(List<Match> matches);

    @Query("SELECT * FROM matches WHERE matchStarted = 1 AND matchEnded = 0 ORDER BY lastUpdated DESC")
    LiveData<List<Match>> getLiveMatches();

    @Query("SELECT * FROM matches WHERE matchEnded = 1 OR (date < :today AND matchStarted = 0) ORDER BY date DESC")
    LiveData<List<Match>> getRecentMatches(String today);

    @Query("SELECT * FROM matches WHERE matchStarted = 0 AND matchEnded = 0 AND date >= :today ORDER BY date ASC")
    LiveData<List<Match>> getUpcomingMatches(String today);

    @Query("SELECT * FROM matches WHERE id = :matchId LIMIT 1")
    LiveData<Match> getMatchById(String matchId);

    @Query("DELETE FROM matches")
    void deleteAllMatches();
}
