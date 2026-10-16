package com.example.ejercicioskotlinroom

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.ejercicioskotlinroom.data.AppDatabase
import com.example.ejercicioskotlinroom.data.CategoriaEntity
import com.example.ejercicioskotlinroom.data.NotaEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

// Ej. 11: relación 1:N, lectura con @Relation y política de borrado SET_NULL (decidida antes de ejecutarla).
@RunWith(AndroidJUnit4::class)
class CategoriaDaoTest {
    private lateinit var db: AppDatabase

    @Before
    fun crearBase() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
    }

    @After
    fun cerrarBase() = db.close()

    @Test
    fun leeUnaCategoriaConSusNotas() = runBlocking {
        val catId = db.categoriaDao().insertar(CategoriaEntity(nombre = "Trabajo")).toInt()
        db.notaDao().insertar(NotaEntity(titulo = "N1", contenido = "", creadaEn = 1, categoriaId = catId))
        db.notaDao().insertar(NotaEntity(titulo = "N2", contenido = "", creadaEn = 2, categoriaId = catId))
        db.notaDao().insertar(NotaEntity(titulo = "Suelta", contenido = "", creadaEn = 3)) // sin categoría

        val resultado = db.categoriaDao().obtenerCategoriaConNotas(catId)
        assertNotNull(resultado)
        assertEquals("Trabajo", resultado!!.categoria.nombre)
        assertEquals(listOf("N1", "N2"), resultado.notas.map { it.titulo }.sorted())
    }

    @Test
    fun borrarCategoria_dejaLasNotasSinCategoria() = runBlocking {
        val cat = CategoriaEntity(nombre = "Casa")
        val catId = db.categoriaDao().insertar(cat).toInt()
        val notaId = db.notaDao().insertar(NotaEntity(titulo = "N", contenido = "", creadaEn = 1, categoriaId = catId)).toInt()

        db.categoriaDao().eliminar(cat.copy(id = catId))

        val nota = db.notaDao().obtenerPorId(notaId)
        assertNotNull(nota)            // la nota NO se borró (no es CASCADE)
        assertNull(nota!!.categoriaId) // quedó sin categoría (SET_NULL)
    }

    @Test
    fun notaConCategoriaInexistente_violaLaClaveForanea() = runBlocking {
        try {
            db.notaDao().insertarAbortando(NotaEntity(titulo = "N", contenido = "", creadaEn = 1, categoriaId = 999))
            fail("Debería fallar por la clave foránea")
        } catch (e: SQLiteConstraintException) {
            assertTrue(true)
        }
    }

    @Test
    fun nombreDeCategoriaRepetido_conIgnoreDevuelveMenosUno() = runBlocking {
        assertTrue(db.categoriaDao().insertar(CategoriaEntity(nombre = "Trabajo")) > 0)
        assertEquals(-1L, db.categoriaDao().insertar(CategoriaEntity(nombre = "Trabajo")))
    }

    // ---- Ejercicio 13: transacciones ----

    private suspend fun prepararDosCategoriasConNotas(): Triple<CategoriaEntity, Int, Int> {
        val origen = CategoriaEntity(nombre = "Origen")
        val origenId = db.categoriaDao().insertar(origen).toInt()
        val destinoId = db.categoriaDao().insertar(CategoriaEntity(nombre = "Destino")).toInt()
        db.notaDao().insertar(NotaEntity(titulo = "N1", contenido = "", creadaEn = 1, categoriaId = origenId))
        db.notaDao().insertar(NotaEntity(titulo = "N2", contenido = "", creadaEn = 2, categoriaId = origenId))
        return Triple(origen.copy(id = origenId), origenId, destinoId)
    }

    @Test
    fun fusionar_muevelasNotasYBorraElOrigen() = runBlocking {
        val (origen, origenId, destinoId) = prepararDosCategoriasConNotas()
        db.categoriaDao().fusionar(origen, destinoId)
        assertNull(db.categoriaDao().obtenerCategoriaConNotas(origenId))
        assertEquals(2, db.categoriaDao().obtenerCategoriaConNotas(destinoId)!!.notas.size)
    }

    @Test
    fun fusionConFalloForzado_conTransaccion_haceRollback() = runBlocking {
        val (origen, origenId, destinoId) = prepararDosCategoriasConNotas()
        try {
            db.categoriaDao().fusionar(origen, destinoId, fallarEntrePasos = true)
            fail("Debería haber fallado")
        } catch (e: IllegalStateException) {
            // esperado
        }
        // Rollback: todo como antes. Las notas siguen en el origen, el destino sigue vacío y el origen existe.
        assertEquals(2, db.categoriaDao().obtenerCategoriaConNotas(origenId)!!.notas.size)
        assertEquals(0, db.categoriaDao().obtenerCategoriaConNotas(destinoId)!!.notas.size)
    }

    @Test
    fun fusionConFalloForzado_sinTransaccion_dejaEstadoIntermedio() = runBlocking {
        val (origen, origenId, destinoId) = prepararDosCategoriasConNotas()
        try {
            db.categoriaDao().fusionarSinTransaccion(origen, destinoId, fallarEntrePasos = true)
            fail("Debería haber fallado")
        } catch (e: IllegalStateException) {
            // esperado
        }
        // Estado inconsistente: las notas YA se movieron pero la categoría origen sigue existiendo, vacía.
        assertEquals(0, db.categoriaDao().obtenerCategoriaConNotas(origenId)!!.notas.size)
        assertEquals(2, db.categoriaDao().obtenerCategoriaConNotas(destinoId)!!.notas.size)
    }
}
