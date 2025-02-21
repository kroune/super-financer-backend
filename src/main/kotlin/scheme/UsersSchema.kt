package scheme

import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@Serializable
data class User(val id: Long, val login: String, val passwordHash: ByteArray, val hashSalt: ByteArray)

@Serializable
data class InsertUserSchema(val login: String, val passwordHash: ByteArray, val hashSalt: ByteArray)

@Serializable
data class UserInfo(val login: String)

class UserService : KoinComponent {
    val database by inject<Database>()

    object Users : Table() {
        val id = long("id").autoIncrement()
        val login = varchar("login", length = 255).uniqueIndex()
        val passwordHash = binary("password_hash")
        val hashSalt = binary("hash_salt", 16)

        override val primaryKey = PrimaryKey(id)
    }

    init {
        transaction(database) {
            SchemaUtils.create(Users)
        }
    }

    suspend fun insertUser(user: InsertUserSchema): Long = dbQuery {
        Users.insert {
            it[login] = user.login
            it[passwordHash] = user.passwordHash
            it[hashSalt] = user.hashSalt
        }[Users.id]
    }

    suspend fun getUserById(id: Long): User? {
        return dbQuery {
            Users.selectAll()
                .where { Users.id eq id }
                .map { User(it[Users.id], it[Users.login], it[Users.passwordHash], it[Users.hashSalt]) }
                .singleOrNull()
        }
    }

    suspend fun loginExists(login: String): Boolean {
        return dbQuery {
            Users.selectAll().where { Users.login eq login }.any()
        }
    }

    suspend fun getUserByLogin(login: String): User? {
        return dbQuery {
            Users.selectAll().where { Users.login eq login }
                .map { User(it[Users.id], it[Users.login], it[Users.passwordHash], it[Users.hashSalt]) }
                .singleOrNull()
        }
    }

    suspend fun delete(id: Long) {
        dbQuery {
            Users.deleteWhere { Users.id eq id }
        }
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}

