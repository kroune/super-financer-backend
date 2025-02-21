package controller

import io.ktor.http.HttpStatusCode
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import scheme.InsertPostBodySchema
import scheme.InsertPostSchema
import scheme.PostsService

class PostsController: KoinComponent {
    suspend fun makeNewPost(postInfo: InsertPostBodySchema, userId: Long): Pair<String?, HttpStatusCode> {
        if (postInfo.title.length !in 1..40) {
            return "Title is too long" to HttpStatusCode.BadRequest
        }
        if (postInfo.text.length !in 1..1024) {
            return "Text is too long" to HttpStatusCode.BadRequest
        }
        val compressController by inject<CompressController>()
        val compressedImage = postInfo.images
//            .map {
//            compressController.compress(it)
//        }
        val postsService by inject<PostsService>()
        postsService.insertPost(
            InsertPostSchema(
                postInfo.title,
                postInfo.text,
                postInfo.tags,
                compressedImage,
                userId,
                postInfo.attachedNewsArticle
            )
        )
        return null to HttpStatusCode.OK
    }
}