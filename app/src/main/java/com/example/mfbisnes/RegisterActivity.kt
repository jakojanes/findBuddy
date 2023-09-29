package com.example.mfbisnes

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.mfbisnes.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private val db = Firebase.firestore
    private val TAG = "RegisterActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }


        binding.registerButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                // show error message
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // create user with email and password
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // create user data in firestore
                        val user = hashMapOf(
                            "gender" to null,
                            "age" to null,
                            "location" to null,
                            "profile-picture" to null,
                            "account-created" to false,
                            "location" to null,
                            "lat" to null,
                            "lng" to null
                        )
                        val userId = FirebaseAuth.getInstance().currentUser!!.uid

                        db.collection("users").document(userId).set(user)
                            .addOnSuccessListener {
                                Log.d(TAG, "User data added to firestore")
                            }
                            .addOnFailureListener { e ->
                                Log.w(TAG, "Error adding user data to firestore", e)
                            }

                        // go to LoginActivity
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    } else {
                        // show error message
                        Toast.makeText(
                            this,
                            "Registration failed: ${task.exception?.localizedMessage}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }

}