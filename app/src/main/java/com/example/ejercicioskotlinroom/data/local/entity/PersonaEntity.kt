package com.example.ejercicioskotlinroom.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/*
 * EJERCICIO 15 - Miniaplicación integradora (local-first): registro de objetos prestados y a quién
 *
 * ETAPA 1 - MODELO (el esquema completo también está en docs/EJERCICIO-15.md)
 *
 *   personas                              prestamos
 *   +----+--------+                       +----+--------+-----------+--------------+---------+
 *   | id | nombre |  1 ----------- N      | id | objeto | personaId | fechaPrestamo| estado  | (+ notas desde v2)
 *   +----+--------+                       +----+--------+-----------+--------------+---------+
 *   PK id, UNIQUE(nombre)                 PK id, FK personaId -> personas.id (RESTRICT)
 *
 *  - Entidades y claves primarias: PersonaEntity.id y PrestamoEntity.id (autogeneradas).
 *  - Relación entre tablas: una persona tiene muchos préstamos (1:N), FK en prestamos.personaId.
 *  - Dato que deliberadamente NO se persiste: la cantidad de días que lleva prestado y el texto "hace N días".
 *    Se calculan al mostrar a partir de fechaPrestamo; guardarlos quedaría desactualizado al día siguiente.
 *    (Tampoco se persisten el texto de búsqueda, el filtro activo ni los diálogos abiertos: estado de UI.)
 *  - Regla de unicidad/integridad que vale la pena proteger:
 *      * UNIQUE(personas.nombre): no tener dos "Ana" duplicando el historial de la misma persona.
 *      * FK con RESTRICT: no se puede borrar una persona que todavía tiene préstamos registrados. Elegí RESTRICT
 *        y no CASCADE porque el historial de qué prestaste es justamente lo que esta app protege: una pulsación
 *        accidental sobre "Borrar persona" no debería llevarse por delante los registros de lo que sigue afuera.
 */
@Entity(
    tableName = "personas",
    indices = [Index(value = ["nombre"], unique = true)]
)
data class PersonaEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String
)
