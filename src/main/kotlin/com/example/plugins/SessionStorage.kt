package com.example.plugins

import com.example.loggin.Logging
import com.example.messages.Message
import com.example.messages.MessageSenderData
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.serialization.json.Json


object SessionStorage : Logging() {
    private var connections = defaultValue()

    suspend fun broadcast(message: Message) {
        val (senderData, _) = message
        connections.first.filterNot { it.key == senderData }.forEach { (_, session) ->
            session.outgoing.send(
                Frame.Text(
                    Json.encodeToString(Message.serializer(), message)
                )
            )
        }
    }

    fun removeSession(senderData: MessageSenderData) {
        connections.first.remove(senderData).also {
            if (it is WebSocketServerSession)
                logger.info("deleted session $senderData")
            else
                logger.error("session not found for $senderData")
        }
    }

    private fun defaultValue(): Pair<MutableMap<MessageSenderData, WebSocketServerSession>, AtomicInteger> =
        Pair(mutableMapOf(), AtomicInteger(0))


    fun putNewSession(session: WebSocketServerSession): MessageSenderData {
        val newSessionId = connections.second.addAndGet(1)
        val senderData = MessageSenderData(newSessionId)
        connections.first[senderData] = session
        logger.info("Added new session for $senderData")
        return senderData
    }
}