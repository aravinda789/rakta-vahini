package com.raktavahini.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Entity(tableName = "donors")
data class Donor(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phone: String,
    val bloodGroup: String,
    val location: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val lastDonationDate: LocalDate? = null,
    val isEligible: Boolean = true,
    val createdAt: LocalDate = LocalDate.now()
) {
    fun isCurrentlyEligible(): Boolean {
        if (lastDonationDate == null) return true
        val daysSinceDonation = ChronoUnit.DAYS.between(lastDonationDate, LocalDate.now())
        return daysSinceDonation >= 90
    }

    fun getDaysSinceLastDonation(): Long? {
        if (lastDonationDate == null) return null
        return ChronoUnit.DAYS.between(lastDonationDate, LocalDate.now())
    }

    fun getDaysUntilEligible(): Long? {
        if (lastDonationDate == null) return null
        val daysSince = getDaysSinceLastDonation() ?: return null
        return if (daysSince >= 90) 0 else 90 - daysSince
    }
}