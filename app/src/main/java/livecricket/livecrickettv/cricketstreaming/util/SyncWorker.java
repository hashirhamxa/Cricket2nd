package livecricket.livecrickettv.cricketstreaming.util;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.hilt.work.HiltWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import livecricket.livecrickettv.cricketstreaming.data.repository.MatchRepository;
import livecricket.livecrickettv.cricketstreaming.data.repository.SeriesRepository;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;

@HiltWorker
public class SyncWorker extends Worker {
    private static final String TAG = "SyncWorker";
    private final MatchRepository matchRepository;
    private final SeriesRepository seriesRepository;

    @AssistedInject
    public SyncWorker(
            @Assisted @NonNull Context context,
            @Assisted @NonNull WorkerParameters params,
            MatchRepository matchRepository,
            SeriesRepository seriesRepository) {
        super(context, params);
        this.matchRepository = matchRepository;
        this.seriesRepository = seriesRepository;
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Background sync started...");
        try {
            // Refresh current matches
            matchRepository.refreshAndCacheMatches();
            
            // Refresh cricScore (Recent)
            matchRepository.refreshRecentMatches();

            // Refresh upcoming matches
            matchRepository.refreshUpcomingMatches();
            
            // Refresh series list
            seriesRepository.refreshSeriesList();
            
            Log.d(TAG, "Background sync completed successfully.");
            return Result.success();
        } catch (Exception e) {
            Log.e(TAG, "Background sync failed: " + e.getMessage());
            return Result.retry();
        }
    }
}
