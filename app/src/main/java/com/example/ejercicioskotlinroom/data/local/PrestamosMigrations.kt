package com.example.ejercicioskotlinroom.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// ETAPA 4 - cambio de esquema que obliga a una migración real: una columna nueva bien justificada.
// DEFAULT '' le da un valor a las filas que ya existían; NOT NULL porque la entidad no admite nulos.
val PRESTAMOS_MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE prestamos ADD COLUMN notas TEXT NOT NULL DEFAULT ''")
    }
}
