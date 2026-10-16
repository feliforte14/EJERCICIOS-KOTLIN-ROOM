package com.example.ejercicioskotlinroom.prestamos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.ejercicioskotlinroom.NotasApp
import com.example.ejercicioskotlinroom.prestamos.data.EstadoPrestamo
import com.example.ejercicioskotlinroom.prestamos.data.PersonaEntity
import com.example.ejercicioskotlinroom.prestamos.data.PrestamoConPersona
import com.example.ejercicioskotlinroom.prestamos.data.PrestamoEntity
import com.example.ejercicioskotlinroom.prestamos.data.PrestamosRepository
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

data class FiltrosPrestamos(val texto: String = "", val soloPendientes: Boolean = false)

sealed interface EventoPrestamos {
    data class Mensaje(val texto: String) : EventoPrestamos
    data object PrestamoGuardado : EventoPrestamos
    data class PrestamoBorrado(val objeto: String) : EventoPrestamos
}

@OptIn(ExperimentalCoroutinesApi::class)
class PrestamosViewModel(private val repositorio: PrestamosRepository) : ViewModel() {

    private val _filtros = MutableStateFlow(FiltrosPrestamos())
    val filtros: StateFlow<FiltrosPrestamos> = _filtros.asStateFlow()

    // Local-first: la fuente de la pantalla es Room. Cada cambio en la base re-emite la lista (Flow).
    val prestamos: StateFlow<List<PrestamoConPersona>> = _filtros
        .flatMapLatest { repositorio.observarPrestamos(it.texto.trim(), it.soloPendientes) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val pendientes: StateFlow<Int> = repositorio.observarPendientes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val personas: StateFlow<List<PersonaEntity>> = repositorio.observarPersonas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _eventos = Channel<EventoPrestamos>(Channel.BUFFERED)
    val eventos = _eventos.receiveAsFlow()

    private var ultimoBorrado: PrestamoEntity? = null

    fun cambiarBusqueda(texto: String) = _filtros.update { it.copy(texto = texto) }
    fun alternarSoloPendientes() = _filtros.update { it.copy(soloPendientes = !it.soloPendientes) }

    // CREAR. La validación de entrada vive acá (igual criterio que en el ej. 9).
    fun prestar(persona: String, objeto: String, notas: String) {
        val p = persona.trim()
        val o = objeto.trim()
        if (p.isEmpty() || o.isEmpty()) {
            _eventos.trySend(EventoPrestamos.Mensaje("Falta la persona o el objeto"))
            return
        }
        viewModelScope.launch {
            repositorio.prestar(p, o, notas.trim())
            _eventos.send(EventoPrestamos.PrestamoGuardado)
            _eventos.send(EventoPrestamos.Mensaje("\"$o\" prestado a $p"))
        }
    }

    // EDITAR: copy() conserva id, persona y fecha.
    fun editar(original: PrestamoEntity, objeto: String, notas: String) {
        val o = objeto.trim()
        if (o.isEmpty()) {
            _eventos.trySend(EventoPrestamos.Mensaje("El objeto no puede estar vacío"))
            return
        }
        viewModelScope.launch { repositorio.actualizar(original.copy(objeto = o, notas = notas.trim())) }
    }

    fun alternarDevuelto(prestamo: PrestamoEntity) {
        val nuevo = if (prestamo.estado == EstadoPrestamo.PRESTADO) EstadoPrestamo.DEVUELTO else EstadoPrestamo.PRESTADO
        viewModelScope.launch { repositorio.actualizar(prestamo.copy(estado = nuevo)) }
    }

    // ELIMINAR (con deshacer: el dato temporal vive en memoria).
    fun eliminar(prestamo: PrestamoEntity) {
        viewModelScope.launch {
            repositorio.eliminar(prestamo)
            ultimoBorrado = prestamo
            _eventos.send(EventoPrestamos.PrestamoBorrado(prestamo.objeto))
        }
    }

    fun deshacerBorrado() {
        val p = ultimoBorrado ?: return
        ultimoBorrado = null
        viewModelScope.launch { repositorio.restaurar(p) }
    }

    fun eliminarPersona(persona: PersonaEntity) {
        viewModelScope.launch {
            if (!repositorio.eliminarPersona(persona)) {
                _eventos.send(EventoPrestamos.Mensaje("${persona.nombre} todavía tiene préstamos registrados; no se puede borrar"))
            }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as NotasApp
                PrestamosViewModel(app.prestamosRepositorio)
            }
        }
    }
}
