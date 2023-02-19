package com.example.messages

import com.example.loggin.LoggerDelegate
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach

class SimpleMessagePassing(
    messageChannel: Channel<Message>,
    callback: suspend (Message) -> Unit
) : MessagePassing(messageChannel, callback) {
    private val logger by LoggerDelegate()

    override suspend fun consume() {
        messageChannel.consumeEach { message ->
            callback(message)
        }
    }

    override suspend fun initializeProducer() {
        logger.info("Producer initialized")
    }
}