package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Branch
import com.example.model.PharmaProduct
import com.example.model.WarehouseInventoryItem
import com.example.service.FirebaseService
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrossBranchInventoryScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    
    // Core state
    var branches by remember { mutableStateOf<List<Branch>>(emptyList()) }
    var products by remember { mutableStateOf<List<PharmaProduct>>(emptyList()) }
    var branchInventories by remember { mutableStateOf<Map<String, List<WarehouseInventoryItem>>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }
    
    // Filter & configuration states
    var searchQuery by remember { mutableStateOf("") }
    var lowStockThresholdStr by remember { mutableStateOf("10") }
    var showOnlyLowStock by remember { mutableStateOf(false) }
    
    val lowStockThreshold = lowStockThresholdStr.toIntOrNull() ?: 10

    // Load data from Firebase
    fun loadAllData() {
        isLoading = true
        FirebaseService.getBranches { fetchedBranches ->
            branches = fetchedBranches
            FirebaseService.getPharmaProducts { fetchedProducts ->
                products = fetchedProducts
                
                if (fetchedBranches.isEmpty()) {
                    isLoading = false
                    return@getPharmaProducts
                }
                
                val inventoryMap = mutableMapOf<String, List<WarehouseInventoryItem>>()
                var completedRequests = 0
                
                fetchedBranches.forEach { branch ->
                    FirebaseService.getWarehouseInventory(branch.branchId) { items ->
                        inventoryMap[branch.branchId] = items
                        completedRequests++
                        if (completedRequests == fetchedBranches.size) {
                            branchInventories = inventoryMap
                            isLoading = false
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        loadAllData()
    }

    // Helper: find stock for a specific branch and SKU
    fun getBranchStock(branchId: String, sku: String): Int {
        val branchItems = branchInventories[branchId] ?: return 0
        val found = branchItems.find { it.sku == sku }
        return found?.availableQuantity ?: 0
    }

    // Process and filter products
    val filteredProducts = products.filter { product ->
        val matchesSearch = product.commercialName.contains(searchQuery, ignoreCase = true) ||
                product.scientificName.contains(searchQuery, ignoreCase = true) ||
                product.sku.contains(searchQuery, ignoreCase = true)
                
        if (!matchesSearch) return@filter false
        
        if (showOnlyLowStock) {
            // Check if any branch is below threshold for this product
            branches.any { branch ->
                getBranchStock(branch.branchId, product.sku) < lowStockThreshold
            }
        } else {
            true
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "مراقبة المخزون الموحد للفروع 📦",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                            Text(
                                text = "نظرة عامة على كميات الأدوية في كافة منافذ التوزيع",
                                fontSize = 11.sp,
                                color = Color.LightGray
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.testTag("inventory_back_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MedBluePrimary,
                        titleContentColor = Color.White
                    )
                )
            }
        ) { paddingVals ->
            Column(
                modifier = Modifier
                    .padding(paddingVals)
                    .fillMaxSize()
                    .background(Color(0xFFF8FAFC))
            ) {
                // Control and Filter Panel Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Search bar
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            label = { Text("بحث باسم الدواء أو الـ SKU 🔍", fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("search_product_input"),
                            shape = RoundedCornerShape(8.dp)
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Threshold input
                            OutlinedTextField(
                                value = lowStockThresholdStr,
                                onValueChange = { lowStockThresholdStr = it },
                                label = { Text("حد المخزون المنخفض ⚠️", fontSize = 10.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("low_stock_threshold_input"),
                                shape = RoundedCornerShape(8.dp)
                            )
                            
                            // Low stock only filter switch
                            Card(
                                modifier = Modifier.weight(1.2f),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "تنبيه المنخفض فقط 🚨",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MedRedPrimary
                                    )
                                    Switch(
                                        checked = showOnlyLowStock,
                                        onCheckedChange = { showOnlyLowStock = it },
                                        modifier = Modifier
                                            .scale(0.8f)
                                            .testTag("show_low_stock_switch")
                                    )
                                }
                            }
                        }
                    }
                }

                // Main Content
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MedBluePrimary)
                    }
                } else if (filteredProducts.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Inventory,
                                contentDescription = null,
                                tint = Color.LightGray,
                                modifier = Modifier.size(64.dp)
                            )
                            Text(
                                text = "لا توجد أدوية تطابق خيارات البحث والمراقبة الحالية",
                                color = Color.Gray,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(filteredProducts) { product ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("product_card_${product.sku}"),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // Header details
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = product.commercialName,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 14.sp,
                                                color = Color(0xFF0F172A)
                                            )
                                            Text(
                                                text = product.scientificName,
                                                fontSize = 11.sp,
                                                color = Color(0xFF64748B)
                                            )
                                        }
                                        Badge(
                                            containerColor = MedBlueAccent.copy(alpha = 0.1f),
                                            contentColor = MedBluePrimary
                                        ) {
                                            Text(
                                                text = "SKU: ${product.sku}",
                                                modifier = Modifier.padding(4.dp),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    HorizontalDivider(color = Color(0xFFE2E8F0))

                                    // Branch Stock Columns / Rows
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "مستويات المخزون بالفروع 🏬:",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF475569)
                                        )

                                        // Render each branch stock
                                        branches.forEach { branch ->
                                            val stock = getBranchStock(branch.branchId, product.sku)
                                            val isLow = stock < lowStockThreshold
                                            
                                            // Optional: If filter is on, only display branches that are low
                                            if (showOnlyLowStock && !isLow) {
                                                return@forEach
                                            }

                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(
                                                        color = if (isLow) Color(0xFFFEF2F2) else Color(0xFFF8FAFC),
                                                        shape = RoundedCornerShape(8.dp)
                                                    )
                                                    .border(
                                                        width = 1.dp,
                                                        color = if (isLow) Color(0xFFFCA5A5) else Color(0xFFE2E8F0),
                                                        shape = RoundedCornerShape(8.dp)
                                                    )
                                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = if (isLow) Icons.Default.Warning else Icons.Default.Storefront,
                                                        contentDescription = null,
                                                        tint = if (isLow) MedRedPrimary else MedBluePrimary,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Text(
                                                        text = branch.branchName,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF1E293B)
                                                    )
                                                }
                                                
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = "$stock وحدة",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isLow) MedRedPrimary else Color(0xFF0F172A)
                                                    )
                                                    if (isLow) {
                                                        Text(
                                                            text = "(حرج ⚠️)",
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.ExtraBold,
                                                            color = MedRedPrimary
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
                }
            }
        }
    }
}
