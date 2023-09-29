package com.example.mfbisnes

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.mfbisnes.databinding.ActivityMainBinding
import com.google.android.libraries.places.api.Places
import com.google.firebase.auth.FirebaseAuth


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        val sharedPreferences = getSharedPreferences("USER_STATE", MODE_PRIVATE)
        val logged = sharedPreferences.getBoolean("logged", false)
        val curUser = FirebaseAuth.getInstance().currentUser?.uid

        if (logged && curUser != null){
            startActivity(Intent(this, WaitingActivityLogin::class.java))
            finish()
        }


        binding.loginButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))

        }

        binding.registerButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))

        }
    }
}