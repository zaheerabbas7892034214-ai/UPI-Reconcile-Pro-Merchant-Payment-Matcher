package com.zaheer.upireconcilepro.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.zaheer.upireconcilepro.data.database.entity.UnmatchedItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UnmatchedItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(items: List<UnmatchedItemEntity>)
    
    @Query("SELECT * FROM unmatched_items WHERE sessionId = :sessionId ORDER BY date DESC")
    fun getAllForSession(sessionId: Long): Flow<List<UnmatchedItemEntity>>
    
    @Query("SELECT * FROM unmatched_items WHERE sessionId = :sessionId AND type = :type ORDER BY date DESC")
    fun getByType(sessionId: Long, type: String): Flow<List<UnmatchedItemEntity>>
    
    @Query("DELETE FROM unmatched_items WHERE sessionId = :sessionId")
    suspend fun deleteForSession(sessionId: Long)
}
