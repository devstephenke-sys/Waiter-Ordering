package com.example.ui.dialogs

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.MpesaGreen

@Composable
fun SettingsDialog(
  currentWaiterName: String,
  currentCurrency: String,
  onSave: (waiterName: String, currency: String) -> Unit,
  onOpenManageMenu: () -> Unit,
  onSignOut: () -> Unit = {},
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier
) {
  var waiterName by remember { mutableStateOf(currentWaiterName) }
  var currency by remember { mutableStateOf(currentCurrency) }

  val currencies = listOf("KSh", "$", "€", "UGX", "TZS", "ZAR")

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(20.dp),
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 6.dp,
      modifier = modifier
        .fillMaxWidth()
        .testTag("settings_dialog")
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(24.dp)
      ) {
        Text(
          text = "Waiter App Settings",
          style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = "Configure your profile and system preferences",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Waiter Profile Name
        OutlinedTextField(
          value = waiterName,
          onValueChange = { waiterName = it },
          label = { Text("Waiter / Waitress Name") },
          leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("settings_waiter_name_input"),
          shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Currency Selector
        Text(
          text = "Currency Symbol",
          style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
          color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          currencies.take(4).forEach { curr ->
            FilterChip(
              selected = currency == curr,
              onClick = { currency = curr },
              label = { Text(curr, fontWeight = FontWeight.Bold) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
              )
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Manage Menu Items Button
        OutlinedButton(
          onClick = {
            onDismiss()
            onOpenManageMenu()
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .testTag("settings_manage_menu_btn"),
          shape = RoundedCornerShape(12.dp)
        ) {
          Icon(Icons.Default.Storage, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text("Manage Menu Items (Add, Edit, Delete)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Sign Out Button
        OutlinedButton(
          onClick = {
            onDismiss()
            onSignOut()
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .testTag("settings_sign_out_btn"),
          shape = RoundedCornerShape(12.dp)
        ) {
          Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text("Sign Out", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Database status card
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
          )
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(28.dp)
                  .clip(CircleShape)
                  .background(MpesaGreen.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.CloudDone,
                  contentDescription = null,
                  tint = MpesaGreen,
                  modifier = Modifier.size(16.dp)
                )
              }
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Database & Persistence Status",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium
              )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "• Engine: Room SQLite Local Store (Instant & Offline-first)\n• Security: Zero data loss, automatic crash-safe transactions\n• Backend Connection: Configured for daily sales settlement",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
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
              onSave(waiterName.trim().ifBlank { "Waitress" }, currency)
            },
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.testTag("save_settings_button")
          ) {
            Text("Save Changes", fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}
