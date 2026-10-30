package com.example

import com.example.data.model.CartItem
import com.example.data.model.OrderStatus
import com.example.data.model.PaymentMethod
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun cartItem_subtotalCalculatesCorrectly() {
    val item = CartItem(
      name = "Grilled Chicken & Chips",
      unitPrice = 550.0,
      quantity = 3
    )
    assertEquals(1650.0, item.subtotal, 0.01)
  }

  @Test
  fun cashPayment_changeCalculatedCorrectly() {
    val totalBill = 750.0
    val cashReceived = 1000.0
    val change = (cashReceived - totalBill).coerceAtLeast(0.0)
    assertEquals(250.0, change, 0.01)
  }

  @Test
  fun paymentMethods_haveCorrectNames() {
    assertEquals("M-Pesa", PaymentMethod.MPESA.displayName)
    assertEquals("Cash", PaymentMethod.CASH.displayName)
    assertEquals("WAITING_FOR_PAYMENT", OrderStatus.WAITING_FOR_PAYMENT.name)
    assertEquals("COMPLETED", OrderStatus.COMPLETED.name)
  }
}
