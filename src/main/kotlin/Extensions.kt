import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
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
