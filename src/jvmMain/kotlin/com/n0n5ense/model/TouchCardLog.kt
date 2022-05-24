package com.n0n5ense.model

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.View
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime

object TouchCardLogTable: IntIdTable("touchCardLog", "id") {
    val cardId = text("cardId")
    val accept = bool("accept")
    val time = datetime("time")
}

class TouchCardLog(id: EntityID<Int>): IntEntity(id) {
    companion object: IntEntityClass<TouchCardLog>(TouchCardLogTable)

    var cardId by TouchCardLogTable.cardId
    var accept by TouchCardLogTable.accept
    var time by TouchCardLogTable.time
}