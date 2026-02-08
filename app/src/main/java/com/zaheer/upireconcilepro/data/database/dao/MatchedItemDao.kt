package com.zaheer.upireconcilepro.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.zaheer.upireconcilepro.data.database.entity.MatchedItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MatchedItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(items: List<MatchedItemEntity>)
    
    @Query("SELECT * FROM matched_items WHERE sessionId = :sessionId ORDER BY invoiceDate DESC")
    fun getAllForSession(sessionId: Long): Flow<List<MatchedItemEntity>>
    
    @Query("DELETE FROM matched_items WHERE sessionId = :sessionId")
    suspend fun deleteForSession(sessionId: Long)
}
