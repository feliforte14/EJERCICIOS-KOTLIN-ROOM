package com.example.ejercicioskotlinroom.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

/*
 * EJERCICIO 3 - DAO: escribir y preguntar a la base
 *
 * El DAO es la interfaz que declara QUÉ operaciones se pueden hacer sobre la tabla "notas".
 * No lo implemento yo: Room (vía KSP) genera la clase con el código real. Todavía no lo conecto a la pantalla.
 *
 * Operaciones (son 6, la consigna pedía mínimo 5 útiles):
 *   - insertar              (dada por la consigna)
 *   - obtenerTodasUnaVez    (dada por la consigna)
 *   - borrarTodas           (dada por la consigna)
 *   - obtenerPorId          (propia: una nota por id)
 *   - obtenerImportantes    (propia: solo las marcadas como importantes)
 *   - contarNotas           (desafío: cantidad total sin cargar las filas)
 * Todas las consultas usan columnas que existen en NotaEntity (titulo, importante, creadaEn, id).
 *
 * PREGUNTAS
 * 1) ¿Qué parte del método interpreta Room y qué parte interpreta SQLite?
 *    Room interpreta las anotaciones (@Insert, @Query) y la firma Kotlin: genera el código que arma la
 *    sentencia, convierte NotaEntity <-> fila y valida en compilación que el SQL referencie tablas y
 *    columnas que existen. SQLite interpreta el texto SQL en sí (SELECT, WHERE, ORDER BY...) y ejecuta
 *    contra el archivo de la base.
 *
 * 2) ¿Por qué insertar() puede devolver un Long?
 *    Porque Room devuelve el rowId de la fila recién insertada, que con una PK autogenerada es el id
 *    nuevo. Sirve para saber con qué id quedó la nota (y, con otras estrategias de conflicto, -1 si no se insertó).
 *
 * 3) ¿Qué pasa si una consulta por id no encuentra nada? ¿Qué tipo de retorno sería razonable?
 *    No hay fila, así que no hay objeto que devolver. Por eso el retorno es NotaEntity? (nullable):
 *    null significa "no existe una nota con ese id". Si lo declarara no nulo, Room fallaría en ejecución.
 *
 * 4) ¿Por qué estos métodos no deberían ejecutarse bloqueando el hilo principal?
 *    Leer o escribir en disco puede tardar. Si se hace en el hilo principal se congela la UI (ANR si tarda
 *    mucho). Por eso son suspend: se llaman desde una corrutina y Room mueve el trabajo a su propio
 *    dispatcher de IO. (De hecho Room tira excepción si se consulta en el main thread, salvo que se
 *    habilite allowMainThreadQueries(), que no hay que usar.)
 */
@Dao
interface NotaDao {

    // Devuelve el id (rowId) con el que quedó la fila.
    @Insert
    suspend fun insertar(nota: NotaEntity): Long

    // Lectura puntual ("una vez"): trae la lista actual y listo. Más adelante la reemplazo por un Flow (ej. 6).
    @Query("SELECT * FROM notas ORDER BY creadaEn DESC")
    suspend fun obtenerTodasUnaVez(): List<NotaEntity>

    // Pensada para pruebas controladas: deja la tabla vacía.
    @Query("DELETE FROM notas")
    suspend fun borrarTodas()

    // Consulta propia 1: una nota por id. Nullable porque puede no existir.
    @Query("SELECT * FROM notas WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: Int): NotaEntity?

    // Consulta propia 2: solo las importantes. En SQLite el Boolean es 0/1, así que importante = 1.
    @Query("SELECT * FROM notas WHERE importante = 1 ORDER BY creadaEn DESC")
    suspend fun obtenerImportantes(): List<NotaEntity>

    // Desafío: COUNT(*) lo calcula SQLite y devuelve un único número; no cargo todas las filas en memoria para contarlas.
    @Query("SELECT COUNT(*) FROM notas")
    suspend fun contarNotas(): Int
}
