package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
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
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.MenuItemEntity
import com.example.data.model.CartItem
import com.example.ui.theme.MpesaGreen
import com.example.ui.theme.Terracotta
import java.util.Locale

@Composable
fun NewOrderScreen(
  menuItems: List<MenuItemEntity>,
  cart: List<CartItem>,
  currentTable: String,
  orderNotes: String,
  selectedCategory: String,
  searchQuery: String,
  currencySymbol: String,
  onTableChange: (String) -> Unit,
  onCategoryChange: (String) -> Unit,
  onSearchChange: (String) -> Unit,
  onNotesChange: (String) -> Unit,
  onAddToCart: (MenuItemEntity) -> Unit,
  onUpdateCartQuantity: (index: Int, newQuantity: Int) -> Unit,
  onRemoveFromCart: (index: Int) -> Unit,
  onClearCart: () -> Unit,
  onOpenCustomItemDialog: () -> Unit,
  onOpenManageMenu: () -> Unit,
  onSubmitOrder: (immediatePay: Boolean) -> Unit,
  modifier: Modifier = Modifier
) {
  var isCartExpanded by remember { mutableStateOf(false) }

  val defaultTables = listOf("Table 1", "Table 2", "Table 3", "Table 4", "Table 5", "Table 6", "Bar Counter", "VIP Room", "Takeaway")
  val categories = listOf("All", "Meals", "Sides", "Drinks", "Hot Drinks")

  val cartTotal = cart.sumOf { it.subtotal }
  val cartItemsCount = cart.sumOf { it.quantity }

  val filteredMenuItems = menuItems.filter { item ->
    val matchesCategory = selectedCategory == "All" || item.category.equals(selectedCategory, ignoreCase = true)
    val matchesSearch = searchQuery.isBlank() ||
      item.name.contains(searchQuery, ignoreCase = true) ||
      item.category.contains(searchQuery, ignoreCase = true)
    matchesCategory && matchesSearch
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp)
  ) {
    Spacer(modifier = Modifier.height(12.dp))

    // Screen Title
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = "Take New Order",
          style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = "Select table and tap items to add",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        OutlinedButton(
          onClick = onOpenManageMenu,
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.testTag("manage_menu_btn")
        ) {
          Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(15.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Menu", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        OutlinedButton(
          onClick = onOpenCustomItemDialog,
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.testTag("add_custom_item_btn")
        ) {
          Icon(Icons.Default.PostAdd, contentDescription = null, modifier = Modifier.size(15.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("+ Custom", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Table Selector Chips
    Text(
      text = "SELECT TABLE / LOCATION",
      style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp, fontWeight = FontWeight.Bold),
      color = MaterialTheme.colorScheme.primary
    )
    Spacer(modifier = Modifier.height(6.dp))
    LazyRow(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      items(defaultTables) { table ->
        val isSelected = currentTable == table
        FilterChip(
          selected = isSelected,
          onClick = { onTableChange(table) },
          label = { Text(table, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = Color.White
          ),
          shape = RoundedCornerShape(12.dp)
        )
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Category Tabs
    ScrollableTabRow(
      selectedTabIndex = categories.indexOf(selectedCategory).coerceAtLeast(0),
      edgePadding = 0.dp,
      containerColor = Color.Transparent,
      divider = {}
    ) {
      categories.forEach { cat ->
        Tab(
          selected = selectedCategory == cat,
          onClick = { onCategoryChange(cat) },
          text = {
            Text(
              text = cat,
              fontWeight = if (selectedCategory == cat) FontWeight.Bold else FontWeight.Normal,
              fontSize = 13.sp
            )
          }
        )
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Search input
    OutlinedTextField(
      value = searchQuery,
      onValueChange = onSearchChange,
      placeholder = { Text("Search dishes, drinks, sides...") },
      leadingIcon = {
        Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
      },
      modifier = Modifier
        .fillMaxWidth()
        .testTag("menu_search_input"),
      singleLine = true,
      shape = RoundedCornerShape(12.dp)
    )

    Spacer(modifier = Modifier.height(10.dp))

    // Menu Items Grid
    LazyVerticalGrid(
      columns = GridCells.Fixed(2),
      horizontalArrangement = Arrangement.spacedBy(10.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth(),
      contentPadding = PaddingValues(bottom = if (cart.isNotEmpty()) 160.dp else 20.dp)
    ) {
      items(filteredMenuItems, key = { it.id }) { item ->
        val cartQuantity = cart.firstOrNull { it.name == item.name }?.quantity ?: 0

        MenuItemCard(
          item = item,
          currencySymbol = currencySymbol,
          cartQuantity = cartQuantity,
          onAdd = { onAddToCart(item) }
        )
      }
    }

    // Persistent Bottom Order Panel (Cart)
    if (cart.isNotEmpty()) {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 8.dp)
          .testTag("order_cart_bottom_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          // Toggle row: Item Count & Total Amount
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { isCartExpanded = !isCartExpanded },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(34.dp)
                  .clip(CircleShape)
                  .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  Icons.Default.ShoppingCart,
                  contentDescription = null,
                  tint = Color.White,
                  modifier = Modifier.size(18.dp)
                )
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = "$currentTable • $cartItemsCount items",
                  fontWeight = FontWeight.Bold,
                  style = MaterialTheme.typography.titleSmall
                )
                Text(
                  text = if (isCartExpanded) "Tap to collapse" else "Tap to review items",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "$currencySymbol ${String.format(Locale.US, "%,.2f", cartTotal)}",
                style = MaterialTheme.typography.titleLarge.copy(
                  fontWeight = FontWeight.ExtraBold,
                  color = MaterialTheme.colorScheme.primary,
                  fontSize = 18.sp
                )
              )
              Spacer(modifier = Modifier.width(6.dp))
              Icon(
                imageVector = if (isCartExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          // Expandable Cart Details
          AnimatedVisibility(visible = isCartExpanded) {
            Column(modifier = Modifier.padding(top = 10.dp)) {
              HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
              Spacer(modifier = Modifier.height(8.dp))

              LazyColumn(
                modifier = Modifier
                  .fillMaxWidth()
                  .height((cart.size * 48).coerceAtMost(180).dp)
              ) {
                items(cart.indices.toList()) { index ->
                  val cartItem = cart[index]
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Column(modifier = Modifier.weight(1f)) {
                      Text(
                        text = cartItem.name,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                      )
                      Text(
                        text = "$currencySymbol ${String.format(Locale.US, "%.0f", cartItem.unitPrice)} each",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                      )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                      IconButton(
                        onClick = { onUpdateCartQuantity(index, cartItem.quantity - 1) },
                        modifier = Modifier.size(32.dp)
                      ) {
                        Icon(
                          imageVector = if (cartItem.quantity == 1) Icons.Default.Delete else Icons.Default.Remove,
                          contentDescription = "Decrease",
                          modifier = Modifier.size(16.dp),
                          tint = if (cartItem.quantity == 1) Terracotta else MaterialTheme.colorScheme.onSurface
                        )
                      }
                      Text(
                        text = "${cartItem.quantity}",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp)
                      )
                      IconButton(
                        onClick = { onUpdateCartQuantity(index, cartItem.quantity + 1) },
                        modifier = Modifier.size(32.dp)
                      ) {
                        Icon(
                          Icons.Default.Add,
                          contentDescription = "Increase",
                          modifier = Modifier.size(16.dp),
                          tint = MaterialTheme.colorScheme.primary
                        )
                      }
                    }
                  }
                }
              }

              Spacer(modifier = Modifier.height(8.dp))

              // Notes Input
              OutlinedTextField(
                value = orderNotes,
                onValueChange = onNotesChange,
                label = { Text("Kitchen / Table Notes (Optional)") },
                placeholder = { Text("e.g. Extra chilli, serve drinks first") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // 2 Action Buttons
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            // Save as Waiting for Payment
            Button(
              onClick = { onSubmitOrder(false) },
              modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .testTag("save_waiting_order_btn"),
              colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
              ),
              shape = RoundedCornerShape(12.dp)
            ) {
              Text(
                text = "Save (Waiting Pay)",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
              )
            }

            // Instant Pay & Complete
            Button(
              onClick = { onSubmitOrder(true) },
              modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .testTag("instant_pay_order_btn"),
              colors = ButtonDefaults.buttonColors(
                containerColor = MpesaGreen
              ),
              shape = RoundedCornerShape(12.dp)
            ) {
              Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Instant Pay",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun MenuItemCard(
  item: MenuItemEntity,
  currencySymbol: String,
  cartQuantity: Int,
  onAdd: () -> Unit,
  modifier: Modifier = Modifier
) {
  val isInCart = cartQuantity > 0

  Card(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp))
      .clickable { onAdd() }
      .border(
        width = if (isInCart) 2.dp else 1.dp,
        color = if (isInCart) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
        shape = RoundedCornerShape(14.dp)
      )
      .testTag("menu_item_${item.id}"),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (isInCart) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface
    )
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
      ) {
        Text(
          text = item.emoji,
          fontSize = 24.sp
        )

        if (isInCart) {
          Box(
            modifier = Modifier
              .size(24.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "$cartQuantity",
              color = Color.White,
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = item.name,
        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
      )

      Spacer(modifier = Modifier.height(4.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "$currencySymbol ${String.format(Locale.US, "%.0f", item.price)}",
          style = MaterialTheme.typography.labelLarge.copy(
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
          )
        )

        Box(
          modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
          )
        }
      }
    }
  }
}
