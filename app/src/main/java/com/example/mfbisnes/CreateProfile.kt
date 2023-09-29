package com.example.mfbisnes

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.Toast
import com.example.mfbisnes.databinding.ActivityCreateProfileBinding
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.io.Serializable
import java.util.*
import com.bumptech.glide.Glide
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.yalantis.ucrop.UCrop
import java.io.File
import kotlin.math.log
import kotlin.properties.Delegates

class CreateProfile : AppCompatActivity() {
    private lateinit var binding: ActivityCreateProfileBinding
    private lateinit var location: String
    private lateinit var userA: User
    private lateinit var activities: List<String>
    private var lat by Delegates.notNull<Double>()
    private var lng by Delegates.notNull<Double>()
    private val db = Firebase.firestore
    private var selectedPhotoUri: Uri? = null
    private lateinit var imageUrl: String
    private val TAG = "CreateProfileError"
    private val userId = FirebaseAuth.getInstance().currentUser!!.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Places.initialize(applicationContext, "AIzaSyDzCNJ32APlCPf89N6qVZnnkjtUhS2d7gA")
        binding = ActivityCreateProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        val autocompleteFragment =
            supportFragmentManager.findFragmentById(R.id.placeComplete)
                    as AutocompleteSupportFragment
        autocompleteFragment.setPlaceFields(
            listOf(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.LAT_LNG
            )
        )


        autocompleteFragment.setTypeFilter(TypeFilter.CITIES)
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                // TODO: Get info about the selected place.
                autocompleteFragment.setText(place.name)
                lat = place.latLng?.latitude!!
                lng = place.latLng?.longitude!!
                val hash = lat
                    ?.let { lng?.let { it1 -> GeoLocation(it, it1) } }
                    ?.let { GeoFireUtils.getGeoHashForLocation(it) }
                if (hash != null) {
                    location = hash
                }

            }

            override fun onError(status: Status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: $status")
            }
        })


        if (intent.hasExtra("user")) {
            userA = intent.getSerializableExtra("user") as User
            location = userA.location.toString()
            lat = userA.lat!!
            lng = userA.lng!!


        } else {
            Log.e("HelloTest", "here3")
            userA = User(userId, null, null, null, null, null, null, false, null)
            location = userA.location.toString()


        }

        Log.e("HelloTest", "Made it here")



        imageUrl = userA.profilePicture.toString()
        Glide.with(this)
            .load(userA.profilePicture).placeholder(R.drawable.ic_baseline_account_circle_24)
            .into(binding.profilePic)


        binding.btnPicture.setOnClickListener {
            val galleryIntent = Intent(Intent.ACTION_PICK)
            galleryIntent.type = "image/*"
            startActivityForResult(galleryIntent, 0)
        }
        binding.saveProfileButton.setOnClickListener {
            try {
                performRegister()
            } catch (e: Exception) {
                Log.e(TAG, "Error writing to Firebase Firestore: ", e)

            }
        }

        if (userA.age != null) {
            binding.ageEditText.setText(userA.age)
        }
        if (userA.gender != null) {
            when (userA.gender) {
                "male" -> binding.genderMale.isChecked = true
                "female" -> binding.genderFemale.isChecked = true
            }
        }


    }


    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            selectedPhotoUri = data.data
            binding.saveProfileButton.visibility = View.INVISIBLE
            UCrop.of(
                selectedPhotoUri!!,
                Uri.fromFile(File(cacheDir, UUID.randomUUID().toString() + ".jpg"))
            )
                .withAspectRatio(1f, 1f)
                .withMaxResultSize(1024, 1024)
                .start(this)


        }



        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            val resultUri = UCrop.getOutput(data!!)
            selectedPhotoUri = resultUri
            Glide.with(this).load(resultUri).into(binding.profilePic)
            uploadImage()


        }
    }

    private fun performRegister() {

        val gender = when (binding.genderGroup.checkedRadioButtonId) {
            R.id.gender_male -> "male"
            R.id.gender_female -> "female"
            else -> ""
        }
        val age = binding.ageEditText.text.toString()


        if (userA.selectedActivities != null) {
            activities = userA.selectedActivities!!
        } else {
            activities = emptyList()
        }

        val user = hashMapOf(
            "gender" to gender,
            "age" to age,
            "location" to location,
            "profile-picture" to imageUrl,
            "account-created" to true,
            "lat" to lat,
            "lng" to lng,
            "activities" to activities
        )


        userA.profilePicture = imageUrl
        userA.gender = gender
        userA.age = age
        userA.location = location
        userA.accountCreated = true
        userA.lng = lng
        userA.lat = lat



        db.collection("users").document(userId).set(user).addOnSuccessListener {
            Log.d(TAG, "DocumentSnapshot successfully written!")
            Toast.makeText(this, "Success.", Toast.LENGTH_SHORT).show()

        }.addOnFailureListener { error ->
            Log.w(TAG, "Error writing document", error)
            Toast.makeText(this, "Failed.", Toast.LENGTH_SHORT).show()
        }

        val intent = Intent(this, HomeActivity::class.java)
        intent.putExtra("user", userA)
        startActivity(intent)
        finish()
    }

    private fun uploadImage() {
        if (selectedPhotoUri == null) return
        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")
        ref.putFile(selectedPhotoUri!!).addOnSuccessListener {
            Log.d("CreateProfile", "Picture uploaded")
            ref.downloadUrl.addOnSuccessListener {
                imageUrl = it.toString()
                binding.saveProfileButton.visibility = View.VISIBLE
                Log.e(TAG, "made it here 2 $imageUrl")
            }
        }

    }


}


class User(
    val uid: String,
    var gender: String?,
    var age: String?,
    var location: String?,
    var profilePicture: String?,
    var lat: Double?,
    var lng: Double?,
    var accountCreated: Boolean,
    var selectedActivities: List<String>?
) : Serializable