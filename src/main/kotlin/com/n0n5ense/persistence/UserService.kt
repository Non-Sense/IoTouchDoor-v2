package com.n0n5ense.persistence

import com.n0n5ense.model.LoginUser
import com.n0n5ense.model.RegisterUser
import com.n0n5ense.model.UserTable
import com.n0n5ense.model.User
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.mindrot.jbcrypt.BCrypt
import java.sql.Connection

class UserService {
    companion object {
        fun init() {
            transaction {
                create(UserTable)
            }
        }

        private fun add(user: RegisterUser) {
            transaction(
                transactionIsolation = Connection.TRANSACTION_SERIALIZABLE,
                repetitionAttempts = 3
            ) {
                UserTable.insert {
                    it[id] = user.id
                    it[name] = user.name
                    it[password] = user.password
                }
            }
        }

        fun get(id: String): User? {
            val t = transaction {
                UserTable.select {
                    UserTable.id.eq(id)
                }.firstOrNull()
            } ?: return null

            return User(
                t[UserTable.id],
                t[UserTable.name],
                t[UserTable.password],
                t[UserTable.enabled]
            )
        }

        fun create(user: RegisterUser) {
            val newUser = RegisterUser(
                user.id,
                user.name,
                BCrypt.hashpw(user.password, BCrypt.gensalt())
            )
            add(newUser)
        }

        fun checkPassword(attemptUser: LoginUser): Boolean {
            val user = get(attemptUser.id) ?: return false
            return BCrypt.checkpw(attemptUser.password, user.password)
        }
    }
}