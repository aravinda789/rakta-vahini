package com.raktavahini.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raktavahini.data.model.BloodGroup
import com.raktavahini.data.model.Donation
import com.raktavahini.data.model.Donor
import com.raktavahini.data.local.NotificationHelper
import com.raktavahini.data.repository.DonorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class DonorProfileState(
    val donor: Donor? = null,
    val isEditing: Boolean = false,
    val name: String = "",
    val phone: String = "",
    val bloodGroup: BloodGroup? = null,
    val location: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val lastDonationDate: LocalDate? = null,
    val isEligible: Boolean = true,
    val donations: List<Donation> = emptyList(),
    val showThankYou: Boolean = false,
    val isSaving: Boolean = false,
    val locationFetched: Boolean = false
)

@HiltViewModel
class DonorProfileViewModel @Inject constructor(
    private val repository: DonorRepository,
    private val application: Application
) : ViewModel() {

    private val notificationHelper = NotificationHelper(application)

    private val _state = MutableStateFlow(DonorProfileState())
    val state: StateFlow<DonorProfileState> = _state.asStateFlow()

    fun loadDonor(donorId: Long) {
        viewModelScope.launch {
            val donor = repository.getDonorById(donorId)
            donor?.let {
                _state.value = _state.value.copy(
                    donor = it,
                    name = it.name,
                    phone = it.phone,
                    bloodGroup = BloodGroup.fromDisplayName(it.bloodGroup),
                    location = it.location,
                    latitude = it.latitude,
                    longitude = it.longitude,
                    lastDonationDate = it.lastDonationDate,
                    isEligible = it.isCurrentlyEligible(),
                    locationFetched = it.latitude != null && it.longitude != null
                )
                loadDonations(donorId)
            }
        }
    }

    private fun loadDonations(donorId: Long) {
        viewModelScope.launch {
            repository.getDonationsByDonor(donorId).collect { donations ->
                _state.value = _state.value.copy(donations = donations)
            }
        }
    }

    fun updateName(name: String) {
        _state.value = _state.value.copy(name = name)
    }

    fun updatePhone(phone: String) {
        _state.value = _state.value.copy(phone = phone)
    }

    fun updateBloodGroup(bloodGroup: BloodGroup) {
        _state.value = _state.value.copy(bloodGroup = bloodGroup)
    }

    fun updateLocation(location: String) {
        _state.value = _state.value.copy(location = location)
    }

    fun updateLocationCoordinates(latitude: Double, longitude: Double) {
        _state.value = _state.value.copy(
            latitude = latitude,
            longitude = longitude,
            locationFetched = true
        )
    }

    fun updateLastDonationDate(date: LocalDate?) {
        _state.value = _state.value.copy(
            lastDonationDate = date,
            isEligible = date?.let {
                java.time.temporal.ChronoUnit.DAYS.between(it, LocalDate.now()) >= 90
            } ?: true
        )
    }

    fun toggleEligibility() {
        _state.value = _state.value.copy(isEligible = !_state.value.isEligible)
    }

    fun saveDonor() {
        val state = _state.value
        if (state.name.isBlank() || state.phone.isBlank() || state.bloodGroup == null || state.location.isBlank()) {
            Toast.makeText(application, "Please fill all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        viewModelScope.launch {
            _state.value = state.copy(isSaving = true)
            val donor = Donor(
                id = state.donor?.id ?: 0,
                name = state.name,
                phone = state.phone,
                bloodGroup = state.bloodGroup.displayName,
                location = state.location,
                latitude = state.latitude,
                longitude = state.longitude,
                lastDonationDate = state.lastDonationDate,
                isEligible = state.isEligible,
                createdAt = state.donor?.createdAt ?: LocalDate.now()
            )

            if (state.donor == null) {
                val newId = repository.insertDonor(donor)
                Toast.makeText(application, "Donor registered successfully!", Toast.LENGTH_SHORT).show()
                _state.value = _state.value.copy(donor = donor.copy(id = newId), isSaving = false, isEditing = false)
            } else {
                repository.updateDonor(donor)
                Toast.makeText(application, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                _state.value = _state.value.copy(donor = donor, isSaving = false, isEditing = false)
            }
        }
    }

    fun logDonation(date: LocalDate, location: String) {
        val donor = _state.value.donor ?: return
        viewModelScope.launch {
            val donation = Donation(
                donorId = donor.id,
                donationDate = date,
                location = location
            )
            repository.logDonation(donation)
            notificationHelper.showThankYouNotification()
            _state.value = _state.value.copy(
                lastDonationDate = date,
                isEligible = false,
                showThankYou = true
            )
            loadDonations(donor.id)
        }
    }

    fun dismissThankYou() {
        _state.value = _state.value.copy(showThankYou = false)
    }

    fun callDonor(phoneNumber: String) {
        try {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$phoneNumber")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            application.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(application, "Unable to make call", Toast.LENGTH_SHORT).show()
        }
    }

    fun deleteDonation(donation: Donation) {
        viewModelScope.launch {
            repository.deleteDonation(donation)
        }
    }
}