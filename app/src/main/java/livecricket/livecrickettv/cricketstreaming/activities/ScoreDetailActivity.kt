package livecricket.livecrickettv.cricketstreaming.activities

import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import livecricket.livecrickettv.cricketstreaming.R
import livecricket.livecrickettv.cricketstreaming.adapters.ScoreLineAdapter
import livecricket.livecrickettv.cricketstreaming.ads.AdsHelper
import livecricket.livecrickettv.cricketstreaming.database.MatchEntity
import livecricket.livecrickettv.cricketstreaming.models.Inning
import livecricket.livecrickettv.cricketstreaming.network.AppRepository
import livecricket.livecrickettv.cricketstreaming.viewmodels.ScoreDetailViewModel
import javax.inject.Inject

@AndroidEntryPoint
class ScoreDetailActivity : AppCompatActivity() {

    @Inject
    lateinit var repository: AppRepository

    private val viewModel: ScoreDetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_match_detail)

        val matchId = intent.getStringExtra("MATCH_ID") ?: ""

        findViewById<android.widget.ImageButton>(R.id.btn_back).setOnClickListener { 
            AdsHelper.getInstance(this@ScoreDetailActivity).showAd_Mob_X_Inter_With_Time(this@ScoreDetailActivity)
            finish() 
        }

        val rvScores = findViewById<RecyclerView>(R.id.recycler_scores)
        rvScores.layoutManager = LinearLayoutManager(this)

        lifecycleScope.launch {
            viewModel.match.collect { match ->
                match?.let { bindMatchData(it, rvScores) }
            }
        }

        if (matchId.isNotEmpty()) {
            viewModel.loadMatch(matchId)
        }
        loadAds()
    }

    private fun loadAds() {
        lifecycleScope.launch {
            val ads = repository.getAllAds()

            // Preload Interstitial
            ads.find { it.adPlacement.equals("Interstitial", ignoreCase = true) }?.let { ad ->
                if (ad.isActive == true && !ad.adUnitId.isNullOrEmpty()) {
                    AdsHelper.getInstance(this@ScoreDetailActivity).preloadAdADMOB_X_Inter(this@ScoreDetailActivity, ad.adUnitId)
                }
            }

            // Preload Rewarded
            ads.find { it.adPlacement.equals("Rewarded", ignoreCase = true) }?.let { ad ->
                if (ad.isActive == true && !ad.adUnitId.isNullOrEmpty()) {
                    AdsHelper.getInstance(this@ScoreDetailActivity).preloadRewardedAd(this@ScoreDetailActivity, ad.adUnitId)
                }
            }
        }
    }

    override fun onBackPressed() {
        AdsHelper.getInstance(this@ScoreDetailActivity).showAd_Mob_X_Inter_With_Time(this@ScoreDetailActivity)
        super.onBackPressed()
    }

    private fun bindMatchData(match: MatchEntity, rv: RecyclerView) {
        findViewById<TextView>(R.id.text_match_name).text = "${match.team1} vs ${match.team2}"
        findViewById<TextView>(R.id.text_match_status).text = match.status
        
        findViewById<TextView>(R.id.text_venue).text = "Venue: ${match.venue}"
        findViewById<TextView>(R.id.text_date).text = "Date: ${match.date}"
        findViewById<TextView>(R.id.text_match_type).text = "Type: ${match.matchType}"

        if (!match.scoreJson.isNullOrEmpty()) {
            val type = object : TypeToken<List<Inning>>() {}.type
            val scores: List<Inning> = Gson().fromJson(match.scoreJson, type)
            rv.adapter = ScoreLineAdapter(scores)
        }
    }
}
