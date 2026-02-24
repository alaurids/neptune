import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

//Tells Room library how to build SQLite Database, creates two tables Species and Scan
@Database(entities = [Species::class, Scan::class], version = 1, exportSchema = false)
abstract class ShellfishDatabase : RoomDatabase() {
    abstract fun shellfishDao(): ShellfishDao

    companion object {
        @Volatile
        private var INSTANCE: ShellfishDatabase? = null

        fun getDatabase(context: Context): ShellfishDatabase {
            // If the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ShellfishDatabase::class.java,
                    "shellfish_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
