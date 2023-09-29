package com.example.mfbisnes

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mfbisnes.databinding.ActivityChatRoomBinding
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ChatRoomActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatRoomBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var currentUserId: String
    private lateinit var receiverId: String
    private lateinit var chatRoomId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChatRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }


        db = FirebaseFirestore.getInstance()
        currentUserId = FirebaseAuth.getInstance().currentUser!!.uid
        receiverId = intent.getStringExtra("userId")!!
        chatRoomId = getChatRoomId(currentUserId, receiverId)

        val chatRoom = ChatRoom(currentUserId, receiverId, mutableListOf())

        val messagesRecyclerView = findViewById<RecyclerView>(R.id.messagesRecyclerView)
        val adapter = MessageAdapter(mutableListOf())
        messagesRecyclerView.adapter = adapter
        messagesRecyclerView.layoutManager = LinearLayoutManager(this)

        // Check if messages document exists
        db.collection("messages").document(chatRoomId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val messagesCollection = db.collection("messages").document(chatRoomId).collection("messages")
                    messagesCollection.orderBy("timestamp", Query.Direction.ASCENDING)
                        .addSnapshotListener { value, e ->
                            if (e != null) {
                                Log.w("HelloTest", "Listen failed.", e)
                                return@addSnapshotListener
                            }

                            val messages = mutableListOf<Message>()
                            for (document in value!!.documents) {
                                val message = document.toObject(Message::class.java)
                                message?.let { messages.add(it) }
                            }



                            adapter.messages = messages
                            messagesRecyclerView.scrollToPosition(adapter.itemCount - 1)
                            adapter.notifyDataSetChanged()

                            Log.d("HelloTest", "New messages received: ${messages.size}")
                        }


                } else {
                    // Document does not exist, create a new one
                    val chatRoom = ChatRoom(currentUserId, receiverId, mutableListOf())
                    db.collection("messages").document(chatRoomId).set(chatRoom)
                        .addOnSuccessListener {
                            Log.d("HelloTest", "Document created")
                        }
                        .addOnFailureListener { e ->
                            Log.w("HelloTest", "Error creating document", e)
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.w("HelloTest", "Error retrieving document", e)
            }




        binding.sendButton.setOnClickListener {
            val messageText = binding.messageEditText.text.toString()
            if (messageText.isNotEmpty()) {
                val message = Message(
                    currentUserId,
                    receiverId,
                    messageText,
                    Timestamp.now()
                )

                db.collection("messages")
                    .document(chatRoomId)
                    .collection("messages")
                    .add(message)
                    .addOnSuccessListener { documentReference ->
                        Log.d("HelloTest", "Message sent with ID: ${documentReference.id}")
                    }
                    .addOnFailureListener { e ->
                        Log.w("HelloTest", "Error adding message", e)
                    }

                binding.messageEditText.setText("")
            }
        }

    }


    private fun getChatRoomId(userId1: String, userId2: String): String {
        return if (userId1 < userId2) {
            "$userId1-$userId2"
        } else {
            "$userId2-$userId1"
        }
    }
}
