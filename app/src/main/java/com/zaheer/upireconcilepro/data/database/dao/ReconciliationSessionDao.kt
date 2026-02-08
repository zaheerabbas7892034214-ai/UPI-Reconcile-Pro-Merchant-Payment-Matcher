package com.zaheer.upireconcilepro.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.zaheer.upireconcilepro.data.database.entity.ReconciliationSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReconciliationSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: ReconciliationSessionEntity): Long
    
    @Query("SELECT * FROM reconciliation_sessions ORDER BY date DESC")
    fun getAll(): Flow<List<ReconciliationSessionEntity>>
    
    @Query("SELECT * FROM reconciliation_sessions WHERE id = :id")
    suspend fun getById(id: Long): ReconciliationSessionEntity?
    
    @Query("DELETE FROM reconciliation_sessions WHERE id = :id")
    suspend fun delete(id: Long)
}
