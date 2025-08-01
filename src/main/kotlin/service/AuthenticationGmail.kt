package com.kontenery.service

import java.io.IOException
import com.google.api.client.googleapis.auth.oauth2.*;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp

object GmailOAuth2Login {
    @Throws(IOException::class)
    fun authorize(): Credential {
        val clientSecrets: GoogleClientSecrets = GoogleClientSecrets()
            .setInstalled(
                GoogleClientSecrets.Details()
                    .setClientId(getClientId())
                    .setClientSecret(getSecret())
            )

        val flow: GoogleAuthorizationCodeFlow = GoogleAuthorizationCodeFlow.Builder(
            NetHttpTransport(),
            JacksonFactory.getDefaultInstance(),
            clientSecrets,
            listOf("https://mail.google.com/")
        )
            .setAccessType("offline")
            .setApprovalPrompt("force")
            .build()

        val receiver: LocalServerReceiver = LocalServerReceiver.Builder().setPort(8888).build()
        return AuthorizationCodeInstalledApp(flow, receiver).authorize("user")
    }

    private fun getSecret(): String {
        return System.getenv("GMAIL_CLIENT_SECRET") ?: throw NullPointerException("There is no GMAIL_CLIENT_SECRET in GmailOAuth2Login")
    }
    private fun getClientId(): String {
        return System.getenv("GMAIL_CLIENT_ID") ?: throw NullPointerException("There is no api port in GmailOAuth2Login")
    }
}
