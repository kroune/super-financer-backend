package scheme

import dbQuery
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@Suppress("RemoveRedundantQualifierName")
class LikesService : KoinComponent {
    val database by inject<Database>()

    object Likes : Table() {
        val id = long("id").autoIncrement()
        val userId = reference("user_id", UserService.Users.id).index()
        val likedPost = long("liked_post").index()

        override val primaryKey = PrimaryKey(id)
    }

    init {
        transaction(database) {
            SchemaUtils.create(Likes)
        }
    }

    suspend fun insertLike(userId: Long, postId: Long): Long = dbQuery {
        Likes.insert {
            it[Likes.userId] = userId
            it[Likes.likedPost] = postId
        }[Likes.id]
    }

    suspend fun isLiked(userId: Long, postId: Long): Boolean = dbQuery {
        Likes.selectAll().where {
            (Likes.userId eq userId) and (Likes.likedPost eq postId)
        }.any()
    }

    suspend fun deleteLike(userId: Long, postId: Long) {
        dbQuery {
            Likes.deleteWhere {
                (Likes.userId eq userId) and (Likes.likedPost eq postId)
            }
        }
    }

    suspend fun readLikes(userId: Long): List<Long> = dbQuery {
        Likes.selectAll().where {
            Likes.userId eq userId
        }.map {
            it[Likes.id]
        }
    }
}
