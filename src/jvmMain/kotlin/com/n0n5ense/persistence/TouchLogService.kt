package com.n0n5ense.persistence

import CardId
import com.n0n5ense.model.TouchCardLog
import com.n0n5ense.model.TouchCardLogTable
import com.n0n5ense.model.TouchCardTable
import com.n0n5ense.model.json.CardTouchLog
import com.n0n5ense.model.json.Count
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.sql.SortOrder
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

        fun add(cardId: CardId, accept: Boolean, time: LocalDateTime = LocalDateTime.now(Clock.systemUTC())) {
            transaction {
                TouchCardLog.new {
                    this.cardId = cardId.id
                    this.accept = accept
                    this.time = time
                    this.cardType = cardId.type.name
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
                    .slice(
                        listOf(
                            TouchCardLogTable.id,
                            TouchCardTable.name,
                            TouchCardLogTable.cardId,
                            TouchCardLogTable.accept,
                            TouchCardLogTable.time,
                            TouchCardLogTable.cardType
                        )
                    )
                    .selectAll()
                    .orderBy(TouchCardLogTable.id, SortOrder.DESC)
                    .limit(width, page.toLong()*width)
                    .map {
                        CardTouchLog(
                            it[TouchCardLogTable.id].value,
                            it.getOrNull(TouchCardTable.name),
                            CardId(
                                kotlin.runCatching { CardIdType.valueOf(it[TouchCardLogTable.cardType]) }.getOrDefault(CardIdType.Unknown),
                                it[TouchCardLogTable.cardId]
                            ),
                            it[TouchCardLogTable.accept],
                            it[TouchCardLogTable.time].toString(),

                        )
                    }
            }
        }

        fun count(): Count {
            return transaction {
                Count(TouchCardLog.count())
            }
        }

    }
}