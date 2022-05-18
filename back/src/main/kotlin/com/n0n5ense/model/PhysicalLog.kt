package com.n0n5ense.model

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.datetime

enum class PhysicalLogAction{
    Open,
    Close,
    Unlock,
    Lock
}

object PhysicalLogTable: LongIdTable("physicalLog", "id") {
    val action = text("action")
    val time = datetime("time")
}

class PhysicalLog(id: EntityID<Long>): LongEntity(id) {
    companion object: LongEntityClass<PhysicalLog>(PhysicalLogTable)

    var action by PhysicalLogTable.action
    var time by PhysicalLogTable.time
}