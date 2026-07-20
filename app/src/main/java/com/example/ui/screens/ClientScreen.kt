package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.model.BranchOffer
import com.example.model.Order
import com.example.model.User
import com.example.model.UserAddress
import com.example.model.CartItem
import com.example.model.Invoice
import com.example.model.PaymentStatus
import com.example.model.PaymentTerms
import com.example.model.ClientAccount
import com.example.model.OrderStatus
import com.example.model.PharmaProduct
import com.example.model.PromotionalOffer
import com.example.service.FirebaseService
import com.example.ui.theme.*
import com.example.utils.calculateReorderSuggestions
import com.example.utils.ReorderUrgency
import com.example.utils.ReorderSuggestion
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun ClientScreen(
    currentUser: User,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val cartItems = remember { mutableStateListOf<CartItem>() }
    var activeTab by remember { mutableStateOf("new_order") } // "new_order", "my_orders", or "account"
    var clientScreenState by remember { mutableStateOf("dashboard") } // "dashboard", "new_order_flow", "digital_card_view", "addresses", "add_address", "edit_address"

    // Address Management States
    var defaultAddress by remember { mutableStateOf<UserAddress?>(null) }
    var editingAddress by remember { mutableStateOf<UserAddress?>(null) }

    fun refreshDefaultAddress() {
        FirebaseService.getUserAddresses(currentUser.userId) { list ->
            defaultAddress = list.find { it.isDefault } ?: list.firstOrNull()
        }
    }

    // Form States
    var orderContent by remember { mutableStateOf("") }
    var urgencyLevel by remember { mutableStateOf("normal") } // "normal", "high", "critical"
    var broadcastType by remember { mutableStateOf("all") } // "all", "nearby", "selected"
    var selectedBranches by remember { mutableStateOf<List<String>>(emptyList()) }
    var isSubmitting by remember { mutableStateOf(false) }

    // Lists and Financial States
    var myOrders by remember { mutableStateOf<List<Order>>(emptyList()) }
    var pharmaProducts by remember { mutableStateOf<List<PharmaProduct>>(emptyList()) }
    var activePromotions by remember { mutableStateOf<List<PromotionalOffer>>(emptyList()) }
    var activeOffersMap by remember { mutableStateOf<Map<String, List<BranchOffer>>>(emptyMap()) }
    var selectedOrderForOffers by remember { mutableStateOf<Order?>(null) }
    var showOffersDialog by remember { mutableStateOf(false) }

    var clientAccountState by remember { mutableStateOf(currentUser.clientAccount) }
    var clientInvoices by remember { mutableStateOf<List<Invoice>>(emptyList()) }
    var clientProfileState by remember { mutableStateOf<com.example.model.ClientProfile?>(null) }
    var dbError by remember { mutableStateOf<String?>(null) }

    fun refreshProfile() {
        FirebaseService.getClientProfile(currentUser.userId) { profile ->
            clientProfileState = profile
        }
    }

    // Refresh function
    fun refreshOrders() {
        dbError = FirebaseService.lastDatabaseError
        FirebaseService.getOrders { allOrders ->
            myOrders = allOrders.filter { it.clientId == currentUser.userId }.sortedByDescending { it.createdAt }
            // Fetch offers for each order
            val tempMap = mutableMapOf<String, List<BranchOffer>>()
            myOrders.forEach { order ->
                FirebaseService.getBranchOffersForOrder(order.orderId) { offers ->
                    tempMap[order.orderId] = offers
                }
            }
            activeOffersMap = tempMap
        }

        // Fetch client specific invoices
        FirebaseService.getClientInvoices(currentUser.userId) { invoices ->
            clientInvoices = invoices.sortedByDescending { it.issuedAt }
        }

        // Fetch updated client account status
        FirebaseService.getClientAccountStatus(currentUser.userId) { account ->
            if (account != null) {
                clientAccountState = account
            }
        }

        // Fetch pharma products catalog
        FirebaseService.getPharmaProducts { products ->
            pharmaProducts = products
        }

        // Fetch active promotions
        FirebaseService.getActiveOffers(currentUser.governorate) { promotions ->
            activePromotions = promotions
        }
    }

    LaunchedEffect(currentUser.userId) {
        refreshOrders()
        refreshDefaultAddress()
        refreshProfile()
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        when (clientScreenState) {
            "order_detail" -> {
                if (selectedOrderForOffers != null) {
                    ClientOrderDetailScreen(
                        order = selectedOrderForOffers!!,
                        offers = activeOffersMap[selectedOrderForOffers!!.orderId] ?: emptyList(),
                        onBackClick = { clientScreenState = "dashboard" }
                    )
                } else {
                    clientScreenState = "dashboard"
                }
            }
            "order_offers" -> {
                if (selectedOrderForOffers != null) {
                    ClientOrderOffersScreen(
                        order = selectedOrderForOffers!!,
                        offers = activeOffersMap[selectedOrderForOffers!!.orderId] ?: emptyList(),
                        onBackClick = { clientScreenState = "dashboard" },
                        onRefresh = {
                            refreshOrders()
                        }
                    )
                } else {
                    clientScreenState = "dashboard"
                }
            }
            "new_order_flow" -> {
                ProductCatalogScreen(
                    onNavigateBack = { clientScreenState = "dashboard" },
                    onNavigateToCart = { clientScreenState = "cart" },
                    cartItems = cartItems,
                    branchId = clientProfileState?.assignedBranchId ?: ""
                )
            }
            "cart" -> {
                CartScreen(
                    currentUser = currentUser,
                    cartItems = cartItems,
                    onNavigateBack = { clientScreenState = "new_order_flow" },
                    onCheckoutSuccess = {
                        clientScreenState = "dashboard"
                        activeTab = "my_orders"
                        refreshOrders()
                    }
                )
            }
            "digital_card_view" -> {
                ClientDigitalCard(
                    userId = currentUser.userId,
                    onNavigateBack = { clientScreenState = "dashboard" }
                )
            }
            "addresses" -> {
                ClientAddressesScreen(
                    currentUser = currentUser,
                    onNavigateBack = { 
                        clientScreenState = "dashboard"
                        refreshDefaultAddress()
                    },
                    onAddNewAddress = { clientScreenState = "add_address" },
                    onEditAddress = { address ->
                        editingAddress = address
                        clientScreenState = "edit_address"
                    }
                )
            }
            "add_address" -> {
                AddAddressScreen(
                    currentUser = currentUser,
                    existingAddress = null,
                    onBackClick = { clientScreenState = "addresses" },
                    onSaveSuccess = { clientScreenState = "addresses" }
                )
            }
            "edit_address" -> {
                AddAddressScreen(
                    currentUser = currentUser,
                    existingAddress = editingAddress,
                    onBackClick = { clientScreenState = "addresses" },
                    onSaveSuccess = { clientScreenState = "addresses" }
                )
            }
            else -> { // "dashboard"
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Column {
                                    Text("ميد-لينك | بوابة العميل 🏥", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                                    Text("${currentUser.orgName} (${currentUser.city})", fontSize = 11.sp, color = Color.LightGray)
                                }
                            },
                            actions = {
                                IconButton(onClick = { clientScreenState = "cart" }, modifier = Modifier.testTag("dashboard_view_cart")) {
                                    BadgedBox(
                                        badge = {
                                            if (cartItems.isNotEmpty()) {
                                                Badge(
                                                    containerColor = MedGreenPrimary,
                                                    contentColor = Color.White
                                                ) {
                                                    Text(cartItems.sumOf { it.quantity }.toString(), fontSize = 10.sp)
                                                }
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Default.ShoppingCart, contentDescription = "سلة المشتريات", tint = Color.White)
                                    }
                                }
                                IconButton(onClick = { refreshOrders() }) {
                                    Icon(Icons.Default.Refresh, contentDescription = "تحديث", tint = Color.White)
                                }
                                TextButton(onClick = onLogout) {
                                    Text("خروج 🚪", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(containerColor = MedBluePrimary, titleContentColor = Color.White)
                        )
                    }
                ) { paddingVals ->
                    Column(
                        modifier = Modifier
                            .padding(paddingVals)
                            .fillMaxSize()
                            .background(Color(0xFFF8FAFC))
                    ) {
                        // ⚠️ Error Banner
                        if (dbError != null) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3F3)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("⚠️", fontSize = 18.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = dbError!!,
                                        color = Color(0xFFDC2626),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        // Tab Switcher
                        TabRow(
                            selectedTabIndex = when (activeTab) {
                                "new_order" -> 0
                                "my_orders" -> 1
                                "financial" -> 2
                                "account" -> 3
                            },
                            containerColor = Color.White,
                            contentColor = MedBluePrimary
                        ) {
                            Tab(
                                selected = activeTab == "new_order",
                                onClick = { activeTab = "new_order" },
                                text = { Text("طلب جديد ✍️", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                            )
                            Tab(
                                selected = activeTab == "my_orders",
                                onClick = { activeTab = "my_orders" },
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("طلباتي وتتبع الشحن 📋", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        if (myOrders.isNotEmpty()) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Badge(containerColor = MedGreenPrimary) {
                                                Text(myOrders.size.toString(), color = Color.White, fontSize = 9.sp)
                                            }
                                        }
                                    }
                                }
                            )
                            Tab(
                                selected = activeTab == "financial",
                                onClick = { activeTab = "financial" },
                                text = { Text("حسابي ومالي 🧾", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                            )
                            Tab(
                                selected = activeTab == "account",
                                onClick = { activeTab = "account" },
                                text = { Text("الحساب 👤", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                            )
                        }

                        AnimatedContent(
                            targetState = activeTab,
                            label = "TabTransition",
                            modifier = Modifier.weight(1f)
                        ) { tab ->
                            when (tab) {
                                "new_order" -> {
                                    LazyColumn(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        // High impact welcome hero banner
                                        item {
                                            Card(
                                                colors = CardDefaults.cardColors(containerColor = MedBluePrimary, contentColor = Color.White),
                                                shape = RoundedCornerShape(16.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Column(
                                                    modifier = Modifier.padding(20.dp),
                                                    horizontalAlignment = Alignment.End
                                                ) {
                                                    Text(
                                                        "بوابة الشفاء للإمداد الدوائي الذكي 🏥",
                                                        color = Color.White,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        fontSize = 18.sp,
                                                        textAlign = TextAlign.Right
                                                    )
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        "تحكم بكافة طلبيات منشأتك الصحية وتواصل مباشرة مع فروع المجموعة لضمان أفضل عروض الأسعار وتبريد الأدوية.",
                                                        color = Color.LightGray,
                                                        fontSize = 12.sp,
                                                        textAlign = TextAlign.Right,
                                                        lineHeight = 20.sp
                                                    )
                                                }
                                            }
                                        }

                                        if (activePromotions.isNotEmpty()) {
                                            item {
                                                Column(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalAlignment = Alignment.Start
                                                ) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            text = "📣 عروض ترويجية حصرية وحملات خصم",
                                                            fontSize = 14.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = OnSurfaceDark
                                                        )
                                                        Box(
                                                            modifier = Modifier
                                                                .background(WarningAmber.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                                        ) {
                                                            Text(
                                                                text = "مباشر من المندوب",
                                                                color = WarningAmber,
                                                                fontSize = 10.sp,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        }
                                                    }
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = "وفر الآن مع أسعار مخفضة وخصومات مباشرة من فروع الشفاء لطلبك القادم:",
                                                        fontSize = 11.sp,
                                                        color = TextSecondaryGray,
                                                        textAlign = TextAlign.Right
                                                     )
                                                    Spacer(modifier = Modifier.height(10.dp))
                                                    LazyRow(
                                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                                        contentPadding = PaddingValues(vertical = 4.dp)
                                                    ) {
                                                        items(activePromotions) { offer ->
                                                            val product = pharmaProducts.find { it.productId == offer.productId }
                                                            Card(
                                                                modifier = Modifier
                                                                    .width(280.dp)
                                                                    .border(
                                                                        width = 1.dp,
                                                                        color = BrandPrimary.copy(alpha = 0.2f),
                                                                        shape = RoundedCornerShape(16.dp)
                                                                    ),
                                                                shape = RoundedCornerShape(16.dp),
                                                                colors = CardDefaults.cardColors(
                                                                    containerColor = Color.White
                                                                ),
                                                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                                            ) {
                                                                Column(
                                                                    modifier = Modifier.padding(14.dp),
                                                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                                                ) {
                                                                    Row(
                                                                        modifier = Modifier.fillMaxWidth(),
                                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                                        verticalAlignment = Alignment.Top
                                                                    ) {
                                                                        Column(modifier = Modifier.weight(1f)) {
                                                                            Text(
                                                                                text = offer.title,
                                                                                fontWeight = FontWeight.Bold,
                                                                                fontSize = 13.sp,
                                                                                color = OnSurfaceDark,
                                                                                maxLines = 1
                                                                            )
                                                                            Text(
                                                                                text = offer.productName.ifBlank { product?.commercialName ?: "منتج دوائي" },
                                                                                fontWeight = FontWeight.SemiBold,
                                                                                fontSize = 11.sp,
                                                                                color = TextSecondaryGray,
                                                                                maxLines = 1
                                                                            )
                                                                        }
                                                                        
                                                                        if (offer.discountPercent > 0) {
                                                                            Box(
                                                                                modifier = Modifier
                                                                                    .background(ErrorRed.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                                                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                                                            ) {
                                                                                Text(
                                                                                    text = "خصم ${offer.discountPercent.toInt()}% 🏷️",
                                                                                    color = ErrorRed,
                                                                                    fontWeight = FontWeight.Bold,
                                                                                    fontSize = 10.sp
                                                                                )
                                                                            }
                                                                        }
                                                                    }

                                                                    Text(
                                                                        text = offer.description,
                                                                        fontSize = 11.sp,
                                                                        color = TextSecondaryGray,
                                                                        lineHeight = 15.sp,
                                                                        maxLines = 2
                                                                    )

                                                                    Row(
                                                                        modifier = Modifier.fillMaxWidth(),
                                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                                        verticalAlignment = Alignment.CenterVertically
                                                                    ) {
                                                                        if (product != null) {
                                                                            val promoPrice = if (offer.specialPrice > 0.0) offer.specialPrice else product.price * (1.0 - offer.discountPercent / 100.0)
                                                                            Column {
                                                                                Text(
                                                                                    text = "السعر الحالي: ${promoPrice.toInt()} ر.ي",
                                                                                    fontWeight = FontWeight.ExtraBold,
                                                                                    fontSize = 13.sp,
                                                                                    color = MedGreenPrimary
                                                                                )
                                                                                Text(
                                                                                    text = "السعر الأصلي: ${product.price.toInt()} ر.ي",
                                                                                    fontSize = 10.sp,
                                                                                    color = TextSecondaryGray,
                                                                                    style = androidx.compose.ui.text.TextStyle(
                                                                                        textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                                                                    )
                                                                                )
                                                                            }
                                                                        } else {
                                                                            if (offer.specialPrice > 0.0) {
                                                                                Text(
                                                                                    text = "سعر خاص: ${offer.specialPrice.toInt()} ر.ي",
                                                                                    fontWeight = FontWeight.ExtraBold,
                                                                                    fontSize = 13.sp,
                                                                                    color = MedGreenPrimary
                                                                                )
                                                                            }
                                                                        }

                                                                        if (offer.targetGovernorate.isNotEmpty()) {
                                                                            Box(
                                                                                modifier = Modifier
                                                                                    .background(MedBluePrimary.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                                                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                                                            ) {
                                                                                Text(
                                                                                    text = "📍 ${offer.targetGovernorate}",
                                                                                    color = MedBluePrimary,
                                                                                    fontSize = 9.sp,
                                                                                    fontWeight = FontWeight.Bold
                                                                                )
                                                                            }
                                                                        }
                                                                    }

                                                                    Button(
                                                                        onClick = {
                                                                            val prod = product ?: PharmaProduct(
                                                                                productId = offer.productId,
                                                                                commercialName = offer.productName,
                                                                                price = offer.specialPrice
                                                                            )
                                                                            val finalPrice = if (offer.specialPrice > 0.0) offer.specialPrice else prod.price * (1.0 - offer.discountPercent / 100.0)
                                                                            val existingIdx = cartItems.indexOfFirst { it.product.productId == prod.productId }
                                                                            if (existingIdx != -1) {
                                                                                val existing = cartItems[existingIdx]
                                                                                cartItems[existingIdx] = existing.copy(quantity = existing.quantity + 1, addedPrice = finalPrice)
                                                                            } else {
                                                                                cartItems.add(
                                                                                    CartItem(
                                                                                        product = prod.copy(price = finalPrice),
                                                                                        quantity = 1,
                                                                                        addedPrice = finalPrice
                                                                                    )
                                                                                )
                                                                            }
                                                                            Toast.makeText(context, "تمت إضافة العرض لـ ${prod.commercialName} إلى السلة بنجاح!", Toast.LENGTH_SHORT).show()
                                                                        },
                                                                        colors = ButtonDefaults.buttonColors(
                                                                            containerColor = MedGreenPrimary,
                                                                            contentColor = Color.White
                                                                        ),
                                                                        shape = RoundedCornerShape(10.dp),
                                                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                                                        modifier = Modifier
                                                                            .fillMaxWidth()
                                                                            .height(34.dp)
                                                                            .testTag("add_promo_to_cart_${offer.offerId}")
                                                                    ) {
                                                                        Row(
                                                                            horizontalArrangement = Arrangement.Center,
                                                                            verticalAlignment = Alignment.CenterVertically
                                                                        ) {
                                                                            Icon(
                                                                                Icons.Default.AddShoppingCart,
                                                                                contentDescription = null,
                                                                                tint = Color.White,
                                                                                modifier = Modifier.size(14.dp)
                                                                            )
                                                                            Spacer(modifier = Modifier.width(6.dp))
                                                                            Text(
                                                                                text = "استفد من العرض واطلب الآن",
                                                                                fontSize = 11.sp,
                                                                                fontWeight = FontWeight.Bold,
                                                                                color = Color.White
                                                                            )
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                }
                                            }
                                        }

                                        // Stacked high fidelity action triggers
                                        item {
                                            Card(
                                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                                shape = RoundedCornerShape(12.dp),
                                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable { clientScreenState = "new_order_flow" }
                                                    .testTag("launcher_new_order_btn")
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(20.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        Icons.Default.ArrowBackIos,
                                                        contentDescription = null,
                                                        tint = MedBluePrimary,
                                                        modifier = Modifier.size(16.dp)
                                                    )

                                                    Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                                                        Text(
                                                            "طلب خاص لصنف غير متوفر بالكتالوج (نادر الاستخدام)",
                                                            color = MedBluePrimary,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 12.sp
                                                        )
                                                        Text(
                                                            "أرسل طلب شراء دوائي خاص مع تفاصيل إضافية لإمشاركتنا في تأمينها.",
                                                            color = Color.Gray,
                                                            fontSize = 10.sp,
                                                            textAlign = TextAlign.Right
                                                        )
                                                    }

                                                    Box(
                                                        modifier = Modifier
                                                            .size(44.dp)
                                                            .background(MedBluePrimary.copy(alpha = 0.1f), CircleShape),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(Icons.Default.AddShoppingCart, contentDescription = null, tint = MedBluePrimary)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                "my_orders" -> {
                                    if (myOrders.isEmpty()) {
                                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Icon(Icons.Default.Inbox, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                                                Text("لم تقم بإرسال أي طلبات توريد بعد.", color = Color.Gray, fontSize = 13.sp)
                                            }
                                        }
                                    } else {
                                         val suggestions = remember(myOrders, pharmaProducts) {
                                             calculateReorderSuggestions(myOrders, pharmaProducts)
                                         }
                                         LazyColumn(
                                             modifier = Modifier
                                                 .fillMaxSize()
                                                 .padding(16.dp),
                                             verticalArrangement = Arrangement.spacedBy(12.dp)
                                         ) {
                                             if (suggestions.isNotEmpty()) {
                                                item {
                                                    Text(
                                                        text = "مرحباً بك مجدداً، " + currentUser.orgName + " 👋",
                                                        fontSize = 15.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = OnSurfaceDark,
                                                        modifier = Modifier.padding(bottom = 6.dp)
                                                    )
                                                    Card(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(bottom = 8.dp),
                                                        shape = RoundedCornerShape(16.dp),
                                                        colors = CardDefaults.cardColors(
                                                            containerColor = Color.White
                                                        ),
                                                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                                    ) {
                                                        Column(
                                                            modifier = Modifier.padding(16.dp),
                                                            horizontalAlignment = Alignment.Start
                                                        ) {
                                                            Text(
                                                                text = "🔔 قد تحتاج إعادة طلب هذه الأصناف قريباً",
                                                                fontSize = 14.sp,
                                                                fontWeight = FontWeight.ExtraBold,
                                                                color = OnSurfaceDark
                                                            )
                                                            Spacer(modifier = Modifier.height(4.dp))
                                                            Text(
                                                                text = "توقعات ذكية بناءً على نمط استهلاكك ونفاذ المخزون المتوقع:",
                                                                fontSize = 11.sp,
                                                                color = TextSecondaryGray
                                                            )
                                                            Spacer(modifier = Modifier.height(12.dp))
                                                            LazyRow(
                                                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                                contentPadding = PaddingValues(vertical = 4.dp)
                                                            ) {
                                                                items(suggestions) { suggestion ->
                                                                    val product = suggestion.product
                                                                    Card(
                                                                        modifier = Modifier
                                                                            .width(240.dp)
                                                                            .border(
                                                                                width = 1.dp,
                                                                                color = if (suggestion.urgency == ReorderUrgency.DUE_NOW) ErrorRed.copy(alpha = 0.3f) else WarningAmber.copy(alpha = 0.3f),
                                                                                shape = RoundedCornerShape(12.dp)
                                                                            ),
                                                                        shape = RoundedCornerShape(12.dp),
                                                                        colors = CardDefaults.cardColors(
                                                                            containerColor = if (suggestion.urgency == ReorderUrgency.DUE_NOW) ErrorRed.copy(alpha = 0.03f) else WarningAmber.copy(alpha = 0.03f)
                                                                        )
                                                                    ) {
                                                                        Column(
                                                                            modifier = Modifier.padding(12.dp),
                                                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                                                        ) {
                                                                            Row(
                                                                                modifier = Modifier.fillMaxWidth(),
                                                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                                                verticalAlignment = Alignment.CenterVertically
                                                                            ) {
                                                                                Text(
                                                                                    text = product.commercialName,
                                                                                    fontWeight = FontWeight.Bold,
                                                                                    fontSize = 13.sp,
                                                                                    color = OnSurfaceDark,
                                                                                    maxLines = 1,
                                                                                    modifier = Modifier.weight(1f)
                                                                                )
                                                                                val (urgencyText, urgencyColor) = if (suggestion.urgency == ReorderUrgency.DUE_NOW) {
                                                                                    Pair("مطلوب الآن ⚠️", ErrorRed)
                                                                                } else {
                                                                                    Pair("قريباً ⏳", WarningAmber)
                                                                                }
                                                                                Box(
                                                                                    modifier = Modifier
                                                                                        .background(urgencyColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                                                ) {
                                                                                    Text(
                                                                                        text = urgencyText,
                                                                                        color = urgencyColor,
                                                                                        fontWeight = FontWeight.Bold,
                                                                                        fontSize = 9.sp
                                                                                    )
                                                                                }
                                                                            }
                                                                            Text(
                                                                                text = "عادة تطلبونه كل " + suggestion.avgIntervalDays + " يوم، آخر طلب قبل " + suggestion.daysSinceLastOrder + " يوم",
                                                                                fontSize = 10.sp,
                                                                                color = TextSecondaryGray,
                                                                                lineHeight = 14.sp
                                                                            )
                                                                            Button(
                                                                                onClick = {
                                                                                    val existingIdx = cartItems.indexOfFirst { it.product.productId == product.productId }
                                                                                    if (existingIdx != -1) {
                                                                                        val existing = cartItems[existingIdx]
                                                                                        cartItems[existingIdx] = existing.copy(quantity = existing.quantity + suggestion.suggestedQuantity)
                                                                                    } else {
                                                                                        cartItems.add(
                                                                                            CartItem(
                                                                                                product = product,
                                                                                                quantity = suggestion.suggestedQuantity,
                                                                                                addedPrice = product.price
                                                                                            )
                                                                                        )
                                                                                    }
                                                                                    Toast.makeText(context, "تمت إضافة " + product.commercialName + " إلى السلة بنجاح!", Toast.LENGTH_SHORT).show()
                                                                                },
                                                                                colors = ButtonDefaults.buttonColors(
                                                                                    containerColor = BrandPrimary,
                                                                                    contentColor = OnBrandPrimary
                                                                                ),
                                                                                shape = RoundedCornerShape(8.dp),
                                                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                                                                modifier = Modifier
                                                                                    .fillMaxWidth()
                                                                                    .height(32.dp)
                                                                                    .testTag("add_suggested_" + product.productId)
                                                                            ) {
                                                                                Row(
                                                                                    horizontalArrangement = Arrangement.Center,
                                                                                    verticalAlignment = Alignment.CenterVertically
                                                                                ) {
                                                                                    Icon(
                                                                                        Icons.Default.AddShoppingCart,
                                                                                        contentDescription = null,
                                                                                        tint = OnBrandPrimary,
                                                                                        modifier = Modifier.size(12.dp)
                                                                                    )
                                                                                    Spacer(modifier = Modifier.width(4.dp))
                                                                                    Text(
                                                                                        text = "أضف للسلة (" + suggestion.suggestedQuantity + ") ➕",
                                                                                        fontSize = 10.sp,
                                                                                        fontWeight = FontWeight.Bold,
                                                                                        color = OnBrandPrimary
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

                                            items(myOrders) { order ->
                                                val offers = activeOffersMap[order.orderId] ?: emptyList()

                                                Card(
                                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                                    shape = RoundedCornerShape(12.dp),
                                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clickable {
                                                            selectedOrderForOffers = order
                                                            clientScreenState = when (order.status) {
                                                                "broadcast" -> "order_detail"
                                                                "offer_received" -> "order_offers"
                                                                "confirmed" -> "order_detail"
                                                                else -> "order_detail"
                                                            }
                                                        }
                                                        .testTag("client_order_item_${order.orderId}")
                                                ) {
                                                    Column(modifier = Modifier.padding(14.dp)) {
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Text(
                                                                "طلب رقم: ${order.orderId.takeLast(6)}",
                                                                fontWeight = FontWeight.Bold,
                                                                fontSize = 13.sp,
                                                                color = MedBluePrimary
                                                            )

                                                            val urgencyColor = when (order.urgencyLevel) {
                                                                "critical" -> MedRedPrimary
                                                                "high" -> Color(0xFFEAB308)
                                                                else -> MedGreenPrimary
                                                            }
                                                            val urgencyText = when (order.urgencyLevel) {
                                                                "critical" -> "طارئ"
                                                                "high" -> "عاجل"
                                                                else -> "عادي"
                                                            }

                                                            Box(
                                                                modifier = Modifier
                                                                    .background(urgencyColor.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                                                            ) {
                                                                Text(urgencyText, color = urgencyColor, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                                            }
                                                        }

                                                        Spacer(modifier = Modifier.height(8.dp))

                                                        Text(
                                                            order.orderContent,
                                                            fontSize = 12.sp,
                                                            color = Color.DarkGray,
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .background(Color(0xFFF8FAFC), RoundedCornerShape(6.dp))
                                                                .padding(10.dp)
                                                        )

                                                        Spacer(modifier = Modifier.height(10.dp))

                                                                      Row(
                                                                          modifier = Modifier.fillMaxWidth(),
                                                                          horizontalArrangement = Arrangement.SpaceBetween,
                                                                          verticalAlignment = Alignment.CenterVertically
                                                                      ) {
                                                                          Column(modifier = Modifier.weight(1f)) {
                                                                Text("الحالة:", fontSize = 9.sp, color = Color.Gray)
                                                                val (statusText, statusColor) = when (order.orderStatus) {
                                                                    is OrderStatus.Draft -> Pair("مسودة قيد المراجعة ✍️", Color.Gray)
                                                                    is OrderStatus.Submitted -> {
                                                                        if (order.status == "offer_received") {
                                                                            Pair("تم استلام عروض فروع! 🎉", MedGreenPrimary)
                                                                        } else if (order.status == "confirmed") {
                                                                            Pair("تم تأكيد طلبك والتعميد ✔", MedGreenPrimary)
                                                                        } else {
                                                                            Pair("بانتظار تسعير الفروع.. ⏳", Color.Gray)
                                                                        }
                                                                    }
                                                                    is OrderStatus.Allocated -> Pair("تم التجهيز بالكامل - بانتظار الشاحنة 🚛", MedBluePrimary)
                                                                    is OrderStatus.PartiallyShipped -> Pair("تم شحن جزء من الطلبية 📦", Color(0xFFF97316))
                                                                    is OrderStatus.Invoiced -> Pair("تم إصدار الفاتورة 🧾", Color(0xFF10B981))
                                                                    is OrderStatus.Delivered -> Pair("تم تسليم الشحنة وتأكيد الاستلام ✔", MedGreenPrimary)
                                                                }
                                                                Text(
                                                                    statusText,
                                                                    fontSize = 11.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = statusColor
                                                                )
                                                            }

                                                            Row(
                                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                                verticalAlignment = Alignment.CenterVertically
                                                            ) {
                                                                if (true) {
                                                                    Button(
                                                                        onClick = {
                                                                            if (order.orderLines.isEmpty()) {
                                                                                Toast.makeText(context, "الطلب لا يحتوي على أصناف صالحة لإعادة الطلب", Toast.LENGTH_SHORT).show()
                                                                            } else {
                                                                                order.orderLines.forEach { line ->
                                                                                    val existingIdx = cartItems.indexOfFirst { it.product.productId == line.product.productId }
                                                                                    if (existingIdx != -1) {
                                                                                        val existing = cartItems[existingIdx]
                                                                                        cartItems[existingIdx] = existing.copy(quantity = existing.quantity + line.requestedQty)
                                                                                    } else {
                                                                                        cartItems.add(CartItem(product = line.product, quantity = line.requestedQty, addedPrice = line.unitPrice))
                                                                                    }
                                                                                }
                                                                                Toast.makeText(context, "تمت إعادة إضافة الأصناف إلى السلة بنجاح 🛒", Toast.LENGTH_LONG).show()
                                                                                clientScreenState = "cart"
                                                                            }
                                                                        },
                                                                        colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary, contentColor = OnBrandPrimary),
                                                                        shape = RoundedCornerShape(8.dp),
                                                                        contentPadding = PaddingValues(horizontal = 10.dp),
                                                                        modifier = Modifier.height(34.dp).testTag("quick_reorder_${order.orderId}")
                                                                    ) {
                                                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                                            Icon(Icons.Default.Refresh, contentDescription = null, tint = OnBrandPrimary, modifier = Modifier.size(14.dp))
                                                                            Text("إعادة طلب سريع 🔁", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = OnBrandPrimary)
                                                                        }
                                                                    }
                                                                }
                                                                if (offers.isNotEmpty() && order.orderStatus is OrderStatus.Submitted) {
                                                                    Button(
                                                                        onClick = {
                                                                            selectedOrderForOffers = order
                                                                            clientScreenState = "order_offers"
                                                                        },
                                                                        colors = ButtonDefaults.buttonColors(containerColor = MedGreenPrimary, contentColor = Color.White),
                                                                        shape = RoundedCornerShape(8.dp),
                                                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                                                        modifier = Modifier.height(34.dp)
                                                                    ) {
                                                                        Text("عرض العروض (${offers.size}) 💰", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                                    }
                                                                } else {
                                                                    val stateText = when (order.orderStatus) {
                                                                        is OrderStatus.Draft -> "مسودة"
                                                                        is OrderStatus.Submitted -> "بانتظار الفروع.. ⏳"
                                                                        is OrderStatus.Allocated -> "قيد الحجز 🚛"
                                                                        is OrderStatus.PartiallyShipped -> "شحن جزئي 📦"
                                                                        is OrderStatus.Invoiced -> "تم إصدار الفاتورة 🧾"
                                                                        else -> "جاري المعالجة ⏳"
                                                                    }
                                                                    Box(
                                                                        modifier = Modifier
                                                                            .background(Color(0xFFF1F5F9), RoundedCornerShape(6.dp))
                                                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                                                    ) {
                                                                        Text(stateText, fontSize = 10.sp, color = Color.Gray)
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

                                "financial" -> {
                                    LazyColumn(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        // 📊 Financial Summary Card
                                        item {
                                            Card(
                                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                                shape = RoundedCornerShape(16.dp),
                                                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Column(
                                                    modifier = Modifier.padding(20.dp),
                                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Column {
                                                            Text(
                                                                "الملخص الائتماني والمالي 📊",
                                                                fontWeight = FontWeight.Bold,
                                                                fontSize = 16.sp,
                                                                color = MedBluePrimary
                                                            )
                                                            Text(
                                                                "حساب مالي معتمد: ${if (clientAccountState.isActive) "نشط ✔" else "موقف ❌"}",
                                                                fontSize = 11.sp,
                                                                color = Color.Gray
                                                            )
                                                        }

                                                        Box(
                                                            modifier = Modifier
                                                                .background(MedBluePrimary.copy(alpha = 0.1f), CircleShape)
                                                                .padding(10.dp)
                                                        ) {
                                                            Icon(
                                                                Icons.Default.AccountBalanceWallet,
                                                                contentDescription = null,
                                                                tint = MedBluePrimary,
                                                                modifier = Modifier.size(24.dp)
                                                            )
                                                        }
                                                    }

                                                    Divider(color = Color(0xFFF1F5F9))

                                                    // Balance and utilization
                                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.Bottom
                                                        ) {
                                                            Column {
                                                                Text("الرصيد المستحق (المديونية الحالية):", fontSize = 11.sp, color = Color.Gray)
                                                                Text(
                                                                    String.format("%,.2f %s", clientAccountState.currentBalance, clientAccountState.currency),
                                                                    fontSize = 22.sp,
                                                                    fontWeight = FontWeight.ExtraBold,
                                                                    color = if (clientAccountState.currentBalance > 0) MedRedPrimary else Color.DarkGray
                                                                )
                                                            }

                                                            Column(horizontalAlignment = Alignment.End) {
                                                                Text("السقف الائتماني الأقصى:", fontSize = 11.sp, color = Color.Gray)
                                                                Text(
                                                                    String.format("%,.2f %s", clientAccountState.creditLimit, clientAccountState.currency),
                                                                    fontSize = 14.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = Color.Gray
                                                                )
                                                            }
                                                        }

                                                        // Progress Bar
                                                        val limitUsageRatio = if (clientAccountState.creditLimit > 0) {
                                                            (clientAccountState.currentBalance / clientAccountState.creditLimit).toFloat().coerceIn(0f, 1f)
                                                        } else 0f

                                                        val progressColor = when {
                                                            limitUsageRatio > 0.8f -> MedRedPrimary
                                                            limitUsageRatio > 0.5f -> Color(0xFFF97316) // Orange
                                                            else -> MedGreenPrimary
                                                        }

                                                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                            LinearProgressIndicator(
                                                                progress = { limitUsageRatio },
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .height(10.dp)
                                                                    .clip(CircleShape),
                                                                color = progressColor,
                                                                trackColor = Color(0xFFE2E8F0)
                                                            )
                                                            Row(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                horizontalArrangement = Arrangement.SpaceBetween
                                                            ) {
                                                                Text(
                                                                    text = String.format("تم استهلاك %.1f%% من السقف الائتماني", limitUsageRatio * 100),
                                                                    fontSize = 10.sp,
                                                                    color = Color.Gray
                                                                )
                                                                Text(
                                                                    text = String.format("المتبقي: %,.0f %s", (clientAccountState.creditLimit - clientAccountState.currentBalance).coerceAtLeast(0.0), clientAccountState.currency),
                                                                    fontSize = 10.sp,
                                                                    color = MedGreenPrimary,
                                                                    fontWeight = FontWeight.Bold
                                                                )
                                                            }
                                                        }
                                                    }

                                                    Divider(color = Color(0xFFF1F5F9))

                                                    // Payment Terms Detail
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
                                                            .padding(12.dp),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Column {
                                                            Text("شروط وآجال الدفع المتعاقد عليها:", fontSize = 11.sp, color = Color.Gray)
                                                            val termsText = when (clientAccountState.paymentTerms) {
                                                                PaymentTerms.NET30 -> "سداد آجل خلال 30 يوماً (NET 30)"
                                                                PaymentTerms.NET60 -> "سداد آجل خلال 60 يوماً (NET 60)"
                                                                PaymentTerms.CASH_ON_DELIVERY -> "الدفع نقداً عند التسليم (COD)"
                                                                PaymentTerms.PREPAID -> "الدفع والتحصيل المسبق (Prepaid)"
                                                            }
                                                            Text(termsText, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MedBluePrimary)
                                                        }

                                                        Icon(
                                                            Icons.Default.Verified,
                                                            contentDescription = null,
                                                            tint = MedGreenPrimary,
                                                            modifier = Modifier.size(20.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        // Invoices List Section
                                        item {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    "المطالبات والفواتير الصادرة 🧾",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    color = Color.DarkGray
                                                )
                                                Badge(containerColor = MedBluePrimary, contentColor = Color.White) {
                                                    Text(clientInvoices.size.toString(), color = Color.White, fontSize = 10.sp)
                                                }
                                            }
                                        }

                                        if (clientInvoices.isEmpty()) {
                                            item {
                                                Card(
                                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                                    shape = RoundedCornerShape(12.dp),
                                                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                                                ) {
                                                    Column(
                                                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                                                        horizontalAlignment = Alignment.CenterHorizontally,
                                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        Icon(
                                                            Icons.Default.ReceiptLong,
                                                            contentDescription = null,
                                                            tint = Color.LightGray,
                                                            modifier = Modifier.size(48.dp)
                                                        )
                                                        Text(
                                                            "لا يوجد فواتير صادرة لحسابك حالياً.",
                                                            fontSize = 12.sp,
                                                            color = Color.Gray
                                                        )
                                                    }
                                                }
                                            }
                                        } else {
                                            items(clientInvoices) { invoice ->
                                                val isOverdueOrClose = invoice.paymentStatus == PaymentStatus.UNPAID &&
                                                        (invoice.dueDate <= System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000L)

                                                Card(
                                                    colors = CardDefaults.cardColors(
                                                        containerColor = if (isOverdueOrClose) Color(0xFFFEF2F2) else Color.White
                                                    ),
                                                    shape = RoundedCornerShape(12.dp),
                                                    border = if (isOverdueOrClose) {
                                                        androidx.compose.foundation.BorderStroke(1.dp, MedRedPrimary.copy(alpha = 0.4f))
                                                    } else {
                                                        androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                                                    },
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                                        // Title & Badge
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Text(
                                                                "فاتورة رقم: ${invoice.invoiceId.takeLast(8).uppercase()}",
                                                                fontWeight = FontWeight.Bold,
                                                                fontSize = 13.sp,
                                                                color = MedBluePrimary
                                                            )

                                                            val (statusBadgeText, statusBadgeColor, statusTextColor) = when (invoice.paymentStatus) {
                                                                PaymentStatus.PAID -> Triple("مدفوعة بالكامل ✔", MedGreenPrimary.copy(alpha = 0.15f), MedGreenPrimary)
                                                                PaymentStatus.PARTIALLY_PAID -> Triple("مدفوعة جزئياً 🔸", Color(0xFFFEF3C7), Color(0xFFD97706))
                                                                PaymentStatus.UNPAID -> Triple("غير مدفوعة ⏳", MedRedPrimary.copy(alpha = 0.15f), MedRedPrimary)
                                                            }

                                                            Box(
                                                                modifier = Modifier
                                                                    .clip(RoundedCornerShape(6.dp))
                                                                    .background(statusBadgeColor)
                                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                                            ) {
                                                                Text(
                                                                    statusBadgeText,
                                                                    color = statusTextColor,
                                                                    fontWeight = FontWeight.Bold,
                                                                    fontSize = 10.sp
                                                                )
                                                            }
                                                        }

                                                        Divider(color = Color(0xFFF1F5F9).copy(alpha = 0.5f))

                                                        // Amount & Taxes
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Column {
                                                                Text("إجمالي مبلغ الفاتورة:", fontSize = 10.sp, color = Color.Gray)
                                                                Text(
                                                                    String.format("%,.2f %s", invoice.totalAmount, clientAccountState.currency),
                                                                    fontSize = 16.sp,
                                                                    fontWeight = FontWeight.ExtraBold,
                                                                    color = Color.DarkGray
                                                                )
                                                            }

                                                            Column(horizontalAlignment = Alignment.End) {
                                                                Text("مشتملة على ضرائب وخصوم:", fontSize = 10.sp, color = Color.Gray)
                                                                Text(
                                                                    String.format("خصم: %.0f | ضريبة: %.0f", invoice.discountAmount, invoice.taxAmount),
                                                                    fontSize = 11.sp,
                                                                    color = Color.Gray
                                                                )
                                                            }
                                                        }

                                                        // Timing details and Urgent Banner
                                                        Row(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .background(Color(0xFFF8FAFC).copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                                                .padding(8.dp),
                                                            horizontalArrangement = Arrangement.SpaceBetween
                                                        ) {
                                                            Text(
                                                                "إصدار: ${formatTimestamp(invoice.issuedAt)}",
                                                                fontSize = 10.sp,
                                                                color = Color.Gray
                                                            )
                                                            Text(
                                                                "استحقاق: ${formatTimestamp(invoice.dueDate)}",
                                                                fontSize = 10.sp,
                                                                color = if (isOverdueOrClose) MedRedPrimary else Color.Gray,
                                                                fontWeight = if (isOverdueOrClose) FontWeight.Bold else FontWeight.Normal
                                                            )
                                                        }

                                                        if (isOverdueOrClose) {
                                                            Row(
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .background(MedRedPrimary.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                                                                    .padding(8.dp),
                                                                verticalAlignment = Alignment.CenterVertically,
                                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                            ) {
                                                                Icon(
                                                                    Icons.Default.Warning,
                                                                    contentDescription = null,
                                                                    tint = MedRedPrimary,
                                                                    modifier = Modifier.size(14.dp)
                                                                )
                                                                Text(
                                                                    "تنبيه: تاريخ استحقاق الفاتورة قريب جداً أو منتهي، يرجى التسوية لتجنب تعليق الائتمان.",
                                                                    fontSize = 9.sp,
                                                                    color = MedRedPrimary,
                                                                    fontWeight = FontWeight.Bold,
                                                                    lineHeight = 13.sp
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                "account" -> {
                                    LazyColumn(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        // User Info Card
                                        item {
                                            Card(
                                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                                shape = RoundedCornerShape(12.dp),
                                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Column(
                                                    modifier = Modifier.padding(16.dp),
                                                    horizontalAlignment = Alignment.Start,
                                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(54.dp)
                                                                .background(MedBluePrimary.copy(alpha = 0.15f), CircleShape),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Person,
                                                                contentDescription = null,
                                                                tint = MedBluePrimary,
                                                                modifier = Modifier.size(28.dp)
                                                            )
                                                        }
                                                        Column {
                                                            Text(
                                                                text = currentUser.name,
                                                                fontWeight = FontWeight.Bold,
                                                                fontSize = 16.sp,
                                                                color = Color.DarkGray
                                                            )
                                                            Text(
                                                                text = if (currentUser.clientType == "hospital") "مستشفى مالي" else "صيدلية تجارية",
                                                                fontSize = 12.sp,
                                                                color = Color.Gray
                                                            )
                                                        }
                                                    }

                                                    Divider(color = Color(0xFFF1F5F9), thickness = 1.dp)

                                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Icon(Icons.Default.Business, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                            Text("المنشأة: ${currentUser.orgName}", fontSize = 13.sp, color = Color.DarkGray)
                                                        }
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Icon(Icons.Default.Email, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                            Text("البريد الإلكتروني: ${currentUser.email}", fontSize = 13.sp, color = Color.DarkGray)
                                                        }
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Icon(Icons.Default.Phone, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                            Text("رقم الهاتف: ${currentUser.phone}", fontSize = 13.sp, color = Color.DarkGray)
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        // Delivery Addresses Card
                                        item {
                                            Card(
                                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                                shape = RoundedCornerShape(12.dp),
                                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Column(
                                                    modifier = Modifier.padding(16.dp),
                                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                            Icon(Icons.Default.Place, contentDescription = null, tint = MedBluePrimary)
                                                            Text(
                                                                text = "عناوين التسليم",
                                                                fontWeight = FontWeight.Bold,
                                                                fontSize = 15.sp,
                                                                color = Color.DarkGray
                                                            )
                                                        }
                                                    }

                                                    Divider(color = Color(0xFFF1F5F9), thickness = 1.dp)

                                                    if (defaultAddress != null) {
                                                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                                Text(
                                                                    text = defaultAddress!!.label,
                                                                    fontWeight = FontWeight.Bold,
                                                                    fontSize = 13.sp,
                                                                    color = MedBluePrimary
                                                                )
                                                                if (defaultAddress!!.isDefault) {
                                                                    Box(
                                                                        modifier = Modifier
                                                                            .clip(RoundedCornerShape(4.dp))
                                                                            .background(MedGreenPrimary.copy(alpha = 0.1f))
                                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                                    ) {
                                                                        Text("افتراضي", color = MedGreenPrimary, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                                                    }
                                                                }
                                                            }
                                                            Text(
                                                                text = defaultAddress!!.fullAddress,
                                                                fontSize = 12.sp,
                                                                color = Color.Gray,
                                                                lineHeight = 18.sp
                                                            )
                                                        }
                                                    } else {
                                                        Column(
                                                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                                            horizontalAlignment = Alignment.CenterHorizontally,
                                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                                        ) {
                                                            Icon(Icons.Default.LocationOff, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(36.dp))
                                                            Text(
                                                                text = "لا يوجد عنوان افتراضي محدد حالياً",
                                                                fontSize = 12.sp,
                                                                color = Color.Gray
                                                            )
                                                        }
                                                    }

                                                    Spacer(modifier = Modifier.height(4.dp))

                                                    Button(
                                                        onClick = { clientScreenState = "addresses" },
                                                        colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary, contentColor = OnBrandPrimary),
                                                        shape = RoundedCornerShape(8.dp),
                                                        modifier = Modifier.fillMaxWidth().height(42.dp).testTag("manage_addresses_btn")
                                                    ) {
                                                        Text("إدارة العناوين ←", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                        }

                                        // Quick Actions Card
                                        item {
                                            Card(
                                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                                shape = RoundedCornerShape(12.dp),
                                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Column(
                                                    modifier = Modifier.padding(16.dp),
                                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                                ) {
                                                    Text(
                                                        text = "إجراءات سريعة",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 14.sp,
                                                        color = Color.DarkGray
                                                    )

                                                    Divider(color = Color(0xFFF1F5F9), thickness = 1.dp)

                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .clickable { clientScreenState = "digital_card_view" }
                                                            .padding(vertical = 8.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(36.dp)
                                                                .background(MedBluePrimary.copy(alpha = 0.1f), CircleShape),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Icon(Icons.Default.Badge, contentDescription = null, tint = MedBluePrimary, modifier = Modifier.size(18.dp))
                                                        }
                                                        Text("عرض بطاقتي الرقمية الطبية 🛡️", fontSize = 13.sp, color = Color.DarkGray, fontWeight = FontWeight.Medium)
                                                    }

                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .clickable { onLogout() }
                                                            .padding(vertical = 8.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(36.dp)
                                                                .background(MedRedPrimary.copy(alpha = 0.1f), CircleShape),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Icon(Icons.Default.ExitToApp, contentDescription = null, tint = MedRedPrimary, modifier = Modifier.size(18.dp))
                                                        }
                                                        Text("تسجيل الخروج من الحساب 🚪", fontSize = 13.sp, color = MedRedPrimary, fontWeight = FontWeight.Bold)
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

        // Offers List Dialog
        if (showOffersDialog && selectedOrderForOffers != null) {
            val order = selectedOrderForOffers!!
            val offers = activeOffersMap[order.orderId] ?: emptyList()

            AlertDialog(
                onDismissRequest = { showOffersDialog = false },
                title = {
                    Text(
                        "العروض المقدمة لطلبك رقم: ${order.orderId.takeLast(6)} 💰",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                text = {
                    Box(modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)) {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(offers) { offer ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(offer.branchName, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MedBluePrimary)
                                            Text("${offer.totalPrice} ${offer.currency}", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = MedGreenPrimary)
                                        }

                                        Text("التفاصيل: ${offer.offerDetails}", fontSize = 11.sp, color = Color.DarkGray)
                                        Text("أيام التوصيل المتوقعة: ${offer.deliveryDays} أيام", fontSize = 10.sp, color = Color.Gray)
                                        Text("كلفة الشحن: YER ${offer.shippingCost}", fontSize = 10.sp, color = Color.Gray)
                                        Text("شروط السداد: ${offer.paymentTerms}", fontSize = 10.sp, color = Color.Gray)

                                        if (offer.status == "pending" && order.status != "confirmed") {
                                            Button(
                                                onClick = {
                                                    FirebaseService.updateBranchOfferStatus(offer.offerId, "accepted", {
                                                        Toast.makeText(context, "تم قبول عرض ${offer.branchName} وتم تعميد الطلب بنجاح! ✔", Toast.LENGTH_LONG).show()
                                                        showOffersDialog = false
                                                        refreshOrders()
                                                    }, {})
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = MedGreenPrimary),
                                                shape = RoundedCornerShape(6.dp),
                                                modifier = Modifier.fillMaxWidth().height(32.dp)
                                            ) {
                                                Text("تعميد وقبول هذا العرض 🤝", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        } else if (offer.status == "accepted") {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(Color(0xFFEFF6FF), RoundedCornerShape(4.dp))
                                                    .padding(6.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("تم قبول هذا العرض وتم التعميد للفرع ✔", color = MedGreenPrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showOffersDialog = false }) {
                        Text("إغلاق", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    mainAxisSpacing: androidx.compose.ui.unit.Dp = 0.dp,
    crossAxisSpacing: androidx.compose.ui.unit.Dp = 0.dp,
    content: @Composable androidx.compose.foundation.layout.FlowRowScope.() -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(mainAxisSpacing),
        verticalArrangement = Arrangement.spacedBy(crossAxisSpacing),
        content = content
    )
}

fun formatTimestamp(timestamp: Long): String {
    return try {
        val sdf = java.text.SimpleDateFormat("yyyy/MM/dd HH:mm", java.util.Locale.getDefault())
        sdf.format(java.util.Date(timestamp))
    } catch (e: Exception) {
        "N/A"
    }
}
