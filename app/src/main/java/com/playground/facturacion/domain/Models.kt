package com.playground.facturacion.domain

enum class UserRole { ADMIN, USER }

enum class PaymentMethod { CASH, CARD, TRANSFER }

enum class CashSessionStatus { OPEN, CLOSED }

data class LoggedInUser(
    val id: Long,
    val username: String,
    val fullName: String,
    val role: UserRole
)

data class LicenseInfo(
    val id: Long,
    val licenseKey: String,
    val businessName: String,
    val isActive: Boolean,
    val expiresAt: Long?
)

data class Product(
    val id: Long = 0,
    val code: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val cost: Double = 0.0,
    val stock: Int = 0,
    val minStock: Int = 0,
    val category: String = "",
    val barcode: String = ""
)

data class Customer(
    val id: Long = 0,
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val document: String = "",
    val balance: Double = 0.0,
    val creditLimit: Double = 0.0
)

data class Supplier(
    val id: Long = 0,
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val contactPerson: String = ""
)

data class SaleCartItem(
    val productId: Long,
    val productCode: String,
    val productName: String,
    val price: Double,
    val quantity: Int
) {
    val subtotal: Double get() = price * quantity
}

data class SaleReceipt(
    val id: Long,
    val customerName: String?,
    val total: Double,
    val paymentMethod: PaymentMethod,
    val createdAt: Long,
    val items: List<SaleCartItem>
)

data class PurchaseItemInput(
    val productId: Long,
    val productName: String,
    val quantity: Int,
    val cost: Double
) {
    val subtotal: Double get() = cost * quantity
}

data class PurchaseRecord(
    val id: Long,
    val supplierName: String,
    val total: Double,
    val createdAt: Long,
    val items: List<PurchaseItemInput>
)

data class CashSession(
    val id: Long,
    val openingBalance: Double,
    val closingBalance: Double?,
    val openedAt: Long,
    val closedAt: Long?,
    val status: CashSessionStatus
)

data class DashboardOverview(
    val productCount: Int = 0,
    val customerCount: Int = 0,
    val supplierCount: Int = 0,
    val todaySales: Double = 0.0,
    val todayTransactions: Int = 0,
    val lowStockCount: Int = 0,
    val totalStockValue: Double = 0.0,
    val totalProfit: Double = 0.0
)

data class ReportSummary(
    val todaySales: Double = 0.0,
    val facturas: Int = 0,
    val ganancia: Double = 0.0,
    val porPago: Map<String, Double> = emptyMap(),
    val porProducto: Map<String, Double> = emptyMap()
)

data class AuditLog(
    val id: Long,
    val userName: String,
    val action: String,
    val module: String,
    val details: String,
    val createdAt: Long
)
