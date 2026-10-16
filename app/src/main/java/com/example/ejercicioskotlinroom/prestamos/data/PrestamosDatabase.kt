package com.example.ejercicioskotlinroom.prestamos.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

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

// ETAPA 4 - cambio de esquema que obliga a una migración real: una columna nueva bien justificada.
// DEFAULT '' le da un valor a las filas que ya existían; NOT NULL porque la entidad no admite nulos.
val PRESTAMOS_MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE prestamos ADD COLUMN notas TEXT NOT NULL DEFAULT ''")
    }
}
