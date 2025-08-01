package com.kontenery.controller

import com.kontenery.library.model.invoice.Invoice
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.channels.Channel
import java.time.LocalDate

fun Route.sendInvoice(mailQueue: Channel<Invoice>) {

    route("/sendMailWithAttachment") {
        post("/withVat") {
            println("sendMailWithAttachment WITH VAT")
            try {
                // recive data to send invoice
                val invoice:Invoice = call.receive<Invoice>()
                println("invoice: $invoice")
                // send invoice to queue
                mailQueue.send(invoice)
//                println("invoice2: $invoice")

                // respond about sending
                call.respond(LocalDate.now().toString())
            } catch (e:Exception) {
                println("EXCEPTION in sendMailWithAttachment WITH VAT:")
                println(e)
                call.respond(HttpStatusCode.BadRequest)
            }
        }
        post("/noVat") {
            println("sendMailWithAttachment NO VAT")
            try {
                // recive data to send invoice
                val invoice:Invoice = call.receive<Invoice>()

                // send invoice to queue
                mailQueue.send(invoice)

                // respond about sending
                call.respond(LocalDate.now().toString())
            } catch (e:Exception) {
                println("EXCEPTION in sendMailWithAttachment NO VAT:")
                println(e)
                call.respond(HttpStatusCode.BadRequest)
            }
        }
    }
}