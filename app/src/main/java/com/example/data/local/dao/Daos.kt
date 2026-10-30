package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.local.entities.DailySettlementEntity
import com.example.data.local.entities.MenuItemEntity
import com.example.data.local.entities.OrderEntity
import com.example.data.local.entities.OrderItemEntity
import kotlinx.coroutines.flow.Flow

data class OrderWithItems(
  @Embedded val order: OrderEntity,
  @Relation(
    parentColumn = "id",
    entityColumn = "orderId"
  )
  val items: List<OrderItemEntity>
)

data class ItemSalesCount(
  val itemName: String,
  val totalQuantity: Int
)

@Dao
interface MenuItemDao {
  @Query("SELECT * FROM menu_items ORDER BY category ASC, name ASC")
  fun getAllMenuItems(): Flow<List<MenuItemEntity>>

  @Query("SELECT COUNT(*) FROM menu_items")
  suspend fun getCount(): Int

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMenuItems(items: List<MenuItemEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMenuItem(item: MenuItemEntity): Long

  @Update
  suspend fun updateMenuItem(item: MenuItemEntity)

  @Delete
  suspend fun deleteMenuItem(item: MenuItemEntity)
}

@Dao
interface OrderDao {
  @Transaction
  @Query("SELECT * FROM orders ORDER BY createdAt DESC")
  fun getAllOrdersWithItems(): Flow<List<OrderWithItems>>

  @Transaction
  @Query("SELECT * FROM orders WHERE createdAt >= :startTime AND createdAt <= :endTime ORDER BY createdAt DESC")
  fun getOrdersWithItemsBetween(startTime: Long, endTime: Long): Flow<List<OrderWithItems>>

  @Transaction
  @Query("SELECT * FROM orders WHERE id = :orderId")
  fun getOrderWithItemsById(orderId: Long): Flow<OrderWithItems?>

  @Query("SELECT * FROM orders WHERE id = :orderId")
  suspend fun getOrderByIdDirect(orderId: Long): OrderEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrder(order: OrderEntity): Long

  @Update
  suspend fun updateOrder(order: OrderEntity)

  @Query("UPDATE orders SET status = :status, paymentMethod = :paymentMethod, amountPaid = :amountPaid, changeAmount = :changeAmount, mpesaCode = :mpesaCode, paidAt = :paidAt WHERE id = :orderId")
  suspend fun markOrderAsPaid(
    orderId: Long,
    status: String,
    paymentMethod: String,
    amountPaid: Double,
    changeAmount: Double,
    mpesaCode: String?,
    paidAt: Long
  )

  @Query("UPDATE orders SET status = 'CANCELLED' WHERE id = :orderId")
  suspend fun cancelOrder(orderId: Long)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrderItems(items: List<OrderItemEntity>)

  @Query("DELETE FROM order_items WHERE orderId = :orderId")
  suspend fun deleteOrderItems(orderId: Long)

  @Delete
  suspend fun deleteOrder(order: OrderEntity)

  @Query("SELECT oi.itemName, SUM(oi.quantity) as totalQuantity FROM order_items oi INNER JOIN orders o ON oi.orderId = o.id WHERE o.createdAt >= :startTime AND o.createdAt <= :endTime AND o.status != 'CANCELLED' GROUP BY oi.itemName ORDER BY totalQuantity DESC LIMIT 10")
  fun getTopSellingItemsBetween(startTime: Long, endTime: Long): Flow<List<ItemSalesCount>>

  @Query("SELECT COUNT(*) FROM orders")
  suspend fun getTotalOrdersCount(): Int
}

@Dao
interface DailySettlementDao {
  @Query("SELECT * FROM daily_settlements ORDER BY closedAt DESC")
  fun getAllSettlements(): Flow<List<DailySettlementEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertSettlement(settlement: DailySettlementEntity): Long

  @Query("SELECT * FROM daily_settlements WHERE dateString = :dateString LIMIT 1")
  suspend fun getSettlementByDate(dateString: String): DailySettlementEntity?
}
