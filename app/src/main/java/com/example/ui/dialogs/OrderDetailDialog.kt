package com.example.ui.dialogs

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.dao.OrderWithItems
import com.example.data.model.OrderStatus
import com.example.data.model.PaymentMethod
import com.example.ui.components.StatusBadge
import com.example.ui.theme.CashAmber
import com.example.ui.theme.MpesaGreen
import com.example.ui.theme.Terracotta
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun OrderDetailDialog(
  orderWithItems: OrderWithItems,
  currencySymbol: String,
  onConfirmDirectPayment: (paymentMethod: PaymentMethod, amountPaid: Double, changeAmount: Double, mpesaCode: String?) -> Unit,
  onCancelOrder: () -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val order = orderWithItems.order
  val isWaiting = order.status == OrderStatus.WAITING_FOR_PAYMENT.name
  val isCompleted = order.status == OrderStatus.COMPLETED.name

  val dateFormat = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())
  val createdTime = dateFormat.format(Date(order.createdAt))

  // Payment section state
  var selectedMethod by remember { mutableStateOf(PaymentMethod.MPESA) }
  var amountPaidInput by remember {
    mutableStateOf(String.format(Locale.US, "%.0f", order.totalAmount))
  }
  var mpesaCodeInput by remember { mutableStateOf("") }

  val total = order.totalAmount
  val enteredAmount = amountPaidInput.toDoubleOrNull() ?: total
  val changeAmount = (enteredAmount - total).coerceAtLeast(0.0)

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      shape = RoundedCornerShape(24.dp),
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 6.dp,
      modifier = modifier
        .fillMaxWidth(0.95f)
        .fillMaxHeight(0.9f)
        .testTag("order_detail_dialog")
    ) {
      LazyColumn(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // Top row
        item {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = order.tableName,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "Order ${order.orderNumber} • Waiter: ${order.waiterName}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            IconButton(onClick = onDismiss) {
              Icon(Icons.Default.Close, contentDescription = "Close")
            }
          }
        }

        // Status & Date row
        item {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = createdTime,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            StatusBadge(order = order)
          }
        }

        item {
          HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        }

        // Ordered Items List
        item {
          Text(
            text = "ITEMS ORDERED (${orderWithItems.items.size})",
            style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.sp, fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
          )
        }

        items(orderWithItems.items) { item ->
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
              Text(
                text = "${item.quantity}x",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.width(32.dp)
              )
              Text(
                text = item.itemName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
              )
            }
            Text(
              text = "$currencySymbol ${String.format(Locale.US, "%,.2f", item.subtotal)}",
              style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
              color = MaterialTheme.colorScheme.onSurface
            )
          }
        }

        item {
          HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        }

        // Total Bill
        item {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Total Bill",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "$currencySymbol ${String.format(Locale.US, "%,.2f", order.totalAmount)}",
              style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 24.sp
              ),
              color = if (isWaiting) Terracotta else MaterialTheme.colorScheme.primary
            )
          }
        }

        // Payment Details Section
        if (isCompleted) {
          // Already Paid Section
          item {
            Card(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(12.dp),
              colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
              )
            ) {
              Column(modifier = Modifier.padding(14.dp)) {
                Text(
                  text = "Payment Information",
                  fontWeight = FontWeight.Bold,
                  style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text("Payment Method:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                  Text(
                    text = if (order.paymentMethod == PaymentMethod.MPESA.name) "M-Pesa" else "Cash",
                    fontWeight = FontWeight.Bold,
                    color = if (order.paymentMethod == PaymentMethod.MPESA.name) MpesaGreen else CashAmber
                  )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text("Amount Paid:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                  Text(
                    text = "$currencySymbol ${String.format(Locale.US, "%,.2f", order.amountPaid)}",
                    fontWeight = FontWeight.SemiBold
                  )
                }
                if (order.changeAmount > 0) {
                  Spacer(modifier = Modifier.height(4.dp))
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Text("Change Given:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                      text = "$currencySymbol ${String.format(Locale.US, "%,.2f", order.changeAmount)}",
                      fontWeight = FontWeight.SemiBold,
                      color = CashAmber
                    )
                  }
                }
                if (!order.mpesaCode.isNullOrBlank()) {
                  Spacer(modifier = Modifier.height(4.dp))
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Text("M-Pesa Reference:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = order.mpesaCode, fontWeight = FontWeight.SemiBold)
                  }
                }
                if (order.paidAt != null) {
                  Spacer(modifier = Modifier.height(4.dp))
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Text("Paid At:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                      text = dateFormat.format(Date(order.paidAt)),
                      style = MaterialTheme.typography.bodySmall
                    )
                  }
                }
              }
            }
          }

          // Share receipt button
          item {
            OutlinedButton(
              onClick = {
                val shareText = buildString {
                  appendLine("🍽️ RECEIPT: ${order.tableName}")
                  appendLine("Order: ${order.orderNumber} • Waiter: ${order.waiterName}")
                  appendLine("--------------------------")
                  orderWithItems.items.forEach {
                    appendLine("${it.quantity}x ${it.itemName} - $currencySymbol ${String.format(Locale.US, "%.2f", it.subtotal)}")
                  }
                  appendLine("--------------------------")
                  appendLine("TOTAL: $currencySymbol ${String.format(Locale.US, "%.2f", order.totalAmount)}")
                  appendLine("Payment: ${order.paymentMethod ?: "PAID"}")
                  if (!order.mpesaCode.isNullOrBlank()) appendLine("Ref: ${order.mpesaCode}")
                  appendLine("Thank you for dining with us!")
                }
                val sendIntent = Intent().apply {
                  action = Intent.ACTION_SEND
                  putExtra(Intent.EXTRA_TEXT, shareText)
                  type = "text/plain"
                }
                context.startActivity(Intent.createChooser(sendIntent, "Share Receipt"))
              },
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(12.dp)
            ) {
              Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text("Share Receipt")
            }
          }
        } else if (isWaiting) {
          // Direct Payment Options Section in Order Details
          item {
            Card(
              modifier = Modifier
                .fillMaxWidth()
                .testTag("detail_payment_section"),
              shape = RoundedCornerShape(16.dp),
              colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
              ),
              border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
            ) {
              Column(modifier = Modifier.padding(16.dp)) {
                Text(
                  text = "MARK ORDER AS PAID",
                  style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.sp, fontWeight = FontWeight.Bold),
                  color = MaterialTheme.colorScheme.primary
                )
                Text(
                  text = "Select payment method and record amount received",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Payment Method Selector (M-Pesa or Cash)
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                  // M-Pesa Option
                  Card(
                    modifier = Modifier
                      .weight(1f)
                      .clip(RoundedCornerShape(12.dp))
                      .border(
                        width = if (selectedMethod == PaymentMethod.MPESA) 2.dp else 1.dp,
                        color = if (selectedMethod == PaymentMethod.MPESA) MpesaGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(12.dp)
                      )
                      .clickable { selectedMethod = PaymentMethod.MPESA }
                      .testTag("detail_select_mpesa"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                      containerColor = if (selectedMethod == PaymentMethod.MPESA) MpesaGreen.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
                    )
                  ) {
                    Row(
                      modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.Center
                    ) {
                      Icon(
                        Icons.Default.PhoneAndroid,
                        contentDescription = null,
                        tint = if (selectedMethod == PaymentMethod.MPESA) MpesaGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                      )
                      Spacer(modifier = Modifier.width(6.dp))
                      Text(
                        text = "M-Pesa",
                        fontWeight = FontWeight.Bold,
                        color = if (selectedMethod == PaymentMethod.MPESA) MpesaGreen else MaterialTheme.colorScheme.onSurface
                      )
                    }
                  }

                  // Cash Option
                  Card(
                    modifier = Modifier
                      .weight(1f)
                      .clip(RoundedCornerShape(12.dp))
                      .border(
                        width = if (selectedMethod == PaymentMethod.CASH) 2.dp else 1.dp,
                        color = if (selectedMethod == PaymentMethod.CASH) CashAmber else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(12.dp)
                      )
                      .clickable { selectedMethod = PaymentMethod.CASH }
                      .testTag("detail_select_cash"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                      containerColor = if (selectedMethod == PaymentMethod.CASH) CashAmber.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
                    )
                  ) {
                    Row(
                      modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.Center
                    ) {
                      Icon(
                        Icons.Default.MonetizationOn,
                        contentDescription = null,
                        tint = if (selectedMethod == PaymentMethod.CASH) CashAmber else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                      )
                      Spacer(modifier = Modifier.width(6.dp))
                      Text(
                        text = "Cash",
                        fontWeight = FontWeight.Bold,
                        color = if (selectedMethod == PaymentMethod.CASH) CashAmber else MaterialTheme.colorScheme.onSurface
                      )
                    }
                  }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Amount Paid field
                OutlinedTextField(
                  value = amountPaidInput,
                  onValueChange = { amountPaidInput = it },
                  label = { Text("Amount Paid ($currencySymbol)") },
                  placeholder = { Text(String.format(Locale.US, "%.0f", total)) },
                  modifier = Modifier
                    .fillMaxWidth()
                    .testTag("detail_amount_paid_input"),
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                  singleLine = true,
                  shape = RoundedCornerShape(10.dp)
                )

                if (selectedMethod == PaymentMethod.MPESA) {
                  Spacer(modifier = Modifier.height(8.dp))
                  OutlinedTextField(
                    value = mpesaCodeInput,
                    onValueChange = { mpesaCodeInput = it },
                    label = { Text("M-Pesa Reference Code (Optional)") },
                    placeholder = { Text("e.g. QHX9821K7Z") },
                    modifier = Modifier
                      .fillMaxWidth()
                      .testTag("detail_mpesa_code_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                  )
                } else {
                  // Cash quick chips & change display
                  Spacer(modifier = Modifier.height(8.dp))
                  val exact = total.toInt()
                  val quickChips = listOf(
                    "Exact" to exact.toDouble(),
                    "+50" to (total + 50),
                    "+100" to (total + 100),
                    "+500" to (total + 500)
                  )

                  LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                  ) {
                    items(quickChips) { (lbl, amt) ->
                      FilterChip(
                        selected = amountPaidInput == String.format(Locale.US, "%.0f", amt),
                        onClick = { amountPaidInput = String.format(Locale.US, "%.0f", amt) },
                        label = { Text(lbl, fontSize = 11.sp) },
                        shape = RoundedCornerShape(16.dp)
                      )
                    }
                  }

                  if (changeAmount > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                      text = "Change to Return: $currencySymbol ${String.format(Locale.US, "%,.2f", changeAmount)}",
                      fontWeight = FontWeight.Bold,
                      color = CashAmber,
                      style = MaterialTheme.typography.bodyMedium
                    )
                  }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Confirm and Complete Button
                Button(
                  onClick = {
                    onConfirmDirectPayment(
                      selectedMethod,
                      enteredAmount,
                      changeAmount,
                      if (selectedMethod == PaymentMethod.MPESA) mpesaCodeInput.trim().ifBlank { null } else null
                    )
                  },
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("detail_confirm_payment_btn"),
                  colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedMethod == PaymentMethod.MPESA) MpesaGreen else MaterialTheme.colorScheme.primary
                  ),
                  shape = RoundedCornerShape(12.dp),
                  enabled = enteredAmount >= total
                ) {
                  Icon(Icons.Default.Check, contentDescription = null)
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = "Mark Paid via ${selectedMethod.displayName}",
                    fontWeight = FontWeight.Bold
                  )
                }
              }
            }
          }

          // Cancel Order Button
          item {
            OutlinedButton(
              onClick = onCancelOrder,
              modifier = Modifier.fillMaxWidth(),
              colors = ButtonDefaults.outlinedButtonColors(contentColor = Terracotta),
              shape = RoundedCornerShape(12.dp)
            ) {
              Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Cancel Order", fontSize = 13.sp)
            }
          }
        }
      }
    }
  }
}
