package com.example.ejercicioskotlinroom.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.ejercicioskotlinroom.domain.model.EstadoPrestamo

@Entity(
    tableName = "prestamos",
    foreignKeys = [
        ForeignKey(
            entity = PersonaEntity::class,
            parentColumns = ["id"],
            childColumns = ["personaId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    // Índice sobre la FK (buscar los préstamos de una persona y validar el RESTRICT) y sobre estado+fecha
    // porque la pantalla principal filtra por estado y ordena por fecha.
    indices = [Index(value = ["personaId"]), Index(value = ["estado", "fechaPrestamo"])]
)
data class PrestamoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val objeto: String,
    val personaId: Int,
    val fechaPrestamo: Long,                       // epoch millis del momento del préstamo
    val estado: EstadoPrestamo = EstadoPrestamo.PRESTADO,
    // ETAPA 4: columna agregada en la versión 2 (cambio de esquema chico y justificado: aclaraciones del estilo
    // "me lo pidió para el sábado"). Las filas de la versión 1 quedan con "" (ver PRESTAMOS_MIGRATION_1_2).
    @ColumnInfo(defaultValue = "''")
    val notas: String = ""
)
