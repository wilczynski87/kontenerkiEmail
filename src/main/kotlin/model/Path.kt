package com.kontenery.model

enum class Path(val path: String) {
    PERIODIC_INVOICE_PDF("invoices/fakturaKontenery"),
    UTILITIES_INVOICE_PDF("invoices/fakturaMedia"),
    PERIODIC_MAIL("mails/invoicePeriodic"),
    UTILITIES_MAIL("mails/invoiceUtilities"),
    FONT("templates/thymeleaf/fonts")
}