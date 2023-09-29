package com.example.mfbisnes


import com.google.firebase.Timestamp

data class Message(
    val sender: String = "",
    val receiver: String = "",
    val text: String = "",
    val timestamp: Timestamp? = null,
    var id: String = ""
)


