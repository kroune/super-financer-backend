package plugins

import Config
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import org.koin.core.context.GlobalContext
import org.koin.ktor.ext.get

fun Application.configureDatabases() {
    val database = with(get<Config>().databaseConfig) {
        Database.connect(
            url = url,
            user = user,
            driver = driver,
            password = password,
        )
    }
    GlobalContext.get().declare(database)
}
