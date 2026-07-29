package livecricket.livecrickettv.cricketstreaming

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TournamentActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tournament)

        val category = intent.getStringExtra("CATEGORY") ?: "CRICKET"
        
        findViewById<TextView>(R.id.text_category_title).text = category
        findViewById<ImageButton>(R.id.btn_back).setOnClickListener { finish() }

        val rvTournaments = findViewById<RecyclerView>(R.id.rv_tournaments)
        
        val items = when(category) {
            "CRICKET" -> listOf(
                HomeMatch("AUSTRALIA VS INDIA", "ICC T20 WORLD CUP", "LIVE NOW", isLive = true),
                HomeMatch("PAKISTAN VS ENGLAND", "IPL 2024", "UPCOMING", isLive = false),
                HomeMatch("SYDNEY SIXERS VS SCORCHERS", "BIG BASH LEAGUE", "LIVE NOW", isLive = true)
            )
            "FOOTBALL" -> listOf(
                HomeMatch("BARCELONA VS REAL MADRID", "LA LIGA", "LIVE", isLive = true),
                HomeMatch("MAN CITY VS ARSENAL", "PREMIER LEAGUE", "LIVE", isLive = true),
                HomeMatch("LIVERPOOL VS CHELSEA", "PREMIER LEAGUE", "UPCOMING", isLive = false)
            )
            else -> listOf(
                HomeMatch("UFC 300", "MIXED MARTIAL ARTS", "LIVE", isLive = true),
                HomeMatch("WIMBLEDON FINAL", "TENNIS", "UPCOMING", isLive = false)
            )
        }

        rvTournaments.adapter = TournamentAdapter(items)
    }
}
