package livecricket.livecrickettv.cricketstreaming

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewPager = findViewById(R.id.view_pager)
        bottomNavigationView = findViewById(R.id.bottom_navigation)

        setupViewPager()
        setupBottomNavigation()
    }

    private fun setupViewPager() {
        val adapter = MainPagerAdapter(this)
        viewPager.adapter = adapter

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                val itemId = when (position) {
                    1 -> R.id.navigation_highlights
                    2 -> R.id.navigation_settings
                    else -> R.id.navigation_home
                }

                if (bottomNavigationView.selectedItemId != itemId) {
                    bottomNavigationView.selectedItemId = itemId
                }
            }
        })
    }

    private fun setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    if (viewPager.currentItem != 0) viewPager.currentItem = 0
                    true
                }
                R.id.navigation_highlights -> {
                    if (viewPager.currentItem != 1) viewPager.currentItem = 1
                    true
                }
                R.id.navigation_settings -> {
                    if (viewPager.currentItem != 2) viewPager.currentItem = 2
                    true
                }
                else -> false
            }
        }
    }
}
