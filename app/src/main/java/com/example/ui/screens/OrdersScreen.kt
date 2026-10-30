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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.ui.components.OrderCard
import com.example.ui.theme.MpesaGreen
import com.example.ui.theme.Terracotta
import com.example.ui.viewmodel.OrdersFilter
import java.util.Locale

@Composable
fun OrdersScreen(
  orders: List<OrderWithItems>,
  currentFilter: OrdersFilter,
  waiterName: String,
  currencySymbol: String,
  onFilterChange: (OrdersFilter) -> Unit,
  onPayClick: (OrderWithItems) -> Unit,
  onOrderClick: (OrderWithItems) -> Unit,
  onNewOrderClick: () -> Unit,
  onSettingsClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  var searchQuery by remember { mutableStateOf("") }

  val pendingCount = orders.count { it.order.status == OrderStatus.WAITING_FOR_PAYMENT.name }
  val completedCount = orders.count { it.order.status == OrderStatus.COMPLETED.name }

  val filteredOrders = orders.filter { orderWithItems ->
    val o = orderWithItems.order
    val matchesFilter = when (currentFilter) {
      OrdersFilter.ALL -> true
      OrdersFilter.WAITING_FOR_PAYMENT -> o.status == OrderStatus.WAITING_FOR_PAYMENT.name
      OrdersFilter.COMPLETED -> o.status == OrderStatus.COMPLETED.name
    }
    val matchesSearch = searchQuery.isBlank() ||
      o.tableName.contains(searchQuery, ignoreCase = true) ||
      o.orderNumber.contains(searchQuery, ignoreCase = true) ||
      orderWithItems.items.any { it.itemName.contains(searchQuery, ignoreCase = true) }

    matchesFilter && matchesSearch
  }

  Box(modifier = modifier.fillMaxSize()) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp)
    ) {
      Spacer(modifier = Modifier.height(12.dp))

      // Top Header Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Welcome, $waiterName 👋",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
          )
          Text(
            text = "Floor Orders & Table Bills",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          if (pendingCount > 0) {
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Terracotta.copy(alpha = 0.15f))
                .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
              Text(
                text = "$pendingCount Waiting Payment",
                color = Terracotta,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
              )
            }
            Spacer(modifier = Modifier.width(8.dp))
          }

          IconButton(
            onClick = onSettingsClick,
            modifier = Modifier.testTag("settings_button")
          ) {
            Icon(
              imageVector = Icons.Default.Settings,
              contentDescription = "Settings",
              tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Search Bar
      OutlinedTextField(
        value = searchQuery,
        onValueChange = { searchQuery = it },
        placeholder = { Text("Search by table, order #, or dish...") },
        leadingIcon = {
          Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        },
        modifier = Modifier
          .fillMaxWidth()
          .testTag("orders_search_input"),
        singleLine = true,
        shape = RoundedCornerShape(12.dp)
      )

      Spacer(modifier = Modifier.height(10.dp))

      // Filter Chips Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        FilterChip(
          selected = currentFilter == OrdersFilter.ALL,
          onClick = { onFilterChange(OrdersFilter.ALL) },
          label = { Text("All (${orders.size})") },
          modifier = Modifier.testTag("filter_all_orders")
        )

        FilterChip(
          selected = currentFilter == OrdersFilter.WAITING_FOR_PAYMENT,
          onClick = { onFilterChange(OrdersFilter.WAITING_FOR_PAYMENT) },
          label = { Text("Waiting Payment ($pendingCount)") },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = Terracotta.copy(alpha = 0.15f),
            selectedLabelColor = Terracotta
          ),
          modifier = Modifier.testTag("filter_waiting_orders")
        )

        FilterChip(
          selected = currentFilter == OrdersFilter.COMPLETED,
          onClick = { onFilterChange(OrdersFilter.COMPLETED) },
          label = { Text("Paid ($completedCount)") },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MpesaGreen.copy(alpha = 0.15f),
            selectedLabelColor = MpesaGreen
          ),
          modifier = Modifier.testTag("filter_completed_orders")
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Orders List or Empty State
      if (filteredOrders.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
          contentAlignment = Alignment.Center
        ) {
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .padding(24.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Box(
                modifier = Modifier
                  .size(56.dp)
                  .clip(CircleShape)
                  .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.RestaurantMenu,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(28.dp)
                )
              }
              Spacer(modifier = Modifier.height(14.dp))
              Text(
                text = if (orders.isEmpty()) "No Orders Yet" else "No Matching Orders",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
              )
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = if (orders.isEmpty())
                  "Tap '+ New Order' below to take food & drink orders for a table."
                else
                  "Try changing your search term or active filter.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
              )
            }
          }
        }
      } else {
        LazyColumn(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
          contentPadding = PaddingValues(bottom = 90.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
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

    // Extended FAB to Take Order
    ExtendedFloatingActionButton(
      onClick = onNewOrderClick,
      icon = { Icon(Icons.Default.Add, contentDescription = null) },
      text = { Text("New Order", fontWeight = FontWeight.Bold) },
      containerColor = MaterialTheme.colorScheme.primary,
      contentColor = Color.White,
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(bottom = 16.dp, end = 16.dp)
        .testTag("new_order_fab")
    )
  }
}
