package com.example.ejercicioskotlinroom

import android.content.Context
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.ejercicioskotlinroom.data.AppDatabase
import com.example.ejercicioskotlinroom.data.MIGRATION_1_2
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/*
 * Ej. 14 (desafío): prueba de migración. Crea una base en versión 1 usando el esquema exportado (1.json), la puebla
 * con 5 notas, migra a la versión 2 y verifica que las filas siguen y que la columna nueva existe con valor 0.
 */
@RunWith(AndroidJUnit4::class)
class MigrationTest {
    private val nombreBase = "migracion-test.db"

    @get:Rule
    val helper = MigrationTestHelper(InstrumentationRegistry.getInstrumentation(), AppDatabase::class.java)

    @Test
    fun migra1a2_conservaLasFilasYAgregaArchivada() {
        // 1) Base en versión 1 con 5 notas (SQL directo contra el esquema viejo: todavía no existe `archivada`).
        helper.createDatabase(nombreBase, 1).apply {
            repeat(5) { i ->
                execSQL(
                    "INSERT INTO notas (titulo, contenido, importante, creadaEn, colorEtiqueta, prioridad) " +
                        "VALUES ('Nota $i', 'c', 0, $i, 'NINGUNO', 'MEDIA')"
                )
            }
            close()
        }

        // 2) Migrar a la versión 2 y validar que el esquema resultante coincide con 2.json.
        val db = helper.runMigrationsAndValidate(nombreBase, 2, true, MIGRATION_1_2)

        // 3) Las 5 filas siguen y todas tienen archivada = 0.
        db.query("SELECT COUNT(*), SUM(archivada) FROM notas").use {
            it.moveToFirst()
            assertEquals(5, it.getInt(0))
            assertEquals(0, it.getInt(1))
        }
    }

    @Test
    fun abreLaBaseRealMigradaConRoom() = runBlocking {
        helper.createDatabase(nombreBase, 1).apply {
            execSQL("INSERT INTO notas (titulo, contenido, importante, creadaEn, colorEtiqueta, prioridad) VALUES ('Vieja', '', 1, 1, 'NINGUNO', 'ALTA')")
            close()
        }
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.databaseBuilder(context, AppDatabase::class.java, nombreBase)
            .addMigrations(MIGRATION_1_2)
            .build()
        try {
            val nota = db.notaDao().obtenerTodasUnaVez().single()
            assertEquals("Vieja", nota.titulo)
            assertFalse(nota.archivada) // valor inicial de la fila existente
        } finally {
            db.close()
            context.deleteDatabase(nombreBase)
        }
    }
}
