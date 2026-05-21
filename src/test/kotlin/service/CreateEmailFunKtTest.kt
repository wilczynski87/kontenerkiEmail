package service

import com.kontenery.service.createEmail
import jakarta.mail.Session
import jakarta.mail.internet.MimeMultipart
import java.util.Properties
import kotlin.test.Test
import kotlin.test.assertEquals

class CreateEmailFunKtTest {
    private val testSession = Session.getInstance(Properties())

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
        assertEquals(2, content.count)
    }
}
