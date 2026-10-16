package com.example.ejercicioskotlinroom

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ejercicioskotlinroom.data.NotaDao
import com.example.ejercicioskotlinroom.data.NotaEntity
import kotlinx.coroutines.launch

/*
 * Pantalla TEMPORAL del ejercicio 4 (la reemplaza el formulario real del ej. 5).
 * Sirve para "código controlado": un botón que inserta 3 notas fijas y otro que lista lo que hay,
 * así se puede abrir App Inspection > Database Inspector, elegir notas.db > tabla "notas" y ver
 * las columnas y los 3 registros.
 *
 * Pasos para mirar la base:
 *   1. Correr la app en debug y tocar "Insertar 3 notas".
 *   2. View > Tool Windows > App Inspection > Database Inspector.
 *   3. Proceso de la app > notas.db > notas: ver columnas (id, titulo, contenido, importante, creadaEn, colorEtiqueta) y filas.
 *   4. Editar una celda desde el inspector, volver a tocar "Leer de la base" y ver que la app trae el valor modificado.
 * Si el inspector no muestra la base: primero verificar que la app realmente la abrió (tocar un botón) antes de tocar dependencias.
 */
@Composable
fun PruebaBase(dao: NotaDao, modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    var notas by remember { mutableStateOf(emptyList<NotaEntity>()) }
    var total by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Ejercicio 4 - Base creada", style = MaterialTheme.typography.titleLarge)

        Button(onClick = {
            // Room ya mueve el trabajo a su dispatcher: acá lanzo la corrutina desde la UI.
            scope.launch {
                val ahora = System.currentTimeMillis()
                dao.insertar(NotaEntity(titulo = "Comprar pan", contenido = "Integral", creadaEn = ahora))
                dao.insertar(NotaEntity(titulo = "Estudiar Room", contenido = "Entity, DAO y Database", importante = true, creadaEn = ahora + 1))
                dao.insertar(NotaEntity(titulo = "Llamar al banco", contenido = "", colorEtiqueta = "ROJO", creadaEn = ahora + 2))
            }
        }) { Text("Insertar 3 notas") }

        Button(onClick = {
            scope.launch {
                notas = dao.obtenerTodasUnaVez()
                total = dao.contarNotas()
            }
        }) { Text("Leer de la base") }

        Button(onClick = { scope.launch { dao.borrarTodas(); notas = emptyList(); total = 0 } }) { Text("Borrar todas") }

        Text("Total en la base: $total")
        LazyColumn {
            items(notas) { Text("#${it.id} ${it.titulo} (importante=${it.importante}, color=${it.colorEtiqueta})") }
        }
    }
}
