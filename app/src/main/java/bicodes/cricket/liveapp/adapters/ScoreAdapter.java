package bicodes.cricket.liveapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import bicodes.cricket.liveapp.R;
import bicodes.cricket.liveapp.data.model.Score;

import java.util.ArrayList;
import java.util.List;

public class ScoreAdapter extends RecyclerView.Adapter<ScoreAdapter.ScoreViewHolder> {

    private List<Score> scores = new ArrayList<>();

    public void setScores(List<Score> scores) {
        this.scores = scores;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ScoreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_score_line, parent, false);
        return new ScoreViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScoreViewHolder holder, int position) {
        holder.bind(scores.get(position));
    }

    @Override
    public int getItemCount() {
        return scores.size();
    }

    static class ScoreViewHolder extends RecyclerView.ViewHolder {
        private final TextView textInning, textScore;

        public ScoreViewHolder(@NonNull View itemView) {
            super(itemView);
            textInning = itemView.findViewById(R.id.text_inning_name);
            textScore = itemView.findViewById(R.id.text_inning_score);
        }

        public void bind(Score score) {
            textInning.setText(score.getInning());
            textScore.setText(String.format("%d/%d (%s)", score.getRuns(), score.getWickets(), score.getOvers()));
        }
    }
}
