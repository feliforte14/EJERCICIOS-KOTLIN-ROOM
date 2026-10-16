package com.example.ejercicioskotlinroom.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/*
 * EJERCICIO 4 - Crear la base y mirarla por dentro
 *
 * Esta clase abstracta DEFINE el esquema (qué entidades hay, qué versión es) y expone los DAO.
 * Room genera AppDatabase_Impl. Quien ABRE físicamente el archivo SQLite es Room.databaseBuilder(...).build(),
 * que se llama una sola vez en NotasApp.
 *
 *   - Define el esquema:        @Database(entities = [...], version = 1) en esta clase.
 *   - Abre físicamente la base: Room.databaseBuilder(...).build() en NotasApp (y se abre de verdad con el primer acceso).
 *
 * PREGUNTAS
 * 1) ¿Qué representa version = 1?
 *    La versión del esquema. Se guarda dentro del archivo (PRAGMA user_version). Cuando cambia el esquema
 *    (columna nueva, tabla nueva) hay que subirla y dar una migración (ej. 14). Room compara la versión
 *    del código con la del archivo al abrirlo.
 *
 * 2) ¿Qué relación hay entre AppDatabase, el archivo SQLite y el DAO?
 *    El archivo SQLite ("notas.db") es donde están los datos de verdad. AppDatabase es el objeto de Room que
 *    administra la conexión a ese archivo y conoce el esquema. El DAO se obtiene de AppDatabase
 *    (notaDao()) y es lo que usa el código para leer/escribir sobre ese archivo. Archivo = datos,
 *    AppDatabase = puerta de entrada, DAO = operaciones.
 *
 * 3) ¿Por qué conviene una única instancia de Room en el proceso?
 *    Cada instancia abre su propia conexión y es cara de crear. Con varias instancias sobre el mismo archivo
 *    aparecen problemas de concurrencia y, sobre todo, los Flow de una no se enteran de lo que escribe otra
 *    (el mecanismo de invalidación es por instancia). Además, si se crea una por recomposición, se
 *    abren conexiones sin parar. Una sola instancia vive tanto como el proceso.
 *
 * 4) ¿El Database Inspector es parte de la app final?
 *    No. Es una herramienta de Android Studio (App Inspection) que se engancha a la app en debug para mirar
 *    y editar la base. No se empaqueta en el APK ni existe en el dispositivo del usuario.
 *
 * exportSchema = true: Room escribe el JSON del esquema en app/schemas (configurado con KSP en build.gradle.kts).
 * Ese JSON se versiona en git y lo usa el test de migración del ej. 14.
 */
@Database(
    entities = [NotaEntity::class, CategoriaEntity::class],
    version = 1,
    exportSchema = true
)
// Ej. 12: registra los conversores para toda la base (entidades y DAO).
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun notaDao(): NotaDao
    abstract fun categoriaDao(): CategoriaDao // Ej. 11
}
