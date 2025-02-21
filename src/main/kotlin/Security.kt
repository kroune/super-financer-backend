package io.github.kroune

import Config
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import controller.BCryptController
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import kotlinx.datetime.Clock
import kotlinx.datetime.toKotlinInstant
import org.koin.ktor.ext.get
import scheme.UserService

fun Application.configureSecurity() {
    val jwtConfig = get<Config>().jwtConfig
    install(Authentication) {
        jwt("auth-jwt") {
            verifier(
                JWT
                    .require(Algorithm.HMAC512(jwtConfig.secret))
                    .withSubject("Authentication")
                    .withIssuer(jwtConfig.issuer)
                    .withAudience(jwtConfig.audience)
                    .build()
            )
            validate {
                // if smth is wrong -> exception is thrown
                runCatching {
                    val id = it.payload.getClaim("userId").asLong()!!
                    val userService = get<UserService>()
                    val userSchema = userService.getUserById(id)!!
                    require(it.payload.getClaim("login").asString() == userSchema.login)
                    val password = it.payload.getClaim("password").asString()!!
                    require(
                        BCryptController.verifyHash(
                            password.toByteArray(Charsets.UTF_8),
                            userSchema.passwordHash
                        )
                    )
                    require(it.payload.issuedAtAsInstant.toKotlinInstant() <= Clock.System.now())
                    return@validate JWTPrincipal(it.payload)
                }.onFailure { it ->
                    return@validate null
                }
            }
            challenge { defaultScheme, realm ->
                call.respond(HttpStatusCode.Unauthorized.description("Token is not valid or has expired"))
                return@challenge
            }
        }
    }
}
