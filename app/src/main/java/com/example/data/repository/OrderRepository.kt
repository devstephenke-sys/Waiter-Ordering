package com.example.data.repository

import com.example.data.local.dao.DailySettlementDao
import com.example.data.local.dao.ItemSalesCount
import com.example.data.local.dao.MenuItemDao
import com.example.data.local.dao.OrderDao
import com.example.data.local.dao.OrderWithItems
import com.example.data.local.entities.DailySettlementEntity
import com.example.data.local.entities.MenuItemEntity
import com.example.data.local.entities.OrderEntity
import com.example.data.local.entities.OrderItemEntity
import com.example.data.model.CartItem
import com.example.data.model.DailySummary
import com.example.data.model.OrderStatus
import com.example.data.model.PaymentMethod
import com.example.data.remote.FirestoreRepository
import com.google.firebase.Timestamp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class OrderRepository(
  private val menuItemDao: MenuItemDao,
  private val orderDao: OrderDao,
  private val dailySettlementDao: DailySettlementDao,
  private val firestoreRepository: FirestoreRepository? = null
) {

  val allMenuItems: Flow<List<MenuItemEntity>> = menuItemDao.getAllMenuItems()
  val allOrders: Flow<List<OrderWithItems>> = orderDao.getAllOrdersWithItems()
  val allSettlements: Flow<List<DailySettlementEntity>> = dailySettlementDao.getAllSettlements()

  fun getTodayOrders(): Flow<List<OrderWithItems>> {
    val (start, end) = getTodayRange()
    return orderDao.getOrdersWithItemsBetween(start, end)
  }

  fun getTodayTopSelling(): Flow<List<ItemSalesCount>> {
    val (start, end) = getTodayRange()
    return orderDao.getTopSellingItemsBetween(start, end)
  }

  fun getTodaySummary(): Flow<DailySummary> {
    val (start, end) = getTodayRange()
    val todayOrdersFlow = orderDao.getOrdersWithItemsBetween(start, end)
    val topSellingFlow = orderDao.getTopSellingItemsBetween(start, end)

    return combine(todayOrdersFlow, topSellingFlow) { orders, topSelling ->
      var totalSales = 0.0
      var mpesaSales = 0.0
      var cashSales = 0.0
      var unpaidAmount = 0.0
      var completedCount = 0
      var pendingCount = 0

      val activeOrders = orders.filter { it.order.status != OrderStatus.CANCELLED.name }

      for (orderWithItems in activeOrders) {
        val o = orderWithItems.order
        if (o.status == OrderStatus.COMPLETED.name) {
          totalSales += o.totalAmount
          completedCount++
          if (o.paymentMethod == PaymentMethod.MPESA.name) {
            mpesaSales += o.totalAmount
          } else if (o.paymentMethod == PaymentMethod.CASH.name) {
            cashSales += o.totalAmount
          }
        } else if (o.status == OrderStatus.WAITING_FOR_PAYMENT.name) {
          unpaidAmount += o.totalAmount
          pendingCount++
        }
      }

      val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
      val todayDateString = dateFormat.format(Date())

      DailySummary(
        date = todayDateString,
        totalSales = totalSales,
        mpesaSales = mpesaSales,
        cashSales = cashSales,
        unpaidAmount = unpaidAmount,
        totalOrdersCount = activeOrders.size,
        completedOrdersCount = completedCount,
        pendingOrdersCount = pendingCount,
        topSellingItems = topSelling.map { it.itemName to it.totalQuantity }
      )
    }
  }

  suspend fun ensureMenuPopulated() = withContext(Dispatchers.IO) {
    if (menuItemDao.getCount() == 0) {
      val initialItems = listOf(
        MenuItemEntity(name = "Grilled Chicken & Chips", category = "Meals", price = 550.0, emoji = "🍗"),
        MenuItemEntity(name = "Beef Stew & Rice", category = "Meals", price = 380.0, emoji = "🍛"),
        MenuItemEntity(name = "Nyama Choma (1/2 kg)", category = "Meals", price = 650.0, emoji = "🥩"),
        MenuItemEntity(name = "Fried Tilapia & Ugali", category = "Meals", price = 500.0, emoji = "🐟"),
        MenuItemEntity(name = "Pilau Beef", category = "Meals", price = 320.0, emoji = "🍚"),
        MenuItemEntity(name = "Ugali & Sukuma Wiki", category = "Meals", price = 180.0, emoji = "🥬"),
        MenuItemEntity(name = "Chapati (2 pcs)", category = "Sides", price = 80.0, emoji = "🫓"),
        MenuItemEntity(name = "French Fries / Chips", category = "Sides", price = 200.0, emoji = "🍟"),
        MenuItemEntity(name = "Samosa Beef (Pair)", category = "Sides", price = 100.0, emoji = "🥟"),
        MenuItemEntity(name = "Mandazi (Pair)", category = "Sides", price = 60.0, emoji = "🍩"),
        MenuItemEntity(name = "Fresh Passion Juice", category = "Drinks", price = 150.0, emoji = "🍹"),
        MenuItemEntity(name = "Fresh Mango Juice", category = "Drinks", price = 150.0, emoji = "🥭"),
        MenuItemEntity(name = "Soda 500ml", category = "Drinks", price = 100.0, emoji = "🥤"),
        MenuItemEntity(name = "Mineral Water 500ml", category = "Drinks", price = 70.0, emoji = "💧"),
        MenuItemEntity(name = "Dawa Herbal Tea", category = "Hot Drinks", price = 150.0, emoji = "🍵"),
        MenuItemEntity(name = "Masala Spiced Tea", category = "Hot Drinks", price = 120.0, emoji = "☕"),
        MenuItemEntity(name = "House Blend Coffee", category = "Hot Drinks", price = 140.0, emoji = "☕")
      )
      menuItemDao.insertMenuItems(initialItems)
    }
  }

  suspend fun createOrder(
    tableName: String,
    waiterName: String,
    items: List<CartItem>,
    notes: String = "",
    immediatePayment: Boolean = false,
    paymentMethod: PaymentMethod? = null,
    amountPaid: Double = 0.0,
    changeAmount: Double = 0.0,
    mpesaCode: String? = null
  ): Long = withContext(Dispatchers.IO) {
    val totalAmount = items.sumOf { it.subtotal }
    val now = System.currentTimeMillis()
    val totalExisting = orderDao.getTotalOrdersCount()
    val orderNumber = String.format(Locale.getDefault(), "#%04d", totalExisting + 1)

    val isPaid = immediatePayment && paymentMethod != null
    val status = if (isPaid) OrderStatus.COMPLETED.name else OrderStatus.WAITING_FOR_PAYMENT.name

    val order = OrderEntity(
      orderNumber = orderNumber,
      tableName = tableName.ifBlank { "Table 1" },
      waiterName = waiterName.ifBlank { "Waitress" },
      createdAt = now,
      status = status,
      totalAmount = totalAmount,
      paymentMethod = if (isPaid) paymentMethod?.name else null,
      amountPaid = if (isPaid) amountPaid else 0.0,
      changeAmount = if (isPaid) changeAmount else 0.0,
      mpesaCode = if (isPaid) mpesaCode else null,
      paidAt = if (isPaid) now else null,
      notes = notes.ifBlank { null }
    )

    val orderId = orderDao.insertOrder(order)
    val orderItems = items.map {
      OrderItemEntity(
        orderId = orderId,
        itemName = it.name,
        unitPrice = it.unitPrice,
        quantity = it.quantity,
        subtotal = it.subtotal,
        notes = it.notes
      )
    }
    orderDao.insertOrderItems(orderItems)

    // Sync to Firestore if repository is available
    firestoreRepository?.let {
      withContext(Dispatchers.IO) {
        launch {
          val orderMap = mapOf(
            "orderId" to orderId,
            "orderNumber" to orderNumber,
            "tableName" to tableName.ifBlank { "Table 1" },
            "waiterName" to waiterName.ifBlank { "Waitress" },
            "status" to status,
            "totalAmount" to totalAmount,
            "paymentMethod" to if (isPaid) paymentMethod?.name else null,
            "amountPaid" to if (isPaid) amountPaid else 0.0,
            "changeAmount" to if (isPaid) changeAmount else 0.0,
            "mpesaCode" to if (isPaid) mpesaCode else null,
            "createdAt" to Timestamp(now / 1000, (now % 1000).toInt() * 1000000),
            "paidAt" to if (isPaid) Timestamp(now / 1000, (now % 1000).toInt() * 1000000) else null,
            "notes" to notes.ifBlank { null},
            "items" to items.map { item ->
              mapOf(
                "itemName" to item.name,
                "unitPrice" to item.unitPrice,
                "quantity" to item.quantity,
                "subtotal" to item.subtotal,
                "notes" to item.notes
              )
            }
          )
          it.syncOrder(orderMap)
        }
      }
    }

    orderId
  }

  suspend fun markOrderAsPaid(
    orderId: Long,
    paymentMethod: PaymentMethod,
    amountPaid: Double,
    changeAmount: Double = 0.0,
    mpesaCode: String? = null
  ) = withContext(Dispatchers.IO) {
    orderDao.markOrderAsPaid(
      orderId = orderId,
      status = OrderStatus.COMPLETED.name,
      paymentMethod = paymentMethod.name,
      amountPaid = amountPaid,
      changeAmount = changeAmount,
      mpesaCode = mpesaCode,
      paidAt = System.currentTimeMillis()
    )
  }

  suspend fun cancelOrder(orderId: Long) = withContext(Dispatchers.IO) {
    orderDao.cancelOrder(orderId)
  }

  suspend fun deleteOrder(order: OrderEntity) = withContext(Dispatchers.IO) {
    orderDao.deleteOrder(order)
  }

  suspend fun addItemsToOrder(orderId: Long, newItems: List<CartItem>) = withContext(Dispatchers.IO) {
    val existingOrder = orderDao.getOrderByIdDirect(orderId) ?: return@withContext
    val newOrderItems = newItems.map {
      OrderItemEntity(
        orderId = orderId,
        itemName = it.name,
        unitPrice = it.unitPrice,
        quantity = it.quantity,
        subtotal = it.subtotal,
        notes = it.notes
      )
    }
    orderDao.insertOrderItems(newOrderItems)
    val newTotal = existingOrder.totalAmount + newItems.sumOf { it.subtotal }
    orderDao.updateOrder(existingOrder.copy(totalAmount = newTotal))
  }

  suspend fun saveDailySettlement(
    waiterName: String,
    summary: DailySummary,
    notes: String? = null
  ): Long = withContext(Dispatchers.IO) {
    val settlement = DailySettlementEntity(
      dateString = summary.date,
      closedAt = System.currentTimeMillis(),
      waiterName = waiterName,
      totalSales = summary.totalSales,
      mpesaSales = summary.mpesaSales,
      cashSales = summary.cashSales,
      unpaidAmount = summary.unpaidAmount,
      totalOrdersCount = summary.totalOrdersCount,
      completedOrdersCount = summary.completedOrdersCount,
      notes = notes
    )
    dailySettlementDao.insertSettlement(settlement)
  }

  suspend fun addMenuItem(name: String, category: String, price: Double, emoji: String) =
    withContext(Dispatchers.IO) {
      menuItemDao.insertMenuItem(
        MenuItemEntity(
          name = name,
          category = category,
          price = price,
          emoji = emoji
        )
      )
    }

  suspend fun updateMenuItem(item: MenuItemEntity) = withContext(Dispatchers.IO) {
    menuItemDao.updateMenuItem(item)
  }

  suspend fun deleteMenuItem(item: MenuItemEntity) = withContext(Dispatchers.IO) {
    menuItemDao.deleteMenuItem(item)
  }

  companion object {
    fun getTodayRange(): Pair<Long, Long> {
      val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
      }
      val start = calendar.timeInMillis
      calendar.apply {
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
      }
      val end = calendar.timeInMillis
      return Pair(start, end)
    }
  }
}
