package bicodes.cricket.liveapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import bicodes.cricket.liveapp.data.model.Match;
import bicodes.cricket.liveapp.data.repository.MatchRepository;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class MatchDetailViewModel extends ViewModel {

    private final MatchRepository repository;
    private final MutableLiveData<String> matchId = new MutableLiveData<>();
    private final LiveData<Match> match;
    private final LiveData<Boolean> isLoading;

    @Inject
    public MatchDetailViewModel(MatchRepository repository) {
        this.repository = repository;
        this.isLoading = repository.getIsRefreshing();
        this.match = Transformations.switchMap(matchId, repository::getMatchScore);
    }

    public void init(String id) {
        if (id != null && !id.equals(matchId.getValue())) {
            matchId.setValue(id);
        }
    }

    public LiveData<Match> getMatch() {
        return match;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public void refreshMatch() {
        repository.refreshAndCacheMatches();
    }

}
