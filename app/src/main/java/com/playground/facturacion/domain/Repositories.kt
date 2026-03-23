package com.playground.facturacion.domain

import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<LoggedInUser?>
    suspend fun login(username: String, password: String, licenseKey: String): Result<Unit>
    suspend fun logout()
    suspend fun getLicense(): LicenseInfo?
}

interface InventoryRepository {
    fun observeProducts(): Flow<List<Product>>
    fun observeLowStockProducts(): Flow<List<Product>>
    suspend fun saveProduct(product: Product, actor: LoggedInUser)
    suspend fun deleteProduct(productId: Long, actor: LoggedInUser)
}

interface CustomerRepository {
    fun observeCustomers(): Flow<List<Customer>>
    suspend fun saveCustomer(customer: Customer, actor: LoggedInUser)
    suspend fun deleteCustomer(customerId: Long, actor: LoggedInUser)
}

interface SupplierRepository {
    fun observeSuppliers(): Flow<List<Supplier>>
    suspend fun saveSupplier(supplier: Supplier, actor: LoggedInUser)
    suspend fun deleteSupplier(supplierId: Long, actor: LoggedInUser)
}

interface SalesRepository {
    fun observeSalesHistory(): Flow<List<SaleReceipt>>
    suspend fun checkout(
        items: List<SaleCartItem>,
        customerId: Long?,
        paymentMethod: PaymentMethod,
        actor: LoggedInUser
    ): Result<Unit>
}

interface PurchasesRepository {
    fun observePurchases(): Flow<List<PurchaseRecord>>
    suspend fun registerPurchase(
        supplierId: Long,
        items: List<PurchaseItemInput>,
        actor: LoggedInUser
    ): Result<Unit>
}

interface CashRepository {
    fun observeLatestSession(): Flow<CashSession?>
    suspend fun openCash(amount: Double, actor: LoggedInUser): Result<Unit>
    suspend fun closeCash(amount: Double, actor: LoggedInUser): Result<Unit>
}

interface DashboardRepository {
    fun observeOverview(): Flow<DashboardOverview>
}

interface ReportsRepository {
    fun observeSummary(): Flow<ReportSummary>
}

interface AuditRepository {
    fun observeLogs(): Flow<List<AuditLog>>
}
