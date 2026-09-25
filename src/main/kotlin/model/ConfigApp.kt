package com.kontenery.model

import java.nio.file.Path

data class ConfigApp(
    val env: EnvEnum,
    val clientId: String,
    val clientSecret: String,
    val refreshToken: String,
    val refreshTokenFile: Path,
    val apiName: String,
    val apiPort: String,
    val apiToken: String,
    val emailUser: String,
    val printRecipient: String,
) {
    companion object {
        fun load(): ConfigApp =
            ConfigApp(
                env = EnvEnum.valueOf(System.getenv("ENV") ?: "DEV"),
                clientId = requireSingleGoogleClientId(
                    System.getenv("GOOGLE_CLIENT_ID") ?: error("Missing GOOGLE_CLIENT_ID")
                ),
                clientSecret = System.getenv("GOOGLE_CLIENT_SECRET")?.trim()
                    ?: error("Missing GOOGLE_CLIENT_SECRET"),
                refreshToken = System.getenv("GOOGLE_REFRESH_TOKEN")?.trim()
                    ?: error("Missing GOOGLE_REFRESH_TOKEN"),
                refreshTokenFile = Path.of(
                    System.getenv("GOOGLE_REFRESH_TOKEN_FILE") ?: "data/google-refresh.token"
                ),
                apiName = System.getenv("API_NAME") ?: error("There is no api address"),
                apiPort = System.getenv("API_PORT") ?: error("There is no api port"),
                apiToken = System.getenv("INTERNAL_API_KEY") ?: error("There is no token"),
                emailUser = System.getenv("EMAIL_USER") ?: error("There is no email user"),
                printRecipient = System.getenv("PRINT_RECIPIENT") ?: "wilczynski87@gmail.com",
            )

        /**
         * Google OAuth accepts exactly one client_id.
         * A comma-separated list (common .env mistake) yields 401 "OAuth client was not found".
         */
        fun requireSingleGoogleClientId(raw: String): String {
            val parts = raw.split(',').map { it.trim() }.filter { it.isNotEmpty() }
            require(parts.size == 1) {
                "GOOGLE_CLIENT_ID must be a single OAuth client ID, got ${parts.size} values. " +
                    "Remove comma-separated IDs from .env / GitHub secrets."
            }
            val clientId = parts.single()
            require(clientId.endsWith(".apps.googleusercontent.com")) {
                "GOOGLE_CLIENT_ID does not look like a Google OAuth client ID"
            }
            return clientId
        }
    }
}

enum class EnvEnum {
    DEV,
    PROD
}