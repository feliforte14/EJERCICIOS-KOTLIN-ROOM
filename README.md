# 🗄️ Room — Ejercicio 1: ¿Qué se pierde cuando la app se cierra?

Práctica de **Desarrollo de Aplicaciones I (UADE)** — *Guía de Ejercicios: Persistencia local con Room* (v1.0).
Kotlin + Jetpack Compose + **Room 2.8.5 (KSP)**, organizado por capas.

📄 **Apuntes** (explicación, aclaraciones para novatos, pruebas con capturas y consultas a la base): [`apuntes/Apuntes_Room.pdf`](apuntes/Apuntes_Room.pdf)

---

## 🧭 Ramas

Cada rama contiene **solo el código de su ejercicio**; la app de notas evoluciona del Ej 4 al 14 y el Ej 15 es una app aparte.

| Rama | Ejercicio | Tema |
|---|---|---|
| [`main`](../../tree/main) | Proyecto base + apuntes completos |  |
| 👉 **[`ej01-estado-en-memoria`](../../tree/ej01-estado-en-memoria)** | **1 — ¿Qué se pierde cuando la app se cierra?** | remember vs rememberSaveable |
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

---

## 🏛️ Arquitectura de esta rama

```
app/src/main/java/com/example/ejercicioskotlinroom/
 ├── MainActivity.kt             # Punto de entrada: muestra la pantalla del ejercicio
 ├── core/                       # tema
 │   └── theme/
 │       ├── Color.kt            # Colores del tema
 │       ├── Theme.kt            # Tema Material 3
 │       └── Type.kt             # Tipografías
 └── presentation/               # una carpeta por pantalla
     └── memoria/
         └── EstadoEnMemoria.kt  # Misma lista con remember y con rememberSaveable para comparar
```

### 📂 Archivos por capa

| Capa | Archivo | Qué hace |
|---|---|---|
| Raíz | `MainActivity.kt` | Punto de entrada: muestra la pantalla del ejercicio |
| Core | `core/theme/Color.kt` | Colores del tema |
| Core | `core/theme/Theme.kt` | Tema Material 3 |
| Core | `core/theme/Type.kt` | Tipografías |
| Presentation | `presentation/memoria/EstadoEnMemoria.kt` | Misma lista con `remember` y con `rememberSaveable` para comparar |

---

## 🎯 Consigna

Lista editable en Compose **sin Room**. Agregar ítems y probar: rotar, volver atrás, cerrar desde recientes y matar el proceso. Registrar qué sobrevive. Tabla “dato / dónde vive / dónde debería vivir”.

## 💡 En pocas palabras

Antes de usar una base de datos hay que ver **el problema**: lo que vive en memoria se pierde. La pantalla tiene la misma lista dos veces, una con `remember` y otra con `rememberSaveable`, para comparar qué sobrevive a cada situación.

## 🔰 Aclaraciones para novatos

- **`remember` no guarda nada en disco:** solo evita que el valor se pierda cuando Compose redibuja. Al rotar, se pierde.
- **`rememberSaveable`** sobrevive a la rotación (Android lo guarda en un “paquetito” de estado), pero no a que el usuario cierre la app.
- **Ninguno de los dos es una base de datos.** Para que algo dure “para siempre” hay que escribirlo en un archivo: eso hace Room.
- **No todo merece guardarse:** el texto a medio escribir o si un diálogo está abierto son del momento; las notas del usuario sí.

## 🔬 Pruebas

La explicación completa con el código, las capturas del emulador y las consultas SQL al archivo real está en los apuntes (sección *Ejercicio 1*).

## ❓ Preguntas de la guía

**1. ¿remember implica persistencia?**  
No. Solo conserva el valor mientras la composición vive; si la Activity se destruye, se pierde.

**2. ¿Diferencia entre conservar estado ante recomposición y conservar datos al reiniciar el proceso?**  
Recomponer es Compose volviendo a ejecutar la función con el proceso vivo: alcanza `remember`. Reiniciar el proceso borra toda la memoria: solo sobrevive lo que está en disco (archivo, DataStore, Room).

**3. ¿Todos los datos de una pantalla deberían guardarse en una base?**  
No. Lo que importa al usuario y debe durar (notas) va a la base; lo del momento (borrador, diálogo abierto, scroll) es estado de UI.

**4. ¿Qué dato sería absurdo persistir?**  
El “estoy escribiendo” del TextField a medio tipear o un flag de diálogo abierto: no sirven al día siguiente y ensucian la base.

## ✅ Criterio de finalización

Caso que se conserva (rememberSaveable al rotar) y caso que se pierde (remember al rotar, ambos al cerrar) documentados. Room resuelve otro problema: guardar en disco para que los datos sobrevivan a todo.

---

## 🚀 Cómo ejecutar

```bash
git clone https://github.com/feliforte14/EJERCICIOS-KOTLIN-ROOM.git
cd EJERCICIOS-KOTLIN-ROOM
git checkout ej01-estado-en-memoria
```

1. Abrir en **Android Studio** y esperar la sincronización de Gradle.
2. **Run ▶** en un emulador o dispositivo (minSdk 24).
3. Ver la base: *View → Tool Windows → App Inspection → Database Inspector*.
