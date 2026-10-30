package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.dao.OrderWithItems
import com.example.data.model.OrderStatus
import com.example.data.model.PaymentMethod
import com.example.ui.theme.CashAmber
import com.example.ui.theme.MpesaGreen
import com.example.ui.theme.Terracotta
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun OrderCard(
  orderWithItems: OrderWithItems,
  currencySymbol: String,
  onPayClick: () -> Unit,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val order = orderWithItems.order
  val isWaitingPayment = order.status == OrderStatus.WAITING_FOR_PAYMENT.name
  val isCompleted = order.status == OrderStatus.COMPLETED.name
  val isCancelled = order.status == OrderStatus.CANCELLED.name

  val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
  val timeString = timeFormat.format(Date(order.createdAt))

  val itemsSummary = orderWithItems.items.joinToString(separator = ", ") {
    "${it.quantity}x ${it.itemName}"
  }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .clickable { onClick() }
      .testTag("order_card_${order.id}"),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      // Header row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .background(
                when {
                  isWaitingPayment -> Terracotta.copy(alpha = 0.15f)
                  isCompleted -> MpesaGreen.copy(alpha = 0.15f)
                  else -> Color.Gray.copy(alpha = 0.15f)
                }
              ),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = when {
                isWaitingPayment -> Icons.Default.AccessTime
                isCompleted -> Icons.Default.CheckCircle
                else -> Icons.Default.Close
              },
              contentDescription = null,
              tint = when {
                isWaitingPayment -> Terracotta
                isCompleted -> MpesaGreen
                else -> Color.Gray
              },
              modifier = Modifier.size(20.dp)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = order.tableName,
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
              ),
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "${order.orderNumber} • $timeString",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        // Status badge
        StatusBadge(order = order)
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Items list summary
      Text(
        text = if (itemsSummary.isNotBlank()) itemsSummary else "No items listed",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
      )

      if (!order.notes.isNullOrBlank()) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "Note: ${order.notes}",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Bottom Row: Total & Action
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Total Amount",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = "$currencySymbol ${String.format(Locale.US, "%,.2f", order.totalAmount)}",
            style = MaterialTheme.typography.titleLarge.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 20.sp
            ),
            color = if (isWaitingPayment) Terracotta else MaterialTheme.colorScheme.primary
          )
        }

        if (isWaitingPayment) {
          Button(
            onClick = onPayClick,
            colors = ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.testTag("pay_button_${order.id}")
          ) {
            Icon(
              imageVector = Icons.Default.Payment,
              contentDescription = null,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = "Pay / Settle", fontWeight = FontWeight.Bold)
          }
        } else {
          OutlinedButton(
            onClick = onClick,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.testTag("details_button_${order.id}")
          ) {
            Text(text = "Details", fontWeight = FontWeight.Medium)
          }
        }
      }
    }
  }
}

@Composable
fun StatusBadge(order: com.example.data.local.entities.OrderEntity) {
  val isWaiting = order.status == OrderStatus.WAITING_FOR_PAYMENT.name
  val isCompleted = order.status == OrderStatus.COMPLETED.name
  val isCancelled = order.status == OrderStatus.CANCELLED.name

  val bgColor: Color
  val contentColor: Color
  val text: String

  when {
    isWaiting -> {
      bgColor = Terracotta.copy(alpha = 0.15f)
      contentColor = Terracotta
      text = "Waiting for Payment"
    }
    isCompleted -> {
      val isMpesa = order.paymentMethod == PaymentMethod.MPESA.name
      bgColor = if (isMpesa) MpesaGreen.copy(alpha = 0.15f) else CashAmber.copy(alpha = 0.15f)
      contentColor = if (isMpesa) MpesaGreen else CashAmber
      text = if (isMpesa) "Paid (M-Pesa)" else if (order.paymentMethod == PaymentMethod.CASH.name) "Paid (Cash)" else "Paid"
    }
    else -> {
      bgColor = Color.Gray.copy(alpha = 0.15f)
      contentColor = Color.Gray
      text = "Cancelled"
    }
  }

  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(8.dp))
      .background(bgColor)
      .border(1.dp, contentColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
      .padding(horizontal = 10.dp, vertical = 5.dp)
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      if (isCompleted) {
        if (order.paymentMethod == PaymentMethod.MPESA.name) {
          Icon(
            imageVector = Icons.Default.PhoneAndroid,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
        } else if (order.paymentMethod == PaymentMethod.CASH.name) {
          Icon(
            imageVector = Icons.Default.MonetizationOn,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
        }
      }
      Text(
        text = text,
        style = MaterialTheme.typography.labelSmall.copy(
          fontWeight = FontWeight.Bold,
          fontSize = 11.sp
        ),
        color = contentColor
      )
    }
  }
}
