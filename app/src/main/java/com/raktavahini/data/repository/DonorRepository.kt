package com.raktavahini.data.repository

import com.raktavahini.data.local.DonationDao
import com.raktavahini.data.local.DonorDao
import com.raktavahini.data.model.Donation
import com.raktavahini.data.model.Donor
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DonorRepository @Inject constructor(
    private val donorDao: DonorDao,
    private val donationDao: DonationDao
) {
    fun getAllDonors(): Flow<List<Donor>> = donorDao.getAllDonors()

    suspend fun getDonorById(id: Long): Donor? = donorDao.getDonorById(id)

    fun getDonorsByBloodGroup(bloodGroup: String): Flow<List<Donor>> =
        donorDao.getDonorsByBloodGroup(bloodGroup)

    fun getEligibleDonorsByBloodGroup(bloodGroup: String): Flow<List<Donor>> =
        donorDao.getEligibleDonorsByBloodGroup(bloodGroup)

    fun getDonorsByLocation(location: String): Flow<List<Donor>> =
        donorDao.getDonorsByLocation(location)

    suspend fun insertDonor(donor: Donor): Long = donorDao.insertDonor(donor)

    suspend fun updateDonor(donor: Donor) {
        val eligible = donor.isCurrentlyEligible()
        donorDao.updateDonor(donor.copy(isEligible = eligible))
    }

    suspend fun deleteDonor(donor: Donor) = donorDao.deleteDonor(donor)

    suspend fun deleteDonorById(id: Long) = donorDao.deleteDonorById(id)

    fun getDonationsByDonor(donorId: Long): Flow<List<Donation>> =
        donationDao.getDonationsByDonor(donorId)

    fun getAllDonations(): Flow<List<Donation>> = donationDao.getAllDonations()

    suspend fun logDonation(donation: Donation): Long {
        val result = donationDao.insertDonation(donation)
        val donor = donorDao.getDonorById(donation.donorId)
        donor?.let {
            val eligible = it.isCurrentlyEligible()
            donorDao.updateDonor(it.copy(lastDonationDate = donation.donationDate, isEligible = eligible))
        }
        return result
    }

    suspend fun deleteDonation(donation: Donation) = donationDao.deleteDonation(donation)

    suspend fun getDonorCount(): Int = donorDao.getDonorCount()

    suspend fun getLastDonationByDonor(donorId: Long): Donation? =
        donationDao.getLastDonationByDonor(donorId)
}