import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.koin.core.context.GlobalContext
import java.io.File
import kotlin.time.Duration

fun loadConfig() {
    val configDirectory =
        System.getenv("CONFIG_DIR") ?: "/etc/super-financer/config.json"
    val file = File(configDirectory)
    require(file.exists()) { "Configuration file doesn't exist in the provided path - $configDirectory" }
    val content = file.readText()
    val config = Json.decodeFromString<Config>(content)
    GlobalContext.get().declare(config)
}

@Serializable
data class Config(
    val deploymentConfig: DeploymentConfig,
    val ratelimitConfig: RatelimitConfig,
    val databaseConfig: DatabaseConfig,
    val jwtConfig: JwtConfig,
    val apiVersion: Int
)

@Serializable
data class JwtConfig(
    val secret: String,
    val audience: String,
    val issuer: String,
    val expiresAt: Duration
)

@Serializable
data class DeploymentConfig(
    val port: Int
)

@Serializable
data class RatelimitConfig(
    val capacity: Int,
    val rate: Duration
)

@Serializable
data class DatabaseConfig(
    val url: String,
    val user: String,
    val password: String,
    val driver: String
)
