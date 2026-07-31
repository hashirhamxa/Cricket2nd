package livecricket.livecrickettv.cricketstreaming.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import livecricket.livecrickettv.cricketstreaming.R
import livecricket.livecrickettv.cricketstreaming.adapters.Channel
import livecricket.livecrickettv.cricketstreaming.adapters.ChannelAdapter
import livecricket.livecrickettv.cricketstreaming.database.LinkEntity
import livecricket.livecrickettv.cricketstreaming.newplayer.NewPlayerActivity
import livecricket.livecrickettv.cricketstreaming.viewmodels.LinksViewModel
import kotlin.random.Random

@AndroidEntryPoint
class LinksActivity : AppCompatActivity() {

    private val viewModel: LinksViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_links)

        val eventId = intent.getIntExtra("EVENT_ID", -1)
        val matchTitle = intent.getStringExtra("MATCH_TITLE") ?: "Match Details"
        val tournament = intent.getStringExtra("TOURNAMENT") ?: "Tournament"
        val isHighlights = intent.getBooleanExtra("IS_HIGHLIGHTS_MODE", false)
        val eventThumbUrl = intent.getStringExtra("EVENT_THUMB_URL")

        Log.e("leolog eventThumbUrl", "eventThumbUrl "+eventThumbUrl)

        findViewById<TextView>(R.id.text_match_title_top).text = matchTitle
        findViewById<TextView>(R.id.text_tournament_top).text = tournament
        findViewById<TextView>(R.id.text_match_title_hero).text = matchTitle
        findViewById<TextView>(R.id.badge_tournament_hero).text = tournament

        // Set hero image
        val imgHero = findViewById<ImageView>(R.id.img_hero)
        Glide.with(this).load(eventThumbUrl)
            .placeholder(R.drawable.bg_section_indicator)
            .into(imgHero)

        // Set random watching count
        val randomWatching = Random.nextInt(1000, 10001)
        findViewById<TextView>(R.id.text_watching).text = "$randomWatching WATCHING"

        findViewById<ImageButton>(R.id.btn_back).setOnClickListener { finish() }

        val rvChannels = findViewById<RecyclerView>(R.id.rv_channels)

        val swipeRefresh = findViewById<SwipeRefreshLayout>(R.id.swipe_refresh_links)
        swipeRefresh.setProgressBackgroundColorSchemeResource(R.color.surface)
        swipeRefresh.setColorSchemeResources(R.color.primary, R.color.secondary)
        swipeRefresh.setOnRefreshListener {
            viewModel.refresh(eventId, isHighlights)
        }
        
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    if (isHighlights) {
                        viewModel.highlights.collectLatest { highlights ->
                            val channels = highlights.map { highlight ->
                                Channel(
                                    name = highlight.title ?: "Highlight",
                                    quality = "HD",
                                    isHighlight = true,
                                    thumbnailLink = highlight.thumbnailLink,
                                    link = LinkEntity(
                                        id = highlight.id,
                                        linkName = highlight.title,
                                        linkUrl = highlight.videoLink,
                                        linkType = "Highlight",
                                        mpdLink = null,
                                        mpdKey = null,
                                        linkImage = highlight.thumbnailLink,
                                        isVisible = highlight.isVisible,
                                        priority = 0,
                                        eventId = eventId
                                    )
                                )
                            }
                            updateAdapter(rvChannels, channels, matchTitle)
                        }
                    } else {
                        viewModel.links.collectLatest { links ->
                            val channels = links.map { link ->
                                Channel(
                                    name = link.linkName ?: "Link",
                                    quality = link.linkType ?: "HD",
                                    link = link,
                                    isHighlight = false,
                                    thumbnailLink = null
                                )
                            }
                            updateAdapter(rvChannels, channels, matchTitle)
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

        if (eventId != -1) {
            if (isHighlights) {
                viewModel.loadHighlights(eventId)
            } else {
                viewModel.loadLinks(eventId)
            }
        }
    }

    private fun updateAdapter(rv: RecyclerView, channels: List<Channel>, matchTitle: String) {
        rv.adapter = ChannelAdapter(channels) { channel ->
            channel.link?.let { link ->
                val intent = Intent(this@LinksActivity, NewPlayerActivity::class.java).apply {
                    putExtra("isVideoLoop", false)
                    putExtra("videoTittle", matchTitle)
                    putExtra("videoLink", if (!link.mpdLink.isNullOrEmpty()) null else link.linkUrl)
                    putExtra("mpdLink", link.mpdLink)
                    putExtra("mpdKey", link.mpdKey)

                    putExtra("unityAds", false)
                    putExtra("showAdInExo", false)

                    putExtra("bannerAdKey", "")

                    setAction(Intent.ACTION_SEND)
                    setType("text/plain")
                    putExtra(Intent.EXTRA_TEXT, link.linkUrl)
                }
                startActivity(intent)
            }
        }
    }
}
