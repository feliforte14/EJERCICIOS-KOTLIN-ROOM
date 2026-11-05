# 🗄️ Room — Ejercicio 15: Miniaplicación integradora: primero local, después vemos

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
| [`ej14-migraciones`](../../tree/ej14-migraciones) | 14 — Migrar una base sin borrar lo anterior | Migración 1 → 2 |
| 👉 **[`ej15-miniapp-integradora`](../../tree/ej15-miniapp-integradora)** | **15 — Miniaplicación integradora: primero local, después vemos** | Préstamos local-first |

---

## 🏛️ Arquitectura de esta rama

```
app/src/main/java/com/example/ejercicioskotlinroom/
 ├── MainActivity.kt                    # Punto de entrada: muestra la pantalla del ejercicio
 ├── PrestamosApp.kt                    # Application: única instancia de prestamos.db y del Repository
 ├── core/                              # tema
 │   └── theme/
 │       ├── Color.kt                   # Colores del tema
 │       ├── Theme.kt                   # Tema Material 3
 │       └── Type.kt                    # Tipografías
 ├── data/                              # CÓMO se guardan los datos
 │   ├── local/                         # Room: base, converters, migraciones
 │   │   ├── PrestamosConverters.kt     # @TypeConverter: EstadoPrestamo ⇄ texto
 │   │   ├── PrestamosDatabase.kt       # @Database de préstamos (prestamos.db, versión 2)
 │   │   ├── PrestamosMigrations.kt     # PRESTAMOS_MIGRATION_1_2: agrega prestamos.notas
 │   │   ├── dao/                       # operaciones (@Dao)
 │   │   │   └── PrestamosDao.kt        # Operaciones de préstamos (prestar en transacción)
 │   │   └── entity/                    # tablas (@Entity) y resultados relacionales
 │   │       ├── PersonaEntity.kt       # Tabla personas (nombre único)
 │   │       ├── PrestamoConPersona.kt  # Resultado relacional: préstamo + persona
 │   │       └── PrestamoEntity.kt      # Tabla prestamos (FK a personas, RESTRICT)
 │   └── repository/                    # API de datos para el ViewModel
 │       └── PrestamosRepository.kt     # Repository de préstamos
 ├── domain/                            # Kotlin puro
 │   └── model/                         # enums del dominio
 │       └── EstadoPrestamo.kt          # enum PRESTADO / DEVUELTO
 └── presentation/                      # una carpeta por pantalla
     └── prestamos/
         ├── PantallaPrestamos.kt       # Pantalla de préstamos: alta, lista, búsqueda, filtros
         └── PrestamosViewModel.kt      # ViewModel de préstamos
```

```
app/src/androidTest/java/com/example/ejercicioskotlinroom/
 └── data/
     └── local/
         ├── PrestamosMigrationTest.kt  # Test de migración de préstamos
         └── dao/
             └── PrestamosDaoTest.kt    # Tests del DAO de préstamos
```

### 📂 Archivos por capa

| Capa | Archivo | Qué hace |
|---|---|---|
| Raíz | `MainActivity.kt` | Punto de entrada: muestra la pantalla del ejercicio |
| Raíz | `PrestamosApp.kt` | `Application`: única instancia de `prestamos.db` y del Repository |
| Core | `core/theme/Color.kt` | Colores del tema |
| Core | `core/theme/Theme.kt` | Tema Material 3 |
| Core | `core/theme/Type.kt` | Tipografías |
| Data | `data/local/PrestamosConverters.kt` | `@TypeConverter`: EstadoPrestamo ⇄ texto |
| Data | `data/local/PrestamosDatabase.kt` | `@Database` de préstamos (`prestamos.db`, versión 2) |
| Data | `data/local/PrestamosMigrations.kt` | `PRESTAMOS_MIGRATION_1_2`: agrega `prestamos.notas` |
| Data | `data/local/dao/PrestamosDao.kt` | Operaciones de préstamos (prestar en transacción) |
| Data | `data/local/entity/PersonaEntity.kt` | Tabla `personas` (nombre único) |
| Data | `data/local/entity/PrestamoConPersona.kt` | Resultado relacional: préstamo + persona |
| Data | `data/local/entity/PrestamoEntity.kt` | Tabla `prestamos` (FK a personas, RESTRICT) |
| Data | `data/repository/PrestamosRepository.kt` | Repository de préstamos |
| Domain | `domain/model/EstadoPrestamo.kt` | `enum` PRESTADO / DEVUELTO |
| Presentation | `presentation/prestamos/PantallaPrestamos.kt` | Pantalla de préstamos: alta, lista, búsqueda, filtros |
| Presentation | `presentation/prestamos/PrestamosViewModel.kt` | ViewModel de préstamos |

---

## 🎯 Consigna

App que tenga sentido sin red, con Room como fuente local real: CRUD, observación reactiva, al menos dos entidades relacionadas y una migración comprobable. Etapas 1 a 5.

## 💡 En pocas palabras

**Registro de objetos prestados**: qué presté, a quién y cuándo. Dos tablas (`personas` y `prestamos`), búsqueda, filtro de pendientes, devolver, editar, borrar con deshacer y una migración real (versión 1 → 2).

## 🔰 Aclaraciones para novatos

- **“Local-first”**: la pantalla lee de Room, así que la app funciona sin internet. Un servidor, si existiera, sería una copia que se sincroniza.
- **`RESTRICT`**: no se puede borrar una persona que tiene préstamos (el historial es lo valioso).
- **Nombre de persona único**: “Prestar” busca a la persona por nombre y la crea solo si no existe, dentro de una transacción.
- **Los días prestados no se guardan**: se calculan con la fecha; guardarlos quedaría desactualizado al día siguiente.
- **El estado (PRESTADO / DEVUELTO) es un enum** guardado como texto con un TypeConverter, como en el Ej 12.

## 🔬 Pruebas

Tests instrumentados de esta rama: **5/5 ✅** (`./gradlew :app:connectedDebugAndroidTest`, en emulador).

La explicación completa con el código, las capturas del emulador y las consultas SQL al archivo real está en los apuntes (sección *Ejercicio 15*).

## ✅ Criterio de finalización

Room como fuente local real, CRUD, observación reactiva, dos entidades relacionadas (1:N con RESTRICT) y una migración comprobable. Tablas en Database Inspector: `personas`, `prestamos` y `room_master_table` (interna de Room, guarda el hash del esquema).

---

## 🚀 Cómo ejecutar

```bash
git clone https://github.com/feliforte14/EJERCICIOS-KOTLIN-ROOM.git
cd EJERCICIOS-KOTLIN-ROOM
git checkout ej15-miniapp-integradora
```

1. Abrir en **Android Studio** y esperar la sincronización de Gradle.
2. **Run ▶** en un emulador o dispositivo (minSdk 24).
3. Ver la base: *View → Tool Windows → App Inspection → Database Inspector*.

## 🧪 Tests

```bash
./gradlew :app:connectedDebugAndroidTest
```
Corren en el emulador con una base **en memoria** descartable (no tocan los datos de la app).
