package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.service.FirebaseService
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DirectorCatalogManagementScreen(
    currentUser: User,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var productsList by remember { mutableStateOf<List<PharmaProduct>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    // Dialog States
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedProductForEdit by remember { mutableStateOf<PharmaProduct?>(null) } // null means "Add New Product"

    // Load pharma products
    fun loadProducts() {
        isLoading = true
        FirebaseService.getPharmaProducts { products ->
            productsList = products
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        loadProducts()
    }

    val filteredProducts = productsList.filter {
        it.commercialName.contains(searchQuery, ignoreCase = true) ||
                it.scientificName.contains(searchQuery, ignoreCase = true) ||
                it.sku.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "إدارة الكتالوج المركزي للأدوية 👑",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = OnBrandPrimary
                        )
                        Text(
                            "الشركة الأم - الإدارة العامة للشفاء",
                            fontSize = 11.sp,
                            color = OnBrandPrimary.copy(alpha = 0.8f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("catalog_manage_back")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "رجوع", tint = OnBrandPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { loadProducts() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "تحديث", tint = OnBrandPrimary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = BrandPrimary)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    selectedProductForEdit = null
                    showEditDialog = true
                },
                containerColor = BrandPrimary,
                contentColor = OnBrandPrimary,
                modifier = Modifier.testTag("add_product_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "إضافة دواء")
                    Text("إضافة دواء 💊", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { paddingVals ->
        Column(
            modifier = Modifier
                .padding(paddingVals)
                .fillMaxSize()
                .background(SurfaceLight)
        ) {
            // Search field
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(0.dp, 0.dp, 12.dp, 12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.padding(14.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("ابحث بالاسم، المادة الفعالة أو الـ SKU...", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondaryGray) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = null, tint = TextSecondaryGray)
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("catalog_manage_search_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandPrimary,
                            unfocusedBorderColor = Color(0xFFE2E8F0)
                        )
                    )
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = BrandPrimary)
                }
            } else if (filteredProducts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Inventory2, contentDescription = null, modifier = Modifier.size(54.dp), tint = Color.LightGray)
                        Text("لا توجد أصناف تطابق معايير البحث الحالية.", color = TextSecondaryGray, fontSize = 13.sp)
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize().weight(1f)
                ) {
                    items(filteredProducts, key = { it.productId }) { product ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(10.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedProductForEdit = product
                                    showEditDialog = true
                                }
                                .testTag("manage_product_item_${product.productId}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = product.commercialName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = BrandPrimary
                                        )
                                        if (product.isColdChain) {
                                            Box(
                                                modifier = Modifier
                                                    .background(Color(0xFFE0F2FE), RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                                            ) {
                                                Text("مبرد ❄️", color = Color(0xFF0284C7), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                        if (product.isControlledSubstance) {
                                            Box(
                                                modifier = Modifier
                                                    .background(Color(0xFFFEE2E2), RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                                            ) {
                                                Text("مقيد ⚠️", color = ErrorRed, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }

                                    Text(
                                        text = "المادة الفعالة: ${product.scientificName}",
                                        fontSize = 10.sp,
                                        color = TextSecondaryGray
                                    )

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Text(
                                            text = "المصنع: ${product.manufacturer}",
                                            fontSize = 9.sp,
                                            color = TextSecondaryGray
                                        )
                                        Text(
                                            text = "التركيز: ${product.strength}",
                                            fontSize = 9.sp,
                                            color = TextSecondaryGray
                                        )
                                        Text(
                                            text = "العبوة: ${product.unitType}",
                                            fontSize = 9.sp,
                                            color = TextSecondaryGray
                                        )
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Text(
                                            text = "SKU: ${product.sku}",
                                            fontSize = 9.sp,
                                            color = TextSecondaryGray,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = "NDC: ${product.ndcCode}",
                                            fontSize = 9.sp,
                                            color = TextSecondaryGray
                                        )
                                        Text(
                                            text = "القطع بالعلبة: ${product.unitsPerBox}",
                                            fontSize = 9.sp,
                                            color = TextSecondaryGray
                                        )
                                    }
                                }

                                Column(
                                    horizontalAlignment = Alignment.End,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "${product.price} ريال",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 13.sp,
                                        color = BrandPrimary
                                    )

                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        IconButton(
                                            onClick = {
                                                selectedProductForEdit = product
                                                showEditDialog = true
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = WarningAmber, modifier = Modifier.size(16.dp))
                                        }

                                        IconButton(
                                            onClick = {
                                                FirebaseService.deletePharmaProduct(product.productId) { success ->
                                                    if (success) {
                                                        Toast.makeText(context, "🗑️ تم حذف الصنف بنجاح", Toast.LENGTH_SHORT).show()
                                                        loadProducts()
                                                    }
                                                }
                                            },
                                            modifier = Modifier.size(32.dp).testTag("delete_product_${product.productId}")
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "حذف", tint = ErrorRed, modifier = Modifier.size(16.dp))
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

    // Add / Edit Product Dialog
    if (showEditDialog) {
        var commName by remember { mutableStateOf(selectedProductForEdit?.commercialName ?: "") }
        var sciName by remember { mutableStateOf(selectedProductForEdit?.scientificName ?: "") }
        var skuField by remember { mutableStateOf(selectedProductForEdit?.sku ?: "") }
        var ndcField by remember { mutableStateOf(selectedProductForEdit?.ndcCode ?: "") }
        var mfgField by remember { mutableStateOf(selectedProductForEdit?.manufacturer ?: "") }
        var dosageFormSelection by remember { mutableStateOf(selectedProductForEdit?.dosageForm ?: DosageForm.TABLET) }
        var strengthField by remember { mutableStateOf(selectedProductForEdit?.strength ?: "") }
        var unitTypeField by remember { mutableStateOf(selectedProductForEdit?.unitType ?: "") }
        var unitsPerBoxField by remember { mutableStateOf(selectedProductForEdit?.unitsPerBox?.toString() ?: "1") }
        var priceField by remember { mutableStateOf(selectedProductForEdit?.price?.toString() ?: "0.0") }
        var isColdChainCheck by remember { mutableStateOf(selectedProductForEdit?.isColdChain ?: false) }
        var isControlledCheck by remember { mutableStateOf(selectedProductForEdit?.isControlledSubstance ?: false) }
        var descField by remember { mutableStateOf(selectedProductForEdit?.description ?: "") }

        var showDosageDropdown by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = {
                Text(
                    text = if (selectedProductForEdit == null) "إضافة مستحضر طبي جديد 💊" else "تعديل بيانات المستحضر ✏️",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = commName,
                        onValueChange = { commName = it },
                        label = { Text("الاسم التجاري باللغتين 🏷️", fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("dialog_commercial_name")
                    )

                    OutlinedTextField(
                        value = sciName,
                        onValueChange = { sciName = it },
                        label = { Text("الاسم العلمي والمادة الفعالة 🧪", fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("dialog_scientific_name")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = skuField,
                            onValueChange = { skuField = it },
                            label = { Text("كود الـ SKU 📦", fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("dialog_sku")
                        )

                        OutlinedTextField(
                            value = ndcField,
                            onValueChange = { ndcField = it },
                            label = { Text("كود الـ NDC الوطني 📄", fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("dialog_ndc")
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = mfgField,
                            onValueChange = { mfgField = it },
                            label = { Text("الشركة المصنعة 🏢", fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = strengthField,
                            onValueChange = { strengthField = it },
                            label = { Text("التركيز والجرعة ⚡", fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Dosage Form Selector
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = when (dosageFormSelection) {
                                DosageForm.TABLET -> "أقراص (Tablet) 💊"
                                DosageForm.CAPSULE -> "كبسولات (Capsule) 💊"
                                DosageForm.INJECTION -> "حقن (Injection) 💉"
                                DosageForm.SYRUP -> "شراب (Syrup) 🧪"
                                DosageForm.OINTMENT -> "مرهم (Ointment) 🧴"
                                DosageForm.CREAM -> "كريم (Cream) 🧴"
                                DosageForm.SUSPENSION -> "معلق سائل (Suspension)"
                                DosageForm.INHALER -> "بخاخ ربو (Inhaler) 🌬️"
                            },
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("الشكل الصيدلاني 🍬", fontSize = 11.sp) },
                            trailingIcon = {
                                IconButton(onClick = { showDosageDropdown = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            },
                            modifier = Modifier.fillMaxWidth().clickable { showDosageDropdown = true }
                        )

                        DropdownMenu(
                            expanded = showDosageDropdown,
                            onDismissRequest = { showDosageDropdown = false }
                        ) {
                            DosageForm.values().forEach { form ->
                                val formLabel = when (form) {
                                    DosageForm.TABLET -> "أقراص (Tablet) 💊"
                                    DosageForm.CAPSULE -> "كبسولات (Capsule) 💊"
                                    DosageForm.INJECTION -> "حقن (Injection) 💉"
                                    DosageForm.SYRUP -> "شراب (Syrup) 🧪"
                                    DosageForm.OINTMENT -> "مرهم (Ointment) 🧴"
                                    DosageForm.CREAM -> "كريم (Cream) 🧴"
                                    DosageForm.SUSPENSION -> "معلق سائل (Suspension)"
                                    DosageForm.INHALER -> "بخاخ ربو (Inhaler) 🌬️"
                                }
                                DropdownMenuItem(
                                    text = { Text(formLabel, fontSize = 12.sp) },
                                    onClick = {
                                        dosageFormSelection = form
                                        showDosageDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = unitTypeField,
                            onValueChange = { unitTypeField = it },
                            label = { Text("نوع التعبئة 📦", fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = unitsPerBoxField,
                            onValueChange = { unitsPerBoxField = it },
                            label = { Text("القطع بالعلبة 🔢", fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = priceField,
                        onValueChange = { priceField = it },
                        label = { Text("سعر البيع الموحد (ريال) 💵", fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("dialog_price")
                    )

                    OutlinedTextField(
                        value = descField,
                        onValueChange = { descField = it },
                        label = { Text("وصف المستحضر والتحذيرات الطبية 📝", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth().height(80.dp)
                    )

                    // Switches
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("يتطلب سلسلة تبريد دقيقة (2-8 مئوية) ❄️", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Switch(
                            checked = isColdChainCheck,
                            onCheckedChange = { isColdChainCheck = it }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("مادة مخدرة مقيدة (رقابة وزارة الصحة) ⚠️", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Switch(
                            checked = isControlledCheck,
                            onCheckedChange = { isControlledCheck = it }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (commName.isBlank() || sciName.isBlank() || skuField.isBlank()) {
                            Toast.makeText(context, "⚠️ يرجى تعبئة الاسم التجاري والعلمي والـ SKU", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val priceVal = priceField.toDoubleOrNull() ?: 0.0
                        val unitsVal = unitsPerBoxField.toIntOrNull() ?: 1

                        val productToSave = PharmaProduct(
                            productId = selectedProductForEdit?.productId ?: "",
                            sku = skuField.trim(),
                            ndcCode = ndcField.trim(),
                            commercialName = commName.trim(),
                            scientificName = sciName.trim(),
                            manufacturer = mfgField.trim(),
                            dosageForm = dosageFormSelection,
                            strength = strengthField.trim(),
                            isColdChain = isColdChainCheck,
                            isControlledSubstance = isControlledCheck,
                            unitType = unitTypeField.trim(),
                            unitsPerBox = unitsVal,
                            price = priceVal,
                            description = descField.trim()
                        )

                        FirebaseService.savePharmaProduct(productToSave) { success ->
                            if (success) {
                                Toast.makeText(context, "✔️ تم حفظ المستحضر بنجاح", Toast.LENGTH_SHORT).show()
                                showEditDialog = false
                                loadProducts()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary, contentColor = OnBrandPrimary),
                    modifier = Modifier.testTag("dialog_save_btn")
                ) {
                    Text("حفظ المستحضر ✅", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = OnBrandPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("إلغاء ❌", fontSize = 12.sp, color = ErrorRed)
                }
            }
        )
    }
}
