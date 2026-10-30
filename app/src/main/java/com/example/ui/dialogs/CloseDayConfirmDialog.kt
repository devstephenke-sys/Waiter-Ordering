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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.DailySummary
import com.example.ui.theme.MpesaGreen
import com.example.ui.theme.Terracotta
import java.util.Locale

@Composable
fun CloseDayConfirmDialog(
  summary: DailySummary,
  waiterName: String,
  currencySymbol: String,
  notes: String,
  onNotesChange: (String) -> Unit,
  onConfirm: () -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier
) {
  val hasUnpaid = summary.unpaidAmount > 0

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(20.dp),
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 6.dp,
      modifier = modifier
        .fillMaxWidth()
        .testTag("close_day_confirm_dialog")
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(24.dp)
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(40.dp)
              .clip(CircleShape)
              .background(if (hasUnpaid) Terracotta.copy(alpha = 0.15f) else MpesaGreen.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = if (hasUnpaid) Icons.Default.Warning else Icons.Default.CheckCircle,
              contentDescription = null,
              tint = if (hasUnpaid) Terracotta else MpesaGreen,
              modifier = Modifier.size(24.dp)
            )
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Text(
              text = "Complete Evening Shift?",
              style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "Official Daily Sales Closeout",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Summary Card
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
          )
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text("Total Sales Sold:", color = MaterialTheme.colorScheme.onSurfaceVariant)
              Text(
                "$currencySymbol ${String.format(Locale.US, "%,.2f", summary.totalSales)}",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text("M-Pesa Received:", color = MaterialTheme.colorScheme.onSurfaceVariant)
              Text(
                "$currencySymbol ${String.format(Locale.US, "%,.2f", summary.mpesaSales)}",
                fontWeight = FontWeight.SemiBold,
                color = MpesaGreen
              )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text("Cash Received:", color = MaterialTheme.colorScheme.onSurfaceVariant)
              Text(
                "$currencySymbol ${String.format(Locale.US, "%,.2f", summary.cashSales)}",
                fontWeight = FontWeight.SemiBold
              )
            }
          }
        }

        if (hasUnpaid) {
          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = "⚠️ Notice: You still have ${summary.pendingOrdersCount} order(s) waiting for payment ($currencySymbol ${String.format(Locale.US, "%,.2f", summary.unpaidAmount)}). It is recommended to collect payment first, but you can still record the closeout.",
            color = Terracotta,
            style = MaterialTheme.typography.bodySmall
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
          value = notes,
          onValueChange = onNotesChange,
          label = { Text("Shift Notes / Handover (Optional)") },
          placeholder = { Text("e.g. All cash turned over to manager Jane") },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("close_day_notes_input"),
          maxLines = 2,
          shape = RoundedCornerShape(12.dp)
        )

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
            onClick = onConfirm,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.testTag("confirm_close_day_button")
          ) {
            Text("Confirm & Close Day", fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}
