package com.example.ejercicioskotlinroom.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/*
 * EJERCICIO 2 - Primera Entity: decidir qué se guarda
 *
 * Una instancia de NotaEntity = UNA FILA de la tabla "notas".
 *
 * Tabla resultante (así me la imagino, tipos aproximados de SQLite):
 *
 *   notas
 *   ---------------------------------------------------------------
 *   id             INTEGER  PRIMARY KEY AUTOINCREMENT  NOT NULL
 *   titulo         TEXT     NOT NULL
 *   contenido      TEXT     NOT NULL
 *   importante     INTEGER  NOT NULL   (Boolean -> 0 / 1)
 *   creadaEn       INTEGER  NOT NULL   (Long -> millis desde epoch)
 *   colorEtiqueta  TEXT     NOT NULL   (columna agregada por mí, ver abajo)
 *
 * Revisión campo por campo:
 *  - id: clave primaria autogenerada. Es lo que identifica la fila de forma estable (no el título,
 *    que se puede repetir o cambiar).
 *  - titulo / contenido: es lo que el usuario escribe, tiene que persistir. Son String no nulos:
 *    una nota siempre tiene los dos (el contenido puede ser "" pero no null).
 *  - importante: Boolean con default false. SQLite no tiene boolean, Room lo guarda como 0/1.
 *  - creadaEn: Long en milisegundos (System.currentTimeMillis()). Lo dejo como Long y no como texto
 *    formateado para poder ordenar por fecha en SQL.
 *  - colorEtiqueta (AGREGADA): etiqueta de color de la nota ("NINGUNO", "ROJO", ...). La elegí porque es
 *    un dato del usuario que sí tiene sentido que dure. Va como String con default para no obligar
 *    a pasarla siempre.
 *
 * No agregué más campos "por gusto": todos los que están se usan en algún ejercicio.
 *
 * PREGUNTAS
 * 1) ¿Por qué id puede empezar en 0 aunque las filas guardadas no queden todas con 0?
 *    Con autoGenerate = true, Room trata el 0 como "todavía no tiene id" y deja que SQLite asigne
 *    el siguiente (1, 2, 3...). El 0 es solo un valor centinela para poder construir el objeto antes
 *    de insertarlo. Después del insert la fila tiene su id real.
 *
 * 2) ¿Qué parte del objeto existe porque la necesita Room y cuál pertenece al problema que modelamos?
 *    Para Room: @Entity, @PrimaryKey, tableName, el id autogenerado y el valor por defecto en 0.
 *    Del problema (notas): titulo, contenido, importante, creadaEn, colorEtiqueta.
 *    (el id es técnico pero igual es parte del dominio porque identifica a la nota).
 *
 * 3) ¿Guardaría un Painter, un Modifier o un texto ya formateado en esta entidad? ¿Por qué?
 *    No. Painter y Modifier son cosas de la UI: no son datos, no se pueden guardar en SQLite
 *    (y son objetos de runtime que no tienen sentido fuera del proceso). Un texto ya formateado
 *    ("hace 5 minutos", "16/10/2026") tampoco: se guarda el dato crudo (creadaEn) y se formatea al
 *    mostrarlo; si guardo el texto formateado pierdo la posibilidad de ordenar, filtrar o reformatear.
 *
 * 4) Si mañana cambia el nombre de una propiedad, ¿puede afectar una base ya instalada?
 *    Sí. El nombre de la propiedad es el nombre de la columna (si no uso @ColumnInfo). Renombrarla cambia
 *    el esquema: la base instalada sigue teniendo la columna vieja, Room detecta que el esquema no
 *    coincide y hace falta subir la versión y escribir una migración (ej. 14).
 *    Por eso conviene pensar bien los nombres de entrada, o fijarlos con @ColumnInfo(name = ...).
 */
@Entity(tableName = "notas")
data class NotaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val titulo: String,
    val contenido: String,
    val importante: Boolean = false,
    val creadaEn: Long,
    // Columna propia del ejercicio (la consigna pedía elegir una).
    val colorEtiqueta: String = "NINGUNO"
)
