package com.kontenery

import com.kontenery.library.model.invoice.Invoice
import io.ktor.server.application.*
import kotlinx.coroutines.channels.Channel

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {

    val mailQueue: Channel<Invoice> = Channel(capacity = Channel.UNLIMITED)

    val apiName = System.getenv("API_NAME") ?: throw NullPointerException("There is no api address")
    val apiPort = System.getenv("API_PORT") ?: throw NullPointerException("There is no api port")

    configureSerialization()
    configureRouting(mailQueue)
    sendingMails(mailQueue)

}
