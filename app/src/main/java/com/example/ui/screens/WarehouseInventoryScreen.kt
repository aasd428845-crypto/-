package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.User
import com.example.model.WarehouseInventoryItem
import com.example.service.FirebaseService
import com.example.ui.theme.MedBlueAccent
import com.example.ui.theme.MedBluePrimary
import com.example.ui.theme.MedGreenPrimary
import com.example.ui.theme.MedRedPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WarehouseInventoryScreen(
    currentUser: User
) {
    val context = LocalContext.current
    var inventoryList by remember { mutableStateOf<List<WarehouseInventoryItem>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // State for Adjust Stock Dialog
    var showAdjustDialog by remember { mutableStateOf(false) }
    var selectedItemForAdjustment by remember { mutableStateOf<WarehouseInventoryItem?>(null) }
    var adjustmentType by remember { mutableStateOf("receive") } // "receive" or "discard"
    var adjustmentQtyStr by remember { mutableStateOf("") }
    var newExpiryDateStr by remember { mutableStateOf("") }

    // Load inventory from Firebase Mock
    fun loadInventory() {
        isLoading = true
        FirebaseService.getWarehouseInventory(currentUser.branchId ?: "branch_sanaa") { items ->
            inventoryList = items
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        loadInventory()
    }

    // Filter items based on Search Query (by name or SKU)
    val filteredInventory = inventoryList.filter { item ->
        item.name.contains(searchQuery, ignoreCase = true) ||
                item.sku.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(16.dp)
    ) {
        // Top stats banner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val totalItemsCount = inventoryList.size
            val lowStockCount = inventoryList.count { it.availableQuantity < 10 }

            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("إجمالي الأصناف بالمخزن", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                    Text("$totalItemsCount أصناف", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MedBluePrimary)
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("أصناف منخفضة المخزون", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("$lowStockCount أصناف", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = if (lowStockCount > 0) MedRedPrimary else MedGreenPrimary)
                        if (lowStockCount > 0) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = MedRedPrimary, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        // Search text field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("البحث باسم الدواء أو الـ SKU...", fontSize = 12.sp, color = Color.Gray) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "مسح")
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .testTag("inventory_search_input"),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                containerColor = Color.White,
                focusedBorderColor = MedBluePrimary,
                unfocusedBorderColor = Color(0xFFCBD5E1)
            ),
            singleLine = true
        )

        if (isLoading) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MedBluePrimary)
            }
        } else if (filteredInventory.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Inventory,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = Color.LightGray
                    )
                    Text(
                        text = if (searchQuery.isNotEmpty()) "لا توجد نتائج مطابقة لبحثك" else "مستودعك فارغ حالياً!",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                }
            }
        } else {
            // Inventory list
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag("warehouse_inventory_list"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredInventory) { item ->
                    val isLowStock = item.availableQuantity < 10

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("inventory_item_card_${item.sku}"),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            width = 1.dp,
                            color = if (isLowStock) MedRedPrimary.copy(alpha = 0.2f) else Color(0xFFE2E8F0)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Left Details side
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = item.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B)
                                )

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFFF1F5F9), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "SKU: ${item.sku}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF475569)
                                        )
                                    }

                                    Text(
                                        text = item.dosageForm,
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Event,
                                        contentDescription = null,
                                        tint = Color.Gray,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = "تاريخ الصلاحية: ${item.expiryDate}",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }

                                if (isLowStock) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier.padding(top = 2.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = MedRedPrimary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = "مخزون منخفض ⚠️",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MedRedPrimary
                                        )
                                    }
                                }
                            }

                            // Right Action & Quantity Side
                            Column(
                                horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Large Quantity indicator
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "${item.availableQuantity}",
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isLowStock) MedRedPrimary else MedBluePrimary
                                    )
                                    Text(
                                        text = "كرتونة متوفرة",
                                        fontSize = 10.sp,
                                        color = Color.Gray,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                // Quick Edit Button
                                Button(
                                    onClick = {
                                        selectedItemForAdjustment = item
                                        adjustmentType = "receive"
                                        adjustmentQtyStr = ""
                                        newExpiryDateStr = item.expiryDate
                                        showAdjustDialog = true
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.White,
                                        contentColor = MedBlueAccent
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MedBlueAccent.copy(alpha = 0.5f)),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier
                                        .height(32.dp)
                                        .testTag("adjust_stock_btn_${item.sku}")
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = null,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Text(
                                            text = "تعديل المخزون",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Material 3 Custom Dialog for stock adjustment (utility style, RTL-friendly)
    if (showAdjustDialog && selectedItemForAdjustment != null) {
        val currentItem = selectedItemForAdjustment!!

        AlertDialog(
            onDismissRequest = { showAdjustDialog = false },
            title = {
                Text(
                    text = "⚙️ تعديل مخزون الصنف",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Right
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "الدواء: ${currentItem.name}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Right
                    )

                    // Operation Type Selection (Segmented-like Row Buttons)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp))
                            .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
                            .padding(2.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Button(
                            onClick = { adjustmentType = "receive" },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("select_receive_tab"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (adjustmentType == "receive") MedGreenPrimary else Color.Transparent,
                                contentColor = if (adjustmentType == "receive") Color.White else Color.DarkGray
                            ),
                            elevation = null,
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            Text("استلام شحنة 📥", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { adjustmentType = "discard" },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("select_discard_tab"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (adjustmentType == "discard") MedRedPrimary else Color.Transparent,
                                contentColor = if (adjustmentType == "discard") Color.White else Color.DarkGray
                            ),
                            elevation = null,
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            Text("تالف / منتهي الصلاحية 🗑️", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Quantity Input Field
                    OutlinedTextField(
                        value = adjustmentQtyStr,
                        onValueChange = { adjustmentQtyStr = it },
                        label = {
                            Text(
                                if (adjustmentType == "receive") "الكمية الواردة بالكرتون" else "الكمية التالفة بالكرتون",
                                fontSize = 12.sp
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("stock_adjustment_qty_input"),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    // Expiry Date (only for Receive Stock)
                    if (adjustmentType == "receive") {
                        OutlinedTextField(
                            value = newExpiryDateStr,
                            onValueChange = { newExpiryDateStr = it },
                            label = { Text("تاريخ انتهاء الصلاحية الشحنة (اختياري)", fontSize = 11.sp) },
                            placeholder = { Text("مثال: 2028-12-31", fontSize = 11.sp, color = Color.Gray) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("stock_expiry_date_input"),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val qtyVal = adjustmentQtyStr.toIntOrNull()
                        if (qtyVal == null || qtyVal <= 0) {
                            Toast.makeText(context, "الرجاء إدخال كمية صحيحة أكبر من الصفر", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        // Determine sign of change
                        val delta = if (adjustmentType == "receive") qtyVal else -qtyVal

                        // Prevent discarding more than available
                        if (adjustmentType == "discard" && qtyVal > currentItem.availableQuantity) {
                            Toast.makeText(context, "لا يمكن إتلاف كمية أكبر من المخزون المتوفر!", Toast.LENGTH_LONG).show()
                            return@Button
                        }

                        // Update in Firebase Service
                        FirebaseService.updateInventoryQuantity(
                            sku = currentItem.sku,
                            addedQty = delta,
                            expiryDate = if (adjustmentType == "receive") newExpiryDateStr else ""
                        ) { success ->
                            if (success) {
                                Toast.makeText(context, "تم تعديل كمية المخزون بنجاح 👍", Toast.LENGTH_SHORT).show()
                                showAdjustDialog = false
                                loadInventory()
                            } else {
                                Toast.makeText(context, "فشل تعديل المخزون", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier.testTag("save_adjustment_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (adjustmentType == "receive") MedGreenPrimary else MedRedPrimary
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("حفظ", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showAdjustDialog = false },
                    modifier = Modifier.testTag("cancel_adjustment_btn")
                ) {
                    Text("إلغاء", color = Color.Gray, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
