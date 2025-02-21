import controller.LikeController
import controller.PostsController
import controller.UserController
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import model.UserLoginBody
import model.UserRegistrationBody
import org.koin.ktor.ext.inject
import scheme.InsertPostBodySchema
import scheme.PostsService

fun Application.configureRouting() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respondText(text = "500: $cause", status = HttpStatusCode.InternalServerError)
            cause.printStackTrace()
        }
    }
    routing {
        get("/") {
            call.respondText("Hello World!")
        }
        route("api") {
            route("auth") {
                post("register") {
                    val userInfo = try {
                        Json.decodeFromString<UserRegistrationBody>(call.receiveText())
                    } catch (e: SerializationException) {
                        call.respond(
                            HttpStatusCode.BadRequest.description("Unable to decode request body ${e.message}")
                        )
                        return@post
                    }
                    val userController by inject<UserController>()
                    val (response, statusCode) = userController.register(userInfo)
                    if (response != null) {
                        call.respondText(Json.encodeToString(response), status = statusCode)
                    } else {
                        call.respond(statusCode)
                    }
                    return@post
                }
                post("login") {
                    val userInfo = try {
                        Json.decodeFromString<UserLoginBody>(call.receiveText())
                    } catch (e: SerializationException) {
                        call.respond(
                            HttpStatusCode.BadRequest.description("Unable to decode request body ${e.message}")
                        )
                        return@post
                    }
                    val userController by inject<UserController>()
                    val (response, statusCode) = userController.login(userInfo)
                    if (response != null) {
                        call.respondText(Json.encodeToString(response), status = statusCode)
                    } else {
                        call.respond(statusCode)
                    }
                    return@post
                }
            }
            route("feed") {
                authenticate("auth-jwt", optional = true) {
                    get {
                        val principal = call.principal<JWTPrincipal>()
                        val userId = principal?.payload?.getClaim("userId")?.asLong()
                        val offset = call.queryParameters["offset"]?.toLongOrNull()
                        if (offset == null || offset < 0) {
                            apiResponse(Status.Error(badRequest, "Invalid offset"), null)
                            return@get
                        }
                        val limit = call.queryParameters["limit"]?.toIntOrNull()
                        if (limit == null || limit !in 1..100) {
                            apiResponse(Status.Error(badRequest, "Invalid limit"), null)
                            return@get
                        }
                        val postsService by inject<PostsService>()
                        val posts = postsService.readLatest(offset - 1, limit, userId).map {
                            PostsFeedItem(
                                it.id,
                                it.isLiked,
                                it.title,
                                it.text,
                                it.tags,
                                it.images,
                                it.userId,
                                it.attachedNewsArticle
                            )
                        }
                        val response = Json.encodeToString(posts)
                        call.respondText(response)
                        return@get
                    }
                }
                authenticate("auth-jwt") {
                    route("like") {
                        post {
                            val principal = call.principal<JWTPrincipal>()
                            val userId = principal!!.payload.getClaim("userId").asLong()
                            val postId = call.parameters["postId"]?.toLongOrNull()
                            if (postId == null) {
                                call.respond(HttpStatusCode.BadRequest.description("you must provide [postId]"))
                                return@post
                            }
                            val likeController by inject<LikeController>()
                            val (response, statusCode) = likeController.likePost(userId, postId)
                            if (response != null) {
                                call.respondText(Json.encodeToString(response), status = statusCode)
                            } else {
                                call.respond(statusCode)
                            }
                            return@post
                        }
                        delete {
                            val principal = call.principal<JWTPrincipal>()
                            val userId = principal!!.payload.getClaim("userId").asLong()
                            val postId = call.parameters["postId"]?.toLongOrNull()
                            if (postId == null) {
                                call.respond(HttpStatusCode.BadRequest.description("you must provide [postId]"))
                                return@delete
                            }
                            val likeController by inject<LikeController>()
                            val (response, statusCode) = likeController.unlikePost(userId, postId)
                            if (response != null) {
                                call.respondText(Json.encodeToString(response), status = statusCode)
                            } else {
                                call.respond(statusCode)
                            }
                            return@delete
                        }
                    }
                    post("new") {
                        val principal = call.principal<JWTPrincipal>()
                        val userId = principal!!.payload.getClaim("userId").asLong()
                        val postInfo = try {
                            Json.decodeFromString<InsertPostBodySchema>(call.receiveText())
                        } catch (e: SerializationException) {
                            call.respondText(
                                "Unable to decode request body ${e.message}",
                                status = HttpStatusCode.BadRequest
                            )
                            return@post
                        }
                        val postsController by inject<PostsController>()
                        val (response, statusCode) = postsController.makeNewPost(
                            postInfo, userId
                        )
                        if (response != null) {
                            call.respondText(Json.encodeToString(response), status = statusCode)
                        } else {
                            call.respond(statusCode)
                        }
                        return@post
                    }
                }
            }
            route("user") {
                get {
                    val userId = call.queryParameters["userId"]?.toLongOrNull()
                    if (userId == null) {
                        call.respond(HttpStatusCode.BadRequest.description("you must provide [userId]"))
                        return@get
                    }
                    val userController by inject<UserController>()
                    val (response, statusCode) = userController.getUserInfo(userId)
                    if (response != null) {
                        call.respondText(Json.encodeToString(response), status = statusCode)
                    } else {
                        call.respond(statusCode)
                    }
                    return@get
                }
            }
        }
    }
}

@Serializable
data class PostsFeedItem(
    val postId: Long,
    val isLiked: Boolean?,
    val title: String,
    val text: String,
    val tags: List<String>,
    val images: List<ByteArray>,
    val userId: Long,
    val attachedNewsArticle: String?
)