package com.example.mfbisnes

data class ChatRoom(
    val user1Id: String,
    val user2Id: String,
    val messages: MutableList<Message>
)

