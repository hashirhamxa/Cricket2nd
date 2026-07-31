package livecricket.livecrickettv.cricketstreaming.activities

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import livecricket.livecrickettv.cricketstreaming.R
import livecricket.livecrickettv.cricketstreaming.adapters.CategoryAdapter
import livecricket.livecrickettv.cricketstreaming.database.EventEntity
import livecricket.livecrickettv.cricketstreaming.database.TournamentEntity
import livecricket.livecrickettv.cricketstreaming.viewmodels.HomeDisplayItem
import livecricket.livecrickettv.cricketstreaming.viewmodels.TournamentViewModel

@AndroidEntryPoint
class TournamentActivity : AppCompatActivity() {

    private val viewModel: TournamentViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tournament)

        val category = intent.getStringExtra("CATEGORY") ?: "CRICKET"
        val isHighlights = intent.getBooleanExtra("IS_HIGHLIGHTS_MODE", false)

        findViewById<TextView>(R.id.text_category_title).text = category
        findViewById<ImageButton>(R.id.btn_back).setOnClickListener { finish() }

        val rvTournaments = findViewById<RecyclerView>(R.id.rv_tournaments)
        rvTournaments.layoutManager = LinearLayoutManager(this)

        val swipeRefresh = findViewById<SwipeRefreshLayout>(R.id.swipe_refresh_tournaments)
        swipeRefresh.setProgressBackgroundColorSchemeResource(R.color.surface)
        swipeRefresh.setColorSchemeResources(R.color.primary, R.color.secondary)
        swipeRefresh.setOnRefreshListener {
            viewModel.refresh(category, isHighlights)
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.items.collectLatest { items ->
                        rvTournaments.adapter = CategoryAdapter(items, true) { handleItemClick(it) }
                    }
                }

                launch {
                    viewModel.isRefreshing.collectLatest { isRefreshing ->
                        swipeRefresh.isRefreshing = isRefreshing
                    }
                }
            }
        }

        viewModel.loadTournamentsBySportType(category, isHighlights)
    }

    private fun handleItemClick(item: HomeDisplayItem) {
        val isHighlights = intent.getBooleanExtra("IS_HIGHLIGHTS_MODE", false)
        when (val original = item.originalObject) {
            is EventEntity -> {
                val intent = Intent(this, LinksActivity::class.java).apply {
                    putExtra("MATCH_TITLE", original.eventName)
                    putExtra("TOURNAMENT", item.subtitle)
                    putExtra("EVENT_ID", original.id)
                    putExtra("EVENT_THUMB_URL", original.eventThumbUrl)
                    putExtra("IS_HIGHLIGHTS_MODE", isHighlights)
                }
                startActivity(intent)
            }
            is TournamentEntity -> {
                val intent = Intent(this, EventActivity::class.java).apply {
                    putExtra("TOURNAMENT_ID", original.id)
                    putExtra("TOURNAMENT_NAME", original.name)
                    putExtra("TOURNAMENT_THUMB_URL", original.thumbUrl)
                    putExtra("IS_HIGHLIGHTS_MODE", isHighlights)
                }
                startActivity(intent)
            }
        }
    }
}
