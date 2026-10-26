# Rutas Trujillo (TruxRutas) - Transporte Público e Incidencias

Aplicación móvil integral en Android (Jetpack Compose & Material 3) para consultar las rutas oficiales de micros y colectivos de Trujillo (basadas en datos de la Municipalidad Provincial de Trujillo y Truxdi), localizar paraderos cercanos con alertas en tiempo real, visualizar la posición en vivo de las unidades con tiempos de llegada (ETA) y reportar incidencias viales en la ciudad.

---

### Decisiones Críticas y Preferencias del Usuario

> [!IMPORTANT]
> A continuación se detallan las decisiones confirmadas y los estándares aplicados para la arquitectura de la app:

- **Catálogo de Transporte Priorizado**: Micros urbanos y colectivos interurbanos principales (rutas consolidadas de *Empresa Huanchaco, El Cortijo, Nuevo California, Miramar, Salaverry Express, Correcaminos, Alto Trujillo, Mochica, Icaro* y colectivos Moche/Laredo/Huanchaco).
- **Seguimiento en Vivo y Tiempos de Espera (ETA)**: Motor de telemetría y simulación en tiempo real que mueve las unidades a lo largo de las coordenadas geográficas de las rutas oficiales de Trujillo, calculando distancias y minutos restantes hacia cada paradero según la velocidad promedio y congestión de la zona.
- **Buscador de Paraderos y Notificaciones de Llegada**: Detección de paraderos cercanos por GPS/distancia con opción de marcar "Mi Parada" para recibir avisos flotantes/notificaciones en tiempo real cuando el micro esté a menos de 300 metros o a 2 minutos de llegar.
- **Sistema Comunitario de Reportes Viales**: Registro y visualización de incidencias en la vía pública (tráfico intenso, baches, obras viales, desvíos temporales, accidentes o seguridad) con ubicación en el mapa, foto/icono descriptivo, fecha/hora y botón de confirmación comunitaria ("Sigue ahí" / "Vía despejada").

---

### 1. Visión General del Producto

- **Propósito**: Empoderar a los ciudadanos trujillanos y visitantes para desplazarse fácilmente por Trujillo metropolitano (Trujillo Centro, Víctor Larco, La Esperanza, El Porvenir, Florencia de Mora, Huanchaco, Moche, Salaverry, Laredo), conociendo con exactitud qué micro o colectivo tomar, cuánto cuesta el pasaje, dónde están los paraderos autorizados, cuándo pasará la unidad y qué incidencias afectan el tráfico.
- **Público Objetivo**: Pasajeros diarios, estudiantes universitarios (UNT, UPAO, UCV, UPN), trabajadores y turistas en la provincia de Trujillo.
- **Valor Principal**: Datos de rutas verídicos basados en Truxdi y la Municipalidad Provincial de Trujillo (MPT), mapa táctil fluido sin dependencias externas bloqueantes, y alertas proactivas que evitan largas esperas en la calle.

---

### 2. Experiencia de Usuario y Diseño Visual

#### Navegación y Flujos Principales
1. **Pestaña Rutas**: Lista y buscador rápido de líneas con filtros por empresa, tipo (Micro / Combi / Colectivo), origen/destino y tarifa (S/ 2.00 - S/ 3.50). Al pulsar una ruta se despliega la ficha técnica con su recorrido ida/vuelta, paraderos clave y botón "Ver en Mapa".
2. **Pestaña Mapa en Vivo**: Vista cartográfica interactiva de Trujillo (Óvalo Papal, Óvalo Larco, Av. España, Av. Mansiche, Av. América, La Hermelinda, etc.) con:
   - Trazado vectorial policromático de la ruta seleccionada.
   - Unidades en movimiento con icono de micro, dirección y código de vehículo.
   - Paraderos interactivos con tarjeta flotante que muestra los micros que vienen y su tiempo estimado de llegada (ETA).
   - Selector de capas (ver micros en vivo, ver paraderos, ver incidencias de tráfico).
3. **Pestaña Paraderos Cercanos**: Muestra los paraderos más próximos a la ubicación actual ordenados por metros de distancia, con lista de líneas que pasan por allí y botón para activar alerta de proximidad.
4. **Pestaña Incidencias**: Feed comunitario y mapa de alertas viales en Trujillo. Permite crear un nuevo reporte con geolocalización, categoría, descripción y foto/icono.

#### Identidad Visual & Estilo
- **Estilo**: Utilitario moderno, limpio y con alta legibilidad en movimiento ("Transit Design System").
- **Paleta de Colores**:
  - `Primary`: Azul Trujillano profundo (`#0D47A1` / `#1976D2`) que evoca orden y confianza cívica.
  - `Secondary / Accent`: Amarillo sol brillante (`#FFB300`) y Naranja tránsito (`#FF6F00`) para paraderos y avisos en vivo.
  - `Route Badges`: Códigos de color icónicos para cada línea (p. ej. Nuevo California rojo/blanco, Huanchaco azul/amarillo, Cortijo verde/blanco, Correcaminos turquesa).
  - `Surface & Backgrounds`: Tonos neutros modernos con soporte claro/oscuro y elevaciones tonales M3.
- **Tipografía**: Jerarquía clara con números de línea grandes y legibles, distancias en metros y minutos ETA destacados.

---

### 3. Decisiones de Producto y Arquitectura de Datos

- **Motor de Datos de Rutas (Truxdi / MPT)**:
  - Repositorio estructurado en Kotlin con el catálogo detallado de rutas oficiales de Trujillo: nombres de empresa, código oficial de ruta (ej. M-36, M-02, M-04, C-13, C-44), recorrido detallado de calles (Av. Mansiche, Av. España, Av. América Norte/Sur, Av. Larco, etc.), paraderos geolocalizados y tarifa oficial.
- **Persistencia Local de Incidencias y Preferencias (Room & State)**:
  - Base de datos local Room para guardar los reportes de incidentes viales creados por el usuario o precargados, con contadores de validación y estado persistente.
  - Marcado de rutas y paraderos favoritos guardados en memoria/DataStore para acceso instantáneo sin internet.
- **Motor de Telemetría y ETA**:
  - Simulación periódica en Coroutines (`StateFlow`) que desplaza los vehículos sobre los waypoints de la ruta, calculando la velocidad y el ETA a los siguientes 3 paraderos.
  - Notificaciones locales / alertas en pantalla tipo Banner interactivo cuando la unidad se acerca a "Mi Paradero".

---

### 4. Arquitectura Técnica

```
┌───────────────────────────────────────────────────────────┐
│                     UI (Jetpack Compose)                  │
│  ┌───────────────┬────────────────┬─────────────────────┐ │
│  │ RoutesCatalog │  LiveTransitMap│  NearbyStopsSheet   │ │
│  │ Screen        │  Screen        │  & ProximityAlerts  │ │
│  └───────┬───────┴────────┬───────┴──────────┬──────────┘ │
│          │                │                  │            │
│          └────────────────┼──────────────────┘            │
│                           ▼                               │
│              IncidentReportDialog & Feed                  │
└───────────────────────────┬───────────────────────────────┘
                            ▼
┌───────────────────────────────────────────────────────────┐
│                  ViewModel Layer (MVVM)                   │
│   • TransitViewModel (Rutas, Paraderos, Filtros, Favoritos)│
│   • LiveTrackingViewModel (Simulador de telemetría y ETA) │
│   • IncidentsViewModel (Creación y validación de reportes) │
└───────────────────────────┬───────────────────────────────┘
                            ▼
┌───────────────────────────────────────────────────────────┐
│                     Data Layer                            │
│  ┌───────────────────────┬─────────────────────────────┐  │
│  │ TruxdiRoutesRepository│ IncidentsLocalDataSource    │  │
│  │ (Rutas MPT, Paraderos,│ (Room Database / DAO)       │  │
│  │ Coordenadas, Tarifas) │                             │  │
│  └───────────────────────┴─────────────────────────────┘  │
└───────────────────────────────────────────────────────────┘
```

#### Componentes Clave:
1. `TransitModels.kt`: Entidades para `BusRoute`, `BusStop`, `LiveBusUnit`, `RouteType`, `RoadIncident`, `IncidentCategory`.
2. `TruxdiRoutesRepository.kt`: Catálogo completo de rutas y paraderos oficiales de Trujillo.
3. `TransitMapCanvas.kt`: Componente Canvas de alto rendimiento que renderiza el mapa base de Trujillo, las líneas de ruta, paraderos, microbuses animados con orientación, e incidencias.
4. `LiveTrackingEngine.kt`: Motor de simulación en Coroutines que emite ubicaciones de vehículos en vivo, actualiza paraderos y calcula tiempos de espera (ETA).
5. `NearbyStopsScreen.kt`: Buscador de paraderos con filtro de distancia y avisos de aproximación.
6. `IncidentsScreen.kt`: Gestión de reportes ciudadanos con categorías (Accidente, Congestión, Obra/Bache, Desvío, Seguridad).
7. `TransitViewModel.kt`: Estado unificado para navegación, búsqueda reactiva y selección de rutas.

---

### 5. Plan de Ejecución por Pasos

1. **Configuración de Metadatos y Dependencias**:
   - Actualizar `metadata.json` con el nombre "TruxRutas" y descripción clara en español.
   - Configurar `strings.xml` con los títulos y textos de la app.
   - Declarar permisos necesarios en `AndroidManifest.xml` (`ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`, `POST_NOTIFICATIONS`, `VIBRATE`).
2. **Capa de Dominio y Datos (Rutas de Trujillo)**:
   - Crear entidades y repositorio de datos con las rutas más transitadas de Trujillo basadas en MPT/Truxdi (Huanchaco, El Cortijo, Nuevo California, Miramar, Salaverry, Correcaminos, Mochica, Alto Trujillo, colectivos).
   - Implementar base de datos Room para persistencia de reportes viales e incidentes.
3. **Motor de Seguimiento en Tiempo Real y ETA**:
   - Crear el gestor de telemetría que calcula distancias euclidianas/geodésicas, velocidad y minutos restantes (ETA).
   - Implementar el sistema de alertas de proximidad para paraderos designados.
4. **Componentes Visuales y Mapa Interactivo**:
   - Desarrollar `TransitMapCanvas` con soporte para zoom, desplazamiento (pan), marcadores de buses con rotación según trayectoria, pines de paraderos e iconos de alertas.
   - Implementar la vista de detalle de ruta con recorrido paso a paso e información de pasajes.
5. **Módulo de Reportes de Incidencias**:
   - Formulario modal para reportar incidencias con selección de coordenadas en mapa, tipo de problema y notas.
   - Tarjetas de incidencias con votación comunitaria ("Confirmar reporte").
6. **Integración UI y Navegación M3**:
   - Integrar `Scaffold` con barra de navegación M3 (Rutas, Mapa en Vivo, Paraderos, Incidencias).
   - Ajustar insets para borde a borde (`enableEdgeToEdge`, safeDrawing).
7. **Compilación y Verificación**:
   - Compilar el proyecto con `compile_applet` para asegurar cero errores de compilación y validar el comportamiento reactivo.
