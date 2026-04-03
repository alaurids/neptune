package com.example.projectneptune

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// 1. Add Regulation::class to entities
// 2. Increment version to 2 (or higher) to trigger a schema update
@Database(entities = [CatchLog::class, Regulation::class], version = 2)
abstract class AppDatabase : RoomDatabase() {

    abstract fun catchLogDao(): CatchLogDao


    abstract fun regulationDao(): RegulationDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                val newInstance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "shellfish_db"
                )
                    // 4. Add this to allow the database to reset when you change the schema
                    .fallbackToDestructiveMigration()
                    .build()
                instance = newInstance
                newInstance
            }
        }
    }
}