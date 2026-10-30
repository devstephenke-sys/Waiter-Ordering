package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.model.OrderStatus
import com.example.data.model.PaymentMethod

@Entity(tableName = "menu_items")
data class MenuItemEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val name: String,
  val category: String,
  val price: Double,
  val isAvailable: Boolean = true,
  val emoji: String = "🍽️"
)

@Entity(
  tableName = "orders",
  indices = [Index(value = ["createdAt"]), Index(value = ["status"])]
)
data class OrderEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val orderNumber: String,
  val tableName: String,
  val waiterName: String,
  val createdAt: Long = System.currentTimeMillis(),
  val status: String = OrderStatus.WAITING_FOR_PAYMENT.name,
  val totalAmount: Double,
  val paymentMethod: String? = null,
  val amountPaid: Double = 0.0,
  val changeAmount: Double = 0.0,
  val mpesaCode: String? = null,
  val paidAt: Long? = null,
  val notes: String? = null,
  val isClosedInSettlement: Boolean = false,
  val settlementDate: String? = null
)

@Entity(
  tableName = "order_items",
  foreignKeys = [
    ForeignKey(
      entity = OrderEntity::class,
      parentColumns = ["id"],
      childColumns = ["orderId"],
      onDelete = ForeignKey.CASCADE
    )
  ],
  indices = [Index(value = ["orderId"])]
)
data class OrderItemEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val orderId: Long,
  val itemName: String,
  val unitPrice: Double,
  val quantity: Int,
  val subtotal: Double,
  val notes: String = ""
)

@Entity(
  tableName = "daily_settlements",
  indices = [Index(value = ["dateString"])]
)
data class DailySettlementEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val dateString: String,
  val closedAt: Long = System.currentTimeMillis(),
  val waiterName: String,
  val totalSales: Double,
  val mpesaSales: Double,
  val cashSales: Double,
  val unpaidAmount: Double,
  val totalOrdersCount: Int,
  val completedOrdersCount: Int,
  val notes: String? = null
)
