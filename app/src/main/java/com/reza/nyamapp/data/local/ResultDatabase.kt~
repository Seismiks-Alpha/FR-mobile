package com.reza.nyamapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ScanResult::class], version = 2, exportSchema = false)
abstract class ResultDatabase : RoomDatabase() {

    abstract fun ResultDao(): ResultDao

    companion object {
        @Volatile
        private var INSTANCE: ResultDatabase? = null

        fun getInstance(context: Context): ResultDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ResultDatabase::class.java,
                    "result_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}