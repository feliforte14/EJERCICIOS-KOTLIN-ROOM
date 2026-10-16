package com.example.ejercicioskotlinroom.prestamos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ejercicioskotlinroom.prestamos.data.EstadoPrestamo
import com.example.ejercicioskotlinroom.prestamos.data.PrestamoConPersona

/*
 * EJERCICIO 15 - ETAPA 3: uso cotidiano. La pantalla permite:
 *   1) crear (persona + objeto + nota),  2) listar de forma reactiva (Flow),  3) editar,  4) eliminar (con confirmación y deshacer),
 *   5) buscar por objeto / filtrar pendientes,  6) mostrar la relación (cada préstamo muestra a qué persona),
 *   7) sobrevivir a un reinicio completo: todo sale de Room (prestamos.db); no hay datos "de ejemplo" en memoria.
 * La UI solo conoce el ViewModel (nunca el DAO ni Room).
 */
@Composable
fun PantallaPrestamos(
    modifier: Modifier = Modifier,
    viewModel: PrestamosViewModel = viewModel(factory = PrestamosViewModel.Factory)
) {
    val snackbar = remember { SnackbarHostState() }

    // Borrador del formulario: estado de UI efímero (no se persiste hasta que se toca "Prestar").
    var persona by rememberSaveable { mutableStateOf("") }
    var objeto by rememberSaveable { mutableStateOf("") }
    var notas by rememberSaveable { mutableStateOf("") }
    var enEdicion by remember { mutableStateOf<PrestamoConPersona?>(null) }
    var porBorrar by remember { mutableStateOf<PrestamoConPersona?>(null) }

    val prestamos by viewModel.prestamos.collectAsStateWithLifecycle()
    val filtros by viewModel.filtros.collectAsStateWithLifecycle()
    val pendientes by viewModel.pendientes.collectAsStateWithLifecycle()
    val personas by viewModel.personas.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.eventos.collect { e ->
            when (e) {
                EventoPrestamos.PrestamoGuardado -> { persona = ""; objeto = ""; notas = "" }
                is EventoPrestamos.Mensaje -> snackbar.showSnackbar(e.texto)
                is EventoPrestamos.PrestamoBorrado -> {
                    val r = snackbar.showSnackbar("\"${e.objeto}\" borrado", actionLabel = "Deshacer")
                    if (r == SnackbarResult.ActionPerformed) viewModel.deshacerBorrado()
                }
            }
        }
    }

    Box(modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item { Text("Ejercicio 15 - Objetos prestados", style = MaterialTheme.typography.titleLarge) }

            // ---- Alta ----
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(persona, { persona = it }, label = { Text("¿A quién?") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(objeto, { objeto = it }, label = { Text("¿Qué objeto?") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(notas, { notas = it }, label = { Text("Notas (opcional)") }, modifier = Modifier.fillMaxWidth())
                    Button(onClick = { viewModel.prestar(persona, objeto, notas) }) { Text("Prestar") }
                }
            }

            // ---- Búsqueda y filtro ----
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    HorizontalDivider()
                    OutlinedTextField(
                        filtros.texto, viewModel::cambiarBusqueda,
                        label = { Text("Buscar objeto") }, singleLine = true, modifier = Modifier.fillMaxWidth()
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = filtros.soloPendientes,
                            onClick = viewModel::alternarSoloPendientes,
                            label = { Text("Solo pendientes") }
                        )
                        // Sale de COUNT(*) en SQL, no de un número mantenido a mano.
                        Text("Sin devolver: $pendientes")
                    }
                }
            }

            if (prestamos.isEmpty()) {
                item { Text(if (filtros.texto.isBlank() && !filtros.soloPendientes) "Todavía no prestaste nada" else "Sin resultados") }
            }
            items(prestamos, key = { it.prestamo.id }) { item ->
                val p = item.prestamo
                // Dato NO persistido: los días se calculan acá a partir de fechaPrestamo.
                val dias = (System.currentTimeMillis() - p.fechaPrestamo) / 86_400_000L
                Column(Modifier.fillMaxWidth()) {
                    Text(
                        "${p.objeto} → ${item.persona.nombre}",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        "${if (p.estado == EstadoPrestamo.PRESTADO) "Prestado hace $dias días" else "Devuelto"}" +
                            (if (p.notas.isNotBlank()) " · ${p.notas}" else ""),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Row {
                        TextButton(onClick = { viewModel.alternarDevuelto(p) }) {
                            Text(if (p.estado == EstadoPrestamo.PRESTADO) "Marcar devuelto" else "Volver a prestado")
                        }
                        TextButton(onClick = { enEdicion = item }) { Text("Editar") }
                        TextButton(onClick = { porBorrar = item }) { Text("Borrar") }
                    }
                }
            }

            // ---- Personas (ejercita la FK con RESTRICT) ----
            item { HorizontalDivider() }
            item { Text("Personas", style = MaterialTheme.typography.titleMedium) }
            items(personas, key = { "persona-${it.id}" }) { per ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(per.nombre, modifier = Modifier.weight(1f))
                    TextButton(onClick = { viewModel.eliminarPersona(per) }) { Text("Borrar") }
                }
            }
        }
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter))
    }

    enEdicion?.let { item ->
        var objetoEd by remember(item.prestamo.id) { mutableStateOf(item.prestamo.objeto) }
        var notasEd by remember(item.prestamo.id) { mutableStateOf(item.prestamo.notas) }
        AlertDialog(
            onDismissRequest = { enEdicion = null },
            title = { Text("Editar préstamo a ${item.persona.nombre}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(objetoEd, { objetoEd = it }, label = { Text("Objeto") }, singleLine = true)
                    OutlinedTextField(notasEd, { notasEd = it }, label = { Text("Notas") })
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.editar(item.prestamo, objetoEd, notasEd)
                    enEdicion = null
                }) { Text("Guardar cambios") }
            },
            dismissButton = { TextButton(onClick = { enEdicion = null }) { Text("Cancelar") } }
        )
    }

    porBorrar?.let { item ->
        AlertDialog(
            onDismissRequest = { porBorrar = null },
            title = { Text("¿Borrar el registro?") },
            text = { Text("\"${item.prestamo.objeto}\" prestado a ${item.persona.nombre}") },
            confirmButton = {
                TextButton(onClick = { viewModel.eliminar(item.prestamo); porBorrar = null }) { Text("Borrar") }
            },
            dismissButton = { TextButton(onClick = { porBorrar = null }) { Text("Cancelar") } }
        )
    }
}
