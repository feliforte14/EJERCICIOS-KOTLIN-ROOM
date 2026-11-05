package com.example.ejercicioskotlinroom.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

// Resultado de lectura (no es entidad): un préstamo junto con la persona a la que se le prestó.
data class PrestamoConPersona(
    @Embedded val prestamo: PrestamoEntity,
    @Relation(parentColumn = "personaId", entityColumn = "id")
    val persona: PersonaEntity
)
