package livecricket.livecrickettv.cricketstreaming.data.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import livecricket.livecrickettv.cricketstreaming.data.api.ApiService;
import livecricket.livecrickettv.cricketstreaming.data.model.CurrentMatchesResponse;
import livecricket.livecrickettv.cricketstreaming.data.model.Match;
import livecricket.livecrickettv.cricketstreaming.util.Constants;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Singleton
public class MatchRepository {
    private static final String TAG = "MatchRepository";
    private final MatchDao matchDao;
    private final ApiService apiService;
    private final ExecutorService executorService;

    private final MutableLiveData<Boolean> isRefreshing = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>(null);

    private long lastMatchesRefresh = 0;
    private long lastRecentMatchesRefresh = 0;
    private long lastUpcomingRefresh = 0;
    private static final long REFRESH_THROTTLE = 5 * 60 * 1000; // 5 minutes

    @Inject
    public MatchRepository(MatchDao matchDao, ApiService apiService) {
        this.matchDao = matchDao;
        this.apiService = apiService;
        this.executorService = Executors.newFixedThreadPool(4);
    }

    public LiveData<List<Match>> getCurrentMatches() {
        return matchDao.getLiveMatches();
    }

    public LiveData<List<Match>> getRecentMatches() {
        String today = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(new java.util.Date());
        return matchDao.getRecentMatches(today);
    }

    public LiveData<List<Match>> getUpcomingMatches() {
        String today = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(new java.util.Date());
        return matchDao.getUpcomingMatches(today);
    }

    public LiveData<Match> getMatchScore(String matchId) {
        return matchDao.getMatchById(matchId);
    }

    public LiveData<Boolean> getIsRefreshing() {
        return isRefreshing;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void refreshAndCacheMatches() {
        if (System.currentTimeMillis() - lastMatchesRefresh < REFRESH_THROTTLE) return;
        lastMatchesRefresh = System.currentTimeMillis();
        
        isRefreshing.postValue(true);
        apiService.getCurrentMatches(Constants.API_KEY, 0).enqueue(new Callback<CurrentMatchesResponse>() {
            @Override
            public void onResponse(Call<CurrentMatchesResponse> call, Response<CurrentMatchesResponse> response) {
                handleResponse(response);
            }

            @Override
            public void onFailure(Call<CurrentMatchesResponse> call, Throwable t) {
                isRefreshing.postValue(false);
                errorMessage.postValue("Network Failure");
                Log.e(TAG, "Network Failure: " + t.getMessage());
            }
        });
    }

    public void refreshRecentMatches() {
        if (System.currentTimeMillis() - lastRecentMatchesRefresh < REFRESH_THROTTLE) return;
        lastRecentMatchesRefresh = System.currentTimeMillis();
        
        isRefreshing.postValue(true);
        apiService.getRecentMatchesScore(Constants.API_KEY).enqueue(new Callback<CurrentMatchesResponse>() {
            @Override
            public void onResponse(Call<CurrentMatchesResponse> call, Response<CurrentMatchesResponse> response) {
                handleResponse(response);
            }

            @Override
            public void onFailure(Call<CurrentMatchesResponse> call, Throwable t) {
                isRefreshing.postValue(false);
                errorMessage.postValue("Network Failure");
                Log.e(TAG, "Network Failure: " + t.getMessage());
            }
        });
    }

    public void refreshUpcomingMatches() {
        if (System.currentTimeMillis() - lastUpcomingRefresh < REFRESH_THROTTLE) return;
        lastUpcomingRefresh = System.currentTimeMillis();

        isRefreshing.postValue(true);
        apiService.getMatchList(Constants.API_KEY, 0).enqueue(new Callback<CurrentMatchesResponse>() {
            @Override
            public void onResponse(Call<CurrentMatchesResponse> call, Response<CurrentMatchesResponse> response) {
                handleResponse(response);
            }

            @Override
            public void onFailure(Call<CurrentMatchesResponse> call, Throwable t) {
                isRefreshing.postValue(false);
                errorMessage.postValue("Network Failure");
                Log.e(TAG, "Network Failure: " + t.getMessage());
            }
        });
    }

    private void handleResponse(Response<CurrentMatchesResponse> response) {
        isRefreshing.postValue(false);
        if (response.isSuccessful() && response.body() != null) {
            String status = response.body().getStatus();
            if (status == null || "success".equalsIgnoreCase(status)) {
                List<Match> matches = response.body().getData();
                if (matches != null) {
                    long currentTime = System.currentTimeMillis();
                    for (Match match : matches) {
                        match.setLastUpdated(currentTime);
                        // Ensure date is in yyyy-MM-dd for sorting/filtering
                        match.getDate();
                    }
                    executorService.execute(() -> matchDao.insertMatches(matches));
                    Log.d(TAG, "Matches cached successfully. Count: " + matches.size());
                } else {
                    Log.d(TAG, "Response successful but matches list is null");
                }
            } else {
                String reason = response.body().getReason();
                String msg = "API: " + (reason != null ? reason : response.body().getStatus());
                errorMessage.postValue(msg);
                Log.e(TAG, msg);
            }
        } else {
            String bodyString = "";
            try {
                if (response.errorBody() != null) bodyString = response.errorBody().string();
            } catch (Exception ignored) {}
            String errorMsg = "API Error: " + response.code();
            errorMessage.postValue(errorMsg);
            Log.e(TAG, errorMsg);
        }
    }


    public void testApiAndCaching() {
        Log.d(TAG, "Starting API Test...");
        refreshAndCacheMatches();
    }
}
