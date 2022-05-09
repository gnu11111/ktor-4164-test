package com.example

import kotlin.test.*
import io.ktor.server.testing.*
import com.example.plugins.*
import java.lang.Thread.sleep

class ApplicationTest {
    @Test
    fun testRoot() = testApplication {
        application {
            configureMessageQueue()
            configureSerialization()
            sleep(5000)
        }
        MessageProducer().run()
    }
}
