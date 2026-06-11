package bicodes.cricket.liveapp.data.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import bicodes.cricket.liveapp.data.api.ApiService;
import bicodes.cricket.liveapp.data.model.Series;
import bicodes.cricket.liveapp.data.model.SeriesInfoResponse;
import bicodes.cricket.liveapp.data.model.SeriesResponse;
import bicodes.cricket.liveapp.data.model.Standing;
import bicodes.cricket.liveapp.util.Constants;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Singleton
public class SeriesRepository {
    private static final String TAG = "SeriesRepository";
    private final SeriesDao seriesDao;
    private final ApiService apiService;
    private final ExecutorService executorService;

    private final MutableLiveData<Boolean> isRefreshing = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>(null);

    private long lastSeriesRefresh = 0;
    private static final long REFRESH_THROTTLE = 5 * 60 * 1000;

    @Inject
    public SeriesRepository(SeriesDao seriesDao, ApiService apiService) {
        this.seriesDao = seriesDao;
        this.apiService = apiService;
        this.executorService = Executors.newFixedThreadPool(4);
    }

    public LiveData<List<Series>> getSeriesList() {
        return seriesDao.getAllSeries();
    }

    public LiveData<List<Standing>> getStandings(String seriesId) {
        return seriesDao.getStandingsForSeries(seriesId);
    }

    public LiveData<Boolean> getIsRefreshing() {
        return isRefreshing;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void refreshSeriesList() {
        if (System.currentTimeMillis() - lastSeriesRefresh < REFRESH_THROTTLE) return;
        lastSeriesRefresh = System.currentTimeMillis();

        isRefreshing.postValue(true);
        apiService.getSeriesList(Constants.API_KEY, 0).enqueue(new Callback<SeriesResponse>() {
            @Override
            public void onResponse(Call<SeriesResponse> call, Response<SeriesResponse> response) {
                isRefreshing.postValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    String status = response.body().getStatus();
                    if (status == null || "success".equalsIgnoreCase(status)) {
                        List<Series> seriesList = response.body().getData();
                        if (seriesList != null) {
                            executorService.execute(() -> seriesDao.insertSeries(seriesList));
                        }
                    } else {
                        Log.e(TAG, "API status: " + status);
                    }
                } else {
                    errorMessage.postValue("Failed to fetch series: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<SeriesResponse> call, Throwable t) {
                isRefreshing.postValue(false);
                Log.e(TAG, "Failed to fetch series list: " + t.getMessage());
            }
        });
    }

    public void refreshStandings(String seriesId) {
        isRefreshing.postValue(true);
        apiService.getSeriesInfo(Constants.API_KEY, seriesId).enqueue(new Callback<SeriesInfoResponse>() {
            @Override
            public void onResponse(Call<SeriesInfoResponse> call, Response<SeriesInfoResponse> response) {
                isRefreshing.postValue(false);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    List<Standing> standings = response.body().getData().getPointsTable();
                    if (standings != null) {
                        for (int i = 0; i < standings.size(); i++) {
                            standings.get(i).setSeriesId(seriesId);
                            standings.get(i).setPosition(i + 1);
                        }
                        executorService.execute(() -> {
                            seriesDao.deleteStandingsForSeries(seriesId);
                            seriesDao.insertStandings(standings);
                        });
                    }
                } else {
                    Log.e(TAG, "Failed to fetch standings: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<SeriesInfoResponse> call, Throwable t) {
                isRefreshing.postValue(false);
                Log.e(TAG, "Failed to fetch standings: " + t.getMessage());
            }
        });
    }

}
