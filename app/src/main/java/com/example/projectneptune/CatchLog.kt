package com.example.projectneptune

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "catch_logs")
data class CatchLog(
    @PrimaryKey(autoGenerate = true) val logId: Int = 0,
    val species: String,
    val quantity: Int?,
    val latitude: Double?,
    val longitude: Double?,
    val timeDate: String,
    val scientificName: String? = "Unknown",
    val description: String? = null,
    val habitat: String? = null,
    val isProtected: Boolean = false
)