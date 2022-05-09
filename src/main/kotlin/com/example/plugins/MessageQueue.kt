package com.example.plugins

import com.example.dto.Test
import io.ktor.server.application.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory
import org.apache.activemq.artemis.jms.client.ActiveMQObjectMessage
import javax.jms.Session
import javax.jms.TextMessage

fun Application.configureMessageQueue() = launch {
    ActiveMQConnectionFactory("tcp://localhost:61616").createConnection().use { connection ->
        connection.start()
        val session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)
        val destination = session.createQueue("ktor-4164-test")
        val messageConsumer = session.createConsumer(destination)
        while (isActive) {
            when (val message = messageConsumer.receive(5000L)) {
                is TextMessage -> {
                    try {
                        val test = Json.decodeFromString<Test>(message.text)
                        log.info("Received Test object as JSON message: $test")
                    } catch (e: SerializationException) {
                        log.error("Unable to JSON-decode the TextMessage to a Test object: ${message.text}", e)
                        continue
                    }
                }
                is ActiveMQObjectMessage -> {
                    // NOTE: with Ktor-2.0.0 and development-mode (-Dio.ktor.development=true) object-messages
                    //       cannot be cast to Test-objects anymore
                    //       (https://youtrack.jetbrains.com/issue/KTOR-4164). Use JSON-TextMessages instead!
                    log.debug("Received object message: ${message.getObject()}")
                    val test = message.getObject() as Test // ?: continue
                    log.info("Received serialized Test object: $test")
                }
                else -> continue
            }
        }
    }
}
