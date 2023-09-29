package com.example.mfbisnes

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.mfbisnes.databinding.ActivityHomeBinding
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.SetOptions


class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var userA: User
    private val db = FirebaseFirestore.getInstance()
    private val cardStack: MutableList<View> = mutableListOf()
    private var currentCardIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }


        userA = intent.getSerializableExtra("user") as User
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        swipeRefreshLayout.setOnRefreshListener {
            refreshContent()
        }

        val center = userA.lat?.let { userA.lng?.let { it1 -> GeoLocation(it, it1) } }
        val radiusInKm = 50.0
        if (center != null) {
            findUsersWithinRadius(db, center, radiusInKm)
        }
    }

    private fun findUsersWithinRadius(
        db: FirebaseFirestore,
        center: GeoLocation,
        radiusInKm: Double
    ) {
        val radiusInM = radiusInKm * 1000.0
        val bounds = GeoFireUtils.getGeoHashQueryBounds(center, radiusInM)
        val tasks: MutableList<Task<QuerySnapshot>> = ArrayList()
        for (b in bounds) {
            val q = db.collection("users")
                .orderBy("location")
                .startAt(b.startHash)
                .endAt(b.endHash)
            tasks.add(q.get())
        }

        Tasks.whenAllComplete(tasks)
            .addOnCompleteListener {
                val matchingUsers = mutableMapOf<String, List<String>>()
                val userLikesReference = db.collection("userLikes").document(userA.uid)
                userLikesReference.get().addOnSuccessListener { documentSnapshot ->
                    val likedUserIds = documentSnapshot.data?.keys?.toSet() ?: emptySet()
                    for (task in tasks) {
                        val snap = task.result
                        for (doc in snap!!.documents) {
                            val userId = doc.id
                            if (userId == userA.uid || likedUserIds.contains(userId)) {
                                continue
                            }

                            val lat = doc.getDouble("lat")!!
                            val lng = doc.getDouble("lng")!!

                            val interest =
                                if (doc.contains("activities")) doc.get("activities")!! as List<*> else null
                            if (interest != null) {
                                val docLocation = GeoLocation(lat, lng)
                                val distanceInM =
                                    GeoFireUtils.getDistanceBetween(docLocation, center)
                                if (distanceInM <= radiusInM) {
                                    val sharedInterests =
                                        interest.filter { userA.selectedActivities?.contains(it) == true }
                                    if (sharedInterests.isNotEmpty()) {
                                        matchingUsers[userId] = sharedInterests as List<String>
                                    }
                                }
                            }
                        }
                    }
                    if (matchingUsers.isNotEmpty()) {
                        generateUserCards(matchingUsers, this)
                        binding.noMatchesTextView.visibility = View.GONE
                        val pictureLayout = findViewById<LinearLayout>(R.id.pictureLayout)
                        pictureLayout.visibility = View.VISIBLE
                    } else {
                        Toast.makeText(this, "No matches found", Toast.LENGTH_SHORT).show()
                        binding.noMatchesTextView.visibility = View.VISIBLE
                        val pictureLayout = findViewById<LinearLayout>(R.id.pictureLayout)
                        pictureLayout.visibility = View.GONE
                    }
                }
            }
    }

    private fun generateUserCards(
        matchingUsers: MutableMap<String, List<String>>,
        context: Context
    ) {
        val userIds = matchingUsers.keys.toList()

        for (userId in userIds) {
            // Retrieve the profile picture URL for the current user
            retrieveProfilePictureUrl(userId) { profilePictureUrl: String ->
                val cardView = LayoutInflater.from(context).inflate(R.layout.card_user, null)
                val imageView = cardView.findViewById<ImageView>(R.id.imageView)
                val interestsContainer = cardView.findViewById<LinearLayout>(R.id.interestsContainer)
                val buttonView = cardView.findViewById<Button>(R.id.likeButton)
                val buttonViewSkip = cardView.findViewById<Button>(R.id.skipBtn)

                // Set the data for the views
                Glide.with(context)
                    .load(profilePictureUrl)
                    .placeholder(R.drawable.account_circle)
                    .apply(RequestOptions.circleCropTransform())
                    .into(imageView)

                matchingUsers[userId]?.forEach { interest ->
                    val itemView = LayoutInflater.from(context).inflate(R.layout.item_interest, null)
                    val textView = itemView.findViewById<TextView>(R.id.interestTextView)
                    textView.text = interest

                    interestsContainer.addView(itemView)
                }


                buttonView.setOnClickListener {
                    onLikeButtonClicked(userId)
                }
                buttonViewSkip.setOnClickListener {
                    showNextCard()
                }

                cardStack.add(cardView)

                if (cardStack.size == userIds.size) {
                    showNextCard()
                }
            }
        }
    }


    private fun retrieveProfilePictureUrl(
        userId: String,
        callback: (profilePictureUrl: String) -> Unit
    ) {
        val docRef = db.collection("users").document(userId)

        docRef.addSnapshotListener { documentSnapshot, exception ->
            if (exception != null) {
                Log.e("HomeActivity", "Error listening to Firestore document: $exception")
                return@addSnapshotListener
            }
            if (documentSnapshot != null && documentSnapshot.data != null) {
                val profilePictureUrl =
                    documentSnapshot.data!!["profile-picture"] as? String ?: ""
                callback(profilePictureUrl)
            }
        }
    }

    private fun onLikeButtonClicked(userId: String) {
        val userLikesReference = db.collection("userLikes").document(userA.uid)
        userLikesReference.set(mapOf(userId to true), SetOptions.merge())
        Log.e("HelloTest", userLikesReference.toString())
        Toast.makeText(this, "You just liked $userId", Toast.LENGTH_SHORT).show()

        db.collection("userLikes").document(userId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.contains(userA.uid)) {
                    val matchReference = db.collection("matches").document()
                    matchReference.set(
                        mapOf(
                            "userA" to userA.uid,
                            "userB" to userId
                        )
                    )
                    Toast.makeText(this, "It's a match!", Toast.LENGTH_SHORT).show()

                    val sharedPref = this.getSharedPreferences(
                        "com.example.app",
                        Context.MODE_PRIVATE
                    )
                    val editor = sharedPref.edit()
                    editor.putStringSet(
                        "matches",
                        sharedPref.getStringSet("matches", setOf())
                            ?.plus(matchReference.id)
                    )
                    editor.apply()


                }
                showNextCard()
            }
    }

    private fun showNextCard() {
        if (cardStack.isNotEmpty()) {
            currentCardIndex = cardStack.indices.random()
            val currentCard = cardStack[currentCardIndex]
            val pictureLayout = findViewById<LinearLayout>(R.id.pictureLayout)
            pictureLayout.removeAllViews()
            pictureLayout.addView(currentCard)
            cardStack.removeAt(currentCardIndex)
        } else {
            Toast.makeText(this, "No more matches", Toast.LENGTH_SHORT).show()
            binding.noMatchesTextView.visibility = View.VISIBLE
            val pictureLayout = findViewById<LinearLayout>(R.id.pictureLayout)
            pictureLayout.visibility = View.GONE
        }
    }

    private fun refreshContent() {
        // Clear the existing content
        val pictureLayout = findViewById<LinearLayout>(R.id.pictureLayout)
        pictureLayout.removeAllViews()
        cardStack.clear()

        // Fetch new data and update UI
        val center = userA.lat?.let { userA.lng?.let { it1 -> GeoLocation(it, it1) } }
        val radiusInKm = 50.0
        if (center != null) {
            findUsersWithinRadius(db, center, radiusInKm)
        }

        swipeRefreshLayout.isRefreshing = false
    }

    fun goToChat(view: View?) {
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra("user", userA)
        startActivity(intent)
    }

    fun goToProfile(view: View?) {
        val intent = Intent(this, Profile::class.java)
        intent.putExtra("user", userA)
        startActivity(intent)
    }

    fun goToHome(view: View?) {

    }

}


