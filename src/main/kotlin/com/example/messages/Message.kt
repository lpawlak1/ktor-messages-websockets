package com.example.messages

import kotlinx.serialization.Serializable

@Serializable
data class Message(val senderData: MessageSenderData, val message: String)