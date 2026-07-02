package livecricket.livecrickettv.cricketstreaming.util;

import android.content.Context;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class WorkManagerScheduler {
    private static final String SYNC_WORK_NAME = "CricketDataSyncWork";
    private static final int REFRESH_INTERVAL_MINUTES = 15;

    public static void startPeriodicSync(Context context) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest syncRequest = new PeriodicWorkRequest.Builder(
                SyncWorker.class,
                REFRESH_INTERVAL_MINUTES,
                TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                SYNC_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
        );
    }

    public static void runOneTimeSync(Context context) {
        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(SyncWorker.class).build();
        WorkManager.getInstance(context).enqueue(request);
    }

    public static void stopPeriodicSync(Context context) {
        WorkManager.getInstance(context).cancelUniqueWork(SYNC_WORK_NAME);
    }
}
