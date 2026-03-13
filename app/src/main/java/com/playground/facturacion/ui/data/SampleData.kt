package com.playground.facturacion.ui.data

import com.playground.facturacion.ui.model.Cliente
import com.playground.facturacion.ui.model.Factura
import com.playground.facturacion.ui.model.Producto

object SampleData {
    val clientes = listOf(
        Cliente("Tecno Red", "NIT 10293847", "+591 70000001"),
        Cliente("Comercial Luna", "NIT 55443322", "+591 70000002"),
        Cliente("Servicios Andinos", "NIT 99887766", "+591 70000003")
    )

    val productos = listOf(
        Producto("Impresora termica", 220.0, 8),
        Producto("Escaner de codigo", 145.5, 15),
        Producto("Tableta Android", 980.0, 5),
        Producto("Papel para tickets", 12.0, 50)
    )

    val facturas = listOf(
        Factura("FAC-0012", "Tecno Red", "2026-03-10", 365.50, "Pagada"),
        Factura("FAC-0013", "Comercial Luna", "2026-03-11", 980.00, "Pendiente"),
        Factura("FAC-0014", "Servicios Andinos", "2026-03-12", 157.00, "Pagada"),
        Factura("FAC-0015", "Tecno Red", "2026-03-12", 48.00, "Pendiente")
    )
}
