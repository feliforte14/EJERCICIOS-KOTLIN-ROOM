package com.example.ejercicioskotlinroom.data.local

import androidx.room.TypeConverter
import com.example.ejercicioskotlinroom.domain.model.EstadoPrestamo

// Mismo criterio que Converters.kt de las notas: se guarda el nombre del enum como TEXT.
class PrestamosConverters {
    @TypeConverter
    fun estadoAString(valor: EstadoPrestamo): String = valor.name

    @TypeConverter
    fun stringAEstado(valor: String): EstadoPrestamo =
        EstadoPrestamo.entries.firstOrNull { it.name == valor } ?: EstadoPrestamo.PRESTADO
}
