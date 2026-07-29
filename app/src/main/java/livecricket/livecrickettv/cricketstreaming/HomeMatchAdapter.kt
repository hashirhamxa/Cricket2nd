package livecricket.livecrickettv.cricketstreaming

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HomeMatchAdapter(private val items: List<HomeMatch>) :
    RecyclerView.Adapter<HomeMatchAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val banner: ImageView = view.findViewById(R.id.img_match_banner)
        val title: TextView = view.findViewById(R.id.text_match_title)
        val tournament: TextView = view.findViewById(R.id.badge_tournament)
        val status: TextView = view.findViewById(R.id.text_match_status)
        val score: TextView = view.findViewById(R.id.text_score_overlay)
        val liveBadge: TextView = view.findViewById(R.id.badge_live)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_home_match, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.title.text = item.title
        holder.tournament.text = item.tournament
        holder.status.text = item.status
        
        if (item.score != null) {
            holder.score.text = item.score
            holder.score.visibility = View.VISIBLE
        } else {
            holder.score.visibility = View.GONE
        }
        
        holder.liveBadge.visibility = if (item.isLive) View.VISIBLE else View.GONE
        
        holder.itemView.setOnClickListener {
            val intent = Intent(it.context, LinksActivity::class.java).apply {
                putExtra("MATCH_TITLE", item.title)
                putExtra("TOURNAMENT", item.tournament)
            }
            it.context.startActivity(intent)
        }
    }

    override fun getItemCount() = items.size
}
