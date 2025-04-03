package com.reminderwater.Dashbord

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.reminderwater.MyApplication.Companion.firebaseAuth
import com.reminderwater.R

class Profile : Fragment() {

    private lateinit var etUsername: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var etHeight: EditText
    private lateinit var etWeight: EditText
    private lateinit var etBirthday: EditText
    private lateinit var btnUpdate: Button
    private lateinit var btnLogout: Button
    private lateinit var btnEdit: ImageButton
    private lateinit var btnBack: ImageButton

    private val database = FirebaseDatabase.getInstance("https://reminder-water-23a84-default-rtdb.europe-west1.firebasedatabase.app/")
    private val userRef = database.getReference("users")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // Initialize UI elements
        etUsername = view.findViewById(R.id.userName)
        etEmail = view.findViewById(R.id.etEmail)
        etPassword = view.findViewById(R.id.etPassword)
        etConfirmPassword = view.findViewById(R.id.confirmPassword)
        etHeight = view.findViewById(R.id.etHeight)
        etWeight = view.findViewById(R.id.etWeight)
        etBirthday = view.findViewById(R.id.etBirthday)
        btnUpdate = view.findViewById(R.id.btnUpdate)
        btnLogout = view.findViewById(R.id.btnLogout)
        btnEdit = view.findViewById(R.id.edite)
        btnBack = view.findViewById(R.id.back)

        setEditingEnabled(false)

        fetchUserData()

        // Set up button click listeners
        btnLogout.setOnClickListener {
            logoutUser()
        }

        btnEdit.setOnClickListener {
            setEditingEnabled(true)
        }

        btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        btnUpdate.setOnClickListener {
            updateUserData()
        }

        val bottomNavigationView: BottomNavigationView = view.findViewById(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.page_4 // Set Profile as selected

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.page_1 -> {
                    findNavController().navigate(R.id.action_profile_to_water)
                    true
                }
                R.id.page_2 -> {
                    findNavController().navigate(R.id.action_profile_to_analysis)
                    true
                }
                R.id.page_3 -> {
                    findNavController().navigate(R.id.action_profile_to_notification)
                    true
                }
                R.id.page_4 -> {
                    true
                }
                else -> false
            }
        }
        val menuItemId = bottomNavigationView.menu.findItem(R.id.page_3).itemId
        val badge = bottomNavigationView.getOrCreateBadge(menuItemId)
        badge.isVisible = true
        badge.number = 1

        return view
    }

    private fun setEditingEnabled(enabled: Boolean) {
        etUsername.isEnabled = enabled
        etEmail.isEnabled = false
        etPassword.isEnabled = enabled
        etConfirmPassword.isEnabled = enabled
        etHeight.isEnabled = enabled
        etWeight.isEnabled = enabled
        etBirthday.isEnabled = enabled
        btnUpdate.isEnabled = enabled
    }

    private fun fetchUserData() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid

            userRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val userData = snapshot.value as? Map<*, *>

                        if (userData != null) {
                            etUsername.setText(userData["fullName"]?.toString() ?: "")
                            etEmail.setText(currentUser.email ?: "")
                            etPassword.setText("********") // Don't show actual password
                            etConfirmPassword.setText("********")
                            etHeight.setText(userData["height"]?.toString() ?: "")
                            etWeight.setText(userData["weight"]?.toString() ?: "")
                            etBirthday.setText(userData["birthday"]?.toString() ?: "")
                        }
                    } else {
                        // Create user data if it doesn't exist
                        val userMap = HashMap<String, Any>()
                        userMap["fullName"] = currentUser.displayName ?: ""
                        userMap["email"] = currentUser.email ?: ""

                        userRef.child(userId).setValue(userMap)
                            .addOnSuccessListener {
                                etUsername.setText(currentUser.displayName ?: "")
                                etEmail.setText(currentUser.email ?: "")
                            }
                            .addOnFailureListener {
                                Toast.makeText(requireContext(), "Failed to create user profile", Toast.LENGTH_SHORT).show()
                            }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun updateUserData() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid

            // Validate inputs
            val fullName = etUsername.text.toString().trim()
            val height = etHeight.text.toString().trim()
            val weight = etWeight.text.toString().trim()
            val birthday = etBirthday.text.toString().trim()
            val password = etPassword.text.toString()
            val confirmPassword = etConfirmPassword.text.toString()

            if (fullName.isEmpty()) {
                etUsername.error = "Name cannot be empty"
                return
            }

            // Update user data in Firebase
            val userMap = HashMap<String, Any>()
            userMap["fullName"] = fullName

            if (height.isNotEmpty()) {
                userMap["height"] = height
            }

            if (weight.isNotEmpty()) {
                userMap["weight"] = weight
            }

            if (birthday.isNotEmpty()) {
                userMap["birthday"] = birthday
            }

            userRef.child(userId).updateChildren(userMap)
                .addOnSuccessListener {
                    if (password != "********" && password == confirmPassword) {
                        currentUser.updatePassword(password)
                            .addOnSuccessListener {
                                Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show()
                                setEditingEnabled(false)
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(requireContext(), "Failed to update password: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    } else if (password != confirmPassword) {
                        Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show()
                        setEditingEnabled(false)
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Failed to update profile: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun logoutUser() {
        if (firebaseAuth.currentUser != null) {
            firebaseAuth.signOut()
            Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_profile_to_login)
        } else {
            Toast.makeText(requireContext(), "No user is currently logged in", Toast.LENGTH_SHORT).show()
        }
    }
}