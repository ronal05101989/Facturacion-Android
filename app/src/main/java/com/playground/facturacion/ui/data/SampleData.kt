package com.playground.facturacion.ui.data

import com.playground.facturacion.ui.model.Cliente
import com.playground.facturacion.ui.model.Factura
import com.playground.facturacion.ui.model.Producto
import com.playground.facturacion.ui.model.ReporteResumen
import com.playground.facturacion.ui.model.VentaItem

object SampleData {
    val clientes = listOf(
        Cliente("Tecno Red", "NIT 10293847", "+591 70000001"),
        Cliente("Comercial Luna", "NIT 55443322", "+591 70000002"),
        Cliente("Servicios Andinos", "NIT 99887766", "+591 70000003")
    )

    val productos = listOf(
        Producto("1001", "Paracetamol 500mg", 220.0, 8, 150.0, "2026-09-30"),
        Producto("1002", "Ibuprofeno 400mg", 145.5, 15, 96.0, "2026-08-15"),
        Producto("1003", "Jarabe para adultos", 980.0, 5, 760.0, "2026-04-20"),
        Producto("1004", "Papel para tickets", 12.0, 50, 7.0, "")
    )

    val facturas = listOf(
        Factura("FAC-0012", "Tecno Red", "2026-03-10", 365.50, "Pagada", "Efectivo"),
        Factura("FAC-0013", "Comercial Luna", "2026-03-11", 980.00, "Pendiente", "Transferencia"),
        Factura("FAC-0014", "Servicios Andinos", "2026-03-12", 157.00, "Pagada", "Tarjeta"),
        Factura("FAC-0015", "Tecno Red", "2026-03-12", 48.00, "Pendiente", "Efectivo")
    )

    val carritoDemo = listOf(
        VentaItem("1001", "Paracetamol 500mg", 2.0, 220.0, 440.0),
        VentaItem("1003", "Jarabe para adultos", 1.0, 980.0, 980.0),
        VentaItem("1004", "Papel para tickets", 3.0, 12.0, 36.0)
    )

    val reporteResumen = ReporteResumen(
        ventas = 1550.50,
        ganancia = 410.25,
        facturas = 4,
        porPago = mapOf(
            "Efectivo" to 413.50,
            "Transferencia" to 980.00,
            "Tarjeta" to 157.00
        ),
        porProducto = mapOf(
            "Paracetamol 500mg" to 7.0,
            "Jarabe para adultos" to 3.0,
            "Ibuprofeno 400mg" to 5.0
        )
    )

    val usuariosDemo = listOf("admin", "caja")
}
