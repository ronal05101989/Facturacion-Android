package com.playground.facturacion.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.playground.facturacion.core.AppContainer
import com.playground.facturacion.core.AppViewModelFactory
import com.playground.facturacion.domain.*
import com.playground.facturacion.presentation.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FacturacionApp(container: AppContainer) {
    val factory = remember(container) { AppViewModelFactory(container) }
    val loginViewModel: LoginViewModel = viewModel(factory = factory)
    val session by loginViewModel.session.collectAsStateWithLifecycle()

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        if (session == null) {
            LoginScreen(loginViewModel)
        } else {
            MainShell(factory = factory, user = requireNotNull(session))
        }
    }
}

private data class DrawerDestination(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val adminOnly: Boolean = false
)

private data class DashboardShortcut(
    val title: String,
    val subtitle: String,
    val route: String,
    val icon: ImageVector,
    val color: Color,
    val adminOnly: Boolean = false
)

private data class DashboardMetric(
    val title: String,
    val value: String,
    val icon: ImageVector,
    val color: Color
)

private val appDestinations = listOf(
    DrawerDestination("dashboard", "Dashboard", Icons.Outlined.Assessment),
    DrawerDestination("inventory", "Inventario", Icons.Outlined.Inventory2),
    DrawerDestination("sales", "Ventas", Icons.Outlined.PointOfSale),
    DrawerDestination("customers", "Clientes", Icons.Outlined.People),
    DrawerDestination("suppliers", "Proveedores", Icons.Outlined.Groups, adminOnly = true),
    DrawerDestination("purchases", "Compras", Icons.Outlined.ShoppingCart, adminOnly = true),
    DrawerDestination("cash", "Caja", Icons.Outlined.Payments),
    DrawerDestination("reports", "Reportes", Icons.Outlined.ReceiptLong, adminOnly = true),
    DrawerDestination("audit", "Auditoria", Icons.Outlined.Badge, adminOnly = true)
)

private val dashboardShortcuts = listOf(
    DashboardShortcut("Productos", "Inventario y stock", "inventory", Icons.Outlined.Inventory2, Color(0xFFF57C00)),
    DashboardShortcut("Ventas", "Facturacion rapida", "sales", Icons.Outlined.PointOfSale, Color(0xFF00897B)),
    DashboardShortcut("Clientes", "Base comercial", "customers", Icons.Outlined.People, Color(0xFF1565C0)),
    DashboardShortcut("Compras", "Reposicion y costos", "purchases", Icons.Outlined.ShoppingCart, Color(0xFFEF6C00), adminOnly = true),
    DashboardShortcut("Caja", "Apertura y cierre", "cash", Icons.Outlined.Payments, Color(0xFF2E7D32)),
    DashboardShortcut("Reportes", "Resumen del negocio", "reports", Icons.Outlined.Assessment, Color(0xFF1976D2), adminOnly = true),
    DashboardShortcut("Ajustes", "Configuracion general", "suppliers", Icons.Outlined.Category, Color(0xFFD84315), adminOnly = true),
    DashboardShortcut("Mas", "Auditoria y control", "audit", Icons.Outlined.Badge, Color(0xFF546E7A), adminOnly = true)
)

@Composable
private fun LoginScreen(viewModel: LoginViewModel) {
    val state = viewModel.uiState
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(28.dp)) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Venta Fácil RD", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text("POS Android offline con Room y MVVM", color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedTextField(value = state.username, onValueChange = viewModel::updateUsername, modifier = Modifier.fillMaxWidth(), label = { Text("Usuario") })
                OutlinedTextField(value = state.password, onValueChange = viewModel::updatePassword, modifier = Modifier.fillMaxWidth(), label = { Text("Clave") })
                OutlinedTextField(value = state.licenseKey, onValueChange = viewModel::updateLicense, modifier = Modifier.fillMaxWidth(), label = { Text("Licencia") })
                Button(onClick = viewModel::login, modifier = Modifier.fillMaxWidth(), enabled = !state.loading) {
                    Text(if (state.loading) "Entrando..." else "Iniciar sesion")
                }
                state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                Text("Demo: admin / 1234 / DEMO-2026-FACTURACION", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainShell(factory: AppViewModelFactory, user: LoggedInUser) {
    val dashboardViewModel: DashboardViewModel = viewModel(factory = factory)
    val inventoryViewModel: InventoryViewModel = viewModel(factory = factory)
    val customersViewModel: CustomersViewModel = viewModel(factory = factory)
    val suppliersViewModel: SuppliersViewModel = viewModel(factory = factory)
    val salesViewModel: SalesViewModel = viewModel(factory = factory)
    val purchasesViewModel: PurchasesViewModel = viewModel(factory = factory)
    val cashViewModel: CashViewModel = viewModel(factory = factory)
    val reportsViewModel: ReportsViewModel = viewModel(factory = factory)
    val auditViewModel: AuditViewModel = viewModel(factory = factory)

    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val destinations = remember(user.role) { appDestinations.filter { !it.adminOnly || user.role == UserRole.ADMIN } }

    ObserveMessage(inventoryViewModel.message, snackbarHostState, inventoryViewModel::clearMessage)
    ObserveMessage(customersViewModel.message, snackbarHostState, customersViewModel::clearMessage)
    ObserveMessage(suppliersViewModel.message, snackbarHostState, suppliersViewModel::clearMessage)
    ObserveMessage(salesViewModel.message, snackbarHostState, salesViewModel::clearMessage)
    ObserveMessage(purchasesViewModel.message, snackbarHostState, purchasesViewModel::clearMessage)
    ObserveMessage(cashViewModel.message, snackbarHostState, cashViewModel::clearMessage)

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(12.dp))
                destinations.forEach { item ->
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination
                    val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                    NavigationDrawerItem(
                        icon = { Icon(item.icon, contentDescription = null) },
                        label = { Text(item.title) },
                        selected = selected,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                NavigationDrawerItem(
                    icon = { Icon(Icons.AutoMirrored.Outlined.Logout, contentDescription = null) },
                    label = { Text("Cerrar sesion") },
                    selected = false,
                    onClick = { dashboardViewModel.logout() },
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Venta Fácil RD", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Outlined.Menu, contentDescription = "Menu")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = "dashboard",
                modifier = Modifier.padding(padding)
            ) {
                composable("dashboard") { DashboardScreen(dashboardViewModel, auditViewModel, navController, user) }
                composable("inventory") { InventoryScreen(inventoryViewModel) }
                composable("sales") { SalesScreen(salesViewModel, navController) }
                composable("customers") { CustomersScreen(customersViewModel, salesViewModel, navController) }
                composable("suppliers") { SuppliersScreen(suppliersViewModel) }
                composable("purchases") { PurchasesScreen(purchasesViewModel) }
                composable("cash") { CashScreen(cashViewModel) }
                composable("reports") { ReportsScreen(reportsViewModel) }
                composable("audit") { AuditScreen(auditViewModel) }
            }
        }
    }
}

@Composable
private fun ObserveMessage(message: String?, host: SnackbarHostState, onClear: () -> Unit) {
    LaunchedEffect(message) {
        if (message != null) {
            host.showSnackbar(message)
            onClear()
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DashboardScreen(
    viewModel: DashboardViewModel,
    auditViewModel: AuditViewModel,
    navController: androidx.navigation.NavController,
    user: LoggedInUser
) {
    val overview by viewModel.overview.collectAsStateWithLifecycle()
    val logs by auditViewModel.logs.collectAsStateWithLifecycle()

    val metrics = remember(overview) {
        listOf(
            DashboardMetric("Ventas hoy", "RD$ ${"%.2f".format(overview.todaySales)}", Icons.Outlined.Payments, Color(0xFF2E7D32)),
            DashboardMetric("Productos", "${overview.productCount}", Icons.Outlined.Inventory2, Color(0xFF1565C0)),
            DashboardMetric("Stock bajo", "${overview.lowStockCount}", Icons.Outlined.Inventory, Color(0xFFD32F2F)),
            DashboardMetric("Clientes", "${overview.customerCount}", Icons.Outlined.People, Color(0xFFF57C00))
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Column {
                Text("Bienvenido, ${user.fullName}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("Estado del negocio hoy", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        item { SummaryGrid(metrics) }

        item {
            Text("Accesos directos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        item {
            val shortcuts = remember(user.role) { dashboardShortcuts.filter { !it.adminOnly || user.role == UserRole.ADMIN } }
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                shortcuts.forEach { shortcut ->
                    ShortcutTile(shortcut) { navController.navigate(shortcut.route) }
                }
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Actividad reciente", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                TextButton(onClick = { navController.navigate("audit") }) { Text("Ver todo") }
            }
        }

        items(logs.take(5)) { log ->
            ActivityItem(log)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SummaryGrid(values: List<DashboardMetric>) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val tileWidth = if (maxWidth > 560.dp) 220.dp else 160.dp
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            values.forEach { metric ->
                Card(
                    modifier = Modifier.width(tileWidth),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F9FC)),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = metric.color.copy(alpha = 0.12f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Box(modifier = Modifier.padding(8.dp), contentAlignment = Alignment.Center) {
                                    Icon(metric.icon, contentDescription = null, tint = metric.color)
                                }
                            }
                            Text(metric.title, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(metric.value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ShortcutTile(shortcut: DashboardShortcut, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .width(160.dp)
            .height(100.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.Center) {
            Icon(shortcut.icon, contentDescription = null, tint = shortcut.color)
            Spacer(modifier = Modifier.height(8.dp))
            Text(shortcut.title, fontWeight = FontWeight.Bold)
            Text(shortcut.subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ActivityItem(log: AuditLog) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(40.dp)
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(log.action, fontWeight = FontWeight.Medium)
            Text(log.details, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(log.createdAt)),
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
fun InventoryScreen(viewModel: InventoryViewModel) {
    val products by viewModel.products.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    var showDeleteConfirm by remember { mutableStateOf<Product?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                selectedProduct = null
                showDialog = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "Agregar")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            Text(
                "Catálogo de Productos",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(16.dp),
                fontWeight = FontWeight.Bold
            )
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(products) { product ->
                    ProductItem(
                        product = product,
                        onEdit = {
                            selectedProduct = product
                            showDialog = true
                        },
                        onDelete = { showDeleteConfirm = product }
                    )
                }
            }
        }
    }

    if (showDialog) {
        ProductDialog(
            product = selectedProduct,
            onDismiss = { showDialog = false },
            onConfirm = {
                viewModel.save(it)
                showDialog = false
            }
        )
    }

    if (showDeleteConfirm != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title = { Text("Confirmar eliminación") },
            text = { Text("¿Estás seguro de que deseas eliminar este producto: ${showDeleteConfirm?.name}?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.delete(showDeleteConfirm!!.id)
                    showDeleteConfirm = null
                }) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = null }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun ProductItem(product: Product, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(product.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Cod: ${product.code} | Stock: ${product.stock}", style = MaterialTheme.typography.bodyMedium)
                Text("Precio: RD$ ${"%.2f".format(product.price)} | Costo: RD$ ${"%.2f".format(product.cost)}", style = MaterialTheme.typography.bodySmall)
                if (product.description.isNotBlank()) {
                    Text("Vencimiento: ${product.description}", style = MaterialTheme.typography.bodySmall, color = Color.Red)
                }
            }
            IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Editar") }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error) }
        }
    }
}

@Composable
private fun ProductDialog(product: Product?, onDismiss: () -> Unit, onConfirm: (Product) -> Unit) {
    var name by remember { mutableStateOf(product?.name ?: "") }
    var code by remember { mutableStateOf(product?.code ?: "PROD-${System.currentTimeMillis().toString().takeLast(6)}") }
    var price by remember { mutableStateOf(product?.price?.toString() ?: "") }
    var stock by remember { mutableStateOf(product?.stock?.toString() ?: "") }
    var cost by remember { mutableStateOf(product?.cost?.toString() ?: "") }
    var vencimiento by remember { mutableStateOf(product?.description ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (product == null) "Agregar Producto" else "Editar Producto") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = code, onValueChange = { code = it }, label = { Text("Código (Auto)") }, modifier = Modifier.fillMaxWidth(), readOnly = true)
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Precio") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = cost, onValueChange = { cost = it }, label = { Text("Costo") }, modifier = Modifier.weight(1f))
                }
                OutlinedTextField(value = stock, onValueChange = { stock = it }, label = { Text("Stock") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = vencimiento, onValueChange = { vencimiento = it }, label = { Text("Vencimiento") }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("AAAA-MM-DD") })
            }
        },
        confirmButton = {
            Button(onClick = {
                val p = (product ?: Product()).copy(
                    code = code,
                    name = name,
                    price = price.toDoubleOrNull() ?: 0.0,
                    cost = cost.toDoubleOrNull() ?: 0.0,
                    stock = stock.toIntOrNull() ?: 0,
                    description = vencimiento
                )
                onConfirm(p)
            }) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun CustomersScreen(
    viewModel: CustomersViewModel,
    salesViewModel: SalesViewModel,
    navController: androidx.navigation.NavController
) {
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Agregar")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            Text(
                "Directorio de Clientes",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(16.dp),
                fontWeight = FontWeight.Bold
            )
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(customers) { customer ->
                    CustomerItem(
                        customer = customer,
                        onSelect = {
                            salesViewModel.selectCustomer(customer.id)
                            navController.navigate("sales") {
                                popUpTo("dashboard") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    }

    if (showDialog) {
        CustomerDialog(
            onDismiss = { showDialog = false },
            onConfirm = {
                viewModel.save(it)
                showDialog = false
            }
        )
    }
}

@Composable
private fun CustomerItem(customer: Customer, onSelect: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(customer.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Tel: ${customer.phone}", style = MaterialTheme.typography.bodyMedium)
            Text("Dir: ${customer.address}", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Balance: RD$ ${"%.2f".format(customer.balance)}", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                Text("Límite: RD$ ${"%.2f".format(customer.creditLimit)}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun CustomerDialog(onDismiss: () -> Unit, onConfirm: (Customer) -> Unit) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var limit by remember { mutableStateOf("5000") }
    var balance by remember { mutableStateOf("0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo Cliente") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Teléfono") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Dirección") }, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = balance, onValueChange = { balance = it }, label = { Text("Balance Inicial") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = limit, onValueChange = { limit = it }, label = { Text("Límite Crédito") }, modifier = Modifier.weight(1f))
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(Customer(
                    name = name,
                    phone = phone,
                    address = address,
                    balance = balance.toDoubleOrNull() ?: 0.0,
                    creditLimit = limit.toDoubleOrNull() ?: 0.0
                ))
            }) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable fun SalesScreen(vm: SalesViewModel, navController: androidx.navigation.NavController) { Box(Modifier.fillMaxSize(), Alignment.Center) { Text("Modulo Ventas") } }
@Composable fun SuppliersScreen(vm: SuppliersViewModel) { Box(Modifier.fillMaxSize(), Alignment.Center) { Text("Modulo Proveedores") } }
@Composable fun PurchasesScreen(vm: PurchasesViewModel) { Box(Modifier.fillMaxSize(), Alignment.Center) { Text("Modulo Compras") } }
@Composable fun CashScreen(vm: CashViewModel) { Box(Modifier.fillMaxSize(), Alignment.Center) { Text("Modulo Caja") } }
@Composable fun ReportsScreen(vm: ReportsViewModel) { Box(Modifier.fillMaxSize(), Alignment.Center) { Text("Modulo Reportes") } }
@Composable fun AuditScreen(vm: AuditViewModel) { Box(Modifier.fillMaxSize(), Alignment.Center) { Text("Modulo Auditoria") } }
