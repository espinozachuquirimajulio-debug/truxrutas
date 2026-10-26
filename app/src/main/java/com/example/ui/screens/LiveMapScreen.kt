package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.IncidentEntity
import com.example.data.local.SavedTransitItemEntity
import com.example.model.BusRoute
import com.example.model.BusStop
import com.example.model.GeoPoint
import com.example.model.IncidentCategory
import com.example.model.LiveBusUnit
import com.example.model.StopEtaArrival
import com.example.ui.components.TransitMapCanvas

@Composable
fun LiveMapScreen(
    routes: List<BusRoute>,
    focusedRouteId: String?,
    stops: List<BusStop>,
    liveUnits: List<LiveBusUnit>,
    incidents: List<IncidentEntity>,
    savedItems: List<SavedTransitItemEntity>,
    userLocation: GeoPoint,
    selectedUnit: LiveBusUnit?,
    selectedStop: BusStop?,
    selectedIncident: IncidentEntity?,
    simulationSpeed: Int,
    computeStopArrivals: (BusStop) -> List<StopEtaArrival>,
    onSelectFocusedRoute: (String?) -> Unit,
    onSelectUnit: (LiveBusUnit?) -> Unit,
    onSelectStop: (BusStop?) -> Unit,
    onSelectIncident: (IncidentEntity?) -> Unit,
    onToggleStopAlert: (String) -> Unit,
    onToggleSimulationSpeed: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showUnitsLayer by remember { mutableStateOf(true) }
    var showStopsLayer by remember { mutableStateOf(true) }
    var showIncidentsLayer by remember { mutableStateOf(true) }

    var zoomLevel by remember { mutableFloatStateOf(1.25f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }

    val alertStopIds = remember(savedItems) {
        savedItems.filter { it.itemType == "STOP" && it.hasProximityAlert }.map { it.itemId }.toSet()
    }

    val visibleUnits = remember(liveUnits, focusedRouteId) {
        if (focusedRouteId != null) liveUnits.filter { it.routeId == focusedRouteId } else liveUnits
    }

    Box(modifier = modifier.fillMaxSize()) {
        // 1. Interactive Vector Map Canvas of Trujillo
        TransitMapCanvas(
            routes = routes,
            focusedRouteId = focusedRouteId,
            stops = stops,
            liveUnits = liveUnits,
            incidents = incidents,
            userLocation = userLocation,
            alertStopIds = alertStopIds,
            selectedUnit = selectedUnit,
            selectedStop = selectedStop,
            selectedIncident = selectedIncident,
            showUnitsLayer = showUnitsLayer,
            showStopsLayer = showStopsLayer,
            showIncidentsLayer = showIncidentsLayer,
            zoomLevel = zoomLevel,
            panOffset = panOffset,
            onTransformChange = { newZoom, newPan ->
                zoomLevel = newZoom
                panOffset = newPan
            },
            onSelectUnit = onSelectUnit,
            onSelectStop = onSelectStop,
            onSelectIncident = onSelectIncident,
            modifier = Modifier.fillMaxSize()
        )

        // 2. Top Overlay: Route Filter Selector & Map Layer Toggles
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Route Strip
            LazyRow(
                contentPadding = PaddingValues(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                item {
                    FilterChip(
                        selected = focusedRouteId == null,
                        onClick = { onSelectFocusedRoute(null) },
                        label = { Text("Todas las rutas") },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.testTag("map_filter_all_routes")
                    )
                }
                items(routes, key = { it.id }) { route ->
                    val isSelected = focusedRouteId == route.id
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectFocusedRoute(route.id) },
                        label = {
                            Text(
                                text = "${route.code} · ${route.shortName}",
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(Color(route.primaryColorHex))
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                            selectedContainerColor = Color(route.primaryColorHex),
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("map_route_chip_${route.id}")
                    )
                }
            }

            // Layer Toggles Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = showUnitsLayer,
                    onClick = { showUnitsLayer = !showUnitsLayer },
                    label = { Text("Micros (${visibleUnits.size})") },
                    leadingIcon = {
                        Icon(Icons.Default.DirectionsBus, contentDescription = null, modifier = Modifier.size(15.dp))
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
                    )
                )
                FilterChip(
                    selected = showStopsLayer,
                    onClick = { showStopsLayer = !showStopsLayer },
                    label = { Text("Paraderos (${stops.size})") },
                    leadingIcon = {
                        Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(15.dp))
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
                    )
                )
                FilterChip(
                    selected = showIncidentsLayer,
                    onClick = { showIncidentsLayer = !showIncidentsLayer },
                    label = { Text("Alertas (${incidents.count { it.status == "ACTIVO" }})") },
                    leadingIcon = {
                        Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(15.dp))
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
                    )
                )
            }
        }

        // 3. Right Side Map Controls (Zoom In, Zoom Out, Recenter, Telemetry Speed)
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.End
        ) {
            SmallFloatingActionButton(
                onClick = { zoomLevel = (zoomLevel * 1.25f).coerceAtMost(4.5f) },
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.testTag("map_zoom_in_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Acercar mapa")
            }
            SmallFloatingActionButton(
                onClick = { zoomLevel = (zoomLevel / 1.25f).coerceAtLeast(0.85f) },
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.testTag("map_zoom_out_btn")
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Alejar mapa")
            }
            SmallFloatingActionButton(
                onClick = {
                    zoomLevel = 1.25f
                    panOffset = Offset.Zero
                },
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.testTag("map_recenter_btn")
            ) {
                Icon(Icons.Default.CenterFocusStrong, contentDescription = "Centrar en Trujillo")
            }
            FloatingActionButton(
                onClick = onToggleSimulationSpeed,
                containerColor = if (simulationSpeed > 1) Color(0xFFFFB300) else MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("map_speed_toggle_btn")
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.FastForward,
                        contentDescription = "Velocidad de simulación",
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${simulationSpeed}x",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 4. Bottom Telemetry / Stop Arrival / Fleet Selector Sheet
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(12.dp)
        ) {
            when {
                selectedUnit != null -> {
                    LiveBusUnitTelemetryCard(
                        unit = selectedUnit,
                        onClose = { onSelectUnit(null) },
                        onFocusRoute = { onSelectFocusedRoute(selectedUnit.routeId) }
                    )
                }
                selectedStop != null -> {
                    val arrivals = computeStopArrivals(selectedStop)
                    val hasAlert = selectedStop.id in alertStopIds
                    SelectedStopArrivalsCard(
                        stop = selectedStop,
                        arrivals = arrivals,
                        hasProximityAlert = hasAlert,
                        onToggleAlert = { onToggleStopAlert(selectedStop.id) },
                        onClose = { onSelectStop(null) }
                    )
                }
                selectedIncident != null -> {
                    SelectedIncidentMapCard(
                        incident = selectedIncident,
                        onClose = { onSelectIncident(null) }
                    )
                }
                else -> {
                    LiveFleetQuickCarousel(
                        units = visibleUnits,
                        onSelectUnit = onSelectUnit
                    )
                }
            }
        }
    }
}

@Composable
private fun LiveBusUnitTelemetryCard(
    unit: LiveBusUnit,
    onClose: () -> Unit,
    onFocusRoute: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("selected_unit_telemetry_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        color = Color(unit.colorHex),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = unit.routeCode,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "${unit.routeShortName} · Placa ${unit.plateNumber}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${unit.companyName} (${unit.padrónNumber})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar tarjeta")
                }
            }

            AnimatedVisibility(visible = unit.isDelayedByIncident) {
                Surface(
                    color = Color(0xFFFFEBEE),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFC62828),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Tránsito lento en este tramo por incidencia reportada (+3 min)",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFC62828)
                        )
                    }
                }
            }

            // Next Stop & Live ETA Banner
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "PRÓXIMO PARADERO (${unit.directionLabel})",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = unit.nextStopName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "A ${unit.distanceMetersToNextStop} metros de distancia",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${unit.etaMinutesToNextStop} min",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "ETA en vivo",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }
                }
            }

            // Speed & Occupancy Strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${unit.speedKmh} km/h",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Surface(
                        color = Color(unit.occupancyLevel.colorHex).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "Ocupación: ${unit.occupancyLevel.label}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(unit.occupancyLevel.colorHex),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Text(
                    text = "Aislar ruta en mapa",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onFocusRoute() }
                )
            }
        }
    }
}

@Composable
private fun SelectedStopArrivalsCard(
    stop: BusStop,
    arrivals: List<StopEtaArrival>,
    hasProximityAlert: Boolean,
    onToggleAlert: () -> Unit,
    onClose: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("selected_stop_arrivals_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = CircleShape
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    Column {
                        Text(
                            text = stop.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${stop.avenue} · ${stop.district}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar paradero")
                }
            }

            HorizontalDivider()

            Text(
                text = "Próximas unidades acercándose a este paradero:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            arrivals.take(3).forEach { arrival ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            color = Color(arrival.colorHex),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = arrival.routeCode,
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "${arrival.routeShortName} (${arrival.plateNumber})",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${arrival.destinationLabel} · a ${arrival.distanceMeters} m",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Surface(
                        color = if (arrival.etaMinutes <= 3) Color(0xFF00C853) else MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${arrival.etaMinutes} min",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            Button(
                onClick = onToggleAlert,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = if (hasProximityAlert) Icons.Default.NotificationsActive else Icons.Default.NotificationsNone,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (hasProximityAlert) "Alerta de Llegada Activa en este Paradero"
                    else "Notificarme cuando el micro esté cerca"
                )
            }
        }
    }
}

@Composable
private fun SelectedIncidentMapCard(
    incident: IncidentEntity,
    onClose: () -> Unit
) {
    val category = IncidentCategory.fromId(incident.categoryId)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color(category.colorHex),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = category.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
                IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar incidencia")
                }
            }
            Text(
                text = incident.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${incident.avenueOrReference} (${incident.district})",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = incident.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (incident.affectedRouteCodes.isNotBlank()) {
                Text(
                    text = "Rutas afectadas: ${incident.affectedRouteCodes}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun LiveFleetQuickCarousel(
    units: List<LiveBusUnit>,
    onSelectUnit: (LiveBusUnit) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Unidades en circulación ahora (Toca un micro en el mapa o lista)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            LazyRow(
                contentPadding = PaddingValues(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(units, key = { it.unitId }) { unit ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .width(215.dp)
                            .clickable { onSelectUnit(unit) }
                            .testTag("fleet_unit_card_${unit.unitId}")
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = Color(unit.colorHex),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = unit.routeCode,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = Color(0xFF1B5E20),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = "${unit.etaMinutesToNextStop} min",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color(0xFF1B5E20),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Text(
                                text = "Próx: ${unit.nextStopName}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${unit.plateNumber} · ${unit.speedKmh} km/h · a ${unit.distanceMetersToNextStop} m",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}
