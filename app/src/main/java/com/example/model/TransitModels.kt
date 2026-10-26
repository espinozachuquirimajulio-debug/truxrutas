package com.example.model

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

enum class RouteType(val label: String, val badgeText: String) {
    MICRO("Microbús Urbano", "MICRO"),
    COMBI("Combi Urbana", "COMBI"),
    COLECTIVO("Colectivo Interurbano", "COLECTIVO")
}

enum class OccupancyLevel(val label: String, val colorHex: Long) {
    BAJA("Asientos libres", 0xFF00C853),
    MEDIA("Moderado", 0xFFFFAB00),
    ALTA("Lleno / De pie", 0xFFD50000)
}

enum class IncidentCategory(
    val id: String,
    val label: String,
    val descriptionHint: String,
    val colorHex: Long
) {
    TRAFICO("TRAFICO", "Tráfico Intenso", "Atasco vehicular o cuello de botella", 0xFFD32F2F),
    OBRA_BACHE("OBRA_BACHE", "Obra Vial / Bache", "Pista rota, zanja o mantenimiento MPT", 0xFFF57C00),
    DESVIO("DESVIO", "Desvío / Cierre", "Calle cerrada, procesión o desvío de ruta", 0xFF7B1FA2),
    ACCIDENTE("ACCIDENTE", "Accidente Vial", "Choque o vehículo varado en la vía", 0xFFC62828),
    SEMAFORO("SEMAFORO", "Semáforo Averiado", "Semáforo apagado o intermitente en cruce", 0xFFFF8F00),
    SEGURIDAD("SEGURIDAD", "Alerta Ciudadana", "Zona con poca iluminación o precaución", 0xFF0277BD);

    companion object {
        fun fromId(id: String): IncidentCategory =
            entries.find { it.id == id } ?: TRAFICO
    }
}

data class GeoPoint(
    val lat: Double,
    val lng: Double,
    val label: String = ""
) {
    fun distanceMetersTo(other: GeoPoint): Int {
        val earthRadiusMeters = 6371000.0
        val dLat = Math.toRadians(other.lat - this.lat)
        val dLng = Math.toRadians(other.lng - this.lng)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(this.lat)) * cos(Math.toRadians(other.lat)) *
                sin(dLng / 2) * sin(dLng / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return (earthRadiusMeters * c).roundToInt()
    }
}

data class BusStop(
    val id: String,
    val name: String,
    val avenue: String,
    val district: String,
    val location: GeoPoint,
    val routeIds: List<String>,
    val landmark: String,
    val hasRoofShelter: Boolean = true
)

data class BusRoute(
    val id: String,
    val code: String, // e.g. "M-36 A", "M-02 H", "C-13 D"
    val shortName: String, // e.g. "La A", "Huanchaco", "Cortijo"
    val companyName: String,
    val type: RouteType,
    val originDistrict: String,
    val destinationDistrict: String,
    val fareSoles: Double,
    val studentFareSoles: Double,
    val universityFareSoles: Double,
    val frequencyMinutes: Int,
    val estimatedDurationMinutes: Int,
    val operatingHours: String,
    val primaryColorHex: Long,
    val secondaryColorHex: Long,
    val keyStreets: List<String>,
    val stopIds: List<String>,
    val pathWaypoints: List<GeoPoint>,
    val officialReferenceUrl: String = "https://truxdi.com/trujillo/rutas/"
)

data class LiveBusUnit(
    val unitId: String,
    val plateNumber: String,
    val padrónNumber: String,
    val routeId: String,
    val routeCode: String,
    val routeShortName: String,
    val companyName: String,
    val colorHex: Long,
    val currentLocation: GeoPoint,
    val headingDegrees: Float,
    val speedKmh: Int,
    val occupancyLevel: OccupancyLevel,
    val nextStopId: String,
    val nextStopName: String,
    val etaMinutesToNextStop: Int,
    val distanceMetersToNextStop: Int,
    val progressFraction: Float,
    val directionLabel: String,
    val isDelayedByIncident: Boolean = false
)

data class StopEtaArrival(
    val unitId: String,
    val plateNumber: String,
    val padrónNumber: String,
    val routeId: String,
    val routeCode: String,
    val routeShortName: String,
    val companyName: String,
    val colorHex: Long,
    val etaMinutes: Int,
    val distanceMeters: Int,
    val occupancy: OccupancyLevel,
    val destinationLabel: String,
    val isDelayed: Boolean = false
)

data class NearbyStopItem(
    val stop: BusStop,
    val distanceMeters: Int,
    val walkingMinutes: Int,
    val routes: List<BusRoute>,
    val upcomingArrivals: List<StopEtaArrival>,
    val hasActiveAlert: Boolean
)

data class ProximityAlert(
    val id: String,
    val stopId: String,
    val stopName: String,
    val routeId: String,
    val routeCode: String,
    val routeCompany: String,
    val unitPlate: String,
    val distanceMeters: Int,
    val etaMinutes: Int,
    val colorHex: Long,
    val timestamp: Long = System.currentTimeMillis()
)

data class TrujilloReferenceSpot(
    val id: String,
    val name: String,
    val district: String,
    val location: GeoPoint
)
