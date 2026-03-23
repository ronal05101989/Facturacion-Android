package com.playground.facturacion.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.playground.facturacion.domain.AuditRepository
import com.playground.facturacion.domain.AuthRepository
import com.playground.facturacion.domain.CashRepository
import com.playground.facturacion.domain.Customer
import com.playground.facturacion.domain.CustomerRepository
import com.playground.facturacion.domain.DashboardOverview
import com.playground.facturacion.domain.DashboardRepository
import com.playground.facturacion.domain.InventoryRepository
import com.playground.facturacion.domain.LoggedInUser
import com.playground.facturacion.domain.PaymentMethod
import com.playground.facturacion.domain.Product
import com.playground.facturacion.domain.PurchaseItemInput
import com.playground.facturacion.domain.PurchasesRepository
import com.playground.facturacion.domain.ReportSummary
import com.playground.facturacion.domain.ReportsRepository
import com.playground.facturacion.domain.SaleCartItem
import com.playground.facturacion.domain.SaleReceipt
import com.playground.facturacion.domain.SalesRepository
import com.playground.facturacion.domain.Supplier
import com.playground.facturacion.domain.SupplierRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class LoginUiState(
    val username: String = "admin",
    val password: String = "1234",
    val licenseKey: String = "DEMO-2026-FACTURACION",
    val loading: Boolean = false,
    val error: String? = null
)

class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    var uiState by mutableStateOf(LoginUiState())
        private set

    val session = authRepository.currentUser.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun updateUsername(value: String) {
        uiState = uiState.copy(username = value)
    }

    fun updatePassword(value: String) {
        uiState = uiState.copy(password = value)
    }

    fun updateLicense(value: String) {
        uiState = uiState.copy(licenseKey = value)
    }

    fun login() {
        viewModelScope.launch {
            uiState = uiState.copy(loading = true, error = null)
            val result = authRepository.login(uiState.username, uiState.password, uiState.licenseKey)
            uiState = uiState.copy(loading = false, error = result.exceptionOrNull()?.message)
        }
    }
}

class DashboardViewModel(
    private val authRepository: AuthRepository,
    dashboardRepository: DashboardRepository
) : ViewModel() {
    val session = authRepository.currentUser.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    val overview = dashboardRepository.observeOverview().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        DashboardOverview()
    )

    fun logout() {
        viewModelScope.launch { authRepository.logout() }
    }
}

class InventoryViewModel(
    authRepository: AuthRepository,
    private val repository: InventoryRepository
) : ViewModel() {
    private val session = authRepository.currentUser.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    val products = repository.observeProducts().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    var message by mutableStateOf<String?>(null)
        private set

    fun save(product: Product) {
        viewModelScope.launch {
            repository.saveProduct(product, requireUser())
            message = "Producto guardado"
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch {
            repository.deleteProduct(id, requireUser())
            message = "Producto eliminado"
        }
    }

    fun clearMessage() {
        message = null
    }

    private suspend fun requireUser(): LoggedInUser = checkNotNull(session.first())
}

class CustomersViewModel(
    authRepository: AuthRepository,
    private val repository: CustomerRepository
) : ViewModel() {
    private val session = authRepository.currentUser.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    val customers = repository.observeCustomers().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    var message by mutableStateOf<String?>(null)
        private set

    fun save(customer: Customer) {
        viewModelScope.launch {
            repository.saveCustomer(customer, requireUser())
            message = "Cliente guardado"
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch {
            repository.deleteCustomer(id, requireUser())
            message = "Cliente eliminado"
        }
    }

    fun clearMessage() {
        message = null
    }

    private suspend fun requireUser(): LoggedInUser = checkNotNull(session.first())
}

class SuppliersViewModel(
    authRepository: AuthRepository,
    private val repository: SupplierRepository
) : ViewModel() {
    private val session = authRepository.currentUser.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    val suppliers = repository.observeSuppliers().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    var message by mutableStateOf<String?>(null)
        private set

    fun save(supplier: Supplier) {
        viewModelScope.launch {
            repository.saveSupplier(supplier, requireUser())
            message = "Proveedor guardado"
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch {
            repository.deleteSupplier(id, requireUser())
            message = "Proveedor eliminado"
        }
    }

    fun clearMessage() {
        message = null
    }

    private suspend fun requireUser(): LoggedInUser = checkNotNull(session.first())
}

class SalesViewModel(
    authRepository: AuthRepository,
    inventoryRepository: InventoryRepository,
    customerRepository: CustomerRepository,
    private val salesRepository: SalesRepository
) : ViewModel() {
    private val session = authRepository.currentUser.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    val products = inventoryRepository.observeProducts().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val customers = customerRepository.observeCustomers().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val sales = salesRepository.observeSalesHistory().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val cart = mutableStateListOf<SaleCartItem>()
    var selectedCustomerId by mutableStateOf<Long?>(null)
        private set
    var paymentMethod by mutableStateOf(PaymentMethod.CASH)
        private set
    var message by mutableStateOf<String?>(null)
        private set

    val total: Double
        get() = cart.sumOf { it.subtotal }

    fun selectCustomer(id: Long?) {
        selectedCustomerId = id
    }

    fun updatePaymentMethod(method: PaymentMethod) {
        paymentMethod = method
    }

    fun addProduct(product: Product) {
        val index = cart.indexOfFirst { it.productId == product.id }
        if (index >= 0) {
            val current = cart[index]
            cart[index] = current.copy(quantity = current.quantity + 1)
        } else {
            cart.add(SaleCartItem(product.id, product.code, product.name, product.price, 1))
        }
    }

    fun increase(item: SaleCartItem) {
        val index = cart.indexOfFirst { it.productId == item.productId }
        if (index >= 0) cart[index] = item.copy(quantity = item.quantity + 1)
    }

    fun decrease(item: SaleCartItem) {
        val index = cart.indexOfFirst { it.productId == item.productId }
        if (index >= 0) {
            if (item.quantity <= 1) cart.removeAt(index) else cart[index] = item.copy(quantity = item.quantity - 1)
        }
    }

    fun checkout() {
        viewModelScope.launch {
            val result = salesRepository.checkout(cart.toList(), selectedCustomerId, paymentMethod, requireUser())
            if (result.isSuccess) {
                cart.clear()
                selectedCustomerId = null
                paymentMethod = PaymentMethod.CASH
                message = "Venta completada"
            } else {
                message = result.exceptionOrNull()?.message
            }
        }
    }

    fun clearMessage() {
        message = null
    }

    private suspend fun requireUser(): LoggedInUser = checkNotNull(session.first())
}

class PurchasesViewModel(
    authRepository: AuthRepository,
    inventoryRepository: InventoryRepository,
    supplierRepository: SupplierRepository,
    private val repository: PurchasesRepository
) : ViewModel() {
    private val session = authRepository.currentUser.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    val products = inventoryRepository.observeProducts().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val suppliers = supplierRepository.observeSuppliers().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val purchases = repository.observePurchases().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val draftItems = mutableStateListOf<PurchaseItemInput>()
    var selectedSupplierId by mutableStateOf<Long?>(null)
        private set
    var message by mutableStateOf<String?>(null)
        private set

    val total: Double
        get() = draftItems.sumOf { it.subtotal }

    fun selectSupplier(id: Long?) {
        selectedSupplierId = id
    }

    fun addProduct(product: Product) {
        val index = draftItems.indexOfFirst { it.productId == product.id }
        if (index >= 0) {
            val current = draftItems[index]
            draftItems[index] = current.copy(quantity = current.quantity + 1)
        } else {
            draftItems.add(PurchaseItemInput(product.id, product.name, 1, product.cost))
        }
    }

    fun increase(item: PurchaseItemInput) {
        val index = draftItems.indexOfFirst { it.productId == item.productId }
        if (index >= 0) draftItems[index] = item.copy(quantity = item.quantity + 1)
    }

    fun decrease(item: PurchaseItemInput) {
        val index = draftItems.indexOfFirst { it.productId == item.productId }
        if (index >= 0) {
            if (item.quantity <= 1) draftItems.removeAt(index) else draftItems[index] = item.copy(quantity = item.quantity - 1)
        }
    }

    fun registerPurchase() {
        viewModelScope.launch {
            val supplierId = selectedSupplierId ?: run {
                message = "Selecciona un proveedor"
                return@launch
            }
            val result = repository.registerPurchase(supplierId, draftItems.toList(), requireUser())
            if (result.isSuccess) {
                draftItems.clear()
                selectedSupplierId = null
                message = "Compra registrada"
            } else {
                message = result.exceptionOrNull()?.message
            }
        }
    }

    fun clearMessage() {
        message = null
    }

    private suspend fun requireUser(): LoggedInUser = checkNotNull(session.first())
}

class CashViewModel(
    authRepository: AuthRepository,
    private val repository: CashRepository
) : ViewModel() {
    private val session = authRepository.currentUser.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    val cashSession = repository.observeLatestSession().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    var message by mutableStateOf<String?>(null)
        private set

    fun openCash(amount: Double) {
        viewModelScope.launch {
            val result = repository.openCash(amount, requireUser())
            message = if (result.isSuccess) "Caja abierta" else result.exceptionOrNull()?.message
        }
    }

    fun closeCash(amount: Double) {
        viewModelScope.launch {
            val result = repository.closeCash(amount, requireUser())
            message = if (result.isSuccess) "Caja cerrada" else result.exceptionOrNull()?.message
        }
    }

    fun clearMessage() {
        message = null
    }

    private suspend fun requireUser(): LoggedInUser = checkNotNull(session.first())
}

data class ReportsUiState(
    val summary: ReportSummary = ReportSummary(),
    val lowStock: List<Product> = emptyList(),
    val sales: List<SaleReceipt> = emptyList()
)

class ReportsViewModel(
    reportsRepository: ReportsRepository,
    inventoryRepository: InventoryRepository,
    salesRepository: SalesRepository
) : ViewModel() {
    val uiState: StateFlow<ReportsUiState> = combine(
        reportsRepository.observeSummary(),
        inventoryRepository.observeLowStockProducts(),
        salesRepository.observeSalesHistory()
    ) { summary, lowStock, sales ->
        ReportsUiState(summary, lowStock, sales.take(10))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ReportsUiState())
}

class AuditViewModel(
    auditRepository: AuditRepository
) : ViewModel() {
    val logs = auditRepository.observeLogs().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
