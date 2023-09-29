package com.example.mfbisnes


import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.mfbisnes.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth


class Profile : AppCompatActivity() {

    private lateinit var userA: User
    private lateinit var profilePictureImageView: ImageView
    private lateinit var interestsContainer: LinearLayout
    private lateinit var editProfileButton: Button
    private lateinit var editActivitiesButton: Button
    private lateinit var binding: com.example.mfbisnes.databinding.ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        userA = intent.getSerializableExtra("user") as User


        profilePictureImageView = findViewById(R.id.profilePictureImageView)
        interestsContainer = findViewById(R.id.interestsContainer)
        editProfileButton = findViewById(R.id.editProfileButton)
        editActivitiesButton = findViewById(R.id.editActivitiesButton)


        Glide.with(this)
            .load(userA.profilePicture)
            .placeholder(R.drawable.account_circle)
            .into(profilePictureImageView)


        interestsContainer.removeAllViews()

        interestsContainer.removeAllViews()


        userA.selectedActivities?.forEach { activity ->
            val interestView = layoutInflater.inflate(R.layout.item_interest, null)
            val interestTextView = interestView.findViewById<TextView>(R.id.interestTextView)
            interestTextView.text = activity

            interestsContainer.addView(interestView)
        }


        editProfileButton.setOnClickListener {
            val intent = Intent(this, CreateProfile::class.java)
            intent.putExtra("user", userA)
            startActivity(intent)
        }

        editActivitiesButton.setOnClickListener {
            val intent = Intent(this, Activities::class.java)
            intent.putExtra("user", userA)
            startActivity(intent)
        }

    }

    fun goToChat(view: View?) {
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra("user", userA)
        startActivity(intent)
    }

    fun goToProfile(view: View?) {

    }

    fun goToHome(view: View?) {
        val intent = Intent(this, HomeActivity::class.java)
        intent.putExtra("user", userA)
        startActivity(intent)
    }

    fun logOut(view: View?) {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }
}
