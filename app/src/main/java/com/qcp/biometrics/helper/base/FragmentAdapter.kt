package com.qcp.biometrics.helper.base

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
/**
 * @author chienpham
 * @since 06/05/2024
 */
class FragmentAdapter(
    fragmentActivity: FragmentActivity,
    private val fragments: ArrayList<BaseFragment>
) :
    FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }
}
