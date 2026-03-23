package com.playground.facturacion.data

import com.playground.facturacion.data.local.AuditLogEntity
import com.playground.facturacion.data.local.CashSessionRow
import com.playground.facturacion.data.local.CustomerEntity
import com.playground.facturacion.data.local.LicenseEntity
import com.playground.facturacion.data.local.ProductEntity
import com.playground.facturacion.data.local.PurchaseSummaryRow
import com.playground.facturacion.data.local.SaleSummaryRow
import com.playground.facturacion.data.local.SupplierEntity
import com.playground.facturacion.data.local.UserEntity
import com.playground.facturacion.domain.AuditLog
import com.playground.facturacion.domain.CashSession
import com.playground.facturacion.domain.CashSessionStatus
import com.playground.facturacion.domain.Customer
import com.playground.facturacion.domain.LicenseInfo
import com.playground.facturacion.domain.LoggedInUser
import com.playground.facturacion.domain.PaymentMethod
import com.playground.facturacion.domain.Product
import com.playground.facturacion.domain.PurchaseRecord
import com.playground.facturacion.domain.SaleReceipt
import com.playground.facturacion.domain.Supplier
import com.playground.facturacion.domain.UserRole

fun UserEntity.toDomain() = LoggedInUser(
    id = id,
    username = username,
    fullName = fullName,
    role = UserRole.valueOf(role)
)

fun LicenseEntity.toDomain() = LicenseInfo(id, licenseKey, businessName, isActive, expiresAt)

fun ProductEntity.toDomain() = Product(id, code, name, description, price, cost, stock, minStock, category, barcode)

fun Product.toEntity(now: Long) = ProductEntity(
    id = id,
    code = code,
    name = name,
    description = description,
    price = price,
    cost = cost,
    stock = stock,
    minStock = minStock,
    category = category,
    barcode = barcode,
    updatedAt = now
)

fun CustomerEntity.toDomain() = Customer(id, name, phone, email, address, document, balance, creditLimit)

fun Customer.toEntity(now: Long) = CustomerEntity(id, name, phone, email, address, document, balance, creditLimit, now)

fun SupplierEntity.toDomain() = Supplier(id, name, phone, email, address, contactPerson)

fun Supplier.toEntity(now: Long) = SupplierEntity(id, name, phone, email, address, contactPerson, now)

fun SaleSummaryRow.toDomain() = SaleReceipt(
    id = id,
    customerName = customerName ?: "Venta general",
    total = total,
    paymentMethod = PaymentMethod.valueOf(paymentMethod),
    createdAt = createdAt,
    createdBy = createdBy
)

fun PurchaseSummaryRow.toDomain() = PurchaseRecord(id, supplierName, total, status, createdAt, createdBy)

fun CashSessionRow.toDomain() = CashSession(
    id = id,
    openedBy = openedBy,
    openedAt = openedAt,
    openingAmount = openingAmount,
    closedAt = closedAt,
    closingAmount = closingAmount,
    expectedAmount = expectedAmount,
    status = CashSessionStatus.valueOf(status)
)

fun AuditLogEntity.toDomain() = AuditLog(id, userName, action, module, details, createdAt)
