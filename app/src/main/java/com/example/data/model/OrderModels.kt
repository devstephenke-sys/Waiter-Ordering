package com.example.data.model

enum class OrderStatus {
  WAITING_FOR_PAYMENT,
  COMPLETED,
  CANCELLED
}

enum class PaymentMethod(val displayName: String) {
  MPESA("M-Pesa"),
  CASH("Cash")
}

data class CartItem(
  val menuItemId: Long? = null,
  val name: String,
  val unitPrice: Double,
  val quantity: Int = 1,
  val notes: String = ""
) {
  val subtotal: Double
    get() = unitPrice * quantity
}

data class DailySummary(
  val date: String,
  val totalSales: Double,
  val mpesaSales: Double,
  val cashSales: Double,
  val unpaidAmount: Double,
  val totalOrdersCount: Int,
  val completedOrdersCount: Int,
  val pendingOrdersCount: Int,
  val topSellingItems: List<Pair<String, Int>> = emptyList()
)
