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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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

@Composable
fun FacturacionApp() {
    val navController = rememberNavController()
    val items = listOf(
        AppDestination.Resumen,
        AppDestination.Clientes,
        AppDestination.Productos,
        AppDestination.Facturas
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
            startDestination = AppDestination.Resumen.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(AppDestination.Resumen.route) {
                ResumenScreen(
                    clientes = SampleData.clientes,
                    productos = SampleData.productos,
                    facturas = SampleData.facturas
                )
            }
            composable(AppDestination.Clientes.route) {
                ClientesScreen(clientes = SampleData.clientes)
            }
            composable(AppDestination.Productos.route) {
                ProductosScreen(productos = SampleData.productos)
            }
            composable(AppDestination.Facturas.route) {
                FacturasScreen(facturas = SampleData.facturas)
            }
        }
    }
}

private sealed class AppDestination(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    data object Resumen : AppDestination("resumen", "Inicio", Icons.Outlined.Home)
    data object Clientes : AppDestination("clientes", "Clientes", Icons.Outlined.People)
    data object Productos : AppDestination("productos", "Productos", Icons.Outlined.Inventory2)
    data object Facturas : AppDestination("facturas", "Facturas", Icons.Outlined.ReceiptLong)
}

@Composable
private fun ResumenScreen(
    clientes: List<Cliente>,
    productos: List<Producto>,
    facturas: List<Factura>
) {
    val totalFacturado = facturas.sumOf { it.total }
    val facturasPendientes = facturas.count { it.estado == "Pendiente" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFF6F1E9), Color(0xFFFFFFFF))
                )
            ),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            HeroCard(
                title = "Panel de facturacion",
                subtitle = "Controla clientes, inventario y cobros desde una sola app."
            )
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryCard(
                    modifier = Modifier.weight(1f),
                    title = "Clientes",
                    value = clientes.size.toString()
                )
                SummaryCard(
                    modifier = Modifier.weight(1f),
                    title = "Productos",
                    value = productos.size.toString()
                )
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryCard(
                    modifier = Modifier.weight(1f),
                    title = "Facturado",
                    value = "$${"%.2f".format(totalFacturado)}"
                )
                SummaryCard(
                    modifier = Modifier.weight(1f),
                    title = "Pendientes",
                    value = facturasPendientes.toString()
                )
            }
        }
        item {
            SectionTitle("Facturas recientes")
        }
        items(facturas.take(3)) { factura ->
            FacturaCard(factura = factura)
        }
    }
}

@Composable
private fun ClientesScreen(clientes: List<Cliente>) {
    EntityListScreen(
        title = "Clientes",
        subtitle = "Base de clientes para emitir facturas rapidamente."
    ) {
        items(clientes) { cliente ->
            InfoCard(
                title = cliente.nombre,
                line1 = cliente.documento,
                line2 = cliente.telefono
            )
        }
    }
}

@Composable
private fun ProductosScreen(productos: List<Producto>) {
    EntityListScreen(
        title = "Productos",
        subtitle = "Catalogo inicial para ventas y control de stock."
    ) {
        items(productos) { producto ->
            InfoCard(
                title = producto.nombre,
                line1 = "Precio: $${"%.2f".format(producto.precio)}",
                line2 = "Stock: ${producto.stock}"
            )
        }
    }
}

@Composable
private fun FacturasScreen(facturas: List<Factura>) {
    var filtroPendientes by remember { mutableStateOf(false) }
    val lista = if (filtroPendientes) {
        facturas.filter { it.estado == "Pendiente" }
    } else {
        facturas
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            HeroCard(
                title = "Facturas emitidas",
                subtitle = if (filtroPendientes) {
                    "Mostrando solo documentos pendientes de cobro."
                } else {
                    "Historial resumido de ventas con estado de pago."
                },
                actionText = if (filtroPendientes) "Ver todas" else "Solo pendientes",
                onAction = { filtroPendientes = !filtroPendientes }
            )
        }
        items(lista) { factura ->
            FacturaCard(factura = factura)
        }
    }
}

@Composable
private fun EntityListScreen(
    title: String,
    subtitle: String,
    content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            HeroCard(title = title, subtitle = subtitle)
        }
        content()
    }
}

@Composable
private fun HeroCard(
    title: String,
    subtitle: String,
    actionText: String? = null,
    onAction: (() -> Unit)? = null
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
                    color = Color.White.copy(alpha = 0.88f)
                )
                if (actionText != null && onAction != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = onAction,
                        modifier = Modifier
                            .background(
                                color = Color.White.copy(alpha = 0.16f),
                                shape = RoundedCornerShape(50)
                            )
                            .align(Alignment.Start)
                    ) {
                        Text(text = actionText, color = Color.White)
                    }
                }
            }
        }
    }
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
private fun InfoCard(
    title: String,
    line1: String,
    line2: String
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(text = line1, style = MaterialTheme.typography.bodyMedium)
            Text(text = line2, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
            Text(
                text = "Total: $${"%.2f".format(factura.total)}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Fecha: ${factura.fecha}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
