package com.kontenery

import com.kontenery.controller.healthRoutes
import com.kontenery.controller.printInvoice
import com.kontenery.controller.sendInvoice
import com.kontenery.data.invoice.Invoice
import com.kontenery.service.GmailHealthService
import com.kontenery.service.MailService
import io.ktor.server.application.Application
import io.ktor.server.routing.routing
import kotlinx.coroutines.channels.Channel

fun Application.configureRouting(
    mailQueue: Channel<Invoice>,
    mailService: MailService,
    gmailHealth: GmailHealthService,
) {
    routing {
        healthRoutes(gmailHealth)
        sendInvoice(mailQueue)
        printInvoice(mailService)
    }
}
