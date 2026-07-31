package livecricket.livecrickettv.cricketstreaming.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import livecricket.livecrickettv.cricketstreaming.adapters.MainPagerAdapter
import livecricket.livecrickettv.cricketstreaming.viewmodels.MainViewModel
import livecricket.livecrickettv.cricketstreaming.R

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var bottomNavigationView: BottomNavigationView

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewPager = findViewById(R.id.view_pager)
        bottomNavigationView = findViewById(R.id.bottom_navigation)

        setupViewPager()
        setupBottomNavigation()
        observeViewModel()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.showHighlights.collect { show ->
                    bottomNavigationView.menu.findItem(R.id.navigation_highlights).isVisible = show
                    
                    (viewPager.adapter as? MainPagerAdapter)?.updateHighlightsVisibility(show)
                    
                    // If current item is highlights and it's now hidden, move to home
                    if (!show && viewPager.currentItem == 1) {
                        viewPager.currentItem = 0
                    }
                }
            }
        }
    }

    private fun setupViewPager() {
        val adapter = MainPagerAdapter(this)
        viewPager.adapter = adapter

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                val showHighlights = (viewPager.adapter as? MainPagerAdapter)?.let { 
                    try {
                        val field = it.javaClass.getDeclaredField("showHighlights")
                        field.isAccessible = true
                        field.get(it) as Boolean
                    } catch (e: Exception) {
                        true
                    }
                } ?: true

                val itemId = if (showHighlights) {
                    when (position) {
                        1 -> R.id.navigation_highlights
                        2 -> R.id.navigation_settings
                        else -> R.id.navigation_home
                    }
                } else {
                    when (position) {
                        1 -> R.id.navigation_settings
                        else -> R.id.navigation_home
                    }
                }

                if (bottomNavigationView.selectedItemId != itemId) {
                    bottomNavigationView.selectedItemId = itemId
                }
            }
        })
    }

    private fun setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener { item ->
            val showHighlights = (viewPager.adapter as? MainPagerAdapter)?.let {
                try {
                    val field = it.javaClass.getDeclaredField("showHighlights")
                    field.isAccessible = true
                    field.get(it) as Boolean
                } catch (e: Exception) {
                    true
                }
            } ?: true

            when (item.itemId) {
                R.id.navigation_home -> {
                    if (viewPager.currentItem != 0) viewPager.currentItem = 0
                    true
                }
                R.id.navigation_highlights -> {
                    if (showHighlights) {
                        if (viewPager.currentItem != 1) viewPager.currentItem = 1
                        true
                    } else false
                }
                R.id.navigation_settings -> {
                    val targetPos = if (showHighlights) 2 else 1
                    if (viewPager.currentItem != targetPos) viewPager.currentItem = targetPos
                    true
                }
                else -> false
            }
        }
    }
}
