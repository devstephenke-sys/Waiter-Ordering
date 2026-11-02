package com.example.ui.dialogs

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.local.entities.MenuItemEntity
import java.util.Locale

@Composable
fun EditMenuItemDialog(
  itemToEdit: MenuItemEntity?,
  currencySymbol: String,
  onSave: (id: Long?, name: String, category: String, price: Double, emoji: String, imageUri: String?) -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier
) {
  val isEditing = itemToEdit != null

  var name by remember { mutableStateOf(itemToEdit?.name ?: "") }
  var category by remember { mutableStateOf(itemToEdit?.category ?: "Meals") }
  var priceInput by remember {
    mutableStateOf(if (itemToEdit != null) String.format(Locale.US, "%.0f", itemToEdit.price) else "")
  }
  var selectedEmoji by remember { mutableStateOf(itemToEdit?.emoji ?: "🍽️") }
  var imageUri by remember { mutableStateOf(itemToEdit?.imageUri) }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  val photoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri: Uri? ->
    uri?.let {
      imageUri = it.toString()
    }
  }

  val defaultCategories = listOf("Meals", "Sides", "Drinks", "Hot Drinks", "Desserts")
  val sampleEmojis = listOf("🍽️", "🍗", "🍛", "🥩", "🐟", "🍚", "🥬", "🫓", "🍟", "🥟", "🍩", "🍹", "🥭", "🥤", "💧", "🍵", "☕", "🍰", "🍕", "🍔")

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(20.dp),
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 6.dp,
      modifier = modifier
        .fillMaxWidth()
        .testTag("edit_menu_item_dialog")
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(24.dp)
          .verticalScroll(rememberScrollState())
      ) {
        Text(
          text = if (isEditing) "Edit Menu Item" else "Add New Menu Item",
          style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = if (isEditing) "Update dish details in database" else "Create a new dish or drink for your menu",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Image / Photo Preview & Selector
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(72.dp)
              .clip(RoundedCornerShape(16.dp))
              .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
              .clickable { photoPickerLauncher.launch("image/*") },
            contentAlignment = Alignment.Center
          ) {
            if (!imageUri.isNullOrBlank()) {
              AsyncImage(
                model = imageUri,
                contentDescription = name,
                modifier = Modifier.size(72.dp),
                contentScale = ContentScale.Crop
              )
            } else {
              Text(selectedEmoji, fontSize = 36.sp)
            }
          }

          Spacer(modifier = Modifier.width(14.dp))

          Column {
            OutlinedButton(
              onClick = { photoPickerLauncher.launch("image/*") },
              shape = RoundedCornerShape(10.dp)
            ) {
              Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Select Photo", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            if (!imageUri.isNullOrBlank()) {
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Remove Photo",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.clickable { imageUri = null }
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Name of Food / Item
        OutlinedTextField(
          value = name,
          onValueChange = {
            name = it
            errorMessage = null
          },
          label = { Text("Item / Dish Name") },
          placeholder = { Text("e.g. Nyama Choma Special") },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("menu_item_name_input"),
          singleLine = true,
          shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Price per 1 Quantity
        OutlinedTextField(
          value = priceInput,
          onValueChange = {
            priceInput = it
            errorMessage = null
          },
          label = { Text("Price per 1 Quantity ($currencySymbol)") },
          placeholder = { Text("e.g. 550") },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("menu_item_price_input"),
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          singleLine = true,
          shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Category Selection
        Text(
          text = "Category",
          style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
          color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          items(defaultCategories) { cat ->
            FilterChip(
              selected = category == cat,
              onClick = { category = cat },
              label = { Text(cat, fontSize = 12.sp) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
              )
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Emoji Selection
        Text(
          text = "Icon / Emoji (Fallback)",
          style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
          color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          items(sampleEmojis) { emoji ->
            FilterChip(
              selected = selectedEmoji == emoji,
              onClick = { selectedEmoji = emoji },
              label = { Text(emoji, fontSize = 18.sp) }
            )
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

        // Actions
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
              val price = priceInput.toDoubleOrNull()
              if (name.isBlank()) {
                errorMessage = "Please enter an item name"
              } else if (price == null || price <= 0) {
                errorMessage = "Please enter a valid price per quantity"
              } else {
                onSave(itemToEdit?.id, name.trim(), category, price, selectedEmoji, imageUri)
              }
            },
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.testTag("save_menu_item_btn")
          ) {
            Text(if (isEditing) "Save Changes" else "Add Item", fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}
