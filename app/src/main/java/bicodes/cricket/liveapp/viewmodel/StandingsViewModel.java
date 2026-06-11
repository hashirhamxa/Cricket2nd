package bicodes.cricket.liveapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import bicodes.cricket.liveapp.data.model.Series;
import bicodes.cricket.liveapp.data.model.Standing;
import bicodes.cricket.liveapp.data.repository.SeriesRepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class StandingsViewModel extends ViewModel {

    private final SeriesRepository repository;
    private final LiveData<List<Series>> seriesList;
    private final MutableLiveData<String> selectedSeriesId = new MutableLiveData<>();
    private final LiveData<List<Standing>> standings;
    private final LiveData<Boolean> isLoading;
    private final LiveData<String> error;
    private final MutableLiveData<Boolean> manualLoading = new MutableLiveData<>(false);

    @Inject
    public StandingsViewModel(SeriesRepository repository) {
        this.repository = repository;
        this.seriesList = Transformations.map(repository.getSeriesList(), this::filterActiveSeries);
        this.standings = Transformations.switchMap(selectedSeriesId, repository::getStandings);
        this.error = repository.getErrorMessage();

        androidx.lifecycle.MediatorLiveData<Boolean> combinedLoading = new androidx.lifecycle.MediatorLiveData<>();
        LiveData<Boolean> repoLoading = repository.getIsRefreshing();
        combinedLoading.addSource(repoLoading, loading -> combinedLoading.setValue(loading || (manualLoading.getValue() != null && manualLoading.getValue())));
        combinedLoading.addSource(manualLoading, loading -> combinedLoading.setValue(loading || (repoLoading.getValue() != null && repoLoading.getValue())));
        this.isLoading = combinedLoading;
        
        // Initial load
        repository.refreshSeriesList();
    }

    private List<Series> filterActiveSeries(List<Series> allSeries) {
        if (allSeries == null) return Collections.emptyList();
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String today = sdf.format(new Date());
        
        List<Series> active = new ArrayList<>();
        for (Series s : allSeries) {
            // If endDate is in the future or today, it's active
            if (s.getEndDate() != null && s.getEndDate().compareTo(today) >= 0) {
                active.add(s);
            }
        }
        // If no active found, return all as fallback or at least first 10
        return active.isEmpty() ? allSeries : active;
    }

    public LiveData<List<Series>> getSeriesList() {
        return seriesList;
    }

    public void selectSeries(String seriesId) {
        if (seriesId != null && !seriesId.equals(selectedSeriesId.getValue())) {
            selectedSeriesId.setValue(seriesId);
            repository.refreshStandings(seriesId);
        }
    }

    public LiveData<List<Standing>> getStandings() {
        return standings;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public void refresh() {
        manualLoading.setValue(true);
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> manualLoading.setValue(false), 2000);
    }

}
