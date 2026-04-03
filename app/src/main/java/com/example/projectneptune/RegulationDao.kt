package com.example.projectneptune

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RegulationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRegulations(regs: List<Regulation>)

    @Query("SELECT * FROM regulations")
    suspend fun getAllRegulations(): List<Regulation>

    @Query("DELETE FROM regulations")
    suspend fun deleteAll()
}