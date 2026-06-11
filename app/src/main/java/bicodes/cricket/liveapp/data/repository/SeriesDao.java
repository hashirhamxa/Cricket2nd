package bicodes.cricket.liveapp.data.repository;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import bicodes.cricket.liveapp.data.model.Series;
import bicodes.cricket.liveapp.data.model.Standing;

import java.util.List;

@Dao
public interface SeriesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSeries(List<Series> series);

    @Query("SELECT * FROM series ORDER BY startDate DESC")
    LiveData<List<Series>> getAllSeries();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertStandings(List<Standing> standings);

    @Query("SELECT * FROM standings WHERE seriesId = :seriesId ORDER BY position ASC")
    LiveData<List<Standing>> getStandingsForSeries(String seriesId);

    @Query("DELETE FROM standings WHERE seriesId = :seriesId")
    void deleteStandingsForSeries(String seriesId);
}
