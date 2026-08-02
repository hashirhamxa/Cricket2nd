package livecricket.livecrickettv.cricketstreaming.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import livecricket.livecrickettv.cricketstreaming.R
import livecricket.livecrickettv.cricketstreaming.activities.ScoreDetailActivity
import livecricket.livecrickettv.cricketstreaming.adapters.MatchesAdapter
import livecricket.livecrickettv.cricketstreaming.ads.AdsHelper
import livecricket.livecrickettv.cricketstreaming.viewmodels.ScoreViewModel

@AndroidEntryPoint
class ScoreFragment : Fragment() {

    private val viewModel: ScoreViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_matches, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rvMatches = view.findViewById<RecyclerView>(R.id.recycler_matches)
        val swipeRefresh = view.findViewById<SwipeRefreshLayout>(R.id.swipe_refresh)

        swipeRefresh.setProgressBackgroundColorSchemeResource(R.color.surface)
        swipeRefresh.setColorSchemeResources(R.color.primary, R.color.secondary)

        rvMatches.layoutManager = LinearLayoutManager(context)
        
        swipeRefresh.setOnRefreshListener {
            viewModel.refresh()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.matches.collectLatest { matches ->
                        rvMatches.adapter = MatchesAdapter(matches) { match ->
                            AdsHelper.getInstance(requireContext()).showAd_Mob_X_Inter_With_Time(requireActivity())
                            val intent = Intent(context, ScoreDetailActivity::class.java)
                            intent.putExtra("MATCH_ID", match.id)
                            startActivity(intent)
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
    }
}
