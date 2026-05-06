package com.raktavahini.ui.screens

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raktavahini.data.model.BloodGroup
import com.raktavahini.ui.theme.*
import com.raktavahini.ui.components.PremiumInputField
import com.raktavahini.ui.viewmodel.DonorProfileViewModel
import java.time.ZoneId
import com.raktavahini.data.LanguageManager

import androidx.compose.ui.res.stringResource
import com.raktavahini.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterDonorScreen(
    viewModel: DonorProfileViewModel,
    onBack: () -> Unit,
    onRegistered: (Long) -> Unit,
    context: Context
) {
    val state by viewModel.state.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }
    var bloodGroupExpanded by remember { mutableStateOf(false) }
    val languageManager = remember { LanguageManager(context) }

    LaunchedEffect(state.donor) {
        state.donor?.let { donor ->
            onRegistered(donor.id)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.register_donor), style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Background,
                    titleContentColor = OnSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                stringResource(R.string.register_as_donor),
                style = MaterialTheme.typography.headlineLarge,
                color = Primary
            )
            Text(
                stringResource(R.string.register_as_donor) + " " + if (languageManager.getSelectedLanguage() == "kn") "ತುರ್ತು ಸಮಯದಲ್ಲಿ ಜ���ವ ಉಳಿಸಬಹುದು" else "and can save lives in emergencies",
                style = MaterialTheme.typography.bodyLarge,
                color = OnSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
            )

            PremiumInputField(
                value = state.name,
                onValueChange = { name -> viewModel.updateName(name) },
                label = stringResource(R.string.name),
                icon = Icons.Default.Person
            )

            Spacer(modifier = Modifier.height(20.dp))

            PremiumInputField(
                value = state.phone,
                onValueChange = { phone -> viewModel.updatePhone(phone) },
                label = stringResource(R.string.phone),
                icon = Icons.Default.Phone
            )

            Spacer(modifier = Modifier.height(20.dp))

            Box(modifier = Modifier.fillMaxWidth()) {
                ExposedDropdownMenuBox(
                    expanded = bloodGroupExpanded,
                    onExpandedChange = { bloodGroupExpanded = it }
                ) {
                    PremiumInputField(
                        value = state.bloodGroup?.displayName ?: "",
                        onValueChange = { _ -> },
                        label = stringResource(R.string.select_blood_group),
                        icon = Icons.Default.Bloodtype,
                        readOnly = true,
                        modifier = Modifier.menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = bloodGroupExpanded,
                        onDismissRequest = { bloodGroupExpanded = false },
                        modifier = Modifier.background(Surface)
                    ) {
                        BloodGroup.ALL_BLOOD_GROUPS.forEach { bg ->
                            DropdownMenuItem(
                                text = { Text(bg.displayName, style = MaterialTheme.typography.bodyLarge) },
                                onClick = {
                                    viewModel.updateBloodGroup(bg)
                                    bloodGroupExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            PremiumInputField(
                value = state.location,
                onValueChange = { loc -> viewModel.updateLocation(loc) },
                label = stringResource(R.string.location),
                icon = Icons.Default.LocationOn
            )

            Spacer(modifier = Modifier.height(20.dp))

            PremiumInputField(
                value = state.lastDonationDate?.toString() ?: stringResource(R.string.never),
                onValueChange = { _ -> },
                label = stringResource(R.string.last_donation_date),
                icon = Icons.Default.Event,
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.CalendarMonth, tint = Primary, contentDescription = null)
                    }
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = if (state.isEligible) AccentGreen.copy(alpha = 0.1f) else SurfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.i_am_eligible), style = MaterialTheme.typography.titleMedium)
                        Text(
                            if (state.isEligible) stringResource(R.string.visible_in_searches) else stringResource(R.string.hidden_from_searches),
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant
                        )
                    }
                    Switch(
                        checked = state.isEligible,
                        onCheckedChange = { viewModel.toggleEligibility() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = OnPrimary,
                            checkedTrackColor = AccentGreen
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = { viewModel.saveDonor() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .shadow(12.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                enabled = !state.isSaving
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(color = OnPrimary, modifier = Modifier.size(24.dp))
                } else {
                    Icon(Icons.Default.Favorite, contentDescription = null)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(stringResource(R.string.save), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(64.dp))
        }

        if (showDatePicker) {
            val datePickerState = rememberDatePickerState()
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val date = java.time.Instant.ofEpochMilli(millis)
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDate()
                            viewModel.updateLastDonationDate(date)
                        }
                        showDatePicker = false
                    }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}