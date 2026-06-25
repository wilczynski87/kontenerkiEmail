package com.kontenery

import com.kontenery.controller.printInvoice
import com.kontenery.controller.sendInvoice
import com.kontenery.data.invoice.Invoice
import com.kontenery.service.MailService
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.channels.Channel

fun Application.configureRouting(mailQueue: Channel<Invoice>, mailService: MailService) {
    routing {
        get("healthcheck") {
            call.respond("ok")
        }

        sendInvoice(mailQueue)
        printInvoice(mailService)
    }
}
