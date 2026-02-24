import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Index

// Reference library for the shellfish species
@Entity(tableName = "species_library")
data class Species(
    @PrimaryKey val speciesId: Int, // Matches index from AI model unique ID per species 
    val commonName: String,
    val scientificName: String,
    val description: String,
    val habitat: String,
    val isProtected: Boolean
)

// History of all scans performed on this device
@Entity(
    tableName = "scans",
    foreignKeys = [
        ForeignKey(
            entity = Species::class,
            parentColumns = ["speciesId"],
            childColumns = ["detectedSpeciesId"]
        )
    ],
    indices = [Index("detectedSpeciesId")] //Index databse for specific species ID, for performance 
)
//class for photos 
data class Scan(
    @PrimaryKey(autoGenerate = true) val scanId: Int = 0, //creates unique ID for every photo taken 
    val detectedSpeciesId: Int,
    val imagePath: String, 
    val aiConfidence: Float,
    val timestamp: Long = System.currentTimeMillis() //stores the exact time/date photo was taken
)
