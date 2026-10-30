package com.example.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreRepository {

  private val db = FirebaseFirestore.getInstance()

  // Orders
  suspend fun syncOrder(orderData: Map<String, Any>): Result<String> {
    return try {
      val documentRef = db.collection("orders").add(orderData).await()
      Result.success(documentRef.id)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun updateOrder(orderId: String, updates: Map<String, Any>): Result<Unit> {
    return try {
      db.collection("orders").document(orderId).update(updates).await()
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  fun observeOrders(): Flow<List<Map<String, Any>>> = callbackFlow {
    val listener = db.collection("orders")
      .addSnapshotListener { snapshot, error ->
        if (error != null) {
          close(error)
          return@addSnapshotListener
        }
        val orders = snapshot?.documents?.map { it.data } ?: emptyList()
        trySend(orders)
      }

    awaitClose { listener.remove() }
  }

  // Menu Items
  suspend fun syncMenuItem(itemData: Map<String, Any>): Result<String> {
    return try {
      val documentRef = db.collection("menuItems").add(itemData).await()
      Result.success(documentRef.id)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun updateMenuItem(itemId: String, updates: Map<String, Any>): Result<Unit> {
    return try {
      db.collection("menuItems").document(itemId).update(updates).await()
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun deleteMenuItem(itemId: String): Result<Unit> {
    return try {
      db.collection("menuItems").document(itemId).delete().await()
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  fun observeMenuItems(): Flow<List<Map<String, Any>>> = callbackFlow {
    val listener = db.collection("menuItems")
      .addSnapshotListener { snapshot, error ->
        if (error != null) {
          close(error)
          return@addSnapshotListener
        }
        val items = snapshot?.documents?.map { it.data } ?: emptyList()
        trySend(items)
      }

    awaitClose { listener.remove() }
  }

  // Daily Settlements
  suspend fun syncDailySettlement(settlementData: Map<String, Any>): Result<String> {
    return try {
      val documentRef = db.collection("dailySettlements").add(settlementData).await()
      Result.success(documentRef.id)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  fun observeDailySettlements(): Flow<List<Map<String, Any>>> = callbackFlow {
    val listener = db.collection("dailySettlements")
      .addSnapshotListener { snapshot, error ->
        if (error != null) {
          close(error)
          return@addSnapshotListener
        }
        val settlements = snapshot?.documents?.map { it.data } ?: emptyList()
        trySend(settlements)
      }

    awaitClose { listener.remove() }
  }
}
