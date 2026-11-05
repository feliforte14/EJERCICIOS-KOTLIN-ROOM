# 🗄️ Room — Ejercicio 4: Crear la base y mirarla por dentro

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
| 👉 **[`ej04-database`](../../tree/ej04-database)** | **4 — Crear la base y mirarla por dentro** | RoomDatabase, instancia única |
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
 ├── NotasApp.kt                # Application: crea la ÚNICA instancia de la base (y del Repository)
 ├── core/                      # tema
 │   └── theme/
 │       ├── Color.kt           # Colores del tema
 │       ├── Theme.kt           # Tema Material 3
 │       └── Type.kt            # Tipografías
 ├── data/                      # CÓMO se guardan los datos
 │   └── local/                 # Room: base, converters, migraciones
 │       ├── AppDatabase.kt     # @Database: tablas, versión y DAO (notas.db)
 │       ├── dao/               # operaciones (@Dao)
 │       │   └── NotaDao.kt     # Operaciones sobre notas (@Dao)
 │       └── entity/            # tablas (@Entity) y resultados relacionales
 │           └── NotaEntity.kt  # Tabla notas (@Entity)
 └── presentation/              # una carpeta por pantalla
     └── prueba/
         └── PruebaBase.kt      # Pantalla de prueba: insertar 3 notas, leer y borrar
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
| Data | `data/local/dao/NotaDao.kt` | Operaciones sobre `notas` (`@Dao`) |
| Data | `data/local/entity/NotaEntity.kt` | Tabla `notas` (`@Entity`) |
| Presentation | `presentation/prueba/PruebaBase.kt` | Pantalla de prueba: insertar 3 notas, leer y borrar |

---

## 🎯 Consigna

Crear `AppDatabase`, construir **una única instancia** para la app, insertar al menos tres notas y verlas en Database Inspector.

## 💡 En pocas palabras

Se crea la base real (`notas.db`) y se la deja en la clase `Application`, que vive mientras vive la app. Una pantalla de prueba inserta 3 notas y las lee.

## 🔰 Aclaraciones para novatos

- **`AppDatabase` define; `Room.databaseBuilder(...).build()` abre.** La primera es la descripción; la segunda crea/abre el archivo.
- **Una sola instancia:** crear la base en cada recomposición abriría conexiones sin parar y los Flow de una no verían lo que escribe otra.
- **`by lazy`** crea la base recién la primera vez que se usa, y una sola vez.
- **`version = 1`** es la versión del esquema: se guarda dentro del archivo y sirve para saber cuándo migrar.
- **Database Inspector no viaja en la app:** es una herramienta de Android Studio para debug.

## 🔬 Pruebas

La explicación completa con el código, las capturas del emulador y las consultas SQL al archivo real está en los apuntes (sección *Ejercicio 4*).

## ❓ Preguntas de la guía

**1. ¿Qué representa version = 1?**  
La versión del esquema, guardada en el archivo (`PRAGMA user_version`). Al cambiar el esquema se sube y se migra.

**2. ¿Relación entre AppDatabase, el archivo SQLite y el DAO?**  
Archivo = los datos; AppDatabase = la puerta de entrada que conoce el esquema; DAO = las operaciones que se obtienen de ella.

**3. ¿Por qué una única instancia?**  
Es cara, cada una abre su conexión, y la invalidación de los Flow es por instancia.

**4. ¿Database Inspector es parte de la app final?**  
No: es de Android Studio, no se empaqueta en el APK.

## ✅ Criterio de finalización

La tabla tiene 3 filas reales (comprobado arriba). Esquema: `data/local/AppDatabase.kt`; apertura física: `NotasApp.kt`.

---

## 🚀 Cómo ejecutar

```bash
git clone https://github.com/feliforte14/EJERCICIOS-KOTLIN-ROOM.git
cd EJERCICIOS-KOTLIN-ROOM
git checkout ej04-database
```

1. Abrir en **Android Studio** y esperar la sincronización de Gradle.
2. **Run ▶** en un emulador o dispositivo (minSdk 24).
3. Ver la base: *View → Tool Windows → App Inspection → Database Inspector*.
