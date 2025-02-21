package controller

import Config
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import model.JwtTokenBody
import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

object JwtController : KoinComponent {
    private val issuer = get<Config>().jwtConfig.issuer
    private val secret = get<Config>().jwtConfig.secret
    private val audience = get<Config>().jwtConfig.audience

    fun produce(jwtTokenBody: JwtTokenBody): String {
        return JWT.create()
            .withSubject("Authentication")
            .withIssuer(issuer)
            .withAudience(audience)
            .withClaim("login", jwtTokenBody.login)
            .withClaim("userId", jwtTokenBody.userId)
            .withClaim("password", jwtTokenBody.password)
            .withIssuedAt(Clock.System.now().toJavaInstant())
            .sign(Algorithm.HMAC512(secret))
    }
}
