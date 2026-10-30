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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.dao.OrderWithItems
import com.example.data.model.PaymentMethod
import com.example.ui.theme.CashAmber
import com.example.ui.theme.MpesaGreen
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentBottomSheet(
  orderWithItems: OrderWithItems,
  currencySymbol: String,
  selectedMethod: PaymentMethod,
  amountPaidInput: String,
  mpesaCodeInput: String,
  onMethodSelect: (PaymentMethod) -> Unit,
  onAmountPaidChange: (String) -> Unit,
  onMpesaCodeChange: (String) -> Unit,
  onConfirmPayment: () -> Unit,
  onDismiss: () -> Unit,
  sheetState: SheetState,
  modifier: Modifier = Modifier
) {
  val order = orderWithItems.order
  val total = order.totalAmount
  val enteredAmount = amountPaidInput.toDoubleOrNull() ?: total
  val changeAmount = (enteredAmount - total).coerceAtLeast(0.0)

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.surface,
    modifier = modifier.testTag("payment_bottom_sheet")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 24.dp)
        .padding(bottom = 32.dp)
    ) {
      // Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Record Payment",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "${order.tableName} • ${order.orderNumber}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
        IconButton(
          onClick = onDismiss,
          modifier = Modifier.testTag("close_payment_button")
        ) {
          Icon(Icons.Default.Close, contentDescription = "Close")
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Bill Amount Box
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        )
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(
            text = "TOTAL BILL DUE",
            style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.2.sp),
            color = MaterialTheme.colorScheme.onPrimaryContainer
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "$currencySymbol ${String.format(Locale.US, "%,.2f", total)}",
            style = MaterialTheme.typography.headlineMedium.copy(
              fontWeight = FontWeight.ExtraBold,
              fontSize = 32.sp
            ),
            color = MaterialTheme.colorScheme.primary
          )
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Payment Method Selection Label
      Text(
        text = "Select Payment Method",
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.onSurface
      )

      Spacer(modifier = Modifier.height(10.dp))

      // Two payment method toggle cards
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // M-PESA Option Card
        PaymentMethodCard(
          title = "M-Pesa",
          subtitle = "Mobile Money",
          icon = Icons.Default.PhoneAndroid,
          isSelected = selectedMethod == PaymentMethod.MPESA,
          selectedColor = MpesaGreen,
          onClick = { onMethodSelect(PaymentMethod.MPESA) },
          modifier = Modifier
            .weight(1f)
            .testTag("mpesa_option_button")
        )

        // CASH Option Card
        PaymentMethodCard(
          title = "Cash",
          subtitle = "Physical Currency",
          icon = Icons.Default.MonetizationOn,
          isSelected = selectedMethod == PaymentMethod.CASH,
          selectedColor = CashAmber,
          onClick = { onMethodSelect(PaymentMethod.CASH) },
          modifier = Modifier
            .weight(1f)
            .testTag("cash_option_button")
        )
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Method-specific Inputs
      if (selectedMethod == PaymentMethod.MPESA) {
        // M-Pesa Details
        OutlinedTextField(
          value = mpesaCodeInput,
          onValueChange = onMpesaCodeChange,
          label = { Text("M-Pesa Confirmation / Trans Code (Optional)") },
          placeholder = { Text("e.g. QHX9821K7Z") },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("mpesa_code_input"),
          singleLine = true,
          shape = RoundedCornerShape(12.dp),
          leadingIcon = {
            Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = MpesaGreen)
          }
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = "Customer will pay $currencySymbol ${String.format(Locale.US, "%,.2f", total)} via M-Pesa till / paybill.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      } else {
        // Cash Tendered & Quick Amount Chips
        OutlinedTextField(
          value = amountPaidInput,
          onValueChange = onAmountPaidChange,
          label = { Text("Cash Received ($currencySymbol)") },
          placeholder = { Text(String.format(Locale.US, "%.0f", total)) },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("cash_amount_input"),
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          singleLine = true,
          shape = RoundedCornerShape(12.dp),
          leadingIcon = {
            Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = CashAmber)
          }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Quick cash suggestion chips
        val exactAmount = total.toInt()
        val quickSuggestions = listOf(
          "Exact ($currencySymbol $exactAmount)" to exactAmount.toDouble(),
          "+50" to (total + 50),
          "+100" to (total + 100),
          "+500" to (total + 500),
          "1,000" to 1000.0,
          "2,000" to 2000.0
        ).filter { it.second >= total }

        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          items(quickSuggestions) { (label, value) ->
            FilterChip(
              selected = amountPaidInput == String.format(Locale.US, "%.0f", value),
              onClick = { onAmountPaidChange(String.format(Locale.US, "%.0f", value)) },
              label = { Text(label, fontSize = 12.sp) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = CashAmber.copy(alpha = 0.2f),
                selectedLabelColor = CashAmber
              ),
              shape = RoundedCornerShape(20.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Change Calculation Box
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(
            containerColor = if (changeAmount > 0) CashAmber.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant
          )
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "Change to Return",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = "$currencySymbol ${String.format(Locale.US, "%,.2f", changeAmount)}",
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Bold,
                  fontSize = 18.sp
                ),
                color = if (changeAmount > 0) CashAmber else MaterialTheme.colorScheme.onSurface
              )
            }
            if (enteredAmount >= total) {
              Box(
                modifier = Modifier
                  .size(32.dp)
                  .clip(CircleShape)
                  .background(MpesaGreen.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  Icons.Default.Check,
                  contentDescription = null,
                  tint = MpesaGreen,
                  modifier = Modifier.size(18.dp)
                )
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Confirm Button
      Button(
        onClick = onConfirmPayment,
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .testTag("confirm_payment_button"),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = if (selectedMethod == PaymentMethod.MPESA) MpesaGreen else MaterialTheme.colorScheme.primary
        ),
        enabled = enteredAmount >= total
      ) {
        Icon(Icons.Default.Check, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Confirm ${selectedMethod.displayName} & Complete Order",
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
          )
        )
      }
    }
  }
}

@Composable
fun PaymentMethodCard(
  title: String,
  subtitle: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  isSelected: Boolean,
  selectedColor: Color,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val borderColor = if (isSelected) selectedColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
  val bgColor = if (isSelected) selectedColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface

  Card(
    modifier = modifier
      .clip(RoundedCornerShape(14.dp))
      .border(
        width = if (isSelected) 2.dp else 1.dp,
        color = borderColor,
        shape = RoundedCornerShape(14.dp)
      )
      .clickable { onClick() },
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = bgColor)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Box(
        modifier = Modifier
          .size(44.dp)
          .clip(CircleShape)
          .background(if (isSelected) selectedColor else MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = title,
          tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(24.dp)
        )
      }
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = if (isSelected) selectedColor else MaterialTheme.colorScheme.onSurface
      )
      Text(
        text = subtitle,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}
