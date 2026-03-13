package com.playground.facturacion.ui.model

data class ReporteResumen(
    val ventas: Double,
    val ganancia: Double,
    val facturas: Int,
    val porPago: Map<String, Double>,
    val porProducto: Map<String, Double>
)
