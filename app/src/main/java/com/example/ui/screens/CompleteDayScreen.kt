package com.example.ui.screens

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.dao.ItemSalesCount
import com.example.data.local.entities.DailySettlementEntity
import com.example.data.model.DailySummary
import com.example.ui.theme.CashAmber
import com.example.ui.theme.MpesaGreen
import com.example.ui.theme.Terracotta
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CompleteDayScreen(
  summary: DailySummary,
  topSellingItems: List<ItemSalesCount>,
  pastSettlements: List<DailySettlementEntity>,
  waiterName: String,
  currencySymbol: String,
  settlementSuccessBanner: String?,
  onDismissSuccessBanner: () -> Unit,
  onOpenCloseDayConfirm: () -> Unit,
  onGoToPendingOrders: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val hasPending = summary.pendingOrdersCount > 0

  val todayFormatted = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(Date())

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    contentPadding = PaddingValues(top = 12.dp, bottom = 100.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Top Title & Subtitle
    item {
      Column {
        Text(
          text = "Evening Sales & Day Closeout",
          style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = "$todayFormatted • Waiter: $waiterName",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    // Success Banner if recently closed
    if (settlementSuccessBanner != null) {
      item {
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = MpesaGreen.copy(alpha = 0.15f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
              Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MpesaGreen)
              Spacer(modifier = Modifier.width(10.dp))
              Text(
                text = settlementSuccessBanner,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MpesaGreen
              )
            }
            IconButton(onClick = onDismissSuccessBanner, modifier = Modifier.size(24.dp)) {
              Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = MpesaGreen)
            }
          }
        }
      }
    }

    // Hero Total Sold Card
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("hero_total_sales_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.primary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "TOTAL SALES COMPLETED TODAY",
              style = MaterialTheme.typography.labelSmall.copy(
                letterSpacing = 1.2.sp,
                fontWeight = FontWeight.Bold
              ),
              color = Color.White.copy(alpha = 0.85f)
            )

            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White.copy(alpha = 0.2f))
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Text(
                text = "${summary.completedOrdersCount} orders paid",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = Color.White
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          Text(
            text = "$currencySymbol ${String.format(Locale.US, "%,.2f", summary.totalSales)}",
            style = MaterialTheme.typography.headlineLarge.copy(
              fontWeight = FontWeight.ExtraBold,
              fontSize = 36.sp
            ),
            color = Color.White
          )

          Spacer(modifier = Modifier.height(6.dp))

          Text(
            text = "Total amount collected across all completed orders today",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.8f)
          )
        }
      }
    }

    // Payment Method Breakdown: M-Pesa vs Cash
    item {
      Column {
        Text(
          text = "PAYMENT METHOD BREAKDOWN",
          style = MaterialTheme.typography.labelSmall.copy(
            letterSpacing = 1.sp,
            fontWeight = FontWeight.Bold
          ),
          color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(10.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          // M-Pesa Card
          Card(
            modifier = Modifier
              .weight(1f)
              .testTag("mpesa_sales_card"),
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
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
              ) {
                Box(
                  modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MpesaGreen.copy(alpha = 0.15f)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.PhoneAndroid,
                    contentDescription = null,
                    tint = MpesaGreen,
                    modifier = Modifier.size(20.dp)
                  )
                }
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(MpesaGreen.copy(alpha = 0.1f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                  Text(
                    text = "M-Pesa",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MpesaGreen
                  )
                }
              }

              Spacer(modifier = Modifier.height(12.dp))

              Text(
                text = "$currencySymbol ${String.format(Locale.US, "%,.2f", summary.mpesaSales)}",
                style = MaterialTheme.typography.titleLarge.copy(
                  fontWeight = FontWeight.Bold,
                  fontSize = 18.sp
                ),
                color = MpesaGreen
              )

              Spacer(modifier = Modifier.height(4.dp))

              val mpesaPct = if (summary.totalSales > 0) ((summary.mpesaSales / summary.totalSales) * 100).toInt() else 0
              Text(
                text = "$mpesaPct% of today's sales",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          // Cash Card
          Card(
            modifier = Modifier
              .weight(1f)
              .testTag("cash_sales_card"),
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
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
              ) {
                Box(
                  modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(CashAmber.copy(alpha = 0.15f)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.MonetizationOn,
                    contentDescription = null,
                    tint = CashAmber,
                    modifier = Modifier.size(20.dp)
                  )
                }
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(CashAmber.copy(alpha = 0.1f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                  Text(
                    text = "Cash",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = CashAmber
                  )
                }
              }

              Spacer(modifier = Modifier.height(12.dp))

              Text(
                text = "$currencySymbol ${String.format(Locale.US, "%,.2f", summary.cashSales)}",
                style = MaterialTheme.typography.titleLarge.copy(
                  fontWeight = FontWeight.Bold,
                  fontSize = 18.sp
                ),
                color = CashAmber
              )

              Spacer(modifier = Modifier.height(4.dp))

              val cashPct = if (summary.totalSales > 0) ((summary.cashSales / summary.totalSales) * 100).toInt() else 0
              Text(
                text = "$cashPct% in cash drawer",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      }
    }

    // Pending / Unsettled Orders Alert
    item {
      if (hasPending) {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(
            containerColor = Terracotta.copy(alpha = 0.12f)
          ),
          border = androidx.compose.foundation.BorderStroke(1.dp, Terracotta.copy(alpha = 0.4f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Warning, contentDescription = null, tint = Terracotta)
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Attention: Unsettled Orders Remaining",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                color = Terracotta
              )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "${summary.pendingOrdersCount} table(s) are still waiting for payment ($currencySymbol ${String.format(Locale.US, "%,.2f", summary.unpaidAmount)}). For complete accuracy, collect payments before closing the day.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(10.dp))
            Button(
              onClick = onGoToPendingOrders,
              colors = ButtonDefaults.buttonColors(containerColor = Terracotta),
              shape = RoundedCornerShape(10.dp)
            ) {
              Text("Review & Settle Pending Orders", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
          }
        }
      } else {
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(
            containerColor = MpesaGreen.copy(alpha = 0.1f)
          ),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MpesaGreen)
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "All Orders Fully Paid!",
                fontWeight = FontWeight.Bold,
                color = MpesaGreen
              )
              Text(
                text = "Every table served today has been settled. Shift is ready for closeout.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      }
    }

    // Top Selling Dishes Today
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Star, contentDescription = null, tint = CashAmber, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Top Selling Items Today",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurface
            )
          }

          Spacer(modifier = Modifier.height(12.dp))

          if (topSellingItems.isEmpty()) {
            Text(
              text = "No items recorded today yet.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          } else {
            topSellingItems.forEachIndexed { index, item ->
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 5.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                    text = "#${index + 1}",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.width(28.dp)
                  )
                  Text(
                    text = item.itemName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                  )
                }
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                  Text(
                    text = "${item.totalQuantity} sold",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }
            }
          }
        }
      }
    }

    // Evening Shift Completion Actions
    item {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Button(
          onClick = onOpenCloseDayConfirm,
          modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .testTag("complete_day_closeout_button"),
          shape = RoundedCornerShape(14.dp),
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
          Icon(Icons.Default.Lock, contentDescription = null)
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Complete Day & Archive Shift",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
          )
        }

        OutlinedButton(
          onClick = {
            val reportText = buildString {
              appendLine("📊 DAILY WAITER SALES REPORT")
              appendLine("Date: $todayFormatted")
              appendLine("Waiter: $waiterName")
              appendLine("--------------------------------")
              appendLine("GRAND TOTAL SOLD: $currencySymbol ${String.format(Locale.US, "%,.2f", summary.totalSales)}")
              appendLine("• M-Pesa Total: $currencySymbol ${String.format(Locale.US, "%,.2f", summary.mpesaSales)}")
              appendLine("• Cash in Hand: $currencySymbol ${String.format(Locale.US, "%,.2f", summary.cashSales)}")
              if (summary.unpaidAmount > 0) {
                appendLine("• Unsettled Orders: $currencySymbol ${String.format(Locale.US, "%,.2f", summary.unpaidAmount)}")
              }
              appendLine("Total Orders Served: ${summary.totalOrdersCount} (${summary.completedOrdersCount} paid)")
              appendLine("--------------------------------")
              appendLine("Top Sellers:")
              topSellingItems.take(5).forEachIndexed { i, it ->
                appendLine("${i + 1}. ${it.itemName}: ${it.totalQuantity} sold")
              }
              appendLine("--------------------------------")
              appendLine("Generated via Waiter Orders")
            }
            val intent = Intent().apply {
              action = Intent.ACTION_SEND
              putExtra(Intent.EXTRA_TEXT, reportText)
              type = "text/plain"
            }
            context.startActivity(Intent.createChooser(intent, "Share Daily Report"))
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("share_daily_report_button"),
          shape = RoundedCornerShape(14.dp)
        ) {
          Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text("Share Daily Sales Report (WhatsApp / SMS)", fontWeight = FontWeight.SemiBold)
        }
      }
    }

    // Past Saved Settlements History
    if (pastSettlements.isNotEmpty()) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Previous Closed Days",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
              )
            }
            Spacer(modifier = Modifier.height(10.dp))

            pastSettlements.take(5).forEach { settlement ->
              val timeStr = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault()).format(Date(settlement.closedAt))
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text(
                    text = settlement.dateString,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                  )
                  Text(
                    text = timeStr,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
                Column(horizontalAlignment = Alignment.End) {
                  Text(
                    text = "$currencySymbol ${String.format(Locale.US, "%,.2f", settlement.totalSales)}",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                  )
                  Text(
                    text = "M-Pesa: ${String.format(Locale.US, "%.0f", settlement.mpesaSales)} | Cash: ${String.format(Locale.US, "%.0f", settlement.cashSales)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }
              HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            }
          }
        }
      }
    }
  }
}
