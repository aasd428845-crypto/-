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
import com.example.service.FirebaseService
import com.example.ui.theme.MedBluePrimary
import com.example.ui.theme.MedGreenPrimary
import com.example.ui.theme.MedRedPrimary

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تفاصيل حالة الطلب 📋", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MedBluePrimary)
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
