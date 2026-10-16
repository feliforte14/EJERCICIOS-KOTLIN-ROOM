package com.example.ejercicioskotlinroom.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/*
 * EJERCICIO 11 - Dos tablas: relación uno a muchos
 *
 * Categoría 1 ---- N Nota
 *
 *   categorias                         notas
 *   +----+--------+                    +----+--------+-----+-------------+
 *   | id | nombre |   <---(FK)----     | id | titulo | ... | categoriaId |
 *   +----+--------+                    +----+--------+-----+-------------+
 *
 * La clave foránea vive en el lado "muchos": NotaEntity.categoriaId apunta a CategoriaEntity.id.
 * (La categoría no sabe cuáles son sus notas: se averiguan con una consulta, ver CategoriaConNotas.)
 *
 * El nombre de categoría es único: no tiene sentido tener dos "Trabajo".
 */
@Entity(
    tableName = "categorias",
    indices = [Index(value = ["nombre"], unique = true)]
)
data class CategoriaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String
)
