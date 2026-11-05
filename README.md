# 🗄️ Room — Ejercicio 3: DAO: escribir y preguntar a la base

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
| 👉 **[`ej03-dao`](../../tree/ej03-dao)** | **3 — DAO: escribir y preguntar a la base** | @Dao, @Insert, @Query |
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
 │       ├── dao/               # operaciones (@Dao)
 │       │   └── NotaDao.kt     # Operaciones sobre notas (@Dao)
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
| Data | `data/local/dao/NotaDao.kt` | Operaciones sobre `notas` (`@Dao`) |
| Data | `data/local/entity/NotaEntity.kt` | Tabla `notas` (`@Entity`) |
| Presentation | `presentation/info/PantallaInfo.kt` | Pantalla informativa: indica dónde está el código del ejercicio |

---

## 🎯 Consigna

Crear `NotaDao`: insertar, listar todas, borrar todas, más una consulta por id y otra de importantes. Desafío: contar sin cargar filas.

## 💡 En pocas palabras

El DAO es la **lista de cosas que se le pueden pedir a la tabla**. Uno escribe la firma y el SQL; Room genera el código. Todavía no se conecta a la pantalla.

## 🔰 Aclaraciones para novatos

- **El DAO es una `interface`**: no se implementa a mano. KSP genera la clase al compilar.
- **`@Query` lleva SQL dentro de un String**, y Room lo verifica: si una columna no existe, el proyecto no compila.
- **`insertar` devuelve un `Long`:** el id que le tocó a la fila nueva.
- **Buscar por id puede no encontrar nada:** por eso devuelve `NotaEntity?` (puede ser null).
- **`suspend`** porque leer o escribir en disco puede tardar; Room no deja hacerlo en el hilo principal.

## 🔬 Pruebas

La explicación completa con el código, las capturas del emulador y las consultas SQL al archivo real está en los apuntes (sección *Ejercicio 3*).

## ❓ Preguntas de la guía

**1. ¿Qué interpreta Room y qué SQLite?**  
Room: las anotaciones y la firma (genera código, convierte Entity ⇄ fila, valida el SQL al compilar). SQLite: el texto SQL, que ejecuta contra el archivo.

**2. ¿Por qué insertar() devuelve Long?**  
Devuelve el rowId de la fila insertada, que con PK autogenerada es el id nuevo.

**3. ¿Y si la consulta por id no encuentra nada?**  
No hay fila: el retorno razonable es nullable (`NotaEntity?`) y null significa “no existe”.

**4. ¿Por qué no bloquear el hilo principal?**  
Disco puede tardar: congelaría la UI (ANR). Son `suspend` y Room usa su propio hilo de IO.

## ✅ Criterio de finalización

6 operaciones útiles, todas sobre columnas que existen en `NotaEntity` (si no, no compilaría).

---

## 🚀 Cómo ejecutar

```bash
git clone https://github.com/feliforte14/EJERCICIOS-KOTLIN-ROOM.git
cd EJERCICIOS-KOTLIN-ROOM
git checkout ej03-dao
```

1. Abrir en **Android Studio** y esperar la sincronización de Gradle.
2. **Run ▶** en un emulador o dispositivo (minSdk 24).
3. Ver la base: *View → Tool Windows → App Inspection → Database Inspector*.
