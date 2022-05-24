package com.n0n5ense.persistence

import com.n0n5ense.model.TouchCardLog
import com.n0n5ense.model.TouchCardLogTable
import com.n0n5ense.model.TouchCardTable
import com.n0n5ense.model.json.CardTouchLog
import com.n0n5ense.model.json.Count
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.sql.leftJoin
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Clock
import java.time.LocalDateTime

class TouchLogService {
    companion object {
        fun init() {
            transaction {
                create(TouchCardLogTable)
            }
        }

        fun add(cardId: String, accept: Boolean, time: LocalDateTime = LocalDateTime.now(Clock.systemUTC())) {
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

        fun getWithName(page: Int, width: Int = 50): List<CardTouchLog> {
            return transaction {
                TouchCardLogTable
                    .leftJoin(TouchCardTable, onColumn = { cardId }, otherColumn = { cardId })
                    .slice(listOf(
                        TouchCardLogTable.id,
                        TouchCardTable.name,
                        TouchCardLogTable.cardId,
                        TouchCardLogTable.accept,
                        TouchCardLogTable.time
                    ))
                    .selectAll()
                    .limit(width, page.toLong()*width)
                    .map { CardTouchLog(
                        it[TouchCardLogTable.id].value,
                        it.getOrNull(TouchCardTable.name),
                        it[TouchCardLogTable.cardId],
                        it[TouchCardLogTable.accept],
                        it[TouchCardLogTable.time].toString()
                    ) }
            }
        }

        fun count(): Count {
            return transaction {
                Count(TouchCardLog.count())
            }
        }

    }
}