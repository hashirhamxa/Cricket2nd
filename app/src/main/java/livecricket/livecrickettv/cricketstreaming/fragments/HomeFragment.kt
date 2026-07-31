package livecricket.livecrickettv.cricketstreaming.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import livecricket.livecrickettv.cricketstreaming.database.EventEntity
import livecricket.livecrickettv.cricketstreaming.database.TournamentEntity
import livecricket.livecrickettv.cricketstreaming.R
import livecricket.livecrickettv.cricketstreaming.activities.EventActivity
import livecricket.livecrickettv.cricketstreaming.activities.LinksActivity
import livecricket.livecrickettv.cricketstreaming.activities.TournamentActivity
import livecricket.livecrickettv.cricketstreaming.adapters.CategoryAdapter
import livecricket.livecrickettv.cricketstreaming.viewmodels.HomeDisplayItem
import livecricket.livecrickettv.cricketstreaming.viewmodels.HomeViewModel

/**
 * HomeFragment: The main landing screen displaying Cricket, Football, and Trending live content.
 * Dynamically switches between multi-section (horizontal carousels) and single-section (vertical list) layouts.
 */
@AndroidEntryPoint
class HomeFragment : Fragment() {

    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize UI components
        val layoutMulti = view.findViewById<LinearLayout>(R.id.layout_multi_sections)
        val layoutSingle = view.findViewById<LinearLayout>(R.id.layout_single_section)

        val rvCricket = view.findViewById<RecyclerView>(R.id.rv_cricket)
        val rvFootball = view.findViewById<RecyclerView>(R.id.rv_football)
        val rvTrending = view.findViewById<RecyclerView>(R.id.rv_trending)
        val rvSingle = view.findViewById<RecyclerView>(R.id.rv_single)

        val sectionCricket = view.findViewById<View>(R.id.section_cricket)
        val sectionFootball = view.findViewById<View>(R.id.section_football)
        val sectionTrending = view.findViewById<View>(R.id.section_trending)

        val textSingleTitle = view.findViewById<TextView>(R.id.text_single_title)

        val swipeRefresh = view.findViewById<SwipeRefreshLayout>(R.id.swipe_refresh_home)
        swipeRefresh.setProgressBackgroundColorSchemeResource(R.color.surface)
        swipeRefresh.setColorSchemeResources(R.color.primary, R.color.secondary)
        swipeRefresh.setOnRefreshListener {
            viewModel.refresh()
        }

        // Observe ViewModel states using lifecycle-aware coroutines
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                
                // Toggle between Multi-Section and Single-Section layout visibility
                launch {
                    viewModel.isSingleSection.collectLatest { isSingle ->
                        layoutSingle.visibility = if (isSingle) View.VISIBLE else View.GONE
                        layoutMulti.visibility = if (isSingle) View.GONE else View.VISIBLE
                    }
                }

                launch {
                    viewModel.isRefreshing.collectLatest { isRefreshing ->
                        swipeRefresh.isRefreshing = isRefreshing
                    }
                }

                // Populate data into the recyclerviews
                launch {
                    viewModel.sections.collectLatest { sections ->
                        val isSingle = viewModel.isSingleSection.value

                        if (isSingle && sections.isNotEmpty()) {
                            // Single section mode: Vertical full-width list
                            val section = sections[0]
                            textSingleTitle.text = section.title
                            rvSingle.apply {
                                layoutManager = LinearLayoutManager(context)
                                adapter = CategoryAdapter(section.items, true) { handleItemClick(it) }
                            }
                        } else {
                            // Multi section mode: Horizontal carousels for Cricket/Football, Vertical for Trending
                            
                            // 1. Reset all visibilities first
                            listOf(sectionCricket, sectionFootball, sectionTrending, rvCricket, rvFootball, rvTrending)
                                .forEach { it.visibility = View.GONE }

                            // 2. Clear layouts to reorder
                            layoutMulti.removeAllViews()

                            // 3. Iterate and enable active sections in order
                            sections.forEach { section ->
                                when (section.sportType) {
                                    "cricket" -> {
                                        sectionCricket.visibility = View.VISIBLE
                                        rvCricket.apply {
                                            visibility = View.VISIBLE
                                            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                                            adapter = CategoryAdapter(section.items) { handleItemClick(it) }
                                        }
                                        layoutMulti.addView(sectionCricket)
                                        layoutMulti.addView(rvCricket)
                                    }
                                    "football" -> {
                                        sectionFootball.visibility = View.VISIBLE
                                        rvFootball.apply {
                                            visibility = View.VISIBLE
                                            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                                            adapter = CategoryAdapter(section.items) { handleItemClick(it) }
                                        }
                                        layoutMulti.addView(sectionFootball)
                                        layoutMulti.addView(rvFootball)
                                    }
                                    "other" -> {
                                        sectionTrending.visibility = View.VISIBLE
                                        rvTrending.apply {
                                            visibility = View.VISIBLE
                                            layoutManager = LinearLayoutManager(context)
                                            adapter = CategoryAdapter(section.items) { handleItemClick(it) }
                                        }
                                        layoutMulti.addView(sectionTrending)
                                        layoutMulti.addView(rvTrending)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        setupStaticClickListeners(view)
    }

    /**
     * Sets up click listeners for "See All" buttons.
     */
    private fun setupStaticClickListeners(view: View) {
        view.findViewById<TextView>(R.id.btn_see_all_cricket).setOnClickListener {
            openTournamentActivity("CRICKET")
        }
        view.findViewById<TextView>(R.id.btn_see_all_football).setOnClickListener {
            openTournamentActivity("FOOTBALL")
        }
        view.findViewById<TextView>(R.id.btn_see_all_trending).setOnClickListener {
            openTournamentActivity("TRENDING NOW")
        }
    }

    /**
     * Handles item click navigation.
     * Navigates to LinksActivity for direct events, or TournamentActivity for tournament groups.
     */
    private fun handleItemClick(item: HomeDisplayItem) {
        when (val original = item.originalObject) {
            is EventEntity -> {
                val intent = Intent(context, LinksActivity::class.java).apply {
                    putExtra("MATCH_TITLE", original.eventName)
                    putExtra("TOURNAMENT", item.subtitle)
                    putExtra("EVENT_ID", original.id)
                    putExtra("EVENT_THUMB_URL", original.eventThumbUrl)
                    putExtra("IS_HIGHLIGHTS_MODE", false)
                }
                startActivity(intent)
            }
            is TournamentEntity -> {
                val intent = Intent(context, EventActivity::class.java).apply {
                    putExtra("TOURNAMENT_ID", original.id)
                    putExtra("TOURNAMENT_NAME", original.name)
                    putExtra("TOURNAMENT_THUMB_URL", original.thumbUrl)
                    putExtra("IS_HIGHLIGHTS_MODE", false)
                }
                startActivity(intent)
            }
        }
    }

    /**
     * Opens TournamentActivity with a specific category filter.
     */
    private fun openTournamentActivity(category: String) {
        val intent = Intent(context, TournamentActivity::class.java)
        intent.putExtra("CATEGORY", category)
        intent.putExtra("IS_HIGHLIGHTS_MODE", false)
        startActivity(intent)
    }
}
