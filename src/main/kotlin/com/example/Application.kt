package com.example

import io.ktor.server.netty.*
import com.example.plugins.*
import io.ktor.server.engine.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        configureMessageQueue()
        configureSerialization()
    }.start(wait = true)
}
