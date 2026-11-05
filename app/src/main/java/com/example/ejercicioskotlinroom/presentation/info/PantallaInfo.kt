package com.example.ejercicioskotlinroom.presentation.info

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ejercicioskotlinroom.data.local.entity.NotaEntity

// Pantalla informativa: en los ejercicios 2 y 3 la guia no pide interfaz (se trabaja la Entity y el DAO).
// Solo indica donde esta el codigo del ejercicio.
@Composable
fun PantallaInfo(modifier: Modifier = Modifier) {
    Column(modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Ejercicio 2 — Primera Entity", style = MaterialTheme.typography.headlineSmall)
        Text("""En esta rama no hay pantalla nueva: el ejercicio define la tabla "notas" con NotaEntity.

Ver data/local/entity/NotaEntity.kt""")
    }
}
