package com.playground.facturacion.core

import android.app.Application
import androidx.room.Room
import com.playground.facturacion.data.DatabaseSeeder
import com.playground.facturacion.data.OfflineAuditRepository
import com.playground.facturacion.data.OfflineAuthRepository
import com.playground.facturacion.data.OfflineCashRepository
import com.playground.facturacion.data.OfflineCustomerRepository
import com.playground.facturacion.data.OfflineDashboardRepository
import com.playground.facturacion.data.OfflineInventoryRepository
import com.playground.facturacion.data.OfflinePurchasesRepository
import com.playground.facturacion.data.OfflineReportsRepository
import com.playground.facturacion.data.OfflineSalesRepository
import com.playground.facturacion.data.OfflineSupplierRepository
import com.playground.facturacion.data.SessionManager
import com.playground.facturacion.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AppContainer(application: Application) {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val database: AppDatabase = Room.databaseBuilder(
        application,
        AppDatabase::class.java,
        "facturacion.db"
    ).fallbackToDestructiveMigration().build()

    val sessionManager = SessionManager()
    val authRepository = OfflineAuthRepository(database, sessionManager)
    val inventoryRepository = OfflineInventoryRepository(database)
    val customerRepository = OfflineCustomerRepository(database)
    val supplierRepository = OfflineSupplierRepository(database)
    val salesRepository = OfflineSalesRepository(database)
    val purchasesRepository = OfflinePurchasesRepository(database)
    val cashRepository = OfflineCashRepository(database)
    val dashboardRepository = OfflineDashboardRepository(database)
    val reportsRepository = OfflineReportsRepository(database)
    val auditRepository = OfflineAuditRepository(database)

    init {
        applicationScope.launch {
            DatabaseSeeder(database).seed()
        }
    }
}
