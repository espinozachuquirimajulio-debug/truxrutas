package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [IncidentEntity::class, SavedTransitItemEntity::class],
    version = 1,
    exportSchema = false
)
abstract class TruxRutasDatabase : RoomDatabase() {
    abstract fun transitDao(): TransitDao

    companion object {
        @Volatile
        private var INSTANCE: TruxRutasDatabase? = null

        fun getInstance(context: Context): TruxRutasDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TruxRutasDatabase::class.java,
                    "truxrutas_mpt.db"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
