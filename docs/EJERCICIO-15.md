# Ejercicio 15 - Miniapp integradora: objetos prestados (local-first)

Registro de objetos que presté y a quién. Funciona sin red: la fuente de datos de la pantalla es Room (`prestamos.db`).
Código en `app/src/main/java/com/example/ejercicioskotlinroom/prestamos/`.

## Etapa 1 - Modelo

```
personas                         prestamos
+----+--------+                  +----+--------+-----------+---------------+---------+-------+
| id | nombre |  1 --------- N   | id | objeto | personaId | fechaPrestamo | estado  | notas |
+----+--------+                  +----+--------+-----------+---------------+---------+-------+
PK id, UNIQUE(nombre)            PK id, FK personaId -> personas.id (ON DELETE RESTRICT)
                                 índices: personaId ; (estado, fechaPrestamo)
```

- **Entidades y claves primarias:** `PersonaEntity.id`, `PrestamoEntity.id` (autogeneradas).
- **Relación:** una persona tiene muchos préstamos (1:N). La clave foránea está en `prestamos.personaId`.
- **Dato que no se persiste:** los días que lleva prestado un objeto (se calculan con `fechaPrestamo`, guardarlos se desactualiza
  al día siguiente) y todo el estado de UI (texto de búsqueda, filtro activo, diálogos abiertos).
- **Unicidad / integridad:** `UNIQUE(personas.nombre)` y FK con `RESTRICT` (no se puede borrar una persona con préstamos:
  el historial es lo que la app protege).
- `estado` es un enum (`PRESTADO`, `DEVUELTO`) guardado como texto con un `TypeConverter`.

## Etapas 2 y 3 - Persistencia y uso cotidiano

DAO (`PrestamosDao`), `RoomDatabase` (`PrestamosDatabase`) y una pantalla (`PantallaPrestamos`) con: crear, listar reactivo
(Flow), editar, eliminar (confirmación + deshacer), buscar por objeto, filtrar pendientes, mostrar la relación (cada préstamo
muestra la persona) y sobrevivir a un reinicio completo. "Prestar" usa una transacción: crea la persona si no existe y registra el préstamo.

## Etapa 4 - Arquitectura y cambio de esquema

`UI -> PrestamosViewModel -> PrestamosRepository -> PrestamosDao -> Room`.
Cambio de esquema: versión 1 -> 2 agrega `prestamos.notas TEXT NOT NULL DEFAULT ''`
(`PRESTAMOS_MIGRATION_1_2` en `PrestamosDatabase.kt`). Probado en `PrestamosMigrationTest` (crea la base v1 con dos préstamos,
migra, y comprueba que las filas siguen y que `notas` vale `''`).

## Etapa 5 - Mirar la app como producto

1. **¿Qué dato sería costoso perder después de seis meses?**
   La lista de préstamos pendientes: es justo lo que no recordás de cabeza (quién tiene qué desde cuándo). Perder el historial
   de lo ya devuelto duele menos, pero sirve para saber a quién le presté y cumplió.
2. **¿Qué pasaría si se agrega sincronización con servidor?**
   Aparecen dos copias del mismo dato y hace falta definir de cuál se fía la app y cómo se resuelven los conflictos (dos
   dispositivos que editan el mismo préstamo). Los ids autogenerados locales dejarían de servir como identidad global (haría
   falta un id que asigne el cliente, como un UUID, o uno del servidor). El lugar natural para coordinarlo es el Repository, que hoy
   ya separa la UI de Room.
3. **¿Qué dato es la fuente de verdad mientras no haya red?**
   Lo que hay en Room. Si algún día hay servidor, Room seguiría siendo la fuente de la pantalla y el servidor una réplica
   que se sincroniza cuando hay conexión (por eso "local-first").
4. **¿Qué error de persistencia sería más grave que un error visual?**
   Perder o corromper préstamos (por ejemplo una migración destructiva, un `REPLACE` que pise un registro o un borrado en
   cascada accidental). Un error visual se arregla con la próxima versión; un registro perdido no vuelve. Por eso la FK es
   `RESTRICT`, la migración conserva datos y no uso `fallbackToDestructiveMigration`.

## Cómo explicar cada tabla en Database Inspector

- `personas`: quién recibió algo. Una fila por persona (nombre único).
- `prestamos`: cada objeto que salió de casa. Apunta a la persona con `personaId`, guarda cuándo (`fechaPrestamo`, epoch ms),
  el estado (`PRESTADO`/`DEVUELTO` como texto) y una nota libre (`notas`, desde la versión 2).
- `room_master_table`: tabla interna de Room con el hash de identidad del esquema.
