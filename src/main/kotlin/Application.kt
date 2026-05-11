package com.kontenery

import com.kontenery.library.model.invoice.Invoice
import io.ktor.server.application.*
import kotlinx.coroutines.channels.Channel

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    println("Środowisko: $env")

    val mailQueue: Channel<Invoice> = Channel(capacity = Channel.UNLIMITED)

    configureSerialization()
    configureRouting(mailQueue)
    sendingMails(mailQueue)

}
