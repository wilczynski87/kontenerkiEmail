package service

import com.kontenery.service.createEmail
import jakarta.mail.Authenticator
import jakarta.mail.PasswordAuthentication
import jakarta.mail.Session
import jakarta.mail.internet.MimeMultipart
import kotlin.test.Test
import kotlin.test.assertEquals
import java.util.*

class MailServiceKtTest {
//    val emailUser = System.getenv("EMAIL_USER") ?: throw NullPointerException("There is no username for email")
//    val emailPassword = System.getenv("EMAIL_PASSWORD") ?: throw NullPointerException("There is no password for email")
    val emailUser = "parkingostrowskiego@gmail.com"
    val emailPassword = "ehpejfervmuwjwrg"

    val props = Properties().apply {
        put("mail.smtp.auth", "true")
        put("mail.smtp.starttls.enable", "true")
        put("mail.smtp.host", "smtp.gmail.com")
        put("mail.smtp.port", "587")
    }

    private val testSession = Session.getInstance(props, object : Authenticator() {
        override fun getPasswordAuthentication(): PasswordAuthentication {
            return PasswordAuthentication(emailUser, emailPassword)
        }
    })

    @Test
    fun `should create email with subject and content`() {
        val msg = createEmail(
            session = testSession,
            from = "sender@example.com",
            to = "recipient@example.com",
            subject = "Test Subject",
            htmlContent = "<h1>Hello</h1>"
        )

        assertEquals("Test Subject", msg.subject)
        assertEquals("sender@example.com", msg.from[0].toString())
//        assertEquals("recipient@example.com", msg.getRecipients(javax.mail.Message.RecipientType.TO)[0].toString())
    }

    @Test
    fun `should create email with PDF attachment`() {
        val pdfBytes = ByteArray(10) { it.toByte() }

        val msg = createEmail(
            session = testSession,
            from = "sender@example.com",
            to = "recipient@example.com",
            subject = "With Attachment",
            htmlContent = "<p>PDF attached</p>",
            pdfAttachment = pdfBytes
        )

        val content = msg.content
        require(content is MimeMultipart)

        // Expecting 2 parts: HTML + PDF
        assertEquals(2, content.count)
    }
}