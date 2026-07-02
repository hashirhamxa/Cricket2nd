package livecricket.livecrickettv.cricketstreaming.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import livecricket.livecrickettv.cricketstreaming.R;
import livecricket.livecrickettv.cricketstreaming.data.model.Match;
import livecricket.livecrickettv.cricketstreaming.data.model.Score;
import livecricket.livecrickettv.cricketstreaming.ui.MatchListItem;
import livecricket.livecrickettv.cricketstreaming.util.DateTimeUtils;
import livecricket.livecrickettv.cricketstreaming.util.OnMatchClickListener;

import java.util.List;

public class RecentAdapter extends ListAdapter<MatchListItem, RecyclerView.ViewHolder> {

    private final OnMatchClickListener listener;

    public RecentAdapter(OnMatchClickListener listener) {
        super(new DiffUtil.ItemCallback<MatchListItem>() {
            @Override
            public boolean areItemsTheSame(@NonNull MatchListItem oldItem, @NonNull MatchListItem newItem) {
                if (oldItem.getType() != newItem.getType()) return false;
                if (oldItem instanceof MatchListItem.HeaderItem) {
                    return ((MatchListItem.HeaderItem) oldItem).getTitle().equals(((MatchListItem.HeaderItem) newItem).getTitle());
                } else {
                    return ((MatchListItem.MatchItem) oldItem).getMatch().getId().equals(((MatchListItem.MatchItem) newItem).getMatch().getId());
                }
            }

            @Override
            public boolean areContentsTheSame(@NonNull MatchListItem oldItem, @NonNull MatchListItem newItem) {
                return oldItem.equals(newItem);
            }
        });
        this.listener = listener;
    }

    public void setMatches(List<MatchListItem> matches) {
        submitList(matches);
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MatchListItem.TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_date_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recent, parent, false);
            return new RecentViewHolder(view, listener);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MatchListItem item = getItem(position);
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).bind((MatchListItem.HeaderItem) item);
        } else if (holder instanceof RecentViewHolder) {
            ((RecentViewHolder) holder).bind(((MatchListItem.MatchItem) item).getMatch());
        }
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        private final TextView textHeader;
        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            textHeader = itemView.findViewById(R.id.text_date_header);
        }
        public void bind(MatchListItem.HeaderItem header) {
            textHeader.setText(header.getTitle());
        }
    }

    static class RecentViewHolder extends RecyclerView.ViewHolder {
        private final TextView textSeriesName, textTeam1Name, textTeam2Name, textTeam1Score, textTeam2Score;
        private final TextView textStatus, textDate, textVenue;
        private final OnMatchClickListener listener;

        public RecentViewHolder(@NonNull View itemView, OnMatchClickListener listener) {
            super(itemView);
            this.listener = listener;
            textSeriesName = itemView.findViewById(R.id.text_series_name);
            textTeam1Name = itemView.findViewById(R.id.text_team1_name);
            textTeam2Name = itemView.findViewById(R.id.text_team2_name);
            textTeam1Score = itemView.findViewById(R.id.text_team1_score);
            textTeam2Score = itemView.findViewById(R.id.text_team2_score);
            textStatus = itemView.findViewById(R.id.text_match_status);
            textDate = itemView.findViewById(R.id.text_date);
            textVenue = itemView.findViewById(R.id.text_venue);
        }

        public void bind(Match match) {
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
            textDate.setText(DateTimeUtils.formatMatchDate(match.getDate()));
            
            // Set Series name
            String matchType = match.getMatchType();
            textSeriesName.setText(matchType != null ? matchType.toUpperCase() + " MATCH" : "CRICKET MATCH");

            // Format venue
            String venueStr = match.getVenue();
            textVenue.setText(venueStr != null ? "📍 " + venueStr : "📍 Venue TBD");

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
                score1 = "-";
                score2 = "-";
            }
            textTeam1Score.setText(score1);
            textTeam2Score.setText(score2);

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onMatchClick(match);
            });
        }
    }
}
