package plugins

import Config
import controller.CompressController
import controller.LikeController
import controller.PostsController
import controller.UserController
import io.ktor.server.application.*
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import scheme.LikesService
import scheme.PostsService
import scheme.UserService

fun Application.configureDI() {
    install(Koin) {
        modules(module {
            single { UserController() }
            single { UserService() }
            single { PostsService() }
            single { PostsController() }
            single { CompressController() }
            single { LikesService() }
            single { LikeController() }
        })
    }
}
