package com.kontenery.service

import com.kontenery.data.invoice.Invoice
import com.kontenery.model.Path

class DocumentService {

    fun renderMail(invoice: Invoice): String {
        val props = mapInvoiceToVariablesMapForMailTemplate(invoice)
        return renderTemplateToHtml(TemplateEngine.engine, props, Path.PERIODIC_MAIL.path)
    }

    fun renderPdf(invoice: Invoice): ByteArray {
        val props = mapInvoiceToVariablesMapForInvoiceTemplate(invoice)
        val html = renderTemplateToHtml(TemplateEngine.engine, props, Path.PERIODIC_INVOICE_PDF.path)
        return htmlToPdfByteArray(html)
    }
}