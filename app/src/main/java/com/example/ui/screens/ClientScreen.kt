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
import com.example.service.FirebaseService
import com.example.ui.theme.MedBluePrimary
import com.example.ui.theme.MedGreenPrimary
import com.example.ui.theme.MedRedPrimary

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun ClientScreen(
    currentUser: User,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    var activeTab by remember { mutableStateOf("new_order") } // "new_order" or "my_orders"
    var clientScreenState by remember { mutableStateOf("dashboard") } // "dashboard", "new_order_flow", "digital_card_view"

    // Form States
    var orderContent by remember { mutableStateOf("") }
    var urgencyLevel by remember { mutableStateOf("normal") } // "normal", "high", "critical"
    var broadcastType by remember { mutableStateOf("all") } // "all", "nearby", "selected"
    var selectedBranches by remember { mutableStateOf<List<String>>(emptyList()) }
    var isSubmitting by remember { mutableStateOf(false) }

    // Lists
    var myOrders by remember { mutableStateOf<List<Order>>(emptyList()) }
    var activeOffersMap by remember { mutableStateOf<Map<String, List<BranchOffer>>>(emptyMap()) }
    var selectedOrderForOffers by remember { mutableStateOf<Order?>(null) }
    var showOffersDialog by remember { mutableStateOf(false) }

    // Refresh function
    fun refreshOrders() {
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
    }

    LaunchedEffect(Unit) {
        refreshOrders()
    }

    if (clientScreenState == "new_order_flow") {
        NewOrderScreen(
            userId = currentUser.userId,
            onNavigateBack = { clientScreenState = "dashboard" },
            onTrackOrderStatus = {
                clientScreenState = "dashboard"
                activeTab = "my_orders"
                refreshOrders()
            }
        )
    } else if (clientScreenState == "digital_card_view") {
        ClientDigitalCard(
            userId = currentUser.userId,
            onNavigateBack = { clientScreenState = "dashboard" }
        )
    } else {
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
                        IconButton(onClick = { refreshOrders() }) {
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
                // Tab Switcher
                TabRow(
                    selectedTabIndex = if (activeTab == "new_order") 0 else 1,
                    containerColor = Color.White,
                    contentColor = MedBluePrimary
                ) {
                    Tab(
                        selected = activeTab == "new_order",
                        onClick = { activeTab = "new_order" },
                        text = { Text("طلب توريد جديد ✍️", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                    )
                    Tab(
                        selected = activeTab == "my_orders",
                        onClick = { activeTab = "my_orders" },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("طلباتي السابقة 📋", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                if (myOrders.isNotEmpty()) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Badge(containerColor = MedGreenPrimary) {
                                        Text(myOrders.size.toString(), color = Color.White, fontSize = 10.sp)
                                    }
                                }
                            }
                        }
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
                                        colors = CardDefaults.cardColors(containerColor = MedBluePrimary),
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
                                                    "بث طلب شراء دوائي ذكي (5 خطوات) ✍️",
                                                    color = MedBluePrimary,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 14.sp
                                                )
                                                Text(
                                                    "حدد احتياجك، المرفقات، نطاق البث، ومستوى الطوارئ لنظامنا التوجيهي.",
                                                    color = Color.Gray,
                                                    fontSize = 11.sp,
                                                    textAlign = TextAlign.Right
                                                )
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .size(48.dp)
                                                    .background(MedGreenPrimary.copy(alpha = 0.15f), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Default.AddShoppingCart, contentDescription = null, tint = MedGreenPrimary)
                                            }
                                        }
                                    }
                                }

                                item {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        shape = RoundedCornerShape(12.dp),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { clientScreenState = "digital_card_view" }
                                            .testTag("launcher_digital_card_btn")
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
                                                    "بطاقتي الطبية الرقمية المعتمدة 🛡️",
                                                    color = MedBluePrimary,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 14.sp
                                                )
                                                Text(
                                                    "اعرض هويتك الطبية الموثقة ورمز الأمان للمجموعة للمشتريات.",
                                                    color = Color.Gray,
                                                    fontSize = 11.sp,
                                                    textAlign = TextAlign.Right
                                                )
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .size(48.dp)
                                                    .background(MedBluePrimary.copy(alpha = 0.15f), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Default.Badge, contentDescription = null, tint = MedBluePrimary)
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
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(myOrders) { order ->
                                    val offers = activeOffersMap[order.orderId] ?: emptyList()

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
                                                Column {
                                                    Text("الحالة:", fontSize = 9.sp, color = Color.Gray)
                                                    val statusText = when (order.status) {
                                                        "broadcast" -> "بث نشط جاري الاستقبال 📡"
                                                        "offer_received" -> "تم استلام عروض فروع! 🎉"
                                                        "confirmed" -> "تم تأكيد طلبك والتعميد ✔"
                                                        else -> order.status
                                                    }
                                                    Text(
                                                        statusText,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (order.status == "confirmed") MedGreenPrimary else MedBluePrimary
                                                    )
                                                }

                                                if (offers.isNotEmpty()) {
                                                    Button(
                                                        onClick = {
                                                            selectedOrderForOffers = order
                                                            showOffersDialog = true
                                                        },
                                                        colors = ButtonDefaults.buttonColors(containerColor = MedGreenPrimary),
                                                        shape = RoundedCornerShape(8.dp),
                                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                                        modifier = Modifier.height(34.dp)
                                                    ) {
                                                        Text("عرض العروض (${offers.size}) 💰", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                } else {
                                                    Box(
                                                        modifier = Modifier
                                                            .background(Color(0xFFF1F5F9), RoundedCornerShape(6.dp))
                                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                                    ) {
                                                        Text("بانتظار تسعير الفروع.. ⏳", fontSize = 10.sp, color = Color.Gray)
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
