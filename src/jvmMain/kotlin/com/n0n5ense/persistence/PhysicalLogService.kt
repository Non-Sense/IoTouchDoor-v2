package com.n0n5ense.persistence

import com.n0n5ense.model.PhysicalLog
import com.n0n5ense.model.PhysicalLogAction
import com.n0n5ense.model.PhysicalLogTable
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Clock
import java.time.LocalDateTime

class PhysicalLogService {
    companion object {
        fun init(){
            transaction {
                create(PhysicalLogTable)
            }
        }

        fun add(action: PhysicalLogAction, time: LocalDateTime = LocalDateTime.now(Clock.systemUTC())){
            transaction {
                PhysicalLog.new {
                    this.action = action.name
                    this.time = time
                }
            }
        }

        fun get(page: Int, width: Int = 50): List<PhysicalLog> {
            return transaction {
                PhysicalLog.all().limit(width, page.toLong()*width).toList()
            }
        }
    }
}