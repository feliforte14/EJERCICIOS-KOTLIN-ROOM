package com.example.ejercicioskotlinroom

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.ejercicioskotlinroom.data.AppDatabase
import com.example.ejercicioskotlinroom.data.NotaDao
import com.example.ejercicioskotlinroom.data.NotaEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/*
 * Pruebas del DAO sobre una base EN MEMORIA (no toca notas.db): datos conocidos -> verifico lectura, filtros,
 * update y delete, como pide la teoría ("una prueba que solo compila no demuestra nada").
 */
@RunWith(AndroidJUnit4::class)
class NotaDaoTest {
    private lateinit var db: AppDatabase
    private lateinit var dao: NotaDao

    @Before
    fun crearBase() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        dao = db.notaDao()
    }

    @After
    fun cerrarBase() = db.close()

    private suspend fun cargarDatos() {
        dao.insertar(NotaEntity(titulo = "Comprar pan", contenido = "", creadaEn = 100))
        dao.insertar(NotaEntity(titulo = "Estudiar Room", contenido = "", importante = true, creadaEn = 200))
        dao.insertar(NotaEntity(titulo = "Estudiar Kotlin", contenido = "", creadaEn = 300))
    }

    @Test
    fun insertarDevuelveIdYLoLee() = runBlocking {
        val id = dao.insertar(NotaEntity(titulo = "A", contenido = "x", creadaEn = 1))
        assertEquals(1L, id)
        assertEquals("A", dao.obtenerPorId(id.toInt())?.titulo)
        assertNull(dao.obtenerPorId(999))
    }

    @Test
    fun actualizarConservaElIdYNoCreaFilaNueva() = runBlocking {
        val id = dao.insertar(NotaEntity(titulo = "Original", contenido = "", creadaEn = 1)).toInt()
        val original = dao.obtenerPorId(id)!!
        val filas = dao.actualizar(original.copy(titulo = "Editada"))
        assertEquals(1, filas)
        assertEquals(1, dao.contarNotas())
        assertEquals("Editada", dao.obtenerPorId(id)?.titulo)
    }

    @Test
    fun eliminarBorraSoloEsaFila() = runBlocking {
        cargarDatos()
        val aBorrar = dao.obtenerTodasUnaVez().first()
        assertEquals(1, dao.eliminar(aBorrar))
        assertEquals(2, dao.contarNotas())
    }

    @Test
    fun filtrarCombinaBusquedaImportantesYOrden() = runBlocking {
        cargarDatos()
        // búsqueda por parte del título
        assertEquals(2, dao.filtrar("Estudiar", false, false).first().size)
        // solo importantes
        assertEquals(listOf("Estudiar Room"), dao.filtrar("", true, false).first().map { it.titulo })
        // orden descendente vs ascendente por fecha
        assertEquals("Estudiar Kotlin", dao.filtrar("", false, false).first().first().titulo)
        assertEquals("Comprar pan", dao.filtrar("", false, true).first().first().titulo)
        // contadores
        assertEquals(3, dao.observarCantidad().first())
        assertEquals(1, dao.observarCantidadImportantes().first())
    }
}
