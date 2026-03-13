package com.playground.facturacion.ui.model

data class Factura(
    val numero: String,
    val cliente: String,
    val fecha: String,
    val total: Double,
    val estado: String
)
