package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BranchOffer
import com.example.model.Order
import com.example.model.Branch
import com.example.model.OrderStatus
import com.example.service.FirebaseService
import com.example.ui.theme.MedBluePrimary
import com.example.ui.theme.MedGreenPrimary
import com.example.ui.theme.MedRedPrimary
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.OnSurfaceDark
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientOrderDetailScreen(
    order: Order,
    offers: List<BranchOffer>,
    onBackClick: () -> Unit
) {
    val acceptedOffer = remember(offers) {
        offers.find { it.status == "accepted" }
    }

    var subOrders by remember { mutableStateOf<List<Order>>(emptyList()) }
    var branches by remember { mutableStateOf<List<Branch>>(emptyList()) }

    LaunchedEffect(order.orderId) {
        FirebaseService.getOrders { allOrders ->
            subOrders = allOrders.filter { it.parentOrderId == order.orderId }
        }
        FirebaseService.getBranches { fetched ->
            branches = fetched
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تفاصيل حالة الطلب 📋", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MedBluePrimary, titleContentColor = Color.White)
            )
        }
    ) { paddingVals ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingVals)
                .fillMaxSize()
                .background(Color(0xFFF8FAFC))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- Header Status Info ---
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (order.status == "confirmed") MedGreenPrimary.copy(alpha = 0.05f) else MedBluePrimary.copy(alpha = 0.05f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (order.status == "confirmed") MedGreenPrimary.copy(alpha = 0.2f) else MedBluePrimary.copy(alpha = 0.2f),
                            RoundedCornerShape(12.dp)
                        )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .background(
                                    if (order.status == "confirmed") MedGreenPrimary.copy(alpha = 0.15f) else MedBluePrimary.copy(alpha = 0.15f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (order.status == "confirmed") Icons.Default.CheckCircle else Icons.Default.Sensors,
                                contentDescription = null,
                                tint = if (order.status == "confirmed") MedGreenPrimary else MedBluePrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Text(
                            text = if (order.status == "confirmed") "طلب مُعمَّد ومكتمل ✔" else "طلب معلق في نظام البث 📡",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = if (order.status == "confirmed") MedGreenPrimary else MedBluePrimary
                        )

                        Text(
                            text = if (order.status == "confirmed") {
                                "تم اختيار العرض الأفضل وتثبيته في مستودعات الشفاء بنجاح!"
                            } else {
                                "جاري بث الاحتياج الدوائي ومراجعته من قبل مدراء الفروع لتوفير أفضل الأسعار."
                            },
                            fontSize = 11.sp,
                            color = Color.DarkGray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // --- Expected Delivery Date Banner ---
            if (order.scheduledDeliveryDate > 0L) {
                item {
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale("ar"))
                    val dateStr = sdf.format(Date(order.scheduledDeliveryDate))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SuccessGreen.copy(alpha = 0.08f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, SuccessGreen, RoundedCornerShape(12.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(SuccessGreen.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalShipping,
                                    contentDescription = null,
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "🚚 موعد التسليم المتوقع",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = SuccessGreen
                                )
                                Text(
                                    text = "موعد التسليم المتوقع: $dateStr",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = OnSurfaceDark
                                )
                            }
                        }
                    }
                }
            }

            // --- Order Content Details ---
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("📝 تفاصيل الاحتياج الدوائي المرفوع:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.DarkGray)
                        Text(
                            text = order.orderContent,
                            fontSize = 12.sp,
                            color = Color.DarkGray,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("رقم الطلب المرجعي:", fontSize = 9.sp, color = Color.Gray)
                                Text("#${order.orderId.takeLast(8)}", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.DarkGray)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("تاريخ الطلب:", fontSize = 9.sp, color = Color.Gray)
                                Text("منذ ساعة تقريباً", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.DarkGray)
                            }
                        }

                        Divider(color = Color(0xFFF1F5F9))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("محافظة التسليم: ${order.clientGovernorate}", fontSize = 11.sp, color = Color.DarkGray)
                            Text("نوع البث: " + if (order.broadcastType == "all") "بث كامل لكافة الفروع" else "محدد جغرافياً للفروع القريبة", fontSize = 11.sp, color = Color.DarkGray)
                        }
                    }
                }
            }

            // --- Sub-orders / Separate Shipments Section ---
            if (subOrders.isNotEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)), // light warning/info background
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFF59E0B), RoundedCornerShape(12.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color(0xFFD97706),
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "📦 شحنات فرعية مضافة للطلب الرئيسي",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF92400E)
                                )
                            }

                            Text(
                                text = "تنبيه: بعض الأصناف في طلبك تم تحويلها لتصل بشحنة منفصلة من فرع آخر لضمان سرعة التسليم وتفادي نقص المخزون.",
                                fontSize = 11.sp,
                                color = Color(0xFFB45309)
                            )

                            Divider(color = Color(0xFFF59E0B).copy(alpha = 0.3f))

                            subOrders.forEachIndexed { index, subOrder ->
                                val targetBranchId = subOrder.targetBranches.firstOrNull() ?: ""
                                val subBranch = branches.find { it.branchId == targetBranchId }
                                val subBranchName = subBranch?.branchName ?: "فرع بديل"
                                
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.White.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "شحنة فرعية #${index + 1} (${subBranchName})",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = Color(0xFF0F172A)
                                        )
                                        
                                        // Status badge for this sub-order
                                        val statusLabel = when (subOrder.orderStatus) {
                                            is OrderStatus.Draft -> "مسودة"
                                            is OrderStatus.Submitted -> "بانتظار التأكيد / التسعير"
                                            is OrderStatus.Allocated -> "تم حجز وتجهيز المخزون ✅"
                                            is OrderStatus.PartiallyShipped -> "شحن جزئي / كميات ناقصة ⚠️"
                                            is OrderStatus.Invoiced -> "بانتظار الشحن والتوصيل 🚚"
                                            is OrderStatus.Delivered -> "تم التسليم ومطابقة الشحنة 📦"
                                            else -> "قيد المعالجة"
                                        }
                                        val badgeColor = when (subOrder.orderStatus) {
                                            is OrderStatus.Delivered -> MedGreenPrimary
                                            is OrderStatus.PartiallyShipped -> Color(0xFFD97706)
                                            else -> MedBluePrimary
                                        }
                                        Box(
                                            modifier = Modifier
                                                .background(badgeColor.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = statusLabel,
                                                color = badgeColor,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    // List items in this sub-order
                                    val itemsList = subOrder.orderLines.map {
                                        "${it.product.commercialName} (${it.requestedQty} كرتون)"
                                    }.joinToString("، ")
                                    
                                    Text(
                                        text = "الأصناف المحولة: $itemsList",
                                        fontSize = 10.sp,
                                        color = Color.DarkGray
                                    )
                                }
                                
                                if (index < subOrders.size - 1) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                }
                            }
                        }
                    }
                }
            }

            // --- Conditionals: If Confirmed -> Show Accepted Offer Summary ---
            if (order.status == "confirmed" && acceptedOffer != null) {
                item {
                    Text("💰 العرض الذي تم تعميده وتأكيده للفرع:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.DarkGray)
                }

                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.border(1.dp, MedGreenPrimary, RoundedCornerShape(12.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(acceptedOffer.branchName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MedBluePrimary)
                                    Text("بإدارة: ${acceptedOffer.managerName}", fontSize = 10.sp, color = Color.Gray)
                                }
                                Box(
                                    modifier = Modifier
                                        .background(MedGreenPrimary.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("معتمد ✔", color = MedGreenPrimary, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                }
                            }

                            Divider(color = Color(0xFFF1F5F9))

                            Text("تفاصيل العرض المعتمد: ${acceptedOffer.offerDetails}", fontSize = 11.sp, color = Color.DarkGray)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("القيمة الإجمالية:", fontSize = 9.sp, color = Color.Gray)
                                    Text("${acceptedOffer.totalPrice} YER", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MedGreenPrimary)
                                }
                                Column {
                                    Text("تكاليف الشحن اللوجستي المبرد:", fontSize = 9.sp, color = Color.Gray)
                                    Text("${acceptedOffer.shippingCost} YER", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.DarkGray)
                                }
                                Column {
                                    Text("مدة التوصيل:", fontSize = 9.sp, color = Color.Gray)
                                    Text("${acceptedOffer.deliveryDays} أيام", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MedBluePrimary)
                                }
                            }

                            if (acceptedOffer.notes.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFEFF6FF), RoundedCornerShape(6.dp))
                                        .padding(10.dp)
                                ) {
                                    Text("📌 شروط السداد واللوجستيات المتفق عليها: ${acceptedOffer.paymentTerms}\nملاحظات: ${acceptedOffer.notes}", fontSize = 10.sp, color = MedBluePrimary)
                                }
                            }
                        }
                    }
                }
            } else {
                // broadcast / pending
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(color = MedBluePrimary, modifier = Modifier.size(24.dp))
                            Text("بانتظار تسعير الفروع وتلقي العروض المناسبة.. ⏳", fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Center)
                        }
                    }
                }
            }
        }
    }
}
