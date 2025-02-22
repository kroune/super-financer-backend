//package scheme
//
//import kotlinx.coroutines.Dispatchers
//import org.jetbrains.exposed.sql.*
//import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
//import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
//import org.jetbrains.exposed.sql.transactions.transaction
//import org.koin.core.component.KoinComponent
//import org.koin.core.component.inject
//
//class CommentsService : KoinComponent {
//    val database by inject<Database>()
//
//    object Comments : Table() {
//        val id = long("id").autoIncrement()
//        val parentPost = reference("parent_post", PostsService.Posts.id)
//        val userId = reference("user_id", UserService.Users.id).index()
//        val comment = long("comment").index()
//
//        override val primaryKey = PrimaryKey(id)
//    }
//
//    init {
//        transaction(database) {
//            SchemaUtils.create(Comments)
//        }
//    }
//
//    suspend fun insertLike(userId: Long, postId: Long): Long = dbQuery {
//        Comments.insert {
//            it[Comments.userId] = userId
//            it[Comments.likedPost] = postId
//        }[Comments.id]
//    }
//
//    suspend fun isLiked(userId: Long, postId: Long): Boolean = dbQuery {
//        Comments.selectAll().where {
//            (Comments.userId eq userId) and (Comments.likedPost eq postId)
//        }.any()
//    }
//
//    suspend fun deleteLike(userId: Long, postId: Long) {
//        dbQuery {
//            Comments.deleteWhere {
//                (Comments.userId eq userId) and (Comments.likedPost eq postId)
//            }
//        }
//    }
//
//    suspend fun readComments(userId: Long): List<Long> = dbQuery {
//        Comments.selectAll().where {
//            Comments.userId eq userId
//        }.map {
//            it[Comments.id]
//        }
//    }
//
//}
