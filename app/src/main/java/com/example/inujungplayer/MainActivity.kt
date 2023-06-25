package com.example.inujungplayer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.viewpager2.widget.ViewPager2
import com.example.inujungplayer.adapter.ViewPagerAdapter
import com.example.inujungplayer.constant.MusicConstants.allRadio2
import com.example.inujungplayer.databinding.ActivityMainBinding
import com.example.inujungplayer.utils.SetStreamUrl
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView

class MainActivity : AppCompatActivity(),  NavigationBarView.OnItemSelectedListener {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val navView: BottomNavigationView = binding.navView
// 페이저에 어댑터 연결
        binding.viewPager.adapter = ViewPagerAdapter(this)

        binding.viewPager.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    navView.menu.getItem(position).isChecked = true
                }
            }
        )

        // 리스너 연결
        navView.setOnItemSelectedListener(this)
        getRadioAddress()
    }

    private fun getRadioAddress() {
//        fSetPlayableUrl(MusicConstants.RADIO_ADDR.kbs1Radio)
        for (i in allRadio2.indices) {
            val m =(allRadio2[i].addr)
            if (!m.contains("m3u8")) {
                SetStreamUrl().setStreamUrl(i, m)
                Log.d("fSetPlayableUrl","${m}")
            }
        }
    }
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.navigation_home -> {
                binding.viewPager.currentItem = 0
//                supportFragmentManager.beginTransaction().replace(R.id.nav_host_fragment_activity_main , HomeFragment()).commitAllowingStateLoss()
                return true
            }
            R.id.navigation_dashboard -> {
                binding.viewPager.currentItem = 1
//                supportFragmentManager.beginTransaction().replace(R.id.nav_host_fragment_activity_main, DashboardFragment()).commitAllowingStateLoss()
                return true
            }
            R.id.navigation_radio -> {
                binding.viewPager.currentItem = 2
//                supportFragmentManager.beginTransaction().replace(R.id.nav_host_fragment_activity_main, RadioFragment()).commitAllowingStateLoss()
                return true
            }
        }
        return false
    }
}