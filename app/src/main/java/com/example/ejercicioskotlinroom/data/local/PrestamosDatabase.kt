package com.example.ejercicioskotlinroom.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.ejercicioskotlinroom.data.local.dao.PrestamosDao
import com.example.ejercicioskotlinroom.data.local.entity.PersonaEntity
import com.example.ejercicioskotlinroom.data.local.entity.PrestamoEntity

// Base propia de la miniapp (archivo prestamos.db), independiente de notas.db de los ejercicios 1 a 14.
// Versión 1: esquema inicial. Versión 2 (ETAPA 4): prestamos.notas. Los JSON de esquema exportados
// (app/schemas/.../1.json y 2.json) los usa PrestamosMigrationTest.
@Database(
    entities = [PersonaEntity::class, PrestamoEntity::class],
    version = 2,
    exportSchema = true
)
@TypeConverters(PrestamosConverters::class)
abstract class PrestamosDatabase : RoomDatabase() {
    abstract fun prestamosDao(): PrestamosDao
}
