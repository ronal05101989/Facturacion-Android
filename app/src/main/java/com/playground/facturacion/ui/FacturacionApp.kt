package com.playground.facturacion.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.PointOfSale
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.playground.facturacion.ui.data.SampleData
import com.playground.facturacion.ui.model.Cliente
import com.playground.facturacion.ui.model.Factura
import com.playground.facturacion.ui.model.Producto
import com.playground.facturacion.ui.model.ReporteResumen
import com.playground.facturacion.ui.model.VentaItem

@Composable
fun FacturacionApp() {
    val navController = rememberNavController()
    val items = listOf(
        AppDestination.Pos,
        AppDestination.Inventario,
        AppDestination.Facturas,
        AppDestination.Reportes,
        AppDestination.Configuracion
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                items.forEach { destination ->
                    NavigationBarItem(
                        selected = currentDestination?.hierarchy?.any { it.route == destination.route } == true,
                        onClick = {
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = destination.label
                            )
                        },
                        label = { Text(destination.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppDestination.Pos.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(AppDestination.Pos.route) {
                PosScreen(
                    carrito = SampleData.carritoDemo,
                    reporte = SampleData.reporteResumen
                )
            }
            composable(AppDestination.Inventario.route) {
                InventarioScreen(
                    productos = SampleData.productos,
                    stockBajo = SampleData.productos.filter { it.stock <= 5 }
                )
            }
            composable(AppDestination.Facturas.route) {
                FacturasScreen(
                    facturas = SampleData.facturas,
                    clientes = SampleData.clientes
                )
            }
            composable(AppDestination.Reportes.route) {
                ReportesScreen(
                    reporte = SampleData.reporteResumen,
                    facturas = SampleData.facturas
                )
            }
            composable(AppDestination.Configuracion.route) {
                ConfiguracionScreen(
                    clientes = SampleData.clientes,
                    usuarios = SampleData.usuariosDemo
                )
            }
        }
    }
}

private sealed class AppDestination(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    data object Pos : AppDestination("pos", "POS", Icons.Outlined.PointOfSale)
    data object Inventario : AppDestination("inventario", "Stock", Icons.Outlined.Inventory2)
    data object Facturas : AppDestination("facturas", "Facturas", Icons.Outlined.ReceiptLong)
    data object Reportes : AppDestination("reportes", "Reportes", Icons.Outlined.Assessment)
    data object Configuracion : AppDestination("configuracion", "Config", Icons.Outlined.Settings)
}

@Composable
private fun PosScreen(
    carrito: List<VentaItem>,
    reporte: ReporteResumen
) {
    LazyColumn(
        modifier = screenModifier(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            HeroCard(
                title = "Vende Facil RD",
                subtitle = "POS para farmacia con inventario, facturas, backup y control por rol."
            )
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryCard(
                    modifier = Modifier.weight(1f),
                    title = "Ventas hoy",
                    value = "RD$ ${"%.2f".format(reporte.ventas)}"
                )
                SummaryCard(
                    modifier = Modifier.weight(1f),
                    title = "Ganancia",
                    value = "RD$ ${"%.2f".format(reporte.ganancia)}"
                )
            }
        }
        item {
            SectionTitle("Acciones principales")
        }
        items(
            listOf(
                "Agregar por codigo de barras",
                "Guardar venta",
                "Guardar e imprimir ticket",
                "Reimprimir ultimo ticket",
                "Abrir ventas",
                "Calcular devuelta"
            )
        ) { action ->
            ActionCard(title = action)
        }
        item {
            SectionTitle("Carrito / factura actual")
        }
        items(carrito) { item ->
            SaleItemCard(item = item)
        }
        item {
            TotalCard(total = carrito.sumOf { it.subtotal }, devuelta = 50.0)
        }
    }
}

@Composable
private fun InventarioScreen(
    productos: List<Producto>,
    stockBajo: List<Producto>
) {
    LazyColumn(
        modifier = screenModifier(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            HeroCard(
                title = "Inventario",
                subtitle = "Registro de productos, costo, stock, vencimiento y alertas de farmacia."
            )
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryCard(
                    modifier = Modifier.weight(1f),
                    title = "Productos",
                    value = productos.size.toString()
                )
                SummaryCard(
                    modifier = Modifier.weight(1f),
                    title = "Stock bajo",
                    value = stockBajo.size.toString()
                )
            }
        }
        item {
            SectionTitle("Alertas de vencimiento y stock")
        }
        items(stockBajo) { producto ->
            AlertCard(
                title = producto.nombre,
                description = "Stock: ${producto.stock} | Vence: ${producto.vencimiento.ifBlank { "Sin fecha" }}"
            )
        }
        item {
            SectionTitle("Catalogo")
        }
        items(productos) { producto ->
            ProductoCard(producto = producto)
        }
    }
}

@Composable
private fun FacturasScreen(
    facturas: List<Factura>,
    clientes: List<Cliente>
) {
    LazyColumn(
        modifier = screenModifier(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            HeroCard(
                title = "Facturas y ventas",
                subtitle = "Historial, edicion de lineas, anulacion y seguimiento del cliente."
            )
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryCard(
                    modifier = Modifier.weight(1f),
                    title = "Facturas",
                    value = facturas.size.toString()
                )
                SummaryCard(
                    modifier = Modifier.weight(1f),
                    title = "Clientes",
                    value = clientes.size.toString()
                )
            }
        }
        item {
            SectionTitle("Historial")
        }
        items(facturas) { factura ->
            FacturaCard(factura = factura)
        }
    }
}

@Composable
private fun ReportesScreen(
    reporte: ReporteResumen,
    facturas: List<Factura>
) {
    LazyColumn(
        modifier = screenModifier(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            HeroCard(
                title = "Reporte diario",
                subtitle = "Resumen de ventas, ganancia, metodos de pago y rendimiento por producto."
            )
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryCard(
                    modifier = Modifier.weight(1f),
                    title = "Ventas",
                    value = "RD$ ${"%.2f".format(reporte.ventas)}"
                )
                SummaryCard(
                    modifier = Modifier.weight(1f),
                    title = "Facturas",
                    value = reporte.facturas.toString()
                )
            }
        }
        item {
            SummaryCard(
                title = "Ganancia estimada",
                value = "RD$ ${"%.2f".format(reporte.ganancia)}"
            )
        }
        item {
            SectionTitle("Metodos de pago")
        }
        items(reporte.porPago.toList()) { (metodo, total) ->
            InfoCard(
                title = metodo,
                line1 = "Total: RD$ ${"%.2f".format(total)}",
                line2 = "Participacion en el cierre diario"
            )
        }
        item {
            SectionTitle("Productos mas vendidos")
        }
        items(reporte.porProducto.toList()) { (producto, cantidad) ->
            InfoCard(
                title = producto,
                line1 = "Cantidad vendida: ${"%.0f".format(cantidad)}",
                line2 = "Consolidado desde ventas del dia"
            )
        }
        item {
            SectionTitle("Facturas incluidas")
        }
        items(facturas.take(3)) { factura ->
            FacturaCard(factura = factura)
        }
    }
}

@Composable
private fun ConfiguracionScreen(
    clientes: List<Cliente>,
    usuarios: List<String>
) {
    LazyColumn(
        modifier = screenModifier(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            HeroCard(
                title = "Configuracion",
                subtitle = "Licencia, usuarios, datos del negocio, mensajes del ticket y respaldos."
            )
        }
        item {
            SectionTitle("Modulos del sistema Python")
        }
        items(
            listOf(
                "Activacion de licencia",
                "Inicio de sesion por usuario",
                "Gestion de usuarios",
                "Configurar factura",
                "Backup automatico",
                "Impresora y ticket"
            )
        ) { module ->
            ActionCard(title = module)
        }
        item {
            SectionTitle("Usuarios de ejemplo")
        }
        items(usuarios) { usuario ->
            InfoCard(
                title = usuario,
                line1 = "Rol configurado en el sistema",
                line2 = "Puede condicionar accesos en Android"
            )
        }
        item {
            SectionTitle("Clientes registrados")
        }
        items(clientes) { cliente ->
            InfoCard(
                title = cliente.nombre,
                line1 = cliente.documento,
                line2 = cliente.telefono
            )
        }
    }
}

private fun screenModifier(): Modifier {
    return Modifier
        .fillMaxSize()
        .background(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFFF6F1E9), Color.White)
            )
        )
}

@Composable
private fun HeroCard(
    title: String,
    subtitle: String
) {
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF1E4D40), Color(0xFF3E7C59))
                    ),
                    shape = RoundedCornerShape(28.dp)
                )
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun SummaryCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F4EF))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.labelLarge)
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ActionCard(title: String) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.PointOfSale,
                contentDescription = null,
                tint = Color(0xFF1E4D40)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun AlertCard(
    title: String,
    description: String
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF4E5))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(text = description, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun ProductoCard(producto: Producto) {
    InfoCard(
        title = producto.nombre,
        line1 = "Codigo ${producto.codigo} | Precio: RD$ ${"%.2f".format(producto.precio)}",
        line2 = "Stock: ${producto.stock} | Costo: RD$ ${"%.2f".format(producto.costo)} | Vence: ${producto.vencimiento.ifBlank { "N/D" }}"
    )
}

@Composable
private fun SaleItemCard(item: VentaItem) {
    InfoCard(
        title = item.nombre,
        line1 = "Cant: ${"%.0f".format(item.cantidad)} | Precio: RD$ ${"%.2f".format(item.precio)}",
        line2 = "Subtotal: RD$ ${"%.2f".format(item.subtotal)}"
    )
}

@Composable
private fun TotalCard(total: Double, devuelta: Double) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1F473C))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(text = "Total venta", color = Color.White.copy(alpha = 0.9f))
            Text(
                text = "RD$ ${"%.2f".format(total)}",
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Devuelta estimada: RD$ ${"%.2f".format(devuelta)}",
                color = Color.White.copy(alpha = 0.84f)
            )
        }
    }
}

@Composable
private fun InfoCard(
    title: String,
    line1: String,
    line2: String
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(text = line1, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = line2,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FacturaCard(factura: Factura) {
    val statusColor = if (factura.estado == "Pagada") Color(0xFF2E7D32) else Color(0xFFC17900)
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = factura.numero,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = factura.estado,
                    color = statusColor,
                    style = MaterialTheme.typography.labelLarge
                )
            }
            Text(text = factura.cliente, style = MaterialTheme.typography.bodyLarge)
            Text(text = "Total: RD$ ${"%.2f".format(factura.total)}", style = MaterialTheme.typography.bodyMedium)
            Text(
                text = "Fecha: ${factura.fecha} | Pago: ${factura.metodoPago}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
