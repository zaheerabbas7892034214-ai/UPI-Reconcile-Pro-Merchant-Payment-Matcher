package com.zaheer.upireconcilepro.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "unmatched_items",
    foreignKeys = [
        ForeignKey(
            entity = ReconciliationSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sessionId"), Index("type")]
)
data class UnmatchedItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: Long,
    val type: String,
    val reference: String,
    val amount: Double,
    val date: Long,
    val reason: String
)
