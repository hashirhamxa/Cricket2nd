package livecricket.livecrickettv.cricketstreaming.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import livecricket.livecrickettv.cricketstreaming.fragments.HighlightsFragment
import livecricket.livecrickettv.cricketstreaming.fragments.HomeFragment
import livecricket.livecrickettv.cricketstreaming.fragments.SettingsFragment

class MainPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    private var showHighlights = true

    fun updateHighlightsVisibility(visible: Boolean) {
        if (showHighlights != visible) {
            showHighlights = visible
            notifyDataSetChanged()
        }
    }

    override fun createFragment(position: Int): Fragment {
        return if (showHighlights) {
            when (position) {
                0 -> HomeFragment()
                1 -> HighlightsFragment()
                2 -> SettingsFragment()
                else -> HomeFragment()
            }
        } else {
            when (position) {
                0 -> HomeFragment()
                1 -> SettingsFragment()
                else -> HomeFragment()
            }
        }
    }

    override fun getItemCount(): Int = if (showHighlights) 3 else 2
}
