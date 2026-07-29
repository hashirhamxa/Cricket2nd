package livecricket.livecrickettv.cricketstreaming

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView

class HighlightsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_highlights, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val rvTournaments = view.findViewById<RecyclerView>(R.id.rv_highlight_tournaments)
        
        val tournaments = listOf(
            TournamentHighlight("IPL 2026", "Indian Premier League", eventCount = "48"),
            TournamentHighlight("ICC T20 WORLD CUP", "International Cricket Council", eventCount = "12"),
            TournamentHighlight("PSL 2026", "Pakistan Super League", eventCount = "24"),
            TournamentHighlight("BIG BASH LEAGUE", "Australian T20 League", eventCount = "18"),
            TournamentHighlight("LA LIGA", "Spanish Football League", eventCount = "30")
        )
        
        rvTournaments.adapter = HighlightTournamentAdapter(tournaments) { tournament ->
            // Navigate to event highlights for this tournament
            val intent = Intent(context, EventActivity::class.java).apply {
                putExtra("TOURNAMENT_NAME", tournament.name)
            }
            startActivity(intent)
        }
    }
}
