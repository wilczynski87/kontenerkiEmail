package com.kontenery

import com.kontenery.library.model.invoice.Invoice
import com.kontenery.library.utils.Path
import com.kontenery.library.utils.Env
import com.kontenery.model.ConfigApp
import com.kontenery.model.EnvEnum
import com.kontenery.service.*
import io.ktor.server.application.*
import jakarta.mail.Authenticator
import jakarta.mail.PasswordAuthentication
import jakarta.mail.Session
import jakarta.mail.Transport
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import java.util.*

//fun Application.sendingMails(mailQueue: Channel<Invoice>) {
//
//    val emailUser = System.getenv("EMAIL_USER") ?: throw NullPointerException("There is no username for email")
//    val emailPassword = System.getenv("EMAIL_PASSWORD") ?: throw NullPointerException("There is no password for email")
//
//    val props = Properties().apply {
//        put("mail.smtp.auth", "true")
//        put("mail.smtp.starttls.enable", "true")
//        put("mail.smtp.host", "smtp.gmail.com")
//        put("mail.smtp.port", "587")
//    }
//
//    launch(Dispatchers.IO) {
//
//        val session: Session = Session.getInstance(props, object : Authenticator() {
//            override fun getPasswordAuthentication(): PasswordAuthentication {
//                return PasswordAuthentication(emailUser, emailPassword)
//            }
//        })
//
//        for (invoice: Invoice in mailQueue) {
//            try {
//                println("\nsending invoice: $invoice")
//                val mailTemplateProps: Map<String, Any> = mapInvoiceToVariablesMapForMailTemplate(invoice)
//                val invoiceTemplateProps: Map<String, Any> = mapInvoiceToVariablesMapForInvoiceTemplate(invoice)
////                println("blad 1")
//
//                val mailClient: String = if(env.name == "DEV") "wilczynski87@gmail.com"
//                    else invoice.customer?.email ?: throw NullPointerException("No email customer, for: ${invoice.customer}")
//
//                val mailContent:String = renderTemplateToHtml(templateEngine = TemplateEngine.engine, variables = mailTemplateProps, template = Path.PERIODIC_MAIL.path)
////                println("blad 2")
//
//                val pdfContent:String = renderTemplateToHtml(TemplateEngine.engine, invoiceTemplateProps, Path.PERIODIC_INVOICE_PDF.path)
//                val pdf:ByteArray = htmlToPdfByteArray(pdfContent)
////                println("blad 3")
//                val document:String = if(invoice.vatApply) "Faktura" else "Rachunek"
//
//                val email = createEmail(
//                    session = session,
//                    from = emailUser,
//                    to = mailClient,
//                    subject = "$document - magazynki przy Ostrowskiego 102",
//                    htmlContent = mailContent,
//                    pdfAttachment = pdf,
//                )
//
//                Transport.send(email)
//
//                log.info("mail wysłany do: $mailClient")
//                println("invoice nr: ${invoice.invoiceNumber}, to client: ${invoice.customer?.name}, to email: $mailClient")
//
//                confirmInvoiceSend(invoice.invoiceNumber ?: throw NullPointerException("No email customer, for: $invoice"))
//            } catch (e: Exception) {
//                println("sendingMails EXCEPTION: $e")
//                try {
//                    mailSendError(invoice.toString(), e.message)
//                } catch (sendError: Exception) {
//                    println("Could not notify API about error: $sendError")
//                }
//            }
//        }
//    }
//}


//fun Application.sendingMails2(mailQueue: Channel<Invoice>) {
//
//    val emailUser = System.getenv("EMAIL_USER") ?: throw NullPointerException("There is no username for email")
//
//    val props = Properties().apply {
//        put("mail.smtp.auth.mechanisms", "XOAUTH2");
//        put("mail.smtp.auth", "true")
//        put("mail.smtp.starttls.enable", "true")
//        put("mail.smtp.host", "smtp.gmail.com")
//        put("mail.smtp.port", "587")
//    }
//
//    launch(Dispatchers.IO) {
//        // pobieram Token z google
//        val credential: Credential = GmailOAuth2Login.authorize()
//
//        val session = Session.getInstance(props, object : Authenticator() {
//            override fun getPasswordAuthentication(): PasswordAuthentication {
//                return PasswordAuthentication(emailUser, credential.accessToken)
//            }
//        })
//
//        for (invoice in mailQueue) {
//            try {
//                println("sending invoice: $invoice")
//                val mailTemplateProps: Map<String, Any> = mapInvoiceToVariablesMapForMailTemplate(invoice)
//                val invoiceTemplateProps: Map<String, Any> = mapInvoiceToVariablesMapForInvoiceTemplate(invoice)
////                println("blad 1")
//                val mailClient: String = if(env.name == "DEV") "wilczynski87@gmail.com"
//                    else invoice.customer?.email ?: throw NullPointerException("No email customer, for: ${invoice.customer}")
//
//                val mailContent:String = renderTemplateToHtml(templateEngine = TemplateEngine.engine, variables = mailTemplateProps, template = Path.PERIODIC_MAIL.path)
////                println("blad 2")
//
//                val pdfContent:String = renderTemplateToHtml(TemplateEngine.engine, invoiceTemplateProps, Path.PERIODIC_INVOICE_PDF.path)
//                val pdf:ByteArray = htmlToPdfByteArray(pdfContent)
////                println("blad 3")
//                val document:String = if(invoice.vatApply) "Faktura" else "Rachunek"
//
//                val email = createEmail(
//                    session = session,
//                    from = emailUser,
//                    to = mailClient,
//                    subject = "$document - magazynki przy Ostrowskiego 102",
//                    htmlContent = mailContent,
//                    pdfAttachment = pdf,
//                )
//
//                Transport.send(email)
//
//                log.info("mail wysłany do: $mailClient")
//                confirmInvoiceSend(invoice.invoiceNumber ?: throw NullPointerException("No email customer, for: ${invoice}"))
//            } catch (e: Exception) {
//                println("sendingMails EXCEPTION: $e")
//                mailSendError(invoice.toString(), e.message)
//            }
//        }
//    }
//}
//
//fun Application.sendingMails(mailQueue: Channel<Invoice>, sendRequest: SendRequest, configApp: ConfigApp) {
//
//    val emailUser = configApp.emailUser
//
//    val gmail = gmailService()
////    val gmail by lazy { gmailService() }
//
//    launch(Dispatchers.IO) {
//
//        for (invoice in mailQueue) {
//            try {
//                println("\nsending invoice: $invoice")
//
//                val mailTemplateProps = mapInvoiceToVariablesMapForMailTemplate(invoice)
//                val invoiceTemplateProps = mapInvoiceToVariablesMapForInvoiceTemplate(invoice)
//
//                val mailClient = if (configApp.env == EnvEnum.DEV)
//                    "wilczynski87@gmail.com"
//                else
//                    invoice.customer?.email?.trim()?.takeIf { it.isNotBlank() }
//                        ?: throw IllegalArgumentException("Invalid customer email for invoice ${invoice.invoiceNumber}")
//
//                val mailContent = renderTemplateToHtml(
//                    TemplateEngine.engine,
//                    mailTemplateProps,
//                    Path.PERIODIC_MAIL.path
//                )
//
//                val pdfContent = renderTemplateToHtml(
//                    TemplateEngine.engine,
//                    invoiceTemplateProps,
//                    Path.PERIODIC_INVOICE_PDF.path
//                )
//
//                val pdf = htmlToPdfByteArray(pdfContent)
//
//                val document = if (invoice.vatApply) "Faktura" else "Rachunek"
//
//                val message = createEmailWithAttachment(
//                    from = emailUser,
//                    to = mailClient,
//                    subject = "$document - magazynki przy Ostrowskiego 102",
//                    htmlContent = mailContent,
//                    pdfAttachment = pdf
//                )
//
////                gmail.users().messages().send("me", message).execute()
//                retryWithBackoff {
//                    val response = gmail.users().messages()
//                        .send("me", message)
//                        .execute()
//
//                    if (response.id.isNullOrBlank()) {
//                        throw RuntimeException("Gmail API returned empty message id")
//                    }
//
//                    response
//                }
//
//                log.info("mail wysłany do: $mailClient")
//
//                sendRequest.confirmInvoiceSend(invoice.invoiceNumber!!)
//            } catch (e: Exception) {
//                println("sendingMails EXCEPTION: $e")
//                try {
//                    sendRequest.mailSendError(invoice.toString(), e.message)
//                } catch (_: Exception) {}
//            }
//        }
//    }
//}

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
                } catch (ignored: Exception) {}
            }
        }
    }

    environment.monitor.subscribe(ApplicationStopped) {
        scope.cancel()
    }
}