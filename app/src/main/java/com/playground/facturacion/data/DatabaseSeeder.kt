package com.playground.facturacion.data

import androidx.room.withTransaction
import com.playground.facturacion.data.local.AppDatabase
import com.playground.facturacion.data.local.CustomerEntity
import com.playground.facturacion.data.local.LicenseEntity
import com.playground.facturacion.data.local.ProductEntity
import com.playground.facturacion.data.local.SupplierEntity
import com.playground.facturacion.data.local.UserEntity

class DatabaseSeeder(
    private val database: AppDatabase
) {
    suspend fun seed() {
        database.withTransaction {
            val now = System.currentTimeMillis()

            if (database.userDao().count() == 0) {
                database.userDao().insertAll(
                    listOf(
                        UserEntity(username = "admin", password = "1234", fullName = "Administrador General", role = "ADMIN", isActive = true),
                        UserEntity(username = "caja", password = "1234", fullName = "Usuario Caja", role = "USER", isActive = true)
                    )
                )
            }

            if (database.licenseDao().count() == 0) {
                database.licenseDao().upsert(
                    LicenseEntity(
                        licenseKey = "DEMO-2026-FACTURACION",
                        businessName = "Vende Facil RD",
                        isActive = true,
                        expiresAt = null
                    )
                )
            }

            if (database.productDao().count() == 0) {
                database.productDao().insertAll(
                    listOf(
                        ProductEntity(code = "P-1001", name = "Paracetamol 500mg", description = "Tabletas para dolor y fiebre", price = 95.0, cost = 60.0, stock = 30, minStock = 10, category = "Medicamentos", barcode = "7501001001", updatedAt = now),
                        ProductEntity(code = "P-1002", name = "Ibuprofeno 400mg", description = "Antiinflamatorio", price = 125.0, cost = 80.0, stock = 20, minStock = 8, category = "Medicamentos", barcode = "7501001002", updatedAt = now),
                        ProductEntity(code = "P-1003", name = "Papel termico 80mm", description = "Rollo para impresora POS", price = 45.0, cost = 20.0, stock = 6, minStock = 10, category = "Consumibles", barcode = "7501001003", updatedAt = now)
                    )
                )
            }

            if (database.customerDao().count() == 0) {
                database.customerDao().insertAll(
                    listOf(
                        CustomerEntity(name = "Cliente contado", phone = "809-000-0000", email = "", address = "Venta general", document = "N/A", updatedAt = now),
                        CustomerEntity(name = "Farmacia Central", phone = "809-555-1000", email = "compras@farmaciacentral.do", address = "Santo Domingo", document = "RNC 101010101", updatedAt = now)
                    )
                )
            }

            if (database.supplierDao().count() == 0) {
                database.supplierDao().insertAll(
                    listOf(
                        SupplierEntity(name = "Distribuidora Medisur", phone = "809-555-2000", email = "ventas@medisur.do", address = "Santiago", contactPerson = "Laura Perez", updatedAt = now),
                        SupplierEntity(name = "Papeles del Caribe", phone = "809-555-3000", email = "contacto@papelescaribe.do", address = "Distrito Nacional", contactPerson = "Carlos Gomez", updatedAt = now)
                    )
                )
            }
        }
    }
}
