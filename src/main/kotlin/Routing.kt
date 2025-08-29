package com.kontenery

import com.kontenery.controller.printInvoice
import com.kontenery.controller.sendInvoice
import com.kontenery.library.model.invoice.Invoice
import com.kontenery.service.GmailOAuth2Login
import com.kontenery.service.createEmail
import io.ktor.server.application.*
import io.ktor.server.plugins.requestvalidation.RequestValidation
import io.ktor.server.plugins.requestvalidation.ValidationResult
import io.ktor.server.response.*
import io.ktor.server.routing.*
import jakarta.mail.Authenticator
import jakarta.mail.PasswordAuthentication
import jakarta.mail.Session
import jakarta.mail.Transport
import kotlinx.coroutines.channels.Channel
import java.util.*

fun Application.configureRouting(mailQueue: Channel<Invoice>) {
    install(RequestValidation) {
        validate<String> { bodyText ->
            if (!bodyText.startsWith("Hello"))
                ValidationResult.Invalid("Body text should start with 'Hello'")
            else ValidationResult.Valid
        }
    }

    routing {
        get("healthcheck") {
            call.respond("ok")
        }
        get("testMail") {
            try {
//                println("testMail")
                val emailUser = "parkingostrowskiego@gmail.com"
                val emailPassword = "litcviutipqwkxsw"
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
                    subject = "Faktura - magazynki przy Ostrowskiego",
                    htmlContent = "",
                    pdfAttachment = null,
                )

                Transport.send(email)
                log.info("mail wysłany")

            } catch (e:Exception) {
                println("testMail Exception:" )
                println(e)
            }
        }

        get("loginTest") {
            try {
                val result = GmailOAuth2Login.authorize()
                println(result.toString())
            } catch (e:Exception) {
                println("testMail Exception:" )
                println(e)
            }
        }

        sendInvoice(mailQueue)

        printInvoice()
    }
}
