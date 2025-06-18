package com.kontenery.service

import jakarta.activation.DataHandler
import jakarta.mail.*
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeBodyPart
import jakarta.mail.internet.MimeMessage
import jakarta.mail.internet.MimeMultipart
import jakarta.mail.util.ByteArrayDataSource

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
            setRecipients(Message.RecipientType.TO, InternetAddress.parse(to))
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