package com.example.ejercicioskotlinroom.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
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
/*
 * EJERCICIO 11 - Relación con categorías
 *
 * Se agrega `categoriaId: Int?` (la clave foránea) con ForeignKey hacia categorias.id y un índice sobre esa columna.
 *
 * Decisión: ¿una nota puede existir sin categoría? SÍ. categoriaId es nullable: la categoría es una forma de
 * organizar, no parte esencial de la nota; obligarla frenaría el alta rápida de notas.
 *
 * Desafío - política de borrado: onDelete = SET_NULL.
 *   Efecto documentado ANTES de ejecutarlo: al borrar una categoría, sus notas NO se borran; quedan con
 *   categoriaId = NULL ("sin categoría"). Elegí SET_NULL y no CASCADE porque borrar una categoría no debería
 *   llevarse notas del usuario por delante. (CASCADE borraría todas las notas de esa categoría; RESTRICT/NO_ACTION
 *   impediría borrar una categoría que tenga notas.) Se prueba en CategoriaDaoTest.
 *   Room activa PRAGMA foreign_keys = ON, por eso la restricción se hace cumplir de verdad: insertar una nota
 *   con un categoriaId inexistente falla.
 * El índice sobre categoriaId acelera buscar las notas de una categoría y los borrados (SQLite tiene que
 * encontrar las notas hijas para aplicar SET_NULL; sin índice recorre toda la tabla).
 *
 * PREGUNTAS DEL EJERCICIO 11
 * 1) ¿Por qué no guardar directamente una lista de notas dentro de una columna de categoría?
 *    Una columna guarda un valor atómico. Meter una lista (JSON, texto separado por comas) rompe el modelo
 *    relacional: no se puede consultar, filtrar ni ordenar notas con SQL, no hay integridad (puede apuntar a
 *    notas que no existen) y para cambiar una nota hay que reescribir toda la lista. Por eso se hace otra tabla.
 * 2) ¿Dónde queda la clave foránea?
 *    En la tabla del lado "muchos": notas.categoriaId referencia categorias.id.
 * 3) ¿Qué debería ocurrir con las notas si se elimina una categoría?
 *    Que queden sin categoría (SET_NULL), ver arriba. Es una decisión de producto.
 * 4) ¿Diferencia entre una entidad y una clase que solo representa un resultado relacional?
 *    La entidad tiene @Entity, tabla propia y se inserta/actualiza. CategoriaConNotas no tiene tabla: es una
 *    clase de lectura que Room arma con @Embedded + @Relation a partir de dos tablas.
 *
 * ---- (Texto del ejercicio 10, sigue valiendo) ----
 * EJERCICIO 10 - Restricciones, índices y conflictos
 *
 * Cada nota puede tener un `codigoExterno` (opcional) que no debería repetirse -> índice ÚNICO.
 *   - Dos notas con el mismo codigoExterno: la segunda viola la restricción UNIQUE y SQLite la rechaza.
 *   - SQLite permite VARIAS filas con codigoExterno NULL (NULL no se considera igual a otro NULL), así que
 *     "sin código" no choca. Ojo: un String vacío "" SÍ es un valor y chocaría; por eso la capa de datos
 *     convierte el texto en blanco a null antes de guardar.
 *
 * Índice NO único sobre creadaEn: todas las consultas de la pantalla hacen ORDER BY creadaEn.
 * Con el índice, SQLite puede recorrer las filas ya ordenadas en vez de ordenarlas cada vez.
 * Elegí creadaEn y no `importante` porque importante solo tiene dos valores (0/1): un índice ahí casi no
 * discrimina. Tampoco titulo: la búsqueda es LIKE '%texto%' con comodín adelante, que no usa un índice común.
 * Costo: el índice ocupa espacio y cada insert/update tiene que mantenerlo.
 *
 * Estrategia de conflicto elegida para insertar: IGNORE (ver NotaDao.insertar).
 *   ABORT   (default): cancela y lanza SQLiteConstraintException. Sirve si un duplicado es un bug que
 *           tiene que explotar fuerte; obliga a capturar la excepción.
 *   IGNORE: no inserta y devuelve -1 como rowId. Es la que uso: un código repetido es un error de usuario
 *           esperable, no una excepción, y el Repository/ViewModel lo traducen a un mensaje.
 *   REPLACE: borra la fila vieja y mete la nueva. Peligrosa por comodidad: la nota anterior se pierde en
 *           silencio, cambia su id y, con foreign keys en CASCADE, arrastraría filas hijas.
 *
 * PREGUNTAS
 * 1) ¿Un índice único es lo mismo que una clave primaria?
 *    No. La PK identifica la fila, no puede ser NULL y hay una sola por tabla. Un índice único solo impide
 *    repetidos en esa(s) columna(s); puede haber varios por tabla y admite NULL. Los dos usan un índice por dentro.
 * 2) ¿Qué problema puede traer REPLACE si se usa por comodidad?
 *    Pierde datos sin avisar (la fila anterior desaparece), cambia el id de la nota y puede disparar borrados
 *    en cascada de las tablas relacionadas. Un "guardar" termina pareciendo un "pisar".
 * 3) ¿Los índices mejoran todo gratis?
 *    No. Aceleran lecturas que filtran u ordenan por esa columna, pero ocupan espacio y hacen más lentas las
 *    escrituras. Un índice que ninguna consulta usa es costo puro.
 * 4) ¿Cómo comprobaría que una restricción realmente se cumple?
 *    Con una prueba: insertar dos filas con el mismo valor y verificar que la segunda no queda (ver
 *    NotaDaoTest.codigoExternoRepetido...). También intentando lo mismo en Database Inspector, que debe rechazarlo.
 *
 * Nota de desarrollo: el esquema cambió (columna nueva + índices) pero la versión sigue en 1 porque la app
 * todavía no tiene usuarios. En el equipo de pruebas hay que borrar los datos de la app (adb shell pm clear ...)
 * para que Room recree la tabla. Desde el ej. 14 los cambios de esquema se hacen con migraciones.
 */
@Entity(
    tableName = "notas",
    foreignKeys = [
        ForeignKey(
            entity = CategoriaEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoriaId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["codigoExterno"], unique = true),
        Index(value = ["creadaEn"]),
        Index(value = ["categoriaId"])
    ]
)
data class NotaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val titulo: String,
    val contenido: String,
    val importante: Boolean = false,
    val creadaEn: Long,
    // Columna propia del ejercicio (la consigna pedía elegir una).
    val colorEtiqueta: String = "NINGUNO",
    // Ej. 10: identificador opcional que viene de afuera. null = sin código (no choca con otras notas sin código).
    val codigoExterno: String? = null,
    // Ej. 11: clave foránea a categorias.id. null = nota sin categoría.
    val categoriaId: Int? = null
)
