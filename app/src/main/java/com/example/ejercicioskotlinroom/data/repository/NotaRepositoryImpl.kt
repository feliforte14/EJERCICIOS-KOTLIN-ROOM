package com.example.ejercicioskotlinroom.data.repository

import com.example.ejercicioskotlinroom.data.local.dao.CategoriaDao
import com.example.ejercicioskotlinroom.data.local.dao.NotaDao
import com.example.ejercicioskotlinroom.data.local.entity.CategoriaEntity
import com.example.ejercicioskotlinroom.data.local.entity.NotaEntity

// Implementación con Room. Recibe el DAO por constructor (no crea la base: esa es única y vive en NotasApp).
class NotaRepositoryImpl(
    private val dao: NotaDao,
    private val categoriaDao: CategoriaDao
) : NotaRepository {

    override fun observarNotas(texto: String, soloImportantes: Boolean, ascendente: Boolean) =
        dao.filtrar(texto, soloImportantes, ascendente)

    override fun observarCantidad() = dao.observarCantidad()
    override fun observarCantidadImportantes() = dao.observarCantidadImportantes()

    // Devuelve el id nuevo, o -1 si el codigoExterno ya existe (IGNORE no inserta).
    override suspend fun agregar(
        titulo: String,
        contenido: String,
        importante: Boolean,
        codigoExterno: String?,
        categoriaId: Int?
    ): Long =
        dao.insertar(
            NotaEntity(
                titulo = titulo,
                contenido = contenido,
                importante = importante,
                // "" no es "sin código": chocaría con otro "". Lo normalizo a null.
                codigoExterno = codigoExterno?.trim()?.ifEmpty { null },
                categoriaId = categoriaId,
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

    override fun observarCategorias() = categoriaDao.observarCategorias()
    override fun observarCategoriasConNotas() = categoriaDao.observarCategoriasConNotas()

    // -1 si ya existe una categoría con ese nombre.
    override suspend fun agregarCategoria(nombre: String): Long = categoriaDao.insertar(CategoriaEntity(nombre = nombre))

    override suspend fun eliminarCategoria(categoria: CategoriaEntity) {
        categoriaDao.eliminar(categoria) // sus notas quedan con categoriaId = NULL (SET_NULL)
    }
}
