package com.example.ejercicioskotlinroom

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.ejercicioskotlinroom.prestamos.data.EstadoPrestamo
import com.example.ejercicioskotlinroom.prestamos.data.PersonaEntity
import com.example.ejercicioskotlinroom.prestamos.data.PrestamosDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

// Ej. 15: pruebas de la miniapp de préstamos (base en memoria).
@RunWith(AndroidJUnit4::class)
class PrestamosDaoTest {
    private lateinit var db: PrestamosDatabase

    @Before
    fun crearBase() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, PrestamosDatabase::class.java).build()
    }

    @After
    fun cerrarBase() = db.close()

    @Test
    fun prestar_creaLaPersonaUnaSolaVezYReusaLaExistente() = runBlocking {
        val dao = db.prestamosDao()
        dao.prestar("Ana", "Taladro", 1)
        dao.prestar("Ana", "Escalera", 2)
        assertEquals(1, dao.observarPersonas().first().size)
        assertEquals(2, dao.observarPrestamos("", false).first().size)
    }

    @Test
    fun nombreDePersonaRepetido_noSeDuplica() = runBlocking {
        val dao = db.prestamosDao()
        assertTrue(dao.insertarPersona(PersonaEntity(nombre = "Ana")) > 0)
        assertEquals(-1L, dao.insertarPersona(PersonaEntity(nombre = "Ana")))
    }

    @Test
    fun noSePuedeBorrarUnaPersonaConPrestamos() = runBlocking {
        val dao = db.prestamosDao()
        dao.prestar("Ana", "Taladro", 1)
        val ana = dao.buscarPersonaPorNombre("Ana")!!
        try {
            dao.eliminarPersona(ana)
            fail("RESTRICT debería impedir el borrado")
        } catch (e: SQLiteConstraintException) {
            assertEquals(1, dao.observarPersonas().first().size)
        }
    }

    @Test
    fun filtraPorTextoYPendientes_yCuentaPendientes() = runBlocking {
        val dao = db.prestamosDao()
        dao.prestar("Ana", "Taladro", 1)
        dao.prestar("Beto", "Libro de Kotlin", 2)
        val libro = dao.observarPrestamos("Libro", false).first().single()
        dao.actualizarPrestamo(libro.prestamo.copy(estado = EstadoPrestamo.DEVUELTO))

        assertEquals(1, dao.observarCantidadPendientes().first())
        assertEquals(listOf("Taladro"), dao.observarPrestamos("", true).first().map { it.prestamo.objeto })
        assertEquals("Beto", dao.observarPrestamos("Libro", false).first().single().persona.nombre)
    }
}
