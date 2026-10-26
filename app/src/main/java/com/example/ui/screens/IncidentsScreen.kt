package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.IncidentEntity
import com.example.model.BusRoute
import com.example.model.GeoPoint
import com.example.model.IncidentCategory
import com.example.model.TrujilloReferenceSpot

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IncidentsScreen(
    incidents: List<IncidentEntity>,
    allRoutes: List<BusRoute>,
    referenceSpots: List<TrujilloReferenceSpot>,
    selectedCategory: IncidentCategory?,
    selectedDistrict: String?,
    onSelectCategory: (IncidentCategory?) -> Unit,
    onSelectDistrict: (String?) -> Unit,
    onSubmitIncident: (
        title: String,
        description: String,
        category: IncidentCategory,
        avenue: String,
        district: String,
        location: GeoPoint,
        reporter: String,
        affectedRoutes: List<String>
    ) -> Unit,
    onUpvoteIncident: (Int) -> Unit,
    onToggleIncidentStatus: (IncidentEntity) -> Unit,
    onViewIncidentOnMap: (IncidentEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var showReportDialog by remember { mutableStateOf(false) }
    val districts = remember {
        listOf("Trujillo", "La Esperanza", "El Porvenir", "Víctor Larco", "Huanchaco", "Moche", "Florencia de Mora")
    }

    val activeCount = incidents.count { it.status == "ACTIVO" }
    val resolvedCount = incidents.count { it.status == "RESUELTO" }
    val totalConfirmations = incidents.sumOf { it.upvotes }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showReportDialog = true },
                containerColor = Color(0xFFD32F2F),
                contentColor = Color.White,
                modifier = Modifier.testTag("open_report_incident_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.AddAlert, contentDescription = "Reportar Incidencia")
                    Text("Reportar Incidencia", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("incidents_feed_list"),
            contentPadding = PaddingValues(bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Header & KPI Summary
            item {
                Surface(
                    color = Color(0xFF0A2E6E),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Incidencias en la Vía Pública",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White
                        )
                        Text(
                            text = "Sistema colaborativo para reportar tráfico, obras viales, baches, desvíos y alertas que afectan el transporte en Trujillo.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            KpiStatBox(
                                value = "$activeCount",
                                label = "Alertas Activas",
                                accentColor = Color(0xFFFFB300),
                                modifier = Modifier.weight(1f)
                            )
                            KpiStatBox(
                                value = "$totalConfirmations",
                                label = "Confirmaciones",
                                accentColor = Color(0xFF4FC3F7),
                                modifier = Modifier.weight(1f)
                            )
                            KpiStatBox(
                                value = "$resolvedCount",
                                label = "Vías Despejadas",
                                accentColor = Color(0xFF00E676),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // 2. Category Filter Chips
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedCategory == null,
                            onClick = { onSelectCategory(null) },
                            label = { Text("Todas las categorías") }
                        )
                    }
                    items(IncidentCategory.entries) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { onSelectCategory(cat) },
                            label = { Text(cat.label) },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(Color(cat.colorHex))
                                )
                            },
                            modifier = Modifier.testTag("incident_cat_${cat.id}")
                        )
                    }
                }
            }

            // 3. District Filter Chips
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedDistrict == null,
                            onClick = { onSelectDistrict(null) },
                            label = { Text("Todo Trujillo") }
                        )
                    }
                    items(districts) { dist ->
                        FilterChip(
                            selected = selectedDistrict == dist,
                            onClick = { onSelectDistrict(dist) },
                            label = { Text(dist) }
                        )
                    }
                }
            }

            // 4. Incident Cards
            items(incidents, key = { it.id }) { incident ->
                IncidentReportCard(
                    incident = incident,
                    onUpvote = { onUpvoteIncident(incident.id) },
                    onToggleStatus = { onToggleIncidentStatus(incident) },
                    onViewOnMap = { onViewIncidentOnMap(incident) },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }

    if (showReportDialog) {
        ReportIncidentModalDialog(
            allRoutes = allRoutes,
            referenceSpots = referenceSpots,
            onDismiss = { showReportDialog = false },
            onSubmit = { title, desc, cat, ave, dist, loc, rep, routes ->
                onSubmitIncident(title, desc, cat, ave, dist, loc, rep, routes)
                showReportDialog = false
            }
        )
    }
}

@Composable
private fun KpiStatBox(
    value: String,
    label: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color.White.copy(alpha = 0.12f),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun IncidentReportCard(
    incident: IncidentEntity,
    onUpvote: () -> Unit,
    onToggleStatus: () -> Unit,
    onViewOnMap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val category = IncidentCategory.fromId(incident.categoryId)
    val isResolved = incident.status == "RESUELTO"
    val minutesAgo = ((System.currentTimeMillis() - incident.createdAtTimestamp) / 60000L).coerceAtLeast(1L)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("incident_card_${incident.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isResolved)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isResolved) 0.dp else 2.dp)
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
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = if (isResolved) Color(0xFF2E7D32) else Color(category.colorHex),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = if (isResolved) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = if (isResolved) "VÍA DESPEJADA" else category.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    if (incident.isVerified) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Verificado por la comunidad",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "Verificado",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Text(
                    text = if (minutesAgo < 60) "Hace $minutesAgo min" else "Hace ${minutesAgo / 60} h",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = incident.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "${incident.avenueOrReference} · Distrito de ${incident.district}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Text(
                text = incident.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (incident.affectedRouteCodes.isNotBlank()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Rutas afectadas:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                    incident.affectedRouteCodes.split(",").map { it.trim() }.filter { it.isNotEmpty() }.forEach { code ->
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = code,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Por: ${incident.reporterName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onUpvote,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("upvote_incident_${incident.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ThumbUp,
                            contentDescription = "Confirmar incidencia",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Sigue ahí (${incident.upvotes})", style = MaterialTheme.typography.labelSmall)
                    }

                    OutlinedButton(
                        onClick = onToggleStatus,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("resolve_incident_${incident.id}")
                    ) {
                        Text(
                            text = if (isResolved) "Reabrir" else "Despejada",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }

                    Button(
                        onClick = onViewOnMap,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.Map, contentDescription = "Ver en mapa", modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ReportIncidentModalDialog(
    allRoutes: List<BusRoute>,
    referenceSpots: List<TrujilloReferenceSpot>,
    onDismiss: () -> Unit,
    onSubmit: (
        title: String,
        description: String,
        category: IncidentCategory,
        avenue: String,
        district: String,
        location: GeoPoint,
        reporter: String,
        affectedRoutes: List<String>
    ) -> Unit
) {
    var selectedCategory by remember { mutableStateOf(IncidentCategory.TRAFICO) }
    var selectedSpot by remember { mutableStateOf(referenceSpots.first()) }
    var title by remember { mutableStateOf("") }
    var avenueReference by remember { mutableStateOf(referenceSpots.first().name) }
    var description by remember { mutableStateOf("") }
    var reporterName by remember { mutableStateOf("") }
    val selectedRouteCodes = remember { mutableStateListOf<String>() }
    var validationError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Reportar Incidencia en la Vía Pública",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "1. Tipo de incidencia:",
                    style = MaterialTheme.typography.labelLarge
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    IncidentCategory.entries.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat.label, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                Text(
                    text = "2. Zona / Punto en el mapa de Trujillo:",
                    style = MaterialTheme.typography.labelLarge
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(referenceSpots, key = { it.id }) { spot ->
                        FilterChip(
                            selected = selectedSpot.id == spot.id,
                            onClick = {
                                selectedSpot = spot
                                avenueReference = spot.name
                            },
                            label = { Text(spot.name, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        validationError = null
                    },
                    label = { Text("Título corto (ej. Tráfico en Óvalo Papal)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("incident_title_input")
                )

                OutlinedTextField(
                    value = avenueReference,
                    onValueChange = { avenueReference = it },
                    label = { Text("Avenida o cuadra exacta") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = {
                        description = it
                        validationError = null
                    },
                    label = { Text("Descripción detallada para conductores y pasajeros") },
                    minLines = 2,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("incident_description_input")
                )

                Text(
                    text = "3. Rutas de micro/colectivo afectadas (opcional):",
                    style = MaterialTheme.typography.labelLarge
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    allRoutes.take(8).forEach { route ->
                        val isPicked = route.code in selectedRouteCodes
                        FilterChip(
                            selected = isPicked,
                            onClick = {
                                if (isPicked) selectedRouteCodes.remove(route.code)
                                else selectedRouteCodes.add(route.code)
                            },
                            label = { Text(route.code, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                OutlinedTextField(
                    value = reporterName,
                    onValueChange = { reporterName = it },
                    label = { Text("Tu nombre o alias (opcional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (validationError != null) {
                    Text(
                        text = validationError ?: "",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) {
                        validationError = "Por favor ingresa un título para la incidencia."
                        return@Button
                    }
                    val finalDescription = description.ifBlank { selectedCategory.descriptionHint }
                    onSubmit(
                        title,
                        finalDescription,
                        selectedCategory,
                        avenueReference.ifBlank { selectedSpot.name },
                        selectedSpot.district,
                        selectedSpot.location,
                        reporterName,
                        selectedRouteCodes.toList()
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                modifier = Modifier.testTag("submit_incident_confirm_btn")
            ) {
                Text("Publicar Alerta Vial")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
