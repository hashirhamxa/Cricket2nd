package livecricket.livecrickettv.cricketstreaming

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LinksActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_links)

        val matchTitle = intent.getStringExtra("MATCH_TITLE") ?: "AUSTRALIA VS INDIA"
        val tournament = intent.getStringExtra("TOURNAMENT") ?: "ICC T20 WORLD CUP"

        findViewById<TextView>(R.id.text_match_title_top).text = matchTitle
        findViewById<TextView>(R.id.text_tournament_top).text = tournament
        findViewById<TextView>(R.id.text_match_title_hero).text = matchTitle
        findViewById<TextView>(R.id.badge_tournament_hero).text = tournament
        
        findViewById<ImageButton>(R.id.btn_back).setOnClickListener { finish() }

        val rvChannels = findViewById<RecyclerView>(R.id.rv_channels)
        val channels = listOf(
            Channel("PTV SPORTS HD", "ULTRA HD"),
            Channel("WILLOW HD", "4K"),
            Channel("STAR SPORTS ULTRA HD", "ULTRA HD"),
            Channel("SKY SPORTS CRICKET", "1080P")
        )

        rvChannels.adapter = ChannelAdapter(channels) {
            // Handle channel click - open player activity
        }
    }
}
