package com.reminderwater.OnBoarding.Controle

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.reminderwater.OnBoarding.First
import com.reminderwater.OnBoarding.Second
import com.reminderwater.OnBoarding.Third
import com.reminderwater.R

class ViewPager : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_view_pager, container, false)
        val fragments = arrayListOf(
            First(),
            Second(),
            Third()
        )
        val adapter = ViewPagerAdapter(fragments, childFragmentManager, lifecycle)
        view.findViewById<ViewPager2>(R.id.viewPager).adapter = adapter
        return view
    }

}