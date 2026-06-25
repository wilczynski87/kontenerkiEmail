package com.kontenery.service

import com.kontenery.data.invoice.Invoice
import com.kontenery.library.utils.Path
import com.kontenery.library.utils.now
import com.kontenery.model.ConfigApp
import com.kontenery.model.EnvEnum
import kotlinx.datetime.LocalDate

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
        val invoiceNumber = invoice.invoiceNumber
            ?: run {
                println("Cannot report mail error: invoice has no number")
                return
            }
        sendRequest.mailSendError(invoiceNumber, e.message)
    }

    suspend fun sendPrintInvoices(invoices: List<Invoice>) {
        require(invoices.isNotEmpty()) { "Invoice list is empty" }

        val invoiceHtmls = invoices
            .map { mapInvoiceToVariablesMapForInvoiceTemplate(it) }
            .map { renderTemplateToHtml(TemplateEngine.engine, it, Path.PERIODIC_INVOICE_PDF.path) }
        val pdf = generatePdfToPrint(invoiceHtmls)

        val invoiceDate = invoices.first().invoiceDate ?: LocalDate.now()
        val mailContent = renderTemplateToHtml(
            TemplateEngine.engine,
            mapVariablesForPrintInvoices(invoiceDate),
            Path.PRINT_MAIL.path,
        )

        val message = createEmailWithAttachment(
            from = config.emailUser,
            to = config.printRecipient,
            subject = "Fakturki do druku",
            htmlContent = mailContent,
            pdfAttachment = pdf,
        )

        retryWithBackoff { gmail.send(message) }
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