package com.raktavahini.data.local

import androidx.room.*
import com.raktavahini.data.model.Donor
import kotlinx.coroutines.flow.Flow

@Dao
interface DonorDao {
    @Query("SELECT * FROM donors ORDER BY name ASC")
    fun getAllDonors(): Flow<List<Donor>>

    @Query("SELECT * FROM donors WHERE id = :id")
    suspend fun getDonorById(id: Long): Donor?

    @Query("SELECT * FROM donors WHERE bloodGroup = :bloodGroup")
    fun getDonorsByBloodGroup(bloodGroup: String): Flow<List<Donor>>

    @Query("SELECT * FROM donors WHERE location LIKE '%' || :location || '%'")
    fun getDonorsByLocation(location: String): Flow<List<Donor>>

    @Query("SELECT * FROM donors WHERE bloodGroup = :bloodGroup AND isEligible = 1")
    fun getEligibleDonorsByBloodGroup(bloodGroup: String): Flow<List<Donor>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDonor(donor: Donor): Long

    @Update
    suspend fun updateDonor(donor: Donor)

    @Delete
    suspend fun deleteDonor(donor: Donor)

    @Query("DELETE FROM donors WHERE id = :id")
    suspend fun deleteDonorById(id: Long)

    @Query("SELECT COUNT(*) FROM donors")
    suspend fun getDonorCount(): Int
}