package com.kontenery.service

import com.kontenery.library.model.invoice.Invoice
import com.kontenery.library.utils.Month
import com.kontenery.library.utils.now
import kotlinx.datetime.LocalDate
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import org.thymeleaf.templatemode.TemplateMode
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver

object TemplateEngine {
    private val resolver = ClassLoaderTemplateResolver().apply {
        prefix = "templates/thymeleaf/"
        suffix = ".html"
        characterEncoding = Charsets.UTF_8.name() // "UTF-8"
        templateMode = TemplateMode.HTML // "HTML"
    }

    val engine: TemplateEngine = TemplateEngine().apply {
        setTemplateResolver(resolver)
    }
}

fun mapInvoiceToVariablesMapForMailTemplate(invoice: Invoice): Map<String, Any> {
    // TODO: docuemnt do poprawy dla rachubku
    val docuemnt: String = if(invoice.vatAmountSum == null) "rachunek" else "fakturę"

    return mapOf(
        Pair("document", docuemnt),
        Pair("salutation", invoice.customer?.salutation ?: "Szanowny Kliencie"),
        Pair("month", Month.getMonth(invoice.invoiceDate ?: LocalDate.now())?.polishName ?: "bierzący miesiąc")
    )
}
fun mapVariablesForPrintInvoices(data: LocalDate): Map<String, Any> {
    return mapOf(
        Pair("month", Month.getMonth(data)?.polishName ?: "bierzący miesiąc")
    )
}
fun mapInvoiceToVariablesMapForInvoiceTemplate(invoice: Invoice): Map<String, Any> {
    val isInvoice: Boolean = invoice.vatApply
    return mapOf(Pair("invoice", invoice), Pair("isInvoice", isInvoice))
}

fun renderTemplateToHtml(templateEngine: TemplateEngine, variables: Map<String, Any>, template: String): String {
    val context = Context().apply {
        setVariables(variables)
    }

    return templateEngine.process(template, context)
}
