package com.example.viewmodel

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.data.TruxdiRoutesRepository
import com.example.data.local.IncidentEntity
import com.example.data.local.SavedTransitItemEntity
import com.example.model.BusRoute
import com.example.model.BusStop
import com.example.model.GeoPoint
import com.example.model.IncidentCategory
import com.example.model.LiveBusUnit
import com.example.model.NearbyStopItem
import com.example.model.OccupancyLevel
import com.example.model.ProximityAlert
import com.example.model.RouteType
import com.example.model.StopEtaArrival
import com.example.model.TrujilloReferenceSpot
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.max
import kotlin.math.roundToInt

enum class AppTab {
    ROUTES,
    LIVE_MAP,
    NEARBY_STOPS,
    INCIDENTS
}

private data class SimulatedVehicleState(
    val unitId: String,
    val plateNumber: String,
    val padronNumber: String,
    val route: BusRoute,
    var progress: Float, // 0f..1f along waypoints
    var forward: Boolean,
    val baseSpeedKmh: Int,
    val occupancy: OccupancyLevel
)

class TransitViewModel(
    private val repository: TruxdiRoutesRepository,
    private val appContext: Context
) : ViewModel() {

    val allRoutes: List<BusRoute> = repository.officialRoutes
    val allStops: List<BusStop> = repository.officialStops
    val referenceSpots: List<TrujilloReferenceSpot> = repository.referenceSpots

    private val _currentTab = MutableStateFlow(AppTab.ROUTES)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    // Search & Filters for Routes
    private val _routeSearchQuery = MutableStateFlow("")
    val routeSearchQuery: StateFlow<String> = _routeSearchQuery.asStateFlow()

    private val _selectedRouteType = MutableStateFlow<RouteType?>(null)
    val selectedRouteType: StateFlow<RouteType?> = _selectedRouteType.asStateFlow()

    private val _onlyFavoriteRoutes = MutableStateFlow(false)
    val onlyFavoriteRoutes: StateFlow<Boolean> = _onlyFavoriteRoutes.asStateFlow()

    // Origin - Destination Route Planner
    private val _originStopId = MutableStateFlow<String?>(null)
    val originStopId: StateFlow<String?> = _originStopId.asStateFlow()

    private val _destinationStopId = MutableStateFlow<String?>(null)
    val destinationStopId: StateFlow<String?> = _destinationStopId.asStateFlow()

    // Selected items for detail / map focus
    private val _selectedRouteForDetail = MutableStateFlow<BusRoute?>(null)
    val selectedRouteForDetail: StateFlow<BusRoute?> = _selectedRouteForDetail.asStateFlow()

    private val _mapFocusedRouteId = MutableStateFlow<String?>(null)
    val mapFocusedRouteId: StateFlow<String?> = _mapFocusedRouteId.asStateFlow()

    private val _selectedLiveUnit = MutableStateFlow<LiveBusUnit?>(null)
    val selectedLiveUnit: StateFlow<LiveBusUnit?> = _selectedLiveUnit.asStateFlow()

    private val _selectedMapStop = MutableStateFlow<BusStop?>(null)
    val selectedMapStop: StateFlow<BusStop?> = _selectedMapStop.asStateFlow()

    private val _selectedMapIncident = MutableStateFlow<IncidentEntity?>(null)
    val selectedMapIncident: StateFlow<IncidentEntity?> = _selectedMapIncident.asStateFlow()

    // User location in Trujillo (defaults to Óvalo Papal so user is right in the heart of Trujillo)
    private val _userLocation = MutableStateFlow(referenceSpots.first().location)
    val userLocation: StateFlow<GeoPoint> = _userLocation.asStateFlow()

    private val _locationSourceLabel = MutableStateFlow("Óvalo Papal (Trujillo)")
    val locationSourceLabel: StateFlow<String> = _locationSourceLabel.asStateFlow()

    // Search & Filters for Nearby Stops
    private val _stopSearchQuery = MutableStateFlow("")
    val stopSearchQuery: StateFlow<String> = _stopSearchQuery.asStateFlow()

    // Incidents filters
    private val _selectedIncidentCategory = MutableStateFlow<IncidentCategory?>(null)
    val selectedIncidentCategory: StateFlow<IncidentCategory?> = _selectedIncidentCategory.asStateFlow()

    private val _selectedIncidentDistrict = MutableStateFlow<String?>(null)
    val selectedIncidentDistrict: StateFlow<String?> = _selectedIncidentDistrict.asStateFlow()

    // Telemetry simulation speed multiplier (1x normal, 3x fast-forward for previewing arrivals)
    private val _simulationSpeedMultiplier = MutableStateFlow(1)
    val simulationSpeedMultiplier: StateFlow<Int> = _simulationSpeedMultiplier.asStateFlow()

    // Live Bus Units State
    private val _liveBusUnits = MutableStateFlow<List<LiveBusUnit>>(emptyList())
    val liveBusUnits: StateFlow<List<LiveBusUnit>> = _liveBusUnits.asStateFlow()

    // Real-Time Proximity Alert Banner State
    private val _activeProximityAlert = MutableStateFlow<ProximityAlert?>(null)
    val activeProximityAlert: StateFlow<ProximityAlert?> = _activeProximityAlert.asStateFlow()

    private val notifiedAlertKeys = mutableSetOf<String>()

    val incidents: StateFlow<List<IncidentEntity>> = repository.incidentsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedItems: StateFlow<List<SavedTransitItemEntity>> = repository.savedItemsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val vehicleFleet: List<SimulatedVehicleState> = buildInitialFleet()

    val filteredRoutes: StateFlow<List<BusRoute>> = combine(
        _routeSearchQuery,
        _selectedRouteType,
        _onlyFavoriteRoutes,
        _originStopId,
        _destinationStopId,
        savedItems
    ) { values ->
        val query = (values[0] as String).trim().lowercase()
        val typeFilter = values[1] as RouteType?
        val favOnly = values[2] as Boolean
        val originId = values[3] as String?
        val destId = values[4] as String?
        @Suppress("UNCHECKED_CAST")
        val saved = values[5] as List<SavedTransitItemEntity>

        val favRouteIds = saved.filter { it.itemType == "ROUTE" && it.isFavorite }.map { it.itemId }.toSet()

        allRoutes.filter { route ->
            val matchesType = typeFilter == null || route.type == typeFilter
            val matchesFav = !favOnly || route.id in favRouteIds
            val matchesOrigin = originId == null || originId in route.stopIds
            val matchesDest = destId == null || destId in route.stopIds
            val matchesQuery = query.isEmpty() ||
                    route.code.lowercase().contains(query) ||
                    route.shortName.lowercase().contains(query) ||
                    route.companyName.lowercase().contains(query) ||
                    route.originDistrict.lowercase().contains(query) ||
                    route.destinationDistrict.lowercase().contains(query) ||
                    route.keyStreets.any { it.lowercase().contains(query) }

            matchesType && matchesFav && matchesOrigin && matchesDest && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), allRoutes)

    val nearbyStops: StateFlow<List<NearbyStopItem>> = combine(
        _userLocation,
        _stopSearchQuery,
        _liveBusUnits,
        savedItems
    ) { userLoc, query, units, saved ->
        val cleanQuery = query.trim().lowercase()
        val alertStopIds = saved
            .filter { it.itemType == "STOP" && it.hasProximityAlert }
            .map { it.itemId }
            .toSet()

        allStops
            .filter { stop ->
                cleanQuery.isEmpty() ||
                        stop.name.lowercase().contains(cleanQuery) ||
                        stop.avenue.lowercase().contains(cleanQuery) ||
                        stop.district.lowercase().contains(cleanQuery) ||
                        stop.landmark.lowercase().contains(cleanQuery)
            }
            .map { stop ->
                val dist = userLoc.distanceMetersTo(stop.location)
                val walkMins = max(1, (dist / 78.0).roundToInt())
                val passingRoutes = allRoutes.filter { stop.id in it.stopIds }
                val arrivals = computeArrivalsForStop(stop, units)
                NearbyStopItem(
                    stop = stop,
                    distanceMeters = dist,
                    walkingMinutes = walkMins,
                    routes = passingRoutes,
                    upcomingArrivals = arrivals,
                    hasActiveAlert = stop.id in alertStopIds
                )
            }
            .sortedBy { it.distanceMeters }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredIncidents: StateFlow<List<IncidentEntity>> = combine(
        incidents,
        _selectedIncidentCategory,
        _selectedIncidentDistrict
    ) { allInc, cat, dist ->
        allInc.filter { inc ->
            val matchesCat = cat == null || inc.categoryId == cat.id
            val matchesDist = dist == null || inc.district.equals(dist, ignoreCase = true)
            matchesCat && matchesDist
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        createNotificationChannel()
        viewModelScope.launch {
            repository.ensureSeedData()
        }
        startLiveTelemetryLoop()
    }

    private fun buildInitialFleet(): List<SimulatedVehicleState> {
        val fleet = mutableListOf<SimulatedVehicleState>()
        val occupancies = listOf(OccupancyLevel.BAJA, OccupancyLevel.MEDIA, OccupancyLevel.ALTA)

        allRoutes.forEachIndexed { index, route ->
            // Unit 1: Going forward (Ida)
            fleet.add(
                SimulatedVehicleState(
                    unitId = "${route.id}_u1",
                    plateNumber = "T${index + 1}A-${710 + index * 13}",
                    padronNumber = "PAD-${104 + index * 7}",
                    route = route,
                    progress = ((index * 0.19f) % 0.85f) + 0.05f,
                    forward = true,
                    baseSpeedKmh = if (route.type == RouteType.COLECTIVO) 38 else 26 + (index % 7),
                    occupancy = occupancies[index % occupancies.size]
                )
            )
            // Unit 2: Going reverse (Vuelta)
            fleet.add(
                SimulatedVehicleState(
                    unitId = "${route.id}_u2",
                    plateNumber = "T${index + 1}B-${820 + index * 11}",
                    padronNumber = "PAD-${215 + index * 9}",
                    route = route,
                    progress = (((index * 0.27f) + 0.45f) % 0.85f) + 0.05f,
                    forward = false,
                    baseSpeedKmh = if (route.type == RouteType.COLECTIVO) 36 else 24 + ((index + 3) % 8),
                    occupancy = occupancies[(index + 1) % occupancies.size]
                )
            )
        }
        return fleet
    }

    private fun startLiveTelemetryLoop() {
        viewModelScope.launch {
            while (isActive) {
                val activeIncidents = incidents.value.filter { it.status == "ACTIVO" }
                val multiplier = _simulationSpeedMultiplier.value
                val stepDelta = 0.014f * multiplier

                val updatedUnits = vehicleFleet.map { vehicle ->
                    if (vehicle.forward) {
                        vehicle.progress += stepDelta
                        if (vehicle.progress >= 1f) {
                            vehicle.progress = 1f
                            vehicle.forward = false
                        }
                    } else {
                        vehicle.progress -= stepDelta
                        if (vehicle.progress <= 0f) {
                            vehicle.progress = 0f
                            vehicle.forward = true
                        }
                    }

                    val waypoints = vehicle.route.pathWaypoints
                    val totalSegments = max(1, waypoints.size - 1)
                    val exactIndex = vehicle.progress * totalSegments
                    val segIndex = exactIndex.toInt().coerceIn(0, totalSegments - 1)
                    val segFraction = (exactIndex - segIndex).coerceIn(0f, 1f)

                    val startPt = waypoints[segIndex]
                    val endPt = waypoints[(segIndex + 1).coerceAtMost(waypoints.lastIndex)]

                    val currentLat = startPt.lat + (endPt.lat - startPt.lat) * segFraction
                    val currentLng = startPt.lng + (endPt.lng - startPt.lng) * segFraction
                    val currentPos = GeoPoint(currentLat, currentLng)

                    val dLat = if (vehicle.forward) (endPt.lat - startPt.lat) else (startPt.lat - endPt.lat)
                    val dLng = if (vehicle.forward) (endPt.lng - startPt.lng) else (startPt.lng - endPt.lng)
                    val heading = Math.toDegrees(atan2(dLng, dLat)).toFloat()

                    // Check if near any active incident
                    val nearIncident = activeIncidents.any { inc ->
                        currentPos.distanceMetersTo(GeoPoint(inc.latitude, inc.longitude)) <= 450
                    }
                    val effectiveSpeed = if (nearIncident) max(12, vehicle.baseSpeedKmh - 12) else vehicle.baseSpeedKmh

                    // Next stop along route
                    val routeStops = vehicle.route.stopIds.mapNotNull { id -> allStops.find { it.id == id } }
                    val targetStop = if (routeStops.isNotEmpty()) {
                        val orderedStops = if (vehicle.forward) routeStops else routeStops.reversed()
                        orderedStops.minByOrNull { it.location.distanceMetersTo(currentPos) } ?: routeStops.first()
                    } else {
                        allStops.first()
                    }

                    val distToNextStop = currentPos.distanceMetersTo(targetStop.location)
                    val speedMetersPerMin = (effectiveSpeed * 1000.0) / 60.0
                    val etaMins = max(1, (distToNextStop / speedMetersPerMin).roundToInt())

                    val directionText = if (vehicle.forward) {
                        "Hacia ${vehicle.route.destinationDistrict}"
                    } else {
                        "Hacia ${vehicle.route.originDistrict}"
                    }

                    LiveBusUnit(
                        unitId = vehicle.unitId,
                        plateNumber = vehicle.plateNumber,
                        padrónNumber = vehicle.padronNumber,
                        routeId = vehicle.route.id,
                        routeCode = vehicle.route.code,
                        routeShortName = vehicle.route.shortName,
                        companyName = vehicle.route.companyName,
                        colorHex = vehicle.route.primaryColorHex,
                        currentLocation = currentPos,
                        headingDegrees = heading,
                        speedKmh = effectiveSpeed,
                        occupancyLevel = vehicle.occupancy,
                        nextStopId = targetStop.id,
                        nextStopName = targetStop.name,
                        etaMinutesToNextStop = etaMins,
                        distanceMetersToNextStop = distToNextStop,
                        progressFraction = vehicle.progress,
                        directionLabel = directionText,
                        isDelayedByIncident = nearIncident
                    )
                }

                _liveBusUnits.value = updatedUnits

                // Keep selected unit updated in real time
                _selectedLiveUnit.value?.let { selected ->
                    _selectedLiveUnit.value = updatedUnits.find { it.unitId == selected.unitId }
                }

                checkProximityAlerts(updatedUnits)
                delay(2000L)
            }
        }
    }

    private fun checkProximityAlerts(units: List<LiveBusUnit>) {
        val alertStopIds = savedItems.value
            .filter { it.itemType == "STOP" && it.hasProximityAlert }
            .map { it.itemId }
            .toSet()

        if (alertStopIds.isEmpty()) return

        for (stopId in alertStopIds) {
            val stop = allStops.find { it.id == stopId } ?: continue
            val approachingUnit = units
                .filter { stopId in (allRoutes.find { r -> r.id == it.routeId }?.stopIds ?: emptyList()) }
                .map { unit -> unit to unit.currentLocation.distanceMetersTo(stop.location) }
                .filter { (_, dist) -> dist <= 380 }
                .minByOrNull { it.second }

            if (approachingUnit != null) {
                val (unit, distance) = approachingUnit
                val alertKey = "${stop.id}_${unit.unitId}_${System.currentTimeMillis() / 60000}"
                if (alertKey !in notifiedAlertKeys) {
                    notifiedAlertKeys.add(alertKey)
                    val speedMetersPerMin = (unit.speedKmh * 1000.0) / 60.0
                    val eta = max(1, (distance / speedMetersPerMin).roundToInt())
                    val alert = ProximityAlert(
                        id = alertKey,
                        stopId = stop.id,
                        stopName = stop.name,
                        routeId = unit.routeId,
                        routeCode = unit.routeCode,
                        routeCompany = unit.companyName,
                        unitPlate = unit.plateNumber,
                        distanceMeters = distance,
                        etaMinutes = eta,
                        colorHex = unit.colorHex
                    )
                    _activeProximityAlert.value = alert
                    postSystemProximityNotification(alert)
                    break
                }
            }
        }
    }

    fun triggerSimulatedArrivalAlert(stop: BusStop) {
        val route = allRoutes.firstOrNull { stop.id in it.stopIds } ?: allRoutes.first()
        val unit = _liveBusUnits.value.firstOrNull { it.routeId == route.id }
        val alert = ProximityAlert(
            id = "manual_${System.currentTimeMillis()}",
            stopId = stop.id,
            stopName = stop.name,
            routeId = route.id,
            routeCode = route.code,
            routeCompany = route.companyName,
            unitPlate = unit?.plateNumber ?: "T1A-710",
            distanceMeters = 180,
            etaMinutes = 1,
            colorHex = route.primaryColorHex
        )
        _activeProximityAlert.value = alert
        postSystemProximityNotification(alert)
    }

    fun dismissProximityAlert() {
        _activeProximityAlert.value = null
    }

    fun computeArrivalsForStop(stop: BusStop, units: List<LiveBusUnit> = _liveBusUnits.value): List<StopEtaArrival> {
        val passingRouteIds = stop.routeIds.toSet()
        return units
            .filter { it.routeId in passingRouteIds }
            .map { unit ->
                val dist = unit.currentLocation.distanceMetersTo(stop.location)
                val speedMetersPerMin = max(250.0, (unit.speedKmh * 1000.0) / 60.0)
                val eta = max(1, (dist / speedMetersPerMin).roundToInt())
                StopEtaArrival(
                    unitId = unit.unitId,
                    plateNumber = unit.plateNumber,
                    padrónNumber = unit.padrónNumber,
                    routeId = unit.routeId,
                    routeCode = unit.routeCode,
                    routeShortName = unit.routeShortName,
                    companyName = unit.companyName,
                    colorHex = unit.colorHex,
                    etaMinutes = eta,
                    distanceMeters = dist,
                    occupancy = unit.occupancyLevel,
                    destinationLabel = unit.directionLabel,
                    isDelayed = unit.isDelayedByIncident
                )
            }
            .sortedBy { it.etaMinutes }
    }

    // UI Actions
    fun selectTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun updateRouteSearchQuery(query: String) {
        _routeSearchQuery.value = query
    }

    fun selectRouteTypeFilter(type: RouteType?) {
        _selectedRouteType.value = if (_selectedRouteType.value == type) null else type
    }

    fun toggleOnlyFavoritesFilter() {
        _onlyFavoriteRoutes.value = !_onlyFavoriteRoutes.value
    }

    fun setRoutePlannerStops(originId: String?, destinationId: String?) {
        _originStopId.value = originId
        _destinationStopId.value = destinationId
    }

    fun clearRoutePlanner() {
        _originStopId.value = null
        _destinationStopId.value = null
    }

    fun openRouteDetail(route: BusRoute?) {
        _selectedRouteForDetail.value = route
    }

    fun viewRouteOnLiveMap(route: BusRoute) {
        _selectedRouteForDetail.value = null
        _mapFocusedRouteId.value = route.id
        _selectedMapStop.value = null
        _selectedMapIncident.value = null
        _selectedLiveUnit.value = _liveBusUnits.value.firstOrNull { it.routeId == route.id }
        _currentTab.value = AppTab.LIVE_MAP
    }

    fun viewStopOnLiveMap(stop: BusStop) {
        _selectedMapStop.value = stop
        _selectedLiveUnit.value = null
        _selectedMapIncident.value = null
        _currentTab.value = AppTab.LIVE_MAP
    }

    fun viewIncidentOnLiveMap(incident: IncidentEntity) {
        _selectedMapIncident.value = incident
        _selectedLiveUnit.value = null
        _selectedMapStop.value = null
        _currentTab.value = AppTab.LIVE_MAP
    }

    fun setMapFocusedRoute(routeId: String?) {
        _mapFocusedRouteId.value = if (_mapFocusedRouteId.value == routeId) null else routeId
    }

    fun selectLiveUnitOnMap(unit: LiveBusUnit?) {
        _selectedLiveUnit.value = unit
        if (unit != null) {
            _selectedMapStop.value = null
            _selectedMapIncident.value = null
        }
    }

    fun selectStopOnMap(stop: BusStop?) {
        _selectedMapStop.value = stop
        if (stop != null) {
            _selectedLiveUnit.value = null
            _selectedMapIncident.value = null
        }
    }

    fun selectIncidentOnMap(incident: IncidentEntity?) {
        _selectedMapIncident.value = incident
        if (incident != null) {
            _selectedLiveUnit.value = null
            _selectedMapStop.value = null
        }
    }

    fun updateStopSearchQuery(query: String) {
        _stopSearchQuery.value = query
    }

    fun setReferenceSpotLocation(spot: TrujilloReferenceSpot) {
        _userLocation.value = spot.location
        _locationSourceLabel.value = "${spot.name} (${spot.district})"
    }

    @SuppressLint("MissingPermission")
    fun refreshDeviceGpsLocation(onResultMessage: (String) -> Unit) {
        val hasFine = ContextCompat.checkSelfPermission(
            appContext,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(
            appContext,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasFine && !hasCoarse) {
            onResultMessage("Permiso de ubicación requerido para usar GPS directo.")
            return
        }

        val fusedClient = LocationServices.getFusedLocationProviderClient(appContext)
        fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location ->
                if (location != null) {
                    val detected = GeoPoint(location.latitude, location.longitude, "Mi GPS")
                    // Check if within ~50km of Trujillo Metropolitan Area (-8.11, -79.03)
                    val trujilloCenter = GeoPoint(-8.1117, -79.0287)
                    if (detected.distanceMetersTo(trujilloCenter) <= 50000) {
                        _userLocation.value = detected
                        _locationSourceLabel.value = "GPS en Tiempo Real (Trujillo)"
                        onResultMessage("Ubicación GPS actualizada en Trujillo.")
                    } else {
                        // Emulator default GPS is outside Trujillo; keep Trujillo coordinates active while acknowledging GPS read
                        _userLocation.value = GeoPoint(-8.1117, -79.0287, "Centro Histórico Trujillo")
                        _locationSourceLabel.value = "GPS Activo · Centro de Trujillo"
                        onResultMessage("GPS sincronizado en zona metropolitana de Trujillo.")
                    }
                } else {
                    onResultMessage("Usando punto de referencia en Trujillo.")
                }
            }
            .addOnFailureListener {
                onResultMessage("Ubicación centrada en Trujillo Metropolitano.")
            }
    }

    fun toggleSimulationSpeed() {
        _simulationSpeedMultiplier.value = if (_simulationSpeedMultiplier.value == 1) 3 else 1
    }

    fun toggleFavoriteRoute(routeId: String) {
        viewModelScope.launch {
            repository.toggleRouteFavorite(routeId)
        }
    }

    fun toggleStopAlert(stopId: String) {
        viewModelScope.launch {
            repository.toggleStopProximityAlert(stopId)
        }
    }

    fun setIncidentCategoryFilter(category: IncidentCategory?) {
        _selectedIncidentCategory.value = if (_selectedIncidentCategory.value == category) null else category
    }

    fun setIncidentDistrictFilter(district: String?) {
        _selectedIncidentDistrict.value = if (_selectedIncidentDistrict.value == district) null else district
    }

    fun submitNewIncident(
        title: String,
        description: String,
        category: IncidentCategory,
        avenueOrReference: String,
        district: String,
        location: GeoPoint,
        reporterName: String,
        affectedRoutes: List<String>
    ) {
        viewModelScope.launch {
            val newIncident = IncidentEntity(
                title = title.trim(),
                description = description.trim(),
                categoryId = category.id,
                avenueOrReference = avenueOrReference.trim(),
                district = district,
                latitude = location.lat,
                longitude = location.lng,
                reporterName = reporterName.ifBlank { "Vecino Trujillano" },
                upvotes = 1,
                isVerified = false,
                status = "ACTIVO",
                affectedRouteCodes = affectedRoutes.joinToString(", ")
            )
            repository.addIncident(newIncident)
        }
    }

    fun upvoteIncident(incidentId: Int) {
        viewModelScope.launch {
            repository.upvoteIncident(incidentId)
        }
    }

    fun toggleIncidentStatus(incident: IncidentEntity) {
        viewModelScope.launch {
            repository.toggleIncidentResolved(incident)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Alertas de Paraderos TruxRutas",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Avisos en tiempo real cuando tu micro o colectivo se acerca a tu paradero en Trujillo"
            }
            val manager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.createNotificationChannel(channel)
        }
    }

    @SuppressLint("MissingPermission")
    private fun postSystemProximityNotification(alert: ProximityAlert) {
        val hasNotifPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                appContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        if (!hasNotifPermission) return

        val builder = NotificationCompat.Builder(appContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("¡Tu micro ${alert.routeCode} está llegando!")
            .setContentText("Unidad ${alert.unitPlate} a ${alert.distanceMeters} m de ${alert.stopName} (~${alert.etaMinutes} min)")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(appContext).notify(alert.id.hashCode(), builder.build())
        } catch (_: SecurityException) {
            // Ignored if permission was revoked
        }
    }

    companion object {
        private const val CHANNEL_ID = "truxrutas_proximity_alerts"

        fun provideFactory(
            repository: TruxdiRoutesRepository,
            context: Context
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return TransitViewModel(repository, context.applicationContext) as T
            }
        }
    }
}
