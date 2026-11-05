# 🗄️ Room — Ejercicio 14: Migrar una base sin borrar lo anterior

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
| [`ej02-entity`](../../tree/ej02-entity) | 2 — Primera Entity: decidir qué se guarda | @Entity y esquema |
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
| 👉 **[`ej14-migraciones`](../../tree/ej14-migraciones)** | **14 — Migrar una base sin borrar lo anterior** | Migración 1 → 2 |
| [`ej15-miniapp-integradora`](../../tree/ej15-miniapp-integradora) | 15 — Miniaplicación integradora: primero local, después vemos | Préstamos local-first |

---

## 🏛️ Arquitectura de esta rama

```
app/src/main/java/com/example/ejercicioskotlinroom/
 ├── MainActivity.kt                   # Punto de entrada: muestra la pantalla del ejercicio
 ├── NotasApp.kt                       # Application: crea la ÚNICA instancia de la base (y del Repository)
 ├── core/                             # tema
 │   └── theme/
 │       ├── Color.kt                  # Colores del tema
 │       ├── Theme.kt                  # Tema Material 3
 │       └── Type.kt                   # Tipografías
 ├── data/                             # CÓMO se guardan los datos
 │   ├── local/                        # Room: base, converters, migraciones
 │   │   ├── AppDatabase.kt            # @Database: tablas, versión y DAO (notas.db)
 │   │   ├── Converters.kt             # @TypeConverter: Prioridad ⇄ texto
 │   │   ├── Migrations.kt             # MIGRATION_1_2: agrega la columna archivada
 │   │   ├── dao/                      # operaciones (@Dao)
 │   │   │   ├── CategoriaDao.kt       # Operaciones sobre categorias (y fusión transaccional desde el Ej 13)
 │   │   │   └── NotaDao.kt            # Operaciones sobre notas (@Dao)
 │   │   └── entity/                   # tablas (@Entity) y resultados relacionales
 │   │       ├── CategoriaConNotas.kt  # Resultado relacional: categoría + sus notas (@Relation)
 │   │       ├── CategoriaEntity.kt    # Tabla categorias
 │   │       └── NotaEntity.kt         # Tabla notas (@Entity)
 │   └── repository/                   # API de datos para el ViewModel
 │       ├── NotaRepository.kt         # Interfaz del Repository (desafío del Ej 9)
 │       └── NotaRepositoryImpl.kt     # Implementación con Room: recibe los DAO por constructor
 ├── domain/                           # Kotlin puro
 │   └── model/                        # enums del dominio
 │       └── Prioridad.kt              # enum class Prioridad { BAJA, MEDIA, ALTA }
 └── presentation/                     # una carpeta por pantalla
     └── notas/
         ├── NotasViewModel.kt         # ViewModel: estado (StateFlow) y acciones de alto nivel
         └── PantallaNotas.kt          # Pantalla de notas (formulario, lista y diálogos)
```

```
app/src/androidTest/java/com/example/ejercicioskotlinroom/
 └── data/
     └── local/
         ├── ConvertersTest.kt        # Test del TypeConverter
         ├── MigrationTest.kt         # Test de migración 1 → 2
         └── dao/
             ├── CategoriaDaoTest.kt  # Tests de categorías, SET_NULL y transacción
             └── NotaDaoTest.kt       # Tests del DAO de notas
```

### 📂 Archivos por capa

| Capa | Archivo | Qué hace |
|---|---|---|
| Raíz | `MainActivity.kt` | Punto de entrada: muestra la pantalla del ejercicio |
| Raíz | `NotasApp.kt` | `Application`: crea la ÚNICA instancia de la base (y del Repository) |
| Core | `core/theme/Color.kt` | Colores del tema |
| Core | `core/theme/Theme.kt` | Tema Material 3 |
| Core | `core/theme/Type.kt` | Tipografías |
| Data | `data/local/AppDatabase.kt` | `@Database`: tablas, versión y DAO (`notas.db`) |
| Data | `data/local/Converters.kt` | `@TypeConverter`: Prioridad ⇄ texto |
| Data | `data/local/Migrations.kt` | `MIGRATION_1_2`: agrega la columna `archivada` |
| Data | `data/local/dao/CategoriaDao.kt` | Operaciones sobre `categorias` (y fusión transaccional desde el Ej 13) |
| Data | `data/local/dao/NotaDao.kt` | Operaciones sobre `notas` (`@Dao`) |
| Data | `data/local/entity/CategoriaConNotas.kt` | Resultado relacional: categoría + sus notas (`@Relation`) |
| Data | `data/local/entity/CategoriaEntity.kt` | Tabla `categorias` |
| Data | `data/local/entity/NotaEntity.kt` | Tabla `notas` (`@Entity`) |
| Data | `data/repository/NotaRepository.kt` | Interfaz del Repository (desafío del Ej 9) |
| Data | `data/repository/NotaRepositoryImpl.kt` | Implementación con Room: recibe los DAO por constructor |
| Domain | `domain/model/Prioridad.kt` | `enum class Prioridad { BAJA, MEDIA, ALTA }` |
| Presentation | `presentation/notas/NotasViewModel.kt` | ViewModel: estado (StateFlow) y acciones de alto nivel |
| Presentation | `presentation/notas/PantallaNotas.kt` | Pantalla de notas (formulario, lista y diálogos) |

---

## 🎯 Consigna

Base con datos en versión 1; agregar `archivada: Boolean` (falso para las existentes), subir a versión 2, escribir y registrar la migración y comprobar que nada se perdió.

## 💡 En pocas palabras

Se cambia la tabla **sin borrar los datos** que el usuario ya tiene: una instrucción SQL agrega la columna nueva y Room la ejecuta una sola vez al abrir la base vieja.

## 🔰 Aclaraciones para novatos

- **El código compila igual aunque la base instalada sea vieja:** son dos “verdades” distintas y Room las compara al abrir.
- **Subir la versión sin migración** hace que Room tire error (“A migration from 1 to 2 was required but not found”).
- **`fallbackToDestructiveMigration()`** “arregla” el error borrando todos los datos: *no* es la solución.
- **`DEFAULT 0`** es el valor que reciben las filas viejas; sin DEFAULT, SQLite no deja agregar una columna NOT NULL a una tabla con datos.
- **No desinstalar para probar:** desinstalar borra justamente la base vieja que hay que migrar.

## 🔬 Pruebas

Tests instrumentados de esta rama: **19/19 ✅** (`./gradlew :app:connectedDebugAndroidTest`, en emulador).

La explicación completa con el código, las capturas del emulador y las consultas SQL al archivo real está en los apuntes (sección *Ejercicio 14*).

## ❓ Preguntas de la guía

**1. ¿Por qué cambiar una Entity puede requerir migración aunque compile?**  
El código compila contra la clase, pero la base del celular tiene el esquema viejo; Room los compara al abrir.

**2. ¿Y si se sube la versión sin camino de migración?**  
Room lanza IllegalStateException; la salida destructiva borraría los datos.

**3. ¿Qué valor deben tener las filas viejas en la columna nueva?**  
0 (no archivadas): el comportamiento que tenían antes.

**4. ¿Por qué la migración es diseño de datos?**  
Decide qué pasa con la información de personas reales al actualizar; no es un detalle de build.

## ✅ Criterio de finalización

La base pasó de versión 1 a 2 conservando los datos, demostrado con filas antes y después.

---

## 🚀 Cómo ejecutar

```bash
git clone https://github.com/feliforte14/EJERCICIOS-KOTLIN-ROOM.git
cd EJERCICIOS-KOTLIN-ROOM
git checkout ej14-migraciones
```

1. Abrir en **Android Studio** y esperar la sincronización de Gradle.
2. **Run ▶** en un emulador o dispositivo (minSdk 24).
3. Ver la base: *View → Tool Windows → App Inspection → Database Inspector*.

## 🧪 Tests

```bash
./gradlew :app:connectedDebugAndroidTest
```
Corren en el emulador con una base **en memoria** descartable (no tocan los datos de la app).
