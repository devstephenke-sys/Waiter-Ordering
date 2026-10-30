package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.DailySettlementDao
import com.example.data.local.dao.MenuItemDao
import com.example.data.local.dao.OrderDao
import com.example.data.local.entities.DailySettlementEntity
import com.example.data.local.entities.MenuItemEntity
import com.example.data.local.entities.OrderEntity
import com.example.data.local.entities.OrderItemEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
  entities = [
    MenuItemEntity::class,
    OrderEntity::class,
    OrderItemEntity::class,
    DailySettlementEntity::class
  ],
  version = 1,
  exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
  abstract fun menuItemDao(): MenuItemDao
  abstract fun orderDao(): OrderDao
  abstract fun dailySettlementDao(): DailySettlementDao

  companion object {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AppDatabase::class.java,
          "waiter_orders_db"
        )
          .addCallback(DatabaseCallback(scope))
          .fallbackToDestructiveMigration()
          .build()
        INSTANCE = instance
        instance
      }
    }

    private class DatabaseCallback(
      private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
      override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        INSTANCE?.let { database ->
          scope.launch(Dispatchers.IO) {
            populateInitialMenu(database.menuItemDao())
          }
        }
      }

      suspend fun populateInitialMenu(menuItemDao: MenuItemDao) {
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
  }
}
