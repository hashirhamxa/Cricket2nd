package livecricket.livecrickettv.cricketstreaming

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EventActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tournament) // Reusing the same list layout

        val tournamentName = intent.getStringExtra("TOURNAMENT_NAME") ?: "IPL 2026"
        
        findViewById<TextView>(R.id.text_category_title).text = tournamentName
        findViewById<ImageButton>(R.id.btn_back).setOnClickListener { finish() }

        val rvEvents = findViewById<RecyclerView>(R.id.rv_tournaments)
        
        val items = listOf(
            HomeMatch("CSK VS DC", tournamentName, "LIVE NOW", isLive = true),
            HomeMatch("GT VS KKR", tournamentName, "UPCOMING", isLive = false),
            HomeMatch("LSG VS PBKS", tournamentName, "LIVE NOW", isLive = true),
            HomeMatch("MI VS RR", tournamentName, "UPCOMING", isLive = false)
        )

        rvEvents.adapter = TournamentAdapter(items)
    }
}
