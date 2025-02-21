package io.github.kroune

import configureRouting
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import loadConfig
import plugins.configureAdministration
import plugins.configureDatabases
import plugins.configureDI
import plugins.configureMonitoring

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureDI()
    loadConfig()
    configureSecurity()
    configureMonitoring()
    configureSerialization()
    configureDatabases()
    configureAdministration()
    configureRouting()
}
