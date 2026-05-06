package com.raktavahini.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raktavahini.data.model.BloodGroup
import com.raktavahini.data.model.Donor
import com.raktavahini.data.model.LocationUtils
import com.raktavahini.data.model.SearchRadius
import com.raktavahini.data.model.UserLocation
import com.raktavahini.data.repository.DonorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchState(
    val selectedBloodGroup: BloodGroup? = null,
    val selectedRadius: SearchRadius = SearchRadius.TWENTY_KM,
    val searchQuery: String = "",
    val userLocation: UserLocation? = null,
    val eligibleDonors: List<DonorWithDistance> = emptyList(),
    val isLoading: Boolean = false,
    val locationPermissionGranted: Boolean = false
)

data class DonorWithDistance(
    val donor: Donor,
    val distanceKm: Double?
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: DonorRepository
) : ViewModel() {

    private val _searchState = MutableStateFlow(SearchState())
    val searchState: StateFlow<SearchState> = _searchState.asStateFlow()

    init {
        searchDonors()
    }

    fun setLocationPermissionGranted(granted: Boolean) {
        _searchState.value = _searchState.value.copy(locationPermissionGranted = granted)
    }

    fun updateUserLocation(latitude: Double, longitude: Double) {
        _searchState.value = _searchState.value.copy(
            userLocation = UserLocation(latitude, longitude)
        )
        searchDonors()
    }

    fun selectBloodGroup(bloodGroup: BloodGroup) {
        _searchState.value = _searchState.value.copy(selectedBloodGroup = bloodGroup)
        searchDonors()
    }

    fun selectRadius(radius: SearchRadius) {
        _searchState.value = _searchState.value.copy(selectedRadius = radius)
        searchDonors()
    }

    fun updateSearchQuery(query: String) {
        _searchState.value = _searchState.value.copy(searchQuery = query)
        searchDonors()
    }

    private var searchJob: kotlinx.coroutines.Job? = null

    fun searchDonors() {
        val state = _searchState.value

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _searchState.value = _searchState.value.copy(isLoading = true)

            val donorsFlow = if (state.selectedBloodGroup != null) {
                repository.getEligibleDonorsByBloodGroup(state.selectedBloodGroup.displayName)
            } else {
                repository.getAllDonors()
            }

            donorsFlow.collect { donors ->
                    val currentState = _searchState.value
                    val donorsWithDistance = donors
                        .filter { donor ->
                            val match = if (currentState.searchQuery.isNotBlank()) {
                                donor.location.contains(currentState.searchQuery, ignoreCase = true) ||
                                        donor.name.contains(currentState.searchQuery, ignoreCase = true)
                            } else {
                                true
                            }

                            val distanceMatch = if (currentState.userLocation != null &&
                                donor.latitude != null && donor.longitude != null) {
                                val distance = LocationUtils.calculateDistance(
                                    currentState.userLocation.latitude,
                                    currentState.userLocation.longitude,
                                    donor.latitude,
                                    donor.longitude
                                )
                                distance <= currentState.selectedRadius.km
                            } else {
                                true
                            }

                            match && distanceMatch
                        }
                        .map { donor ->
                            val distance = if (currentState.userLocation != null &&
                                donor.latitude != null && donor.longitude != null) {
                                LocationUtils.calculateDistance(
                                    currentState.userLocation.latitude,
                                    currentState.userLocation.longitude,
                                    donor.latitude,
                                    donor.longitude
                                )
                            } else null

                            DonorWithDistance(donor, distance)
                        }
                        .sortedBy { it.distanceKm }

                    _searchState.value = _searchState.value.copy(
                        eligibleDonors = donorsWithDistance,
                        isLoading = false
                    )
                }
        }
    }

    fun clearSearch() {
        _searchState.value = SearchState()
    }
}