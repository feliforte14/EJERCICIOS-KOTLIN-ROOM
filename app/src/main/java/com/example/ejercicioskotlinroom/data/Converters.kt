package com.example.ejercicioskotlinroom.data

import androidx.room.TypeConverter

/*
 * EJERCICIO 12 - TypeConverters: cuando SQLite no conoce tu tipo
 *
 * SQLite solo tiene INTEGER, REAL, TEXT y BLOB. Un enum no existe para él, así que Room necesita saber cómo
 * pasarlo a un tipo que SQLite sí guarda y cómo volver. Se registra con @TypeConverters en AppDatabase.
 *
 * Elegí el enum Prioridad (BAJA, MEDIA, ALTA) y lo guardo como TEXT con el nombre ("ALTA").
 * Qué quedó guardado de verdad: en Database Inspector la columna `prioridad` de la tabla notas muestra el
 * texto BAJA / MEDIA / ALTA (se comprueba también por código en ConvertersTest).
 *
 * PREGUNTAS
 * 1) ¿Room guarda el enum completo?
 *    No. Guarda solo un String con el nombre de la constante. La clase enum vive en el código, no en la base.
 * 2) ¿Qué pasa si en una versión futura se renombra ALTA a URGENTE?
 *    Las filas ya guardadas siguen diciendo "ALTA", que ya no existe como constante: valueOf("ALTA") tira
 *    IllegalArgumentException al leerlas. Hace falta una migración que actualice esos valores
 *    (UPDATE notas SET prioridad = 'URGENTE' WHERE prioridad = 'ALTA'). Por eso, por seguridad, abajo la
 *    lectura de un texto desconocido cae a MEDIA en vez de romper toda la lista (y ojo: eso esconde el
 *    problema, no lo arregla). Por la misma razón, guardar el ordinal (0,1,2) sería peor: reordenar o
 *    insertar una constante en el medio cambia el significado de todos los datos viejos.
 * 3) ¿Ventaja de guardar una fecha como número y de guardarla como texto?
 *    Número (epoch millis): compacto, se ordena y compara bien en SQL, sin ambigüedad de zona horaria.
 *    Texto (ISO-8601): legible a ojo en el inspector y fácil de depurar, pero ocupa más y depende de que el
 *    formato sea siempre el mismo para ordenar bien. Para creadaEn me quedo con el número.
 * 4) ¿Qué parte del problema resuelve el TypeConverter y cuál no?
 *    Resuelve la traducción tipo de Kotlin <-> tipo de SQLite en ambos sentidos. NO resuelve la evolución:
 *    si cambia el enum (renombrar, borrar constantes) o la unidad de una fecha, los datos viejos hay que
 *    migrarlos aparte. Tampoco valida que el valor guardado sea legal (SQLite aceptaría cualquier texto).
 */
class Converters {

    @TypeConverter
    fun prioridadAString(valor: Prioridad): String = valor.name

    @TypeConverter
    fun stringAPrioridad(valor: String): Prioridad =
        // Simetría con prioridadAString; un valor desconocido no tira abajo la lectura (ver pregunta 2).
        Prioridad.entries.firstOrNull { it.name == valor } ?: Prioridad.MEDIA
}
