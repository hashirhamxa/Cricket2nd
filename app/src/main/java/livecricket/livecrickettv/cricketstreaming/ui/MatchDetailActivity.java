package livecricket.livecrickettv.cricketstreaming.ui;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import livecricket.livecrickettv.cricketstreaming.R;
import livecricket.livecrickettv.cricketstreaming.data.model.Match;
import livecricket.livecrickettv.cricketstreaming.adapters.ScoreAdapter;
import livecricket.livecrickettv.cricketstreaming.viewmodel.MatchDetailViewModel;
import livecricket.livecrickettv.cricketstreaming.util.Constants;
import livecricket.livecrickettv.cricketstreaming.util.DateTimeUtils;
import livecricket.livecrickettv.cricketstreaming.util.StatusHelper;

import java.util.ArrayList;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MatchDetailActivity extends AppCompatActivity {

    private MatchDetailViewModel viewModel;
    private ScoreAdapter scoreAdapter;

    private TextView textName, textStatus, textBadge, textVenue, textDate, textType;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_detail);

        // Handle window insets for Edge-to-Edge compatibility (API 35/36)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.detail_root), (v, windowInsets) -> {
            Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());

            // Apply top padding to toolbar to prevent overlapping with status bar
            View toolbar = findViewById(R.id.toolbar);
            if (toolbar != null) {
                toolbar.setPadding(0, systemBars.top, 0, 0);
                TypedValue tv = new TypedValue();
                int actionBarHeight = 0;
                if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                    actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
                }
                if (actionBarHeight > 0) {
                    toolbar.getLayoutParams().height = actionBarHeight + systemBars.top;
                }
            }

            // Apply bottom padding to coordinator to keep content above system navigation bar
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), systemBars.bottom);

            return windowInsets;
        });

        initViews();
        setupRecyclerView();
        setupViewModel();

        String matchId = getIntent().getStringExtra(Constants.EXTRA_MATCH_ID);
        if (matchId != null) {
            viewModel.init(matchId);
            viewModel.refreshMatch();
        }
    }

    private void initViews() {
        textName = findViewById(R.id.text_match_name);
        textStatus = findViewById(R.id.text_match_status);
        textBadge = findViewById(R.id.text_status_badge);
        textVenue = findViewById(R.id.text_venue);
        textDate = findViewById(R.id.text_date);
        textType = findViewById(R.id.text_match_type);
        progressBar = findViewById(R.id.progress_bar);

        findViewById(R.id.toolbar).setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recycler_scores);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        scoreAdapter = new ScoreAdapter();
        recyclerView.setAdapter(scoreAdapter);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(MatchDetailViewModel.class);

        viewModel.getMatch().observe(this, match -> {
            if (match != null) {
                bindMatchData(match);
            }
        });

        viewModel.getIsLoading().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        // Add error observation if needed, though MatchRepository already emits to shared status
    }


    private void bindMatchData(Match match) {
        textName.setText(match.getName());
        textStatus.setText(match.getStatus());
        textVenue.setText("Venue: " + match.getVenue());
        textDate.setText("Date: " + DateTimeUtils.formatMatchDate(match.getDate()));
        textType.setText("Type: " + match.getMatchType());

        StatusHelper.setStatusBadge(textBadge, match.isMatchEnded() ? "FINISHED" : (match.isMatchStarted() ? "LIVE" : "UPCOMING"));

        if (match.getScore() != null) {
            scoreAdapter.setScores(match.getScore());
        } else {
            scoreAdapter.setScores(new ArrayList<>());
        }
    }
}
