import io.ktor.http.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.koin.core.context.GlobalContext
import kotlin.coroutines.CoroutineContext

suspend inline fun <T> dbQuery(
    context: CoroutineContext? = Dispatchers.IO,
    db: Database? = null,
    transactionIsolation: Int? = null,
    readOnly: Boolean? = null,
    crossinline block: suspend Transaction.() -> T
): T = newSuspendedTransaction(
    context,
    db,
    transactionIsolation,
    readOnly
) { block() }

@OptIn(ExperimentalSerializationApi::class)
@Serializable
class ApiResponse<T>(
    val status: Status,
    val data: T,
    @Suppress("unused") @EncodeDefault
    val apiVersion: Int = GlobalContext.get().get<Config>().apiVersion
)

suspend inline fun <reified T> RoutingContext.apiResponse(apiResponse: ApiResponse<T>) {
    call.respondText(
        status = when (apiResponse.status) {
            is Status.Success -> {
                HttpStatusCode(200, "")
            }

            is Status.Error -> {
                HttpStatusCode(apiResponse.status.code, apiResponse.status.message)
            }
        },
    ) {
        Json.encodeToString(ApiResponse(apiResponse.status, apiResponse.data))
    }
}

suspend inline fun <reified T> RoutingContext.apiResponse(status: Status, data: T) {
    call.respondText(
        status = when (status) {
            is Status.Success -> {
                HttpStatusCode(200, "")
            }

            is Status.Error -> {
                HttpStatusCode(status.code, status.message)
            }
        },
    ) {
        Json.encodeToString(ApiResponse(status, data))
    }
}

suspend inline fun <reified T> JWTChallengeContext.apiResponse(status: Status, data: T) {
    call.respondText(
        status = when (status) {
            is Status.Success -> {
                HttpStatusCode(200, "")
            }

            is Status.Error -> {
                HttpStatusCode(status.code, status.message)
            }
        },
    ) {
        Json.encodeToString(ApiResponse(status, data))
    }
}

@Serializable
sealed class Status {
    @Serializable
    data object Success : Status()

    @Serializable
    data class Error(val code: Int, val message: String) : Status()
}
