package com.kontenery.oauth

import com.kontenery.model.ConfigApp

data class GoogleOAuthCredentials(
    val clientId: String,
    val clientSecret: String,
) {
    companion object {
        fun from(config: ConfigApp): GoogleOAuthCredentials =
            GoogleOAuthCredentials(
                clientId = config.clientId,
                clientSecret = config.clientSecret,
            )
    }
}
