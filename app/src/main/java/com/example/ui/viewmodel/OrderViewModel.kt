package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.dao.ItemSalesCount
import com.example.data.local.dao.OrderWithItems
import com.example.data.local.entities.DailySettlementEntity
import com.example.data.local.entities.MenuItemEntity
import com.example.data.local.entities.OrderEntity
import com.example.data.model.CartItem
import com.example.data.model.DailySummary
import com.example.data.model.OrderStatus
import com.example.data.model.PaymentMethod
import com.example.data.remote.FirestoreRepository
import com.example.data.repository.OrderRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ScreenTab {
  ORDERS,
  NEW_ORDER,
  COMPLETE_DAY,
  HISTORY
}

enum class OrdersFilter {
  ALL,
  WAITING_FOR_PAYMENT,
  COMPLETED
}

enum class HistoryDateFilter {
  ALL,
  TODAY,
  YESTERDAY,
  THIS_WEEK
}

data class OrderUiState(
  val currentTab: ScreenTab = ScreenTab.ORDERS,
  val ordersFilter: OrdersFilter = OrdersFilter.ALL,
  val historyDateFilter: HistoryDateFilter = HistoryDateFilter.ALL,
  val waiterName: String = "Sarah Wanjiku",
  val currencySymbol: String = "KSh",
  // New Order Form state
  val currentTable: String = "Table 1",
  val cart: List<CartItem> = emptyList(),
  val newOrderNotes: String = "",
  val selectedMenuCategory: String = "All",
  val menuSearchQuery: String = "",
  // Payment Sheet State
  val paymentOrder: OrderWithItems? = null,
  val selectedPaymentMethod: PaymentMethod = PaymentMethod.MPESA,
  val amountPaidInput: String = "",
  val mpesaCodeInput: String = "",
  // Detail Dialog State
  val detailOrder: OrderWithItems? = null,
  // Add item dialog state
  val showAddCustomItemDialog: Boolean = false,
  val showSettingsDialog: Boolean = false,
  val showCloseDayConfirm: Boolean = false,
  val closeDayNotes: String = "",
  val settlementSuccessBanner: String? = null,
  // Menu Management State
  val showManageMenuDialog: Boolean = false,
  val itemToEdit: MenuItemEntity? = null,
  val showEditItemDialog: Boolean = false
)

class OrderViewModel(application: Application) : AndroidViewModel(application) {

  private val repository: OrderRepository

  init {
    val db = AppDatabase.getDatabase(application, viewModelScope)
    val firestoreRepository = FirestoreRepository()
    repository = OrderRepository(
      menuItemDao = db.menuItemDao(),
      orderDao = db.orderDao(),
      dailySettlementDao = db.dailySettlementDao(),
      firestoreRepository = firestoreRepository
    )
    viewModelScope.launch {
      repository.ensureMenuPopulated()
    }
  }

  private val _uiState = MutableStateFlow(OrderUiState())
  val uiState: StateFlow<OrderUiState> = _uiState.asStateFlow()

  val menuItems: StateFlow<List<MenuItemEntity>> = repository.allMenuItems
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allOrders: StateFlow<List<OrderWithItems>> = repository.allOrders
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val todayOrders: StateFlow<List<OrderWithItems>> = repository.getTodayOrders()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val todaySummary: StateFlow<DailySummary> = repository.getTodaySummary()
    .stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(5000),
      DailySummary(
        date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
        totalSales = 0.0,
        mpesaSales = 0.0,
        cashSales = 0.0,
        unpaidAmount = 0.0,
        totalOrdersCount = 0,
        completedOrdersCount = 0,
        pendingOrdersCount = 0
      )
    )

  val topSellingToday: StateFlow<List<ItemSalesCount>> = repository.getTodayTopSelling()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val pastSettlements: StateFlow<List<DailySettlementEntity>> = repository.allSettlements
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  private val _snackbarEvent = MutableSharedFlow<String>()
  val snackbarEvent: SharedFlow<String> = _snackbarEvent.asSharedFlow()

  // Navigation
  fun setTab(tab: ScreenTab) {
    _uiState.value = _uiState.value.copy(currentTab = tab)
  }

  fun setOrdersFilter(filter: OrdersFilter) {
    _uiState.value = _uiState.value.copy(ordersFilter = filter)
  }

  fun setHistoryDateFilter(filter: HistoryDateFilter) {
    _uiState.value = _uiState.value.copy(historyDateFilter = filter)
  }

  fun setWaiterName(name: String) {
    _uiState.value = _uiState.value.copy(waiterName = name)
  }

  fun setCurrencySymbol(symbol: String) {
    _uiState.value = _uiState.value.copy(currencySymbol = symbol)
  }

  fun setTable(table: String) {
    _uiState.value = _uiState.value.copy(currentTable = table)
  }

  fun setNewOrderNotes(notes: String) {
    _uiState.value = _uiState.value.copy(newOrderNotes = notes)
  }

  fun setMenuCategory(category: String) {
    _uiState.value = _uiState.value.copy(selectedMenuCategory = category)
  }

  fun setMenuSearchQuery(query: String) {
    _uiState.value = _uiState.value.copy(menuSearchQuery = query)
  }

  // Cart operations
  fun addToCart(item: MenuItemEntity) {
    val current = _uiState.value.cart.toMutableList()
    val existingIndex = current.indexOfFirst { it.name == item.name }
    if (existingIndex >= 0) {
      val existing = current[existingIndex]
      current[existingIndex] = existing.copy(quantity = existing.quantity + 1)
    } else {
      current.add(
        CartItem(
          menuItemId = item.id,
          name = item.name,
          unitPrice = item.price,
          quantity = 1
        )
      )
    }
    _uiState.value = _uiState.value.copy(cart = current)
  }

  fun addCustomItemToCart(name: String, price: Double, quantity: Int = 1) {
    if (name.isBlank() || price <= 0) return
    val current = _uiState.value.cart.toMutableList()
    current.add(
      CartItem(
        menuItemId = null,
        name = name.trim(),
        unitPrice = price,
        quantity = quantity
      )
    )
    _uiState.value = _uiState.value.copy(cart = current, showAddCustomItemDialog = false)
  }

  fun updateCartItemQuantity(index: Int, newQuantity: Int) {
    val current = _uiState.value.cart.toMutableList()
    if (index in current.indices) {
      if (newQuantity <= 0) {
        current.removeAt(index)
      } else {
        current[index] = current[index].copy(quantity = newQuantity)
      }
      _uiState.value = _uiState.value.copy(cart = current)
    }
  }

  fun removeFromCart(index: Int) {
    val current = _uiState.value.cart.toMutableList()
    if (index in current.indices) {
      current.removeAt(index)
      _uiState.value = _uiState.value.copy(cart = current)
    }
  }

  fun clearCart() {
    _uiState.value = _uiState.value.copy(cart = emptyList(), newOrderNotes = "")
  }

  // Create Order
  fun submitOrder(immediatePay: Boolean = false) {
    val state = _uiState.value
    if (state.cart.isEmpty()) {
      viewModelScope.launch {
        _snackbarEvent.emit("Please add at least one item to the order")
      }
      return
    }

    viewModelScope.launch {
      val orderId = repository.createOrder(
        tableName = state.currentTable,
        waiterName = state.waiterName,
        items = state.cart,
        notes = state.newOrderNotes,
        immediatePayment = immediatePay,
        paymentMethod = if (immediatePay) state.selectedPaymentMethod else null,
        amountPaid = if (immediatePay) (state.amountPaidInput.toDoubleOrNull() ?: state.cart.sumOf { it.subtotal }) else 0.0,
        changeAmount = 0.0,
        mpesaCode = if (immediatePay && state.selectedPaymentMethod == PaymentMethod.MPESA) state.mpesaCodeInput.ifBlank { null } else null
      )

      clearCart()
      _snackbarEvent.emit(
        if (immediatePay) "Order created and marked as PAID!" else "Order recorded: Waiting for Payment"
      )
      setTab(ScreenTab.ORDERS)
    }
  }

  // Payment flow
  fun openPaymentSheet(orderWithItems: OrderWithItems) {
    _uiState.value = _uiState.value.copy(
      paymentOrder = orderWithItems,
      selectedPaymentMethod = PaymentMethod.MPESA,
      amountPaidInput = String.format(Locale.US, "%.0f", orderWithItems.order.totalAmount),
      mpesaCodeInput = ""
    )
  }

  fun closePaymentSheet() {
    _uiState.value = _uiState.value.copy(paymentOrder = null)
  }

  fun setPaymentMethod(method: PaymentMethod) {
    _uiState.value = _uiState.value.copy(selectedPaymentMethod = method)
  }

  fun setAmountPaidInput(amount: String) {
    _uiState.value = _uiState.value.copy(amountPaidInput = amount)
  }

  fun setMpesaCodeInput(code: String) {
    _uiState.value = _uiState.value.copy(mpesaCodeInput = code)
  }

  fun confirmPayment() {
    val order = _uiState.value.paymentOrder ?: return
    val method = _uiState.value.selectedPaymentMethod
    val total = order.order.totalAmount
    val enteredPaid = _uiState.value.amountPaidInput.toDoubleOrNull() ?: total

    if (enteredPaid < total) {
      viewModelScope.launch {
        _snackbarEvent.emit("Amount paid must cover the order total (${_uiState.value.currencySymbol} $total)")
      }
      return
    }

    val change = (enteredPaid - total).coerceAtLeast(0.0)
    val mpesaCode = if (method == PaymentMethod.MPESA) _uiState.value.mpesaCodeInput.trim().ifBlank { null } else null

    viewModelScope.launch {
      repository.markOrderAsPaid(
        orderId = order.order.id,
        paymentMethod = method,
        amountPaid = enteredPaid,
        changeAmount = change,
        mpesaCode = mpesaCode
      )
      closePaymentSheet()
      val changeNotice = if (change > 0) " (Change: ${_uiState.value.currencySymbol} $change)" else ""
      _snackbarEvent.emit("Payment confirmed via ${method.displayName}$changeNotice! Order completed.")
    }
  }

  // Detail Dialog
  fun openOrderDetail(orderWithItems: OrderWithItems) {
    _uiState.value = _uiState.value.copy(detailOrder = orderWithItems)
  }

  fun closeOrderDetail() {
    _uiState.value = _uiState.value.copy(detailOrder = null)
  }

  fun cancelOrder(orderId: Long) {
    viewModelScope.launch {
      repository.cancelOrder(orderId)
      closeOrderDetail()
      _snackbarEvent.emit("Order cancelled.")
    }
  }

  // Evening Complete Day Closeout
  fun openCloseDayConfirm() {
    _uiState.value = _uiState.value.copy(showCloseDayConfirm = true, closeDayNotes = "")
  }

  fun closeCloseDayConfirm() {
    _uiState.value = _uiState.value.copy(showCloseDayConfirm = false)
  }

  fun setCloseDayNotes(notes: String) {
    _uiState.value = _uiState.value.copy(closeDayNotes = notes)
  }

  fun confirmCloseDay() {
    val summary = todaySummary.value
    val state = _uiState.value
    viewModelScope.launch {
      repository.saveDailySettlement(
        waiterName = state.waiterName,
        summary = summary,
        notes = state.closeDayNotes.ifBlank { null }
      )
      _uiState.value = _uiState.value.copy(
        showCloseDayConfirm = false,
        settlementSuccessBanner = "Evening sales closeout saved! Total: ${state.currencySymbol} ${String.format(Locale.US, "%,.2f", summary.totalSales)}"
      )
      _snackbarEvent.emit("Day completed and settled successfully!")
    }
  }

  fun dismissSettlementBanner() {
    _uiState.value = _uiState.value.copy(settlementSuccessBanner = null)
  }

  // Dialog triggers
  fun setShowAddCustomItemDialog(show: Boolean) {
    _uiState.value = _uiState.value.copy(showAddCustomItemDialog = show)
  }

  fun setShowSettingsDialog(show: Boolean) {
    _uiState.value = _uiState.value.copy(showSettingsDialog = show)
  }

  // Menu Management CRUD
  fun setShowManageMenuDialog(show: Boolean) {
    _uiState.value = _uiState.value.copy(showManageMenuDialog = show)
  }

  fun openAddMenuItemDialog() {
    _uiState.value = _uiState.value.copy(itemToEdit = null, showEditItemDialog = true)
  }

  fun openEditMenuItemDialog(item: MenuItemEntity) {
    _uiState.value = _uiState.value.copy(itemToEdit = item, showEditItemDialog = true)
  }

  fun closeEditMenuItemDialog() {
    _uiState.value = _uiState.value.copy(itemToEdit = null, showEditItemDialog = false)
  }

  fun saveMenuItem(id: Long?, name: String, category: String, price: Double, emoji: String) {
    if (name.isBlank() || price <= 0) return
    viewModelScope.launch {
      if (id == null || id == 0L) {
        repository.addMenuItem(name.trim(), category.trim(), price, emoji.ifBlank { "🍽️" })
        _snackbarEvent.emit("Added \"$name\" to menu")
      } else {
        repository.updateMenuItem(
          MenuItemEntity(
            id = id,
            name = name.trim(),
            category = category.trim(),
            price = price,
            emoji = emoji.ifBlank { "🍽️" }
          )
        )
        _snackbarEvent.emit("Updated \"$name\"")
      }
      closeEditMenuItemDialog()
    }
  }

  fun deleteMenuItem(item: MenuItemEntity) {
    viewModelScope.launch {
      repository.deleteMenuItem(item)
      _snackbarEvent.emit("Removed \"${item.name}\" from menu")
    }
  }

  // Direct Order Payment from Details Dialog
  fun markOrderAsPaidDirect(
    orderId: Long,
    paymentMethod: PaymentMethod,
    amountPaid: Double,
    changeAmount: Double,
    mpesaCode: String?
  ) {
    viewModelScope.launch {
      repository.markOrderAsPaid(
        orderId = orderId,
        paymentMethod = paymentMethod,
        amountPaid = amountPaid,
        changeAmount = changeAmount,
        mpesaCode = mpesaCode
      )
      closeOrderDetail()
      val changeNotice = if (changeAmount > 0) " (Change: ${_uiState.value.currencySymbol} $changeAmount)" else ""
      _snackbarEvent.emit("Payment confirmed via ${paymentMethod.displayName}$changeNotice! Order completed.")
    }
  }
}
