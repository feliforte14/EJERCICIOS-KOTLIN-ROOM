package com.example.ejercicioskotlinroom

import android.app.Application
import androidx.room.Room
import com.example.ejercicioskotlinroom.data.local.PRESTAMOS_MIGRATION_1_2
import com.example.ejercicioskotlinroom.data.local.PrestamosDatabase
import com.example.ejercicioskotlinroom.data.repository.PrestamosRepository

/*
 * Application de la miniapp de prestamos: vive tanto como el proceso. Aca se construye la UNICA instancia
 * de la base (prestamos.db) y del repositorio. `by lazy` la crea en el primer uso y una sola vez.
 * Las pantallas nunca llaman a Room.databaseBuilder: reciben el repositorio ya armado.
 */
class PrestamosApp : Application() {
    val prestamosDatabase: PrestamosDatabase by lazy {
        Room.databaseBuilder(applicationContext, PrestamosDatabase::class.java, "prestamos.db")
            .addMigrations(PRESTAMOS_MIGRATION_1_2) // sin migracion destructiva: son datos del usuario
            .build()
    }
    val prestamosRepositorio: PrestamosRepository by lazy { PrestamosRepository(prestamosDatabase.prestamosDao()) }
}
