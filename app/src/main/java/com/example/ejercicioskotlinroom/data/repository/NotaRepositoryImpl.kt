package com.example.ejercicioskotlinroom.data.repository

import com.example.ejercicioskotlinroom.data.local.dao.NotaDao
import com.example.ejercicioskotlinroom.data.local.entity.NotaEntity

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
