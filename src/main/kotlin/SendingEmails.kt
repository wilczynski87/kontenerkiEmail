package com.kontenery

import com.kontenery.data.invoice.Invoice
import com.kontenery.service.MailService
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopped
import io.ktor.server.application.log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

fun Application.sendingMails(
    mailQueue: Channel<Invoice>,
    mailService: MailService
) {
    val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    scope.launch {
        for (invoice in mailQueue) {
            try {
                mailService.sendInvoice(invoice)
            } catch (e: Exception) {
                log.error("Mail failed for invoice=${invoice.invoiceNumber}", e)
                try {
                    mailService.reportError(invoice, e)
                } catch (_: Exception) {}
            }
        }
    }

    environment.monitor.subscribe(ApplicationStopped) {
        scope.cancel()
    }
}
