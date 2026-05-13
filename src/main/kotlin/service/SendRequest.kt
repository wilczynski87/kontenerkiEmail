package com.kontenery.service

import com.kontenery.model.ConfigApp
import com.kontenery.model.MailSendParam
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.server.application.Application
import java.time.LocalDate

class SendRequest(private val httpClient: HttpClient, configApp: ConfigApp) {
    val baseUrl = "http://${configApp.apiName}:${configApp.apiPort}"
    val token = configApp.apiToken

    suspend fun confirmInvoiceSend(invoiceNumber:String) {
        val url = "$baseUrl/mailSend/invoice"
        println("url: $url")
        val send = httpClient.get(url) {
            header("X-Internal-Key", token)
            url {
                parameters.append("invoiceNumber", invoiceNumber)
                parameters.append("sendDate", LocalDate.now().toString())
            }
        }
        println(send)
    }

    suspend fun mailSendError(invoiceNumber:String, message: String? = null) {
        httpClient.get("$baseUrl/mailSend") {
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
    }

}