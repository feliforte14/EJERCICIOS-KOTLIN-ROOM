package com.example.ejercicioskotlinroom.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

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
    // Ej. 10: IGNORE. Si choca con una restricción (codigoExterno repetido) no inserta y devuelve -1,
    // sin lanzar excepción. Ver los motivos en NotaEntity.
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertar(nota: NotaEntity): Long

    // Las otras dos estrategias existen SOLO para compararlas en las pruebas (NotaDaoTest); la app no las usa.
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertarAbortando(nota: NotaEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarReemplazando(nota: NotaEntity): Long

    // Lectura puntual ("una vez"): trae la lista actual y listo. Más adelante la reemplazo por un Flow (ej. 6).
    @Query("SELECT * FROM notas ORDER BY creadaEn DESC")
    suspend fun obtenerTodasUnaVez(): List<NotaEntity>

    // EJERCICIO 6: lectura observable. NO es suspend: devuelve un Flow "frío" y Room lo re-ejecuta cada vez
    // que cambia la tabla notas. Cada emisión trae la lista completa y actualizada.
    @Query("SELECT * FROM notas ORDER BY creadaEn DESC")
    fun observarNotas(): Flow<List<NotaEntity>>

    // EJERCICIO 7: Room arma el UPDATE ... WHERE id = nota.id usando la clave primaria del objeto.
    // Devuelve la cantidad de filas modificadas (0 si ese id ya no existe).
    @Update
    suspend fun actualizar(nota: NotaEntity): Int

    // Idem con DELETE ... WHERE id = nota.id. Devuelve filas borradas.
    @Delete
    suspend fun eliminar(nota: NotaEntity): Int

    // ---------- EJERCICIO 8: búsquedas, filtros, orden y conteos (todo resuelto en SQL) ----------
    // 1) búsqueda por parte del título. `||` concatena en SQL: queda LIKE '%texto%'.
    //    :texto entra como parámetro enlazado (Room lo pasa como argumento, no pega el texto en el SQL).
    @Query("""
        SELECT * FROM notas
        WHERE titulo LIKE '%' || :texto || '%'
        ORDER BY creadaEn DESC
    """)
    fun buscarPorTitulo(texto: String): Flow<List<NotaEntity>>

    // 2) solo importantes
    @Query("SELECT * FROM notas WHERE importante = 1 ORDER BY creadaEn DESC")
    fun observarImportantes(): Flow<List<NotaEntity>>

    // 3) orden ascendente por fecha (el descendente es observarNotas())
    @Query("SELECT * FROM notas ORDER BY creadaEn ASC")
    fun observarAscendente(): Flow<List<NotaEntity>>

    // 4) y 5) cantidades: las calcula SQLite y salen como Flow, así se actualizan solas.
    @Query("SELECT COUNT(*) FROM notas")
    fun observarCantidad(): Flow<Int>

    @Query("SELECT COUNT(*) FROM notas WHERE importante = 1")
    fun observarCantidadImportantes(): Flow<Int>

    // Consulta combinada que usa la pantalla: búsqueda + filtro + orden en UNA sola consulta.
    // Orden de evaluación en SQL: FROM -> WHERE (filtra filas) -> ORDER BY (ordena lo que quedó).
    // El truco de los dos CASE: solo uno de los dos tiene valor, el otro es NULL y no influye en el orden.
    @Query("""
        SELECT * FROM notas
        WHERE titulo LIKE '%' || :texto || '%'
          AND (:soloImportantes = 0 OR importante = 1)
        ORDER BY
          CASE WHEN :ascendente = 1 THEN creadaEn END ASC,
          CASE WHEN :ascendente = 0 THEN creadaEn END DESC
    """)
    fun filtrar(texto: String, soloImportantes: Boolean, ascendente: Boolean): Flow<List<NotaEntity>>

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
