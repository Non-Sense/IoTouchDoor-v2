package com.n0n5ense.persistence

import com.n0n5ense.model.*
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction

class TouchCardService {
    companion object {
        fun init() {
            transaction {
                create(TouchCardTable)
            }
        }

        fun add(newTouchCard: NewTouchCard) {
            transaction {
                TouchCardEntity.new {
                    this.cardId = newTouchCard.cardId
                    this.name = newTouchCard.name
                    this.enabled = newTouchCard.enabled
                    this.owner = newTouchCard.owner?.let { EntityID(it, UserTable) }
                }
            }
        }

        fun find(cardId: String): TouchCard? {
            return kotlin.runCatching {
                transaction {
                    TouchCardEntity.find {
                        TouchCardTable.cardId eq cardId
                    }.single()
                }
            }.map(TouchCard.Companion::fromEntity).getOrNull()
        }

        fun updateOwner(owner: String, id: Int) {
            transaction {
                TouchCardEntity.findById(id)?.owner = EntityID(owner, UserTable)
            }
        }

        fun updateName(name: String, id: Int, userId: String): Boolean {
            return transaction {
                findTouchCard(id, userId)?.let {
                    it.name = name
                } != null
            }
        }

        fun updateEnable(enabled: Boolean, id: Int, userId: String): Boolean {
            return transaction {
                findTouchCard(id, userId)?.let {
                    it.enabled = enabled
                } != null
            }
        }

        private fun Transaction.findTouchCard(id: Int, userId: String): TouchCardEntity? {
            return TouchCardEntity.findById(id).takeIf { it?.owner?.value == userId }
        }
    }
}