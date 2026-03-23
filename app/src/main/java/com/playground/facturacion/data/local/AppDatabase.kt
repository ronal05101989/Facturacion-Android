package com.playground.facturacion.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserEntity::class,
        LicenseEntity::class,
        ProductEntity::class,
        CustomerEntity::class,
        SupplierEntity::class,
        SaleEntity::class,
        SaleItemEntity::class,
        PurchaseEntity::class,
        PurchaseItemEntity::class,
        CashSessionEntity::class,
        AuditLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun licenseDao(): LicenseDao
    abstract fun productDao(): ProductDao
    abstract fun customerDao(): CustomerDao
    abstract fun supplierDao(): SupplierDao
    abstract fun saleDao(): SaleDao
    abstract fun purchaseDao(): PurchaseDao
    abstract fun cashDao(): CashDao
    abstract fun auditDao(): AuditDao
}
