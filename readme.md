# Ktor messaging with websockets

## Goal

It's simple at core. 2 clients connected to server via websockets. Sending messages that will be sent to everyone else.
Goal of this project is to seek oportunities given in kotlin lang, ktor and rabbitMQ.

## Implementation details

Of course Kotlin can be seen here. I've made 2 implementations for message passing.

One is done with simply [Channel](https://kotlinlang.org/docs/channels.html) as async queue.
Its done in [here](src/main/kotlin/com/example/messages/SimpleMessagePassing.kt)

Second one is done with [RabbitMQ](https://www.rabbitmq.com/) as middle man via queue in it.
Its done in [here](src/main/kotlin/com/example/messages/RabbitMQMessagePassing.kt)
It's fairly simple implementation as it's not fault tolerant. Just single instance is made in coroutine and as long as connection to rabbitMQ is available its' working.

Main concern as beign no so proficient in kotlin was using coroutines to handle threading for consumer/producer alongside Ktor application.

What tested here are also query parameters in ws connection. In ktor these are suprisingly similar to query parameters in http app so implementing them wasn't a problem.

## Future

Project probably won't be maintained and should be used in any serious projects (due to lack of fault tolerance/ simple mutable maps used).