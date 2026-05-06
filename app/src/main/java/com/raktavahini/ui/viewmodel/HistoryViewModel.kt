package com.raktavahini.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raktavahini.data.model.Donation
import com.raktavahini.data.model.Donor
import com.raktavahini.data.repository.DonorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistoryState(
    val allDonations: List<Donation> = emptyList(),
    val donorMap: Map<Long, Donor> = emptyMap()
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: DonorRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HistoryState())
    val state: StateFlow<HistoryState> = _state.asStateFlow()

    init {
        loadAllDonations()
    }

    private fun loadAllDonations() {
        viewModelScope.launch {
            repository.getAllDonations().collect { donations ->
                val donorIds = donations.map { it.donorId }.distinct()
                val donorMap = mutableMapOf<Long, Donor>()

                donorIds.forEach { id ->
                    repository.getDonorById(id)?.let { donor ->
                        donorMap[id] = donor
                    }
                }

                _state.value = _state.value.copy(
                    allDonations = donations,
                    donorMap = donorMap
                )
            }
        }
    }
}