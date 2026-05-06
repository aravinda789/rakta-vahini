package com.raktavahini.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raktavahini.data.model.Donor
import com.raktavahini.ui.theme.*
import com.raktavahini.ui.components.PremiumInputField
import com.raktavahini.ui.viewmodel.DonorProfileViewModel
import java.time.LocalDate
import java.time.Instant
import java.time.ZoneId
import androidx.compose.ui.res.stringResource
import com.raktavahini.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonorProfileScreen(
    viewModel: DonorProfileViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var showAddDonationDialog by remember { mutableStateOf(false) }

    if (state.showThankYou) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissThankYou() },
            icon = { Icon(Icons.Default.Favorite, contentDescription = null, tint = AccentGreen, modifier = Modifier.size(64.dp)) },
            title = { Text(stringResource(R.string.thank_you_hero)) },
            text = { Text(stringResource(R.string.donation_recorded)) },
            confirmButton = {
                Button(
                    onClick = { viewModel.dismissThankYou() },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text(stringResource(R.string.proud_to_help))
                }
            },
            shape = RoundedCornerShape(28.dp),
            containerColor = Surface
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.donor_identity), style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Background,
                    titleContentColor = OnSurface,
                    navigationIconContentColor = OnSurface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDonationDialog = true },
                containerColor = Primary,
                contentColor = OnPrimary,
                shape = RoundedCornerShape(16.dp),
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.log_donation)) }
            )
        }
    ) { paddingValues ->
        state.donor?.let { donor ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                item {
                    PremiumProfileHeader(donor = donor)
                }

                item {
                    PremiumEligibilityCard(donor = donor)
                }

                item {
                    PremiumContactCard(donor = donor, onCallClick = { viewModel.callDonor(donor.phone) })
                }

                item {
                    PremiumDonationHistoryCard(donations = state.donations)
                }
            }
        } ?: run {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }
        }

        if (showAddDonationDialog) {
            AddDonationDialog(
                onDismiss = { showAddDonationDialog = false },
                onConfirm = { date, location ->
                    viewModel.logDonation(date, location)
                    showAddDonationDialog = false
                }
            )
        }
    }
}

@Composable
fun PremiumProfileHeader(donor: Donor) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(32.dp))
            .background(SurfaceGradient, RoundedCornerShape(32.dp))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.Center) {
                // Outer glow
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(Primary.copy(alpha = 0.1f), CircleShape)
                )
                // Main Avatar
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(PrimaryGradient),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        donor.bloodGroup,
                        style = MaterialTheme.typography.headlineLarge,
                        color = OnPrimary,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                donor.name,
                style = MaterialTheme.typography.headlineMedium,
                color = OnSurface
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = OnSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    donor.location,
                    style = MaterialTheme.typography.bodyLarge,
                    color = OnSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun PremiumEligibilityCard(donor: Donor) {
    val isEligible = donor.isCurrentlyEligible()
    val daysSince = donor.getDaysSinceLastDonation()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isEligible) AccentGreen.copy(alpha = 0.1f) else AccentRed.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(if (isEligible) AccentGreen else AccentRed, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (isEligible) Icons.Default.Check else Icons.Default.Block,
                    contentDescription = null,
                    tint = OnPrimary
                )
            }
            
            Spacer(modifier = Modifier.width(20.dp))
            
            Column {
                Text(
                    if (isEligible) stringResource(R.string.ready_to_save) else stringResource(R.string.cooldown_period),
                    style = MaterialTheme.typography.titleLarge,
                    color = if (isEligible) AccentGreen else AccentRed
                )
                
                Text(
                    when {
                        !isEligible && daysSince != null -> stringResource(R.string.eligible_again_in, donor.getDaysUntilEligible() ?: 0)
                        isEligible && daysSince != null -> stringResource(R.string.last_donation_days_ago, daysSince)
                        else -> stringResource(R.string.no_donation_records)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun PremiumContactCard(donor: Donor, onCallClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.PhoneInTalk,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(20.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.primary_contact), style = MaterialTheme.typography.labelLarge, color = OnSurfaceVariant)
                Text(donor.phone, style = MaterialTheme.typography.titleMedium)
            }
            Button(
                onClick = onCallClick,
                colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.call_text))
            }
        }
    }
}

@Composable
fun PremiumDonationHistoryCard(donations: List<com.raktavahini.data.model.Donation>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            stringResource(R.string.donation_timeline),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
        )

        if (donations.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(Surface, RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.become_hero), color = OnSurfaceVariant)
            }
        } else {
            donations.forEachIndexed { index, donation ->
                DonationHistoryItem(donation = donation, isLast = index == donations.size - 1)
            }
        }
    }
}

@Composable
fun DonationHistoryItem(donation: com.raktavahini.data.model.Donation, isLast: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = if (isLast) 0.dp else 16.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(Primary, CircleShape)
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(60.dp)
                        .background(Primary.copy(alpha = 0.2f))
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    donation.donationDate.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    donation.location,
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDonationDialog(
    onDismiss: () -> Unit,
    onConfirm: (LocalDate, String) -> Unit
) {
    var donationDate by remember { mutableStateOf(LocalDate.now()) }
    var location by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.log_new_donation), style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(modifier = Modifier.padding(top = 16.dp)) {
                PremiumInputField(
                    value = donationDate.toString(),
                    onValueChange = { _ -> },
                    label = stringResource(R.string.donation_date),
                    icon = Icons.Default.CalendarToday,
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.Edit, tint = Primary, contentDescription = null)
                        }
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
                PremiumInputField(
                    value = location,
                    onValueChange = { newLocation -> location = newLocation },
                    label = stringResource(R.string.hospital_location),
                    icon = Icons.Default.LocalHospital
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(donationDate, location) },
                enabled = location.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text(stringResource(R.string.save_donation))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = Surface
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        donationDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                    }
                    showDatePicker = false
                }) {
                    Text(stringResource(R.string.ok))
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