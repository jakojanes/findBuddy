package com.example.mfbisnes

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.example.mfbisnes.databinding.ActivityChatBinding
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var userA: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        userA = intent.getSerializableExtra("user") as User
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val db = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        val currentUserId = auth.currentUser?.uid

        val matchedUsers = mutableListOf<Match>()

        val tasks = mutableListOf<Task<*>>()

        db.collection("matches")
            .whereEqualTo("userA", currentUserId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot) {
                    val matchedUserId = document.getString("userB")
                    if (matchedUserId != null) {
                        val userDoc = db.collection("users").document(matchedUserId)
                        val task = userDoc.get().continueWith { userSnapshot ->
                            val userImage = userSnapshot.result?.getString("profile-picture")
                            Match(matchedUserId, userImage)
                        }
                        tasks.add(task)
                    }
                }
                continueFetchingMatches(db, currentUserId, tasks, matchedUsers)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error getting matches: $exception")
            }
    }

    private fun continueFetchingMatches(
        db: FirebaseFirestore,
        currentUserId: String?,
        tasks: MutableList<Task<*>>,
        matchedUsers: MutableList<Match>
    ) {
        db.collection("matches")
            .whereEqualTo("userB", currentUserId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot) {
                    val matchedUserId = document.getString("userA")
                    if (matchedUserId != null) {
                        val userDoc = db.collection("users").document(matchedUserId)
                        val task = userDoc.get().continueWith { userSnapshot ->
                            val userImage = userSnapshot.result?.getString("profile-picture")
                            Match(matchedUserId, userImage)
                        }
                        tasks.add(task)
                    }
                }
                Tasks.whenAllComplete(tasks)
                    .addOnSuccessListener { taskList ->
                        for (task in taskList) {
                            if (task.isSuccessful) {
                                val match = task.result as? Match
                                if (match != null) {
                                    matchedUsers.add(match)
                                }
                            }
                        }

                        val adapter = MatchAdapter(matchedUsers)
                        binding.matchList.adapter = adapter
                    }
            }
            .addOnFailureListener { exception ->
                // Handle error
                Log.e(TAG, "Error getting matches: $exception")
            }
    }


    fun goToChat(view: View?) {

    }

    fun goToProfile(view: View?) {
        val intent = Intent(this, Profile::class.java)
        intent.putExtra("user", userA)
        startActivity(intent)
    }

    fun goToHome(view: View?) {
        val intent = Intent(this, HomeActivity::class.java)
        intent.putExtra("user", userA)
        startActivity(intent)
    }
}


