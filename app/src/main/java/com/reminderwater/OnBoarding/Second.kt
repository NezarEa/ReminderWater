package com.reminderwater.OnBoarding

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.viewpager2.widget.ViewPager2
import com.reminderwater.R

class Second : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_second, container, false)
        val viewPager = activity?.findViewById<ViewPager2>(R.id.viewPager)
        val next = view.findViewById<Button>(R.id.next)
        next.setOnClickListener {
            viewPager?.currentItem = 2
        }
        return view
    }

}