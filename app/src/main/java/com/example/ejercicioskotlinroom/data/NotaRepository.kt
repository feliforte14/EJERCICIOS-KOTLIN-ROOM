package com.example.ejercicioskotlinroom.data

import kotlinx.coroutines.flow.Flow

/*
 * EJERCICIO 9 - Repository (desafío: interfaz + implementación concreta)
 *
 * Cadena de esta capa:  Compose UI -> NotasViewModel -> NotaRepository -> NotaDao -> Room
 *
 * El Repository es la "API de datos" que ve el resto de la app: habla en términos de notas
 * ("agregar", "observar") y esconde que atrás hay Room y SQL. El ViewModel no sabe que existe un DAO.
 *
 * DESAFÍO: ¿aporta algo la interfaz en este tamaño de proyecto?
 *   A favor: el ViewModel depende de una abstracción, así en un test se le puede pasar un repositorio falso
 *   sin base de datos, y si mañana hay una fuente remota se cambia la implementación sin tocar el ViewModel.
 *   En contra: hoy hay UNA sola implementación y la interfaz repite las firmas; es una capa más para mantener.
 *   Mi conclusión: en una app tan chica casi alcanzaría con la clase concreta. La dejo porque la consigna
 *   lo pide y porque cuesta poco, pero no la justificaría en un proyecto de este tamaño si no hubiera tests ni
 *   segunda fuente de datos.
 *
 * PREGUNTAS DEL EJERCICIO 9
 * 1) ¿Qué gana el proyecto con esta separación y qué costo agrega?
 *    Gana: la UI deja de conocer el DAO; cada capa tiene una responsabilidad (UI dibuja, ViewModel guarda estado y
 *    reglas de pantalla, Repository coordina datos, DAO habla SQL); el estado sobrevive a la rotación porque vive
 *    en el ViewModel; es más fácil de probar. Cuesta: más archivos y más "pasamanos" (métodos que solo delegan).
 * 2) ¿El Repository tiene que existir siempre? Justificar para este caso.
 *    No. Si solo reenvía llamadas al DAO es una capa sin aporte. Acá hace algo más que pasar: genera creadaEn,
 *    arma la entidad y es el punto donde después entraría una fuente remota. Por eso lo mantengo, aunque justo.
 * 3) ¿Qué clase debería decidir si un título vacío es válido?
 *    El ViewModel: es una regla de la pantalla/entrada de usuario (y es quien arma el mensaje de error). El
 *    Repository asume que le llegan datos válidos y el DAO no valida nada. Antes lo hacía una función suelta
 *    de la UI, que es el lugar que menos corresponde.
 * 4) Si mañana los datos vinieran de red y de Room, ¿qué parte coordinaría?
 *    El Repository: decide de dónde leer (Room como fuente de verdad), cuándo refrescar desde la red y cómo
 *    guardar el resultado en Room. ViewModel y UI no se enteran.
 */
interface NotaRepository {
    fun observarNotas(texto: String, soloImportantes: Boolean, ascendente: Boolean): Flow<List<NotaEntity>>
    fun observarCantidad(): Flow<Int>
    fun observarCantidadImportantes(): Flow<Int>

    suspend fun agregar(titulo: String, contenido: String, importante: Boolean): Long
    suspend fun actualizar(nota: NotaEntity): Boolean
    suspend fun eliminar(nota: NotaEntity)
    suspend fun restaurar(nota: NotaEntity)
}

// Implementación con Room. Recibe el DAO por constructor (no crea la base: esa es única y vive en NotasApp).
class NotaRepositoryImpl(private val dao: NotaDao) : NotaRepository {

    override fun observarNotas(texto: String, soloImportantes: Boolean, ascendente: Boolean) =
        dao.filtrar(texto, soloImportantes, ascendente)

    override fun observarCantidad() = dao.observarCantidad()
    override fun observarCantidadImportantes() = dao.observarCantidadImportantes()

    override suspend fun agregar(titulo: String, contenido: String, importante: Boolean): Long =
        dao.insertar(
            NotaEntity(
                titulo = titulo,
                contenido = contenido,
                importante = importante,
                creadaEn = System.currentTimeMillis() // el momento del guardado lo fija esta capa
            )
        )

    // Una actualización es correcta si tocó exactamente una fila (la que tiene ese id).
    override suspend fun actualizar(nota: NotaEntity): Boolean = dao.actualizar(nota) == 1

    override suspend fun eliminar(nota: NotaEntity) {
        dao.eliminar(nota)
    }

    // Reinserta una nota borrada con su id y su fecha originales (deshacer).
    override suspend fun restaurar(nota: NotaEntity) {
        dao.insertar(nota)
    }
}
