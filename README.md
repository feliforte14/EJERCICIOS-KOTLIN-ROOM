# EJERCICIOS-KOTLIN-ROOM

Ejercicios de la guía de cátedra **Persistencia local con Room** (Desarrollo de Aplicaciones I, UADE).
Android + Kotlin + Jetpack Compose. Un solo proyecto, una rama por ejercicio.

Las ramas son **incrementales**: cada una parte de la anterior (como pide la guía, desde el ej. 4 todo depende de lo anterior).
Los comentarios en el código explican las decisiones y contestan las preguntas de cada consigna.

| Rama | Ejercicio |
|---|---|
| `main` | Proyecto base (compila, sin Room) |
| `ej01-estado-en-memoria` | 1 - Qué se pierde cuando la app se cierra |
| `ej02-entity` | 2 - Primera Entity |
| `ej03-dao` | 3 - DAO |
| `ej04-database` | 4 - RoomDatabase y Database Inspector |
| `ej05-alta-compose` | 5 - Alta de datos desde Compose |
| `ej06-flow` | 6 - Lectura con Flow |
| `ej07-crud` | 7 - CRUD completo |
| `ej08-consultas` | 8 - Búsqueda, filtros y orden |
| `ej09-repository-viewmodel` | 9 - Repository + ViewModel |
| `ej10-restricciones-indices` | 10 - Restricciones, índices y conflictos |
| `ej11-relacion-uno-a-muchos` | 11 - Categorías (1:N) |
| `ej12-typeconverters` | 12 - TypeConverters |
| `ej13-transacciones` | 13 - Transacciones |
| `ej14-migraciones` | 14 - Migración 1 -> 2 |
| `ej15-miniapp-integradora` | 15 - Miniapp integradora (préstamos de objetos) |

Compilar: `./gradlew :app:assembleDebug`
