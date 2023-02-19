package com.example.plugins

import com.example.messages.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import java.time.Duration
import io.ktor.server.application.*
import io.ktor.server.routing.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json


fun Application.configureSockets() {
    val channel = Channel<Message>(200)

    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }
    routing {
        webSocket("/ws") {
            val domainSession = call.parameters["sessionId"]
            if (domainSession is String) {
                val sessionData = SessionStorage.putNewSession(this)
                try {
                    val stringSessionData = Json.encodeToString(MessageSenderData.serializer(), sessionData)
                    outgoing.send(Frame.Text(stringSessionData))
                    for (frame in incoming) {
                        if (frame is Frame.Text) {
                            val text = frame.readText()
                            println("text sent by: $stringSessionData - [$text]")
                            channel.send(Message(sessionData, text))
                            if (text.equals("bye", ignoreCase = true)) {
                                close(CloseReason(CloseReason.Codes.NORMAL, "Client said BYE"))
                            }
                        }
                    }
                } catch (e: Exception) {
                    println(e)
                } finally {
                    SessionStorage.removeSession(sessionData)
                }
            } else {
                println("sessionId not present")
                close(CloseReason(CloseReason.Codes.NORMAL, "Session id not present"))
            }
        }
    }

    val messagePassing: MessagePassing = RabbitMQMessagePassing(channel, SessionStorage::broadcast)

    launch {
        messagePassing.initializeProducer()
    }
    launch {
        messagePassing.consume()
    }
}
