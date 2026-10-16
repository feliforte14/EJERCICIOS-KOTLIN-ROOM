package com.example.ejercicioskotlinroom.prestamos.data

import android.database.sqlite.SQLiteConstraintException
import kotlinx.coroutines.flow.Flow

/*
 * ETAPA 4 - Arquitectura: UI -> PrestamosViewModel -> PrestamosRepository -> PrestamosDao -> Room.
 * Acá no armé una interfaz aparte: con una sola implementación y sin segunda fuente de datos hubiera sido una capa
 * más sin aporte (misma conclusión que en el ejercicio 9).
 */
class PrestamosRepository(private val dao: PrestamosDao) {

    fun observarPrestamos(texto: String, soloPendientes: Boolean): Flow<List<PrestamoConPersona>> =
        dao.observarPrestamos(texto, soloPendientes)

    fun observarPendientes(): Flow<Int> = dao.observarCantidadPendientes()
    fun observarPersonas(): Flow<List<PersonaEntity>> = dao.observarPersonas()

    // Crea la persona si hace falta y registra el préstamo en una sola transacción. La fecha la fija esta capa.
    suspend fun prestar(persona: String, objeto: String, notas: String): Long =
        dao.prestar(persona, objeto, System.currentTimeMillis(), notas)

    suspend fun actualizar(prestamo: PrestamoEntity): Boolean = dao.actualizarPrestamo(prestamo) == 1
    suspend fun eliminar(prestamo: PrestamoEntity) { dao.eliminarPrestamo(prestamo) }
    suspend fun restaurar(prestamo: PrestamoEntity) { dao.insertarPrestamo(prestamo) }

    // false si la persona todavía tiene préstamos (FK RESTRICT): el borrado queda rechazado por la base.
    suspend fun eliminarPersona(persona: PersonaEntity): Boolean =
        try {
            dao.eliminarPersona(persona)
            true
        } catch (e: SQLiteConstraintException) {
            false
        }
}
