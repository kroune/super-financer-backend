package scheme

import dbQuery
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

data class ReadPostSchema(
    val id: Long,
    val title: String,
    val text: String,
    val tags: List<String>,
    val images: List<Long>,
    val userId: Long,
    val dateTime: LocalDateTime,
    val attachedNewsArticle: String?
)

data class InsertPostSchema(
    val title: String,
    val text: String,
    val tags: List<String>,
    val images: List<Long>,
    val userId: Long,
    val attachedNewsArticle: String?
)

class PostsService : KoinComponent {
    val database by inject<Database>()

    object Posts : Table() {
        val id = long("id").autoIncrement().uniqueIndex()
        val title = varchar("title", 50)
        val text = varchar("text", 1024)
        val tags = array<String>("tags")
        val images = array<Long>("images")
        val userId = reference("user_id", UserService.Users.id)
        val createdAt = datetime("created_at").index()
        val attachedNewsArticle = varchar("attached_articles", 200).nullable()

        override val primaryKey = PrimaryKey(id)
    }

    init {
        transaction(database) {
            SchemaUtils.create(Posts)
        }
    }

    suspend fun insertPost(post: InsertPostSchema): Long = dbQuery {
        Posts.insert {
            it[title] = post.title
            it[text] = post.text
            it[tags] = post.tags
            it[images] = post.images
            it[userId] = post.userId
            it[createdAt] = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            it[attachedNewsArticle] = post.attachedNewsArticle
        }[Posts.id]
    }

    suspend fun readLatest(offset: Long, limit: Int): List<ReadPostSchema> {
        return dbQuery {
            Posts.selectAll().orderBy(Posts.createdAt to SortOrder.DESC).offset(offset).limit(limit).map {
                ReadPostSchema(
                    it[Posts.id],
                    it[Posts.title],
                    it[Posts.text],
                    it[Posts.tags],
                    it[Posts.images],
                    it[Posts.userId],
                    it[Posts.createdAt],
                    it[Posts.attachedNewsArticle]
                )
            }
        }
    }

    suspend fun delete(id: Long) {
        dbQuery {
            Posts.deleteWhere { Posts.id eq id }
        }
    }
}
