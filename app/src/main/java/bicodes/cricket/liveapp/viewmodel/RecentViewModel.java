package bicodes.cricket.liveapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import bicodes.cricket.liveapp.data.model.Match;
import bicodes.cricket.liveapp.data.repository.MatchRepository;
import bicodes.cricket.liveapp.ui.MatchListItem;
import bicodes.cricket.liveapp.util.DateTimeUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class RecentViewModel extends ViewModel {

    private final MatchRepository repository;
    private final LiveData<List<MatchListItem>> groupedMatches;
    private final LiveData<Boolean> isLoading;
    private final LiveData<String> error;
    private final androidx.lifecycle.MutableLiveData<Boolean> manualLoading = new androidx.lifecycle.MutableLiveData<>(false);

    @Inject
    public RecentViewModel(MatchRepository repository) {
        this.repository = repository;
        this.groupedMatches = Transformations.map(repository.getRecentMatches(), list -> {
            android.util.Log.d("RecentViewModel", "Matches from DB: " + (list != null ? list.size() : 0));
            return groupMatchesByDate(list);
        });
        this.error = repository.getErrorMessage();

        androidx.lifecycle.MediatorLiveData<Boolean> combinedLoading = new androidx.lifecycle.MediatorLiveData<>();
        LiveData<Boolean> repoLoading = repository.getIsRefreshing();
        combinedLoading.addSource(repoLoading, loading -> combinedLoading.setValue(loading || (manualLoading.getValue() != null && manualLoading.getValue())));
        combinedLoading.addSource(manualLoading, loading -> combinedLoading.setValue(loading || (repoLoading.getValue() != null && repoLoading.getValue())));
        this.isLoading = combinedLoading;
        
        // Initial load called once
        repository.refreshAndCacheMatches();
        repository.refreshRecentMatches();
    }

    private List<MatchListItem> groupMatchesByDate(List<Match> matches) {
        if (matches == null || matches.isEmpty()) return Collections.emptyList();

        Map<String, List<Match>> groups = new TreeMap<>(Collections.reverseOrder());
        for (Match match : matches) {
            String date = match.getDate();
            if (date == null) date = "Unknown";
            if (!groups.containsKey(date)) {
                groups.put(date, new ArrayList<>());
            }
            groups.get(date).add(match);
        }

        List<MatchListItem> items = new ArrayList<>();
        for (Map.Entry<String, List<Match>> entry : groups.entrySet()) {
            items.add(new MatchListItem.HeaderItem(DateTimeUtils.getHeaderDate(entry.getKey())));
            for (Match m : entry.getValue()) {
                items.add(new MatchListItem.MatchItem(m));
            }
        }
        return items;
    }

    public LiveData<List<MatchListItem>> getMatches() {
        return groupedMatches;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
    
    public LiveData<String> getError() {
        return error;
    }

    public void refreshMatches() {
        manualLoading.setValue(true);
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> manualLoading.setValue(false), 2000);
    }

}
