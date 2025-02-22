package controller

import InsertPostBodySchema
import PostsFeedItem
import io.ktor.http.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import scheme.ImagesService
import scheme.InsertPostSchema
import scheme.LikesService
import scheme.PostsService
import kotlin.coroutines.coroutineContext

class PostsController : KoinComponent {
    val postsService by inject<PostsService>()
    val imagesService by inject<ImagesService>()
    val likeService by inject<LikesService>()

    suspend fun makeNewPost(postInfo: InsertPostBodySchema, userId: Long): Pair<String?, HttpStatusCode> {
        if (postInfo.title.length !in 1..40) {
            return "Title is too long" to HttpStatusCode.BadRequest
        }
        if (postInfo.text.length !in 1..1024) {
            return "Text is too long" to HttpStatusCode.BadRequest
        }
        val imagesAsIds = with(CoroutineScope(coroutineContext)) {
            postInfo.images.map {
                async {
                    imagesService.insertImage(it)
                }
            }.awaitAll()
        }
        postsService.insertPost(
            InsertPostSchema(
                postInfo.title,
                postInfo.text,
                postInfo.tags,
                imagesAsIds,
                userId,
                postInfo.attachedNewsArticle
            )
        )
        return null to HttpStatusCode.OK
    }

    suspend fun readLatestPosts(offset: Long, limit: Int, userId: Long?): List<PostsFeedItem> {
        val scope = CoroutineScope(coroutineContext)
        return postsService.readLatest(offset - 1, limit).map {
            val postId = it.id
            val images = it.images.map {
                scope.async {
                    imagesService.readImages(it)
                }
            }
            val isPostLiked = scope.async {
                userId?.let { likeService.isLiked(it, postId) }
            }
            PostsFeedItem(
                it.id,
                isPostLiked.await(),
                it.title,
                it.text,
                it.tags,
                images.awaitAll().filterNotNull(),
                it.userId,
                it.attachedNewsArticle
            )
        }
    }
}