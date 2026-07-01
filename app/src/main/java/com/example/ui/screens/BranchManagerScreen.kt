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
import com.example.model.BranchOffer
import com.example.model.Order
import com.example.model.User
import com.example.model.UserAddress
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.example.service.FirebaseService
import com.example.ui.theme.MedBluePrimary
import com.example.ui.theme.MedGreenPrimary
import com.example.ui.theme.MedRedPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BranchManagerScreen(
    currentUser: User,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    var activeTab by remember { mutableStateOf("client_orders") } // "client_orders" or "my_offers"

    var branchManagerScreenState by remember { mutableStateOf("dashboard") } // dashboard, addresses, add_address, edit_address
    var selectedOrderForBranchDetail by remember { mutableStateOf<Order?>(null) }
    var activeSubTab by remember { mutableStateOf("new") } // "new", "submitted", "accepted", "rejected"
    var editingBranchAddress by remember { mutableStateOf<UserAddress?>(null) }
    var defaultAddress by remember { mutableStateOf<UserAddress?>(null) }

    // Form states for bidding
    var activeBiddingOrder by remember { mutableStateOf<Order?>(null) }
    var showBiddingDialog by remember { mutableStateOf(false) }

    var offerDetails by remember { mutableStateOf("") }
    var totalPriceStr by remember { mutableStateOf("") }
    var deliveryDaysStr by remember { mutableStateOf("") }
    var shippingCostStr by remember { mutableStateOf("") }
    var paymentTerms by remember { mutableStateOf("الدفع نقداً أو تحويل كريمي") }
    var isSubmittingOffer by remember { mutableStateOf(false) }

    // Lists
    var incomingOrders by remember { mutableStateOf<List<Order>>(emptyList()) }
    var myOffers by remember { mutableStateOf<List<BranchOffer>>(emptyList()) }

    fun refreshData() {
        // Fetch orders targeting this branch
        FirebaseService.getOrders { allOrders ->
            incomingOrders = allOrders.filter { order ->
                if (order.status == "broadcast" || order.status == "offer_received") {
                    when (order.broadcastType) {
                        "all" -> true
                        "nearby" -> order.clientGovernorate == currentUser.governorate
                        "selected" -> order.targetBranches.contains(currentUser.branchId)
                        else -> true
                    }
                } else false
            }.sortedByDescending { order -> order.createdAt }
        }

        // Fetch our submitted offers
        FirebaseService.getAllBranchOffers { allOffers ->
            myOffers = allOffers.filter { it.branchId == currentUser.branchId }.sortedByDescending { it.createdAt }
        }

        // Fetch addresses to find default
        FirebaseService.getUserAddresses(currentUser.userId) { addresses ->
            defaultAddress = addresses.find { it.isDefault }
        }
    }

    LaunchedEffect(branchManagerScreenState) {
        if (branchManagerScreenState == "dashboard") {
            refreshData()
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        when (branchManagerScreenState) {
            "order_detail" -> {
                if (selectedOrderForBranchDetail != null) {
                    OrderDetailForBranchScreen(
                        order = selectedOrderForBranchDetail!!,
                        currentUser = currentUser,
                        onBackClick = { branchManagerScreenState = "dashboard" },
                        onSendOfferClick = { branchManagerScreenState = "send_offer" },
                        onRefresh = { refreshData() }
                    )
                } else {
                    branchManagerScreenState = "dashboard"
                }
            }
            "send_offer" -> {
                if (selectedOrderForBranchDetail != null) {
                    SendPriceOfferScreen(
                        order = selectedOrderForBranchDetail!!,
                        currentUser = currentUser,
                        onBackClick = { branchManagerScreenState = "order_detail" },
                        onSuccessSubmit = {
                            branchManagerScreenState = "dashboard"
                            refreshData()
                        }
                    )
                } else {
                    branchManagerScreenState = "dashboard"
                }
            }
            "addresses" -> {
                BranchAddressesScreen(
                    currentUser = currentUser,
                    onNavigateBack = { branchManagerScreenState = "dashboard" },
                    onAddNewAddress = { branchManagerScreenState = "add_address" },
                    onEditAddress = { address ->
                        editingBranchAddress = address
                        branchManagerScreenState = "edit_address"
                    }
                )
            }
            "add_address" -> {
                AddAddressScreen(
                    currentUser = currentUser,
                    existingAddress = null,
                    onBackClick = { branchManagerScreenState = "addresses" },
                    onSaveSuccess = { branchManagerScreenState = "addresses" }
                )
            }
            "edit_address" -> {
                AddAddressScreen(
                    currentUser = currentUser,
                    existingAddress = editingBranchAddress,
                    onBackClick = { branchManagerScreenState = "addresses" },
                    onSaveSuccess = { branchManagerScreenState = "addresses" }
                )
            }
            else -> {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Column {
                                    Text("ميد-لينك | إدارة الفرع 💼", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                                    Text("الفرع: ${currentUser.branchName} (${currentUser.governorate})", fontSize = 11.sp, color = Color.LightGray)
                                }
                            },
                            actions = {
                                IconButton(onClick = { refreshData() }) {
                                    Icon(Icons.Default.Refresh, contentDescription = "تحديث", tint = Color.White)
                                }
                                TextButton(onClick = onLogout) {
                                    Text("خروج 🚪", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(containerColor = MedBluePrimary)
                        )
                    }
                ) { paddingVals ->
                    Column(
                        modifier = Modifier
                            .padding(paddingVals)
                            .fillMaxSize()
                            .background(Color(0xFFF8FAFC))
                    ) {
                        // Branch card info
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("👤 المدير المسؤول: ${currentUser.name}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MedBluePrimary)
                                        Text("📞 هاتف التواصل: ${currentUser.phone}", fontSize = 11.sp, color = Color.Gray)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .background(MedGreenPrimary.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text("فرع نشط 🟢", color = MedGreenPrimary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Divider(color = Color(0xFFF1F5F9), thickness = 1.dp)

                                // مواقع الفرع والمستودعات Section
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "🏢 مواقع الفرع والمستودعات",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.DarkGray,
                                            fontSize = 12.sp
                                        )
                                        
                                        TextButton(
                                            onClick = { branchManagerScreenState = "addresses" },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            modifier = Modifier.height(28.dp).testTag("manage_locations_btn")
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("إدارة المواقع ←", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }

                                    if (defaultAddress != null) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
                                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                                                .padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Place, contentDescription = null, tint = MedBluePrimary, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = defaultAddress!!.label,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp,
                                                    color = Color.DarkGray
                                                )
                                                Text(
                                                    text = defaultAddress!!.fullAddress,
                                                    fontSize = 10.sp,
                                                    color = Color.Gray,
                                                    maxLines = 1
                                                )
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .background(MedGreenPrimary.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text("افتراضي", color = MedGreenPrimary, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    } else {
                                        Text(
                                            text = "لم يتم تحديد موقع افتراضي للفرع بعد.",
                                            fontSize = 11.sp,
                                            color = Color.Gray,
                                            modifier = Modifier.padding(vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }

            // Tab switcher
            TabRow(
                selectedTabIndex = if (activeTab == "client_orders") 0 else 1,
                containerColor = Color.White,
                contentColor = MedBluePrimary
            ) {
                Tab(
                    selected = activeTab == "client_orders",
                    onClick = { activeTab = "client_orders" },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("طلبات معلقة للتسعير 📡", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            if (incomingOrders.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Badge(containerColor = MedRedPrimary) {
                                    Text(incomingOrders.size.toString(), color = Color.White, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                )
                Tab(
                    selected = activeTab == "my_offers",
                    onClick = { activeTab = "my_offers" },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("العروض السعرية المرفوعة 💰", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            if (myOffers.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Badge(containerColor = MedBluePrimary) {
                                    Text(myOffers.size.toString(), color = Color.White, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                )
            }

            AnimatedContent(
                targetState = activeTab,
                label = "ManagerTabTransition",
                modifier = Modifier.weight(1f)
            ) { tab ->
                when (tab) {
                    "client_orders" -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Secondary Sub-Tabs for incoming orders
                            val subTabs = listOf(
                                Triple("new", "جديدة 🆕", Icons.Default.FiberNew),
                                Triple("submitted", "قدمت عرضاً ✉️", Icons.Default.Send),
                                Triple("accepted", "مقبولة ✅", Icons.Default.CheckCircle),
                                Triple("rejected", "مرفوضة ❌", Icons.Default.Cancel)
                            )
                            
                            TabRow(
                                selectedTabIndex = subTabs.indexOfFirst { it.first == activeSubTab },
                                containerColor = Color(0xFFF1F5F9),
                                contentColor = MedBluePrimary,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                subTabs.forEach { (key, label, icon) ->
                                    Tab(
                                        selected = activeSubTab == key,
                                        onClick = { activeSubTab = key },
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp))
                                                Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    )
                                }
                            }

                            val filteredOrders = incomingOrders.filter { order ->
                                val offer = myOffers.find { it.orderId == order.orderId }
                                when (activeSubTab) {
                                    "new" -> offer == null
                                    "submitted" -> offer != null && (offer.status == "pending" || offer.status == "negotiating")
                                    "accepted" -> offer != null && offer.status == "accepted"
                                    "rejected" -> offer != null && offer.status == "rejected"
                                    else -> true
                                }
                            }

                            if (filteredOrders.isEmpty()) {
                                Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Icon(Icons.Default.PendingActions, contentDescription = null, modifier = Modifier.size(56.dp), tint = Color.LightGray)
                                        Text(
                                            text = when (activeSubTab) {
                                                "new" -> "لا تتوفر أي طلبات معلقة جديدة لتسعيرها حالياً."
                                                "submitted" -> "لم تقم بتقديم عروض تسعير معلقة حالياً."
                                                "accepted" -> "لا تتوفر عروض معتمدة من العملاء بعد."
                                                else -> "لا تتوفر أي عروض مرفوضة حالياً."
                                            },
                                            color = Color.Gray,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .weight(1f)
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(filteredOrders) { order ->
                                        val offer = myOffers.find { it.orderId == order.orderId }
                                        val hasAlreadyOffered = offer != null

                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = Color.White),
                                            shape = RoundedCornerShape(12.dp),
                                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    selectedOrderForBranchDetail = order
                                                    branchManagerScreenState = "order_detail"
                                                }
                                                .testTag("manager_order_item_${order.orderId}")
                                        ) {
                                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column {
                                                        Text("منشأة العميل: ${order.clientName}", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = MedBluePrimary)
                                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                            Text("المحافظة: ${order.clientGovernorate}", fontSize = 10.sp, color = Color.Gray)
                                                            Box(
                                                                modifier = Modifier
                                                                    .background(MedBluePrimary.copy(alpha = 0.05f), RoundedCornerShape(4.dp))
                                                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                                                            ) {
                                                                Text(if (order.clientType == "hospital") "مستشفى 🏥" else "صيدلية 💊", fontSize = 9.sp, color = MedBluePrimary)
                                                            }
                                                        }
                                                    }

                                                    val urgencyColor = when (order.urgencyLevel) {
                                                        "critical" -> MedRedPrimary
                                                        "high" -> Color(0xFFEAB308)
                                                        else -> MedGreenPrimary
                                                    }
                                                    val urgencyLabel = when (order.urgencyLevel) {
                                                        "critical" -> "طارئ"
                                                        "high" -> "عاجل"
                                                        else -> "عادي"
                                                    }

                                                    Box(
                                                        modifier = Modifier
                                                            .background(urgencyColor.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                                    ) {
                                                        Text(urgencyLabel, color = urgencyColor, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                                    }
                                                }

                                                Text(
                                                    text = if (order.orderContent.length > 100) order.orderContent.take(100) + "..." else order.orderContent,
                                                    fontSize = 12.sp,
                                                    color = Color.DarkGray,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .background(Color(0xFFF8FAFC), RoundedCornerShape(6.dp))
                                                        .padding(10.dp)
                                                )

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        "نطاق البث: " + if (order.broadcastType == "all") "بث كامل 🌐" else "محدد جغرافياً 📍",
                                                        fontSize = 10.sp,
                                                        color = Color.Gray
                                                    )

                                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                        if (hasAlreadyOffered) {
                                                            val statusColor = when (offer!!.status) {
                                                                "accepted" -> MedGreenPrimary
                                                                "rejected" -> MedRedPrimary
                                                                "negotiating" -> Color(0xFFF59E0B)
                                                                else -> Color.Gray
                                                            }
                                                            val label = when (offer.status) {
                                                                "accepted" -> "مقبول ✔"
                                                                "rejected" -> "مرفوض ❌"
                                                                "negotiating" -> "تفاوض نشط 💬"
                                                                else -> "العرض معلق ⏳"
                                                            }
                                                            Box(
                                                                modifier = Modifier
                                                                    .background(statusColor.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                                                            ) {
                                                                Text(label, color = statusColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                            }
                                                        } else {
                                                            Button(
                                                                onClick = {
                                                                    selectedOrderForBranchDetail = order
                                                                    branchManagerScreenState = "order_detail"
                                                                },
                                                                colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary),
                                                                shape = RoundedCornerShape(6.dp),
                                                                contentPadding = PaddingValues(horizontal = 10.dp),
                                                                modifier = Modifier.height(28.dp).testTag("view_and_bid_${order.orderId}")
                                                            ) {
                                                                Text("تفاصيل وتقديم عرض 💰", fontSize = 10.sp, fontWeight = FontWeight.Bold)
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

                    "my_offers" -> {
                        if (myOffers.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                                    Text("لا توجد عروض سعرية مسجلة للفرع بعد.", color = Color.Gray, fontSize = 13.sp)
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(myOffers) { offer ->
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        shape = RoundedCornerShape(12.dp),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    "عرض لطلب رقم: ${offer.orderId.takeLast(6)}",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = MedBluePrimary
                                                )

                                                val statusColor = when (offer.status) {
                                                    "accepted" -> MedGreenPrimary
                                                    "rejected" -> MedRedPrimary
                                                    else -> Color(0xFFCA8A04)
                                                }
                                                val statusLabel = when (offer.status) {
                                                    "accepted" -> "تم القبول ✔"
                                                    "rejected" -> "مرفوض ❌"
                                                    else -> "بانتظار التعميد ⏳"
                                                }

                                                Box(
                                                    modifier = Modifier
                                                        .background(statusColor.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                                ) {
                                                    Text(statusLabel, color = statusColor, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(8.dp))

                                            Text("تفاصيل العرض: ${offer.offerDetails}", fontSize = 11.sp, color = Color.DarkGray)
                                            Divider(modifier = Modifier.padding(vertical = 6.dp), color = Color(0xFFF1F5F9))

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column {
                                                    Text("كلفة الأدوية الكلية:", fontSize = 9.sp, color = Color.Gray)
                                                    Text("${offer.totalPrice} YER", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MedGreenPrimary)
                                                }
                                                Column {
                                                    Text("كلفة الشحن والتوصيل:", fontSize = 9.sp, color = Color.Gray)
                                                    Text("${offer.shippingCost} YER", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.DarkGray)
                                                }
                                                Column {
                                                    Text("التسليم المتوقع:", fontSize = 9.sp, color = Color.Gray)
                                                    Text("${offer.deliveryDays} أيام", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MedBluePrimary)
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

    // Pricing submission Dialog
    if (showBiddingDialog && activeBiddingOrder != null) {
        val order = activeBiddingOrder!!

        AlertDialog(
            onDismissRequest = { showBiddingDialog = false },
            title = {
                Text(
                    "تسعير طلب العميل: ${order.clientName} 💸",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Text("طلب العميل:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.Gray)
                        Text(
                            order.orderContent,
                            fontSize = 11.sp,
                            color = Color.DarkGray,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF1F5F9), RoundedCornerShape(6.dp))
                                .padding(8.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    item {
                        OutlinedTextField(
                            value = totalPriceStr,
                            onValueChange = { totalPriceStr = it },
                            label = { Text("كلفة الأدوية الكلية (YER)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth().testTag("bid_total_price"),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MedGreenPrimary)
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = deliveryDaysStr,
                            onValueChange = { deliveryDaysStr = it },
                            label = { Text("أيام التوصيل المتوقعة") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth().testTag("bid_delivery_days"),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MedGreenPrimary)
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = shippingCostStr,
                            onValueChange = { shippingCostStr = it },
                            label = { Text("كلفة الشحن والتوصيل (YER)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth().testTag("bid_shipping_cost"),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MedGreenPrimary)
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = offerDetails,
                            onValueChange = { offerDetails = it },
                            label = { Text("ملاحظات وتفاصيل توفر الأدوية") },
                            placeholder = { Text("مثال: متوفر بكافة الكميات المطلوبة في تاريخ الشحن مبرد مطابق تماماً") },
                            modifier = Modifier.fillMaxWidth().testTag("bid_notes"),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MedGreenPrimary)
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = paymentTerms,
                            onValueChange = { paymentTerms = it },
                            label = { Text("شروط وطريقة السداد المقترحة") },
                            modifier = Modifier.fillMaxWidth().testTag("bid_payment_terms"),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MedGreenPrimary)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val totalPrice = totalPriceStr.toDoubleOrNull()
                        val deliveryDays = deliveryDaysStr.toIntOrNull()
                        val shippingCost = shippingCostStr.toDoubleOrNull()

                        if (totalPrice == null || totalPrice <= 0) {
                            Toast.makeText(context, "الرجاء كتابة سعر صحيح للمواد", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (deliveryDays == null || deliveryDays < 0) {
                            Toast.makeText(context, "الرجاء تحديد تاريخ توصيل منطقي باليوم", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (shippingCost == null || shippingCost < 0) {
                            Toast.makeText(context, "الرجاء تحديد كلفة الشحن بـ YER", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (offerDetails.isBlank()) {
                            Toast.makeText(context, "الرجاء كتابة تفاصيل توفر الأدوية", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        isSubmittingOffer = true
                        val newOffer = BranchOffer(
                            offerId = "",
                            orderId = order.orderId,
                            branchId = currentUser.branchId,
                            branchName = currentUser.branchName,
                            managerId = currentUser.userId,
                            managerName = currentUser.name,
                            offerDetails = offerDetails,
                            totalPrice = totalPrice,
                            currency = "YER",
                            deliveryDays = deliveryDays,
                            shippingCost = shippingCost,
                            paymentTerms = paymentTerms,
                            status = "pending",
                            createdAt = System.currentTimeMillis()
                        )

                        FirebaseService.submitBranchOffer(newOffer, {
                            Toast.makeText(context, "💰 تم تقديم العرض السعري وتثبيته للعميل بنجاح!", Toast.LENGTH_LONG).show()
                            showBiddingDialog = false
                            totalPriceStr = ""
                            deliveryDaysStr = ""
                            shippingCostStr = ""
                            offerDetails = ""
                            activeBiddingOrder = null
                            isSubmittingOffer = false
                            refreshData()
                        }, {
                            isSubmittingOffer = false
                            Toast.makeText(context, "فشل تقديم العرض", Toast.LENGTH_SHORT).show()
                        })
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MedGreenPrimary)
                ) {
                    if (isSubmittingOffer) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                    } else {
                        Text("إرسال العرض السعري ✔", fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showBiddingDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}
}
}
}
