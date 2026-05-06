package com.raktavahini.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Looper
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.raktavahini.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.location.*
import com.raktavahini.data.model.BloodGroup
import com.raktavahini.data.model.SearchRadius
import com.raktavahini.ui.theme.*
import com.raktavahini.ui.viewmodel.DonorWithDistance
import com.raktavahini.ui.viewmodel.HomeViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onDonorClick: (Long) -> Unit,
    onLanguageChange: () -> Unit
) {
    val state by viewModel.searchState.collectAsState()
    val context = LocalContext.current

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineLocationGranted || coarseLocationGranted) {
            viewModel.setLocationPermissionGranted(true)
        } else {
            Toast.makeText(context, "Location permission needed for distance-based search", Toast.LENGTH_LONG).show()
        }
    }

    var fusedLocationClient by remember { mutableStateOf<FusedLocationProviderClient?>(null) }

    LaunchedEffect(Unit) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    }

    DisposableEffect(state.locationPermissionGranted) {
        if (state.locationPermissionGranted && fusedLocationClient != null) {
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(5000)
                .build()

            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    result.lastLocation?.let { location ->
                        viewModel.updateUserLocation(location.latitude, location.longitude)
                    }
                }
            }

            try {
                fusedLocationClient?.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                )
            } catch (e: SecurityException) {
                viewModel.setLocationPermissionGranted(false)
            }

            onDispose {
                fusedLocationClient?.removeLocationUpdates(locationCallback)
            }
        } else {
            onDispose { }
        }
    }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineMedium)
                        Text(stringResource(R.string.slogan), style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = Background,
                    titleContentColor = OnSurface
                ),
                actions = {
                        IconButton(
                            onClick = onLanguageChange,
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .background(GlassWhite, CircleShape)
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Primary)
                        }
                }
            )
        },
        containerColor = Background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            // Blood Group Selection Section
            Text(
                stringResource(R.string.select_blood_group),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(vertical = 12.dp)
            )
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(BloodGroup.ALL_BLOOD_GROUPS) { bloodGroup ->
                    PremiumBloodGroupChip(
                        bloodGroup = bloodGroup,
                        isSelected = state.selectedBloodGroup == bloodGroup,
                        onClick = { viewModel.selectBloodGroup(bloodGroup) }
                    )
                }
            }

            // Search Radius Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.search_radius), style = MaterialTheme.typography.titleLarge)
                if (!state.locationPermissionGranted) {
                    TextButton(onClick = {
                        locationPermissionLauncher.launch(
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                        )
                    }) {
                        Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.enable_gps))
                    }
                }
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 20.dp)
            ) {
                items(SearchRadius.entries) { radius ->
                    PremiumRadiusChip(
                        radius = radius,
                        isSelected = state.selectedRadius == radius,
                        onClick = { viewModel.selectRadius(radius) }
                    )
                }
            }

            // Location Search Input
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                placeholder = { Text(stringResource(R.string.filter_hint)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Primary) },
                trailingIcon = {
                    if (state.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = OnSurfaceVariant)
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    imeAction = androidx.compose.ui.text.input.ImeAction.Search
                ),
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                    onSearch = { viewModel.searchDonors() }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    containerColor = Surface,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Primary
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Results Section
            AnimatedContent(
                targetState = state,
                transitionSpec = {
                    fadeIn(animationSpec = tween(400)) togetherWith fadeOut(animationSpec = tween(400))
                },
                label = "results"
            ) { currentState ->
                when {
                    currentState.isLoading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Primary)
                        }
                    }
                    currentState.eligibleDonors.isEmpty() && currentState.searchQuery.isBlank() && currentState.selectedBloodGroup == null -> {
                        EmptyStatePlaceholder(
                            icon = Icons.Default.Bloodtype,
                            message = stringResource(R.string.select_bg_hint)
                        )
                    }
                    currentState.eligibleDonors.isEmpty() -> {
                        EmptyStatePlaceholder(
                            icon = Icons.Default.SearchOff,
                            message = stringResource(R.string.no_donors_area)
                        )
                    }
                    else -> {
                        Column {
                            Text(
                                stringResource(R.string.lifesavers_available, currentState.eligibleDonors.size),
                                style = MaterialTheme.typography.titleMedium,
                                color = AccentGreen,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Column(
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.padding(bottom = 32.dp)
                            ) {
                                currentState.eligibleDonors.forEachIndexed { index, donorWithDistance ->
                                    StaggeredDonorCard(
                                        index = index,
                                        donorWithDistance = donorWithDistance,
                                        onClick = { onDonorClick(donorWithDistance.donor.id) },
                                        onCallClick = {
                                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                                data = Uri.parse("tel:${donorWithDistance.donor.phone}")
                                            }
                                            context.startActivity(intent)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PremiumBloodGroupChip(
    bloodGroup: BloodGroup,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) Primary else Surface,
        animationSpec = tween(300), label = "color"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) OnPrimary else OnSurface,
        animationSpec = tween(300), label = "content"
    )

    Surface(
        onClick = onClick,
        modifier = Modifier
            .width(64.dp)
            .height(64.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = if (isSelected) 0.dp else 1.dp,
                color = GlassWhite,
                shape = RoundedCornerShape(16.dp)
            ),
        color = backgroundColor,
        tonalElevation = if (isSelected) 8.dp else 2.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                bloodGroup.displayName,
                style = MaterialTheme.typography.titleLarge,
                color = contentColor,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
fun PremiumRadiusChip(
    radius: SearchRadius,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) Primary.copy(alpha = 0.15f) else GlassWhite,
        modifier = Modifier.border(
            width = 1.dp,
            color = if (isSelected) Primary else Color.Transparent,
            shape = RoundedCornerShape(12.dp)
        )
    ) {
        Text(
            "${radius.km} ${stringResource(R.string.km)}",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            color = if (isSelected) Primary else OnSurfaceVariant
        )
    }
}

@Composable
fun StaggeredDonorCard(
    index: Int,
    donorWithDistance: DonorWithDistance,
    onClick: () -> Unit,
    onCallClick: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(index * 100L)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { 50 }) + fadeIn(),
        label = "donor_card"
    ) {
        val donor = donorWithDistance.donor
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Blood Group Avatar with Gradient
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(PrimaryGradient),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        donor.bloodGroup,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = OnPrimary
                    )
                }

                Spacer(modifier = Modifier.width(20.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        donor.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = OnSurfaceVariant
                        )
                        Text(
                            donor.location,
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnSurfaceVariant,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }

                    donorWithDistance.distanceKm?.let { distance ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(AccentGreen.copy(alpha = 0.1f), CircleShape)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.NearMe,
                                        contentDescription = null,
                                        modifier = Modifier.size(12.dp),
                                        tint = AccentGreen
                                    )
                                    Text(
                                        "${String.format("%.1f", distance)} ${stringResource(R.string.km)} ${stringResource(R.string.away)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = AccentGreen,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(start = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                FilledIconButton(
                    onClick = onCallClick,
                    modifier = Modifier.size(56.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = AccentGreen
                    )
                ) {
                    Icon(Icons.Default.Phone, contentDescription = "Call", tint = OnPrimary)
                }
            }
        }
    }
}

@Composable
fun EmptyStatePlaceholder(icon: androidx.compose.ui.graphics.vector.ImageVector, message: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = GlassWhite
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                message,
                style = MaterialTheme.typography.bodyLarge,
                color = OnSurfaceVariant
            )
        }
    }
}