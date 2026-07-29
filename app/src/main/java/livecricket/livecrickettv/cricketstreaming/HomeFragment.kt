package livecricket.livecrickettv.cricketstreaming

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HomeFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupCricketList(view)
        setupFootballList(view)
        setupTrendingList(view)

        view.findViewById<TextView>(R.id.btn_see_all_cricket).setOnClickListener {
            openTournament("CRICKET")
        }
        view.findViewById<TextView>(R.id.btn_see_all_football).setOnClickListener {
            openTournament("FOOTBALL")
        }
        view.findViewById<TextView>(R.id.btn_see_all_trending).setOnClickListener {
            openTournament("TRENDING NOW")
        }
    }

    private fun setupCricketList(view: View) {
        val rvCricket = view.findViewById<RecyclerView>(R.id.rv_cricket)
        val items = listOf(
            HomeMatch("AUSTRALIA VS INDIA", "ICC T20 WORLD CUP", "LIVE NOW • 45,000 watching"),
            HomeMatch("ENGLAND VS PAKISTAN", "TEST SERIES", "STARTS IN 2H 30M", isLive = false),
            HomeMatch("SOUTH AFRICA VS NZ", "ODI SERIES", "LIVE NOW • 12,000 watching")
        )
        rvCricket.adapter = HomeMatchAdapter(items)
        rvCricket.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
    }

    private fun setupFootballList(view: View) {
        val rvFootball = view.findViewById<RecyclerView>(R.id.rv_football)
        val items = listOf(
            HomeMatch("BARCELONA VS REAL MADRID", "LA LIGA", "LIVE • 78'", score = "2-1"),
            HomeMatch("MAN CITY VS ARSENAL", "PREMIER LEAGUE", "LIVE • 15'", score = "0-0"),
            HomeMatch("LIVERPOOL VS CHELSEA", "PREMIER LEAGUE", "TODAY • 21:00", isLive = false)
        )
        rvFootball.adapter = HomeMatchAdapter(items)
        rvFootball.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
    }

    private fun setupTrendingList(view: View) {
        val rvTrending = view.findViewById<RecyclerView>(R.id.rv_trending)
        val items = listOf(
            HomeTrending(
                "UFC 300", 
                "MIXED MARTIAL ARTS", 
                "Pereira vs. Hill - The historic triple-header championship event is underway. Stream the main card live."
            ),
            HomeTrending(
                "WIMBLEDON FINAL", 
                "TENNIS", 
                "Watch the epic showdown at the center court. Experience every serve and volley live."
            )
        )
        rvTrending.adapter = HomeTrendingAdapter(items)
        rvTrending.layoutManager = LinearLayoutManager(context)
        rvTrending.isNestedScrollingEnabled = false
    }

    private fun openTournament(category: String) {
        val intent = Intent(context, TournamentActivity::class.java)
        intent.putExtra("CATEGORY", category)
        startActivity(intent)
    }
}
