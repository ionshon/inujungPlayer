package com.example.inujungplayer.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.inujungplayer.ui.dashboard.DashboardFragment
import com.example.inujungplayer.ui.home.HomeFragment
import com.example.inujungplayer.ui.radio.RadioFragment

class ViewPagerAdapter  (fragment : FragmentActivity) : FragmentStateAdapter(fragment){
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> HomeFragment()
            1 -> DashboardFragment()
            else -> RadioFragment()
        }
    }
}