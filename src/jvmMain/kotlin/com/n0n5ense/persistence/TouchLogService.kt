package com.n0n5ense.persistence

import com.n0n5ense.model.TouchCardLog
import com.n0n5ense.model.TouchCardLogTable
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

class TouchLogService {
    companion object {
        fun init() {
            transaction {
                create(TouchCardLogTable)
            }
        }

        fun add(cardId: String, accept: Boolean, time: LocalDateTime = LocalDateTime.now()) {
            transaction {
                TouchCardLog.new {
                    this.cardId = cardId
                    this.accept = accept
                    this.time = time
                }
            }
        }

        fun get(page: Int, width: Int = 50): List<TouchCardLog> {
            return transaction {
                TouchCardLog.all().limit(width, page.toLong()*width).toList()
            }
        }

    }
}