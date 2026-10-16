package com.example.ejercicioskotlinroom.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

/*
 * EJERCICIO 13 - Transacciones: dos pasos que deberían ser uno
 *
 * Operación elegida: FUSIONAR categorías = mover todas las notas de una categoría a otra y después eliminar la
 * categoría que quedó vacía. Son DOS escrituras (UPDATE de notas + DELETE de categoría).
 *
 * Para poder usar @Transaction con un método con cuerpo, el DAO pasó de interface a clase abstracta
 * (el método con cuerpo es `open`).
 *
 * 1) Versión SIN transacción (fusionarSinTransaccion) - qué podría pasar si falla entre los dos pasos:
 *      paso 1: UPDATE notas SET categoriaId = destino WHERE categoriaId = origen   -> ya quedó confirmado
 *      ------- falla acá (excepción, proceso muerto, batería...) -------
 *      paso 2: DELETE categoria origen                                             -> nunca se ejecuta
 *    Estado intermedio peligroso: las notas ya están en la categoría destino pero la categoría origen sigue
 *    existiendo, vacía. SQLite no se queja (todas las claves foráneas son válidas), pero para la app es un
 *    estado a medias: el usuario pidió "fusionar" y ve las dos categorías. Y si se hiciera en el orden inverso
 *    (borrar primero), el SET_NULL dejaría las notas sin categoría y ya no habría forma de saber a cuál iban a ir.
 * 2) Versión CON transacción (fusionar, @Transaction): Room abre una transacción, ejecuta los dos pasos y hace
 *    commit solo si ninguno falló; ante una excepción hace rollback y la base queda como estaba antes.
 *    Prueba con fallo forzado: CategoriaDaoTest.fusionConFalloForzado_...
 * 3) Lectura relacional con @Transaction: obtenerCategoriaConNotas / observarCategoriasConNotas (ej. 11).
 *    Room hace dos SELECT (categoría y sus notas); en una transacción ven una foto consistente de la base.
 *    Sin ella, una escritura entre las dos consultas podría devolver una categoría con una lista de notas
 *    que ya no corresponde (por ejemplo, justo en medio de una fusión).
 *
 * PREGUNTAS
 * 1) ¿Qué significa que una operación sea atómica?
 *    Que se hace completa o no se hace: no existe el estado "a medias". Si falla cualquier paso, se deshace todo.
 * 2) ¿Una transacción sirve solo para escrituras?
 *    No. También sirve para lecturas que necesitan consistencia entre varias consultas (punto 3).
 * 3) ¿Qué error lógico podría dejar la base en un estado válido para SQLite pero inválido para la app?
 *    Justamente la categoría vacía que debía borrarse, o notas movidas sin que se borre el origen:
 *    ninguna restricción de SQLite se rompe, pero la regla de negocio ("fusionar = una sola categoría al final") sí.
 * 4) ¿Qué parte conviene probar forzando un fallo controlado?
 *    El punto exacto entre los dos pasos: se lanza una excepción después del UPDATE y se verifica que, con
 *    transacción, las notas siguen en la categoría original (rollback), y sin transacción quedaron movidas.
 *    (El parámetro `fallarEntrePasos` existe solo para esa prueba; la app siempre lo deja en false.)
 */
@Dao
abstract class CategoriaDao {

    // IGNORE: si el nombre ya existe (índice único) devuelve -1 en vez de lanzar excepción (mismo criterio que NotaDao).
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insertar(categoria: CategoriaEntity): Long

    @Query("SELECT * FROM categorias ORDER BY nombre")
    abstract fun observarCategorias(): Flow<List<CategoriaEntity>>

    // Al borrar una categoría, qué pasa con sus notas lo decide la ForeignKey de NotaEntity (SET_NULL).
    @Delete
    abstract suspend fun eliminar(categoria: CategoriaEntity): Int

    // Lectura relacional (ver punto 3 de la explicación de arriba).
    @Transaction
    @Query("SELECT * FROM categorias WHERE id = :id")
    abstract suspend fun obtenerCategoriaConNotas(id: Int): CategoriaConNotas?

    @Transaction
    @Query("SELECT * FROM categorias ORDER BY nombre")
    abstract fun observarCategoriasConNotas(): Flow<List<CategoriaConNotas>>

    // Paso 1 de la fusión: devuelve cuántas notas se movieron.
    @Query("UPDATE notas SET categoriaId = :destinoId WHERE categoriaId = :origenId")
    abstract suspend fun moverNotas(origenId: Int, destinoId: Int): Int

    // Versión 1: SIN transacción. Cada paso se confirma por separado. Es la que muestra el problema.
    open suspend fun fusionarSinTransaccion(origen: CategoriaEntity, destinoId: Int, fallarEntrePasos: Boolean = false) {
        moverNotas(origen.id, destinoId)
        if (fallarEntrePasos) throw IllegalStateException("Fallo forzado entre los dos pasos")
        eliminar(origen)
    }

    // Versión 2: CON transacción. Los dos pasos son una sola unidad: o se hacen los dos o ninguno.
    @Transaction
    open suspend fun fusionar(origen: CategoriaEntity, destinoId: Int, fallarEntrePasos: Boolean = false) {
        moverNotas(origen.id, destinoId)
        if (fallarEntrePasos) throw IllegalStateException("Fallo forzado entre los dos pasos")
        eliminar(origen)
    }
}
