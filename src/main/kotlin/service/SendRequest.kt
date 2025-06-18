package com.kontenery.service

import com.kontenery.model.MailSendParam
import io.ktor.client.*
import io.ktor.client.request.*
import java.time.LocalDate

val apiName = System.getenv("API_NAME") ?: throw NullPointerException("There is no api address")
val apiPort = System.getenv("API_PORT") ?: throw NullPointerException("There is no api port")

suspend fun confirmInvoiceSend(invoiceNumber:String) {
    val client = HttpClient()
    val url = "http://$apiName:$apiPort/mailSend/invoice"
    client.get(url) {
        url {
            parameters.append("invoiceNumber", invoiceNumber)
            parameters.append("sendDate", LocalDate.now().toString())
        }
    }
}

suspend fun mailSendError(invoiceNumber:String, message: String? = null) {
    val client = HttpClient()
    client.get("http://$apiName:$apiPort/mailSend") {
        url {
            parameters.append(MailSendParam.INVOICE_NUMBER.param, invoiceNumber)
            parameters.append(MailSendParam.SEND_DATE.param, LocalDate.now().toString())
            parameters.append(MailSendParam.ERROR.param, "ERROR")
            if (message != null) {
                parameters.append(MailSendParam.MESSAGE.param, message)
            }
        }
    }
}