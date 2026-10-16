package com.example.ejercicioskotlinroom.data

import androidx.room.Embedded
import androidx.room.Relation

/*
 * Clase de RELACIÓN (no es una entidad, no tiene tabla): describe el RESULTADO de leer una categoría junto
 * con todas sus notas. Pregunta 4 de la guía: una entidad representa una tabla y se guarda; esta clase solo
 * representa el resultado de una lectura relacional y se arma al consultar.
 *
 *   @Embedded  -> los campos de CategoriaEntity se leen de la fila de categorias.
 *   @Relation  -> Room hace una segunda consulta: notas WHERE categoriaId = categoria.id
 *                 (parentColumn = columna de la clase embebida, entityColumn = columna de la entidad hija).
 */
data class CategoriaConNotas(
    @Embedded val categoria: CategoriaEntity,
    @Relation(parentColumn = "id", entityColumn = "categoriaId")
    val notas: List<NotaEntity>
)
