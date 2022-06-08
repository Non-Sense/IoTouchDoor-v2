package com.n0n5ense.persistence

import CardId
import com.n0n5ense.model.TouchCardEntity
import com.n0n5ense.model.TouchCardTable
import com.n0n5ense.model.UserTable
import com.n0n5ense.model.json.Count
import com.n0n5ense.model.json.NewTouchCard
import com.n0n5ense.model.json.TouchCard
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction

class TouchCardService {
    companion object {

        private fun fromEntity(entity: TouchCardEntity): TouchCard {
            return TouchCard(
                entity.id.value,
                entity.name,
                CardId(
                    kotlin.runCatching { CardIdType.valueOf(entity.cardType) }.getOrDefault(CardIdType.Unknown),
                    entity.cardId
                ),
                entity.enabled,
                entity.owner?.value,

            )
        }

        fun init() {
            transaction {
                create(TouchCardTable)
            }
        }

        fun add(newTouchCard: NewTouchCard) {
            transaction {
                TouchCardEntity.new {
                    this.cardId = newTouchCard.cardId.id
                    this.name = newTouchCard.name
                    this.enabled = newTouchCard.enabled
                    this.owner = newTouchCard.owner?.let { EntityID(it, UserTable) }
                    this.cardType = newTouchCard.cardId.type.name
                }
            }
        }

        fun get(id: Int): TouchCard? {
            return transaction {
                TouchCardEntity.findById(id)?.let(::fromEntity)
            }
        }

        fun get(page: Int, width: Int = 50): List<TouchCard> {
            return transaction {
                TouchCardEntity
                    .all()
                    .limit(width, page.toLong()*width)
                    .toList()
                    .map(::fromEntity)
            }
        }

        fun delete(id: Int): Boolean {
            return transaction {
                TouchCardEntity.findById(id)?.apply {
                    this.delete()
                }
            } != null
        }

        fun count(): Count {
            return transaction {
                Count(TouchCardEntity.count())
            }
        }

        fun find(cardId: CardId): TouchCard? {
            return kotlin.runCatching {
                transaction {
                    TouchCardEntity.find {
                        (TouchCardTable.cardId eq cardId.id) and (TouchCardTable.cardType eq cardId.type.name)
                    }.single()
                }
            }.map(::fromEntity).getOrNull()
        }

        fun updateOwner(owner: String, id: Int) {
            transaction {
                TouchCardEntity.findById(id)?.owner = EntityID(owner, UserTable)
            }
        }

        fun updateName(id: Int, name: String): Boolean {
            return transaction {
                TouchCardEntity.findById(id)?.apply {
                    this.name = name
                } != null
            }
        }

        fun updateEnable(id: Int, enabled: Boolean): Boolean {
            return transaction {
                TouchCardEntity.findById(id)?.apply {
                    this.enabled = enabled
                } != null
            }
        }
    }
}