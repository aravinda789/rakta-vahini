package com.raktavahini.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raktavahini.data.model.Donor
import com.raktavahini.data.repository.DonorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileState(
    val searchPhone: String = "",
    val foundDonor: Donor? = null,
    val allDonors: List<Donor> = emptyList(),
    val searched: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: DonorRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    init {
        loadAllDonors()
    }

    private fun loadAllDonors() {
        viewModelScope.launch {
            repository.getAllDonors().collect { donors ->
                _state.value = _state.value.copy(allDonors = donors)
            }
        }
    }

    fun updateSearchPhone(phone: String) {
        _state.value = _state.value.copy(searchPhone = phone)
    }

    fun searchByPhone() {
        val phone = _state.value.searchPhone
        if (phone.isBlank()) return

        viewModelScope.launch {
            val donors = _state.value.allDonors
            val found = donors.find { it.phone == phone }
            _state.value = _state.value.copy(
                foundDonor = found,
                searched = true
            )
        }
    }
}