package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.ui.theme.MedBluePrimary
import com.example.ui.theme.MedGreenPrimary
import com.example.ui.theme.MedRedPrimary

// قائمة تجريبية مسبقة بالمنتجات الدوائية والمستحضرات الطبية المتطابقة مع PharmaProduct
val mockPharmaProducts = listOf(
    PharmaProduct(
        productId = "prod_1",
        sku = "AMX-500-CAP",
        ndcCode = "0007-4112-20",
        commercialName = "أموكسيسيلين 500 ملجم (أمبيسيل)",
        scientificName = "Amoxicillin Trihydrate",
        manufacturer = "الشركة اليمنية لصناعة الأدوية (يدكو)",
        dosageForm = DosageForm.CAPSULE,
        strength = "500 mg",
        isColdChain = false,
        isControlledSubstance = false,
        unitType = "صندوق (3 شريط)",
        unitsPerBox = 30,
        price = 1500.0,
        description = "مضاد حيوي واسع الطيف لعلاج الالتهابات البكتيرية الحادة."
    ),
    PharmaProduct(
        productId = "prod_2",
        sku = "INS-ACT-INJ",
        ndcCode = "0169-1834-11",
        commercialName = "إنسولين أكتRapid مبرد 💉",
        scientificName = "Insulin Human (rDNA)",
        manufacturer = "Novo Nordisk",
        dosageForm = DosageForm.INJECTION,
        strength = "100 IU/ml",
        isColdChain = true,
        isControlledSubstance = false,
        unitType = "فيال (Vial)",
        unitsPerBox = 1,
        price = 8500.0,
        description = "إنسولين سريع المفعول للتحكم بالسكري. يحفظ في درجة حرارة 2-8 مئوية."
    ),
    PharmaProduct(
        productId = "prod_3",
        sku = "FEN-50-INJ",
        ndcCode = "50458-038-10",
        commercialName = "فنتانيل حقن مخدرة ⚠️ (مقيد)",
        scientificName = "Fentanyl Citrate",
        manufacturer = "Janssen-Cilag",
        dosageForm = DosageForm.INJECTION,
        strength = "50 mcg/ml",
        isColdChain = false,
        isControlledSubstance = true,
        unitType = "أمبولة",
        unitsPerBox = 5,
        price = 12000.0,
        description = "مسكن ألم أفيوني قوي جداً للعمليات الجراحية. خاضع للرقابة الصارمة."
    ),
    PharmaProduct(
        productId = "prod_4",
        sku = "PAR-500-TAB",
        ndcCode = "0012-4001-50",
        commercialName = "باراسيتامول 500 ملجم الشفاء",
        scientificName = "Paracetamol",
        manufacturer = "مجموعة الشفاء الدوائية",
        dosageForm = DosageForm.TABLET,
        strength = "500 mg",
        isColdChain = false,
        isControlledSubstance = false,
        unitType = "كرتون (10 شريط)",
        unitsPerBox = 100,
        price = 800.0,
        description = "خافض حرارة ومسكن للآلام الخفيفة والمتوسطة."
    ),
    PharmaProduct(
        productId = "prod_5",
        sku = "CEF-1G-INJ",
        ndcCode = "0781-3204-95",
        commercialName = "سيف ترياكسون 1 جرام حقن",
        scientificName = "Ceftriaxone Sodium",
        manufacturer = "Sandoz",
        dosageForm = DosageForm.INJECTION,
        strength = "1 g",
        isColdChain = false,
        isControlledSubstance = false,
        unitType = "فيال + مذيب",
        unitsPerBox = 1,
        price = 3500.0,
        description = "مضاد حيوي قوي من الجيل الثالث للسيفالوسبورينات."
    ),
    PharmaProduct(
        productId = "prod_6",
        sku = "ATO-20-TAB",
        ndcCode = "0071-0156-23",
        commercialName = "أتورفاستاتين 20 ملجم (ليبيتور)",
        scientificName = "Atorvastatin Calcium",
        manufacturer = "Pfizer",
        dosageForm = DosageForm.TABLET,
        strength = "20 mg",
        isColdChain = false,
        isControlledSubstance = false,
        unitType = "شريطين",
        unitsPerBox = 28,
        price = 4200.0,
        description = "خافض للكوليسترول والدهون الثلاثية للوقاية من أمراض القلب."
    ),
    PharmaProduct(
        productId = "prod_7",
        sku = "ATO-10-TAB",
        ndcCode = "0071-0155-23",
        commercialName = "أتورفاستاتين 10 ملجم (منتهي)",
        scientificName = "Atorvastatin Calcium",
        manufacturer = "Pfizer",
        dosageForm = DosageForm.TABLET,
        strength = "10 mg",
        isColdChain = false,
        isControlledSubstance = false,
        unitType = "شريطين",
        unitsPerBox = 28,
        price = 3900.0,
        description = "مخزون نافد تماماً من المستودعات حالياً."
    ),
    PharmaProduct(
        productId = "prod_8",
        sku = "VEN-100-INH",
        ndcCode = "0173-0321-88",
        commercialName = "فنتولين بخاخ للربو 🌬️",
        scientificName = "Salbutamol Inhaler",
        manufacturer = "GlaxoSmithKline",
        dosageForm = DosageForm.INHALER,
        strength = "100 mcg",
        isColdChain = false,
        isControlledSubstance = false,
        unitType = "جهاز استنشاق",
        unitsPerBox = 1,
        price = 5000.0,
        description = "موسع للقصبات الهوائية سريع المفعول لنوبات الربو وضيق التنفس."
    ),
    PharmaProduct(
        productId = "prod_9",
        sku = "AUG-312-SYR",
        ndcCode = "0173-0315-10",
        commercialName = "أوجمنتين شراب معلق للأطفال",
        scientificName = "Amoxicillin / Clavulanate",
        manufacturer = "GSK",
        dosageForm = DosageForm.SYRUP,
        strength = "312 mg / 5ml",
        isColdChain = false,
        isControlledSubstance = false,
        unitType = "زجاجة معلق سائل",
        unitsPerBox = 1,
        price = 4800.0,
        description = "مضاد حيوي مركب للأطفال لعلاج التهاب اللوزتين والأذن الوسطى."
    )
)

// دالة لمعرفة حالة مخزون الصنف بشكل افتراضي
fun getMockInventoryStatus(productId: String): InventoryStatus {
    return when (productId) {
        "prod_7" -> InventoryStatus.OUT_OF_STOCK
        "prod_3" -> InventoryStatus.LOW_STOCK
        "prod_9" -> InventoryStatus.EXPECTED_SOON
        else -> InventoryStatus.AVAILABLE
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductCatalogScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCart: () -> Unit,
    cartItems: MutableList<CartItem>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedDosageForm by remember { mutableStateOf<DosageForm?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "كتالوج المستحضرات والأدوية 💊",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                        Text(
                            "مجموعة الشفاء الدوائية B2B",
                            fontSize = 11.sp,
                            color = Color.LightGray
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("catalog_back")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "رجوع", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToCart, modifier = Modifier.testTag("catalog_view_cart")) {
                        BadgedBox(
                            badge = {
                                if (cartItems.isNotEmpty()) {
                                    Badge(
                                        containerColor = MedGreenPrimary,
                                        contentColor = Color.White,
                                        modifier = Modifier.testTag("cart_badge_count")
                                    ) {
                                        Text(cartItems.sumOf { it.quantity }.toString(), fontSize = 10.sp)
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = "سلة المشتريات", tint = Color.White)
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MedBluePrimary)
            )
        }
    ) { paddingVals ->
        Column(
            modifier = Modifier
                .padding(paddingVals)
                .fillMaxSize()
                .background(Color(0xFFF1F5F9)) // خلفية مؤسسية رمادية فاتحة جداً
        ) {
            // 🔍 شريط البحث الاحترافي (Search Bar)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(0.dp, 0.dp, 12.dp, 12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("ابحث بالاسم التجاري، الاسم العلمي أو الـ SKU...", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = null, tint = Color.Gray)
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("catalog_search_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MedBluePrimary,
                            unfocusedBorderColor = Color.LightGray
                        )
                    )

                    // 📑 أزرار الفلاتر (Filter Chips) الأفقية للأشكال الصيدلانية
                    Text("الشكل الصيدلاني:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            FilterChip(
                                selected = selectedDosageForm == null,
                                onClick = { selectedDosageForm = null },
                                label = { Text("الكل 🌐", fontSize = 11.sp) },
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.testTag("filter_dosage_all")
                            )
                        }
                        items(DosageForm.values()) { form ->
                            val label = when (form) {
                                DosageForm.TABLET -> "أقراص 💊"
                                DosageForm.CAPSULE -> "كبسولات 💊"
                                DosageForm.INJECTION -> "حقن 💉"
                                DosageForm.SYRUP -> "شراب 🧪"
                                DosageForm.OINTMENT -> "مرهم 🧴"
                                DosageForm.CREAM -> "كريم 🧴"
                                DosageForm.SUSPENSION -> "معلق سائل"
                                DosageForm.INHALER -> "بخاخ ربو 🌬️"
                            }
                            FilterChip(
                                selected = selectedDosageForm == form,
                                onClick = { selectedDosageForm = form },
                                label = { Text(label, fontSize = 11.sp) },
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.testTag("filter_dosage_${form.name}")
                            )
                        }
                    }
                }
            }

            // 📦 قائمة الأصناف والمستحضرات الدوائية المفلترة
            val filteredProducts = mockPharmaProducts.filter { product ->
                val matchesSearch = product.commercialName.contains(searchQuery, ignoreCase = true) ||
                        product.scientificName.contains(searchQuery, ignoreCase = true) ||
                        product.sku.contains(searchQuery, ignoreCase = true)
                val matchesForm = selectedDosageForm == null || product.dosageForm == selectedDosageForm
                matchesSearch && matchesForm
            }

            if (filteredProducts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(54.dp), tint = Color.LightGray)
                        Text("لا توجد مستحضرات تطابق معايير البحث الحالية.", color = Color.Gray, fontSize = 13.sp)
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize().weight(1f)
                ) {
                    items(filteredProducts) { product ->
                        val invStatus = getMockInventoryStatus(product.productId)
                        val isOutOfStock = invStatus == InventoryStatus.OUT_OF_STOCK

                        // تلوين كرت المنتج لو كان منتهي الصلاحية أو نفد
                        val cardBg = if (isOutOfStock) Color(0xFFF1F5F9) else Color.White
                        val textColor = if (isOutOfStock) Color.Gray else Color.DarkGray
                        val primaryColor = if (isOutOfStock) Color.Gray else MedBluePrimary

                        Card(
                            colors = CardDefaults.cardColors(containerColor = cardBg),
                            shape = RoundedCornerShape(10.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("product_item_${product.productId}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // معلومات المنتج التفصيلية
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
                                            color = primaryColor
                                        )

                                        // علامة سلسلة التبريد الدقيقة ❄️
                                        if (product.isColdChain) {
                                            Box(
                                                modifier = Modifier
                                                    .background(Color(0xFFE0F2FE), RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Default.AcUnit,
                                                        contentDescription = "سلسلة تبريد",
                                                        tint = Color(0xFF0284C7),
                                                        modifier = Modifier.size(10.dp)
                                                    )
                                                    Text("مبرد ❄️", color = Color(0xFF0284C7), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }

                                        // علامة دواء مقيد ومراقب ⚠️
                                        if (product.isControlledSubstance) {
                                            Box(
                                                modifier = Modifier
                                                    .background(Color(0xFFFEE2E2), RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                                            ) {
                                                Text("مقيد ⚠️", color = MedRedPrimary, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }

                                    Text(
                                        text = "الاسم العلمي: ${product.scientificName}",
                                        fontSize = 10.sp,
                                        color = Color.Gray
                                    )

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Text(
                                            text = "المصنع: ${product.manufacturer}",
                                            fontSize = 9.sp,
                                            color = Color.Gray
                                        )
                                        Text(
                                            text = "التركيز: ${product.strength}",
                                            fontSize = 9.sp,
                                            color = Color.Gray
                                        )
                                        Text(
                                            text = "العبوة: ${product.unitType} (${product.unitsPerBox} وحدة)",
                                            fontSize = 9.sp,
                                            color = Color.Gray
                                        )
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // كود الـ SKU للتعرف السريع
                                        Text(
                                            text = "SKU: ${product.sku}",
                                            fontSize = 9.sp,
                                            color = Color.Gray,
                                            fontWeight = FontWeight.Medium
                                        )

                                        // بادج حالة المخزون
                                        val (statusText, statusColor) = when (invStatus) {
                                            InventoryStatus.AVAILABLE -> Pair("متوفر للشراء", MedGreenPrimary)
                                            InventoryStatus.LOW_STOCK -> Pair("مخزون منخفض ⚠️", Color(0xFFD97706))
                                            InventoryStatus.OUT_OF_STOCK -> Pair("نفد ❌", MedRedPrimary)
                                            InventoryStatus.EXPECTED_SOON -> Pair("قريباً ⏳", Color(0xFF0284C7))
                                        }
                                        Box(
                                            modifier = Modifier
                                                .background(statusColor.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 1.dp)
                                        ) {
                                            Text(statusText, color = statusColor, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                // السعر وأزرار التحكم بالطلب والسلة
                                Column(
                                    horizontalAlignment = Alignment.End,
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = if (isOutOfStock) "غير متاح" else "${product.price} YER",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 13.sp,
                                        color = if (isOutOfStock) Color.Gray else MedBluePrimary
                                    )

                                    IconButton(
                                        onClick = {
                                            if (isOutOfStock) {
                                                Toast.makeText(context, "هذا الصنف غير متوفر حالياً بالمخزن", Toast.LENGTH_SHORT).show()
                                                return@IconButton
                                            }
                                            val existing = cartItems.find { it.product.productId == product.productId }
                                            if (existing != null) {
                                                val idx = cartItems.indexOf(existing)
                                                cartItems[idx] = existing.copy(quantity = existing.quantity + 1)
                                            } else {
                                                cartItems.add(CartItem(product = product, quantity = 1, addedPrice = product.price))
                                            }
                                            Toast.makeText(context, "✔️ تمت إضافة ${product.commercialName} للسلة", Toast.LENGTH_SHORT).show()
                                        },
                                        enabled = !isOutOfStock,
                                        modifier = Modifier
                                            .size(34.dp)
                                            .background(
                                                if (isOutOfStock) Color.LightGray else MedBluePrimary,
                                                CircleShape
                                            )
                                            .testTag("add_to_cart_btn_${product.productId}")
                                    ) {
                                        Icon(
                                            Icons.Default.Add,
                                            contentDescription = "إضافة للسلة",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
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
