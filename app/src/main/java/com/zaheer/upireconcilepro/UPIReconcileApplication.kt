package com.zaheer.upireconcilepro

import android.app.Application
import com.zaheer.upireconcilepro.data.database.AppDatabase

class UPIReconcileApplication : Application() {
    
    companion object {
        @Volatile
        lateinit var database: AppDatabase
            private set
    }
    
    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getInstance(applicationContext)
    }
}
