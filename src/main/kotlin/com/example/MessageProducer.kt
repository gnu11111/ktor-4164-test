package com.example

import com.example.MessageProducer.Companion.log
import com.example.dto.Test
import kotlinx.coroutines.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.jms.*

suspend fun main() = MessageProducer().run()

class MessageProducer {

    private val sender = MessageQueueSender("tcp://localhost:61616/ktor-4164-test")

    suspend fun run() {
        sender.send(Test(1), true)
        sender.send(Test(2), false)
        sender.close()
    }

    companion object {
        val log: Logger = LoggerFactory.getLogger(MessageProducer::class.java)
    }
}

sealed interface MessageSender {
    suspend fun send(test: Test, json: Boolean)
    fun close()
}

class MessageQueueSender(private val endpoint: String) : MessageSender, ExceptionListener {

    private val connection: Connection
    private val session: Session
    private val producer: javax.jms.MessageProducer

    init {
        val factory = ActiveMQConnectionFactory(endpoint.substringBeforeLast("/"))
        connection = factory.createConnection()
        connection.start()
        connection.exceptionListener = this
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)
        val destination = session.createQueue(endpoint.substringAfterLast("/"))
        producer = session.createProducer(destination)
        producer.deliveryMode = DeliveryMode.NON_PERSISTENT
    }

    override suspend fun send(test: Test, json: Boolean): Unit = runBlocking {
        val message = if (json)
            session.createTextMessage(Json.encodeToString(test))
        else
            session.createObjectMessage(test)
        try {
            log.info("Sending message $test as ${if (json) "JSON-message" else "object-message"} " +
                    "to endpoint $endpoint ...")
            producer.send(message)
        } catch (e: Exception) {
            log.error("Error sending message, reason = ${e.message}")
        }
    }

    override fun close() {
        session.close()
        connection.close()
    }

    override fun onException(e: JMSException?) {
        log.error("Error talking to message queue '$endpoint': ${(e?.message ?: "unkown reason")}")
    }
}
