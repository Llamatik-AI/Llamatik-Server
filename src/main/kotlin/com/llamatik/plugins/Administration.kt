package com.llamatik.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.ShutDownUrl

fun Application.configureAdministration() {
    if (System.getenv("ENABLE_SHUTDOWN_URL")?.equals("true", ignoreCase = true) != true) {
        return
    }

    install(ShutDownUrl.ApplicationCallPlugin) {
        // Disabled by default because this URL terminates the process and has no authentication wrapper here.
        shutDownUrl = "/ktor/application/shutdown"
        // A function that will be executed to get the exit code of the process
        exitCodeSupplier = { 0 } // ApplicationCall.() -> Int
    }
}
