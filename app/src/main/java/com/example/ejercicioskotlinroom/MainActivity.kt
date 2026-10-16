package com.example.ejercicioskotlinroom

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ejercicioskotlinroom.prestamos.PantallaPrestamos
import com.example.ejercicioskotlinroom.ui.theme.EjerciciosKotlinRoomTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EjerciciosKotlinRoomTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Ejercicio 15: la miniapp de préstamos es la pantalla principal. La app de notas (ej. 1 a 14)
                    // sigue en el proyecto y se puede ver con el selector de arriba.
                    var verMiniapp by rememberSaveable { mutableStateOf(true) }
                    Column(Modifier.padding(innerPadding)) {
                        Row(Modifier.padding(horizontal = 16.dp)) {
                            FilterChip(
                                selected = verMiniapp,
                                onClick = { verMiniapp = true },
                                label = { Text("Préstamos (ej. 15)") }
                            )
                            FilterChip(
                                selected = !verMiniapp,
                                onClick = { verMiniapp = false },
                                label = { Text("Notas (ej. 1-14)") },
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                        if (verMiniapp) PantallaPrestamos() else PantallaNotas()
                    }
                }
            }
        }
    }
}
