package com.n0n5ense.model

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object TouchCardTable: IntIdTable("touchCard", "id") {
    val name = text("name")
    val cardId = text("cardId").uniqueIndex()
    val enabled = bool("enabled")
    val owner = reference("owner", UserTable.id, onDelete = ReferenceOption.SET_NULL).nullable()
    val cardType = text("cardType")
}

class TouchCardEntity(id: EntityID<Int>): IntEntity(id) {
    companion object: IntEntityClass<TouchCardEntity>(TouchCardTable)

    var name by TouchCardTable.name
    var cardId by TouchCardTable.cardId
    var enabled by TouchCardTable.enabled
    var owner by TouchCardTable.owner
    var cardType by TouchCardTable.cardType
}