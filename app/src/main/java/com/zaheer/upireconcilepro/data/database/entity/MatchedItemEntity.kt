package com.zaheer.upireconcilepro.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "matched_items",
    foreignKeys = [
        ForeignKey(
            entity = ReconciliationSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sessionId")]
)
data class MatchedItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: Long,
    val invoiceRef: String,
    val upiRef: String,
    val amount: Double,
    val invoiceDate: Long,
    val upiDate: Long,
    val matchType: String,
    val merchant: String
)
