package com.reminderwater.Auth

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase
import com.reminderwater.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class Sign : Fragment() {

    private lateinit var back: ImageButton
    private lateinit var etUserName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var etHeight: EditText
    private lateinit var etWeight: EditText
    private lateinit var etBirthday: EditText
    private lateinit var btnSignUp: Button
    private lateinit var linkLogin: TextView

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_sign, container, false)

        firebaseAuth = FirebaseAuth.getInstance()
        database =
            FirebaseDatabase.getInstance("https://reminder-water-23a84-default-rtdb.europe-west1.firebasedatabase.app/")

        initializeViews(view)
        setupListeners()
        setupTextWatchers()

        return view
    }

    private fun initializeViews(view: View) {
        back = view.findViewById(R.id.back)
        etUserName = view.findViewById(R.id.userName)
        etEmail = view.findViewById(R.id.etEmail)
        etPassword = view.findViewById(R.id.etPassword)
        etConfirmPassword = view.findViewById(R.id.confirmPassword)
        etHeight = view.findViewById(R.id.etHeight)
        etWeight = view.findViewById(R.id.etWeight)
        etBirthday = view.findViewById(R.id.etBirthday)
        btnSignUp = view.findViewById(R.id.btnSignUp)
        linkLogin = view.findViewById(R.id.linkLogin)
    }

    private fun setupListeners() {
        btnSignUp.setOnClickListener { validateInputs() }
        back.setOnClickListener { navigateToLogin() }
        linkLogin.setOnClickListener { navigateToLogin() }
    }

    private fun setupTextWatchers() {
        etBirthday.addTextChangedListener(birthdayTextWatcher)
    }

    private val birthdayTextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable?) {
            val input = s.toString()
            val cleanedInput = input.replace("/", "")
            val formattedInput = StringBuilder()
            for (i in cleanedInput.indices) {
                if (i == 2 || i == 4) {
                    formattedInput.append('/')
                }
                formattedInput.append(cleanedInput[i])
            }

            if (input != formattedInput.toString()) {
                etBirthday.removeTextChangedListener(this)
                etBirthday.setText(formattedInput.toString())
                etBirthday.setSelection(formattedInput.length)
                etBirthday.addTextChangedListener(this)
            }

            if (formattedInput.length == 10) {
                if (!isValidDate(formattedInput.toString())) {
                    etBirthday.error = "Invalid date"
                } else if (!isUser18OrOlder(formattedInput.toString())) {
                    etBirthday.error = "You must be 18 years or older"
                } else {
                    etBirthday.error = null
                }
            }
        }
    }

    private fun isValidDate(date: String): Boolean {
        val parts = date.split("/")
        if (parts.size != 3) return false

        val day = parts[0].toIntOrNull() ?: return false
        val month = parts[1].toIntOrNull() ?: return false
        val year = parts[2].toIntOrNull() ?: return false

        return day in 1..31 && month in 1..12 && year in 1900..2100
    }

    private fun isUser18OrOlder(date: String): Boolean {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val birthday = dateFormat.parse(date) ?: return false
        val today = Calendar.getInstance()
        val birthDate = Calendar.getInstance().apply { time = birthday }
        var age = today.get(Calendar.YEAR) - birthDate.get(Calendar.YEAR)
        if (today.get(Calendar.DAY_OF_YEAR) < birthDate.get(Calendar.DAY_OF_YEAR)) {
            age--
        }

        return age >= 18
    }

    private fun validateInputs() {
        val username = etUserName.text?.toString()?.trim()
        val email = etEmail.text?.toString()?.trim()
        val password = etPassword.text?.toString()?.trim()
        val confirmPassword = etConfirmPassword.text?.toString()?.trim()
        val height = etHeight.text?.toString()?.trim()
        val weight = etWeight.text?.toString()?.trim()
        val birthday = etBirthday.text?.toString()?.trim()

        when {
            username.isNullOrEmpty() -> showToast("Please enter your full name")
            email.isNullOrEmpty() -> showToast("Please enter your email address")
            password.isNullOrEmpty() || password.length < 8 -> showToast("Password must be at least 8 characters")
            password != confirmPassword -> showToast("Passwords do not match")
            height.isNullOrEmpty() -> showToast("Please enter your height")
            weight.isNullOrEmpty() -> showToast("Please enter your weight")
            birthday.isNullOrEmpty() -> showToast("Please enter your birthday")
            else -> {
                val heightInt = height.toIntOrNull()
                val weightInt = weight.toIntOrNull()

                if (heightInt == null || weightInt == null) {
                    showToast("Invalid height or weight")
                } else {
                    signUpUser(username, email, password, heightInt, weightInt, birthday)
                }
            }
        }
    }

    private fun signUpUser(
        name: String,
        email: String,
        password: String,
        height: Int,
        weight: Int,
        birthday: String
    ) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    user?.let {
                        // Update user profile with display name
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(name)
                            .build()

                        it.updateProfile(profileUpdates).addOnCompleteListener { profileTask ->
                            if (profileTask.isSuccessful) {
                                saveUserDataToDatabase(
                                    it.uid,
                                    name,
                                    email,
                                    height,
                                    weight,
                                    birthday
                                )
                            } else {
                                showToast("Error updating profile: ${profileTask.exception?.message}")
                            }
                        }
                    }
                } else {
                    showToast("Registration failed: ${task.exception?.message}")
                }
            }
    }

    private fun saveUserDataToDatabase(
        uid: String,
        name: String,
        email: String,
        height: Int,
        weight: Int,
        birthday: String
    ) {
        val userRef = database.reference.child("users").child(uid)

        val userData = hashMapOf(
            "name" to name,
            "email" to email,
            "height" to height,
            "weight" to weight,
            "birthday" to birthday,
            "createdAt" to System.currentTimeMillis()
        )

        userRef.setValue(userData)
            .addOnSuccessListener {
                showToast("Registration successful!")
                navigateToWaterFragment()
            }
            .addOnFailureListener { e ->
                showToast("Error saving user data: ${e.message}")
            }
    }

    private fun navigateToLogin() {
        findNavController().navigate(R.id.action_sign_to_login)
    }

    private fun navigateToWaterFragment() {
        findNavController().navigate(R.id.action_sign_to_water)
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}