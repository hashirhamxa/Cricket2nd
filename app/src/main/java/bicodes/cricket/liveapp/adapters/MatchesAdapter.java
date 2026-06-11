package bicodes.cricket.liveapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import bicodes.cricket.liveapp.R;
import bicodes.cricket.liveapp.data.model.Match;
import bicodes.cricket.liveapp.data.model.Score;
import bicodes.cricket.liveapp.util.OnMatchClickListener;
import bicodes.cricket.liveapp.util.StatusHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MatchesAdapter extends ListAdapter<Match, MatchesAdapter.MatchViewHolder> {

    private final OnMatchClickListener listener;

    public MatchesAdapter(OnMatchClickListener listener) {
        super(new DiffUtil.ItemCallback<Match>() {
            @Override
            public boolean areItemsTheSame(@NonNull Match oldItem, @NonNull Match newItem) {
                return oldItem.getId().equals(newItem.getId());
            }

            @Override
            public boolean areContentsTheSame(@NonNull Match oldItem, @NonNull Match newItem) {
                return oldItem.equals(newItem);
            }
        });
        this.listener = listener;
    }

    public void setMatches(List<Match> matches) {
        submitList(matches);
    }

    @NonNull
    @Override
    public MatchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_match, parent, false);
        return new MatchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MatchViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    static class MatchViewHolder extends RecyclerView.ViewHolder {
        private final TextView textSeriesName, textTeam1Name, textTeam2Name, textTeam1Score, textTeam2Score;
        private final TextView textStatus, textVenue, textLastUpdated, textBadge;

        public MatchViewHolder(@NonNull View itemView) {
            super(itemView);
            textSeriesName = itemView.findViewById(R.id.text_series_name);
            textTeam1Name = itemView.findViewById(R.id.text_team1_name);
            textTeam2Name = itemView.findViewById(R.id.text_team2_name);
            textTeam1Score = itemView.findViewById(R.id.text_team1_score);
            textTeam2Score = itemView.findViewById(R.id.text_team2_score);
            textStatus = itemView.findViewById(R.id.text_match_status);
            textVenue = itemView.findViewById(R.id.text_venue);
            textLastUpdated = itemView.findViewById(R.id.text_last_updated);
            textBadge = itemView.findViewById(R.id.text_status_badge);
        }

        public void bind(Match match, OnMatchClickListener listener) {
            // Extracted team names with fallbacks
            String t1 = match.getTeam1();
            String t2 = match.getTeam2();
            if (t1 == null || t2 == null) {
                String name = match.getName();
                if (name != null && name.contains(" vs ")) {
                    String[] parts = name.split(" vs ");
                    t1 = parts[0].trim();
                    t2 = parts[1].trim();
                } else if (name != null && name.contains(" V ")) {
                    String[] parts = name.split(" V ");
                    t1 = parts[0].trim();
                    t2 = parts[1].trim();
                } else {
                    t1 = name != null ? name : "TBD";
                    t2 = "TBD";
                }
            }

            textTeam1Name.setText(t1);
            textTeam2Name.setText(t2);
            textStatus.setText(match.getStatus());
            
            // Set Series name
            String matchType = match.getMatchType();
            textSeriesName.setText(matchType != null ? matchType.toUpperCase() + " MATCH" : "CRICKET MATCH");
            
            // Format venue
            String venueStr = match.getVenue();
            textVenue.setText(venueStr != null ? "📍 " + venueStr : "📍 Venue TBD");

            StatusHelper.setStatusBadge(textBadge, match.isMatchEnded() ? "FINISHED" : (match.isMatchStarted() ? "LIVE" : "UPCOMING"));

            // Parse Scores to individual Team scores
            String score1 = "-";
            String score2 = "-";
            List<Score> scores = match.getScore();
            if (scores != null && !scores.isEmpty()) {
                String team1Lower = t1.toLowerCase();
                String team2Lower = t2.toLowerCase();
                
                for (Score s : scores) {
                    String inning = s.getInning().toLowerCase();
                    String formatted = s.getRuns() + "/" + s.getWickets() + " (" + s.getOvers() + ")";
                    
                    if (inning.contains(team1Lower) || (scores.indexOf(s) == 0 && !inning.contains(team2Lower))) {
                        score1 = formatted;
                    } else if (inning.contains(team2Lower) || scores.indexOf(s) == 1 || (scores.indexOf(s) == 0 && inning.contains(team2Lower))) {
                        score2 = formatted;
                    }
                }
            } else {
                score1 = match.isMatchStarted() ? "Yet to bat" : "-";
                score2 = match.isMatchStarted() ? "Yet to bat" : "-";
            }
            textTeam1Score.setText(score1);
            textTeam2Score.setText(score2);

            // Format Last Updated
            if (match.getLastUpdated() > 0) {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());
                textLastUpdated.setVisibility(View.VISIBLE);
                textLastUpdated.setText("Updated: " + sdf.format(new Date(match.getLastUpdated())));
            } else {
                textLastUpdated.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onMatchClick(match);
            });
        }
    }
}
