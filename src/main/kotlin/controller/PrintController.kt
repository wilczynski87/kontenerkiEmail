package com.kontenery.controller

import com.kontenery.data.invoice.Invoice
import com.kontenery.service.MailService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.printInvoice(mailService: MailService) {
    route("printInvoices") {
        post {
            val invoices: List<Invoice> = call.receive()
            println("invoices received: ${invoices.map { it.invoiceNumber }}")

            mailService.sendPrintInvoices(invoices)
            call.respond(HttpStatusCode.OK)
        }
    }
}
