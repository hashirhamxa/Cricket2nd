package livecricket.livecrickettv.cricketstreaming.adapters

import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import livecricket.livecrickettv.cricketstreaming.database.EventEntity
import livecricket.livecrickettv.cricketstreaming.R
import livecricket.livecrickettv.cricketstreaming.activities.LinksActivity

class EventAdapter(
    private val items: List<EventEntity>,
    private val tournamentName: String,
    private val isHighlightsMode: Boolean = false,
    private val tournamentThumbUrl: String? = null
) : RecyclerView.Adapter<EventAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val container: View = view
        val banner: ImageView = view.findViewById(R.id.img_banner)
        val tournamentName: TextView = view.findViewById(R.id.text_tournament_name)
        val liveBadge: TextView = view.findViewById(R.id.text_live_badge)
        val matchTitle: TextView = view.findViewById(R.id.text_match_title)
        val statusMain: TextView = view.findViewById(R.id.text_status_main)
        val statusSub: TextView = view.findViewById(R.id.text_status_sub)
        val btnAction: AppCompatButton = view.findViewById(R.id.btn_action)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tournament_large, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        val imageUrl = item.eventThumbUrl ?: tournamentThumbUrl
        Glide.with(holder.itemView.context)
            .load(imageUrl)
            .placeholder(R.drawable.bg_section_indicator)
            .into(holder.banner)

        holder.tournamentName.text = tournamentName
        holder.matchTitle.text = item.eventName
        
        val isLive = item.isVisible == true
        holder.statusMain.text = if (isLive) "LIVE NOW" else "UPCOMING"
        holder.statusSub.text = item.description ?: ""

        if (isLive) {
            holder.liveBadge.text = "LIVE"
            holder.liveBadge.setBackgroundResource(R.drawable.bg_badge_live_red)
            holder.btnAction.text = "WATCH NOW"
            holder.btnAction.setBackgroundResource(R.drawable.bg_button_watch_now)
            holder.btnAction.setTextColor(Color.BLACK)
        } else {
            holder.liveBadge.text = "UPCOMING"
            holder.liveBadge.setBackgroundResource(R.drawable.bg_badge_upcoming)
            holder.btnAction.text = "DETAILS"
            holder.btnAction.setBackgroundResource(R.drawable.bg_button_details)
            holder.btnAction.setTextColor(Color.WHITE)
        }

        holder.container.setOnClickListener {
            val intent = Intent(it.context, LinksActivity::class.java).apply {
                putExtra("MATCH_TITLE", item.eventName)
                putExtra("TOURNAMENT", tournamentName)
                putExtra("EVENT_ID", item.id)
                putExtra("EVENT_THUMB_URL", item.eventThumbUrl ?: tournamentThumbUrl)
                putExtra("IS_HIGHLIGHTS_MODE", isHighlightsMode)
            }
            it.context.startActivity(intent)
        }
        
        holder.btnAction.setOnClickListener {
            holder.container.performClick()
        }
    }

    override fun getItemCount() = items.size
}
