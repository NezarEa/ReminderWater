package com.reminderwater.OnBoarding

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.reminderwater.R

class Third : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_third, container, false)
        val viewPager = activity?.findViewById<ViewPager2>(R.id.viewPager)
        val finish = view.findViewById<Button>(R.id.finish)

        finish.setOnClickListener {
            findNavController().navigate(R.id.action_viewPager_to_login)
            onBoardingFinished()
        }

        return view
    }

    private fun onBoardingFinished() {
        val sharedPreferences = requireActivity().getSharedPreferences("onBoarding", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("Finished", true)
        editor.apply()
        Log.d("OnBoarding", "Onboarding Finished: ${sharedPreferences.getBoolean("Finished", false)}")
    }
}
