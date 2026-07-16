package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailForBranchScreen(
    order: Order,
    currentUser: User,
    onBackClick: () -> Unit,
    onSendOfferClick: () -> Unit,
    onRefresh: () -> Unit
) {
    val context = LocalContext.current
    var showRejectDialog by remember { mutableStateOf(false) }
    var rejectReason by remember { mutableStateOf("") }
    var branchOffer by remember { mutableStateOf<BranchOffer?>(null) }
    var isLoadingOffer by remember { mutableStateOf(true) }

    fun loadOffer() {
        isLoadingOffer = true
        FirebaseService.getAllBranchOffers { allOffers ->
            branchOffer = allOffers.find { it.orderId == order.orderId && it.branchId == currentUser.branchId }
            isLoadingOffer = false
        }
    }

    LaunchedEffect(order.orderId) {
        loadOffer()
    }

    // Helper to mask phone numbers
    fun maskPhone(phone: String, isAccepted: Boolean): String {
        if (isAccepted) return phone
        if (phone.length <= 4) return "xxxx"
        return phone.take(4) + "xxx" + phone.takeLast(2)
    }

    val isOfferAccepted = branchOffer?.status == "accepted"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تفاصيل طلب العميل 📦", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White) },
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // --- 1. Client Info Card ---
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("👤 معلومات جهة الطلب:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MedBluePrimary)
                        
                        Divider(color = Color(0xFFF1F5F9))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("اسم المنشأة الطبية:", fontSize = 10.sp, color = Color.Gray)
                                Text(order.clientName.ifEmpty { "صيدلية النور المركزية" }, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.DarkGray)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("نوع العميل:", fontSize = 10.sp, color = Color.Gray)
                                Text(if (order.clientType == "hospital") "مستشفى 🏥" else "صيدلية 💊", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MedBluePrimary)
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("المحافظة:", fontSize = 10.sp, color = Color.Gray)
                                Text(order.clientGovernorate, fontWeight = FontWeight.Medium, fontSize = 12.sp, color = Color.DarkGray)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("رقم الهاتف للتواصل:", fontSize = 10.sp, color = Color.Gray)
                                val dummyPhone = "غير متاح"
                                Text(
                                    text = maskPhone(dummyPhone, isOfferAccepted),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (isOfferAccepted) MedGreenPrimary else Color.Gray
                                )
                                if (!isOfferAccepted) {
                                    Text("(يظهر الرقم بالكامل عند قبول العرض وتعميده)", fontSize = 8.sp, color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }

            // --- 2. Order Content ---
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("📝 محتوى الاحتياج الطبي المطلوب:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.DarkGray)
                            
                            val urgencyColor = when (order.urgencyLevel) {
                                "critical" -> MedRedPrimary
                                "high" -> Color(0xFFEAB308)
                                else -> MedGreenPrimary
                            }
                            Box(
                                modifier = Modifier
                                    .background(urgencyColor.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (order.urgencyLevel == "critical") "طارئ" else if (order.urgencyLevel == "high") "عاجل" else "عادي",
                                    color = urgencyColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            }
                        }

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
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("نوع البث: " + if (order.broadcastType == "all") "بث عام لكافة الفروع" else "محدد جغرافياً للفروع القريبة", fontSize = 10.sp, color = Color.Gray)
                            Text("رقم المرجع: #${order.orderId.takeLast(6)}", fontSize = 10.sp, color = Color.Gray)
                        }
                    }
                }
            }

            // --- 3. Branch Offer Status Segment ---
            if (isLoadingOffer) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MedBluePrimary)
                    }
                }
            } else {
                if (branchOffer != null) {
                    val offer = branchOffer!!
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.border(1.dp, MedGreenPrimary.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("💰 عرض السعر المقدم من فرعكم:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MedGreenPrimary)
                                    
                                    val statusColor = when (offer.status) {
                                        "accepted" -> MedGreenPrimary
                                        "rejected" -> MedRedPrimary
                                        "negotiating" -> Color(0xFFF59E0B)
                                        else -> Color.Gray
                                    }
                                    val statusLabel = when (offer.status) {
                                        "accepted" -> "مقبول ومعتمد ✔"
                                        "rejected" -> "مرفوض"
                                        "negotiating" -> "قيد التفاوض 💬"
                                        else -> "معلق بانتظار العميل ⏳"
                                    }
                                    Box(
                                        modifier = Modifier
                                            .background(statusColor.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(statusLabel, color = statusColor, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                    }
                                }

                                Divider(color = Color(0xFFF1F5F9))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("السعر الإجمالي: ${offer.totalPrice} YER", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MedGreenPrimary)
                                    Text("كلفة الشحن: ${offer.shippingCost} YER", fontSize = 11.sp, color = Color.Gray)
                                }

                                Text("زمن التوصيل: ${offer.deliveryDays} أيام", fontSize = 11.sp, color = Color.DarkGray)
                                Text("تفاصيل العرض: ${offer.offerDetails}", fontSize = 11.sp, color = Color.DarkGray)

                                if (offer.notes.isNotEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFFFFFBEB), RoundedCornerShape(6.dp))
                                            .padding(8.dp)
                                    ) {
                                        Text("📌 ملاحظات وشروط إضافية:\n${offer.notes}", fontSize = 10.sp, color = Color(0xFFD97706))
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // Edit offer button if not accepted
                                if (offer.status != "accepted") {
                                    Button(
                                        onClick = onSendOfferClick,
                                        colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary, contentColor = Color.White),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth().height(38.dp).testTag("edit_offer_btn")
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("تعديل العرض المالي الحالي ✏️", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // No offer sent yet
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Send offer action
                            Button(
                                onClick = onSendOfferClick,
                                colors = ButtonDefaults.buttonColors(containerColor = MedGreenPrimary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().height(44.dp).testTag("send_offer_btn")
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("تقديم عرض سعر مالي ولوجستي 💰", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }

                            // Reject / Ignore action
                            OutlinedButton(
                                onClick = { showRejectDialog = true },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MedRedPrimary),
                                border = BorderStroke(1.dp, MedRedPrimary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().height(44.dp).testTag("reject_order_btn")
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("الاعتذار عن تلبية الطلب مع السبب ❌", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    // --- Reject with reason Dialog ---
    if (showRejectDialog) {
        AlertDialog(
            onDismissRequest = { showRejectDialog = false },
            title = { Text("الاعتذار عن تلبية الطلب ❌", fontWeight = FontWeight.Bold, fontSize = 14.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("يرجى توضيح سبب الاعتذار عن تلبية هذا الطلب الطبي للعميل:", fontSize = 11.sp, color = Color.Gray)
                    OutlinedTextField(
                        value = rejectReason,
                        onValueChange = { rejectReason = it },
                        label = { Text("سبب الاعتذار") },
                        placeholder = { Text("مثال: عدم توفر الصنف الدوائي حالياً في مخازننا") },
                        modifier = Modifier.fillMaxWidth().testTag("branch_reject_reason_input"),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MedRedPrimary)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (rejectReason.isBlank()) {
                            Toast.makeText(context, "يرجى إدخال سبب الاعتذار", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        Toast.makeText(context, "تم الاعتذار عن تلبية الطلب بنجاح للعميل.", Toast.LENGTH_LONG).show()
                        showRejectDialog = false
                        onBackClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MedRedPrimary)
                ) {
                    Text("إرسال الاعتذار ❌")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRejectDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}
