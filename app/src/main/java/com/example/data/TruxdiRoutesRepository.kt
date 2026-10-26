package com.example.data

import com.example.data.local.IncidentEntity
import com.example.data.local.SavedTransitItemEntity
import com.example.data.local.TransitDao
import com.example.model.BusRoute
import com.example.model.BusStop
import com.example.model.GeoPoint
import com.example.model.RouteType
import com.example.model.TrujilloReferenceSpot
import kotlinx.coroutines.flow.Flow

class TruxdiRoutesRepository(private val transitDao: TransitDao) {

    val incidentsFlow: Flow<List<IncidentEntity>> = transitDao.observeAllIncidents()
    val savedItemsFlow: Flow<List<SavedTransitItemEntity>> = transitDao.observeSavedItems()

    val referenceSpots: List<TrujilloReferenceSpot> = listOf(
        TrujilloReferenceSpot("spot_ovalo_papal", "Óvalo Papal (Av. Juan Pablo II)", "Trujillo", GeoPoint(-8.1225, -79.0385, "Óvalo Papal")),
        TrujilloReferenceSpot("spot_centro", "Plaza de Armas / Av. España", "Trujillo Centro", GeoPoint(-8.1117, -79.0287, "Centro Histórico")),
        TrujilloReferenceSpot("spot_unt", "Universidad Nacional de Trujillo (UNT)", "Trujillo", GeoPoint(-8.1150, -79.0380, "UNT Puerta 1")),
        TrujilloReferenceSpot("spot_upao", "UPAO - Av. América Sur", "Trujillo", GeoPoint(-8.1270, -79.0310, "Campus UPAO")),
        TrujilloReferenceSpot("spot_mallplaza", "Mallplaza / Av. Mansiche", "Trujillo", GeoPoint(-8.1018, -79.0435, "Mallplaza Trujillo")),
        TrujilloReferenceSpot("spot_hermelinda", "Mercado La Hermelinda", "Trujillo Norte", GeoPoint(-8.0935, -79.0135, "La Hermelinda")),
        TrujilloReferenceSpot("spot_esperanza", "Plaza de La Esperanza", "La Esperanza", GeoPoint(-8.0785, -79.0430, "La Esperanza")),
        TrujilloReferenceSpot("spot_porvenir", "Av. Pumacahua / El Porvenir", "El Porvenir", GeoPoint(-8.0835, -79.0020, "El Porvenir")),
        TrujilloReferenceSpot("spot_huanchaco", "Balneario de Huanchaco (El Boquerón)", "Huanchaco", GeoPoint(-8.0780, -79.1195, "Huanchaco"))
    )

    val officialStops: List<BusStop> = listOf(
        BusStop(
            id = "s_ovalo_papal",
            name = "Paradero Óvalo Papal",
            avenue = "Av. Juan Pablo II cdra. 5 / Av. América Oeste",
            district = "Trujillo",
            location = GeoPoint(-8.1225, -79.0385, "Óvalo Papal"),
            routeIds = listOf("r_m36_a", "r_m04_c", "r_c13_d", "r_m26_b", "r_m14_i", "r_col_huanchaco"),
            landmark = "Frente a Plaza Vea El Chacarero y monumento al Papa",
            hasRoofShelter = true
        ),
        BusStop(
            id = "s_unt_puerta1",
            name = "Paradero Ciudad Universitaria UNT",
            avenue = "Av. Juan Pablo II s/n",
            district = "Trujillo",
            location = GeoPoint(-8.1150, -79.0380, "UNT Puerta Principal"),
            routeIds = listOf("r_m36_a", "r_c13_d", "r_m18_l", "r_m14_i", "r_col_huanchaco"),
            landmark = "Puerta Principal Universidad Nacional de Trujillo",
            hasRoofShelter = true
        ),
        BusStop(
            id = "s_av_espana_almagro",
            name = "Paradero Av. España - Centro Histórico",
            avenue = "Av. España cdra. 14 esq. Jr. Almagro",
            district = "Trujillo",
            location = GeoPoint(-8.1105, -79.0315, "Av. España / Almagro"),
            routeIds = listOf("r_m36_a", "r_m02_h", "r_m04_c", "r_m26_b", "r_m10_sal", "r_m18_l"),
            landmark = "A 3 cuadras de la Plaza de Armas de Trujillo",
            hasRoofShelter = true
        ),
        BusStop(
            id = "s_zona_franca",
            name = "Paradero Av. España - Zona Franca",
            avenue = "Av. España cdra. 21 (Ex-Estación Ferrocarril)",
            district = "Trujillo",
            location = GeoPoint(-8.1155, -79.0225, "Zona Franca / Av. España"),
            routeIds = listOf("r_m02_h", "r_m04_c", "r_m26_b", "r_m10_sal", "r_col_laredo"),
            landmark = "Centro Comercial Zona Franca y Alameda del Calzado",
            hasRoofShelter = true
        ),
        BusStop(
            id = "s_ovalo_larco",
            name = "Paradero Óvalo Larco",
            avenue = "Av. Víctor Larco cdra. 10 / Av. América Sur",
            district = "Trujillo",
            location = GeoPoint(-8.1280, -79.0355, "Óvalo Larco"),
            routeIds = listOf("r_m36_a", "r_c13_d", "r_m26_b", "r_m14_i"),
            landmark = "Cruce hacia Urb. La Merced y Av. Larco",
            hasRoofShelter = true
        ),
        BusStop(
            id = "s_upao_america",
            name = "Paradero UPAO América Sur",
            avenue = "Av. América Sur cdra. 31",
            district = "Trujillo",
            location = GeoPoint(-8.1270, -79.0310, "UPAO América Sur"),
            routeIds = listOf("r_m36_a", "r_c44_t2", "r_m18_l", "r_m14_i"),
            landmark = "Puerta principal Universidad Privada Antenor Orrego",
            hasRoofShelter = true
        ),
        BusStop(
            id = "s_ovalo_grau",
            name = "Paradero Óvalo Grau",
            avenue = "Av. América Sur / Av. La Marina",
            district = "Trujillo",
            location = GeoPoint(-8.1240, -79.0220, "Óvalo Grau"),
            routeIds = listOf("r_m02_h", "r_c44_t2", "r_m10_sal", "r_m18_l", "r_col_moche"),
            landmark = "Salida hacia Moche, Salaverry y Terrapuerto",
            hasRoofShelter = true
        ),
        BusStop(
            id = "s_mallplaza_mansiche",
            name = "Paradero Mallplaza Mansiche",
            avenue = "Av. Mansiche cdra. 18",
            district = "Trujillo",
            location = GeoPoint(-8.1018, -79.0435, "Mallplaza Mansiche"),
            routeIds = listOf("r_m02_h", "r_m10_sal", "r_m18_l", "r_col_huanchaco"),
            landmark = "Frente a Mallplaza y Hospital Regional Docente",
            hasRoofShelter = true
        ),
        BusStop(
            id = "s_hermelinda",
            name = "Paradero Mercado La Hermelinda",
            avenue = "Av. Federico Villarreal cdra. 12",
            district = "Trujillo",
            location = GeoPoint(-8.0935, -79.0135, "Mercado La Hermelinda"),
            routeIds = listOf("r_m04_c", "r_c13_d", "r_m26_b", "r_m14_i"),
            landmark = "Sector Mayorista La Hermelinda y Av. Villarreal",
            hasRoofShelter = false
        ),
        BusStop(
            id = "s_esperanza_arco",
            name = "Paradero Arco de La Esperanza",
            avenue = "Av. José Gabriel Condorcanqui cdra. 11",
            district = "La Esperanza",
            location = GeoPoint(-8.0785, -79.0430, "Arco La Esperanza"),
            routeIds = listOf("r_m36_a", "r_m04_c", "r_m18_l"),
            landmark = "Ingreso principal al distrito de La Esperanza y Hospital Jerusalén",
            hasRoofShelter = true
        ),
        BusStop(
            id = "s_cortijo_final",
            name = "Paradero El Cortijo - Parte Alta",
            avenue = "Av. Tahuantinsuyo / Sector El Cortijo",
            district = "La Esperanza",
            location = GeoPoint(-8.0680, -79.0510, "El Cortijo"),
            routeIds = listOf("r_m04_c"),
            landmark = "Terminal Empresa El Cortijo S.A.",
            hasRoofShelter = false
        ),
        BusStop(
            id = "s_porvenir_pumacahua",
            name = "Paradero Av. Pumacahua Central",
            avenue = "Av. Mateo Pumacahua cdra. 15",
            district = "El Porvenir",
            location = GeoPoint(-8.0835, -79.0020, "Av. Pumacahua"),
            routeIds = listOf("r_c44_t2", "r_m26_b", "r_col_laredo"),
            landmark = "Zona comercial de calzado El Porvenir",
            hasRoofShelter = true
        ),
        BusStop(
            id = "s_alto_trujillo",
            name = "Paradero Barrio 3 - Alto Trujillo",
            avenue = "Av. Principal Alto Trujillo",
            district = "Alto Trujillo",
            location = GeoPoint(-8.0675, -78.9860, "Alto Trujillo B3"),
            routeIds = listOf("r_c13_d", "r_m26_b"),
            landmark = "Plaza Cívica de Alto Trujillo",
            hasRoofShelter = false
        ),
        BusStop(
            id = "s_florencia_mora",
            name = "Paradero 26 de Marzo - Florencia de Mora",
            avenue = "Av. 26 de Marzo cdra. 8",
            district = "Florencia de Mora",
            location = GeoPoint(-8.0860, -79.0210, "Florencia de Mora"),
            routeIds = listOf("r_c13_d", "r_m14_i"),
            landmark = "Cerca a Municipalidad de Florencia de Mora",
            hasRoofShelter = false
        ),
        BusStop(
            id = "s_nuevo_california",
            name = "Paradero Nuevo California / El Golf",
            avenue = "Prolongación César Vallejo",
            district = "Víctor Larco",
            location = GeoPoint(-8.1335, -79.0390, "Paradero Nuevo California"),
            routeIds = listOf("r_m36_a", "r_m04_c"),
            landmark = "Cerca a Real Plaza Trujillo y Urb. El Golf",
            hasRoofShelter = true
        ),
        BusStop(
            id = "s_buenos_aires",
            name = "Paradero Buenos Aires / Vista Alegre",
            avenue = "Av. Larco cdra. 18 - Municipalidad Víctor Larco",
            district = "Víctor Larco",
            location = GeoPoint(-8.1430, -79.0545, "Buenos Aires / Vista Alegre"),
            routeIds = listOf("r_m36_a", "r_c13_d", "r_m26_b", "r_m14_i"),
            landmark = "Plaza Vista Alegre y Malecón de Buenos Aires",
            hasRoofShelter = true
        ),
        BusStop(
            id = "s_huanchaco_boqueron",
            name = "Paradero El Boquerón - Huanchaco",
            avenue = "Av. La Rivera cdra. 3",
            district = "Huanchaco",
            location = GeoPoint(-8.0780, -79.1195, "Muelle de Huanchaco"),
            routeIds = listOf("r_m02_h", "r_col_huanchaco"),
            landmark = "Frente al Muelle Histórico y Caballitos de Totora",
            hasRoofShelter = true
        ),
        BusStop(
            id = "s_terrapuerto",
            name = "Paradero Terrapuerto de Trujillo",
            avenue = "Panamericana Norte Km 558",
            district = "Moche",
            location = GeoPoint(-8.1460, -79.0110, "Terrapuerto Trujillo"),
            routeIds = listOf("r_c44_t2", "r_m10_sal", "r_col_moche"),
            landmark = "Terminal Terrestre Interprovincial de Trujillo",
            hasRoofShelter = true
        ),
        BusStop(
            id = "s_moche_plaza",
            name = "Paradero Plaza de Armas de Moche",
            avenue = "Calle Diego de Mora / Panamericana Sur",
            district = "Moche",
            location = GeoPoint(-8.1710, -79.0090, "Moche Centro"),
            routeIds = listOf("r_c44_t2", "r_m10_sal", "r_col_moche"),
            landmark = "Ingreso a Campiña de Moche y Huacas del Sol y la Luna",
            hasRoofShelter = true
        ),
        BusStop(
            id = "s_salaverry_puerto",
            name = "Paradero Puerto de Salaverry",
            avenue = "Calle Aduana / Plaza de Salaverry",
            district = "Salaverry",
            location = GeoPoint(-8.2210, -78.9770, "Puerto Salaverry"),
            routeIds = listOf("r_m10_sal"),
            landmark = "Terminal Portuario de Salaverry y Malecón",
            hasRoofShelter = true
        ),
        BusStop(
            id = "s_laredo_plaza",
            name = "Paradero Laredo Centro",
            avenue = "Av. Julián Arce / Plaza de Laredo",
            district = "Laredo",
            location = GeoPoint(-8.0890, -78.9610, "Laredo Centro"),
            routeIds = listOf("r_col_laredo"),
            landmark = "Planta Agroindustrial Laredo y Plaza Principal",
            hasRoofShelter = true
        )
    )

    val officialRoutes: List<BusRoute> = listOf(
        BusRoute(
            id = "r_m36_a",
            code = "M-36 A",
            shortName = "La A",
            companyName = "E.T. Nuevo California S.A. (ETNCASA)",
            type = RouteType.MICRO,
            originDistrict = "La Esperanza",
            destinationDistrict = "Víctor Larco (Buenos Aires)",
            fareSoles = 2.50,
            studentFareSoles = 1.00,
            universityFareSoles = 1.50,
            frequencyMinutes = 4,
            estimatedDurationMinutes = 42,
            operatingHours = "05:00 AM - 10:30 PM",
            primaryColorHex = 0xFFD32F2F,
            secondaryColorHex = 0xFFFFFFFF,
            keyStreets = listOf(
                "Av. Condorcanqui (La Esperanza)",
                "Av. América Norte",
                "Av. España (Centro Histórico)",
                "Av. Juan Pablo II (UNT / Óvalo Papal)",
                "Av. América Sur (UPAO)",
                "Prol. César Vallejo (Real Plaza)",
                "Av. Víctor Larco (Buenos Aires)"
            ),
            stopIds = listOf(
                "s_esperanza_arco",
                "s_av_espana_almagro",
                "s_unt_puerta1",
                "s_ovalo_papal",
                "s_upao_america",
                "s_nuevo_california",
                "s_ovalo_larco",
                "s_buenos_aires"
            ),
            pathWaypoints = listOf(
                GeoPoint(-8.0785, -79.0430, "Arco La Esperanza"),
                GeoPoint(-8.0950, -79.0370, "Av. Túpac Amaru"),
                GeoPoint(-8.1105, -79.0315, "Av. España"),
                GeoPoint(-8.1150, -79.0380, "UNT"),
                GeoPoint(-8.1225, -79.0385, "Óvalo Papal"),
                GeoPoint(-8.1270, -79.0310, "UPAO"),
                GeoPoint(-8.1335, -79.0390, "Nuevo California"),
                GeoPoint(-8.1280, -79.0355, "Óvalo Larco"),
                GeoPoint(-8.1430, -79.0545, "Buenos Aires")
            )
        ),
        BusRoute(
            id = "r_m02_h",
            code = "M-02 H",
            shortName = "Huanchaco H",
            companyName = "Empresa de Transporte Huanchaco S.A. (ETHSA)",
            type = RouteType.MICRO,
            originDistrict = "Huanchaco",
            destinationDistrict = "Trujillo (Óvalo Grau / La Perla)",
            fareSoles = 2.50,
            studentFareSoles = 1.00,
            universityFareSoles = 1.50,
            frequencyMinutes = 6,
            estimatedDurationMinutes = 48,
            operatingHours = "05:15 AM - 10:00 PM",
            primaryColorHex = 0xFF0288D1,
            secondaryColorHex = 0xFFFFB300,
            keyStreets = listOf(
                "Av. La Rivera (Balneario Huanchaco)",
                "Vía Evitamiento / Aeropuerto",
                "Av. Mansiche (Mallplaza / Hospital Regional)",
                "Av. España (Centro Histórico / Zona Franca)",
                "Av. América Sur / Óvalo Grau"
            ),
            stopIds = listOf(
                "s_huanchaco_boqueron",
                "s_mallplaza_mansiche",
                "s_av_espana_almagro",
                "s_zona_franca",
                "s_ovalo_grau"
            ),
            pathWaypoints = listOf(
                GeoPoint(-8.0780, -79.1195, "Muelle Huanchaco"),
                GeoPoint(-8.0890, -79.0820, "Cruce Aeropuerto"),
                GeoPoint(-8.1018, -79.0435, "Mallplaza Mansiche"),
                GeoPoint(-8.1105, -79.0315, "Av. España Centro"),
                GeoPoint(-8.1155, -79.0225, "Zona Franca"),
                GeoPoint(-8.1240, -79.0220, "Óvalo Grau")
            )
        ),
        BusRoute(
            id = "r_m04_c",
            code = "M-04 C",
            shortName = "El Cortijo",
            companyName = "E.T. El Cortijo S.A. (ETECSA)",
            type = RouteType.MICRO,
            originDistrict = "La Esperanza (El Cortijo)",
            destinationDistrict = "Víctor Larco (El Golf)",
            fareSoles = 2.20,
            studentFareSoles = 1.00,
            universityFareSoles = 1.30,
            frequencyMinutes = 5,
            estimatedDurationMinutes = 40,
            operatingHours = "05:00 AM - 10:15 PM",
            primaryColorHex = 0xFF2E7D32,
            secondaryColorHex = 0xFFFFFFFF,
            keyStreets = listOf(
                "Sector El Cortijo (La Esperanza)",
                "Av. Condorcanqui",
                "Av. Villarreal (Mercado La Hermelinda)",
                "Av. España (Zona Franca / Centro)",
                "Óvalo Papal (Av. Juan Pablo II)",
                "Prol. César Vallejo (Nuevo California)"
            ),
            stopIds = listOf(
                "s_cortijo_final",
                "s_esperanza_arco",
                "s_hermelinda",
                "s_zona_franca",
                "s_av_espana_almagro",
                "s_ovalo_papal",
                "s_nuevo_california"
            ),
            pathWaypoints = listOf(
                GeoPoint(-8.0680, -79.0510, "El Cortijo"),
                GeoPoint(-8.0785, -79.0430, "Arco La Esperanza"),
                GeoPoint(-8.0935, -79.0135, "La Hermelinda"),
                GeoPoint(-8.1155, -79.0225, "Zona Franca"),
                GeoPoint(-8.1105, -79.0315, "Av. España"),
                GeoPoint(-8.1225, -79.0385, "Óvalo Papal"),
                GeoPoint(-8.1335, -79.0390, "Nuevo California")
            )
        ),
        BusRoute(
            id = "r_c13_d",
            code = "C-13 D",
            shortName = "La D Mochica",
            companyName = "Empresa de Transportes Mochica S.A.",
            type = RouteType.COMBI,
            originDistrict = "Alto Trujillo / Florencia",
            destinationDistrict = "Víctor Larco (Buenos Aires)",
            fareSoles = 2.50,
            studentFareSoles = 1.00,
            universityFareSoles = 1.50,
            frequencyMinutes = 5,
            estimatedDurationMinutes = 35,
            operatingHours = "05:30 AM - 10:00 PM",
            primaryColorHex = 0xFF7B1FA2,
            secondaryColorHex = 0xFFFFEB3B,
            keyStreets = listOf(
                "Barrio 3 Alto Trujillo",
                "Av. 26 de Marzo (Florencia de Mora)",
                "Av. Federico Villarreal (La Hermelinda)",
                "Av. América Norte",
                "Av. Juan Pablo II (UNT / Óvalo Papal)",
                "Av. Víctor Larco (Óvalo Larco - Buenos Aires)"
            ),
            stopIds = listOf(
                "s_alto_trujillo",
                "s_florencia_mora",
                "s_hermelinda",
                "s_unt_puerta1",
                "s_ovalo_papal",
                "s_ovalo_larco",
                "s_buenos_aires"
            ),
            pathWaypoints = listOf(
                GeoPoint(-8.0675, -78.9860, "Alto Trujillo"),
                GeoPoint(-8.0860, -79.0210, "Florencia de Mora"),
                GeoPoint(-8.0935, -79.0135, "La Hermelinda"),
                GeoPoint(-8.1150, -79.0380, "UNT"),
                GeoPoint(-8.1225, -79.0385, "Óvalo Papal"),
                GeoPoint(-8.1280, -79.0355, "Óvalo Larco"),
                GeoPoint(-8.1430, -79.0545, "Buenos Aires")
            )
        ),
        BusRoute(
            id = "r_c44_t2",
            code = "C-44 T2",
            shortName = "Miramar T2",
            companyName = "E.T. Señor de los Milagros - Miramar S.A.",
            type = RouteType.COMBI,
            originDistrict = "El Porvenir",
            destinationDistrict = "Moche / Las Delicias",
            fareSoles = 2.50,
            studentFareSoles = 1.00,
            universityFareSoles = 1.50,
            frequencyMinutes = 7,
            estimatedDurationMinutes = 45,
            operatingHours = "05:20 AM - 09:45 PM",
            primaryColorHex = 0xFFF57C00,
            secondaryColorHex = 0xFFFFFFFF,
            keyStreets = listOf(
                "Av. Mateo Pumacahua (El Porvenir)",
                "Av. América Sur (UPAO)",
                "Óvalo Grau",
                "Panamericana Sur (Terrapuerto)",
                "Cruce Moche / Plaza de Armas de Moche"
            ),
            stopIds = listOf(
                "s_porvenir_pumacahua",
                "s_upao_america",
                "s_ovalo_grau",
                "s_terrapuerto",
                "s_moche_plaza"
            ),
            pathWaypoints = listOf(
                GeoPoint(-8.0835, -79.0020, "El Porvenir"),
                GeoPoint(-8.1080, -79.0160, "Av. Eguren"),
                GeoPoint(-8.1270, -79.0310, "UPAO"),
                GeoPoint(-8.1240, -79.0220, "Óvalo Grau"),
                GeoPoint(-8.1460, -79.0110, "Terrapuerto"),
                GeoPoint(-8.1710, -79.0090, "Plaza de Moche")
            )
        ),
        BusRoute(
            id = "r_m26_b",
            code = "M-26 B",
            shortName = "La B",
            companyName = "Empresa Alto Trujillo S.A.",
            type = RouteType.MICRO,
            originDistrict = "Alto Trujillo",
            destinationDistrict = "Víctor Larco (Vista Alegre)",
            fareSoles = 2.50,
            studentFareSoles = 1.00,
            universityFareSoles = 1.50,
            frequencyMinutes = 5,
            estimatedDurationMinutes = 44,
            operatingHours = "05:00 AM - 10:15 PM",
            primaryColorHex = 0xFF00796B,
            secondaryColorHex = 0xFFFFF176,
            keyStreets = listOf(
                "Alto Trujillo Barrio 3",
                "Av. Pumacahua (El Porvenir)",
                "Av. Villarreal (La Hermelinda)",
                "Av. España (Zona Franca / Centro)",
                "Óvalo Papal",
                "Av. Larco (Vista Alegre)"
            ),
            stopIds = listOf(
                "s_alto_trujillo",
                "s_porvenir_pumacahua",
                "s_hermelinda",
                "s_zona_franca",
                "s_av_espana_almagro",
                "s_ovalo_papal",
                "s_ovalo_larco",
                "s_buenos_aires"
            ),
            pathWaypoints = listOf(
                GeoPoint(-8.0675, -78.9860, "Alto Trujillo"),
                GeoPoint(-8.0835, -79.0020, "El Porvenir"),
                GeoPoint(-8.0935, -79.0135, "La Hermelinda"),
                GeoPoint(-8.1155, -79.0225, "Zona Franca"),
                GeoPoint(-8.1105, -79.0315, "Av. España"),
                GeoPoint(-8.1225, -79.0385, "Óvalo Papal"),
                GeoPoint(-8.1280, -79.0355, "Óvalo Larco"),
                GeoPoint(-8.1430, -79.0545, "Buenos Aires")
            )
        ),
        BusRoute(
            id = "r_m10_sal",
            code = "M-10 S",
            shortName = "Salaverry",
            companyName = "Salaverry Express S.A.",
            type = RouteType.MICRO,
            originDistrict = "Salaverry",
            destinationDistrict = "Trujillo (Mallplaza Mansiche)",
            fareSoles = 3.00,
            studentFareSoles = 1.50,
            universityFareSoles = 2.00,
            frequencyMinutes = 8,
            estimatedDurationMinutes = 50,
            operatingHours = "05:30 AM - 09:30 PM",
            primaryColorHex = 0xFF1565C0,
            secondaryColorHex = 0xFFFFFFFF,
            keyStreets = listOf(
                "Puerto de Salaverry",
                "Autopista Salaverry - Trujillo",
                "Cruce Moche / Terrapuerto",
                "Óvalo Grau",
                "Av. España (Zona Franca / Centro)",
                "Av. Mansiche (Mallplaza)"
            ),
            stopIds = listOf(
                "s_salaverry_puerto",
                "s_moche_plaza",
                "s_terrapuerto",
                "s_ovalo_grau",
                "s_zona_franca",
                "s_av_espana_almagro",
                "s_mallplaza_mansiche"
            ),
            pathWaypoints = listOf(
                GeoPoint(-8.2210, -78.9770, "Puerto Salaverry"),
                GeoPoint(-8.1710, -79.0090, "Cruce Moche"),
                GeoPoint(-8.1460, -79.0110, "Terrapuerto"),
                GeoPoint(-8.1240, -79.0220, "Óvalo Grau"),
                GeoPoint(-8.1155, -79.0225, "Zona Franca"),
                GeoPoint(-8.1105, -79.0315, "Av. España"),
                GeoPoint(-8.1018, -79.0435, "Mallplaza Mansiche")
            )
        ),
        BusRoute(
            id = "r_m18_l",
            code = "M-18 L",
            shortName = "Correcaminos",
            companyName = "E.T. Correcaminos S.A.",
            type = RouteType.MICRO,
            originDistrict = "La Esperanza",
            destinationDistrict = "Trujillo Sur (Óvalo Grau)",
            fareSoles = 2.20,
            studentFareSoles = 1.00,
            universityFareSoles = 1.30,
            frequencyMinutes = 6,
            estimatedDurationMinutes = 36,
            operatingHours = "05:15 AM - 10:00 PM",
            primaryColorHex = 0xFF00838F,
            secondaryColorHex = 0xFFFFCA28,
            keyStreets = listOf(
                "Arco de La Esperanza",
                "Av. Mansiche (Mallplaza)",
                "Av. Juan Pablo II (UNT)",
                "Av. España",
                "Av. América Sur (UPAO)",
                "Óvalo Grau"
            ),
            stopIds = listOf(
                "s_esperanza_arco",
                "s_mallplaza_mansiche",
                "s_unt_puerta1",
                "s_av_espana_almagro",
                "s_upao_america",
                "s_ovalo_grau"
            ),
            pathWaypoints = listOf(
                GeoPoint(-8.0785, -79.0430, "La Esperanza"),
                GeoPoint(-8.1018, -79.0435, "Mallplaza Mansiche"),
                GeoPoint(-8.1150, -79.0380, "UNT"),
                GeoPoint(-8.1105, -79.0315, "Av. España"),
                GeoPoint(-8.1270, -79.0310, "UPAO"),
                GeoPoint(-8.1240, -79.0220, "Óvalo Grau")
            )
        ),
        BusRoute(
            id = "r_m14_i",
            code = "M-14 I",
            shortName = "Ícaro",
            companyName = "Empresa de Transportes Ícaro S.A.",
            type = RouteType.MICRO,
            originDistrict = "Florencia de Mora",
            destinationDistrict = "Víctor Larco (Buenos Aires)",
            fareSoles = 2.20,
            studentFareSoles = 1.00,
            universityFareSoles = 1.30,
            frequencyMinutes = 6,
            estimatedDurationMinutes = 38,
            operatingHours = "05:30 AM - 10:00 PM",
            primaryColorHex = 0xFFC2185B,
            secondaryColorHex = 0xFFFFFFFF,
            keyStreets = listOf(
                "Av. 26 de Marzo (Florencia de Mora)",
                "Mercado La Hermelinda",
                "Av. América Sur (UPAO)",
                "Óvalo Papal / UNT",
                "Óvalo Larco / Buenos Aires"
            ),
            stopIds = listOf(
                "s_florencia_mora",
                "s_hermelinda",
                "s_upao_america",
                "s_ovalo_papal",
                "s_unt_puerta1",
                "s_ovalo_larco",
                "s_buenos_aires"
            ),
            pathWaypoints = listOf(
                GeoPoint(-8.0860, -79.0210, "Florencia de Mora"),
                GeoPoint(-8.0935, -79.0135, "La Hermelinda"),
                GeoPoint(-8.1270, -79.0310, "UPAO"),
                GeoPoint(-8.1225, -79.0385, "Óvalo Papal"),
                GeoPoint(-8.1150, -79.0380, "UNT"),
                GeoPoint(-8.1280, -79.0355, "Óvalo Larco"),
                GeoPoint(-8.1430, -79.0545, "Buenos Aires")
            )
        ),
        BusRoute(
            id = "r_col_huanchaco",
            code = "COL-H1",
            shortName = "Colectivo Huanchaco",
            companyName = "Asoc. Colectivos Trujillo - Huanchaco Express",
            type = RouteType.COLECTIVO,
            originDistrict = "Trujillo (Óvalo Papal / Mansiche)",
            destinationDistrict = "Huanchaco (El Boquerón)",
            fareSoles = 3.50,
            studentFareSoles = 3.00,
            universityFareSoles = 3.00,
            frequencyMinutes = 4,
            estimatedDurationMinutes = 20,
            operatingHours = "05:30 AM - 11:00 PM",
            primaryColorHex = 0xFFF9A825,
            secondaryColorHex = 0xFF0D47A1,
            keyStreets = listOf(
                "Óvalo Papal / Av. Juan Pablo II",
                "UNT Puerta Principal",
                "Av. Mansiche (Mallplaza)",
                "Autopista a Huanchaco / Aeropuerto",
                "El Boquerón - Muelle de Huanchaco"
            ),
            stopIds = listOf(
                "s_ovalo_papal",
                "s_unt_puerta1",
                "s_mallplaza_mansiche",
                "s_huanchaco_boqueron"
            ),
            pathWaypoints = listOf(
                GeoPoint(-8.1225, -79.0385, "Óvalo Papal"),
                GeoPoint(-8.1150, -79.0380, "UNT"),
                GeoPoint(-8.1018, -79.0435, "Mallplaza Mansiche"),
                GeoPoint(-8.0890, -79.0820, "Óvalo Huanchaco"),
                GeoPoint(-8.0780, -79.1195, "Muelle Huanchaco")
            )
        ),
        BusRoute(
            id = "r_col_laredo",
            code = "COL-L2",
            shortName = "Colectivo Laredo",
            companyName = "Comité Colectivos Trujillo - Laredo Rápido",
            type = RouteType.COLECTIVO,
            originDistrict = "Trujillo (Zona Franca / Av. España)",
            destinationDistrict = "Laredo Centro",
            fareSoles = 3.00,
            studentFareSoles = 2.50,
            universityFareSoles = 2.50,
            frequencyMinutes = 3,
            estimatedDurationMinutes = 18,
            operatingHours = "05:00 AM - 10:30 PM",
            primaryColorHex = 0xFF388E3C,
            secondaryColorHex = 0xFFFFEB3B,
            keyStreets = listOf(
                "Av. España cdra. 21 (Zona Franca)",
                "Av. Pumacahua / Cruce El Porvenir",
                "Carretera Industrial a Laredo",
                "Plaza de Armas de Laredo"
            ),
            stopIds = listOf(
                "s_zona_franca",
                "s_porvenir_pumacahua",
                "s_laredo_plaza"
            ),
            pathWaypoints = listOf(
                GeoPoint(-8.1155, -79.0225, "Zona Franca"),
                GeoPoint(-8.0835, -79.0020, "Cruce Pumacahua"),
                GeoPoint(-8.0890, -78.9610, "Laredo Centro")
            )
        ),
        BusRoute(
            id = "r_col_moche",
            code = "COL-M3",
            shortName = "Colectivo Moche",
            companyName = "Colectivos Óvalo Grau - Campiña de Moche",
            type = RouteType.COLECTIVO,
            originDistrict = "Trujillo (Óvalo Grau)",
            destinationDistrict = "Moche / Campiña",
            fareSoles = 2.50,
            studentFareSoles = 2.00,
            universityFareSoles = 2.00,
            frequencyMinutes = 4,
            estimatedDurationMinutes = 15,
            operatingHours = "05:30 AM - 10:00 PM",
            primaryColorHex = 0xFFD84315,
            secondaryColorHex = 0xFFFFFFFF,
            keyStreets = listOf(
                "Óvalo Grau (Av. Marina)",
                "Panamericana Sur (Terrapuerto)",
                "Puente Moche",
                "Plaza de Armas de Moche"
            ),
            stopIds = listOf(
                "s_ovalo_grau",
                "s_terrapuerto",
                "s_moche_plaza"
            ),
            pathWaypoints = listOf(
                GeoPoint(-8.1240, -79.0220, "Óvalo Grau"),
                GeoPoint(-8.1460, -79.0110, "Terrapuerto"),
                GeoPoint(-8.1710, -79.0090, "Plaza de Moche")
            )
        )
    )

    suspend fun ensureSeedData() {
        val currentCount = transitDao.getIncidentsCount()
        if (currentCount == 0) {
            val now = System.currentTimeMillis()
            val initialIncidents = listOf(
                IncidentEntity(
                    title = "Congestión fuerte en Av. España (Zona Franca)",
                    description = "Tráfico lento entre las cuadras 19 y 22 de Av. España por carga y descarga comercial. Los micros demoran 6 min adicionales.",
                    categoryId = "TRAFICO",
                    avenueOrReference = "Av. España cdra. 21 - Zona Franca",
                    district = "Trujillo",
                    latitude = -8.1155,
                    longitude = -79.0225,
                    reporterName = "Carlos M. (Pasajero)",
                    upvotes = 14,
                    isVerified = true,
                    status = "ACTIVO",
                    affectedRouteCodes = "M-02 H, M-04 C, M-26 B, COL-L2",
                    photoTag = "TRAFICO_ESPANA",
                    createdAtTimestamp = now - 18 * 60 * 1000L
                ),
                IncidentEntity(
                    title = "Mantenimiento de carpeta asfáltica en Av. Mansiche",
                    description = "Cuadrilla municipal realizando bacheo en carril derecho sentido hacia Huanchaco, pasando Mallplaza. Reducción a un solo carril.",
                    categoryId = "OBRA_BACHE",
                    avenueOrReference = "Av. Mansiche cdra. 18 (Frente a Mallplaza)",
                    district = "Trujillo",
                    latitude = -8.1018,
                    longitude = -79.0435,
                    reporterName = "Inspector Vial MPT",
                    upvotes = 23,
                    isVerified = true,
                    status = "ACTIVO",
                    affectedRouteCodes = "M-02 H, M-10 S, M-18 L, COL-H1",
                    photoTag = "OBRA_MANSICHE",
                    createdAtTimestamp = now - 45 * 60 * 1000L
                ),
                IncidentEntity(
                    title = "Semáforo intermitente frente a UPAO",
                    description = "Cruce peatonal de Av. América Sur con flujo lento por falla en sincronización de semáforo. Policía de tránsito apoyando en el lugar.",
                    categoryId = "SEMAFORO",
                    avenueOrReference = "Av. América Sur cdra. 31 - UPAO",
                    district = "Trujillo",
                    latitude = -8.1270,
                    longitude = -79.0310,
                    reporterName = "Lucía R. (Estudiante UPAO)",
                    upvotes = 9,
                    isVerified = true,
                    status = "ACTIVO",
                    affectedRouteCodes = "M-36 A, C-44 T2, M-18 L, M-14 I",
                    photoTag = "SEMAFORO_UPAO",
                    createdAtTimestamp = now - 65 * 60 * 1000L
                ),
                IncidentEntity(
                    title = "Desvío corto por feria en Av. Villarreal",
                    description = "Unidades toman vía auxiliar a la altura del Mercado La Hermelinda por congestión de camiones mayoristas.",
                    categoryId = "DESVIO",
                    avenueOrReference = "Av. Federico Villarreal cdra. 12",
                    district = "Trujillo",
                    latitude = -8.0935,
                    longitude = -79.0135,
                    reporterName = "Jorge V. (Conductor M-04)",
                    upvotes = 7,
                    isVerified = true,
                    status = "ACTIVO",
                    affectedRouteCodes = "M-04 C, C-13 D, M-26 B",
                    photoTag = "DESVIO_HERMELINDA",
                    createdAtTimestamp = now - 95 * 60 * 1000L
                ),
                IncidentEntity(
                    title = "Baches pronunciados en ingreso a La Esperanza",
                    description = "Precaución para micros y colectivos pasando el Arco de La Esperanza, pista deteriorada en carril central.",
                    categoryId = "OBRA_BACHE",
                    avenueOrReference = "Av. Condorcanqui cdra. 10",
                    district = "La Esperanza",
                    latitude = -8.0785,
                    longitude = -79.0430,
                    reporterName = "Vecinos La Esperanza",
                    upvotes = 11,
                    isVerified = true,
                    status = "ACTIVO",
                    affectedRouteCodes = "M-36 A, M-04 C, M-18 L",
                    photoTag = "BACHE_ESPERANZA",
                    createdAtTimestamp = now - 140 * 60 * 1000L
                )
            )
            transitDao.insertAllIncidents(initialIncidents)

            // Seed default favorite route and proximity alert on Óvalo Papal so user immediately sees real-time tracking & alert capability
            transitDao.upsertSavedItem(
                SavedTransitItemEntity(
                    itemKey = "STOP_s_ovalo_papal",
                    itemId = "s_ovalo_papal",
                    itemType = "STOP",
                    isFavorite = true,
                    hasProximityAlert = true
                )
            )
            transitDao.upsertSavedItem(
                SavedTransitItemEntity(
                    itemKey = "ROUTE_r_m36_a",
                    itemId = "r_m36_a",
                    itemType = "ROUTE",
                    isFavorite = true,
                    hasProximityAlert = false
                )
            )
        }
    }

    suspend fun addIncident(incident: IncidentEntity) {
        transitDao.insertIncident(incident)
    }

    suspend fun upvoteIncident(incidentId: Int) {
        transitDao.upvoteIncident(incidentId)
    }

    suspend fun toggleIncidentResolved(incident: IncidentEntity) {
        val nextStatus = if (incident.status == "ACTIVO") "RESUELTO" else "ACTIVO"
        transitDao.updateIncidentStatus(incident.id, nextStatus)
    }

    suspend fun toggleRouteFavorite(routeId: String) {
        val key = "ROUTE_$routeId"
        val existing = transitDao.getSavedItem(key)
        val updated = SavedTransitItemEntity(
            itemKey = key,
            itemId = routeId,
            itemType = "ROUTE",
            isFavorite = !(existing?.isFavorite ?: false),
            hasProximityAlert = existing?.hasProximityAlert ?: false
        )
        transitDao.upsertSavedItem(updated)
    }

    suspend fun toggleStopProximityAlert(stopId: String) {
        val key = "STOP_$stopId"
        val existing = transitDao.getSavedItem(key)
        val updated = SavedTransitItemEntity(
            itemKey = key,
            itemId = stopId,
            itemType = "STOP",
            isFavorite = existing?.isFavorite ?: true,
            hasProximityAlert = !(existing?.hasProximityAlert ?: false)
        )
        transitDao.upsertSavedItem(updated)
    }
}
