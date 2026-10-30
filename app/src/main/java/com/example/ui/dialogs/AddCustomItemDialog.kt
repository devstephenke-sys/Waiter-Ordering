package com.example.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun AddCustomItemDialog(
  currencySymbol: String,
  onAddItem: (name: String, price: Double, quantity: Int) -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier
) {
  var itemName by remember { mutableStateOf("") }
  var itemPrice by remember { mutableStateOf("") }
  var quantity by remember { mutableIntStateOf(1) }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(20.dp),
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 6.dp,
      modifier = modifier
        .fillMaxWidth()
        .testTag("add_custom_item_dialog")
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(24.dp)
      ) {
        Text(
          text = "Add Custom / Off-Menu Item",
          style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = "Quickly add any dish, special, or custom request",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
          value = itemName,
          onValueChange = {
            itemName = it
            errorMessage = null
          },
          label = { Text("Item Name") },
          placeholder = { Text("e.g. Special Grilled Fish") },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("custom_item_name_input"),
          singleLine = true,
          shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
          value = itemPrice,
          onValueChange = {
            itemPrice = it
            errorMessage = null
          },
          label = { Text("Price per item ($currencySymbol)") },
          placeholder = { Text("e.g. 450") },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("custom_item_price_input"),
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          singleLine = true,
          shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Quantity selector
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Quantity",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface
          )
          Row(verticalAlignment = Alignment.CenterVertically) {
            FilledIconButton(
              onClick = { if (quantity > 1) quantity-- },
              shape = RoundedCornerShape(8.dp),
              colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
              )
            ) {
              androidx.compose.material3.Icon(Icons.Default.Remove, contentDescription = "Decrease")
            }
            Text(
              text = "$quantity",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              modifier = Modifier.padding(horizontal = 16.dp)
            )
            FilledIconButton(
              onClick = { quantity++ },
              shape = RoundedCornerShape(8.dp),
              colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
              )
            ) {
              androidx.compose.material3.Icon(Icons.Default.Add, contentDescription = "Increase")
            }
          }
        }

        if (errorMessage != null) {
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = errorMessage ?: "",
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall
          )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End
        ) {
          OutlinedButton(
            onClick = onDismiss,
            shape = RoundedCornerShape(10.dp)
          ) {
            Text("Cancel")
          }
          Spacer(modifier = Modifier.width(12.dp))
          Button(
            onClick = {
              val price = itemPrice.toDoubleOrNull()
              if (itemName.isBlank()) {
                errorMessage = "Please enter an item name"
              } else if (price == null || price <= 0) {
                errorMessage = "Please enter a valid price"
              } else {
                onAddItem(itemName.trim(), price, quantity)
              }
            },
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.testTag("submit_custom_item_button")
          ) {
            Text("Add to Order", fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}
