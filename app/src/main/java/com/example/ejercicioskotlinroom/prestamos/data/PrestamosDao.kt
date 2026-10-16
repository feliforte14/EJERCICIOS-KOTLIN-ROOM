package com.example.ejercicioskotlinroom.prestamos.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/*
 * ETAPA 2 y 3: DAO con crear, listar de forma reactiva (Flow), editar, eliminar, buscar/filtrar y la relación.
 * Clase abstracta para poder tener un método @Transaction con cuerpo (prestar).
 */
@Dao
abstract class PrestamosDao {

    // ---------- Personas ----------
    @Insert(onConflict = OnConflictStrategy.IGNORE) // nombre repetido -> -1, sin excepción
    abstract suspend fun insertarPersona(persona: PersonaEntity): Long

    @Query("SELECT * FROM personas WHERE nombre = :nombre LIMIT 1")
    abstract suspend fun buscarPersonaPorNombre(nombre: String): PersonaEntity?

    @Query("SELECT * FROM personas ORDER BY nombre")
    abstract fun observarPersonas(): Flow<List<PersonaEntity>>

    // Lanza SQLiteConstraintException si la persona tiene préstamos (FK RESTRICT): lo maneja el ViewModel.
    @Delete
    abstract suspend fun eliminarPersona(persona: PersonaEntity): Int

    // ---------- Préstamos ----------
    @Insert
    abstract suspend fun insertarPrestamo(prestamo: PrestamoEntity): Long

    @Update
    abstract suspend fun actualizarPrestamo(prestamo: PrestamoEntity): Int

    @Delete
    abstract suspend fun eliminarPrestamo(prestamo: PrestamoEntity): Int

    // Listado principal. Búsqueda por objeto, filtro "solo pendientes" y orden: todo en SQL.
    // @Transaction porque @Relation hace una segunda consulta (la persona de cada préstamo).
    @Transaction
    @Query("""
        SELECT * FROM prestamos
        WHERE objeto LIKE '%' || :texto || '%'
          AND (:soloPendientes = 0 OR estado = 'PRESTADO')
        ORDER BY fechaPrestamo DESC
    """)
    abstract fun observarPrestamos(texto: String, soloPendientes: Boolean): Flow<List<PrestamoConPersona>>

    @Query("SELECT COUNT(*) FROM prestamos WHERE estado = 'PRESTADO'")
    abstract fun observarCantidadPendientes(): Flow<Int>

    // Atomicidad: "prestar a alguien" = (crear la persona si no existe) + (registrar el préstamo).
    // Sin transacción, si falla el segundo paso quedaría una persona nueva sin préstamo.
    @Transaction
    open suspend fun prestar(nombrePersona: String, objeto: String, ahora: Long, notas: String = ""): Long {
        val existente = buscarPersonaPorNombre(nombrePersona)
        val personaId = existente?.id ?: insertarPersona(PersonaEntity(nombre = nombrePersona)).toInt()
        return insertarPrestamo(PrestamoEntity(objeto = objeto, personaId = personaId, fechaPrestamo = ahora, notas = notas))
    }
}
