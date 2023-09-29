package com.example.mfbisnes

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.example.mfbisnes.databinding.ActivityActivitiesBinding
import com.google.firebase.firestore.FirebaseFirestore

class Activities : AppCompatActivity() {

    private lateinit var selectedActivities: MutableList<String>
    private lateinit var binding: ActivityActivitiesBinding
    private lateinit var userA: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        binding = ActivityActivitiesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        userA = intent.getSerializableExtra("user") as User

        selectedActivities = userA.selectedActivities?.toMutableList() ?: mutableListOf()

        binding.cbAdventureOutdoor.isChecked =
            selectedActivities.contains("Adventure & Outdoor Activities")
        binding.cbArtsCulture.isChecked = selectedActivities.contains("Arts & Culture")
        binding.cbFoodDrink.isChecked = selectedActivities.contains("Food & Drink")
        binding.cbFitnessSports.isChecked = selectedActivities.contains("Fitness & Sports")
        binding.cbMusicPerformances.isChecked = selectedActivities.contains("Music & Performance")
        binding.cbNatureWildlife.isChecked = selectedActivities.contains("Nature & Wildlife")
        binding.cbNightlifeSocializing.isChecked =
            selectedActivities.contains("Nightlife & Socializing")
        binding.cbTechnologyGaming.isChecked = selectedActivities.contains("Technology & Gaming")
        binding.cbShoppingFashion.isChecked = selectedActivities.contains("Shopping & Fashion")
        binding.cbTravelSightseeing.isChecked = selectedActivities.contains("Travel & Sightseeing")


        binding.cbAdventureOutdoor.setOnCheckedChangeListener { _, isChecked ->
            handleCheckboxChange("Adventure & Outdoor Activities", isChecked)
        }

        binding.cbArtsCulture.setOnCheckedChangeListener { _, isChecked ->
            handleCheckboxChange("Arts & Culture", isChecked)
        }

        binding.cbFoodDrink.setOnCheckedChangeListener { _, isChecked ->
            handleCheckboxChange("Food & Drink", isChecked)
        }

        binding.cbFitnessSports.setOnCheckedChangeListener { _, isChecked ->
            handleCheckboxChange("Fitness & Sports", isChecked)
        }

        binding.cbMusicPerformances.setOnCheckedChangeListener { _, isChecked ->
            handleCheckboxChange("Music & Performance", isChecked)
        }

        binding.cbNatureWildlife.setOnCheckedChangeListener { _, isChecked ->
            handleCheckboxChange("Nature & Wildlife", isChecked)
        }

        binding.cbNightlifeSocializing.setOnCheckedChangeListener { _, isChecked ->
            handleCheckboxChange("Nightlife & Socializing", isChecked)
        }

        binding.cbTechnologyGaming.setOnCheckedChangeListener { _, isChecked ->
            handleCheckboxChange("Technology & Gaming", isChecked)
        }

        binding.cbShoppingFashion.setOnCheckedChangeListener { _, isChecked ->
            handleCheckboxChange("Shopping & Fashion", isChecked)
        }

        binding.cbTravelSightseeing.setOnCheckedChangeListener { _, isChecked ->
            handleCheckboxChange("Travel & Sightseeing", isChecked)
        }

        binding.btnSaveActivity.setOnClickListener {
            userA.selectedActivities = selectedActivities
            val ref = FirebaseFirestore.getInstance().collection("users").document(userA.uid)
            ref.update("activities", userA.selectedActivities).addOnSuccessListener {
                val intent = Intent(this, Profile::class.java)
                intent.putExtra("user", userA)
                Thread.sleep(1000)
                startActivity(intent)
            }.addOnFailureListener {
                Log.e("HelloTest", userA.selectedActivities.toString())
            }
        }
    }

    private fun handleCheckboxChange(activity: String, isChecked: Boolean) {
        if (isChecked) {
            if (!selectedActivities.contains(activity)) {
                selectedActivities.add(activity)
            }
        } else {
            selectedActivities.remove(activity)
        }
    }
}
