package plugins

import Config
import io.github.flaxoos.ktor.server.plugins.ratelimiter.RateLimiting
import io.github.flaxoos.ktor.server.plugins.ratelimiter.implementations.TokenBucket
import io.ktor.server.application.*
import org.koin.ktor.ext.get

fun Application.configureAdministration() {
    install(RateLimiting) {
        rateLimiter {
            with(get<Config>()) {
                type = TokenBucket::class
                capacity = ratelimitConfig.capacity
                rate = ratelimitConfig.rate
            }
        }
    }
}
