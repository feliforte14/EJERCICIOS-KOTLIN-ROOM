package com.example.ejercicioskotlinroom

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ejercicioskotlinroom.data.NotaDao
import com.example.ejercicioskotlinroom.data.NotaEntity
import kotlinx.coroutines.launch

/*
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

// Función intermedia: valida, arma la entidad y la inserta. Devuelve el id nuevo o null si el título es inválido.
suspend fun guardarNota(
    dao: NotaDao,
    titulo: String,
    contenido: String,
    importante: Boolean
): Long? {
    val tituloLimpio = titulo.trim()
    if (tituloLimpio.isEmpty()) return null // validación 1: nada de títulos vacíos
    return dao.insertar(
        NotaEntity(
            titulo = tituloLimpio,                  // validación 2: sin espacios al comienzo/final
            contenido = contenido.trim(),
            importante = importante,
            creadaEn = System.currentTimeMillis()   // se genera acá, no en la UI
        )
    )
}

@Composable
fun PantallaNotas(dao: NotaDao, modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()

    // Borrador del formulario: es estado de UI, por eso rememberSaveable (sobrevive a la rotación).
    var titulo by rememberSaveable { mutableStateOf("") }
    var contenido by rememberSaveable { mutableStateOf("") }
    var importante by rememberSaveable { mutableStateOf(false) }

    var mensaje by remember { mutableStateOf<String?>(null) }
    var guardando by remember { mutableStateOf(false) }

    // Lista "básica": se lee una vez al entrar y después de cada guardado. El Flow que la mantiene sola llega en el ej. 6.
    var notas by remember { mutableStateOf(emptyList<NotaEntity>()) }
    LaunchedEffect(Unit) { notas = dao.obtenerTodasUnaVez() }

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Ejercicio 5 - Alta de notas", style = MaterialTheme.typography.titleLarge)

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
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = importante, onCheckedChange = { importante = it })
            Text("Importante")
        }

        Button(
            enabled = !guardando, // evita que dos toques rápidos disparen dos inserts
            onClick = {
                guardando = true
                scope.launch {
                    val id = guardarNota(dao, titulo, contenido, importante)
                    if (id == null) {
                        mensaje = "El título no puede estar vacío"
                    } else {
                        mensaje = "Nota guardada (id $id)"
                        titulo = ""
                        contenido = ""
                        importante = false
                        notas = dao.obtenerTodasUnaVez()
                    }
                    guardando = false
                }
            }
        ) { Text("Guardar") }

        mensaje?.let { Text(it, color = MaterialTheme.colorScheme.primary) }

        Text("Notas guardadas: ${notas.size}", style = MaterialTheme.typography.titleMedium)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items(notas, key = { it.id }) { nota ->
                Text("#${nota.id} ${if (nota.importante) "★ " else ""}${nota.titulo} - ${nota.contenido}")
            }
        }
    }
}
