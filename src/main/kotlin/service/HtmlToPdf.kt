package com.kontenery.service

import com.kontenery.model.Path
import com.lowagie.text.pdf.BaseFont
import org.xhtmlrenderer.pdf.ITextRenderer
import java.io.ByteArrayOutputStream

fun htmlToPdfByteArray(html: String): ByteArray {
    val outputStream = ByteArrayOutputStream()
    val renderer = ITextRenderer()

    renderer.fontResolver.addFont("templates/thymeleaf/fonts/AlexBrush-Regular.ttf", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED)
    renderer.fontResolver.addFont("templates/thymeleaf/fonts/Voces-Regular.ttf", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED)

    renderer.setDocumentFromString(html)
    renderer.layout()
    renderer.createPDF(outputStream)

    return outputStream.toByteArray()
}

fun generatePdfToPrint(htmls: List<String>): ByteArray {
    val byteArrayOutputStream: ByteArrayOutputStream = ByteArrayOutputStream()

    val renderer = ITextRenderer()
    renderer.fontResolver.addFont("templates/thymeleaf/fonts/AlexBrush-Regular.ttf", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED)
    renderer.fontResolver.addFont("templates/thymeleaf/fonts/Voces-Regular.ttf", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED)

    renderer.setDocumentFromString(htmls[0]);
    renderer.layout();
    renderer.createPDF(byteArrayOutputStream, false);

    for (html in htmls) {
        if (html == htmls[0]) continue // skip first
        renderer.setDocumentFromString(html)
        renderer.layout()
        renderer.writeNextDocument()
    }

    renderer.finishPDF()

    val byteArray: ByteArray = byteArrayOutputStream.toByteArray()
    byteArrayOutputStream.close()

    return byteArray
}