# 🗄️ Room — Ejercicio 9: Poner orden: Repository, ViewModel y Room

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
| 👉 **[`ej09-repository-viewmodel`](../../tree/ej09-repository-viewmodel)** | **9 — Poner orden: Repository, ViewModel y Room** | Repository + ViewModel |
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
 ├── MainActivity.kt                # Punto de entrada: muestra la pantalla del ejercicio
 ├── NotasApp.kt                    # Application: crea la ÚNICA instancia de la base (y del Repository)
 ├── core/                          # tema
 │   └── theme/
 │       ├── Color.kt               # Colores del tema
 │       ├── Theme.kt               # Tema Material 3
 │       └── Type.kt                # Tipografías
 ├── data/                          # CÓMO se guardan los datos
 │   ├── local/                     # Room: base, converters, migraciones
 │   │   ├── AppDatabase.kt         # @Database: tablas, versión y DAO (notas.db)
 │   │   ├── dao/                   # operaciones (@Dao)
 │   │   │   └── NotaDao.kt         # Operaciones sobre notas (@Dao)
 │   │   └── entity/                # tablas (@Entity) y resultados relacionales
 │   │       └── NotaEntity.kt      # Tabla notas (@Entity)
 │   └── repository/                # API de datos para el ViewModel
 │       ├── NotaRepository.kt      # Interfaz del Repository (desafío del Ej 9)
 │       └── NotaRepositoryImpl.kt  # Implementación con Room: recibe los DAO por constructor
 └── presentation/                  # una carpeta por pantalla
     └── notas/
         ├── NotasViewModel.kt      # ViewModel: estado (StateFlow) y acciones de alto nivel
         └── PantallaNotas.kt       # Pantalla de notas (formulario, lista y diálogos)
```

```
app/src/androidTest/java/com/example/ejercicioskotlinroom/
 └── data/
     └── local/
         └── dao/
             └── NotaDaoTest.kt  # Tests del DAO de notas
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
| Data | `data/repository/NotaRepository.kt` | Interfaz del Repository (desafío del Ej 9) |
| Data | `data/repository/NotaRepositoryImpl.kt` | Implementación con Room: recibe los DAO por constructor |
| Presentation | `presentation/notas/NotasViewModel.kt` | ViewModel: estado (StateFlow) y acciones de alto nivel |
| Presentation | `presentation/notas/PantallaNotas.kt` | Pantalla de notas (formulario, lista y diálogos) |

---

## 🎯 Consigna

Reorganizar: Compose UI → ViewModel → Repository → DAO → Room. El Repository recibe el DAO por constructor; la UI no llama al DAO. Desafío: interfaz + implementación.

## 💡 En pocas palabras

La pantalla deja de hablar con la base. Ahora pide cosas al **ViewModel** (“guardar nota”), que valida y se lo pasa al **Repository**, que es quien usa el DAO. Mismo comportamiento, mejor ordenado.

## 🔰 Aclaraciones para novatos

- **El ViewModel guarda el estado y sobrevive a la rotación**; la pantalla solo dibuja y avisa eventos.
- **El Repository habla “en idioma de notas”** (agregar, observar) y esconde que atrás hay SQL.
- **La validación (“título vacío”) la decide el ViewModel**, no la pantalla ni el DAO.
- **`flatMapLatest`**: cuando cambia el texto de búsqueda, deja la consulta anterior y empieza la nueva.
- **`stateIn`** convierte el Flow en StateFlow con un valor inicial para que la pantalla siempre tenga algo que mostrar.

## 🔬 Pruebas

Tests instrumentados de esta rama: **4/4 ✅** (`./gradlew :app:connectedDebugAndroidTest`, en emulador).

La explicación completa con el código, las capturas del emulador y las consultas SQL al archivo real está en los apuntes (sección *Ejercicio 9*).

## ❓ Preguntas de la guía

**1. ¿Qué gana y qué cuesta la separación?**  
Gana: la UI no conoce el DAO, cada capa una responsabilidad, estado que sobrevive a la rotación, más fácil de probar. Cuesta: más archivos y métodos que solo delegan.

**2. ¿El Repository tiene que existir siempre?**  
No; si solo reenvía llamadas no aporta. Acá genera `creadaEn`, arma la entidad y sería el lugar para una fuente remota.

**3. ¿Quién decide si un título vacío es válido?**  
El ViewModel: es una regla de entrada de usuario y arma el mensaje.

**4. Con red y Room, ¿quién coordina?**  
El Repository: Room como fuente de verdad y la red para refrescar.

## ✅ Criterio de finalización

La pantalla funciona igual que antes sin tocar el DAO.

---

## 🚀 Cómo ejecutar

```bash
git clone https://github.com/feliforte14/EJERCICIOS-KOTLIN-ROOM.git
cd EJERCICIOS-KOTLIN-ROOM
git checkout ej09-repository-viewmodel
```

1. Abrir en **Android Studio** y esperar la sincronización de Gradle.
2. **Run ▶** en un emulador o dispositivo (minSdk 24).
3. Ver la base: *View → Tool Windows → App Inspection → Database Inspector*.

## 🧪 Tests

```bash
./gradlew :app:connectedDebugAndroidTest
```
Corren en el emulador con una base **en memoria** descartable (no tocan los datos de la app).
