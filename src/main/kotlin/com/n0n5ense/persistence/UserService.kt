package com.n0n5ense.persistence

import com.n0n5ense.model.*
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt

class UserService {
    companion object {
        fun init() {
            transaction {
                create(UserTable)
            }
        }

        fun get(id: String): User? {
            return transaction {
                kotlin.runCatching { User[id] }.getOrNull()
            }
        }

        fun create(user: RegisterUser) {
            transaction {
                User.new(user.id) {
                    name = user.name
                    role = user.role.name
                    password = BCrypt.hashpw(user.password, BCrypt.gensalt())
                }
            }
        }

        fun checkPassword(attemptUser: LoginUser): Boolean {
            val user = get(attemptUser.id) ?: return false
            return BCrypt.checkpw(attemptUser.password, user.password)
        }

        fun isAdminRole(id: String): Boolean {
            return get(id)?.role == UserRole.Admin.name
        }
    }
}