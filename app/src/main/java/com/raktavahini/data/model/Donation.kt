package com.raktavahini.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "donations")
data class Donation(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val donorId: Long,
    val donationDate: LocalDate,
    val location: String,
    val notes: String = ""
)