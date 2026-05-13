package com.kontenery.service

import com.kontenery.library.model.invoice.Invoice
import com.kontenery.model.ConfigApp
import com.kontenery.model.EnvEnum

class MailService(
    private val gmail: GmailRestService,
    private val sendRequest: SendRequest,
    private val documentService: DocumentService,
    private val config: ConfigApp,
) {
    suspend fun sendInvoice(invoice: Invoice) {

        val recipient = resolveRecipient(invoice)

        val mailContent = documentService.renderMail(invoice)
        val pdf = documentService.renderPdf(invoice)

        val message = createEmailWithAttachment(
            from = config.emailUser,
            to = recipient,
            subject = "${invoice.documentTitle()} - magazynki przy Ostrowskiego 102",
            htmlContent = mailContent,
            pdfAttachment = pdf
        )

        retryWithBackoff {
            gmail.send(message)
        }

        sendRequest.confirmInvoiceSend(invoice.invoiceNumber!!)

    }

    suspend fun reportError(invoice: Invoice, e: Exception) {
        sendRequest.mailSendError(invoice.toString(), e.message)
    }

    private fun resolveRecipient(invoice: Invoice): String {
        return if (config.env == EnvEnum.DEV)
            "wilczynski87@gmail.com"
        else
            invoice.customer?.email?.trim()?.takeIf { it.isNotBlank() }
                ?: error("Invalid email")
    }
}

fun Invoice.documentTitle(): String =
    if (vatApply) "Faktura" else "Rachunek"