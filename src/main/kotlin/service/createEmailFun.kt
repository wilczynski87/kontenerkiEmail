package com.kontenery.service

import jakarta.activation.DataHandler
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeBodyPart
import jakarta.mail.internet.MimeMessage
import jakarta.mail.internet.MimeMultipart
import jakarta.mail.util.ByteArrayDataSource
import java.io.ByteArrayOutputStream
import java.util.Base64
import java.util.Properties
import com.google.api.services.gmail.model.Message
import jakarta.mail.Message.RecipientType
import jakarta.mail.Session
import kotlinx.coroutines.delay
import kotlin.math.min
import kotlin.random.Random
import java.io.IOException
import com.google.api.client.googleapis.json.GoogleJsonResponseException

fun createEmail(
    session: Session,
    from: String,
    to: String,
    subject: String,
    htmlContent: String,
    pdfAttachment: ByteArray? = null
) : MimeMessage {

    return MimeMessage(session).apply {
        try {
            setFrom(InternetAddress(from))
            setRecipients(RecipientType.TO, InternetAddress.parse(to))
            setSubject(subject)
//            println("from: $from, to: $to")

            val multipart = MimeMultipart()

            val htmlPart = MimeBodyPart()
            htmlPart.setContent(htmlContent, "text/html; charset=utf-8")
            multipart.addBodyPart(htmlPart)

            pdfAttachment?.let {
                val attachmentPart = MimeBodyPart().apply {
                    val ds = ByteArrayDataSource(it, "application/pdf")
                    dataHandler = DataHandler(ds)
                    fileName = "Faktura"
                }
                multipart.addBodyPart(attachmentPart)
            }

            setContent(multipart)
        } catch (e:Exception) {
            println(e)
        }

    }
}

fun createEmailWithAttachment(
    from: String,
    to: String,
    subject: String,
    htmlContent: String,
    pdfAttachment: ByteArray
): Message {

    val session = Session.getInstance(Properties())

    val mimeMessage = MimeMessage(session).apply {
        setFrom(InternetAddress(from))
        addRecipient(RecipientType.TO, InternetAddress(to))
        setSubject(subject, "UTF-8")

        val multipart = MimeMultipart("mixed")

        // treść maila
        val body = MimeBodyPart().apply {
            setContent(htmlContent, "text/html; charset=UTF-8")
        }

        // załączniki
        val attachment = MimeBodyPart().apply {
            dataHandler = DataHandler(
                ByteArrayDataSource(pdfAttachment, "application/pdf")
            )
            fileName = "faktura.pdf"
        }

        val wrapper = MimeMultipart("alternative").apply {
            addBodyPart(body)
        }

        val wrapperPart = MimeBodyPart().apply {
            setContent(wrapper)
        }

        multipart.addBodyPart(wrapperPart)
        multipart.addBodyPart(attachment)

        setContent(multipart)
    }

    val buffer = ByteArrayOutputStream()
    mimeMessage.writeTo(buffer)

    val rawMessage = Base64.getUrlEncoder()
        .encodeToString(buffer.toByteArray())

    return Message().setRaw(rawMessage)
}


suspend fun <T> retryWithBackoff(
    maxRetries: Int = 5,
    initialDelayMs: Long = 500,
    maxDelayMs: Long = 10_000,
    multiplier: Double = 2.0,
    jitterMs: Long = 250,
    block: suspend () -> T
): T {

    var currentDelay = initialDelayMs
    var lastError: Exception? = null

    repeat(maxRetries) { attempt ->

        try {
            return block()
        } catch (e: Exception) {

            if (!e.isRetryable()) throw e

            lastError = e

            val jitter = Random.nextLong(0, jitterMs)
            val sleepTime = min(currentDelay + jitter, maxDelayMs)

            println("🔁 Retry ${attempt + 1}/$maxRetries in ${sleepTime}ms due to: ${e.message}")

            delay(sleepTime)

            currentDelay = (currentDelay * multiplier).toLong()
        }
    }

    throw lastError ?: RuntimeException("Unknown retry failure")
}

fun Throwable.isRetryable(): Boolean {
    return when (this) {

        is GoogleJsonResponseException -> {
            val code = statusCode
            code == 429 || code in 500..599
        }
        is IOException -> true

        else -> false
    }
}
