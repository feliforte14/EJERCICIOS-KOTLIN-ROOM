package com.example.ejercicioskotlinroom

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.ejercicioskotlinroom.data.CategoriaConNotas
import com.example.ejercicioskotlinroom.data.CategoriaEntity
import com.example.ejercicioskotlinroom.data.NotaEntity
import com.example.ejercicioskotlinroom.data.NotaRepository
import com.example.ejercicioskotlinroom.data.Prioridad
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/*
 * EJERCICIO 9 - ViewModel
 *
 * Expone estado estable a Compose (StateFlow) y acciones de alto nivel: guardarNota, eliminarNota,
 * cambiarImportante, actualizarNota, deshacerBorrado. La UI NO llama al DAO: habla solo con este ViewModel.
 *
 * Recorrido de una inserción, de ida y vuelta:
 *   IDA:    botón Guardar -> viewModel.guardarNota() -> valida -> repository.agregar() -> dao.insertar() -> SQLite
 *   VUELTA: SQLite cambia -> Room invalida y re-emite el Flow del DAO -> repository.observarNotas() ->
 *           flatMapLatest + stateIn en `notas` -> collectAsStateWithLifecycle en la pantalla -> LazyColumn se recompone.
 */

// Filtros de la pantalla agrupados en un solo objeto inmutable.
data class Filtros(
    val texto: String = "",
    val soloImportantes: Boolean = false,
    val ascendente: Boolean = false
)

// Cosas que pasan una sola vez y la UI tiene que reaccionar (no son "estado" que se queda en pantalla).
sealed interface EventoUi {
    data class Mensaje(val texto: String) : EventoUi
    data object NotaGuardada : EventoUi
    data class NotaBorrada(val titulo: String) : EventoUi
}

@OptIn(ExperimentalCoroutinesApi::class)
class NotasViewModel(private val repositorio: NotaRepository) : ViewModel() {

    private val _filtros = MutableStateFlow(Filtros())
    val filtros: StateFlow<Filtros> = _filtros.asStateFlow()

    // Cada vez que cambian los filtros, flatMapLatest cancela la consulta anterior y pasa a la nueva.
    // stateIn la convierte en StateFlow: tiene valor inicial y la comparten todos los que la miran.
    // WhileSubscribed(5000): si la UI deja de mirar (ej. rotación) la consulta sigue 5 s y no se reinicia.
    val notas: StateFlow<List<NotaEntity>> = _filtros
        .flatMapLatest { f -> repositorio.observarNotas(f.texto.trim(), f.soloImportantes, f.ascendente) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val total: StateFlow<Int> = repositorio.observarCantidad()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)
    val totalImportantes: StateFlow<Int> = repositorio.observarCantidadImportantes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    // Ej. 11: categorías para los chips del formulario y el panel con sus notas (relación 1:N).
    val categorias: StateFlow<List<CategoriaEntity>> = repositorio.observarCategorias()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val categoriasConNotas: StateFlow<List<CategoriaConNotas>> = repositorio.observarCategoriasConNotas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // El guardado en curso es estado de pantalla: evita el doble toque (ej. 5).
    private val _guardando = MutableStateFlow(false)
    val guardando: StateFlow<Boolean> = _guardando.asStateFlow()

    private val _eventos = Channel<EventoUi>(Channel.BUFFERED)
    val eventos = _eventos.receiveAsFlow()

    // Para "Deshacer": dato temporal en memoria (se pierde si muere el proceso).
    private var ultimaBorrada: NotaEntity? = null

    fun cambiarBusqueda(texto: String) = _filtros.update { it.copy(texto = texto) }
    fun alternarSoloImportantes() = _filtros.update { it.copy(soloImportantes = !it.soloImportantes) }
    fun alternarOrden() = _filtros.update { it.copy(ascendente = !it.ascendente) }

    // Regla de validación (pregunta 3 de la guía): vive acá, no en la UI ni en el Repository.
    fun guardarNota(
        titulo: String,
        contenido: String,
        importante: Boolean,
        codigoExterno: String = "",
        categoriaId: Int? = null,
        prioridad: Prioridad = Prioridad.MEDIA
    ) {
        if (_guardando.value) return // segundo toque mientras guarda: se ignora
        val tituloLimpio = titulo.trim()
        if (tituloLimpio.isEmpty()) {
            _eventos.trySend(EventoUi.Mensaje("El título no puede estar vacío"))
            return
        }
        _guardando.value = true
        viewModelScope.launch {
            try {
                val id = repositorio.agregar(tituloLimpio, contenido.trim(), importante, codigoExterno, categoriaId, prioridad)
                if (id == -1L) {
                    // Ej. 10: IGNORE devolvió -1 -> el código ya existe. No se limpia el formulario para que se pueda corregir.
                    _eventos.send(EventoUi.Mensaje("Ya existe una nota con ese código externo"))
                } else {
                    // Primero limpiar el formulario y después el mensaje: el snackbar suspende al colector hasta que se cierra.
                    _eventos.send(EventoUi.NotaGuardada)
                    _eventos.send(EventoUi.Mensaje("Nota guardada (id $id)"))
                }
            } finally {
                _guardando.value = false
            }
        }
    }

    // Editar: copy() sobre la nota original conserva id y creadaEn, así @Update toca la misma fila.
    fun actualizarNota(original: NotaEntity, titulo: String, contenido: String, importante: Boolean) {
        val tituloLimpio = titulo.trim()
        if (tituloLimpio.isEmpty()) {
            _eventos.trySend(EventoUi.Mensaje("El título no puede estar vacío"))
            return
        }
        viewModelScope.launch {
            val ok = repositorio.actualizar(
                original.copy(titulo = tituloLimpio, contenido = contenido.trim(), importante = importante)
            )
            if (!ok) _eventos.send(EventoUi.Mensaje("La nota ya no existe"))
        }
    }

    fun agregarCategoria(nombre: String) {
        val limpio = nombre.trim()
        if (limpio.isEmpty()) {
            _eventos.trySend(EventoUi.Mensaje("El nombre de la categoría no puede estar vacío"))
            return
        }
        viewModelScope.launch {
            if (repositorio.agregarCategoria(limpio) == -1L) {
                _eventos.send(EventoUi.Mensaje("Ya existe la categoría \"$limpio\""))
            }
        }
    }

    fun eliminarCategoria(categoria: CategoriaEntity) {
        viewModelScope.launch { repositorio.eliminarCategoria(categoria) }
    }

    fun cambiarImportante(nota: NotaEntity) {
        viewModelScope.launch { repositorio.actualizar(nota.copy(importante = !nota.importante)) }
    }

    fun eliminarNota(nota: NotaEntity) {
        viewModelScope.launch {
            repositorio.eliminar(nota)
            ultimaBorrada = nota
            _eventos.send(EventoUi.NotaBorrada(nota.titulo))
        }
    }

    fun deshacerBorrado() {
        val nota = ultimaBorrada ?: return
        ultimaBorrada = null
        viewModelScope.launch { repositorio.restaurar(nota) }
    }

    companion object {
        // Fábrica: le da al ViewModel el repositorio que arma la Application (que tiene la única base).
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as NotasApp
                NotasViewModel(app.repositorio)
            }
        }
    }
}
