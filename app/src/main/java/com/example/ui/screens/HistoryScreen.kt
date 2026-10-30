package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.dao.OrderWithItems
import com.example.data.model.OrderStatus
import com.example.data.model.PaymentMethod
import com.example.ui.components.OrderCard
import com.example.ui.theme.CashAmber
import com.example.ui.theme.MpesaGreen
import com.example.ui.viewmodel.HistoryDateFilter
import java.util.Calendar
import java.util.Locale

@Composable
fun HistoryScreen(
  allOrders: List<OrderWithItems>,
  currentDateFilter: HistoryDateFilter,
  currencySymbol: String,
  onDateFilterChange: (HistoryDateFilter) -> Unit,
  onPayClick: (OrderWithItems) -> Unit,
  onOrderClick: (OrderWithItems) -> Unit,
  modifier: Modifier = Modifier
) {
  var searchQuery by remember { mutableStateOf("") }

  val calendar = Calendar.getInstance()
  val now = calendar.timeInMillis

  // Start of today
  calendar.set(Calendar.HOUR_OF_DAY, 0)
  calendar.set(Calendar.MINUTE, 0)
  calendar.set(Calendar.SECOND, 0)
  calendar.set(Calendar.MILLISECOND, 0)
  val startOfToday = calendar.timeInMillis

  // Start of yesterday
  calendar.add(Calendar.DAY_OF_YEAR, -1)
  val startOfYesterday = calendar.timeInMillis

  // Start of 7 days ago
  calendar.timeInMillis = startOfToday
  calendar.add(Calendar.DAY_OF_YEAR, -6)
  val startOfWeek = calendar.timeInMillis

  val filteredOrders = allOrders.filter { orderWithItems ->
    val o = orderWithItems.order
    val matchesDate = when (currentDateFilter) {
      HistoryDateFilter.ALL -> true
      HistoryDateFilter.TODAY -> o.createdAt >= startOfToday
      HistoryDateFilter.YESTERDAY -> o.createdAt in startOfYesterday until startOfToday
      HistoryDateFilter.THIS_WEEK -> o.createdAt >= startOfWeek
    }
    val matchesSearch = searchQuery.isBlank() ||
      o.tableName.contains(searchQuery, ignoreCase = true) ||
      o.orderNumber.contains(searchQuery, ignoreCase = true) ||
      orderWithItems.items.any { it.itemName.contains(searchQuery, ignoreCase = true) }

    matchesDate && matchesSearch
  }

  val totalSales = filteredOrders
    .filter { it.order.status == OrderStatus.COMPLETED.name }
    .sumOf { it.order.totalAmount }

  val mpesaSales = filteredOrders
    .filter { it.order.status == OrderStatus.COMPLETED.name && it.order.paymentMethod == PaymentMethod.MPESA.name }
    .sumOf { it.order.totalAmount }

  val cashSales = filteredOrders
    .filter { it.order.status == OrderStatus.COMPLETED.name && it.order.paymentMethod == PaymentMethod.CASH.name }
    .sumOf { it.order.totalAmount }

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp)
  ) {
    Spacer(modifier = Modifier.height(12.dp))

    // Header
    Column {
      Text(
        text = "Order History & Logs",
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface
      )
      Text(
        text = "Audit trail of all orders, payments, and receipts",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Date Filter Chips
    LazyRow(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      items(HistoryDateFilter.values()) { filter ->
        val label = when (filter) {
          HistoryDateFilter.ALL -> "All History"
          HistoryDateFilter.TODAY -> "Today"
          HistoryDateFilter.YESTERDAY -> "Yesterday"
          HistoryDateFilter.THIS_WEEK -> "Past 7 Days"
        }
        FilterChip(
          selected = currentDateFilter == filter,
          onClick = { onDateFilterChange(filter) },
          label = { Text(label) },
          shape = RoundedCornerShape(12.dp)
        )
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Period Summary Stat Card
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(14.dp),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
      )
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text("Total Revenue", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          Text(
            "$currencySymbol ${String.format(Locale.US, "%,.0f", totalSales)}",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
          )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text("M-Pesa", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          Text(
            "$currencySymbol ${String.format(Locale.US, "%,.0f", mpesaSales)}",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium,
            color = MpesaGreen
          )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text("Cash", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          Text(
            "$currencySymbol ${String.format(Locale.US, "%,.0f", cashSales)}",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium,
            color = CashAmber
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Search bar
    OutlinedTextField(
      value = searchQuery,
      onValueChange = { searchQuery = it },
      placeholder = { Text("Filter by table or order #...") },
      leadingIcon = {
        Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
      },
      modifier = Modifier
        .fillMaxWidth()
        .testTag("history_search_input"),
      singleLine = true,
      shape = RoundedCornerShape(12.dp)
    )

    Spacer(modifier = Modifier.height(12.dp))

    // Orders List or Empty
    if (filteredOrders.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(
            imageVector = Icons.Default.History,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(48.dp)
          )
          Spacer(modifier = Modifier.height(10.dp))
          Text(
            text = "No history records found",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        contentPadding = PaddingValues(bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        items(filteredOrders, key = { it.order.id }) { orderWithItems ->
          OrderCard(
            orderWithItems = orderWithItems,
            currencySymbol = currencySymbol,
            onPayClick = { onPayClick(orderWithItems) },
            onClick = { onOrderClick(orderWithItems) }
          )
        }
      }
    }
  }
}
