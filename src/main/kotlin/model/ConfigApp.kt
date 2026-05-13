package com.kontenery.model

data class ConfigApp(
    val env: EnvEnum,
    val clientId: String,
    val clientSecret: String,
    val refreshToken: String,
    val apiName: String,
    val apiPort: String,
    val apiToken: String,
    val emailUser: String
) {
    companion object {
        fun load(): ConfigApp =
            ConfigApp(
                env = EnvEnum.valueOf(System.getenv("ENV") ?: "DEV"),
                clientId = System.getenv("GOOGLE_CLIENT_ID") ?: error("Missing GOOGLE_CLIENT_ID"),
                clientSecret = System.getenv("GOOGLE_CLIENT_SECRET") ?: error("Missing GOOGLE_CLIENT_SECRET"),
                refreshToken = System.getenv("GOOGLE_REFRESH_TOKEN") ?: error("Missing GOOGLE_REFRESH_TOKEN"),
                apiName = System.getenv("API_NAME") ?: error("There is no api address"),
                apiPort = System.getenv("API_PORT") ?: error("There is no api port"),
                apiToken = System.getenv("INTERNAL_API_KEY") ?: error("There is no token"),
                emailUser = System.getenv("EMAIL_USER") ?: error("There is no email user"),
            )
    }
    fun ConfigApp.withRefreshToken(newToken: String) =
        copy(refreshToken = newToken)
}

enum class EnvEnum {
    DEV,
    PROD
}