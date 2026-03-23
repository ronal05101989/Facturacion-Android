package com.playground.facturacion.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val username: String,
    val password: String,
    val fullName: String,
    val role: String,
    val isActive: Boolean
)

@Entity(tableName = "licenses")
data class LicenseEntity(
    @PrimaryKey val id: Long = 1,
    val licenseKey: String,
    val businessName: String,
    val isActive: Boolean,
    val expiresAt: Long?
)

@Entity(tableName = "products", indices = [Index(value = ["code"], unique = true)])
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val code: String,
    val name: String,
    val description: String,
    val price: Double,
    val cost: Double,
    val stock: Int,
    val minStock: Int,
    val category: String,
    val barcode: String,
    val updatedAt: Long
)

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String,
    val email: String,
    val address: String,
    val document: String,
    val balance: Double = 0.0,
    val creditLimit: Double = 0.0,
    val updatedAt: Long
)

@Entity(tableName = "suppliers")
data class SupplierEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String,
    val email: String,
    val address: String,
    val contactPerson: String,
    val updatedAt: Long
)

@Entity(
    tableName = "sales",
    foreignKeys = [
        ForeignKey(
            entity = CustomerEntity::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["createdByUserId"],
            onDelete = ForeignKey.NO_ACTION
        )
    ],
    indices = [Index("customerId"), Index("createdByUserId")]
)
data class SaleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerId: Long?,
    val total: Double,
    val paymentMethod: String,
    val createdAt: Long,
    val createdByUserId: Long
)

@Entity(
    tableName = "sale_items",
    foreignKeys = [
        ForeignKey(
            entity = SaleEntity::class,
            parentColumns = ["id"],
            childColumns = ["saleId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.NO_ACTION
        )
    ],
    indices = [Index("saleId"), Index("productId")]
)
data class SaleItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val saleId: Long,
    val productId: Long,
    val quantity: Int,
    val unitPrice: Double
)

@Entity(
    tableName = "purchases",
    foreignKeys = [
        ForeignKey(
            entity = SupplierEntity::class,
            parentColumns = ["id"],
            childColumns = ["supplierId"],
            onDelete = ForeignKey.NO_ACTION
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["createdByUserId"],
            onDelete = ForeignKey.NO_ACTION
        )
    ],
    indices = [Index("supplierId"), Index("createdByUserId")]
)
data class PurchaseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val supplierId: Long,
    val total: Double,
    val status: String,
    val notes: String,
    val createdAt: Long,
    val createdByUserId: Long
)

@Entity(
    tableName = "purchase_items",
    foreignKeys = [
        ForeignKey(
            entity = PurchaseEntity::class,
            parentColumns = ["id"],
            childColumns = ["purchaseId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.NO_ACTION
        )
    ],
    indices = [Index("purchaseId"), Index("productId")]
)
data class PurchaseItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val purchaseId: Long,
    val productId: Long,
    val quantity: Int,
    val cost: Double
)

@Entity(
    tableName = "cash_sessions",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["openedByUserId"],
            onDelete = ForeignKey.NO_ACTION
        )
    ],
    indices = [Index("openedByUserId")]
)
data class CashSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val openedByUserId: Long,
    val openedAt: Long,
    val openingAmount: Double,
    val closedAt: Long?,
    val closingAmount: Double?,
    val expectedAmount: Double?,
    val status: String
)

@Entity(
    tableName = "audit_logs",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("userId")]
)
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long?,
    val userName: String,
    val action: String,
    val module: String,
    val details: String,
    val createdAt: Long
)
