package com.example.ejercicioskotlinroom

import android.app.Application
import androidx.room.Room
import com.example.ejercicioskotlinroom.data.AppDatabase
import com.example.ejercicioskotlinroom.data.NotaRepository
import com.example.ejercicioskotlinroom.data.NotaRepositoryImpl

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
        ).build()
    }

    // Ej. 9: el repositorio recibe el DAO por constructor; no crea la base, usa la única que hay.
    val repositorio: NotaRepository by lazy { NotaRepositoryImpl(database.notaDao()) }
}
