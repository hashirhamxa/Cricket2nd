package livecricket.livecrickettv.cricketstreaming.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import livecricket.livecrickettv.cricketstreaming.R;
import livecricket.livecrickettv.cricketstreaming.adapters.UpcomingAdapter;
import livecricket.livecrickettv.cricketstreaming.viewmodel.UpcomingViewModel;

import java.util.ArrayList;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class UpcomingFragment extends Fragment {

    private UpcomingViewModel viewModel;
    private UpcomingAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;
    private TextView textEmpty, textError;

    public UpcomingFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_upcoming, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerView();
        setupViewModel();
        setupSwipeRefresh();
    }

    private void initViews(View view) {
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        textEmpty = view.findViewById(R.id.text_empty);
        textError = view.findViewById(R.id.text_error);
    }

    private void setupRecyclerView() {
        if (getView() == null) return;
        RecyclerView recyclerView = getView().findViewById(R.id.recycler_upcoming);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new UpcomingAdapter(match -> {
            android.content.Intent intent = new android.content.Intent(getContext(), livecricket.livecrickettv.cricketstreaming.ui.MatchDetailActivity.class);
            intent.putExtra(livecricket.livecrickettv.cricketstreaming.util.Constants.EXTRA_MATCH_ID, match.getId());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(UpcomingViewModel.class);

        viewModel.getMatches().observe(getViewLifecycleOwner(), matches -> {
            if (matches != null && !matches.isEmpty()) {
                adapter.setMatches(matches);
                textEmpty.setVisibility(View.GONE);
            } else {
                adapter.setMatches(new ArrayList<>());
                textEmpty.setVisibility(View.VISIBLE);
            }
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> swipeRefresh.setRefreshing(isLoading));

        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && adapter.getItemCount() == 0) {
                textError.setVisibility(View.VISIBLE);
                textEmpty.setVisibility(View.GONE);
            } else {
                textError.setVisibility(View.GONE);
            }
        });
    }


    private void setupSwipeRefresh() {
        swipeRefresh.setOnRefreshListener(() -> viewModel.refreshMatches());
    }
}
