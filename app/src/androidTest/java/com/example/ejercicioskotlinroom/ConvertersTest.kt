package com.example.ejercicioskotlinroom

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.ejercicioskotlinroom.data.AppDatabase
import com.example.ejercicioskotlinroom.data.Converters
import com.example.ejercicioskotlinroom.data.NotaEntity
import com.example.ejercicioskotlinroom.data.Prioridad
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

// Ej. 12: el enum se recupera bien Y el valor físico guardado es el texto del nombre (se mira con una consulta cruda).
@RunWith(AndroidJUnit4::class)
class ConvertersTest {

    @Test
    fun ida_y_vuelta_delConversor() {
        val c = Converters()
        Prioridad.entries.forEach { assertEquals(it, c.stringAPrioridad(c.prioridadAString(it))) }
        assertEquals(Prioridad.MEDIA, c.stringAPrioridad("URGENTE")) // texto desconocido -> MEDIA
    }

    @Test
    fun enLaBaseSeGuardaElTextoDelNombre() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        try {
            val id = db.notaDao().insertar(NotaEntity(titulo = "N", contenido = "", creadaEn = 1, prioridad = Prioridad.ALTA))
            // Valor físico: consulta directa a SQLite, sin pasar por Room ni por el conversor.
            db.openHelper.readableDatabase.query("SELECT prioridad FROM notas WHERE id = $id").use {
                it.moveToFirst()
                assertEquals("ALTA", it.getString(0))
            }
            // Y por Room se recupera el enum.
            assertEquals(Prioridad.ALTA, db.notaDao().obtenerPorId(id.toInt())?.prioridad)
        } finally {
            db.close()
        }
    }
}
