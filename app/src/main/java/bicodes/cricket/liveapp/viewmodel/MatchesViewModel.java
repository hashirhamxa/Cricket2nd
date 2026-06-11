package bicodes.cricket.liveapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import bicodes.cricket.liveapp.data.model.Match;
import bicodes.cricket.liveapp.data.repository.MatchRepository;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class MatchesViewModel extends ViewModel {

    private final MatchRepository repository;
    private final LiveData<List<Match>> matches;
    private final LiveData<Boolean> isLoading;
    private final LiveData<String> error;
    private final MutableLiveData<Boolean> manualLoading = new MutableLiveData<>(false);

    @Inject
    public MatchesViewModel(MatchRepository repository) {
        this.repository = repository;
        this.matches = repository.getCurrentMatches();
        this.error = repository.getErrorMessage();

        // Combine repository loading and manual 2-second timer
        MediatorLiveData<Boolean> combinedLoading = new MediatorLiveData<>();
        LiveData<Boolean> repoLoading = repository.getIsRefreshing();
        
        combinedLoading.addSource(repoLoading, loading -> combinedLoading.setValue(loading || (manualLoading.getValue() != null && manualLoading.getValue())));
        combinedLoading.addSource(manualLoading, loading -> combinedLoading.setValue(loading || (repoLoading.getValue() != null && repoLoading.getValue())));
        this.isLoading = combinedLoading;
        
        this.matches.observeForever(list -> {
            if (list != null) {
                android.util.Log.d("MatchesViewModel", "Received matches from DB: " + list.size());
            }
        });
        
        // Initial load called once when ViewModel is created
        repository.refreshAndCacheMatches();
    }

    public LiveData<List<Match>> getMatches() {
        return matches;
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
