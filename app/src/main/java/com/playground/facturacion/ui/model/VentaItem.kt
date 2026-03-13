package com.playground.facturacion.ui.model

data class VentaItem(
    val codigo: String,
    val nombre: String,
    val cantidad: Double,
    val precio: Double,
    val subtotal: Double
)
