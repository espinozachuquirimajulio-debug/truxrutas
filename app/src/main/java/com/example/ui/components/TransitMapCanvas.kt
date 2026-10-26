package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import com.example.data.local.IncidentEntity
import com.example.model.BusRoute
import com.example.model.BusStop
import com.example.model.GeoPoint
import com.example.model.IncidentCategory
import com.example.model.LiveBusUnit
import kotlin.math.hypot

// Bounding Box for Metropolitan Trujillo (Huanchaco to Salaverry, Laredo to Coast)
private const val MIN_LAT = -8.2300
private const val MAX_LAT = -8.0600
private const val MIN_LNG = -79.1300
private const val MAX_LNG = -78.9500

@Composable
fun TransitMapCanvas(
    routes: List<BusRoute>,
    focusedRouteId: String?,
    stops: List<BusStop>,
    liveUnits: List<LiveBusUnit>,
    incidents: List<IncidentEntity>,
    userLocation: GeoPoint,
    alertStopIds: Set<String>,
    selectedUnit: LiveBusUnit?,
    selectedStop: BusStop?,
    selectedIncident: IncidentEntity?,
    showUnitsLayer: Boolean,
    showStopsLayer: Boolean,
    showIncidentsLayer: Boolean,
    zoomLevel: Float,
    panOffset: Offset,
    onTransformChange: (Float, Offset) -> Unit,
    onSelectUnit: (LiveBusUnit?) -> Unit,
    onSelectStop: (BusStop?) -> Unit,
    onSelectIncident: (IncidentEntity?) -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "map_pulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 12f,
        targetValue = 30f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_radius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 0.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_alpha"
    )

    var canvasSize by remember { mutableStateOf(Size.Zero) }
    var currentZoom by remember(zoomLevel) { mutableFloatStateOf(zoomLevel) }
    var currentPan by remember(panOffset) { mutableStateOf(panOffset) }

    fun geoToScreen(point: GeoPoint, size: Size, zoom: Float, pan: Offset): Offset {
        if (size.width <= 0f || size.height <= 0f) return Offset.Zero
        val normX = ((point.lng - MIN_LNG) / (MAX_LNG - MIN_LNG)).toFloat()
        // Latitude increases northward (up on screen, so invert Y)
        val normY = (1.0 - (point.lat - MIN_LAT) / (MAX_LAT - MIN_LAT)).toFloat()

        val baseX = normX * size.width
        val baseY = normY * size.height

        val centerX = size.width / 2f
        val centerY = size.height / 2f

        val zoomedX = centerX + (baseX - centerX) * zoom + pan.x
        val zoomedY = centerY + (baseY - centerY) * zoom + pan.y
        return Offset(zoomedX, zoomedY)
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFEAF1F8))
            .testTag("trujillo_interactive_map")
            .pointerInput(zoomLevel, panOffset) {
                detectTransformGestures { _, pan, zoomChange, _ ->
                    val newZoom = (currentZoom * zoomChange).coerceIn(0.85f, 4.5f)
                    val maxPanX = size.width * newZoom * 0.6f
                    val maxPanY = size.height * newZoom * 0.6f
                    val newPan = Offset(
                        x = (currentPan.x + pan.x).coerceIn(-maxPanX, maxPanX),
                        y = (currentPan.y + pan.y).coerceIn(-maxPanY, maxPanY)
                    )
                    currentZoom = newZoom
                    currentPan = newPan
                    onTransformChange(newZoom, newPan)
                }
            }
            .pointerInput(liveUnits, stops, incidents, showUnitsLayer, showStopsLayer, showIncidentsLayer, currentZoom, currentPan) {
                detectTapGestures { tapOffset ->
                    if (canvasSize.width <= 0f) return@detectTapGestures

                    // 1. Check live bus units first
                    if (showUnitsLayer) {
                        val visibleUnits = if (focusedRouteId != null) {
                            liveUnits.filter { it.routeId == focusedRouteId }
                        } else {
                            liveUnits
                        }
                        val tappedUnit = visibleUnits.minByOrNull { unit ->
                            val pos = geoToScreen(unit.currentLocation, canvasSize, currentZoom, currentPan)
                            hypot(tapOffset.x - pos.x, tapOffset.y - pos.y)
                        }
                        if (tappedUnit != null) {
                            val pos = geoToScreen(tappedUnit.currentLocation, canvasSize, currentZoom, currentPan)
                            if (hypot(tapOffset.x - pos.x, tapOffset.y - pos.y) <= 46f) {
                                onSelectUnit(tappedUnit)
                                return@detectTapGestures
                            }
                        }
                    }

                    // 2. Check incidents
                    if (showIncidentsLayer) {
                        val activeIncidents = incidents.filter { it.status == "ACTIVO" }
                        val tappedInc = activeIncidents.minByOrNull { inc ->
                            val pos = geoToScreen(GeoPoint(inc.latitude, inc.longitude), canvasSize, currentZoom, currentPan)
                            hypot(tapOffset.x - pos.x, tapOffset.y - pos.y)
                        }
                        if (tappedInc != null) {
                            val pos = geoToScreen(GeoPoint(tappedInc.latitude, tappedInc.longitude), canvasSize, currentZoom, currentPan)
                            if (hypot(tapOffset.x - pos.x, tapOffset.y - pos.y) <= 44f) {
                                onSelectIncident(tappedInc)
                                return@detectTapGestures
                            }
                        }
                    }

                    // 3. Check stops
                    if (showStopsLayer) {
                        val tappedStop = stops.minByOrNull { stop ->
                            val pos = geoToScreen(stop.location, canvasSize, currentZoom, currentPan)
                            hypot(tapOffset.x - pos.x, tapOffset.y - pos.y)
                        }
                        if (tappedStop != null) {
                            val pos = geoToScreen(tappedStop.location, canvasSize, currentZoom, currentPan)
                            if (hypot(tapOffset.x - pos.x, tapOffset.y - pos.y) <= 44f) {
                                onSelectStop(tappedStop)
                                return@detectTapGestures
                            }
                        }
                    }
                }
            }
    ) {
        canvasSize = size

        // 1. Draw Pacific Ocean Coastline (West / Southwest of Trujillo: Huanchaco -> Buenos Aires -> Salaverry)
        val oceanPath = Path().apply {
            val nw = geoToScreen(GeoPoint(-8.0600, -79.1300), size, currentZoom, currentPan)
            val huanchaco = geoToScreen(GeoPoint(-8.0780, -79.1210), size, currentZoom, currentPan)
            val buenosAires = geoToScreen(GeoPoint(-8.1450, -79.0580), size, currentZoom, currentPan)
            val salaverry = geoToScreen(GeoPoint(-8.2250, -78.9820), size, currentZoom, currentPan)
            val sw = geoToScreen(GeoPoint(-8.2300, -79.1300), size, currentZoom, currentPan)
            moveTo(nw.x, nw.y)
            lineTo(huanchaco.x, huanchaco.y)
            lineTo(buenosAires.x, buenosAires.y)
            lineTo(salaverry.x, salaverry.y)
            lineTo(sw.x, sw.y)
            close()
        }
        drawPath(oceanPath, color = Color(0xFFB3E5FC))

        // 2. Draw Subtle Urban Grid & Major Trujillo Arteries (Av. España Ring, Av. América Ring, Río Moche)
        val gridPaintColor = Color(0xFFD5E1EE)
        for (i in 1..9) {
            val lat = MIN_LAT + (MAX_LAT - MIN_LAT) * (i / 10.0)
            val start = geoToScreen(GeoPoint(lat, MIN_LNG), size, currentZoom, currentPan)
            val end = geoToScreen(GeoPoint(lat, MAX_LNG), size, currentZoom, currentPan)
            drawLine(gridPaintColor, start, end, strokeWidth = 1.2f)
        }
        for (i in 1..9) {
            val lng = MIN_LNG + (MAX_LNG - MIN_LNG) * (i / 10.0)
            val start = geoToScreen(GeoPoint(MIN_LAT, lng), size, currentZoom, currentPan)
            val end = geoToScreen(GeoPoint(MAX_LAT, lng), size, currentZoom, currentPan)
            drawLine(gridPaintColor, start, end, strokeWidth = 1.2f)
        }

        // Río Moche (Southern natural boundary)
        val rioMochePath = Path().apply {
            val p1 = geoToScreen(GeoPoint(-8.1600, -79.0480), size, currentZoom, currentPan)
            val p2 = geoToScreen(GeoPoint(-8.1550, -79.0050), size, currentZoom, currentPan)
            val p3 = geoToScreen(GeoPoint(-8.1200, -78.9550), size, currentZoom, currentPan)
            moveTo(p1.x, p1.y)
            quadraticTo(p2.x, p2.y, p3.x, p3.y)
        }
        drawPath(
            path = rioMochePath,
            color = Color(0xFF81D4FA),
            style = Stroke(width = 6f * currentZoom.coerceAtMost(2f), cap = StrokeCap.Round)
        )

        // Iconic Av. España Ring (Centro Histórico de Trujillo)
        val centroPos = geoToScreen(GeoPoint(-8.1117, -79.0287), size, currentZoom, currentPan)
        drawOval(
            color = Color(0xFFCBD5E1),
            topLeft = Offset(centroPos.x - 28f * currentZoom, centroPos.y - 22f * currentZoom),
            size = Size(56f * currentZoom, 44f * currentZoom),
            style = Stroke(width = 4.5f)
        )

        // District Labels on Map Canvas
        drawDistrictLabels(size, currentZoom, currentPan, ::geoToScreen)

        // 3. Draw Bus Route Polylines
        routes.forEach { route ->
            val isFocused = focusedRouteId == null || focusedRouteId == route.id
            val strokeWidth = if (focusedRouteId == route.id) 9f else if (isFocused) 4.5f else 2f
            val alpha = if (focusedRouteId == route.id) 0.95f else if (isFocused) 0.55f else 0.14f

            if (route.pathWaypoints.size >= 2) {
                val routePath = Path()
                route.pathWaypoints.forEachIndexed { idx, pt ->
                    val screenPt = geoToScreen(pt, size, currentZoom, currentPan)
                    if (idx == 0) routePath.moveTo(screenPt.x, screenPt.y)
                    else routePath.lineTo(screenPt.x, screenPt.y)
                }

                if (focusedRouteId == route.id) {
                    // White casing outline for focused route
                    drawPath(
                        path = routePath,
                        color = Color.White,
                        style = Stroke(width = strokeWidth + 5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                }

                drawPath(
                    path = routePath,
                    color = Color(route.primaryColorHex).copy(alpha = alpha),
                    style = Stroke(
                        width = strokeWidth,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round,
                        pathEffect = if (route.type.name == "COLECTIVO" && focusedRouteId != route.id) {
                            PathEffect.dashPathEffect(floatArrayOf(14f, 8f), 0f)
                        } else null
                    )
                )
            }
        }

        // 4. Draw Bus Stops Layer
        if (showStopsLayer) {
            stops.forEach { stop ->
                val isSelected = selectedStop?.id == stop.id
                val hasAlert = stop.id in alertStopIds
                val isOnFocusedRoute = focusedRouteId == null || focusedRouteId in stop.routeIds
                if (!isOnFocusedRoute && !isSelected && !hasAlert) return@forEach

                val pos = geoToScreen(stop.location, size, currentZoom, currentPan)
                if (hasAlert || isSelected) {
                    drawCircle(
                        color = if (hasAlert) Color(0xFFFFB300).copy(alpha = pulseAlpha) else Color(0xFF0D47A1).copy(alpha = pulseAlpha),
                        radius = pulseRadius,
                        center = pos
                    )
                }

                val stopRadius = if (isSelected) 13f else if (hasAlert) 11f else 8.5f
                drawCircle(
                    color = Color.White,
                    radius = stopRadius + 3f,
                    center = pos
                )
                drawCircle(
                    color = when {
                        isSelected -> Color(0xFF0D47A1)
                        hasAlert -> Color(0xFFFF8F00)
                        else -> Color(0xFF334155)
                    },
                    radius = stopRadius,
                    center = pos
                )
                drawCircle(
                    color = Color.White,
                    radius = stopRadius * 0.42f,
                    center = pos
                )
            }
        }

        // 5. Draw Road Incidents Layer
        if (showIncidentsLayer) {
            incidents.filter { it.status == "ACTIVO" }.forEach { incident ->
                val pos = geoToScreen(GeoPoint(incident.latitude, incident.longitude), size, currentZoom, currentPan)
                val cat = IncidentCategory.fromId(incident.categoryId)
                val isSelected = selectedIncident?.id == incident.id

                drawCircle(
                    color = Color(cat.colorHex).copy(alpha = pulseAlpha),
                    radius = if (isSelected) pulseRadius * 1.25f else pulseRadius * 0.9f,
                    center = pos
                )

                // Warning Diamond / Triangle badge
                val r = if (isSelected) 16f else 13f
                val triangle = Path().apply {
                    moveTo(pos.x, pos.y - r)
                    lineTo(pos.x + r, pos.y + r * 0.85f)
                    lineTo(pos.x - r, pos.y + r * 0.85f)
                    close()
                }
                drawPath(triangle, color = Color.White, style = Stroke(width = 4f))
                drawPath(triangle, color = Color(cat.colorHex))
                drawContext.canvas.nativeCanvas.apply {
                    val paint = android.graphics.Paint().apply {
                        color = android.graphics.Color.WHITE
                        textSize = 18f
                        textAlign = android.graphics.Paint.Align.CENTER
                        isFakeBoldText = true
                        isAntiAlias = true
                    }
                    drawText("!", pos.x, pos.y + 8f, paint)
                }
            }
        }

        // 6. Draw User GPS Position Marker
        val userPos = geoToScreen(userLocation, size, currentZoom, currentPan)
        drawCircle(
            color = Color(0xFF2979FF).copy(alpha = 0.22f),
            radius = 24f,
            center = userPos
        )
        drawCircle(
            color = Color.White,
            radius = 10f,
            center = userPos
        )
        drawCircle(
            color = Color(0xFF2979FF),
            radius = 7.5f,
            center = userPos
        )

        // 7. Draw Live Bus Units Layer
        if (showUnitsLayer) {
            val unitsToRender = if (focusedRouteId != null) {
                liveUnits.filter { it.routeId == focusedRouteId }
            } else {
                liveUnits
            }

            unitsToRender.forEach { unit ->
                val pos = geoToScreen(unit.currentLocation, size, currentZoom, currentPan)
                val isSelected = selectedUnit?.unitId == unit.unitId
                val unitColor = Color(unit.colorHex)

                if (isSelected) {
                    drawCircle(
                        color = unitColor.copy(alpha = pulseAlpha),
                        radius = pulseRadius * 1.3f,
                        center = pos
                    )
                }

                // Bus pill badge
                val pillWidth = if (isSelected) 76f else 64f
                val pillHeight = if (isSelected) 32f else 26f
                val topLeft = Offset(pos.x - pillWidth / 2f, pos.y - pillHeight / 2f)

                drawRoundRect(
                    color = Color.White,
                    topLeft = Offset(topLeft.x - 2.5f, topLeft.y - 2.5f),
                    size = Size(pillWidth + 5f, pillHeight + 5f),
                    cornerRadius = CornerRadius(14f, 14f)
                )
                drawRoundRect(
                    color = unitColor,
                    topLeft = topLeft,
                    size = Size(pillWidth, pillHeight),
                    cornerRadius = CornerRadius(12f, 12f)
                )

                drawContext.canvas.nativeCanvas.apply {
                    val textPaint = android.graphics.Paint().apply {
                        color = android.graphics.Color.WHITE
                        textSize = if (isSelected) 20f else 17f
                        textAlign = android.graphics.Paint.Align.CENTER
                        isFakeBoldText = true
                        isAntiAlias = true
                    }
                    drawText(unit.routeCode, pos.x, pos.y + 6f, textPaint)
                }
            }
        }
    }
}

private fun DrawScope.drawDistrictLabels(
    size: Size,
    zoom: Float,
    pan: Offset,
    geoToScreen: (GeoPoint, Size, Float, Offset) -> Offset
) {
    val districts = listOf(
        "TRUJILLO CENTRO" to GeoPoint(-8.1117, -79.0287),
        "LA ESPERANZA" to GeoPoint(-8.0750, -79.0430),
        "EL PORVENIR" to GeoPoint(-8.0810, -79.0020),
        "ALTO TRUJILLO" to GeoPoint(-8.0660, -78.9860),
        "VÍCTOR LARCO" to GeoPoint(-8.1410, -79.0520),
        "HUANCHACO" to GeoPoint(-8.0760, -79.1150),
        "MOCHE" to GeoPoint(-8.1720, -79.0090),
        "SALAVERRY" to GeoPoint(-8.2180, -78.9770),
        "LAREDO" to GeoPoint(-8.0890, -78.9610)
    )

    val labelPaint = android.graphics.Paint().apply {
        color = android.graphics.Color.argb(150, 30, 41, 59)
        textSize = (18f * zoom.coerceIn(0.9f, 1.4f))
        textAlign = android.graphics.Paint.Align.CENTER
        isFakeBoldText = true
        isAntiAlias = true
    }

    districts.forEach { (name, pt) ->
        val pos = geoToScreen(pt, size, zoom, pan)
        if (pos.x in 0f..size.width && pos.y in 0f..size.height) {
            drawContext.canvas.nativeCanvas.drawText(name, pos.x, pos.y - 16f, labelPaint)
        }
    }
}
