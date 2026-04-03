package com.example.projectneptune

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CatchLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogs(logs: List<CatchLog>): LongArray

    @Query("SELECT * FROM catch_logs")
    suspend fun getAllLogs(): List<CatchLog>

    @Query("DELETE FROM catch_logs")
    suspend fun deleteAllLogs()
}