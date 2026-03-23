package com.playground.facturacion.core

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.playground.facturacion.presentation.AuditViewModel
import com.playground.facturacion.presentation.CashViewModel
import com.playground.facturacion.presentation.CustomersViewModel
import com.playground.facturacion.presentation.DashboardViewModel
import com.playground.facturacion.presentation.InventoryViewModel
import com.playground.facturacion.presentation.LoginViewModel
import com.playground.facturacion.presentation.PurchasesViewModel
import com.playground.facturacion.presentation.ReportsViewModel
import com.playground.facturacion.presentation.SalesViewModel
import com.playground.facturacion.presentation.SuppliersViewModel

class AppViewModelFactory(
    private val container: AppContainer
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when (modelClass) {
            LoginViewModel::class.java -> LoginViewModel(container.authRepository) as T
            DashboardViewModel::class.java -> DashboardViewModel(container.authRepository, container.dashboardRepository) as T
            InventoryViewModel::class.java -> InventoryViewModel(container.authRepository, container.inventoryRepository) as T
            CustomersViewModel::class.java -> CustomersViewModel(container.authRepository, container.customerRepository) as T
            SuppliersViewModel::class.java -> SuppliersViewModel(container.authRepository, container.supplierRepository) as T
            SalesViewModel::class.java -> SalesViewModel(
                container.authRepository,
                container.inventoryRepository,
                container.customerRepository,
                container.salesRepository
            ) as T
            PurchasesViewModel::class.java -> PurchasesViewModel(
                container.authRepository,
                container.inventoryRepository,
                container.supplierRepository,
                container.purchasesRepository
            ) as T
            CashViewModel::class.java -> CashViewModel(container.authRepository, container.cashRepository) as T
            ReportsViewModel::class.java -> ReportsViewModel(
                container.reportsRepository,
                container.inventoryRepository,
                container.salesRepository
            ) as T
            AuditViewModel::class.java -> AuditViewModel(container.auditRepository) as T
            else -> error("Unsupported ViewModel: ${modelClass.name}")
        }
    }
}
