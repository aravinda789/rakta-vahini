package com.raktavahini.data.local

import androidx.room.*
import com.raktavahini.data.model.Donation
import kotlinx.coroutines.flow.Flow

@Dao
interface DonationDao {
    @Query("SELECT * FROM donations WHERE donorId = :donorId ORDER BY donationDate DESC")
    fun getDonationsByDonor(donorId: Long): Flow<List<Donation>>

    @Query("SELECT * FROM donations ORDER BY donationDate DESC")
    fun getAllDonations(): Flow<List<Donation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDonation(donation: Donation): Long

    @Delete
    suspend fun deleteDonation(donation: Donation)

    @Query("DELETE FROM donations WHERE id = :id")
    suspend fun deleteDonationById(id: Long)

    @Query("SELECT * FROM donations WHERE donorId = :donorId ORDER BY donationDate DESC LIMIT 1")
    suspend fun getLastDonationByDonor(donorId: Long): Donation?
}