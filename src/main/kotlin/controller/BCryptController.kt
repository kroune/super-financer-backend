package controller

import at.favre.lib.crypto.bcrypt.BCrypt

object BCryptController {
    private const val COMPLEXITY = 4

    fun hash(value: String, hashSalt: ByteArray): ByteArray {
        return BCrypt.withDefaults().hash(COMPLEXITY, hashSalt, value.toByteArray(Charsets.UTF_8))!!
    }

    fun verifyHash(value: ByteArray?, hash: ByteArray?): Boolean {
        if (value == null || hash == null) {
            return false
        }
        return BCrypt.verifyer()
            .verify(value, hash).verified
    }
}