package com.n0n5ense.model

import com.n0n5ense.model.json.UserRole
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable

object UserTable: IdTable<String>("user") {
    override val id = varchar("id", 64).uniqueIndex().entityId()
    override val primaryKey = PrimaryKey(id)
    val name = text("name")
    val password = char("password", 60)
    val enabled = bool("enabled").default(true)
    val role = text("role").default(UserRole.None.name)
}

class User(id: EntityID<String>): Entity<String>(id) {
    companion object: EntityClass<String, User>(UserTable)
    var name by UserTable.name
    var password by UserTable.password
    var enabled by UserTable.enabled
    var role by UserTable.role
}