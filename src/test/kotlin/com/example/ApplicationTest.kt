package com.example

import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlin.test.*
import io.ktor.server.testing.*
import com.example.plugins.*
import kotlinx.coroutines.delay
import java.lang.Thread.sleep

class ApplicationTest {
    @Test
    fun testRoot() = testApplication {
        application {
            configureMessageQueue()
            sleep(5000)
        }
        MessageProducer().run()
    }
}
