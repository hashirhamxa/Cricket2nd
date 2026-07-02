package livecricket.livecrickettv.cricketstreaming.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import livecricket.livecrickettv.cricketstreaming.R;
import livecricket.livecrickettv.cricketstreaming.data.model.Series;
import livecricket.livecrickettv.cricketstreaming.adapters.StandingsAdapter;
import livecricket.livecrickettv.cricketstreaming.viewmodel.StandingsViewModel;

import java.util.ArrayList;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class StandingsFragment extends Fragment {

    private StandingsViewModel viewModel;
    private StandingsAdapter adapter;
    private Spinner spinner;
    private ProgressBar progressBar;
    private androidx.swiperefreshlayout.widget.SwipeRefreshLayout swipeRefresh;
    private TextView textEmpty, textError;

    public StandingsFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_standings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerView();
        setupViewModel();
    }

    private void initViews(View view) {
        spinner = view.findViewById(R.id.spinner_tournaments);
        progressBar = view.findViewById(R.id.progress_bar);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        textEmpty = view.findViewById(R.id.text_empty);
        textError = view.findViewById(R.id.text_error);
    }

    private void setupRecyclerView() {
        if (getView() == null) return;
        RecyclerView recyclerView = getView().findViewById(R.id.recycler_standings);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new StandingsAdapter();
        recyclerView.setAdapter(adapter);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(StandingsViewModel.class);

        viewModel.getSeriesList().observe(getViewLifecycleOwner(), seriesList -> {
            if (seriesList != null && !seriesList.isEmpty() && getContext() != null) {
                ArrayAdapter<Series> spinnerAdapter = new ArrayAdapter<>(getContext(),
                        android.R.layout.simple_spinner_item, seriesList);
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(spinnerAdapter);

                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        Series selected = (Series) parent.getItemAtPosition(position);
                        viewModel.selectSeries(selected.getId());
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}
                });
            }
        });

        viewModel.getStandings().observe(getViewLifecycleOwner(), standings -> {
            if (standings != null && !standings.isEmpty()) {
                adapter.setStandings(standings);
                textEmpty.setVisibility(View.GONE);
            } else {
                adapter.setStandings(new ArrayList<>());
                textEmpty.setVisibility(View.VISIBLE);
            }
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            swipeRefresh.setRefreshing(isLoading);
        });

        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && adapter.getItemCount() == 0) {
                textError.setVisibility(View.VISIBLE);
                textEmpty.setVisibility(View.GONE);
            } else {
                textError.setVisibility(View.GONE);
            }
        });

        viewModel.refresh();
        swipeRefresh.setOnRefreshListener(() -> viewModel.refresh());
    }

}
