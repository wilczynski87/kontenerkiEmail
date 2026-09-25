package com.kontenery.controller

import com.kontenery.service.AppHealthResponse
import com.kontenery.service.GmailHealthService
import com.kontenery.service.GmailHealthStatus
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route

fun Route.healthRoutes(gmailHealth: GmailHealthService) {
    route("healthcheck") {
        // Liveness + last known Gmail status (no live Google call)
        get {
            val gmail = gmailHealth.lastStatus()
            val httpStatus = when (gmail.status) {
                GmailHealthStatus.DOWN -> HttpStatusCode.ServiceUnavailable
                GmailHealthStatus.UNKNOWN -> HttpStatusCode.OK
                GmailHealthStatus.UP, GmailHealthStatus.DEGRADED -> HttpStatusCode.OK
            }
            call.respond(
                httpStatus,
                AppHealthResponse(
                    status = if (gmail.status == GmailHealthStatus.DOWN) "degraded" else "ok",
                    gmail = gmail,
                ),
            )
        }

        // On-demand live probe: validates client id/secret + refresh token + Gmail API
        get("gmail") {
            val gmail = gmailHealth.checkNow()
            val httpStatus = when (gmail.status) {
                GmailHealthStatus.UP -> HttpStatusCode.OK
                GmailHealthStatus.DEGRADED -> HttpStatusCode.OK
                GmailHealthStatus.DOWN, GmailHealthStatus.UNKNOWN -> HttpStatusCode.ServiceUnavailable
            }
            call.respond(
                httpStatus,
                AppHealthResponse(
                    status = when (gmail.status) {
                        GmailHealthStatus.UP -> "ok"
                        GmailHealthStatus.DEGRADED -> "degraded"
                        else -> "down"
                    },
                    gmail = gmail,
                ),
            )
        }
    }
}
