package livecricket.livecrickettv.cricketstreaming.activities

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import livecricket.livecrickettv.cricketstreaming.R
import livecricket.livecrickettv.cricketstreaming.adapters.EventAdapter
import livecricket.livecrickettv.cricketstreaming.viewmodels.EventViewModel

@AndroidEntryPoint
class EventActivity : AppCompatActivity() {

    private val viewModel: EventViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tournament) // Reusing the same list layout

        val tournamentId = intent.getIntExtra("TOURNAMENT_ID", -1)
        val tournamentName = intent.getStringExtra("TOURNAMENT_NAME") ?: "Tournament"
        val tournamentThumbUrl = intent.getStringExtra("TOURNAMENT_THUMB_URL")
        val isHighlightsMode = intent.getBooleanExtra("IS_HIGHLIGHTS_MODE", false)

        findViewById<TextView>(R.id.text_category_title).text = tournamentName
        findViewById<ImageButton>(R.id.btn_back).setOnClickListener { finish() }

        val rvEvents = findViewById<RecyclerView>(R.id.rv_tournaments)

        val swipeRefresh = findViewById<SwipeRefreshLayout>(R.id.swipe_refresh_tournaments)
        swipeRefresh.setOnRefreshListener {
            viewModel.refresh(tournamentId, isHighlightsMode)
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    if (isHighlightsMode) {
                        viewModel.highlightEvents.collectLatest { events ->
                            rvEvents.adapter = EventAdapter(events, tournamentName, true, tournamentThumbUrl)
                        }
                    } else {
                        viewModel.events.collectLatest { events ->
                            rvEvents.adapter = EventAdapter(events, tournamentName, false, tournamentThumbUrl)
                        }
                    }
                }

                launch {
                    viewModel.isRefreshing.collectLatest { isRefreshing ->
                        swipeRefresh.isRefreshing = isRefreshing
                    }
                }
            }
        }

        if (tournamentId != -1) {
            if (isHighlightsMode) {
                viewModel.loadHighlightEvents(tournamentId)
            } else {
                viewModel.loadEvents(tournamentId)
            }
        }
    }
}
