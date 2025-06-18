package com.kontenery

import com.kontenery.library.model.invoice.Invoice
import com.kontenery.library.utils.Path
import com.kontenery.service.*
import io.ktor.server.application.*
import jakarta.mail.Authenticator
import jakarta.mail.PasswordAuthentication
import jakarta.mail.Session
import jakarta.mail.Transport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import java.util.*

fun Application.sendingMails(mailQueue: Channel<Invoice>) {

    val emailUser = System.getenv("EMAIL_USER") ?: throw NullPointerException("There is no username for email")
    val emailPassword = System.getenv("EMAIL_PASSWORD") ?: throw NullPointerException("There is no password for email")

    val props = Properties().apply {
        put("mail.smtp.auth", "true")
        put("mail.smtp.starttls.enable", "true")
        put("mail.smtp.host", "smtp.gmail.com")
        put("mail.smtp.port", "587")
    }

    launch(Dispatchers.IO) {

        val session: Session = Session.getInstance(props, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(emailUser, emailPassword)
            }
        })

        for (invoice in mailQueue) {
            try {
                println("sending invoice:")
                println(invoice)
                val mailTemplateProps: Map<String, Any> = mapInvoiceToVariablesMapForMailTemplate(invoice)
                val invoiceTemplateProps: Map<String, Any> = mapInvoiceToVariablesMapForInvoiceTemplate(invoice)
//                println("blad 1")
                val mailClient: String = invoice.customer?.email ?: throw NullPointerException("No email customer, for: ${invoice.customer}")

                val mailContent:String = renderTemplateToHtml(templateEngine = TemplateEngine.engine, variables = mailTemplateProps, template = Path.PERIODIC_MAIL.path)
//                println("blad 2")

                val pdfContent:String = renderTemplateToHtml(TemplateEngine.engine, invoiceTemplateProps, Path.PERIODIC_INVOICE_PDF.path)
                val pdf:ByteArray = htmlToPdfByteArray(pdfContent)
//                println("blad 3")
                val document:String = if(invoice.vatApply) "Faktura" else "Rachunek"

                val email = createEmail(
                    session = session,
                    from = emailUser,
                    to = mailClient,
                    subject = "$document - magazynki przy Ostrowskiego 102",
                    htmlContent = mailContent,
                    pdfAttachment = pdf,
                )

                Transport.send(email)

                log.info("mail wysłany do: $mailClient")
                confirmInvoiceSend(invoice.invoiceNumber ?: throw NullPointerException("No email customer, for: ${invoice}"))
            } catch (e: Exception) {
                println("sendingMails EXCEPTION: $e")
                mailSendError(invoice.toString(), e.message)
            }
        }
    }
}