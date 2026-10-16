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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/*
 * EJERCICIO 1 - ¿Qué se pierde cuando la app se cierra?
 *
 * Lista editable en Compose SIN Room. Hay dos copias de la misma pantalla para poder comparar:
 *   - arriba: estado con remember
 *   - abajo:  estado con rememberSaveable
 * Procedimiento de la consigna: agregar 3 ítems y después
 *   1) rotar el dispositivo, 2) volver atrás y entrar de nuevo, 3) cerrar desde recientes y abrir,
 *   4) detener el proceso desde Android Studio y volver a ejecutar.
 *
 * Qué sobrevive en cada caso (comportamiento esperado según cómo funciona Compose;
 * hay que confirmarlo a mano en el emulador y anotar cualquier diferencia):
 *
 *                                   remember   rememberSaveable
 *   1) rotación                     SE PIERDE  sobrevive (se guarda en el Bundle de estado de la Activity)
 *   2) salir con "atrás" y volver   SE PIERDE  SE PIERDE (la Activity se destruye por completo, no hay estado que restaurar)
 *   3) cerrar desde recientes       SE PIERDE  SE PIERDE (el usuario descarta la tarea, se borra el estado guardado)
 *   4) matar el proceso (Studio)    SE PIERDE  SE PIERDE
 *
 * Tabla "dato / dónde vive ahora / dónde debería vivir":
 *
 *   dato                               vive ahora                       debería vivir
 *   ---------------------------------  -------------------------------  ------------------------------------------
 *   texto del TextField (borrador)     remember                         rememberSaveable (es efímero, no va a la base)
 *   lista de elementos                 remember / rememberSaveable      base de datos (Room): es info del usuario
 *   preferencia visual (modo oscuro)   remember                         preferencia chica -> DataStore, no Room
 *
 * PREGUNTAS
 * 1) ¿remember implica persistencia?
 *    No. remember solo evita perder el valor cuando se recompone el composable. Vive en memoria,
 *    atado a la composición. Si la composición sale del árbol o muere la Activity, se pierde.
 *
 * 2) ¿Diferencia entre conservar estado ante recomposición y conservar datos al reiniciar el proceso?
 *    La recomposición es Compose volviendo a ejecutar la función con el mismo proceso vivo: remember alcanza.
 *    Reiniciar el proceso borra TODA la memoria; solo sobrevive lo que está en disco (archivo, DataStore, Room).
 *    rememberSaveable queda en el medio: sobrevive a rotación y a que Android mate el proceso en segundo plano
 *    (restaura desde el Bundle), pero no a que el usuario cierre la app a propósito. Y tampoco es para datos grandes.
 *
 * 3) ¿Todos los datos de una pantalla deberían guardarse en una base?
 *    No. Lo que importa para el usuario y debe durar (notas, tareas) va a la base. Lo que es del momento
 *    (borrador a medio escribir, qué diálogo está abierto, scroll) es estado de UI.
 *
 * 4) ¿Qué dato de los que probé sería absurdo persistir?
 *    El estado "estoy escribiendo" del TextField a medio tipear, o un flag como "el diálogo está abierto".
 *    No tiene valor al día siguiente y ensucia la base.
 *
 * POR QUÉ ROOM RESUELVE OTRO PROBLEMA QUE remember
 * remember/rememberSaveable mantienen estado de UI mientras la pantalla o el proceso viven.
 * Room guarda datos en un archivo SQLite en disco, así que sobreviven a cerrar la app, matar el proceso
 * y reiniciar el celu. Son problemas distintos: uno es "no perder el estado al recomponer/rotar",
 * el otro es "no perder los datos nunca".
 */
@Composable
fun PantallaEstadoEnMemoria(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Ejercicio 1 - Estado en memoria", style = MaterialTheme.typography.titleLarge)

        // Panel 1: todo con remember. Es lo que se pierde al rotar.
        PanelEstado(titulo = "Con remember", guardable = false)
        HorizontalDivider()
        // Panel 2: mismo panel pero con rememberSaveable. Sobrevive a la rotación, no a cerrar la app.
        PanelEstado(titulo = "Con rememberSaveable", guardable = true)
    }
}

@Composable
private fun PanelEstado(titulo: String, guardable: Boolean) {
    // Hay que elegir el tipo de estado en tiempo de ejecución, por eso la doble versión de cada uno.
    var texto by if (guardable) rememberSaveable { mutableStateOf("") } else remember { mutableStateOf("") }
    var items by if (guardable) {
        // Se guarda como List<String> (ArrayList es Serializable, entra en el Bundle sin problemas).
        rememberSaveable { mutableStateOf(emptyList<String>()) }
    } else {
        remember { mutableStateOf(emptyList<String>()) }
    }
    // Preferencia visual sencilla: fondo oscuro/claro para esta tarjeta.
    var oscuro by if (guardable) rememberSaveable { mutableStateOf(false) } else remember { mutableStateOf(false) }

    Surface(
        color = if (oscuro) Color(0xFF2B2B2B) else MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            val colorTexto = if (oscuro) Color.White else Color.Unspecified
            Text(titulo, style = MaterialTheme.typography.titleMedium, color = colorTexto)

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Fondo oscuro", color = colorTexto)
                Switch(checked = oscuro, onCheckedChange = { oscuro = it })
            }

            OutlinedTextField(
                value = texto,
                onValueChange = { texto = it },
                label = { Text("Nuevo ítem") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    if (texto.isNotBlank()) {
                        items = items + texto.trim()
                        texto = ""
                    }
                }
            ) { Text("Agregar") }

            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(items) { Text("• $it", color = colorTexto) }
            }
        }
    }
}
