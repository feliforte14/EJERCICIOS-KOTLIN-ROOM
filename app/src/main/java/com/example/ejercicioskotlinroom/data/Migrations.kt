package com.example.ejercicioskotlinroom.data

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/*
 * EJERCICIO 14 - Migrar una base sin borrar lo anterior
 *
 * Cambio de la versión 2: NotaEntity gana `archivada: Boolean`, falsa para las filas que ya existían.
 *
 * Pasos que se siguieron (el esquema de la versión 1 quedó exportado en app/schemas/.../1.json en la rama ej13):
 *   1. Ejecutar la app en versión 1 (rama ej13) y cargar al menos 5 notas.
 *   2. Verificar los datos en Database Inspector.
 *   3. Modificar la entidad (campo archivada).
 *   4. Incrementar version en @Database: 1 -> 2 (Room exporta 2.json).
 *   5. Escribir y registrar MIGRATION_1_2 (abajo) en el builder de NotasApp.
 *   6. Ejecutar de nuevo SIN desinstalar la app.
 *   7. Comprobar que las notas viejas siguen y que la columna nueva existe (archivada = 0 en todas).
 *
 * SQL de la migración y por qué:
 *   ALTER TABLE notas ADD COLUMN archivada INTEGER NOT NULL DEFAULT 0
 *   - INTEGER: así guarda Room un Boolean (0/1).
 *   - NOT NULL: la entidad declara un Boolean no nulo.
 *   - DEFAULT 0: es el valor que reciben las filas YA existentes (falso = no archivadas). Sin DEFAULT,
 *     SQLite no deja agregar una columna NOT NULL a una tabla con datos.
 *   En la entidad lo espejo con @ColumnInfo(defaultValue = "0") para que el esquema que declara Room y el que
 *   deja la migración sean idénticos (Room valida al abrir que la tabla real coincida con la esperada).
 *
 * Procedimiento reproducible para que otra persona compruebe la migración sin borrar datos (desafío):
 *   a) git checkout ej13-transacciones; instalar y cargar 5 notas (Database Inspector: tabla notas con 5 filas).
 *   b) NO desinstalar ni borrar datos. git checkout ej14-migraciones; ejecutar de nuevo (Run, instala encima).
 *   c) Database Inspector: las 5 filas siguen y la tabla tiene la columna archivada = 0.
 *   d) Automatizado: ./gradlew :app:connectedDebugAndroidTest (MigrationTest crea la base v1, la puebla, migra y valida).
 *   Si Room tira "A migration from 1 to 2 was required but not found", se subió la versión sin registrar la migración.
 *
 * PREGUNTAS
 * 1) ¿Por qué cambiar una Entity puede requerir migración aunque el código compile?
 *    Porque el código compila contra la clase, pero la base instalada en el celu del usuario sigue teniendo
 *    el esquema viejo. Hay dos "verdades": el esquema declarado en las entidades y el esquema real del archivo.
 *    Room las compara al abrir (con la versión y un hash de identidad) y si no coinciden no puede trabajar.
 * 2) ¿Qué sucede si se sube la versión pero no se registra un camino de migración?
 *    Room lanza IllegalStateException ("A migration from 1 to 2 was required but not found"). La salida fácil
 *    sería fallbackToDestructiveMigration(), que borra todos los datos del usuario: justo lo que no queremos.
 * 3) ¿Qué datos deberían tener las filas antiguas en la nueva columna?
 *    false/0 (no archivadas): es el valor neutro, el mismo comportamiento que tenían antes del cambio.
 * 4) ¿Por qué una migración es parte del diseño de datos y no solo un detalle de Gradle o Android Studio?
 *    Porque decide qué pasa con la información de personas reales al actualizar: qué valor toman los datos
 *    viejos, si se conservan, si se transforman. Es una decisión de modelo (igual que elegir las columnas),
 *    no una tarea de build. Y el camino tiene que existir para TODAS las versiones desde las que se pueda
 *    actualizar (un usuario puede saltar de la 1 a la 4 sin pasar por la 2 ni la 3: hacen falta 1→2, 2→3 y 3→4,
 *    o una migración directa).
 *
 * Nota: las ramas ej10 a ej13 cambiaban el esquema sin subir la versión porque la app no estaba "publicada".
 * Una base instalada desde esas ramas tiene otro esquema con version=1 y NO migra con esto; para esos equipos de
 * desarrollo hay que borrar los datos una vez (adb shell pm clear com.example.ejercicioskotlinroom).
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE notas ADD COLUMN archivada INTEGER NOT NULL DEFAULT 0")
    }
}
