package bicodes.cricket.liveapp.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import bicodes.cricket.liveapp.R;
import bicodes.cricket.liveapp.data.model.Standing;

import java.util.List;

public class StandingsAdapter extends ListAdapter<Standing, StandingsAdapter.StandingViewHolder> {

    public StandingsAdapter() {
        super(new DiffUtil.ItemCallback<Standing>() {
            @Override
            public boolean areItemsTheSame(@NonNull Standing oldItem, @NonNull Standing newItem) {
                return oldItem.getId() == newItem.getId();
            }

            @Override
            public boolean areContentsTheSame(@NonNull Standing oldItem, @NonNull Standing newItem) {
                return oldItem.equals(newItem);
            }
        });
    }

    public void setStandings(List<Standing> standings) {
        submitList(standings);
    }

    @NonNull
    @Override
    public StandingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_standing, parent, false);
        return new StandingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StandingViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    static class StandingViewHolder extends RecyclerView.ViewHolder {
        private final TextView textPos, textTeam, textP, textW, textL, textPts, textNRR;

        public StandingViewHolder(@NonNull View itemView) {
            super(itemView);
            textPos = itemView.findViewById(R.id.text_pos);
            textTeam = itemView.findViewById(R.id.text_team);
            textP = itemView.findViewById(R.id.text_played);
            textW = itemView.findViewById(R.id.text_won);
            textL = itemView.findViewById(R.id.text_lost);
            textPts = itemView.findViewById(R.id.text_pts);
            textNRR = itemView.findViewById(R.id.text_nrr);
        }

        public void bind(Standing standing) {
            textPos.setText(String.valueOf(standing.getPosition()));
            textTeam.setText(standing.getTeamName());
            textP.setText(String.valueOf(standing.getPlayed()));
            textW.setText(String.valueOf(standing.getWon()));
            textL.setText(String.valueOf(standing.getLost()));
            textPts.setText(String.valueOf(standing.getPoints()));
            textNRR.setText(String.valueOf(standing.getNrr()));

            if (standing.getPosition() <= 2) {
                itemView.setBackgroundColor(Color.parseColor("#E8F5E9")); // Light green for top 2
            } else {
                itemView.setBackgroundColor(Color.TRANSPARENT);
            }
        }
    }
}
