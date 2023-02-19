package com.example.messages

import kotlinx.coroutines.channels.Channel

abstract class MessagePassing(
    protected val messageChannel: Channel<Message>,
    protected val callback: suspend (Message) -> Unit
) {
    abstract suspend fun consume()
    abstract suspend fun initializeProducer()
}