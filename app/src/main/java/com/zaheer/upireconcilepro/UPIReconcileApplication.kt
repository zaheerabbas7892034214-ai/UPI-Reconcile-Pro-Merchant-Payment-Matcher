package com.zaheer.upireconcilepro

import android.app.Application
import androidx.room.Room
import com.zaheer.upireconcilepro.data.AppDatabase

class UPIReconcileApplication : Application() {
    
    companion object {
        private const val DATABASE_NAME = "upi_reconcile_db"
        lateinit var database: AppDatabase
            private set
    }
    
    override fun onCreate() {
        super.onCreate()
        initializeDatabase()
    }
    
    private fun initializeDatabase() {
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }
}
