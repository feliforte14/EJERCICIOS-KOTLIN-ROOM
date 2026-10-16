package com.example.ejercicioskotlinroom

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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.ejercicioskotlinroom.data.NotaEntity

/*
 * EJERCICIO 10 - Restricciones, índices y conflictos
 *
 * Agrego el campo opcional "Código externo" al formulario. Si se repite, el insert se ignora (-1) y la pantalla
 * avisa en vez de guardar en silencio o romper. El detalle de índices y estrategias está en NotaEntity.kt y NotaDao.kt.
 *
 * ---- (Texto del ejercicio 9, sigue valiendo) ----
 * EJERCICIO 9 - Poner orden: Repository, ViewModel y Room
 *
 * La pantalla ahora se parece a la del ej. 8 (mismo comportamiento) pero ya NO conoce el DAO:
 *   Compose UI -> NotasViewModel -> NotaRepository -> NotaDao -> Room
 * Las funciones sueltas guardarNota()/actualizarNota() que vivían acá pasaron al ViewModel y al Repository.
 * Lo único que queda en la UI es estado puramente visual: el borrador del formulario, qué diálogo está abierto.
 * Las respuestas a las preguntas del ejercicio están en NotaRepository.kt y NotasViewModel.kt.
 *
 * ---- (Texto del ejercicio 8, sigue valiendo) ----
 * EJERCICIO 8 - Consultas que empiezan a parecerse a una app real (sobre lo anterior)
 *
 * Buscador por título + filtro "solo importantes" + botón de orden por fecha + contadores.
 * Todo el filtrado ocurre en SQL (NotaDao.filtrar); la pantalla no trae toda la tabla para filtrarla en Kotlin.
 * El DAO cubre los 5 puntos de la consigna: buscarPorTitulo, observarImportantes, orden asc/desc,
 * observarCantidad y observarCantidadImportantes. La pantalla usa la versión combinada (filtrar) para poder
 * mezclar búsqueda, filtro y orden a la vez, y los dos contadores que salen de COUNT(*), no de números llevados a mano.
 *
 * PREGUNTAS DEL EJERCICIO 8
 * 1) ¿Qué ventaja tiene filtrar en SQL en vez de traer todo y usar filter?
 *    SQLite recorre y descarta filas en el motor (y puede usar índices): a la app solo llega el resultado.
 *    Con filter en Kotlin se cargan TODAS las filas a memoria y se crean todos los objetos para tirar la mayoría.
 *    Con miles de notas eso es memoria y tiempo de más.
 * 2) ¿Cómo entra el parámetro :texto en la consulta?
 *    Room toma el parámetro del método con el mismo nombre (texto) y lo manda como argumento enlazado (bind arg);
 *    no concatena el String en el SQL. Eso evita inyección SQL y deja al motor reutilizar la consulta.
 * 3) ¿Diferencia entre ordenar en SQL y ordenar la lista en memoria?
 *    En SQL ordena el motor antes de devolver las filas (y puede apoyarse en un índice); en memoria hay que cargar
 *    todo primero y ordenar con CPU de la app. Además el Flow ya emite la lista ordenada.
 * 4) ¿Qué consulta podría beneficiarse de un índice si la tabla creciera mucho?
 *    El ORDER BY creadaEn (se usa en todas las consultas) y el filtro por importante. LIKE '%texto%' con el comodín
 *    adelante NO aprovecha un índice común sobre titulo (hace falta recorrer todo). Los índices reales se agregan
 *    en el ejercicio 10.
 *
 * ---- (Texto del ejercicio 7, sigue valiendo) ----
 * EJERCICIO 7 - CRUD completo: editar y eliminar (sobre lo del ej. 5 y 6)
 *
 * DAO nuevo: @Update actualizar(nota) y @Delete eliminar(nota).
 * Desde la lista: tocar "Editar" abre un diálogo con título, contenido e importante; "Guardar cambios" hace el update.
 * "Borrar" pide una confirmación mínima (AlertDialog) antes de eliminar.
 * Comprobación de que editar NO crea una fila nueva: el id se muestra en la lista (#id) y no cambia después de editar;
 * en Database Inspector sigue habiendo la misma cantidad de filas.
 *
 * PREGUNTAS DEL EJERCICIO 7
 * 1) ¿Cómo sabe @Update qué fila debe modificar?
 *    Por la clave primaria del objeto que le paso: genera UPDATE notas SET ... WHERE id = :id con el id de la nota.
 *    Por eso al editar uso nota.copy(...) y no armo una NotaEntity nueva.
 * 2) ¿Qué sucede si se construye un objeto editado con otro id?
 *    Con id distinto actualiza OTRA fila (la de ese id), pisándole los datos. Con un id que no existe no
 *    modifica nada y devuelve 0. Con id = 0 tampoco encuentra fila (el 0 no es un id real).
 *    Para insertar uno nuevo hay que usar @Insert, no @Update.
 * 3) ¿Conviene borrar apenas se toca un ícono o pedir confirmación?
 *    Pedir confirmación: borrar es destructivo y un toque accidental es muy fácil en el celu. Es decisión de
 *    producto además de código. Un término medio (el desafío) es borrar directo pero ofrecer "Deshacer".
 *    Acá hice las dos: diálogo de confirmación + Snackbar con Deshacer.
 * 4) ¿Qué parte de esta pantalla podría sobrevivir en memoria sin persistirse?
 *    Qué nota se está editando, si el diálogo de borrado está abierto, el borrador del formulario y la nota
 *    borrada que guardo para el "Deshacer": todo estado de UI temporal.
 *
 * Desafío (Deshacer solo a nivel de interfaz): al borrar, guardo la NotaEntity eliminada en memoria
 * (con su id y su creadaEn originales, que es lo que tiene que conservarse) y el Snackbar ofrece "Deshacer",
 * que la vuelve a insertar. Como el id != 0, Room respeta ese id y la nota vuelve en el mismo lugar.
 * Si la app se cierra antes de deshacer, esa nota se pierde: el deshacer no es persistente.
 *
 * ---- (Texto del ejercicio 6, sigue valiendo) ----
 * EJERCICIO 6 - Leer con Flow y dejar que la pantalla reaccione (sobre el formulario del ej. 5)
 *
 * Cambio: el DAO expone observarNotas(): Flow<List<NotaEntity>> y la pantalla lo consume con
 * collectAsStateWithLifecycle(). La lista se actualiza sola cuando se inserta una fila: ya no hay
 * que releer a mano después de guardar ni botón "Recargar".
 *
 * Prueba concreta (a hacer en Android Studio): dejar la lista abierta y agregar una fila desde Database Inspector
 * (tabla notas > "Add row"). Lo esperable es que la lista se actualice sola, porque Room invalida el Flow cuando
 * cambia la tabla "notas"; Database Inspector avisa a Room de las escrituras que hace. Falta anotar qué se vio
 * realmente en el equipo (si no se actualiza, probar insertando desde la propia app para descartar el inspector).
 *
 * PREGUNTAS DEL EJERCICIO 6
 * 1) ¿Diferencia entre Flow<List<NotaEntity>> y List<NotaEntity>?
 *    List es una foto del momento en que se consultó: no se entera de cambios posteriores.
 *    Flow es un flujo que emite una lista cada vez que la tabla cambia; el que lo recolecta recibe
 *    las actualizaciones sin volver a pedir.
 * 2) ¿Room vuelve a ejecutar la consulta cuando detecta cambios en la tabla relevante?
 *    Sí. Mientras haya alguien recolectando el Flow, Room vuelve a correr el SELECT cada vez que
 *    se invalida alguna tabla que la consulta lee (notas) y emite el resultado nuevo.
 * 3) ¿Es lo mismo un Flow que un estado de Compose?
 *    No. Flow es de corrutinas (stream asíncrono, no sabe nada de la UI). El State de Compose guarda un
 *    valor y le avisa a Compose que recomponga. collectAsStateWithLifecycle convierte uno en el otro.
 * 4) ¿Qué responsabilidad tiene collectAsState / collectAsStateWithLifecycle en este recorrido?
 *    Es el puente Flow -> State: recolecta el Flow, guarda el último valor en un State (con un valor inicial,
 *    emptyList() acá) y dispara recomposición en cada emisión. La versión WithLifecycle además deja de
 *    recolectar cuando la app pasa a segundo plano (por debajo de STARTED) y retoma al volver: no gasta
 *    recursos ni mantiene la consulta viva sin que nadie la mire.
 *
 * Cadena completa: Room (tabla cambia) -> Flow emite lista nueva -> collectAsStateWithLifecycle
 * actualiza el State -> LazyColumn se recompone.
 *
 * Desafío: estado vacío "Todavía no hay notas" derivado de la MISMA lista (notas.isEmpty()),
 * sin inventar una segunda lista aparte.
 *
 * ---- (Texto del ejercicio 5, sigue valiendo) ----
 * EJERCICIO 5 - Alta de datos desde Compose
 *
 * Formulario: título, contenido, importante y botón Guardar, con mensaje corto al terminar.
 * La pantalla llama a una función intermedia simple (guardarNota); todavía no hay Repository/ViewModel (ej. 9).
 *
 * Validaciones:
 *   1) título vacío -> no se guarda y se avisa (no se inserta "en silencio").
 *   2) trim() de título y contenido antes de guardar.
 *   3) doble toque: mientras hay un guardado en curso el botón se deshabilita (flag `guardando`).
 *      Cómo observar el problema SIN el flag: sacar `enabled = !guardando` y el chequeo, tocar dos veces rápido
 *      y mirar en Database Inspector: aparecen dos filas iguales con ids distintos. Es la razón del flag.
 *
 * Prueba de persistencia: guardar 4 notas, cerrar la app por completo (deslizar de recientes) y abrir de nuevo;
 * la lista (todavía básica, se carga una vez al entrar) vuelve a mostrar las 4 y se confirman en Database Inspector.
 *
 * PREGUNTAS
 * 1) ¿Qué dato de la UI no debería terminar en la tabla?
 *    El estado de la propia pantalla: el texto a medio escribir, el mensaje "Guardada", el flag `guardando`.
 *    En la tabla va solo lo que forma la nota (titulo, contenido, importante, creadaEn, colorEtiqueta).
 *
 * 2) ¿Dónde se genera creadaEn y por qué?
 *    En guardarNota(), justo antes de insertar, con System.currentTimeMillis(). No lo carga el usuario
 *    (no es un campo del formulario) y tiene que ser el momento real del guardado, no el de abrir la pantalla.
 *    Más adelante lo asume el ViewModel/Repository (ej. 9).
 *
 * 3) ¿Qué señal usaría para saber que una inserción terminó bien?
 *    El Long que devuelve insertar(): si es un id mayor a 0, la fila existe. Eso es lo que muestro en el mensaje
 *    ("Nota guardada (id N)"). Para confirmar de verdad, Database Inspector.
 *
 * 4) ¿Qué riesgo hay en lanzar una corrutina nueva en cualquier lugar solo para "hacer que funcione"?
 *    Si el scope no está atado al ciclo de vida, la corrutina puede seguir corriendo con la pantalla muerta
 *    (fugas, escribir en estado que ya no existe) o lanzarse varias veces (duplicados, como en el doble toque).
 *    Acá uso rememberCoroutineScope(), que se cancela cuando el composable sale de la composición.
 *    Cuando haya ViewModel (ej. 9), el lugar correcto es viewModelScope.
 */

@Composable
fun PantallaNotas(
    modifier: Modifier = Modifier,
    // viewModel(factory) devuelve el MISMO ViewModel tras rotaciones: por eso el estado sobrevive.
    viewModel: NotasViewModel = viewModel(factory = NotasViewModel.Factory)
) {
    val snackbarHostState = remember { SnackbarHostState() }

    // Borrador del formulario: estado de UI efímero -> rememberSaveable, no va al ViewModel ni a la base.
    var titulo by rememberSaveable { mutableStateOf("") }
    var contenido by rememberSaveable { mutableStateOf("") }
    var importante by rememberSaveable { mutableStateOf(false) }
    var codigoExterno by rememberSaveable { mutableStateOf("") }

    var notaEnEdicion by remember { mutableStateOf<NotaEntity?>(null) }
    var notaPorBorrar by remember { mutableStateOf<NotaEntity?>(null) }

    // Estado que viene del ViewModel (StateFlow -> State de Compose, atado al ciclo de vida).
    val notas by viewModel.notas.collectAsStateWithLifecycle()
    val filtros by viewModel.filtros.collectAsStateWithLifecycle()
    val total by viewModel.total.collectAsStateWithLifecycle()
    val totalImportantes by viewModel.totalImportantes.collectAsStateWithLifecycle()
    val guardando by viewModel.guardando.collectAsStateWithLifecycle()

    // Eventos de una sola vez: mensajes, limpiar formulario, snackbar de deshacer.
    LaunchedEffect(viewModel) {
        viewModel.eventos.collect { evento ->
            when (evento) {
                is EventoUi.Mensaje -> snackbarHostState.showSnackbar(evento.texto)
                EventoUi.NotaGuardada -> {
                    titulo = ""
                    contenido = ""
                    importante = false
                    codigoExterno = ""
                }
                is EventoUi.NotaBorrada -> {
                    val resultado = snackbarHostState.showSnackbar(
                        message = "\"${evento.titulo}\" borrada",
                        actionLabel = "Deshacer"
                    )
                    if (resultado == SnackbarResult.ActionPerformed) viewModel.deshacerBorrado()
                }
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Ejercicio 10 - Restricciones e índices", style = MaterialTheme.typography.titleLarge)

            OutlinedTextField(
                value = titulo,
                onValueChange = { titulo = it },
                label = { Text("Título") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = contenido,
                onValueChange = { contenido = it },
                label = { Text("Contenido") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = codigoExterno,
                onValueChange = { codigoExterno = it },
                label = { Text("Código externo (opcional, único)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = importante, onCheckedChange = { importante = it })
                Text("Importante")
            }
            Button(
                enabled = !guardando,
                onClick = { viewModel.guardarNota(titulo, contenido, importante, codigoExterno) }
            ) { Text("Guardar") }

            HorizontalDivider()
            OutlinedTextField(
                value = filtros.texto,
                onValueChange = viewModel::cambiarBusqueda,
                label = { Text("Buscar por título") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                FilterChip(
                    selected = filtros.soloImportantes,
                    onClick = viewModel::alternarSoloImportantes,
                    label = { Text("Solo importantes") }
                )
                OutlinedButton(onClick = viewModel::alternarOrden) {
                    Text(if (filtros.ascendente) "Más viejas primero" else "Más nuevas primero")
                }
            }
            Text(
                "Total: $total - Importantes: $totalImportantes - Mostrando: ${notas.size}",
                style = MaterialTheme.typography.titleMedium
            )

            if (notas.isEmpty()) {
                Text(if (total == 0) "Todavía no hay notas" else "Ninguna nota coincide con la búsqueda")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(notas, key = { it.id }) { nota ->
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            // Acción de alto nivel del ViewModel: cambiarImportante (no sabe nada de @Update).
                            Checkbox(checked = nota.importante, onCheckedChange = { viewModel.cambiarImportante(nota) })
                            Text(
                                "#${nota.id} ${nota.titulo} - ${nota.contenido}" +
                                    (nota.codigoExterno?.let { " [$it]" } ?: ""),
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(onClick = { notaEnEdicion = nota }) { Text("Editar") }
                            TextButton(onClick = { notaPorBorrar = nota }) { Text("Borrar") }
                        }
                    }
                }
            }
        }
        SnackbarHost(snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }

    notaEnEdicion?.let { nota ->
        DialogEditarNota(
            nota = nota,
            onCancelar = { notaEnEdicion = null },
            onGuardar = { t, c, i ->
                viewModel.actualizarNota(nota, t, c, i)
                notaEnEdicion = null
            }
        )
    }

    notaPorBorrar?.let { nota ->
        AlertDialog(
            onDismissRequest = { notaPorBorrar = null },
            title = { Text("¿Borrar la nota?") },
            text = { Text("\"${nota.titulo}\" se va a eliminar.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.eliminarNota(nota)
                    notaPorBorrar = null
                }) { Text("Borrar") }
            },
            dismissButton = { TextButton(onClick = { notaPorBorrar = null }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun DialogEditarNota(
    nota: NotaEntity,
    onCancelar: () -> Unit,
    onGuardar: (titulo: String, contenido: String, importante: Boolean) -> Unit
) {
    var titulo by remember(nota.id) { mutableStateOf(nota.titulo) }
    var contenido by remember(nota.id) { mutableStateOf(nota.contenido) }
    var importante by remember(nota.id) { mutableStateOf(nota.importante) }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Editar nota #${nota.id}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = titulo, onValueChange = { titulo = it }, label = { Text("Título") }, singleLine = true)
                OutlinedTextField(value = contenido, onValueChange = { contenido = it }, label = { Text("Contenido") })
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = importante, onCheckedChange = { importante = it })
                    Text("Importante")
                }
            }
        },
        confirmButton = { TextButton(onClick = { onGuardar(titulo, contenido, importante) }) { Text("Guardar cambios") } },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } }
    )
}
