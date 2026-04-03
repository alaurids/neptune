package com.example.projectneptune

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "regulations")
data class Regulation(
    @PrimaryKey(autoGenerate = true) val regId: Int = 0,
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