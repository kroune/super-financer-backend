package controller

import io.ktor.http.*
import model.JwtTokenBody
import model.UserLoginBody
import model.UserRegistrationBody
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import scheme.InsertUserSchema
import scheme.UserInfo
import scheme.UserService
import java.security.SecureRandom

class UserController : KoinComponent {
    val userService by inject<UserService>()
    suspend fun register(info: UserRegistrationBody): Pair<String?, HttpStatusCode> {
        if (info.login.any { !it.isLetterOrDigit() || it.isWhitespace() }) {
            return null to HttpStatusCode.BadRequest.description("Invalid username")
        }
        if (info.password.any { (!it.isLetterOrDigit() && it != '!' && it != '.' && it != '?') || it.isWhitespace() }) {
            return null to HttpStatusCode.BadRequest.description("Invalid password")
        }
        if (userService.loginExists(info.login)) {
            return null to HttpStatusCode.Conflict.description("User with this login already exists")
        }

        val random = SecureRandom()
        val hashSalt = ByteArray(16)
        random.nextBytes(hashSalt)

        val passwordHash = BCryptController.hash(info.password, hashSalt)
        val userId = userService.insertUser(InsertUserSchema(info.login, passwordHash, hashSalt))
        val jwtToken = JwtController.produce(
            JwtTokenBody(
                info.login,
                info.password,
                userId
            )
        )
        return jwtToken to HttpStatusCode.OK
    }

    suspend fun login(info: UserLoginBody): Pair<String?, HttpStatusCode> {
        val userInfo = userService.getUserByLogin(info.login) ?: run {
            return null to HttpStatusCode.Unauthorized.description("Invalid login or password")
        }
        if (!BCryptController.verifyHash(info.password.toByteArray(Charsets.UTF_8), userInfo.passwordHash)) {
            return null to HttpStatusCode.Unauthorized.description("Invalid login or password")
        }
        val jwtToken = JwtController.produce(
            JwtTokenBody(
                info.login,
                info.password,
                userInfo.id
            )
        )
        return jwtToken to HttpStatusCode.OK
    }

    suspend fun getUserInfo(userId: Long): Pair<UserInfo?, HttpStatusCode> {
        val userInfo = userService.getUserById(userId)
        if (userInfo == null) {
            return null to HttpStatusCode.BadRequest
        }
        return UserInfo(userInfo.login) to HttpStatusCode.OK
    }

    fun likePost(postId: Long, userId: Long) {
        userService
        TODO("Not yet implemented")
    }
}