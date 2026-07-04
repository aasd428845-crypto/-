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
    var activeTab by remember { mutableStateOf("products") } // "products" or "promotions"
    var searchQuery by remember { mutableStateOf("") }
    var productsList by remember { mutableStateOf<List<PharmaProduct>>(emptyList()) }
    var promoList by remember { mutableStateOf<List<PromotionalOffer>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    // Dialog States
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedProductForEdit by remember { mutableStateOf<PharmaProduct?>(null) } // null means "Add New Product"

    var showPromoEditDialog by remember { mutableStateOf(false) }
    var selectedPromoForEdit by remember { mutableStateOf<PromotionalOffer?>(null) } // null means "Add New Promo"

    // Load pharma products and promotional campaigns
    fun loadProducts() {
        isLoading = true
        FirebaseService.getPharmaProducts { products ->
            productsList = products
            // Load promotions from the mock / Firestore system
            promoList = FirebaseService.fallbackPromotionalOffers.toList()
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

    val filteredPromos = promoList.filter {
        it.title.contains(searchQuery, ignoreCase = true) ||
                it.productName.contains(searchQuery, ignoreCase = true) ||
                it.description.contains(searchQuery, ignoreCase = true)
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
                    if (activeTab == "products") {
                        selectedProductForEdit = null
                        showEditDialog = true
                    } else {
                        selectedPromoForEdit = null
                        showPromoEditDialog = true
                    }
                },
                containerColor = BrandPrimary,
                contentColor = OnBrandPrimary,
                modifier = Modifier.testTag("catalog_manage_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (activeTab == "products") {
                        Icon(Icons.Default.Add, contentDescription = "إضافة دواء")
                        Text("إضافة دواء 💊", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Icon(Icons.Default.Add, contentDescription = "إضافة عرض ترويجي")
                        Text("إضافة عرض ترويجي 📣", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
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
            // Segmented tab navigation
            TabRow(
                selectedTabIndex = if (activeTab == "products") 0 else 1,
                containerColor = Color.White,
                contentColor = BrandPrimary
            ) {
                Tab(
                    selected = activeTab == "products",
                    onClick = { 
                        activeTab = "products"
                        searchQuery = ""
                    },
                    modifier = Modifier.testTag("tab_products")
                ) {
                    Text(
                        text = "الأدوية والمستلزمات 💊",
                        modifier = Modifier.padding(14.dp),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (activeTab == "products") BrandPrimary else TextSecondaryGray
                    )
                }
                Tab(
                    selected = activeTab == "promotions",
                    onClick = { 
                        activeTab = "promotions"
                        searchQuery = ""
                    },
                    modifier = Modifier.testTag("tab_promotions")
                ) {
                    Text(
                        text = "العروض والخصومات 📣",
                        modifier = Modifier.padding(14.dp),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (activeTab == "promotions") BrandPrimary else TextSecondaryGray
                    )
                }
            }

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
                        placeholder = { 
                            Text(
                                if (activeTab == "products") "ابحث بالاسم، المادة الفعالة أو الـ SKU..." 
                                else "ابحث باسم العرض، المنتج أو الوصف...", 
                                fontSize = 12.sp
                            ) 
                        },
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

            if (activeTab == "products") {
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
            } else if (activeTab == "promotions") {
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = BrandPrimary)
                    }
                } else if (filteredPromos.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(54.dp), tint = Color.LightGray)
                            Text("لا توجد عروض ترويجية مطابقة حالياً.", color = TextSecondaryGray, fontSize = 13.sp)
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize().weight(1f)
                    ) {
                        items(filteredPromos, key = { it.offerId }) { offer ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(10.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedPromoForEdit = offer
                                        showPromoEditDialog = true
                                    }
                                    .testTag("manage_promo_item_${offer.offerId}")
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
                                                text = offer.title,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = BrandPrimary
                                            )
                                            if (offer.isActive) {
                                                Box(
                                                    modifier = Modifier
                                                        .background(Color(0xFFDCFCE7), RoundedCornerShape(4.dp))
                                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                                ) {
                                                    Text("نشط 🟢", color = Color(0xFF15803D), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                                }
                                            } else {
                                                Box(
                                                    modifier = Modifier
                                                        .background(Color(0xFFF1F5F9), RoundedCornerShape(4.dp))
                                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                                ) {
                                                    Text("موقف 🔴", color = Color(0xFF475569), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }

                                        Text(
                                            text = "المنتج: ${offer.productName}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = OnSurfaceDark
                                        )

                                        Text(
                                            text = offer.description,
                                            fontSize = 10.sp,
                                            color = TextSecondaryGray,
                                            lineHeight = 14.sp
                                        )

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            if (offer.targetGovernorate.isNotEmpty()) {
                                                Text(
                                                    text = "المحافظة: ${offer.targetGovernorate} 📍",
                                                    fontSize = 9.sp,
                                                    color = MedBluePrimary,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            } else {
                                                Text(
                                                    text = "المحافظة: كل المحافظات 🌍",
                                                    fontSize = 9.sp,
                                                    color = TextSecondaryGray
                                                )
                                            }
                                            
                                            if (offer.discountPercent > 0.0) {
                                                Text(
                                                    text = "الخصم: ${offer.discountPercent.toInt()}%",
                                                    fontSize = 9.sp,
                                                    color = ErrorRed,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }

                                    Column(
                                        horizontalAlignment = Alignment.End,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        if (offer.specialPrice > 0.0) {
                                            Text(
                                                text = "${offer.specialPrice.toInt()} ر.ي",
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 13.sp,
                                                color = MedGreenPrimary
                                            )
                                        } else {
                                            Text(
                                                text = "نسبة خصم",
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 11.sp,
                                                color = TextSecondaryGray
                                            )
                                        }

                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            IconButton(
                                                onClick = {
                                                    selectedPromoForEdit = offer
                                                    showPromoEditDialog = true
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = WarningAmber, modifier = Modifier.size(16.dp))
                                            }

                                            IconButton(
                                                onClick = {
                                                    FirebaseService.deleteOffer(offer.offerId) { success ->
                                                        if (success) {
                                                            Toast.makeText(context, "🗑️ تم حذف العرض بنجاح", Toast.LENGTH_SHORT).show()
                                                            loadProducts()
                                                        }
                                                    }
                                                },
                                                modifier = Modifier.size(32.dp).testTag("delete_promo_${offer.offerId}")
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

    // Add / Edit Promotional Offer Dialog
    if (showPromoEditDialog) {
        var promoTitle by remember { mutableStateOf(selectedPromoForEdit?.title ?: "") }
        var promoDesc by remember { mutableStateOf(selectedPromoForEdit?.description ?: "") }
        var selectedProdId by remember { mutableStateOf(selectedPromoForEdit?.productId ?: "") }
        var discountValField by remember { mutableStateOf(selectedPromoForEdit?.discountPercent?.toInt()?.toString() ?: "0") }
        var specialPriceField by remember { mutableStateOf(selectedPromoForEdit?.specialPrice?.toInt()?.toString() ?: "0") }
        var targetGovField by remember { mutableStateOf(selectedPromoForEdit?.targetGovernorate ?: "") }
        var isPromoActiveCheck by remember { mutableStateOf(selectedPromoForEdit?.isActive ?: true) }

        var showProductDropdown by remember { mutableStateOf(false) }

        // Find currently selected product commercial name
        val selectedProductObj = productsList.find { it.productId == selectedProdId }
        val selectedProductLabel = selectedProductObj?.commercialName ?: "اختر صنف دواء مرتبط 💊"

        AlertDialog(
            onDismissRequest = { showPromoEditDialog = false },
            title = {
                Text(
                    text = if (selectedPromoForEdit == null) "إضافة حملة ترويجية جديدة 📣" else "تعديل الحملة الترويجية ✏️",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = BrandPrimary,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = promoTitle,
                        onValueChange = { promoTitle = it },
                        label = { Text("عنوان الحملة الترويجية (مثال: عرض الصيف الخاص) ✍️", fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("dialog_promo_title")
                    )

                    OutlinedTextField(
                        value = promoDesc,
                        onValueChange = { promoDesc = it },
                        label = { Text("تفاصيل العرض والمميزات 📝", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth().height(80.dp)
                    )

                    // Product Selector dropdown
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedProductLabel,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("الصنف الدوائي المرتبط للعرض 💊", fontSize = 11.sp) },
                            trailingIcon = {
                                IconButton(onClick = { showProductDropdown = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showProductDropdown = true }
                        )

                        DropdownMenu(
                            expanded = showProductDropdown,
                            onDismissRequest = { showProductDropdown = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            productsList.forEach { product ->
                                DropdownMenuItem(
                                    text = { Text("${product.commercialName} (${product.scientificName})", fontSize = 11.sp) },
                                    onClick = {
                                        selectedProdId = product.productId
                                        showProductDropdown = false
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
                            value = discountValField,
                            onValueChange = { discountValField = it },
                            label = { Text("نسبة الخصم % 🏷️", fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = specialPriceField,
                            onValueChange = { specialPriceField = it },
                            label = { Text("سعر خاص مباشر (ريال) 💰", fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = targetGovField,
                        onValueChange = { targetGovField = it },
                        label = { Text("المحافظة المستهدفة (اتركه فارغاً لكل المحافظات) 📍", fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("حالة العرض نشط حالياً 🟢", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Switch(
                            checked = isPromoActiveCheck,
                            onCheckedChange = { isPromoActiveCheck = it }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (promoTitle.isBlank() || selectedProdId.isBlank()) {
                            Toast.makeText(context, "⚠️ يرجى إدخال عنوان العرض وتحديد صنف دواء مرتبط", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val discPct = discountValField.toDoubleOrNull() ?: 0.0
                        val specPrice = specialPriceField.toDoubleOrNull() ?: 0.0
                        val associatedProduct = productsList.find { it.productId == selectedProdId }

                        val offerToSave = PromotionalOffer(
                            offerId = selectedPromoForEdit?.offerId ?: "",
                            productId = selectedProdId,
                            productName = associatedProduct?.commercialName ?: "صنف طبي",
                            title = promoTitle.trim(),
                            description = promoDesc.trim(),
                            discountPercent = discPct,
                            specialPrice = specPrice,
                            targetGovernorate = targetGovField.trim(),
                            startDate = selectedPromoForEdit?.startDate ?: System.currentTimeMillis(),
                            endDate = selectedPromoForEdit?.endDate ?: (System.currentTimeMillis() + 7 * 24 * 3600 * 1000L),
                            isActive = isPromoActiveCheck
                        )

                        FirebaseService.createOffer(offerToSave) { success ->
                            if (success) {
                                Toast.makeText(context, "✔️ تم حفظ الحملة الترويجية بنجاح", Toast.LENGTH_SHORT).show()
                                showPromoEditDialog = false
                                loadProducts() // reload promotions list
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary, contentColor = OnBrandPrimary),
                    modifier = Modifier.testTag("dialog_save_promo_btn")
                ) {
                    Text("حفظ الحملة ✅", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = OnBrandPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPromoEditDialog = false }) {
                    Text("إلغاء ❌", fontSize = 12.sp, color = ErrorRed)
                }
            }
        )
    }
}
