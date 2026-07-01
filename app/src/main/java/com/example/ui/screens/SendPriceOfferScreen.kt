package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BranchOffer
import com.example.model.DirectorNotification
import com.example.model.Order
import com.example.model.User
import com.example.service.FirebaseService
import com.example.ui.theme.MedBluePrimary
import com.example.ui.theme.MedGreenPrimary
import com.example.ui.theme.MedRedPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendPriceOfferScreen(
    order: Order,
    currentUser: User,
    onBackClick: () -> Unit,
    onSuccessSubmit: () -> Unit
) {
    val context = LocalContext.current

    // Form Field States
    var totalPriceStr by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf("YER") } // YER or USD
    var shippingCostStr by remember { mutableStateOf("") }
    var deliveryDaysStr by remember { mutableStateOf("") }
    var paymentTerms by remember { mutableStateOf("فوري عند الاستلام") } // فوري / آجل / أقساط / قابل للتفاوض
    var operationNumber by remember { mutableStateOf("") }
    var offerDetails by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    
    // File Attachment State (Simulation)
    var attachmentPath by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    val paymentTermsOptions = listOf(
        "فوري عند الاستلام",
        "آجل (سداد خلال 30 يوم)",
        "أقساط شهرية ميسرة",
        "قابل للتفاوض والمناقشة"
    )
    var showTermsDropdown by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تقديم وتسعير عرض التوريد 💰", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White) },
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // --- Info Card ---
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.border(1.dp, MedBluePrimary.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("الطلب المراد تسعيره:", fontSize = 10.sp, color = Color.Gray)
                        Text(
                            text = "طلب رقم: #${order.orderId.takeLast(6)} - ${order.clientName}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MedBluePrimary
                        )
                        Text(
                            text = order.orderContent,
                            fontSize = 11.sp,
                            color = Color.DarkGray,
                            maxLines = 2
                        )
                    }
                }
            }

            // --- Form Inputs ---
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text("📌 أدخل بيانات التسعير المالي واللوجستي:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.DarkGray)

                        Divider(color = Color(0xFFF1F5F9))

                        // Price & Currency Fields
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = totalPriceStr,
                                onValueChange = { totalPriceStr = it },
                                label = { Text("السعر الإجمالي") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                placeholder = { Text("مثال: 45000") },
                                modifier = Modifier.weight(1.5f).testTag("total_price_input"),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MedGreenPrimary)
                            )

                            // Currency Selector Column
                            Column(modifier = Modifier.weight(1f)) {
                                Text("العملة:", fontSize = 10.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 2.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp)
                                        .border(1.dp, Color.LightGray, RoundedCornerShape(4.dp))
                                        .background(Color(0xFFF8FAFC)),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    Text(
                                        text = "ريال (YER)",
                                        fontSize = 11.sp,
                                        fontWeight = if (currency == "YER") FontWeight.Bold else FontWeight.Normal,
                                        color = if (currency == "YER") MedGreenPrimary else Color.Gray,
                                        modifier = Modifier
                                            .clickable { currency = "YER" }
                                            .padding(6.dp)
                                    )
                                    Divider(color = Color.LightGray, modifier = Modifier.fillMaxHeight().width(1.dp))
                                    Text(
                                        text = "دولار ($)",
                                        fontSize = 11.sp,
                                        fontWeight = if (currency == "USD") FontWeight.Bold else FontWeight.Normal,
                                        color = if (currency == "USD") MedGreenPrimary else Color.Gray,
                                        modifier = Modifier
                                            .clickable { currency = "USD" }
                                            .padding(6.dp)
                                    )
                                }
                            }
                        }

                        // Shipping Cost
                        OutlinedTextField(
                            value = shippingCostStr,
                            onValueChange = { shippingCostStr = it },
                            label = { Text("كلفة الشحن والتوصيل المبرد (YER)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            placeholder = { Text("اكتب 0 إذا كان الشحن مجاني") },
                            modifier = Modifier.fillMaxWidth().testTag("shipping_cost_input"),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MedBluePrimary)
                        )

                        // Delivery Days
                        OutlinedTextField(
                            value = deliveryDaysStr,
                            onValueChange = { deliveryDaysStr = it },
                            label = { Text("زمن التوصيل المتوقع (بالأيام)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            placeholder = { Text("مثال: 2") },
                            modifier = Modifier.fillMaxWidth().testTag("delivery_days_input"),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MedBluePrimary)
                        )

                        // Payment Terms Dropdown Selector
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text("شروط السداد المعتمدة:", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 2.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Color.LightGray, RoundedCornerShape(4.dp))
                                    .clickable { showTermsDropdown = true }
                                    .padding(horizontal = 12.dp, vertical = 14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(paymentTerms, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }

                                DropdownMenu(
                                    expanded = showTermsDropdown,
                                    onDismissRequest = { showTermsDropdown = false },
                                    modifier = Modifier.fillMaxWidth(0.85f)
                                ) {
                                    paymentTermsOptions.forEach { term ->
                                        DropdownMenuItem(
                                            text = { Text(term, fontSize = 12.sp) },
                                            onClick = {
                                                paymentTerms = term
                                                showTermsDropdown = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Operation Number (Optional)
                        OutlinedTextField(
                            value = operationNumber,
                            onValueChange = { operationNumber = it },
                            label = { Text("رقم العملية / الكود اللوجستي الداخلي (اختياري)") },
                            placeholder = { Text("مثال: OP-9941") },
                            modifier = Modifier.fillMaxWidth().testTag("operation_number_input"),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MedBluePrimary)
                        )

                        // Offer Details Description
                        OutlinedTextField(
                            value = offerDetails,
                            onValueChange = { offerDetails = it },
                            label = { Text("تفاصيل العرض والمواصفات الدوائية المتوفرة") },
                            placeholder = { Text("اكتب هنا الصنف المتوفر وتاريخ الانتهاء والمواصفات الفنية الشاملة..") },
                            modifier = Modifier.fillMaxWidth().height(100.dp).testTag("offer_details_input"),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MedGreenPrimary)
                        )

                        // Notes (Optional)
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("أي ملاحظات أو شروط إضافية") },
                            placeholder = { Text("أكتب هنا أي تعليمات إضافية بخصوص النقل أو التخزين المبرد..") },
                            modifier = Modifier.fillMaxWidth().height(80.dp).testTag("notes_input"),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MedBluePrimary)
                        )

                        // --- Visual PDF / Image Attachment Simulation ---
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("📁 وثيقة العرض والمستندات الداعمة (PDF / صورة):", fontSize = 11.sp, color = Color.Gray)
                            
                            if (attachmentPath.isNotEmpty()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MedGreenPrimary.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                        .border(1.dp, MedGreenPrimary.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = MedGreenPrimary)
                                        Text(attachmentPath.substringAfterLast("/"), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MedGreenPrimary)
                                    }
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "حذف المرفق",
                                        tint = MedRedPrimary,
                                        modifier = Modifier.clickable { attachmentPath = "" }
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
                                        .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                                        .clickable {
                                            // Simulate PDF select
                                            attachmentPath = "/storage/emulated/0/Documents/price_offer_${order.orderId.takeLast(4)}.pdf"
                                            Toast.makeText(context, "📎 تم إرفاق وثيقة التسعير الفني والمالي بنجاح!", Toast.LENGTH_SHORT).show()
                                        }
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(32.dp))
                                        Text("تحميل ملف عرض أسعار معتمد PDF / صورة 📂", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                                        Text("(انقر للمحاكاة وتعيين مسار مستند رسمي معتمد)", fontSize = 9.sp, color = Color.Gray)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // --- Submit Actions ---
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onBackClick,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Gray),
                        modifier = Modifier.weight(1f).height(46.dp)
                    ) {
                        Text("إلغاء وتراجع")
                    }

                    Button(
                        onClick = {
                            // Validation
                            val price = totalPriceStr.toDoubleOrNull()
                            val days = deliveryDaysStr.toIntOrNull()
                            val shipping = shippingCostStr.toDoubleOrNull() ?: 0.0

                            if (price == null || price <= 0) {
                                Toast.makeText(context, "الرجاء إدخال سعر إجمالي صحيح أكبر من الصفر", Toast.LENGTH_LONG).show()
                                return@Button
                            }
                            if (days == null || days <= 0) {
                                Toast.makeText(context, "الرجاء إدخال عدد أيام توصيل صحيح", Toast.LENGTH_LONG).show()
                                return@Button
                            }
                            if (offerDetails.trim().isEmpty()) {
                                Toast.makeText(context, "الرجاء كتابة تفاصيل العرض الفنية والدوائية لكي يثق بها العميل", Toast.LENGTH_LONG).show()
                                return@Button
                            }

                            isSubmitting = true
                            
                            // Construct BranchOffer
                            val newOffer = BranchOffer(
                                offerId = "offer_" + System.currentTimeMillis(),
                                orderId = order.orderId,
                                branchId = currentUser.branchId,
                                branchName = currentUser.branchName.ifEmpty { "فرع صنعاء الرئيسي" },
                                managerName = currentUser.name,
                                // omitted
                                totalPrice = price,
                                currency = currency,
                                shippingCost = shipping,
                                deliveryDays = days,
                                paymentTerms = paymentTerms,
                                offerDetails = offerDetails,
                                notes = notes + if (operationNumber.isNotEmpty()) "\nرقم العملية: $operationNumber" else "",
                                attachmentUrl = attachmentPath,
                                status = "pending",
                                createdAt = System.currentTimeMillis()
                            )

                            // 1. Submit branch offer
                            FirebaseService.submitBranchOffer(offer = newOffer, onSuccess = {
                                // 2. Send simulated client FCM notification via Toast
                                Toast.makeText(context, "🔔 إشعار FCM مرسل للعميل: لقد تلقيت عرضاً مالياً مميزاً من ${newOffer.branchName} بقيمة ${newOffer.totalPrice} ${newOffer.currency}!", Toast.LENGTH_LONG).show()

                                // 3. Log notification to general director
                                val directorNotif = DirectorNotification(
                                    notificationId = "notif_" + System.currentTimeMillis(),
                                    title = "عطاء وتسعير جديد من الفروع 💰",
                                    message = "قام ${newOffer.branchName} بتقديم عرض توريد للطلب #${order.orderId.takeLast(6)} التابع لـ ${order.clientName} بقيمة ${newOffer.totalPrice} YER.",
                                    orderId = order.orderId,
                                    clientId = order.clientId,
                                    clientName = order.clientName,
                                    createdAt = System.currentTimeMillis()
                                )
                                FirebaseService.notifyDirector(directorNotif) {
                                    isSubmitting = false
                                    Toast.makeText(context, "🚀 تم إرسال العطاء وحفظه بنجاح وإبلاغ الإدارة العامة للشفاء!", Toast.LENGTH_LONG).show()
                                    onSuccessSubmit()
                                }
                            },
                            onFailure = { err ->
                                isSubmitting = false
                                Toast.makeText(context, "فشل تقديم العرض: $err", Toast.LENGTH_SHORT).show()
                            }
                        )
                    },
                        colors = ButtonDefaults.buttonColors(containerColor = MedGreenPrimary),
                        modifier = Modifier.weight(2.5f).height(46.dp).testTag("submit_offer_to_database")
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                        } else {
                            Icon(Icons.Default.Send, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("تأكيد وإرسال العطاء المالي 🚀", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
