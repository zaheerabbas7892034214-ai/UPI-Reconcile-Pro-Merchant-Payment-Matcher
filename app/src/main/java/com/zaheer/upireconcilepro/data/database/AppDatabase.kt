package com.zaheer.upireconcilepro.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.zaheer.upireconcilepro.data.database.dao.EntitlementDao
import com.zaheer.upireconcilepro.data.database.dao.MatchedItemDao
import com.zaheer.upireconcilepro.data.database.dao.ReconciliationSessionDao
import com.zaheer.upireconcilepro.data.database.dao.UnmatchedItemDao
import com.zaheer.upireconcilepro.data.database.entity.EntitlementEntity
import com.zaheer.upireconcilepro.data.database.entity.MatchedItemEntity
import com.zaheer.upireconcilepro.data.database.entity.ReconciliationSessionEntity
import com.zaheer.upireconcilepro.data.database.entity.UnmatchedItemEntity

@Database(
    entities = [
        ReconciliationSessionEntity::class,
        MatchedItemEntity::class,
        UnmatchedItemEntity::class,
        EntitlementEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun reconciliationSessionDao(): ReconciliationSessionDao
    abstract fun matchedItemDao(): MatchedItemDao
    abstract fun unmatchedItemDao(): UnmatchedItemDao
    abstract fun entitlementDao(): EntitlementDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "upi_reconcile_pro_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
