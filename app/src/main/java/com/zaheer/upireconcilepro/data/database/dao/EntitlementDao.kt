package com.zaheer.upireconcilepro.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.zaheer.upireconcilepro.data.database.entity.EntitlementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EntitlementDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entitlement: EntitlementEntity)
    
    @Update
    suspend fun update(entitlement: EntitlementEntity)
    
    @Query("SELECT * FROM entitlement WHERE id = 1")
    fun get(): Flow<EntitlementEntity?>
    
    @Query("SELECT * FROM entitlement WHERE id = 1")
    suspend fun getOnce(): EntitlementEntity?
}
