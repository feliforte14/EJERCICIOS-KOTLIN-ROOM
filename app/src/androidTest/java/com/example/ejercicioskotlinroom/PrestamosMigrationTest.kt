package com.example.ejercicioskotlinroom

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.ejercicioskotlinroom.prestamos.data.PRESTAMOS_MIGRATION_1_2
import com.example.ejercicioskotlinroom.prestamos.data.PrestamosDatabase
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// Ej. 15 etapa 4: la base de préstamos pasa de la versión 1 a la 2 conservando datos (filas visibles antes y después).
@RunWith(AndroidJUnit4::class)
class PrestamosMigrationTest {
    @get:Rule
    val helper = MigrationTestHelper(InstrumentationRegistry.getInstrumentation(), PrestamosDatabase::class.java)

    @Test
    fun migra1a2_conservaPrestamosYAgregaNotas() {
        helper.createDatabase("prestamos-migracion-test.db", 1).apply {
            execSQL("INSERT INTO personas (id, nombre) VALUES (1, 'Ana')")
            execSQL("INSERT INTO prestamos (objeto, personaId, fechaPrestamo, estado) VALUES ('Taladro', 1, 100, 'PRESTADO')")
            execSQL("INSERT INTO prestamos (objeto, personaId, fechaPrestamo, estado) VALUES ('Libro', 1, 200, 'DEVUELTO')")
            close()
        }
        val db = helper.runMigrationsAndValidate("prestamos-migracion-test.db", 2, true, PRESTAMOS_MIGRATION_1_2)
        db.query("SELECT objeto, notas, estado FROM prestamos ORDER BY id").use {
            assertEquals(2, it.count)                // las filas viejas siguen
            it.moveToFirst()
            assertEquals("Taladro", it.getString(0))
            assertEquals("", it.getString(1))        // columna nueva con el valor por defecto
            assertEquals("PRESTADO", it.getString(2))
        }
    }
}
