package com.example.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "road_incidents")
data class IncidentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val categoryId: String,
    val avenueOrReference: String,
    val district: String,
    val latitude: Double,
    val longitude: Double,
    val reporterName: String,
    val upvotes: Int = 1,
    val isVerified: Boolean = false,
    val status: String = "ACTIVO", // ACTIVO, RESUELTO
    val affectedRouteCodes: String = "", // Comma-separated codes e.g. "M-36,M-02"
    val photoTag: String = "ALERTA_VIAL",
    val createdAtTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_preferences_items")
data class SavedTransitItemEntity(
    @PrimaryKey val itemKey: String, // e.g. "ROUTE_r_m36" or "STOP_s_ovalo_papal"
    val itemId: String,
    val itemType: String, // "ROUTE" or "STOP"
    val isFavorite: Boolean = false,
    val hasProximityAlert: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

@Dao
interface TransitDao {
    @Query("SELECT * FROM road_incidents ORDER BY status ASC, createdAtTimestamp DESC")
    fun observeAllIncidents(): Flow<List<IncidentEntity>>

    @Query("SELECT COUNT(*) FROM road_incidents")
    suspend fun getIncidentsCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncident(incident: IncidentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllIncidents(incidents: List<IncidentEntity>)

    @Query("UPDATE road_incidents SET upvotes = upvotes + 1, isVerified = CASE WHEN upvotes + 1 >= 3 THEN 1 ELSE isVerified END WHERE id = :incidentId")
    suspend fun upvoteIncident(incidentId: Int)

    @Query("UPDATE road_incidents SET status = :newStatus WHERE id = :incidentId")
    suspend fun updateIncidentStatus(incidentId: Int, newStatus: String)

    @Query("DELETE FROM road_incidents WHERE id = :incidentId")
    suspend fun deleteIncident(incidentId: Int)

    @Query("SELECT * FROM user_preferences_items")
    fun observeSavedItems(): Flow<List<SavedTransitItemEntity>>

    @Query("SELECT * FROM user_preferences_items WHERE itemKey = :key LIMIT 1")
    suspend fun getSavedItem(key: String): SavedTransitItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSavedItem(item: SavedTransitItemEntity)
}
