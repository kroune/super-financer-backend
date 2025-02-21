package controller

import io.ktor.http.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import scheme.LikesService

class LikeController : KoinComponent {
    suspend fun likePost(userId: Long, postId: Long): Pair<String?, HttpStatusCode> {
        val likesService by inject<LikesService>()
        if (likesService.isLiked(userId, postId)) {
            return "post is already liked" to HttpStatusCode.OK
        }
        likesService.insertLike(userId, postId)
        return null to HttpStatusCode.OK
    }

    suspend fun unlikePost(userId: Long, postId: Long): Pair<String?, HttpStatusCode> {
        val likesService by inject<LikesService>()
        if (!likesService.isLiked(userId, postId)) {
            return "post is not liked" to HttpStatusCode.OK
        }
        likesService.deleteLike(userId, postId)
        return null to HttpStatusCode.OK
    }
}