package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.OrderStatus
import com.example.ui.components.PaymentBottomSheet
import com.example.ui.dialogs.AddCustomItemDialog
import com.example.ui.dialogs.CloseDayConfirmDialog
import com.example.ui.dialogs.EditMenuItemDialog
import com.example.ui.dialogs.ManageMenuDialog
import com.example.ui.dialogs.OrderDetailDialog
import com.example.ui.dialogs.SettingsDialog
import com.example.ui.screens.CompleteDayScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.NewOrderScreen
import com.example.ui.screens.OrdersScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.Terracotta
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.OrderViewModel
import com.example.ui.viewmodel.ScreenTab
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    // Initialize Firebase
    try {
      com.google.firebase.FirebaseApp.initializeApp(this)
    } catch (e: Exception) {
      android.util.Log.e("MainActivity", "Failed to initialize Firebase", e)
    }
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        WaiterApp()
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaiterApp(
  authViewModel: AuthViewModel = viewModel(),
  orderViewModel: OrderViewModel = viewModel()
) {
  val authUiState by authViewModel.uiState.collectAsStateWithLifecycle()
  val uiState by orderViewModel.uiState.collectAsStateWithLifecycle()
  val allOrders by orderViewModel.allOrders.collectAsStateWithLifecycle()
  val menuItems by orderViewModel.menuItems.collectAsStateWithLifecycle()
  val todaySummary by orderViewModel.todaySummary.collectAsStateWithLifecycle()
  val topSellingToday by orderViewModel.topSellingToday.collectAsStateWithLifecycle()
  val pastSettlements by orderViewModel.pastSettlements.collectAsStateWithLifecycle()

  val snackbarHostState = remember { SnackbarHostState() }
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  LaunchedEffect(Unit) {
    orderViewModel.snackbarEvent.collectLatest { message ->
      snackbarHostState.showSnackbar(message)
    }
  }

  // Show login screen if not authenticated
  if (!authUiState.isAuthenticated) {
    LoginScreen(
      isLoading = authUiState.isLoading,
      errorMessage = authUiState.errorMessage,
      onSignInClick = { authViewModel.signInWithGoogle() }
    )
    return
  }

  // Back handling: pop to Orders if on another tab
  BackHandler(enabled = uiState.currentTab != ScreenTab.ORDERS) {
    orderViewModel.setTab(ScreenTab.ORDERS)
  }

  val pendingCount = allOrders.count { it.order.status == OrderStatus.WAITING_FOR_PAYMENT.name }

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    snackbarHost = { SnackbarHost(snackbarHostState) },
    bottomBar = {
      NavigationBar(
        modifier = Modifier
          .windowInsetsPadding(WindowInsets.navigationBars)
          .testTag("main_bottom_nav"),
        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface
      ) {
        // Orders Tab
        NavigationBarItem(
          selected = uiState.currentTab == ScreenTab.ORDERS,
          onClick = { orderViewModel.setTab(ScreenTab.ORDERS) },
          icon = {
            if (pendingCount > 0) {
              BadgedBox(
                badge = {
                  Badge(containerColor = Terracotta) {
                    Text("$pendingCount")
                  }
                }
              ) {
                Icon(Icons.Default.RestaurantMenu, contentDescription = "Orders")
              }
            } else {
              Icon(Icons.Default.RestaurantMenu, contentDescription = "Orders")
            }
          },
          label = { Text("Orders", fontWeight = FontWeight.SemiBold) },
          colors = NavigationBarItemDefaults.colors(
            indicatorColor = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer
          ),
          modifier = Modifier.testTag("nav_tab_orders")
        )

        // New Order Tab
        NavigationBarItem(
          selected = uiState.currentTab == ScreenTab.NEW_ORDER,
          onClick = { orderViewModel.setTab(ScreenTab.NEW_ORDER) },
          icon = {
            if (uiState.cart.isNotEmpty()) {
              BadgedBox(
                badge = {
                  Badge {
                    Text("${uiState.cart.sumOf { it.quantity }}")
                  }
                }
              ) {
                Icon(Icons.Default.AddCircle, contentDescription = "New Order")
              }
            } else {
              Icon(Icons.Default.AddCircle, contentDescription = "New Order")
            }
          },
          label = { Text("New Order", fontWeight = FontWeight.SemiBold) },
          colors = NavigationBarItemDefaults.colors(
            indicatorColor = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer
          ),
          modifier = Modifier.testTag("nav_tab_new_order")
        )

        // Complete Day Tab (Evening Settlement)
        NavigationBarItem(
          selected = uiState.currentTab == ScreenTab.COMPLETE_DAY,
          onClick = { orderViewModel.setTab(ScreenTab.COMPLETE_DAY) },
          icon = { Icon(Icons.Default.Assessment, contentDescription = "Complete Day") },
          label = { Text("Complete Day", fontWeight = FontWeight.SemiBold) },
          colors = NavigationBarItemDefaults.colors(
            indicatorColor = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer
          ),
          modifier = Modifier.testTag("nav_tab_complete_day")
        )

        // History Tab
        NavigationBarItem(
          selected = uiState.currentTab == ScreenTab.HISTORY,
          onClick = { orderViewModel.setTab(ScreenTab.HISTORY) },
          icon = { Icon(Icons.Default.History, contentDescription = "History") },
          label = { Text("History", fontWeight = FontWeight.SemiBold) },
          colors = NavigationBarItemDefaults.colors(
            indicatorColor = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer
          ),
          modifier = Modifier.testTag("nav_tab_history")
        )
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      when (uiState.currentTab) {
        ScreenTab.ORDERS -> {
          OrdersScreen(
            orders = allOrders,
            currentFilter = uiState.ordersFilter,
            waiterName = uiState.waiterName,
            currencySymbol = uiState.currencySymbol,
            onFilterChange = { orderViewModel.setOrdersFilter(it) },
            onPayClick = { orderViewModel.openPaymentSheet(it) },
            onOrderClick = { orderViewModel.openOrderDetail(it) },
            onNewOrderClick = { orderViewModel.setTab(ScreenTab.NEW_ORDER) },
            onSettingsClick = { orderViewModel.setShowSettingsDialog(true) }
          )
        }

        ScreenTab.NEW_ORDER -> {
          NewOrderScreen(
            menuItems = menuItems,
            cart = uiState.cart,
            currentTable = uiState.currentTable,
            orderNotes = uiState.newOrderNotes,
            selectedCategory = uiState.selectedMenuCategory,
            searchQuery = uiState.menuSearchQuery,
            currencySymbol = uiState.currencySymbol,
            onTableChange = { orderViewModel.setTable(it) },
            onCategoryChange = { orderViewModel.setMenuCategory(it) },
            onSearchChange = { orderViewModel.setMenuSearchQuery(it) },
            onNotesChange = { orderViewModel.setNewOrderNotes(it) },
            onAddToCart = { orderViewModel.addToCart(it) },
            onUpdateCartQuantity = { idx, qty -> orderViewModel.updateCartItemQuantity(idx, qty) },
            onRemoveFromCart = { orderViewModel.removeFromCart(it) },
            onClearCart = { orderViewModel.clearCart() },
            onOpenCustomItemDialog = { orderViewModel.setShowAddCustomItemDialog(true) },
            onOpenManageMenu = { orderViewModel.setShowManageMenuDialog(true) },
            onSubmitOrder = { immediatePay -> orderViewModel.submitOrder(immediatePay) }
          )
        }

        ScreenTab.COMPLETE_DAY -> {
          CompleteDayScreen(
            summary = todaySummary,
            topSellingItems = topSellingToday,
            pastSettlements = pastSettlements,
            waiterName = uiState.waiterName,
            currencySymbol = uiState.currencySymbol,
            settlementSuccessBanner = uiState.settlementSuccessBanner,
            onDismissSuccessBanner = { orderViewModel.dismissSettlementBanner() },
            onOpenCloseDayConfirm = { orderViewModel.openCloseDayConfirm() },
            onGoToPendingOrders = {
              orderViewModel.setOrdersFilter(com.example.ui.viewmodel.OrdersFilter.WAITING_FOR_PAYMENT)
              orderViewModel.setTab(ScreenTab.ORDERS)
            }
          )
        }

        ScreenTab.HISTORY -> {
          HistoryScreen(
            allOrders = allOrders,
            currentDateFilter = uiState.historyDateFilter,
            currencySymbol = uiState.currencySymbol,
            onDateFilterChange = { orderViewModel.setHistoryDateFilter(it) },
            onPayClick = { orderViewModel.openPaymentSheet(it) },
            onOrderClick = { orderViewModel.openOrderDetail(it) }
          )
        }
      }
    }
  }

  // Payment BottomSheet
  if (uiState.paymentOrder != null) {
    PaymentBottomSheet(
      orderWithItems = uiState.paymentOrder!!,
      currencySymbol = uiState.currencySymbol,
      selectedMethod = uiState.selectedPaymentMethod,
      amountPaidInput = uiState.amountPaidInput,
      mpesaCodeInput = uiState.mpesaCodeInput,
      onMethodSelect = { orderViewModel.setPaymentMethod(it) },
      onAmountPaidChange = { orderViewModel.setAmountPaidInput(it) },
      onMpesaCodeChange = { orderViewModel.setMpesaCodeInput(it) },
      onConfirmPayment = { orderViewModel.confirmPayment() },
      onDismiss = { orderViewModel.closePaymentSheet() },
      sheetState = sheetState
    )
  }

  // Order Detail Dialog (supports direct payment options selection & amount paid recording)
  if (uiState.detailOrder != null) {
    OrderDetailDialog(
      orderWithItems = uiState.detailOrder!!,
      currencySymbol = uiState.currencySymbol,
      onConfirmDirectPayment = { method, paid, change, code ->
        orderViewModel.markOrderAsPaidDirect(
          orderId = uiState.detailOrder!!.order.id,
          paymentMethod = method,
          amountPaid = paid,
          changeAmount = change,
          mpesaCode = code
        )
      },
      onCancelOrder = {
        val order = uiState.detailOrder!!
        orderViewModel.cancelOrder(order.order.id)
      },
      onDismiss = { orderViewModel.closeOrderDetail() }
    )
  }

  // Add Custom Item Dialog
  if (uiState.showAddCustomItemDialog) {
    AddCustomItemDialog(
      currencySymbol = uiState.currencySymbol,
      onAddItem = { name, price, qty ->
        orderViewModel.addCustomItemToCart(name, price, qty)
      },
      onDismiss = { orderViewModel.setShowAddCustomItemDialog(false) }
    )
  }

  // Close Day Confirmation Dialog
  if (uiState.showCloseDayConfirm) {
    CloseDayConfirmDialog(
      summary = todaySummary,
      waiterName = uiState.waiterName,
      currencySymbol = uiState.currencySymbol,
      notes = uiState.closeDayNotes,
      onNotesChange = { orderViewModel.setCloseDayNotes(it) },
      onConfirm = { orderViewModel.confirmCloseDay() },
      onDismiss = { orderViewModel.closeCloseDayConfirm() }
    )
  }

  // Settings Dialog
  if (uiState.showSettingsDialog) {
    SettingsDialog(
      currentWaiterName = uiState.waiterName,
      currentCurrency = uiState.currencySymbol,
      onSave = { name, curr ->
        orderViewModel.setWaiterName(name)
        orderViewModel.setCurrencySymbol(curr)
        orderViewModel.setShowSettingsDialog(false)
      },
      onOpenManageMenu = {
        orderViewModel.setShowManageMenuDialog(true)
      },
      onSignOut = {
        authViewModel.signOut()
      },
      onDismiss = { orderViewModel.setShowSettingsDialog(false) }
    )
  }

  // Manage Menu Catalog Dialog
  if (uiState.showManageMenuDialog) {
    ManageMenuDialog(
      menuItems = menuItems,
      currencySymbol = uiState.currencySymbol,
      onAddNewItem = { orderViewModel.openAddMenuItemDialog() },
      onEditItem = { orderViewModel.openEditMenuItemDialog(it) },
      onDeleteItem = { orderViewModel.deleteMenuItem(it) },
      onDismiss = { orderViewModel.setShowManageMenuDialog(false) }
    )
  }

  // Edit/Add Menu Item Dialog
  if (uiState.showEditItemDialog) {
    EditMenuItemDialog(
      itemToEdit = uiState.itemToEdit,
      currencySymbol = uiState.currencySymbol,
      onSave = { id, name, category, price, emoji ->
        orderViewModel.saveMenuItem(id, name, category, price, emoji)
      },
      onDismiss = { orderViewModel.closeEditMenuItemDialog() }
    )
  }
}
