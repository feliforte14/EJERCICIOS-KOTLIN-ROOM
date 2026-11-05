# 🗄️ Room — Ejercicio 2: Primera Entity: decidir qué se guarda

Práctica de **Desarrollo de Aplicaciones I (UADE)** — *Guía de Ejercicios: Persistencia local con Room* (v1.0).
Kotlin + Jetpack Compose + **Room 2.8.5 (KSP)**, organizado por capas.

📄 **Apuntes** (explicación, aclaraciones para novatos, pruebas con capturas y consultas a la base): [`apuntes/Apuntes_Room.pdf`](apuntes/Apuntes_Room.pdf)

---

## 🧭 Ramas

Cada rama contiene **solo el código de su ejercicio**; la app de notas evoluciona del Ej 4 al 14 y el Ej 15 es una app aparte.

| Rama | Ejercicio | Tema |
|---|---|---|
| [`main`](../../tree/main) | Proyecto base + apuntes completos |  |
| [`ej01-estado-en-memoria`](../../tree/ej01-estado-en-memoria) | 1 — ¿Qué se pierde cuando la app se cierra? | remember vs rememberSaveable |
| 👉 **[`ej02-entity`](../../tree/ej02-entity)** | **2 — Primera Entity: decidir qué se guarda** | @Entity y esquema |
| [`ej03-dao`](../../tree/ej03-dao) | 3 — DAO: escribir y preguntar a la base | @Dao, @Insert, @Query |
| [`ej04-database`](../../tree/ej04-database) | 4 — Crear la base y mirarla por dentro | RoomDatabase, instancia única |
| [`ej05-alta-compose`](../../tree/ej05-alta-compose) | 5 — Alta de datos desde Compose | Alta desde Compose |
| [`ej06-flow`](../../tree/ej06-flow) | 6 — Leer con Flow y dejar que la pantalla reaccione | Flow reactivo |
| [`ej07-crud`](../../tree/ej07-crud) | 7 — CRUD completo: editar y eliminar | Editar, borrar, deshacer |
| [`ej08-consultas`](../../tree/ej08-consultas) | 8 — Consultas que empiezan a parecerse a una app real | Búsqueda, filtros, COUNT |
| [`ej09-repository-viewmodel`](../../tree/ej09-repository-viewmodel) | 9 — Poner orden: Repository, ViewModel y Room | Repository + ViewModel |
| [`ej10-restricciones-indices`](../../tree/ej10-restricciones-indices) | 10 — Restricciones, índices y conflictos | Índice único, conflictos |
| [`ej11-relacion-uno-a-muchos`](../../tree/ej11-relacion-uno-a-muchos) | 11 — Dos tablas: relación uno a muchos | 1:N, ForeignKey, @Relation |
| [`ej12-typeconverters`](../../tree/ej12-typeconverters) | 12 — TypeConverters: cuando SQLite no conoce tu tipo | TypeConverter (enum) |
| [`ej13-transacciones`](../../tree/ej13-transacciones) | 13 — Transacciones: dos pasos que deberían ser uno | @Transaction, rollback |
| [`ej14-migraciones`](../../tree/ej14-migraciones) | 14 — Migrar una base sin borrar lo anterior | Migración 1 → 2 |
| [`ej15-miniapp-integradora`](../../tree/ej15-miniapp-integradora) | 15 — Miniaplicación integradora: primero local, después vemos | Préstamos local-first |

---

## 🏛️ Arquitectura de esta rama

```
app/src/main/java/com/example/ejercicioskotlinroom/
 ├── MainActivity.kt            # Punto de entrada: muestra la pantalla del ejercicio
 ├── core/                      # tema
 │   └── theme/
 │       ├── Color.kt           # Colores del tema
 │       ├── Theme.kt           # Tema Material 3
 │       └── Type.kt            # Tipografías
 ├── data/                      # CÓMO se guardan los datos
 │   └── local/                 # Room: base, converters, migraciones
 │       └── entity/            # tablas (@Entity) y resultados relacionales
 │           └── NotaEntity.kt  # Tabla notas (@Entity)
 └── presentation/              # una carpeta por pantalla
     └── info/
         └── PantallaInfo.kt    # Pantalla informativa: indica dónde está el código del ejercicio
```

### 📂 Archivos por capa

| Capa | Archivo | Qué hace |
|---|---|---|
| Raíz | `MainActivity.kt` | Punto de entrada: muestra la pantalla del ejercicio |
| Core | `core/theme/Color.kt` | Colores del tema |
| Core | `core/theme/Theme.kt` | Tema Material 3 |
| Core | `core/theme/Type.kt` | Tipografías |
| Data | `data/local/entity/NotaEntity.kt` | Tabla `notas` (`@Entity`) |
| Presentation | `presentation/info/PantallaInfo.kt` | Pantalla informativa: indica dónde está el código del ejercicio |

---

## 🎯 Consigna

Crear `NotaEntity` revisando campo por campo, agregar una columna propia y describir la tabla resultante.

## 💡 En pocas palabras

Se define **cómo es una fila** de la tabla `notas` con una `data class`. Todavía no se guarda nada: es el “molde”. Se agregó la columna `colorEtiqueta`.

## 🔰 Aclaraciones para novatos

- **Una instancia de `NotaEntity` = una fila.** Cada propiedad = una columna.
- **`id = 0` no significa que todas las notas tengan id 0:** con `autoGenerate`, el 0 quiere decir “todavía no tiene”; SQLite asigna 1, 2, 3…
- **SQLite no tiene Boolean:** Room guarda `true/false` como 1/0.
- **Guardar datos crudos, no formateados:** la fecha va como número (`Long`) para poder ordenar; el “hace 5 minutos” se arma al mostrar.
- **Cambiar el nombre de una propiedad cambia el nombre de la columna** y obliga a migrar una base ya instalada (Ej 14).

## 🔬 Pruebas

La explicación completa con el código, las capturas del emulador y las consultas SQL al archivo real está en los apuntes (sección *Ejercicio 2*).

## ❓ Preguntas de la guía

**1. ¿Por qué id puede empezar en 0?**  
Con `autoGenerate` el 0 es un centinela (“sin id todavía”); al insertar, SQLite asigna el siguiente.

**2. ¿Qué parte existe para Room y qué parte es del problema?**  
Para Room: `@Entity`, `@PrimaryKey`, `tableName`, el id autogenerado. Del problema: título, contenido, importante, fecha, color.

**3. ¿Guardarías un Painter, un Modifier o un texto formateado?**  
No: Painter y Modifier son objetos de la UI sin sentido fuera del proceso; un texto formateado impide ordenar y reformatear.

**4. ¿Cambiar el nombre de una propiedad puede afectar una base instalada?**  
Sí: cambia el esquema. Hay que subir la versión y migrar, o fijar el nombre con `@ColumnInfo(name = ...)`.

## ✅ Criterio de finalización

Compila con una `@Entity` válida y PK clara; la explicación de qué representa una fila está en el código.

---

## 🚀 Cómo ejecutar

```bash
git clone https://github.com/feliforte14/EJERCICIOS-KOTLIN-ROOM.git
cd EJERCICIOS-KOTLIN-ROOM
git checkout ej02-entity
```

1. Abrir en **Android Studio** y esperar la sincronización de Gradle.
2. **Run ▶** en un emulador o dispositivo (minSdk 24).
3. Ver la base: *View → Tool Windows → App Inspection → Database Inspector*.
