# 🗄️ Room — Ejercicio 11: Dos tablas: relación uno a muchos

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
| 👉 **[`ej11-relacion-uno-a-muchos`](../../tree/ej11-relacion-uno-a-muchos)** | **11 — Dos tablas: relación uno a muchos** | 1:N, ForeignKey, @Relation |
| [`ej12-typeconverters`](../../tree/ej12-typeconverters) | 12 — TypeConverters: cuando SQLite no conoce tu tipo | TypeConverter (enum) |
| [`ej13-transacciones`](../../tree/ej13-transacciones) | 13 — Transacciones: dos pasos que deberían ser uno | @Transaction, rollback |
| [`ej14-migraciones`](../../tree/ej14-migraciones) | 14 — Migrar una base sin borrar lo anterior | Migración 1 → 2 |
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
 └── presentation/                     # una carpeta por pantalla
     └── notas/
         ├── NotasViewModel.kt         # ViewModel: estado (StateFlow) y acciones de alto nivel
         └── PantallaNotas.kt          # Pantalla de notas (formulario, lista y diálogos)
```

```
app/src/androidTest/java/com/example/ejercicioskotlinroom/
 └── data/
     └── local/
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
| Data | `data/local/dao/CategoriaDao.kt` | Operaciones sobre `categorias` (y fusión transaccional desde el Ej 13) |
| Data | `data/local/dao/NotaDao.kt` | Operaciones sobre `notas` (`@Dao`) |
| Data | `data/local/entity/CategoriaConNotas.kt` | Resultado relacional: categoría + sus notas (`@Relation`) |
| Data | `data/local/entity/CategoriaEntity.kt` | Tabla `categorias` |
| Data | `data/local/entity/NotaEntity.kt` | Tabla `notas` (`@Entity`) |
| Data | `data/repository/NotaRepository.kt` | Interfaz del Repository (desafío del Ej 9) |
| Data | `data/repository/NotaRepositoryImpl.kt` | Implementación con Room: recibe los DAO por constructor |
| Presentation | `presentation/notas/NotasViewModel.kt` | ViewModel: estado (StateFlow) y acciones de alto nivel |
| Presentation | `presentation/notas/PantallaNotas.kt` | Pantalla de notas (formulario, lista y diálogos) |

---

## 🎯 Consigna

Agregar `CategoriaEntity`; una categoría tiene muchas notas. Clave foránea, clase con `@Embedded` + `@Relation`. Desafío: política de borrado.

## 💡 En pocas palabras

Aparece una segunda tabla, `categorias`. Cada nota puede apuntar a una categoría. Si se borra una categoría, sus notas **no se borran**: quedan “sin categoría”.

## 🔰 Aclaraciones para novatos

- **La clave foránea va del lado “muchos”:** `notas.categoriaId` apunta a `categorias.id`.
- **No se guarda una lista de notas dentro de la categoría:** una columna guarda un solo valor; para eso está la otra tabla.
- **`SET_NULL`**: al borrar la categoría, las notas quedan con `categoriaId = NULL`. `CASCADE` las borraría; `RESTRICT` impediría borrar.
- **`CategoriaConNotas` no es una tabla:** es una clase de lectura que Room arma juntando las dos.
- **`categoriaId` es nullable:** una nota puede existir sin categoría.

## 🔬 Pruebas

Tests instrumentados de esta rama: **12/12 ✅** (`./gradlew :app:connectedDebugAndroidTest`, en emulador).

La explicación completa con el código, las capturas del emulador y las consultas SQL al archivo real está en los apuntes (sección *Ejercicio 11*).

## ❓ Preguntas de la guía

**1. ¿Por qué no guardar la lista de notas en una columna?**  
Rompe el modelo relacional: no se puede consultar ni filtrar, no hay integridad y hay que reescribir toda la lista.

**2. ¿Dónde queda la clave foránea?**  
En `notas.categoriaId`, el lado “muchos”.

**3. ¿Qué pasa con las notas al borrar una categoría?**  
Quedan sin categoría (SET_NULL): decisión de producto para no perder notas.

**4. ¿Entidad vs clase de resultado relacional?**  
La entidad tiene tabla y se inserta; `CategoriaConNotas` solo se lee.

## ✅ Criterio de finalización

Dos tablas relacionadas, consulta de categoría con sus notas y efecto del borrado decidido y probado.

---

## 🚀 Cómo ejecutar

```bash
git clone https://github.com/feliforte14/EJERCICIOS-KOTLIN-ROOM.git
cd EJERCICIOS-KOTLIN-ROOM
git checkout ej11-relacion-uno-a-muchos
```

1. Abrir en **Android Studio** y esperar la sincronización de Gradle.
2. **Run ▶** en un emulador o dispositivo (minSdk 24).
3. Ver la base: *View → Tool Windows → App Inspection → Database Inspector*.

## 🧪 Tests

```bash
./gradlew :app:connectedDebugAndroidTest
```
Corren en el emulador con una base **en memoria** descartable (no tocan los datos de la app).
