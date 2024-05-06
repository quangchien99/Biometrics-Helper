package com.qcp.biometrics.helper.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.tabs.TabLayoutMediator
import com.qcp.biometrics.helper.base.BaseFragment
import com.qcp.biometrics.helper.base.FragmentAdapter
import com.qcp.biometrics.helper.databinding.ActivityMainBinding

/**
 * @author chienpham
 * @since 06/05/2024
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val fragments: ArrayList<BaseFragment> by lazy {
        arrayListOf(
            NormalBiometricFragment(),
            CryptoObjectBiometricFragment()
        )
    }

    private val pagerAdapter: FragmentAdapter by lazy {
        FragmentAdapter(this, fragments)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        setUpViews()
    }

    private fun setUpViews() {
        binding.viewPager.adapter = pagerAdapter

        // Set up the TabLayout with the ViewPager
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = fragments[position].getName()
        }.attach()
    }
}