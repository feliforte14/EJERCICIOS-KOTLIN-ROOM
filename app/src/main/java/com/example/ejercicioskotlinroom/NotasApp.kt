package com.example.ejercicioskotlinroom

import android.app.Application
import androidx.room.Room
import com.example.ejercicioskotlinroom.data.AppDatabase
import com.example.ejercicioskotlinroom.data.MIGRATION_1_2
import com.example.ejercicioskotlinroom.data.NotaRepository
import com.example.ejercicioskotlinroom.data.NotaRepositoryImpl
import com.example.ejercicioskotlinroom.prestamos.data.PRESTAMOS_MIGRATION_1_2
import com.example.ejercicioskotlinroom.prestamos.data.PrestamosDatabase
import com.example.ejercicioskotlinroom.prestamos.data.PrestamosRepository

/*
 * Application: el objeto que vive tanto como el proceso. Acá se construye la ÚNICA instancia de la base
 * (ver preguntas del ejercicio 4 en AppDatabase). `by lazy` hace que se cree recién en el primer uso,
 * y una sola vez. Las pantallas nunca llaman a Room.databaseBuilder: reciben la base ya armada.
 */
class NotasApp : Application() {
    val database: AppDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "notas.db" // nombre del archivo SQLite; el que se ve en Database Inspector
        )
            // Ej. 14: camino de migración registrado. NO uso fallbackToDestructiveMigration: borraría las notas del usuario.
            .addMigrations(MIGRATION_1_2)
            .build()
    }

    // Ej. 9: el repositorio recibe el DAO por constructor; no crea la base, usa la única que hay.
    val repositorio: NotaRepository by lazy { NotaRepositoryImpl(database.notaDao(), database.categoriaDao()) }

    // Ej. 15: la miniapp de préstamos tiene su propia base (prestamos.db), también con una única instancia.
    val prestamosDatabase: PrestamosDatabase by lazy {
        Room.databaseBuilder(applicationContext, PrestamosDatabase::class.java, "prestamos.db")
            .addMigrations(PRESTAMOS_MIGRATION_1_2) // sin migración destructiva: son datos del usuario
            .build()
    }
    val prestamosRepositorio: PrestamosRepository by lazy { PrestamosRepository(prestamosDatabase.prestamosDao()) }
}
