# 🗄️ EJERCICIOS-KOTLIN-ROOM

Ejercicios de la guía de cátedra **Persistencia local con Room** (Desarrollo de Aplicaciones I, UADE).
Android + **Kotlin** + **Jetpack Compose** + **Room 2.8.5 (KSP)**. Un solo proyecto, **una rama por ejercicio**.

📄 **Apuntes completos** (glosario, explicación de cada ejercicio, aclaraciones para novatos, pruebas con capturas del
emulador y consultas SQL a la base real): [`apuntes/Apuntes_Room.pdf`](apuntes/Apuntes_Room.pdf)

---

## 🧭 Cómo está organizado

- **`main`**: proyecto base (Compose + Room/KSP configurados, tema en `core/theme/`) + apuntes completos.
- **Una rama por ejercicio**, cada una con **solo el código de su ejercicio** y su propio `README.md`
  (árbol de carpetas comentado, consigna, aclaraciones, pruebas y preguntas de la guía).
- La app de **notas** evoluciona del Ej 4 al 14 (cada rama parte de la anterior, como pide la guía);
  el **Ej 15** es una app aparte (préstamos de objetos).

## 🏛️ Estructura de carpetas

```
app/src/main/java/com/example/ejercicioskotlinroom/
 ├── MainActivity.kt          # punto de entrada
 ├── NotasApp.kt              # Application: ÚNICA instancia de la base (Ej 4–14; PrestamosApp en el 15)
 ├── core/theme/              # tema de Compose
 ├── data/
 │    ├── local/              # AppDatabase, Converters, Migrations
 │    │    ├── entity/        # tablas (@Entity) y resultados relacionales
 │    │    └── dao/           # operaciones (@Dao)
 │    └── repository/         # API de datos para el ViewModel (desde el Ej 9)
 ├── domain/model/            # enums del dominio (Prioridad, EstadoPrestamo)
 └── presentation/            # una carpeta por pantalla
app/src/androidTest/.../data/local/   # tests de DAO, converters y migraciones (emulador)
app/schemas/                          # JSON del esquema de cada versión (exportSchema)
```

## 🌿 Ramas

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
| [`ej15-miniapp-integradora`](../../tree/ej15-miniapp-integradora) | 15 — Miniaplicación integradora: primero local, después vemos | Préstamos local-first |

## 🚀 Cómo ejecutar

```bash
git clone https://github.com/feliforte14/EJERCICIOS-KOTLIN-ROOM.git
cd EJERCICIOS-KOTLIN-ROOM
git checkout ej14-migraciones      # o la rama del ejercicio que quieras ver
```

1. Abrir en **Android Studio** y esperar la sincronización de Gradle.
2. **Run ▶** en un emulador o dispositivo (minSdk 24).
3. Ver la base: *App Inspection → Database Inspector*.
4. Tests (ramas 8 en adelante, en emulador): `./gradlew :app:connectedDebugAndroidTest`.
