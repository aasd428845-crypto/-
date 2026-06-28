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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.service.FirebaseService
import com.example.ui.theme.MedBluePrimary
import com.example.ui.theme.MedGreenPrimary
import com.example.ui.theme.MedRedPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContainer() {
    val context = LocalContext.current

    // Session States
    var userLoggedIn by remember { mutableStateOf<User?>(null) }
    var currentScreenState by remember { mutableStateOf("login") } // "login", "dashboard", "add_address", "delivery_method", "payment", "bank_accounts", "schedules"

    // Temporary storage for item checkout
    var activeCheckoutOffer by remember { mutableStateOf<PriceOffer?>(null) }
    var activeCheckoutOrder by remember { mutableStateOf<Order?>(null) }

    // Search query states
    var searchQuery by remember { mutableStateOf("") }
    var userAddressesList by remember { mutableStateOf<List<UserAddress>>(emptyList()) }
    var selectedDefaultAddress by remember { mutableStateOf<UserAddress?>(null) }

    // Dialog state for fast testing instructions
    var showExplanationHelper by remember { mutableStateOf(true) }

    // Fetch and populate database arrays
    fun refreshCurrentUserData() {
        val user = userLoggedIn
        if (user != null) {
            FirebaseService.getUserAddresses(user.userId) { list ->
                userAddressesList = list
                selectedDefaultAddress = list.find { it.isDefault } ?: list.firstOrNull()
            }
        }
    }

    LaunchedEffect(userLoggedIn) {
        refreshCurrentUserData()
    }

    if (userLoggedIn == null) {
        // --- 🟢 Login / Welcome Screen 🟢 ---
        var emailInput by remember { mutableStateOf("") }
        var isProgressing by remember { mutableStateOf(false) }

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("ميد-لينك اليمن | MedLink Yemen 🏥", fontWeight = FontWeight.ExtraBold, color = Color.White) },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MedBluePrimary)
                )
            }
        ) { paddingVals ->
            Column(
                modifier = Modifier
                    .padding(paddingVals)
                    .fillMaxSize()
                    .background(Color(0xFF0F172A)) // Aesthetic dark cosmic theme
                    .padding(20.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Branded Logo Card
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(MedBluePrimary, CircleShape)
                        .border(3.dp, MedGreenPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MedicalServices,
                        contentDescription = "Logo",
                        tint = MedGreenPrimary,
                        modifier = Modifier.size(50.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "مِنصة الإمداد والوساطة الدوائية الذكية",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "رابط آمن ومباشر بين المستشفيات والشركات الموردة للأدوية في اليمن لتخطيط الدفع واللوجستيات المبردة",
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )

                Spacer(modifier = Modifier.height(30.dp))

                // Standard email input
                OutlinedTextField(
                    value = emailInput,
                    onValueChange = { emailInput = it },
                    label = { Text("أدخل بريدك الإلكتروني للتجربة") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("login_email_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = MedGreenPrimary,
                        unfocusedLabelColor = Color.LightGray,
                        focusedBorderColor = MedGreenPrimary,
                        unfocusedBorderColor = Color.Gray
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        if (emailInput.isBlank()) {
                            Toast.makeText(context, "الرجاء كتابة البريد الإلكتروني أو اختيار حساب تجريبي بالأسفل 👇", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isProgressing = true
                        FirebaseService.loginUser(emailInput) { user, _ ->
                            userLoggedIn = user
                            currentScreenState = "dashboard"
                            isProgressing = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MedGreenPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("login_submit_btn"),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (isProgressing) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("الدخول للمنصة ➡️", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text("👇 أو تسجيل الدخول السريع كأحد الأطراف للتجربة:", color = Color.LightGray, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(8.dp))

                // Quick Login Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            emailInput = "thawra@yemen.org"
                            FirebaseService.loginUser(emailInput) { user, _ ->
                                userLoggedIn = user
                                currentScreenState = "dashboard"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary),
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .testTag("login_as_hospital_btn"),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("🏥 مستشفى الثورة", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            emailInput = "global@yemen.org"
                            FirebaseService.loginUser(emailInput) { user, _ ->
                                userLoggedIn = user
                                currentScreenState = "dashboard"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary),
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .testTag("login_as_supplier_btn"),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("🚚 الشركة العالمية", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val mockAdmin = User("admin_1", "مدير النظام", "admin@medlink.ye", "admin", "صنعاء", "77000000", "إدارة المنصة والرقابة")
                            userLoggedIn = mockAdmin
                            currentScreenState = "dashboard"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .testTag("login_as_admin_btn"),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("👑 مشرف النظام", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    } else {
        // Logged in. Dispatch based on currentScreenState
        val loggedUser = userLoggedIn!!

        when (currentScreenState) {
            "add_address" -> {
                AddAddressScreen(
                    currentUser = loggedUser,
                    onBackClick = { currentScreenState = "dashboard" },
                    onSaveSuccess = {
                        refreshCurrentUserData()
                        currentScreenState = "dashboard"
                    }
                )
            }
            "bank_accounts" -> {
                BankAccountsScreen(
                    currentUser = loggedUser,
                    onBackClick = { currentScreenState = "dashboard" }
                )
            }
            "delivery_method" -> {
                if (activeCheckoutOffer != null) {
                    DeliveryMethodScreen(
                        currentUser = loggedUser,
                        priceOffer = activeCheckoutOffer!!,
                        onBackClick = { currentScreenState = "dashboard" },
                        onSelfDeliverSelect = { finalAddress, distance, eta ->
                            // Update price offer with chosen details as accepted
                            FirebaseService.updatePriceOfferWithDelivery(
                                activeCheckoutOffer!!.priceOfferId,
                                finalAddress,
                                distance,
                                eta,
                                "self",
                                {
                                    // Move onto direct payout screen
                                    currentScreenState = "payment"
                                },
                                {}
                            )
                        },
                        onPlatformDeliverSelect = { finalAddress, distance, eta, estimatedPrice ->
                            // Update price offer details with mediator
                            FirebaseService.updatePriceOfferWithDelivery(
                                activeCheckoutOffer!!.priceOfferId,
                                finalAddress,
                                distance,
                                eta,
                                "platform",
                                {
                                    // Assemble a new platform delivery request
                                    val pickupAddressCoords = FirebaseService.fallbackAddresses.find { it.userId == activeCheckoutOffer!!.supplierId }
                                    val dRequest = DeliveryRequest(
                                        deliveryId = "",
                                        orderId = "order_" + activeCheckoutOffer!!.priceOfferId,
                                        hospitalId = loggedUser.userId,
                                        supplierId = activeCheckoutOffer!!.supplierId,
                                        pickupAddress = pickupAddressCoords?.fullAddress ?: "مخازن الشركة الموردة مآرب/صنعاء",
                                        pickupLat = pickupAddressCoords?.latitude ?: 15.3482,
                                        pickupLng = pickupAddressCoords?.longitude ?: 44.2191,
                                        deliveryAddress = finalAddress.fullAddress,
                                        deliveryLat = finalAddress.latitude,
                                        deliveryLng = finalAddress.longitude,
                                        distance = distance,
                                        estimatedPrice = estimatedPrice,
                                        urgencyLevel = "high",
                                        packageSize = "medium",
                                        status = "pending",
                                        createdAt = System.currentTimeMillis()
                                    )

                                    FirebaseService.submitDeliveryRequest(dRequest, {
                                        Toast.makeText(context, "📦 تم إرفاق وإخطار مشرف المنصة لتعيين ناقل التوصيل مبرداً فوراً!", Toast.LENGTH_LONG).show()
                                        // Forward to Schedule setup directly
                                        val generatedOrder = FirebaseService.fallbackOrders.find { it.priceOfferId == activeCheckoutOffer!!.priceOfferId }
                                        activeCheckoutOrder = generatedOrder
                                        currentScreenState = "schedules"
                                    }, {})
                                },
                                {}
                            )
                        }
                    )
                }
            }
            "payment" -> {
                if (activeCheckoutOffer != null) {
                    PaymentScreen(
                        currentUser = loggedUser,
                        priceOffer = activeCheckoutOffer!!,
                        onBackClick = { currentScreenState = "dashboard" },
                        onPaymentSuccess = {
                            // Forward onto Delivery Scheduling after confirming payouts
                            val generatedOrder = FirebaseService.fallbackOrders.find { it.priceOfferId == activeCheckoutOffer!!.priceOfferId }
                            activeCheckoutOrder = generatedOrder
                            currentScreenState = "schedules"
                        }
                    )
                }
            }
            "schedules" -> {
                if (activeCheckoutOrder != null) {
                    DeliveryScheduleScreen(
                        currentUser = loggedUser,
                        order = activeCheckoutOrder!!,
                        onBackClick = { currentScreenState = "dashboard" },
                        onScheduleCompleted = {
                            currentScreenState = "dashboard"
                            activeCheckoutOffer = null
                            activeCheckoutOrder = null
                        }
                    )
                }
            }
            else -> {
                // --- 📦 CORE DASHBOARDS DISPATCHER 📦 ---
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Column {
                                    Text(
                                        "ميد-لينك | ${if (loggedUser.role == "hospital") "بوابة المستشفى 🏥" else if (loggedUser.role == "supplier") "بوابة المورد 🚚" else "لوحة الرقابة والمشرفين 👑"}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        "المستخدم: ${loggedUser.name} (${loggedUser.orgName})",
                                        fontSize = 10.sp,
                                        color = Color.LightGray
                                    )
                                }
                            },
                            actions = {
                                // Info Button
                                IconButton(onClick = { showExplanationHelper = true }) {
                                    Icon(Icons.Default.Info, contentDescription = "Manual Guide", tint = Color.White)
                                }

                                // Quick profile tools
                                if (loggedUser.role == "supplier") {
                                    IconButton(
                                        onClick = { currentScreenState = "bank_accounts" },
                                        modifier = Modifier.testTag("nav_bank_settings")
                                    ) {
                                        Icon(Icons.Default.AccountBalance, contentDescription = "Bank Accounts", tint = MedGreenPrimary)
                                    }
                                } else if (loggedUser.role == "hospital") {
                                    IconButton(
                                        onClick = { currentScreenState = "add_address" },
                                        modifier = Modifier.testTag("nav_add_address")
                                    ) {
                                        Icon(Icons.Default.AddLocation, contentDescription = "Add Address", tint = MedGreenPrimary)
                                    }
                                }

                                TextButton(onClick = { userLoggedIn = null }) {
                                    Text("خروج 🚪", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(containerColor = MedBluePrimary)
                        )
                    }
                ) { dashboardPadding ->
                    Box(modifier = Modifier.padding(dashboardPadding).fillMaxSize()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFFF1F5F9))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            if (loggedUser.role == "hospital") {
                                // ==========================================
                                // Hospital View
                                // ==========================================

                                // Top Address Selection preview
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp).fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Place, contentDescription = null, tint = MedBluePrimary)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Column {
                                                Text("نقطة الاستلام والتموضع المحدد:", fontSize = 9.sp, color = Color.Gray)
                                                Text(
                                                    selectedDefaultAddress?.label ?: "لم يحدد عنوان مسبقاً (سيُستعمل افتراضياً صنعاء)",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }

                                        Button(
                                            onClick = { currentScreenState = "add_address" },
                                            colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary),
                                            shape = RoundedCornerShape(20.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                            modifier = Modifier.height(30.dp)
                                        ) {
                                            Text("إدارة العناوين الجغرافية", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                // Search box
                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    placeholder = { Text("ابحث عن الأدوية أو تصنيفاتها ...") },
                                    modifier = Modifier.fillMaxWidth().testTag("medicine_search_bar"),
                                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                    colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
                                )

                                Text("🔍 الموردين المتاحين طبقاً لـ (Haversine Formula) الأقرب مسافة:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.DarkGray)

                                // Render closest Suppliers list applying haversine
                                val hospitalLat = selectedDefaultAddress?.latitude ?: 15.3482
                                val hospitalLng = selectedDefaultAddress?.longitude ?: 44.2191

                                val suppliersWithCalculatedDistance = remember(selectedDefaultAddress, userAddressesList) {
                                    FirebaseService.fallbackUsers.filter { it.role == "supplier" }.map { sup ->
                                        val supCoords = FirebaseService.fallbackAddresses.find { it.userId == sup.userId }
                                        val dist = if (supCoords != null) {
                                            FirebaseService.calculateDistanceKm(hospitalLat, hospitalLng, supCoords.latitude, supCoords.longitude)
                                        } else {
                                            35.0 // fallback
                                        }
                                        Pair(sup, dist)
                                    }.sortedBy { it.second } // Sort ascendingly based on distance
                                }

                                LazyColumn(
                                    modifier = Modifier.weight(1f).fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    item {
                                        Text("📦 العروض السعرية المتاحة لطلب الدواء وبدأ التسديد:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MedBluePrimary)
                                    }

                                    // Display Price Bids
                                    val unfilteredOffers = FirebaseService.fallbackPriceOffers
                                    val filteredOffers = unfilteredOffers.filter {
                                        it.medicineName.contains(searchQuery, ignoreCase = true) || searchQuery.isEmpty()
                                    }

                                    if (filteredOffers.isEmpty()) {
                                        item {
                                            Box(modifier = Modifier.fillMaxWidth().padding(30.dp), contentAlignment = Alignment.Center) {
                                                Text("لا تتوفر أي عروض مطابقة للبحث حالياً", color = Color.Gray, fontSize = 11.sp)
                                            }
                                        }
                                    } else {
                                        items(filteredOffers) { offer ->
                                            // Determine matching distance to this supplier
                                            val sDist = suppliersWithCalculatedDistance.find { it.first.userId == offer.supplierId }?.second ?: 25.0

                                            Card(
                                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                                shape = RoundedCornerShape(12.dp),
                                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Column(modifier = Modifier.padding(14.dp)) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Column {
                                                            Text(offer.medicineName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MedBluePrimary)
                                                            Text("بواسطة: ${offer.supplierName}", fontSize = 11.sp, color = Color.Gray)
                                                        }

                                                        // Distance Badge
                                                        Box(
                                                            modifier = Modifier
                                                                .background(MedBluePrimary.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                                                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                                        ) {
                                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                                Icon(Icons.Default.Place, contentDescription = null, size = 12.dp, tint = MedBluePrimary)
                                                                Spacer(modifier = Modifier.width(2.dp))
                                                                Text("📍 $sDist كم", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = MedBluePrimary)
                                                            }
                                                        }
                                                    }

                                                    Spacer(modifier = Modifier.height(8.dp))

                                                    Text(
                                                        "ملاحظة المورد: \"${offer.notes}\"",
                                                        fontSize = 11.sp,
                                                        color = Color.DarkGray,
                                                        modifier = Modifier.fillMaxWidth().background(Color(0xFFF8FAFC)).padding(8.dp)
                                                    )

                                                    Spacer(modifier = Modifier.height(8.dp))

                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Column {
                                                            Text("التكلفة المباشرة (كمية: ${offer.quantity}):", fontSize = 9.sp, color = Color.Gray)
                                                            Text("${offer.price * offer.quantity} دولار", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MedGreenPrimary)
                                                        }

                                                        if (offer.status == "pending") {
                                                            Button(
                                                                onClick = {
                                                                    activeCheckoutOffer = offer
                                                                    currentScreenState = "delivery_method"
                                                                },
                                                                colors = ButtonDefaults.buttonColors(containerColor = MedGreenPrimary),
                                                                shape = RoundedCornerShape(8.dp),
                                                                modifier = Modifier.testTag("accept_offer_action_${offer.priceOfferId}")
                                                            ) {
                                                                Text("قبول والتوجه للشحن 🚚", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                            }
                                                        } else {
                                                            Box(
                                                                modifier = Modifier
                                                                    .background(Color(0xFFFFFAF0), RoundedCornerShape(8.dp))
                                                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                                            ) {
                                                                Text("الحالة: ${offer.status.uppercase()}", color = MedGreenPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    item {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text("📋 سجل العمليات والمشتريات المفتوحة:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.DarkGray)
                                    }

                                    // Display list of orders
                                    val myOrders = FirebaseService.fallbackOrders.filter { it.hospitalId == loggedUser.userId }
                                    if (myOrders.isEmpty()) {
                                        item {
                                            Box(modifier = Modifier.fillMaxWidth().padding(14.dp), contentAlignment = Alignment.Center) {
                                                Text("لا توجد طلبيات مسجلة باسم المستشفى بعد.", fontSize = 11.sp, color = Color.Gray)
                                            }
                                        }
                                    } else {
                                        items(myOrders) { o ->
                                            Card(
                                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column {
                                                        Text(o.medicineName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                        Text("سعر الكلي: ${o.price * o.quantity} $", fontSize = 10.sp)
                                                        Text("شحن: ${if (o.deliveryMethod == "self") "سأتوصل بنفسي" else "عبر المنصة"}", fontSize = 10.sp, color = Color.Gray)
                                                    }

                                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                        Box(
                                                            modifier = Modifier
                                                                .background(Color(0xFFF1F5F9), RoundedCornerShape(4.dp))
                                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                                        ) {
                                                            Text(o.status.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MedBluePrimary)
                                                        }

                                                        // Schedule check button
                                                        Button(
                                                            onClick = {
                                                                activeCheckoutOrder = o
                                                                currentScreenState = "schedules"
                                                            },
                                                            colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary),
                                                            shape = RoundedCornerShape(4.dp),
                                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                            modifier = Modifier.height(30.dp)
                                                        ) {
                                                            Text("جدولة الاستلام", fontSize = 9.sp)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                            } else if (loggedUser.role == "supplier") {
                                // ==========================================
                                // Supplier View
                                // ==========================================

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("📥 عروضي المرفوعة للمستشفيات والطلبيات معلقة السداد:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.DarkGray)
                                    Button(
                                        onClick = { currentScreenState = "bank_accounts" },
                                        colors = ButtonDefaults.buttonColors(containerColor = MedGreenPrimary),
                                        shape = RoundedCornerShape(20.dp),
                                        modifier = Modifier.height(30.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp)
                                    ) {
                                        Text("إعدادات حساباتي البنكية 🏦", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                LazyColumn(
                                    modifier = Modifier.weight(1f).fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // Display supplier's bids
                                    val supplierOffers = FirebaseService.fallbackPriceOffers.filter { it.supplierId == loggedUser.userId }
                                    items(supplierOffers) { bid ->
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = Color.White),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(bid.medicineName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                    Box(
                                                        modifier = Modifier
                                                            .background(Color(0xFFEFF6FF), RoundedCornerShape(4.dp))
                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    ) {
                                                        Text(bid.status.uppercase(), color = MedBluePrimary, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text("سعر العرض الكلي: ${bid.price * bid.quantity} دولار", fontSize = 11.sp, color = Color.Gray)
                                            }
                                        }
                                    }

                                    item {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text("📄 التسديد والحوالات المالية الواردة للمطابقة:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.DarkGray)
                                    }

                                    // Display Payments list
                                    val incomingPayments = FirebaseService.fallbackPayments.filter { it.supplierId == loggedUser.userId }
                                    if (incomingPayments.isEmpty()) {
                                        item {
                                            Box(modifier = Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                                                Text("لا توجد دفعات مالية من المستشفيات معلقة للمطابقة بعد.", color = Color.Gray, fontSize = 11.sp)
                                            }
                                        }
                                    } else {
                                        items(incomingPayments) { p ->
                                            Card(
                                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Column(modifier = Modifier.padding(14.dp)) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Column {
                                                            Text("تحويل من مستشفى: ${p.hospitalName}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                            Text("طريقة السداد: ${p.paymentMethod}", fontSize = 10.sp, color = Color.Gray)
                                                            Text("المبلع المحوّل: ${p.amount} ${p.currency}", fontWeight = FontWeight.Bold, color = MedGreenPrimary, fontSize = 11.sp)
                                                        }

                                                        Box(
                                                            modifier = Modifier
                                                                .background(
                                                                    if (p.status == "confirmed") Color(0xFFD1FAE5) else Color(0xFFFEF3C7),
                                                                    RoundedCornerShape(4.dp)
                                                                )
                                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                                        ) {
                                                            Text(p.status.uppercase(), color = if (p.status == "confirmed") MedGreenPrimary else Color(0x7F000000), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                                        }
                                                    }

                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .background(Color(0xFFF1F5F9), RoundedCornerShape(6.dp))
                                                            .padding(8.dp)
                                                    ) {
                                                        Column {
                                                            Text("رقم مرجع الحوالة: ${p.receiptUrl}", fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                                            if (p.receiptNote.isNotBlank()) {
                                                                Text("ملاحظة: \"${p.receiptNote}\"", fontSize = 9.sp, color = Color.DarkGray)
                                                            }
                                                        }
                                                    }

                                                    if (p.status == "pending") {
                                                        Spacer(modifier = Modifier.height(8.dp))
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                        ) {
                                                            Button(
                                                                onClick = {
                                                                    FirebaseService.updatePaymentStatus(p.paymentId, "confirmed", {
                                                                        Toast.makeText(context, "تم تأكيد واعتماد استلام حوالة المستشفى بنجاح! ✔", Toast.LENGTH_SHORT).show()
                                                                        refreshCurrentUserData()
                                                                    }, {})
                                                                },
                                                                colors = ButtonDefaults.buttonColors(containerColor = MedGreenPrimary),
                                                                modifier = Modifier.weight(1f).height(34.dp)
                                                            ) {
                                                                Text("تأكيد الاستلام ✔", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                            }

                                                            Button(
                                                                onClick = {
                                                                    FirebaseService.updatePaymentStatus(p.paymentId, "rejected", {
                                                                        Toast.makeText(context, "تم رفض دفعة الحوالة ومخاطبة المستشفى بالتدقيق.", Toast.LENGTH_SHORT).show()
                                                                        refreshCurrentUserData()
                                                                    }, {})
                                                                },
                                                                colors = ButtonDefaults.buttonColors(containerColor = MedRedPrimary),
                                                                modifier = Modifier.weight(1f).height(34.dp)
                                                            ) {
                                                                Text("رفض وبلاغ ⚠️", fontSize = 10.sp)
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                }

                            } else {
                                // ==========================================
                                // Platform Supervisor Panel
                                // ==========================================
                                Text("👑 طلبات التوصيل المعلقة المشحونة عبر المنصة (MedLink)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.DarkGray)

                                val dRequests = FirebaseService.fallbackDeliveryRequests
                                if (dRequests.isEmpty()) {
                                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Icon(Icons.Default.PendingActions, contentDescription = null, size = 48.dp, color = Color.LightGray)
                                            Text("لا تتوفر أي طلبات توصيل تحتاج التعيين عبر المنصة حالياً.", fontSize = 11.sp, color = Color.Gray)
                                        }
                                    }
                                } else {
                                    LazyColumn(
                                        modifier = Modifier.weight(1f).fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        items(dRequests) { r ->
                                            Card(
                                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text("طلب توصيل رقم: ${r.deliveryId.takeLast(6)}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                        Box(
                                                            modifier = Modifier
                                                                .background(Color(0xFFFEF3C7), RoundedCornerShape(4.dp))
                                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                                        ) {
                                                            Text(r.status.uppercase(), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFCA8A04))
                                                        }
                                                    }

                                                    Divider(color = Color(0xFFF1F5F9))

                                                    Text("📍 من: ${r.pickupAddress}", fontSize = 10.sp, color = Color.Gray)
                                                    Text("🏁 إلى: ${r.deliveryAddress}", fontSize = 10.sp, color = Color.Gray)
                                                    Text("المسافة المرصودة: ${r.distance} كم", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                    Text("كلفة التوصيل المبرر المقررة: YER ${r.estimatedPrice}", fontSize = 11.sp, color = MedGreenPrimary, fontWeight = FontWeight.Bold)

                                                    if (!r.adminAssigned) {
                                                        Button(
                                                            onClick = {
                                                                FirebaseService.assignDeliveryDriver(r.deliveryId, "صالح الماوري - السائق المعتمد", {
                                                                    Toast.makeText(context, "تم تعيين الناقل والشاحنة رقم 3 بنجاح! 🚚", Toast.LENGTH_SHORT).show()
                                                                    refreshCurrentUserData()
                                                                }, {})
                                                            },
                                                            colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary),
                                                            modifier = Modifier.fillMaxWidth().height(36.dp)
                                                        ) {
                                                            Text("تعيين المندوب ومباشرة الشحن 🚚", fontSize = 10.sp)
                                                        }
                                                    } else {
                                                        Box(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .background(Color(0xFFF0FDF4), RoundedCornerShape(6.dp))
                                                                .padding(8.dp),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text("تم تعيين الناقل وباشر شحن المواد المبردة بنجاح ✔", color = MedGreenPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
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

    // --- Guide Modal Dialog on startup ---
    if (showExplanationHelper) {
        AlertDialog(
            onDismissRequest = { showExplanationHelper = false },
            title = {
                Text(
                    "بوابة التجربة لنظام الدفع والتوصيل المرن 🏥 🚚",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "تطبيق MedLink Yemen يسمح للجهات بتنظيم خطط الشحن والدفع المباشر:",
                        fontSize = 11.sp,
                        color = Color.DarkGray
                    )
                    Text(
                        "1️⃣ إضافة العناوين الجغرافية (AddAddressScreen): حفظ المحافظة، المديرية والحي بفضل خوارزمية GPS ونفي الخلاف الجغرافي.",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                    Text(
                        "2️⃣ احتساب المسافة (Haversine Formula): ترتيب تلقائي للموردين الأقرب جغرافياً وعرض المسافة الدقيقة بالكم على بطاقة الأسعار.",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                    Text(
                        "3️⃣ خيار الشحن المرن (DeliveryMethodScreen): الخيار للمستشفى إما إسناد النقل للمورد ذاتياً، أو الشحن المبرد الآمن بكفاءة عبر المنصة.",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                    Text(
                        "4️⃣ الحوالات البنكية المباشرة (PaymentScreen): توفير قائمة حسابات الصرف البنكية للمورد، وإمكانية إرفاق Screenshot أو PDF للسداد الفوري مع دقات تنبيه FCM.",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                    Text(
                        "5️⃣ جدولة تسليم مبرمجة (DeliveryScheduleScreen): فرز المواعيد الشاغرة لدى الطرفين والتفاوض آلياً حتى التوافق المطلق.",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showExplanationHelper = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary)
                ) {
                    Text("البدء بالتجربة الحالية 👍", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
