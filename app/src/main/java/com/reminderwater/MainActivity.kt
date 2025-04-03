package com.reminderwater

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        enableEdgeToEdge()

        firebaseAuth = FirebaseAuth.getInstance()
        val currentUser = firebaseAuth.currentUser

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        if (currentUser != null) {
            navController.navigate(R.id.action_viewPager_to_water)
            finish()
            return
        }

        val navOptions = NavOptions.Builder()
            .setPopUpTo(R.id.viewPager, true)
            .build()

        if (onBoardingFinish()) {
            navController.navigate(R.id.login, null, navOptions)
        } else {
            navController.navigate(R.id.viewPager)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun onBoardingFinish(): Boolean {
        val sharedPreferences = getSharedPreferences("onBoarding", Context.MODE_PRIVATE)
        val finished = sharedPreferences.getBoolean("Finished", false)
        Log.d("MainActivity", "Onboarding Finished: $finished")
        return finished
    }

    override fun onBackPressed() {
        if (onBoardingFinish()) {
            if (navController.currentDestination?.id == R.id.viewPager) {
                return
            }
        }
        super.onBackPressed()
    }
}