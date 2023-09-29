package com.example.mfbisnes

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.mfbisnes.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val db = Firebase.firestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        binding.loginButton.setOnClickListener {

            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.registerButton.setOnClickListener {
                val intent = Intent(this, RegisterActivity::class.java)
                startActivity(intent)
            }

            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val userId = FirebaseAuth.getInstance().currentUser!!.uid
                        val docRef = db.collection("users").document(userId)

                        docRef.get().addOnSuccessListener { document ->
                            if (document != null && document.data != null) {
                                val accountCreated =
                                    document.data?.get("account-created") as Boolean
                                if (accountCreated) {
                                    val sharedPreferences =
                                        getSharedPreferences("USER_STATE", MODE_PRIVATE)
                                    val editor = sharedPreferences.edit()
                                    editor.putBoolean("logged", true)
                                    editor.apply()
                                    val intent = Intent(this, WaitingActivityLogin::class.java)
                                    startActivity(intent)
                                    finish()
                                } else {
                                    val intent = Intent(this, CreateProfile::class.java)
                                    Log.e("HelloTest", "Made it here")
                                    startActivity(intent)
                                    finish()
                                }
                            } else {
                                Toast.makeText(
                                    this,
                                    "Error occurred while fetching user data",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }.addOnFailureListener { exception ->
                            Log.d(TAG, "Error getting documents: ${exception.message}")
                        }
                    }
                }
        }
    }
}