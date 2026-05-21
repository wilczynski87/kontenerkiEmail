package com.kontenery.service

import com.kontenery.model.ConfigApp
import com.kontenery.model.MailSendParam
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import java.time.LocalDate

class SendRequest(private val httpClient: HttpClient, configApp: ConfigApp) {
    private val baseUrl = "http://${configApp.apiName}:${configApp.apiPort}"
    private val token = configApp.apiToken

    suspend fun confirmInvoiceSend(invoiceNumber: String) {
        val response = httpClient.get("$baseUrl/mailSend/invoice") {
            header("X-Internal-Key", token)
            url {
                parameters.append("invoiceNumber", invoiceNumber)
                parameters.append("sendDate", LocalDate.now().toString())
            }
        }
        if (!response.status.isSuccess()) {
            logApiFailure("confirmInvoiceSend", invoiceNumber, response)
        }
    }

    suspend fun mailSendError(invoiceNumber: String, message: String? = null) {
        val response = httpClient.get("$baseUrl/mailSend") {
            header("X-Internal-Key", token)
            url {
                parameters.append(MailSendParam.INVOICE_NUMBER.param, invoiceNumber)
                parameters.append(MailSendParam.SEND_DATE.param, LocalDate.now().toString())
                parameters.append(MailSendParam.ERROR.param, "ERROR")
                if (message != null) {
                    parameters.append(MailSendParam.MESSAGE.param, message)
                }
            }
        }
        if (!response.status.isSuccess()) {
            logApiFailure("mailSendError", invoiceNumber, response)
        }
    }

    private suspend fun logApiFailure(action: String, invoiceNumber: String, response: HttpResponse) {
        val body = runCatching { response.bodyAsText() }.getOrDefault("")
        println("API $action failed for $invoiceNumber: ${response.status} $body")
    }
}
