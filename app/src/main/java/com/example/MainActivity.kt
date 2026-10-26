package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.DirectionsBus
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.TruxdiRoutesRepository
import com.example.data.local.TruxRutasDatabase
import com.example.model.ProximityAlert
import com.example.ui.screens.IncidentsScreen
import com.example.ui.screens.LiveMapScreen
import com.example.ui.screens.NearbyStopsScreen
import com.example.ui.screens.RoutesCatalogScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AppTab
import com.example.viewmodel.TransitViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val database = TruxRutasDatabase.getInstance(applicationContext)
        val repository = TruxdiRoutesRepository(database.transitDao())

        setContent {
            MyApplicationTheme {
                val context = LocalContext.current
                val transitViewModel: TransitViewModel = viewModel(
                    factory = TransitViewModel.provideFactory(repository, context)
                )
                TruxRutasApp(viewModel = transitViewModel)
            }
        }
    }
}

@Composable
fun TruxRutasApp(
    viewModel: TransitViewModel
) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val filteredRoutes by viewModel.filteredRoutes.collectAsStateWithLifecycle()
    val nearbyStops by viewModel.nearbyStops.collectAsStateWithLifecycle()
    val liveUnits by viewModel.liveBusUnits.collectAsStateWithLifecycle()
    val incidents by viewModel.filteredIncidents.collectAsStateWithLifecycle()
    val allIncidents by viewModel.incidents.collectAsStateWithLifecycle()
    val savedItems by viewModel.savedItems.collectAsStateWithLifecycle()

    val routeSearchQuery by viewModel.routeSearchQuery.collectAsStateWithLifecycle()
    val selectedRouteType by viewModel.selectedRouteType.collectAsStateWithLifecycle()
    val onlyFavoriteRoutes by viewModel.onlyFavoriteRoutes.collectAsStateWithLifecycle()
    val originStopId by viewModel.originStopId.collectAsStateWithLifecycle()
    val destinationStopId by viewModel.destinationStopId.collectAsStateWithLifecycle()
    val selectedRouteDetail by viewModel.selectedRouteForDetail.collectAsStateWithLifecycle()

    val mapFocusedRouteId by viewModel.mapFocusedRouteId.collectAsStateWithLifecycle()
    val selectedLiveUnit by viewModel.selectedLiveUnit.collectAsStateWithLifecycle()
    val selectedMapStop by viewModel.selectedMapStop.collectAsStateWithLifecycle()
    val selectedMapIncident by viewModel.selectedMapIncident.collectAsStateWithLifecycle()
    val simulationSpeed by viewModel.simulationSpeedMultiplier.collectAsStateWithLifecycle()

    val userLocation by viewModel.userLocation.collectAsStateWithLifecycle()
    val locationLabel by viewModel.locationSourceLabel.collectAsStateWithLifecycle()
    val stopSearchQuery by viewModel.stopSearchQuery.collectAsStateWithLifecycle()

    val selectedIncidentCategory by viewModel.selectedIncidentCategory.collectAsStateWithLifecycle()
    val selectedIncidentDistrict by viewModel.selectedIncidentDistrict.collectAsStateWithLifecycle()

    val activeProximityAlert by viewModel.activeProximityAlert.collectAsStateWithLifecycle()

    // BackHandler on secondary tabs to return to the primary Routes catalog
    BackHandler(enabled = currentTab != AppTab.ROUTES) {
        viewModel.selectTab(AppTab.ROUTES)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("bottom_navigation_bar")
            ) {
                NavigationBarItem(
                    selected = currentTab == AppTab.ROUTES,
                    onClick = { viewModel.selectTab(AppTab.ROUTES) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == AppTab.ROUTES) Icons.Filled.DirectionsBus else Icons.Outlined.DirectionsBus,
                            contentDescription = "Rutas"
                        )
                    },
                    label = { Text("Rutas") },
                    modifier = Modifier.testTag("nav_tab_routes")
                )

                NavigationBarItem(
                    selected = currentTab == AppTab.LIVE_MAP,
                    onClick = { viewModel.selectTab(AppTab.LIVE_MAP) },
                    icon = {
                        BadgedBox(
                            badge = {
                                Badge(containerColor = Color(0xFF00C853), contentColor = Color.White) {
                                    Text("${liveUnits.size}")
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (currentTab == AppTab.LIVE_MAP) Icons.Filled.Map else Icons.Outlined.Map,
                                contentDescription = "Mapa en Vivo"
                            )
                        }
                    },
                    label = { Text("Mapa Vivo") },
                    modifier = Modifier.testTag("nav_tab_map")
                )

                NavigationBarItem(
                    selected = currentTab == AppTab.NEARBY_STOPS,
                    onClick = { viewModel.selectTab(AppTab.NEARBY_STOPS) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == AppTab.NEARBY_STOPS) Icons.Filled.LocationOn else Icons.Outlined.LocationOn,
                            contentDescription = "Paraderos Cercanos"
                        )
                    },
                    label = { Text("Paraderos") },
                    modifier = Modifier.testTag("nav_tab_stops")
                )

                val activeIncidentCount = allIncidents.count { it.status == "ACTIVO" }
                NavigationBarItem(
                    selected = currentTab == AppTab.INCIDENTS,
                    onClick = { viewModel.selectTab(AppTab.INCIDENTS) },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (activeIncidentCount > 0) {
                                    Badge { Text("$activeIncidentCount") }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (currentTab == AppTab.INCIDENTS) Icons.Filled.Warning else Icons.Outlined.Warning,
                                contentDescription = "Incidencias Viales"
                            )
                        }
                    },
                    label = { Text("Incidencias") },
                    modifier = Modifier.testTag("nav_tab_incidents")
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Real-Time Proximity Arrival Notification Banner
            AnimatedVisibility(
                visible = activeProximityAlert != null,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                activeProximityAlert?.let { alert ->
                    ProximityAlertBanner(
                        alert = alert,
                        onOpenOnMap = {
                            val stop = viewModel.allStops.find { it.id == alert.stopId }
                            if (stop != null) {
                                viewModel.viewStopOnLiveMap(stop)
                            }
                            viewModel.dismissProximityAlert()
                        },
                        onDismiss = { viewModel.dismissProximityAlert() }
                    )
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                when (currentTab) {
                    AppTab.ROUTES -> {
                        RoutesCatalogScreen(
                            routes = filteredRoutes,
                            allStops = viewModel.allStops,
                            liveUnits = liveUnits,
                            savedItems = savedItems,
                            searchQuery = routeSearchQuery,
                            selectedType = selectedRouteType,
                            onlyFavorites = onlyFavoriteRoutes,
                            originStopId = originStopId,
                            destinationStopId = destinationStopId,
                            selectedRouteForDetail = selectedRouteDetail,
                            onSearchQueryChange = viewModel::updateRouteSearchQuery,
                            onSelectType = viewModel::selectRouteTypeFilter,
                            onToggleFavorites = viewModel::toggleOnlyFavoritesFilter,
                            onSetRoutePlanner = viewModel::setRoutePlannerStops,
                            onClearPlanner = viewModel::clearRoutePlanner,
                            onToggleFavoriteRoute = viewModel::toggleFavoriteRoute,
                            onSelectRouteDetail = viewModel::openRouteDetail,
                            onViewRouteOnMap = viewModel::viewRouteOnLiveMap
                        )
                    }

                    AppTab.LIVE_MAP -> {
                        LiveMapScreen(
                            routes = viewModel.allRoutes,
                            focusedRouteId = mapFocusedRouteId,
                            stops = viewModel.allStops,
                            liveUnits = liveUnits,
                            incidents = allIncidents,
                            savedItems = savedItems,
                            userLocation = userLocation,
                            selectedUnit = selectedLiveUnit,
                            selectedStop = selectedMapStop,
                            selectedIncident = selectedMapIncident,
                            simulationSpeed = simulationSpeed,
                            computeStopArrivals = { stop -> viewModel.computeArrivalsForStop(stop) },
                            onSelectFocusedRoute = viewModel::setMapFocusedRoute,
                            onSelectUnit = viewModel::selectLiveUnitOnMap,
                            onSelectStop = viewModel::selectStopOnMap,
                            onSelectIncident = viewModel::selectIncidentOnMap,
                            onToggleStopAlert = viewModel::toggleStopAlert,
                            onToggleSimulationSpeed = viewModel::toggleSimulationSpeed
                        )
                    }

                    AppTab.NEARBY_STOPS -> {
                        NearbyStopsScreen(
                            nearbyStops = nearbyStops,
                            referenceSpots = viewModel.referenceSpots,
                            currentLocationLabel = locationLabel,
                            searchQuery = stopSearchQuery,
                            onSearchQueryChange = viewModel::updateStopSearchQuery,
                            onSelectReferenceSpot = viewModel::setReferenceSpotLocation,
                            onRequestGpsSync = viewModel::refreshDeviceGpsLocation,
                            onToggleStopAlert = viewModel::toggleStopAlert,
                            onSimulateArrivalAlert = viewModel::triggerSimulatedArrivalAlert,
                            onViewStopOnMap = viewModel::viewStopOnLiveMap
                        )
                    }

                    AppTab.INCIDENTS -> {
                        IncidentsScreen(
                            incidents = incidents,
                            allRoutes = viewModel.allRoutes,
                            referenceSpots = viewModel.referenceSpots,
                            selectedCategory = selectedIncidentCategory,
                            selectedDistrict = selectedIncidentDistrict,
                            onSelectCategory = viewModel::setIncidentCategoryFilter,
                            onSelectDistrict = viewModel::setIncidentDistrictFilter,
                            onSubmitIncident = viewModel::submitNewIncident,
                            onUpvoteIncident = viewModel::upvoteIncident,
                            onToggleIncidentStatus = viewModel::toggleIncidentStatus,
                            onViewIncidentOnMap = viewModel::viewIncidentOnLiveMap
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProximityAlertBanner(
    alert: ProximityAlert,
    onOpenOnMap: () -> Unit,
    onDismiss: () -> Unit
) {
    Surface(
        color = Color(0xFF1B5E20),
        contentColor = Color.White,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenOnMap() }
            .testTag("live_proximity_alert_banner")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFB300)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = Color(0xFF0A2E6E),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            color = Color(alert.colorHex),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = alert.routeCode,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = "¡Micro acercándose a ${alert.stopName}!",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White
                        )
                    }
                    Text(
                        text = "Unidad ${alert.unitPlate} a ${alert.distanceMeters} m · Llega en ~${alert.etaMinutes} min (Toca para ver en mapa)",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFE8F5E9)
                    )
                }
            }

            Spacer(modifier = Modifier.width(4.dp))
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cerrar alerta",
                    tint = Color.White
                )
            }
        }
    }
}
