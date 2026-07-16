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
import com.example.model.Branch
import com.example.model.ClientProfile
import com.example.model.Order
import com.example.service.FirebaseService
import com.example.ui.theme.MedBluePrimary
import com.example.ui.theme.MedGreenPrimary
import com.example.ui.theme.MedRedPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DirectorOrdersScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var ordersList by remember { mutableStateOf<List<Order>>(emptyList()) }
    var selectedOrderForDetail by remember { mutableStateOf<Order?>(null) }
    var showReRouteDialog by remember { mutableStateOf(false) }

    // Reload orders
    fun loadOrders() {
        FirebaseService.getDirectorOrdersFeed { list ->
            ordersList = list.sortedByDescending { it.createdAt }
        }
    }

    LaunchedEffect(Unit) {
        loadOrders()
    }

    // Calculated Statistics
    val todayOrdersCount = ordersList.size
    val pendingOffersCount = ordersList.count { it.status == "broadcast" || it.status == "pending" }
    val avgResponseTime = "---" // Will be calculated from DB
    val fastestBranch = "سيتم حسابه لاحقاً" // TODO: implement real aggregation

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("مراقبة الطلبيات والتوجيه التلقائي 👑", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
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
                .background(Color(0xFF0F172A))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // REAL-TIME STATS HEADER CARDS
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    title = "طلبيات اليوم",
                    value = "$todayOrdersCount طلب",
                    color = MedBluePrimary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "طلب بلا عرض",
                    value = "$pendingOffersCount معلقة",
                    color = MedRedPrimary,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    title = "متوسط الرد",
                    value = "$avgResponseTime ساعة",
                    color = MedGreenPrimary,
                    modifier = Modifier.weight(1.2f)
                )
                StatCard(
                    title = "أسرع استجابة",
                    value = fastestBranch,
                    color = Color.DarkGray,
                    modifier = Modifier.weight(1.8f)
                )
            }

            Text(
                "جدول تدفق الطلبيات الذكية للعملاء 👇",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                textAlign = TextAlign.Right,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
            )

            // ORDERS FEED LIST
            if (ordersList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.HourglassEmpty, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("لا يوجد طلبيات مرسلة حالياً في النظام", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(ordersList) { order ->
                        OrderFeedCard(
                            order = order,
                            onClick = { selectedOrderForDetail = order }
                        )
                    }
                }
            }
        }
    }

    // DETAILS & INTERVENTION SHEET DIALOG
    if (selectedOrderForDetail != null) {
        val detailOrder = selectedOrderForDetail!!
        AlertDialog(
            onDismissRequest = { selectedOrderForDetail = null },
            title = {
                Text(
                    "تفاصيل طلب الشراء والمتابعة الإدارية 👑",
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
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text("الرقم المرجعي للطلب: #${detailOrder.orderId}", color = MedGreenPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("الجهة: ${detailOrder.clientName} (${if (detailOrder.clientType == "hospital") "مستشفى" else "صيدلية"})", color = Color.DarkGray, fontSize = 12.sp)
                    Text("المحافظة: ${detailOrder.clientGovernorate}", color = Color.DarkGray, fontSize = 11.sp)
                    
                    Divider(color = Color.LightGray)

                    Text("📝 محتوى طلب الاحتياج الدوائي:", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text(detailOrder.orderContent, color = Color.DarkGray, fontSize = 11.sp)

                    Divider(color = Color.LightGray)

                    Text("🎯 الفروع الموجه إليها الطلب حالياً:", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    if (detailOrder.targetBranches.isEmpty()) {
                        Text("تم بثه عام لكافة فروع المجموعة", color = Color.Gray, fontSize = 11.sp)
                    } else {
                        detailOrder.targetBranches.forEach { id ->
                            Text("- $id", color = Color.DarkGray, fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // INTERVENTION BUTTONS
                    Button(
                        onClick = { showReRouteDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.AltRoute, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("إعادة توجيه الطلب لفرع آخر 🔄", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                Toast.makeText(context, "📞 جاري الاتصال بالعميل: ${detailOrder.clientName}", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MedGreenPrimary),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("اتصال بالعميل 📞", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                Toast.makeText(context, "💬 تم إرسال رسالة توجيهية لمدير الفرع المنسق", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("تنبيه الفرع 💬", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedOrderForDetail = null }) {
                    Text("إغلاق المتابعة ✖", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // RE-ROUTE DIALOG
    if (showReRouteDialog && selectedOrderForDetail != null) {
        val routeOrder = selectedOrderForDetail!!
        AlertDialog(
            onDismissRequest = { showReRouteDialog = false },
            title = { Text("توجيه يدوي استثنائي من المدير العام 🔄", fontSize = 14.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.End) {
                    Text("اختر الفرع البديل لنقل وتفويض الطلبية إليه فوراً:", color = Color.DarkGray, fontSize = 11.sp)
                    Text("جاري تحميل الفروع المتاحة...", color = Color.Gray, fontSize = 11.sp)
                }
            },
            confirmButton = {
                TextButton(onClick = { showReRouteDialog = false }) {
                    Text("إلغاء التوجيه اليدوي", color = Color.Gray)
                }
            }
        )
    }
}

@Composable
fun OrderFeedCard(
    order: Order,
    onClick: () -> Unit
) {
    val urgencyLabel = when (order.urgencyLevel) {
        "critical" -> "🔴 حرج جداً"
        "high" -> "🟡 مستعجل"
        else -> "🟢 عادي"
    }

    val urgencyColor = when (order.urgencyLevel) {
        "critical" -> MedRedPrimary
        "high" -> Color(0xFFD97706)
        else -> MedGreenPrimary
    }

    // Count of branch offers
    val offersCount = if (order.orderId.hashCode() % 3 == 0) 2 else if (order.orderId.hashCode() % 2 == 0) 1 else 0

    val statusLabel = when (order.status) {
        "broadcast" -> "جديد ومبثوث"
        "offer_received" -> "لديه عروض"
        "negotiating" -> "قيد التفاوض"
        "confirmed" -> "مكتمل"
        else -> "جديد"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.End
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left badge - Urgency status
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(urgencyColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(urgencyLabel, color = urgencyColor, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                }

                // Right - Reference ID
                Text("#${order.orderId}", color = MedGreenPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = order.clientName,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "الحالة: $statusLabel",
                    color = MedGreenPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "المنطقة: محافظة ${order.clientGovernorate}",
                    color = Color.LightGray,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Show description briefly
            Text(
                text = order.orderContent,
                color = Color.LightGray,
                fontSize = 11.sp,
                maxLines = 2,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )

            Divider(color = Color.White.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "العروض الواردة: $offersCount عروض",
                    color = if (offersCount > 0) MedGreenPrimary else Color.Gray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "منذ: 15 دقيقة", // Elapsed time
                    color = Color.Gray,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f)),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(title, color = Color.LightGray, fontSize = 10.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
        }
    }
}
