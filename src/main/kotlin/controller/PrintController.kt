package com.kontenery.controller

import com.kontenery.library.model.invoice.Invoice
import com.kontenery.library.utils.Path
import com.kontenery.library.utils.now
import com.kontenery.service.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import jakarta.mail.Authenticator
import jakarta.mail.PasswordAuthentication
import jakarta.mail.Session
import jakarta.mail.Transport
import kotlinx.datetime.LocalDate
import java.util.*

fun Route.printInvoice() {
    route("printInvoices") {
        post() {
            val invoices: List<Invoice> = call.receive<List<Invoice>>()
            println("invoices recived: ${invoices.map { it.invoiceNumber }}")

            val invoicesString: List<String> = invoices
                .map { mapInvoiceToVariablesMapForInvoiceTemplate(it) }
                .map { renderTemplateToHtml(TemplateEngine.engine, it, Path.PERIODIC_INVOICE_PDF.path) }
            val invoicesPdfToSend = generatePdfToPrint(invoicesString)

            val mailTemplateProps: Map<String, Any> = mapVariablesForPrintInvoices(invoices[0].invoiceDate ?: LocalDate.now())
            val mailContent:String = renderTemplateToHtml(templateEngine = TemplateEngine.engine, variables = mailTemplateProps, template = Path.PRINT_MAIL.path)

            val emailUser = System.getenv("EMAIL_USER") ?: throw NullPointerException("There is no username for email")
            val emailPassword = System.getenv("EMAIL_PASSWORD") ?: throw NullPointerException("There is no password for email")
            val props = Properties().apply {
                put("mail.smtp.auth", "true")
                put("mail.smtp.starttls.enable", "true")
                put("mail.smtp.host", "smtp.gmail.com")
                put("mail.smtp.port", "587")
            }
            val session: Session = Session.getInstance(props, object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(emailUser, emailPassword)
                }
            })

            val email = createEmail(
                session = session,
                from = emailUser,
                to = "wilczynski87@gmail.com",
                subject = "Fakturki do druku",
                htmlContent = mailContent,
                pdfAttachment = invoicesPdfToSend,
            )

            Transport.send(email)
        }
    }
}