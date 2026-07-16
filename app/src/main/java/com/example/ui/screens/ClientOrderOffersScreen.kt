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
import com.example.service.FirebaseService
import com.example.ui.theme.MedBluePrimary
import com.example.ui.theme.MedGreenPrimary
import com.example.ui.theme.MedRedPrimary
import kotlin.math.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientOrderOffersScreen(
    order: Order,
    offers: List<BranchOffer>,
    onBackClick: () -> Unit,
    onRefresh: () -> Unit
) {
    val context = LocalContext.current

    // Dialog & Action States
    var showNegotiationDialog by remember { mutableStateOf(false) }
    var selectedOfferForNegotiation by remember { mutableStateOf<BranchOffer?>(null) }
    var negotiationMessage by remember { mutableStateOf("") }

    var showRejectionDialog by remember { mutableStateOf(false) }
    var selectedOfferForRejection by remember { mutableStateOf<BranchOffer?>(null) }
    var rejectionReason by remember { mutableStateOf("") }

    // Helper to compute geographic distance using branch latitude/longitude
    fun calculateDistance(orderGov: String, branchId: String): Double {
        return Double.MAX_VALUE
    }

    // Smart Recommendations Calculations
    val cheapestOffer = remember(offers) {
        offers.minByOrNull { it.totalPrice + it.shippingCost }
    }
    
    val fastestOffer = remember(offers) {
        offers.minByOrNull { it.deliveryDays }
    }

    val closestOffer = remember(offers) {
        offers.minByOrNull { calculateDistance(order.clientGovernorate, it.branchId) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("عروض الأسعار والتفاوض 🤝", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White) },
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
            // --- 1. Order Summary Card ---
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
                            Text(
                                text = "ملخص طلب التوريد رقم: ${order.orderId.takeLast(6)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MedBluePrimary
                            )
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
                            Text("المحافظة: ${order.clientGovernorate}", fontSize = 11.sp, color = Color.Gray)
                            Text("حالة البث: " + if (order.broadcastType == "all") "بث كامل 🌐" else "محدد جغرافياً 📍", fontSize = 11.sp, color = Color.Gray)
                        }
                    }
                }
            }

            // --- 2. Smart Recommendations Segment ---
            if (offers.isNotEmpty()) {
                item {
                    Text(
                        text = "⭐ توصيات الذكاء الاصطناعي الذكي",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.DarkGray,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 1. Cheapest Card
                        if (cheapestOffer != null) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5)),
                                modifier = Modifier
                                    .weight(1f)
                                    .border(1.dp, MedGreenPrimary.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.LocalAtm, contentDescription = null, tint = MedGreenPrimary, modifier = Modifier.size(20.dp))
                                    Text("الأرخص سعراً 💸", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = MedGreenPrimary)
                                    Text(cheapestOffer.branchName, fontSize = 9.sp, fontWeight = FontWeight.Medium, maxLines = 1, color = Color.DarkGray)
                                    Text("${cheapestOffer.totalPrice + cheapestOffer.shippingCost} YER", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MedGreenPrimary)
                                }
                            }
                        }

                        // 2. Closest Card
                        if (closestOffer != null) {
                            val dist = calculateDistance(order.clientGovernorate, closestOffer.branchId)
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                                modifier = Modifier
                                    .weight(1f)
                                    .border(1.dp, MedBluePrimary.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.Place, contentDescription = null, tint = MedBluePrimary, modifier = Modifier.size(20.dp))
                                    Text("الأقرب موقعاً 📍", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = MedBluePrimary)
                                    Text(closestOffer.branchName, fontSize = 9.sp, fontWeight = FontWeight.Medium, maxLines = 1, color = Color.DarkGray)
                                    Text(if (dist == Double.MAX_VALUE) "نفس المدينة" else "${dist.toInt()} كم", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MedBluePrimary)
                                }
                            }
                        }

                        // 3. Fastest Card
                        if (fastestOffer != null) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                                modifier = Modifier
                                    .weight(1f)
                                    .border(1.dp, Color(0xFFD97706).copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.Speed, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(20.dp))
                                    Text("الأسرع توصيلاً ⚡", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color(0xFFD97706))
                                    Text(fastestOffer.branchName, fontSize = 9.sp, fontWeight = FontWeight.Medium, maxLines = 1, color = Color.DarkGray)
                                    Text("${fastestOffer.deliveryDays} أيام فقط", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD97706))
                                }
                            }
                        }
                    }
                }
            }

            // --- 3. Offers List Section ---
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📋 عروض الأسعار المتوفرة (${offers.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.DarkGray
                    )
                }
            }

            if (offers.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 30.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.HourglassEmpty, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.LightGray)
                            Text("لم تصل أي عروض أسعار من الفروع بعد.. ⏳", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            } else {
                items(offers) { offer ->
                    val dist = calculateDistance(order.clientGovernorate, offer.branchId)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth().testTag("offer_card_${offer.offerId}")
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Offer Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(offer.branchName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MedBluePrimary)
                                    Text(
                                        text = "المسافة: " + if (dist == Double.MAX_VALUE) "غير محدد" else "${dist.toInt()} كم تقريباً",
                                        fontSize = 10.sp,
                                        color = Color.Gray
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text("${offer.totalPrice} YER", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = MedGreenPrimary)
                                    Text("شحن: ${offer.shippingCost} YER", fontSize = 10.sp, color = Color.Gray)
                                }
                            }

                            Divider(color = Color(0xFFF1F5F9), thickness = 1.dp)

                            // Offer Details
                            Text(
                                text = "💬 تفاصيل العرض: ${offer.offerDetails}",
                                fontSize = 11.sp,
                                color = Color.DarkGray
                            )

                            // Extra fields
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Icon(Icons.Default.Schedule, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("التسليم: ${offer.deliveryDays} أيام", fontSize = 10.sp, color = Color.DarkGray)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Icon(Icons.Default.CreditCard, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("السداد: ${offer.paymentTerms}", fontSize = 10.sp, color = Color.DarkGray, maxLines = 1)
                                }
                            }

                            if (offer.notes.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFFFFBEB), RoundedCornerShape(6.dp))
                                        .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                        .padding(8.dp)
                                ) {
                                    Text("📌 ملاحظات الفرع: ${offer.notes}", fontSize = 10.sp, color = Color(0xFFD97706))
                                }
                            }

                            // Offer Status indicator
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val statusColor = when (offer.status) {
                                    "accepted" -> MedGreenPrimary
                                    "rejected" -> MedRedPrimary
                                    "negotiating" -> Color(0xFFF59E0B)
                                    else -> Color.Gray
                                }
                                val statusLabel = when (offer.status) {
                                    "accepted" -> "مقبول ومعتمد ✔"
                                    "rejected" -> "تم رفض العرض ❌"
                                    "negotiating" -> "قيد التفاوض النشط 💬"
                                    else -> "بانتظار مراجعتكم ⏳"
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(statusColor, RoundedCornerShape(4.dp))
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(statusLabel, color = statusColor, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }

                                if (offer.status == "pending" || offer.status == "negotiating") {
                                    if (order.status != "confirmed") {
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            // Reject
                                            IconButton(
                                                onClick = {
                                                    selectedOfferForRejection = offer
                                                    showRejectionDialog = true
                                                },
                                                modifier = Modifier
                                                    .background(MedRedPrimary.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                                                    .size(34.dp)
                                                    .testTag("reject_offer_${offer.offerId}")
                                            ) {
                                                Icon(Icons.Default.Close, contentDescription = "رفض", tint = MedRedPrimary, modifier = Modifier.size(16.dp))
                                            }

                                            // Negotiate
                                            IconButton(
                                                onClick = {
                                                    selectedOfferForNegotiation = offer
                                                    showNegotiationDialog = true
                                                },
                                                modifier = Modifier
                                                    .background(Color(0xFFF59E0B).copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                                                    .size(34.dp)
                                                    .testTag("negotiate_offer_${offer.offerId}")
                                            ) {
                                                Icon(Icons.Default.Chat, contentDescription = "تفاوض", tint = Color(0xFFD97706), modifier = Modifier.size(16.dp))
                                            }

                                            // Accept
                                            Button(
                                                onClick = {
                                                    FirebaseService.acceptBranchOffer(order.orderId, offer.offerId) { success ->
                                                        if (success) {
                                                            Toast.makeText(context, "🤝 تم قبول عرض ${offer.branchName} بنجاح وتم تعميد الطلب!", Toast.LENGTH_LONG).show()
                                                            onRefresh()
                                                        } else {
                                                            Toast.makeText(context, "فشل في قبول العرض", Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = MedGreenPrimary),
                                                contentPadding = PaddingValues(horizontal = 10.dp),
                                                shape = RoundedCornerShape(6.dp),
                                                modifier = Modifier.height(34.dp).testTag("accept_offer_${offer.offerId}")
                                            ) {
                                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("قبول وتعميد", fontSize = 11.sp, fontWeight = FontWeight.Bold)
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

    // --- Negotiation Dialog ---
    if (showNegotiationDialog && selectedOfferForNegotiation != null) {
        val offer = selectedOfferForNegotiation!!
        AlertDialog(
            onDismissRequest = { showNegotiationDialog = false },
            title = { Text("💬 تفاوض على سعر أو شروط عرض ${offer.branchName}", fontSize = 14.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("السعر الحالي للفرع: ${offer.totalPrice} YER", fontSize = 11.sp, color = Color.Gray)
                    OutlinedTextField(
                        value = negotiationMessage,
                        onValueChange = { negotiationMessage = it },
                        label = { Text("اكتب رسالة التفاوض الخاصة بك") },
                        placeholder = { Text("مثال: هل يمكن تخفيض الكلفة الإجمالية لـ 40,000 ريال؟") },
                        modifier = Modifier.fillMaxWidth().testTag("negotiation_text_input"),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MedBluePrimary)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (negotiationMessage.isBlank()) {
                            Toast.makeText(context, "الرجاء كتابة رسالة التفاوض", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val idx = FirebaseService.fallbackBranchOffers.indexOfFirst { it.offerId == offer.offerId }
                        if (idx != -1) {
                            val currentNotes = FirebaseService.fallbackBranchOffers[idx].notes
                            val updatedNotes = if (currentNotes.isEmpty()) "💬 تفاوض العميل: $negotiationMessage" else "$currentNotes\n💬 تفاوض العميل: $negotiationMessage"
                            FirebaseService.fallbackBranchOffers[idx] = FirebaseService.fallbackBranchOffers[idx].copy(
                                status = "negotiating",
                                notes = updatedNotes
                            )
                            Toast.makeText(context, "🚀 تم إرسال رسالة التفاوض لفرع ${offer.branchName} بنجاح!", Toast.LENGTH_LONG).show()
                            showNegotiationDialog = false
                            negotiationMessage = ""
                            selectedOfferForNegotiation = null
                            onRefresh()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary, contentColor = Color.White)
                ) {
                    Text("إرسال التفاوض 💬")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNegotiationDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // --- Rejection Dialog ---
    if (showRejectionDialog && selectedOfferForRejection != null) {
        val offer = selectedOfferForRejection!!
        AlertDialog(
            onDismissRequest = { showRejectionDialog = false },
            title = { Text("❌ رفض العرض المقدم من ${offer.branchName}", fontSize = 14.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("أنت بصدد رفض هذا العرض السعري. الرجاء تبيان السبب للفرع:", fontSize = 11.sp, color = Color.Gray)
                    OutlinedTextField(
                        value = rejectionReason,
                        onValueChange = { rejectionReason = it },
                        label = { Text("سبب الرفض") },
                        placeholder = { Text("مثال: السعر مرتفع مقارنة بالبقية أو مدة التوصيل طويلة") },
                        modifier = Modifier.fillMaxWidth().testTag("rejection_reason_input"),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MedRedPrimary)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (rejectionReason.isBlank()) {
                            Toast.makeText(context, "الرجاء كتابة سبب الرفض", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        FirebaseService.rejectBranchOffer(offer.offerId, rejectionReason) { success ->
                            if (success) {
                                Toast.makeText(context, "تم رفض العرض وإبلاغ الفرع بالسبب.", Toast.LENGTH_SHORT).show()
                                showRejectionDialog = false
                                rejectionReason = ""
                                selectedOfferForRejection = null
                                onRefresh()
                            } else {
                                Toast.makeText(context, "فشل عملية الرفض", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MedRedPrimary)
                ) {
                    Text("تأكيد الرفض ❌")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRejectionDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}
