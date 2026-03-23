package com.playground.facturacion.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

data class SaleSummaryRow(
    val id: Long,
    val customerName: String?,
    val total: Double,
    val paymentMethod: String,
    val createdAt: Long,
    val createdBy: String
)

data class PurchaseSummaryRow(
    val id: Long,
    val supplierName: String,
    val total: Double,
    val status: String,
    val createdAt: Long,
    val createdBy: String
)

data class CashSessionRow(
    val id: Long,
    val openedBy: String,
    val openedAt: Long,
    val openingAmount: Double,
    val closedAt: Long?,
    val closingAmount: Double?,
    val expectedAmount: Double?,
    val status: String
)

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE username = :username AND password = :password AND isActive = 1 LIMIT 1")
    suspend fun getByCredentials(username: String, password: String): UserEntity?

    @Query("SELECT COUNT(*) FROM users")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(users: List<UserEntity>)
}

@Dao
interface LicenseDao {
    @Query("SELECT * FROM licenses LIMIT 1")
    suspend fun getLicense(): LicenseEntity?

    @Query("SELECT COUNT(*) FROM licenses")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(license: LicenseEntity)
}

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun observeAll(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE stock <= minStock ORDER BY stock ASC, name ASC")
    fun observeLowStock(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): ProductEntity?

    @Query("SELECT COUNT(*) FROM products")
    fun observeCount(): Flow<Int>

    @Query("SELECT COALESCE(SUM(stock * cost), 0) FROM products")
    fun observeInventoryValue(): Flow<Double>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(product: ProductEntity): Long

    @Update
    suspend fun update(product: ProductEntity)

    @Query("DELETE FROM products WHERE id = :productId")
    suspend fun deleteById(productId: Long)

    @Query("UPDATE products SET stock = :stock, cost = :cost, updatedAt = :updatedAt WHERE id = :productId")
    suspend fun updateStockAndCost(productId: Long, stock: Int, cost: Double, updatedAt: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<ProductEntity>)

    @Query("SELECT COUNT(*) FROM products")
    suspend fun count(): Int
}

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun observeAll(): Flow<List<CustomerEntity>>

    @Query("SELECT COUNT(*) FROM customers")
    fun observeCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(customer: CustomerEntity): Long

    @Update
    suspend fun update(customer: CustomerEntity)

    @Query("DELETE FROM customers WHERE id = :customerId")
    suspend fun deleteById(customerId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(customers: List<CustomerEntity>)

    @Query("SELECT COUNT(*) FROM customers")
    suspend fun count(): Int
}

@Dao
interface SupplierDao {
    @Query("SELECT * FROM suppliers ORDER BY name ASC")
    fun observeAll(): Flow<List<SupplierEntity>>

    @Query("SELECT COUNT(*) FROM suppliers")
    fun observeCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(supplier: SupplierEntity): Long

    @Update
    suspend fun update(supplier: SupplierEntity)

    @Query("DELETE FROM suppliers WHERE id = :supplierId")
    suspend fun deleteById(supplierId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(suppliers: List<SupplierEntity>)

    @Query("SELECT COUNT(*) FROM suppliers")
    suspend fun count(): Int
}

@Dao
interface SaleDao {
    @Query(
        """
        SELECT sales.id, customers.name AS customerName, sales.total, sales.paymentMethod, sales.createdAt,
        users.fullName AS createdBy
        FROM sales
        LEFT JOIN customers ON customers.id = sales.customerId
        INNER JOIN users ON users.id = sales.createdByUserId
        ORDER BY sales.createdAt DESC
        """
    )
    fun observeSales(): Flow<List<SaleSummaryRow>>

    @Query("SELECT COALESCE(SUM(total), 0) FROM sales WHERE createdAt >= :startOfDay")
    fun observeTodaySales(startOfDay: Long): Flow<Double>

    @Query("SELECT COUNT(*) FROM sales WHERE createdAt >= :startOfDay")
    fun observeTodayTransactions(startOfDay: Long): Flow<Int>

    @Query("SELECT COALESCE(SUM(total), 0) FROM sales WHERE createdAt >= :startOfDay")
    suspend fun getTodaySalesAmount(startOfDay: Long): Double

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: SaleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaleItems(items: List<SaleItemEntity>)
}

@Dao
interface PurchaseDao {
    @Query(
        """
        SELECT purchases.id, suppliers.name AS supplierName, purchases.total, purchases.status, purchases.createdAt,
        users.fullName AS createdBy
        FROM purchases
        INNER JOIN suppliers ON suppliers.id = purchases.supplierId
        INNER JOIN users ON users.id = purchases.createdByUserId
        ORDER BY purchases.createdAt DESC
        """
    )
    fun observePurchases(): Flow<List<PurchaseSummaryRow>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchase(purchase: PurchaseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchaseItems(items: List<PurchaseItemEntity>)
}

@Dao
interface CashDao {
    @Query(
        """
        SELECT cash_sessions.id, users.fullName AS openedBy, cash_sessions.openedAt, cash_sessions.openingAmount,
        cash_sessions.closedAt, cash_sessions.closingAmount, cash_sessions.expectedAmount, cash_sessions.status
        FROM cash_sessions
        INNER JOIN users ON users.id = cash_sessions.openedByUserId
        ORDER BY cash_sessions.openedAt DESC
        LIMIT 1
        """
    )
    fun observeLatestSession(): Flow<CashSessionRow?>

    @Query("SELECT * FROM cash_sessions WHERE status = 'OPEN' ORDER BY openedAt DESC LIMIT 1")
    suspend fun getActiveSession(): CashSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: CashSessionEntity): Long

    @Update
    suspend fun update(session: CashSessionEntity)
}

@Dao
interface AuditDao {
    @Query("SELECT * FROM audit_logs ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: AuditLogEntity)
}
