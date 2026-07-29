package livecricket.livecrickettv.cricketstreaming

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class Channel(
    val name: String,
    val quality: String,
    val iconRes: Int = R.drawable.about_icon
)

class ChannelAdapter(private val items: List<Channel>, private val onChannelClick: (Channel) -> Unit) :
    RecyclerView.Adapter<ChannelAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.text_channel_name)
        val quality: TextView = view.findViewById(R.id.text_quality)
        val btnPlay: View = view.findViewById(R.id.btn_play)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_channel, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.name.text = item.name
        holder.quality.text = item.quality
        
        holder.itemView.setOnClickListener { onChannelClick(item) }
        holder.btnPlay.setOnClickListener { onChannelClick(item) }
    }

    override fun getItemCount() = items.size
}
