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
            // Checks to see if database exist or not
            return INSTANCE ?: synchronized(this) {
                //constructer, physically creates on phone
                val instance = Room.databaseBuilder(
                    //database connection stays live as long as the app is running
                    context.applicationContext,
                    ShellfishDatabase::class.java,
                    //important, this is the name of the DB room will create on the clients phone "shellfish_database.db"
                    "shellfish_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
