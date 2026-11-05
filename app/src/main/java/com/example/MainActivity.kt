package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.ui.unit.dp
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
import com.example.ui.screens.NewOrderScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.OrdersScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.Terracotta
import com.example.ui.viewmodel.OrderViewModel
import com.example.ui.viewmodel.ScreenTab
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    try {
      com.google.firebase.FirebaseApp.initializeApp(this)
    } catch (_: Exception) {}
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        val orderViewModel: OrderViewModel = viewModel()
        val uiState by orderViewModel.uiState.collectAsStateWithLifecycle()

        if (!uiState.isFirstTimeSetupComplete) {
          OnboardingScreen(
            onComplete = { waiter, hotel, currency ->
              orderViewModel.completeFirstTimeSetup(waiter, hotel, currency)
            }
          )
        } else {
          WaiterApp(orderViewModel = orderViewModel)
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaiterApp(
  orderViewModel: OrderViewModel = viewModel()
) {
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
          val isManager = uiState.waiterName.equals("Manager", ignoreCase = true)
          val displayedOrders = if (isManager) allOrders else allOrders.filter { it.order.waiterName.equals(uiState.waiterName, ignoreCase = true) }
          OrdersScreen(
            orders = displayedOrders,
            currentFilter = uiState.ordersFilter,
            waiterName = uiState.waiterName,
            hotelName = uiState.hotelName,
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
            orderNotes = uiState.newOrderNotes,
            selectedCategory = uiState.selectedMenuCategory,
            searchQuery = uiState.menuSearchQuery,
            currencySymbol = uiState.currencySymbol,
            showOrderConfirmDialog = uiState.showOrderConfirmDialog,
            onPromptSubmit = { orderViewModel.promptSubmitOrder(it) },
            onConfirmSubmit = { orderViewModel.confirmAndSubmitOrder() },
            onDismissConfirm = { orderViewModel.dismissOrderConfirmDialog() },
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
          val isManager = uiState.waiterName.equals("Manager", ignoreCase = true)
          if (!isManager) {
            Box(
              modifier = Modifier.fillMaxSize().padding(24.dp),
              contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
              Column(
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
              ) {
                Text(
                  text = "🔒 Manager Restricted Area",
                  style = androidx.compose.material3.MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                  color = androidx.compose.material3.MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                  text = "Only the Manager can close the day and view daily settlements.\nYou are currently logged in as ${uiState.waiterName}.",
                  style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                  textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                  color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          } else {
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
        }

        ScreenTab.HISTORY -> {
          val isManager = uiState.waiterName.equals("Manager", ignoreCase = true)
          val displayedHistory = if (isManager) allOrders else allOrders.filter { it.order.waiterName.equals(uiState.waiterName, ignoreCase = true) }
          HistoryScreen(
            allOrders = displayedHistory,
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

  // Order Detail Dialog
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
      onEditOrder = { orderViewModel.editOrder(it) },
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
      onSave = { id, name, category, price, emoji, imageUri ->
        orderViewModel.saveMenuItem(id, name, category, price, emoji, imageUri)
      },
      onDismiss = { orderViewModel.closeEditMenuItemDialog() }
    )
  }
}
