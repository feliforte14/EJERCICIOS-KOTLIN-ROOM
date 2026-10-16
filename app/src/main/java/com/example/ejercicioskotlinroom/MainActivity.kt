package com.example.ejercicioskotlinroom

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.ejercicioskotlinroom.ui.theme.EjerciciosKotlinRoomTheme

// Proyecto base de la guía de Room. Cada ejercicio vive en su propia rama (ver README.md).
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EjerciciosKotlinRoomTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Ejercicio 1: pantalla para comparar remember vs rememberSaveable (sin Room todavía).
                    PantallaEstadoEnMemoria(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}
