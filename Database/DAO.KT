import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ShellfishDao {
    // Adds a new scan result to the history
    @Insert
    suspend fun insertScan(scan: Scan)

    // Streams the full history to the UI, sorted by newest first
    @Query("SELECT * FROM scans ORDER BY timestamp DESC")
    fun getAllScans(): Flow<List<Scan>>

    // Fetches encyclopedia info for a specific species ID
    @Query("SELECT * FROM species_library WHERE speciesId = :id")
    suspend fun getSpeciesById(id: Int): Species?

}
