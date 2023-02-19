package com.example.messages

import com.example.loggin.LoggerDelegate
import com.rabbitmq.client.AMQP
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.DefaultConsumer
import com.rabbitmq.client.Envelope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.lang.IllegalStateException
import java.nio.charset.StandardCharsets


class RabbitMQMessagePassing(
    messageChannel: Channel<Message>,
    callback: suspend (Message) -> Unit
) : MessagePassing(messageChannel, callback) {
    private val logger by LoggerDelegate()

    override suspend fun consume() {
        ConnectionFactory().newConnection("amqp://localhost:5672").use { connection ->
            connection.createChannel().use { channel ->
                val consumerTag = "consumer-1"
                logger.info("[$consumerTag] Waiting for messages...")
                while (true) {
                    channel.basicConsume(
                        QUEUE_NAME,
                        true,
                        MyConsumer(channel, callback)
                    )
                }
            }
        }
    }

    override suspend fun initializeProducer() {
        ConnectionFactory().newConnection("amqp://localhost:5672").use { connection ->
            connection.createChannel().use { channel ->
                channel.queueDeclare(QUEUE_NAME, true, false, false, null)
                logger.info("channel created")
                for (message in messageChannel) {
                    logger.info("Message is being sent on $QUEUE_NAME: $message")
                    channel.basicPublish(
                        "",
                        QUEUE_NAME,
                        null,
                        Json.encodeToString(Message.serializer(), message).toByteArray(StandardCharsets.UTF_8)
                    )
                }
            }
        }
        logger.error("end start publishing")
    }

    private val QUEUE_NAME = "test_queue"

    class MyConsumer(channel: com.rabbitmq.client.Channel, val callback: suspend (Message) -> Unit) :
        DefaultConsumer(channel) {

        private val logger by LoggerDelegate()

        override fun handleDelivery(
            consumerTag: String?,
            envelope: Envelope?,
            properties: AMQP.BasicProperties?,
            body: ByteArray?
        ) {
            if (body is ByteArray) {
                val messageStr = String(body, StandardCharsets.UTF_8)
                val message = Json.decodeFromString<Message>(messageStr)
                logger.info("[$consumerTag] Received message: '$message'")
                runBlocking {
                    callback(message)
                }
            } else {
                throw IllegalStateException("Body is null")
            }
        }
    }
}