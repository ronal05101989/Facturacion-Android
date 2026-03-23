package com.playground.facturacion.data

import androidx.room.withTransaction
import com.playground.facturacion.data.local.AppDatabase
import com.playground.facturacion.data.local.AuditLogEntity
import com.playground.facturacion.data.local.CashSessionEntity
import com.playground.facturacion.data.local.PurchaseEntity
import com.playground.facturacion.data.local.PurchaseItemEntity
import com.playground.facturacion.data.local.SaleEntity
import com.playground.facturacion.data.local.SaleItemEntity
import com.playground.facturacion.domain.AuditLog
import com.playground.facturacion.domain.AuthRepository
import com.playground.facturacion.domain.CashRepository
import com.playground.facturacion.domain.CashSessionStatus
import com.playground.facturacion.domain.Customer
import com.playground.facturacion.domain.CustomerRepository
import com.playground.facturacion.domain.DashboardOverview
import com.playground.facturacion.domain.DashboardRepository
import com.playground.facturacion.domain.InventoryRepository
import com.playground.facturacion.domain.LicenseInfo
import com.playground.facturacion.domain.LoggedInUser
import com.playground.facturacion.domain.PaymentMethod
import com.playground.facturacion.domain.Product
import com.playground.facturacion.domain.PurchaseItemInput
import com.playground.facturacion.domain.PurchaseRecord
import com.playground.facturacion.domain.PurchasesRepository
import com.playground.facturacion.domain.ReportSummary
import com.playground.facturacion.domain.ReportsRepository
import com.playground.facturacion.domain.SaleCartItem
import com.playground.facturacion.domain.SaleReceipt
import com.playground.facturacion.domain.SalesRepository
import com.playground.facturacion.domain.Supplier
import com.playground.facturacion.domain.SupplierRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.util.Calendar

class SessionManager {
    private val _currentUser = MutableStateFlow<LoggedInUser?>(null)
    val currentUser = _currentUser.asStateFlow()

    fun setUser(user: LoggedInUser?) {
        _currentUser.value = user
    }
}

class OfflineAuthRepository(
    private val database: AppDatabase,
    private val sessionManager: SessionManager
) : AuthRepository {
    override val currentUser: Flow<LoggedInUser?> = sessionManager.currentUser

    override suspend fun login(username: String, password: String, licenseKey: String): Result<Unit> {
        val license = database.licenseDao().getLicense()
            ?: return Result.failure(IllegalStateException("Licencia no configurada"))
        if (!license.isActive || license.licenseKey != licenseKey.trim()) {
            return Result.failure(IllegalArgumentException("Licencia invalida"))
        }

        val user = database.userDao().getByCredentials(username.trim(), password.trim())
            ?: return Result.failure(IllegalArgumentException("Credenciales incorrectas"))

        sessionManager.setUser(user.toDomain())
        database.auditDao().insert(
            AuditLogEntity(
                userId = user.id,
                userName = user.fullName,
                action = "LOGIN",
                module = "AUTH",
                details = "Inicio de sesion exitoso",
                createdAt = now()
            )
        )
        return Result.success(Unit)
    }

    override suspend fun logout() {
        val user = sessionManager.currentUser.value
        if (user != null) {
            database.auditDao().insert(
                AuditLogEntity(
                    userId = user.id,
                    userName = user.fullName,
                    action = "LOGOUT",
                    module = "AUTH",
                    details = "Cierre de sesion",
                    createdAt = now()
                )
            )
        }
        sessionManager.setUser(null)
    }

    override suspend fun getLicense(): LicenseInfo? = database.licenseDao().getLicense()?.toDomain()
}

class OfflineInventoryRepository(
    private val database: AppDatabase
) : InventoryRepository {
    override fun observeProducts(): Flow<List<Product>> =
        database.productDao().observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeLowStockProducts(): Flow<List<Product>> =
        database.productDao().observeLowStock().map { list -> list.map { it.toDomain() } }

    override suspend fun saveProduct(product: Product, actor: LoggedInUser) {
        val entity = product.toEntity(now())
        if (product.id == 0L) {
            database.productDao().insert(entity.copy(id = 0))
            log(actor, "CREATE", "INVENTORY", "Producto creado: ${product.name}")
        } else {
            database.productDao().update(entity)
            log(actor, "UPDATE", "INVENTORY", "Producto actualizado: ${product.name}")
        }
    }

    override suspend fun deleteProduct(productId: Long, actor: LoggedInUser) {
        database.productDao().deleteById(productId)
        log(actor, "DELETE", "INVENTORY", "Producto eliminado con id $productId")
    }

    private suspend fun log(actor: LoggedInUser, action: String, module: String, details: String) {
        database.auditDao().insert(
            AuditLogEntity(
                userId = actor.id,
                userName = actor.fullName,
                action = action,
                module = module,
                details = details,
                createdAt = now()
            )
        )
    }
}

class OfflineCustomerRepository(
    private val database: AppDatabase
) : CustomerRepository {
    override fun observeCustomers(): Flow<List<Customer>> =
        database.customerDao().observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun saveCustomer(customer: Customer, actor: LoggedInUser) {
        val entity = customer.toEntity(now())
        if (customer.id == 0L) {
            database.customerDao().insert(entity.copy(id = 0))
            log(actor, "CREATE", "CUSTOMERS", "Cliente creado: ${customer.name}")
        } else {
            database.customerDao().update(entity)
            log(actor, "UPDATE", "CUSTOMERS", "Cliente actualizado: ${customer.name}")
        }
    }

    override suspend fun deleteCustomer(customerId: Long, actor: LoggedInUser) {
        database.customerDao().deleteById(customerId)
        log(actor, "DELETE", "CUSTOMERS", "Cliente eliminado con id $customerId")
    }

    private suspend fun log(actor: LoggedInUser, action: String, module: String, details: String) {
        database.auditDao().insert(
            AuditLogEntity(
                userId = actor.id,
                userName = actor.fullName,
                action = action,
                module = module,
                details = details,
                createdAt = now()
            )
        )
    }
}

class OfflineSupplierRepository(
    private val database: AppDatabase
) : SupplierRepository {
    override fun observeSuppliers(): Flow<List<Supplier>> =
        database.supplierDao().observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun saveSupplier(supplier: Supplier, actor: LoggedInUser) {
        val entity = supplier.toEntity(now())
        if (supplier.id == 0L) {
            database.supplierDao().insert(entity.copy(id = 0))
            log(actor, "CREATE", "SUPPLIERS", "Proveedor creado: ${supplier.name}")
        } else {
            database.supplierDao().update(entity)
            log(actor, "UPDATE", "SUPPLIERS", "Proveedor actualizado: ${supplier.name}")
        }
    }

    override suspend fun deleteSupplier(supplierId: Long, actor: LoggedInUser) {
        database.supplierDao().deleteById(supplierId)
        log(actor, "DELETE", "SUPPLIERS", "Proveedor eliminado con id $supplierId")
    }

    private suspend fun log(actor: LoggedInUser, action: String, module: String, details: String) {
        database.auditDao().insert(
            AuditLogEntity(
                userId = actor.id,
                userName = actor.fullName,
                action = action,
                module = module,
                details = details,
                createdAt = now()
            )
        )
    }
}

class OfflineSalesRepository(
    private val database: AppDatabase
) : SalesRepository {
    override fun observeSalesHistory(): Flow<List<SaleReceipt>> =
        database.saleDao().observeSales().map { rows -> rows.map { it.toDomain() } }

    override suspend fun checkout(
        items: List<SaleCartItem>,
        customerId: Long?,
        paymentMethod: PaymentMethod,
        actor: LoggedInUser
    ): Result<Unit> {
        if (items.isEmpty()) {
            return Result.failure(IllegalArgumentException("El carrito esta vacio"))
        }

        return runCatching {
            database.withTransaction {
                items.forEach { item ->
                    val product = database.productDao().getById(item.productId)
                        ?: error("Producto no encontrado")
                    require(product.stock >= item.quantity) { "Stock insuficiente para ${product.name}" }
                }

                val total = items.sumOf { it.subtotal }
                val saleId = database.saleDao().insertSale(
                    SaleEntity(
                        customerId = customerId,
                        total = total,
                        paymentMethod = paymentMethod.name,
                        createdAt = now(),
                        createdByUserId = actor.id
                    )
                )

                database.saleDao().insertSaleItems(
                    items.map {
                        SaleItemEntity(
                            saleId = saleId,
                            productId = it.productId,
                            quantity = it.quantity,
                            unitPrice = it.price
                        )
                    }
                )

                items.forEach { item ->
                    val product = database.productDao().getById(item.productId) ?: return@forEach
                    database.productDao().updateStockAndCost(item.productId, product.stock - item.quantity, product.cost, now())
                }

                database.auditDao().insert(
                    AuditLogEntity(
                        userId = actor.id,
                        userName = actor.fullName,
                        action = "CHECKOUT",
                        module = "SALES",
                        details = "Venta registrada por RD$ %.2f".format(total),
                        createdAt = now()
                    )
                )
            }
        }
    }
}

class OfflinePurchasesRepository(
    private val database: AppDatabase
) : PurchasesRepository {
    override fun observePurchases(): Flow<List<PurchaseRecord>> =
        database.purchaseDao().observePurchases().map { rows -> rows.map { it.toDomain() } }

    override suspend fun registerPurchase(
        supplierId: Long,
        items: List<PurchaseItemInput>,
        actor: LoggedInUser
    ): Result<Unit> {
        if (items.isEmpty()) {
            return Result.failure(IllegalArgumentException("No hay items en la compra"))
        }

        return runCatching {
            database.withTransaction {
                val total = items.sumOf { it.subtotal }
                val purchaseId = database.purchaseDao().insertPurchase(
                    PurchaseEntity(
                        supplierId = supplierId,
                        total = total,
                        status = "RECEIVED",
                        notes = "",
                        createdAt = now(),
                        createdByUserId = actor.id
                    )
                )

                database.purchaseDao().insertPurchaseItems(
                    items.map {
                        PurchaseItemEntity(
                            purchaseId = purchaseId,
                            productId = it.productId,
                            quantity = it.quantity,
                            cost = it.cost
                        )
                    }
                )

                items.forEach { item ->
                    val product = database.productDao().getById(item.productId)
                        ?: error("Producto no encontrado")
                    database.productDao().updateStockAndCost(item.productId, product.stock + item.quantity, item.cost, now())
                }

                database.auditDao().insert(
                    AuditLogEntity(
                        userId = actor.id,
                        userName = actor.fullName,
                        action = "PURCHASE",
                        module = "PURCHASES",
                        details = "Compra registrada por RD$ %.2f".format(total),
                        createdAt = now()
                    )
                )
            }
        }
    }
}

class OfflineCashRepository(
    private val database: AppDatabase
) : CashRepository {
    override fun observeLatestSession() =
        database.cashDao().observeLatestSession().map { row -> row?.toDomain() }

    override suspend fun openCash(amount: Double, actor: LoggedInUser): Result<Unit> {
        val active = database.cashDao().getActiveSession()
        if (active != null) {
            return Result.failure(IllegalStateException("Ya existe una caja abierta"))
        }
        database.cashDao().insert(
            CashSessionEntity(
                openedByUserId = actor.id,
                openedAt = now(),
                openingAmount = amount,
                closedAt = null,
                closingAmount = null,
                expectedAmount = null,
                status = CashSessionStatus.OPEN.name
            )
        )
        database.auditDao().insert(
            AuditLogEntity(
                userId = actor.id,
                userName = actor.fullName,
                action = "OPEN",
                module = "CASH",
                details = "Caja abierta con RD$ %.2f".format(amount),
                createdAt = now()
            )
        )
        return Result.success(Unit)
    }

    override suspend fun closeCash(amount: Double, actor: LoggedInUser): Result<Unit> {
        val active = database.cashDao().getActiveSession()
            ?: return Result.failure(IllegalStateException("No hay caja abierta"))
        val expected = active.openingAmount + database.saleDao().getTodaySalesAmount(startOfDay())
        database.cashDao().update(
            active.copy(
                closedAt = now(),
                closingAmount = amount,
                expectedAmount = expected,
                status = CashSessionStatus.CLOSED.name
            )
        )
        database.auditDao().insert(
            AuditLogEntity(
                userId = actor.id,
                userName = actor.fullName,
                action = "CLOSE",
                module = "CASH",
                details = "Caja cerrada con RD$ %.2f".format(amount),
                createdAt = now()
            )
        )
        return Result.success(Unit)
    }
}

class OfflineDashboardRepository(
    private val database: AppDatabase
) : DashboardRepository {
    override fun observeOverview(): Flow<DashboardOverview> =
        combine(
            database.productDao().observeCount(),
            database.customerDao().observeCount(),
            database.supplierDao().observeCount(),
            database.saleDao().observeTodaySales(startOfDay()),
            database.saleDao().observeTodayTransactions(startOfDay()),
            database.productDao().observeLowStock(),
            database.productDao().observeInventoryValue()
        ) { args: Array<Any?> ->
            val productCount = args[0] as Int
            val customerCount = args[1] as Int
            val supplierCount = args[2] as Int
            val todaySales = args[3] as Double
            val todayTransactions = args[4] as Int
            val lowStock = args[5] as List<*>
            val inventoryValue = args[6] as Double
            
            // Calculo simple de ganancias basado en ventas de hoy (Precio - Costo)
            // Nota: Esto es una estimacion simplificada para el dashboard
            DashboardOverview(
                productCount = productCount,
                customerCount = customerCount,
                supplierCount = supplierCount,
                todaySales = todaySales,
                todayTransactions = todayTransactions,
                lowStockCount = lowStock.size,
                totalStockValue = inventoryValue,
                totalProfit = todaySales * 0.25 // Estimacion del 25% de margen para el ejemplo
            )
        }
}

class OfflineReportsRepository(
    private val database: AppDatabase
) : ReportsRepository {
    override fun observeSummary(): Flow<ReportSummary> =
        combine(
            database.saleDao().observeTodaySales(startOfDay()),
            database.saleDao().observeTodayTransactions(startOfDay()),
            database.productDao().observeInventoryValue(),
            database.productDao().observeLowStock()
        ) { sales, transactions, inventoryValue, lowStock ->
            ReportSummary(sales, transactions, inventoryValue, lowStock.size)
        }
}

class OfflineAuditRepository(
    private val database: AppDatabase
) : com.playground.facturacion.domain.AuditRepository {
    override fun observeLogs(): Flow<List<AuditLog>> =
        database.auditDao().observeAll().map { rows -> rows.map { it.toDomain() } }
}

private fun now(): Long = System.currentTimeMillis()

private fun startOfDay(): Long {
    return Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}
