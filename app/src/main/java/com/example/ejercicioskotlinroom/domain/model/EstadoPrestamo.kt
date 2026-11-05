package com.example.ejercicioskotlinroom.domain.model

// Se guarda como TEXT con el nombre (ver Converters). Un enum es más claro que un Boolean "devuelto" si más
// adelante aparece un tercer estado (ej. PERDIDO).
enum class EstadoPrestamo { PRESTADO, DEVUELTO }
