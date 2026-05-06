package com.raktavahini.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.raktavahini.data.model.Donation
import com.raktavahini.data.model.Donor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

@Database(
    entities = [Donor::class, Donation::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class RaktaVahiniDatabase : RoomDatabase() {
    abstract fun donorDao(): DonorDao
    abstract fun donationDao(): DonationDao

    companion object {
        @Volatile
        private var INSTANCE: RaktaVahiniDatabase? = null

        fun getDatabase(context: Context): RaktaVahiniDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RaktaVahiniDatabase::class.java,
                    "rakta_vahini_database"
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateDatabase(database.donorDao())
                }
            }
        }

        suspend fun populateDatabase(donorDao: DonorDao) {
            val donors = listOf(
                Donor(
                    name = "Rajesh Kumar",
                    phone = "9876543210",
                    bloodGroup = "A+",
                    location = "Bangalore",
                    latitude = 12.9716,
                    longitude = 77.5946,
                    isEligible = true,
                    createdAt = LocalDate.now().minusMonths(4)
                ),
                Donor(
                    name = "Priya Sharma",
                    phone = "9876543211",
                    bloodGroup = "O+",
                    location = "Mysore",
                    latitude = 12.2958,
                    longitude = 76.6394,
                    isEligible = true,
                    createdAt = LocalDate.now().minusMonths(2)
                ),
                Donor(
                    name = "Arun Reddy",
                    phone = "9876543212",
                    bloodGroup = "B+",
                    location = "Hubli",
                    latitude = 15.3647,
                    longitude = 75.1240,
                    isEligible = true,
                    lastDonationDate = LocalDate.now().minusMonths(5),
                    createdAt = LocalDate.now().minusMonths(6)
                )
            )
            donors.forEach { donorDao.insertDonor(it) }
        }
    }
}