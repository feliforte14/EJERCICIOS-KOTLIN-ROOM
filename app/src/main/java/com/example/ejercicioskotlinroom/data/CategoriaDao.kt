package com.example.ejercicioskotlinroom.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoriaDao {

    // IGNORE: si el nombre ya existe (índice único) devuelve -1 en vez de lanzar excepción (mismo criterio que NotaDao).
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertar(categoria: CategoriaEntity): Long

    @Query("SELECT * FROM categorias ORDER BY nombre")
    fun observarCategorias(): Flow<List<CategoriaEntity>>

    // Al borrar una categoría, qué pasa con sus notas lo decide la ForeignKey de NotaEntity (SET_NULL).
    @Delete
    suspend fun eliminar(categoria: CategoriaEntity): Int

    // Lectura relacional. @Transaction: Room ejecuta DOS consultas (categorias y después notas de esa categoría);
    // dentro de una transacción ven una foto consistente. Sin ella, una escritura entre las dos consultas
    // podría dejar una categoría con una lista de notas que ya no corresponde.
    @Transaction
    @Query("SELECT * FROM categorias WHERE id = :id")
    suspend fun obtenerCategoriaConNotas(id: Int): CategoriaConNotas?

    @Transaction
    @Query("SELECT * FROM categorias ORDER BY nombre")
    fun observarCategoriasConNotas(): Flow<List<CategoriaConNotas>>
}
