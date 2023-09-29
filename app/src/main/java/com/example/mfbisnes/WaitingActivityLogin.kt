package com.example.mfbisnes

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class WaitingActivityLogin : AppCompatActivity() {
    private lateinit var userA: User
    val db = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser!!.uid
    val docRef = db.collection("users").document(userId)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_waiting)
        getValuesFrom()




    }

    private fun getValuesFrom(){
        docRef.addSnapshotListener { documentSnapshot, exception ->
            if (exception != null) {
                Log.e("HomeActivity", "Error listening to Firestore document: $exception")
                return@addSnapshotListener
            }
            if (documentSnapshot != null && documentSnapshot.data != null) {
                val gender = documentSnapshot.data!!["gender"] as? String ?: null
                val age = documentSnapshot.data!!["age"] as? String ?: null
                val location = documentSnapshot.data!!["location"] as? String ?: null
                val profilePicture = documentSnapshot.data!!["profile-picture"] as? String ?: null
                val lat = documentSnapshot.data!!["lat"] as? Double ?: null
                val lng = documentSnapshot.data!!["lng"] as? Double ?: null
                val accountCreated =  documentSnapshot.data!!["account-created"] as Boolean
                val activities = documentSnapshot.data!!["activities"] as? List<String> ?: emptyList()

                userA = User(userId, gender, age, location, profilePicture, lat, lng, accountCreated, activities)



                val intent = Intent(this, HomeActivity::class.java)
                intent.putExtra("user",userA)
                startActivity(intent)
                finish()

            }
        }

    }

}