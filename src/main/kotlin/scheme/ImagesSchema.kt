package scheme

import dbQuery
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.statements.api.ExposedBlob
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@Suppress("RemoveRedundantQualifierName")
class ImagesService : KoinComponent {
    val database by inject<Database>()

    object Images : Table() {
        val id = long("id").autoIncrement().uniqueIndex()
        val image = blob("image")

        override val primaryKey = PrimaryKey(id)
    }

    init {
        transaction(database) {
            SchemaUtils.create(Images)
        }
    }

    suspend fun insertImage(image: ByteArray): Long {
        return dbQuery {
            Images.insert {
                it[Images.image] = ExposedBlob(image)
            }[Images.id]
        }
    }

    suspend fun deleteImage(imageId: Long) {
        dbQuery {
            Images.deleteWhere {
                Images.id eq imageId
            }
        }
    }

    suspend fun readImages(imageId: Long): ByteArray? {
        return dbQuery {
            Images.select(Images.image).where {
                Images.id eq imageId
            }.limit(1).map {
                it[Images.image]
            }.singleOrNull()?.bytes
        }
    }
}
