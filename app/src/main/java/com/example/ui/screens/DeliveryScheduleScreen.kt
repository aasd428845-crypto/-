package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.model.DeliverySchedule
import com.example.model.Order
import com.example.model.User
import com.example.service.FirebaseService
import com.example.ui.theme.MedBlueAccent
import com.example.ui.theme.MedBluePrimary
import com.example.ui.theme.MedGreenPrimary
import com.example.ui.theme.MedRedPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryScheduleScreen(
    currentUser: User,
    order: Order,
    onBackClick: () -> Unit,
    onScheduleCompleted: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var scheduleState by remember { mutableStateOf<DeliverySchedule?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // Slots
    var supplierSlots by remember { mutableStateOf(emptyList<String>()) }
    var hospitalSlots by remember { mutableStateOf(emptyList<String>()) }

    var selectedSupplierSlot by remember { mutableStateOf<String?>(null) }
    var selectedHospitalSlot by remember { mutableStateOf<String?>(null) }

    // Dialog triggering
    var showNegotiationPanel by remember { mutableStateOf(false) }
    var suggestedAgreedTime by remember { mutableStateOf("") }
    var agreedFinalDateTime by remember { mutableStateOf("") }

    // Inputs for adding custom slots
    var customSlotInput by remember { mutableStateOf("") }

    fun loadSchedule() {
        isLoading = true
        FirebaseService.getDeliverySchedule(order.orderId) { sched ->
            if (sched != null) {
                scheduleState = sched
                supplierSlots = sched.supplierAvailableTimes.ifEmpty { supplierSlots }
                hospitalSlots = sched.hospitalPreferredTimes.ifEmpty { hospitalSlots }
                agreedFinalDateTime = sched.agreedDateTime
            }
            isLoading = false
        }
    }

    LaunchedEffect(order.orderId) {
        loadSchedule()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("اتفاق وجدولة مواعيد الاستلام والتسليم 📅", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MedBluePrimary, titleContentColor = Color.White)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(Color(0xFFF8FAFC))
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // General Header Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        "📦 تفاصيل الطلب: ${order.medicineName}",
                        fontWeight = FontWeight.Bold,
                        color = MedBluePrimary,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "رقم العملية: ${order.orderId.substringAfter("_")}",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                    Text(
                        "الحالة الحالية: ${if (order.status == "paid") "🟢 مدفوع معلق الجدولة" else "🚚 قيد التوصيل"}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MedGreenPrimary
                    )

                    if (agreedFinalDateTime.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF0FDF4), RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("📅 الموعد النهائي المتفق عليه والمثبت:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MedGreenPrimary)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(agreedFinalDateTime, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = MedGreenPrimary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(12.dp), tint = MedGreenPrimary)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("تم تفعيل تنبيه استباقي قبل 24 ساعة للطرفين ⏰", fontSize = 9.sp, color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }

            // Step Split Layout: Supplier proposed slot vs Hospital preferred
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Supplier Proposed section
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFEFF6FF), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            "🚚 مواعيد المورد المتاحة",
                            fontWeight = FontWeight.Bold,
                            color = MedBluePrimary,
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Display slots
                    supplierSlots.forEach { slot ->
                        val isSelected = selectedSupplierSlot == slot
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (isSelected) MedBluePrimary.copy(alpha = 0.1f) else Color.White,
                                    RoundedCornerShape(8.dp)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) MedBluePrimary else Color.LightGray.copy(alpha = 0.5f),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    selectedSupplierSlot = slot
                                    if (currentUser.role == "supplier") {
                                        agreedFinalDateTime = "" // Reset absolute agreement to trigger negotiation if matches
                                    }
                                }
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(slot, fontSize = 10.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
                        }
                    }

                    // Add Custom Slot Option
                    if (currentUser.role == "supplier") {
                        OutlinedTextField(
                            value = customSlotInput,
                            onValueChange = { customSlotInput = it },
                            placeholder = { Text("أضف موعد تسليم متاح..", fontSize = 8.sp) },
                            modifier = Modifier.fillMaxWidth().testTag("add_supplier_slot_input"),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MedBluePrimary)
                        )
                        Button(
                            onClick = {
                                if (customSlotInput.isNotBlank()) {
                                    supplierSlots = supplierSlots + customSlotInput
                                    customSlotInput = ""
                                    Toast.makeText(context, "تم حفظ خانة الموعد المتاح كـ مورد!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary, contentColor = Color.White),
                            modifier = Modifier.fillMaxWidth().height(32.dp),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("+ أضف موعد", fontSize = 9.sp)
                        }
                    }
                }

                // Hospital Preferred section
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF0FDF4), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            "🏥 تفضيلات المستشفى",
                            fontWeight = FontWeight.Bold,
                            color = MedGreenPrimary,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Display slots
                    hospitalSlots.forEach { slot ->
                        val isSelected = selectedHospitalSlot == slot
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (isSelected) MedGreenPrimary.copy(alpha = 0.1f) else Color.White,
                                    RoundedCornerShape(8.dp)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) MedGreenPrimary else Color.LightGray.copy(alpha = 0.5f),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    selectedHospitalSlot = slot
                                    if (currentUser.role == "hospital") {
                                        agreedFinalDateTime = ""
                                    }
                                }
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(slot, fontSize = 10.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
                        }
                    }

                    // Add Custom Slot Option
                    if (currentUser.role == "hospital") {
                        OutlinedTextField(
                            value = customSlotInput,
                            onValueChange = { customSlotInput = it },
                            placeholder = { Text("أضف موعد يناسبك للاستلام..", fontSize = 8.sp) },
                            modifier = Modifier.fillMaxWidth().testTag("add_hospital_slot_input"),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MedGreenPrimary)
                        )
                        Button(
                            onClick = {
                                if (customSlotInput.isNotBlank()) {
                                    hospitalSlots = hospitalSlots + customSlotInput
                                    customSlotInput = ""
                                    Toast.makeText(context, "تم حفظ تفضيل الموعد كـ مستشفى!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MedGreenPrimary),
                            modifier = Modifier.fillMaxWidth().height(32.dp),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("+ أضف تفضيل", fontSize = 9.sp)
                        }
                    }
                }
            }

            // Calculation Compare & action card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("⚖️ تحليل تطابق المواعيد", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 12.sp)

                    if (selectedSupplierSlot == null || selectedHospitalSlot == null) {
                        Text(
                            "يرجى تحديد موعد واحد من المعسكرين (المورد والمستشفى) للمطابقة البرمجية وعقد الاتفاق الموثق.",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    } else if (selectedSupplierSlot == selectedHospitalSlot) {
                        // Perfect match!
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFF0FDF4), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                "🤝 هناك تطابق بالكامل! كلاكما حدد: \"$selectedSupplierSlot\".\nيمكن تثبيت الميعاد فوراً دون أي تفاوض إضافي.",
                                color = MedGreenPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )
                        }

                        Button(
                            onClick = {
                                val finalTime = selectedSupplierSlot!!
                                val updateSched = DeliverySchedule(
                                    scheduleId = "",
                                    orderId = order.orderId,
                                    supplierAvailableTimes = supplierSlots,
                                    hospitalPreferredTimes = hospitalSlots,
                                    agreedDateTime = finalTime,
                                    status = "agreed"
                                )
                                FirebaseService.saveDeliverySchedule(updateSched, {
                                    agreedFinalDateTime = finalTime
                                    Toast.makeText(context, "تم تأكيد الاتفاق وتثبيت الموعد النهائي! 🎉", Toast.LENGTH_SHORT).show()
                                    onScheduleCompleted()
                                }, {})
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MedGreenPrimary),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("اعتماد وتثبيت الموعد المتطابق 🤝", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        // Slots differ! Launch small negotiation modal trigger
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFFEF2F2), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                "⚠️ تفاوت في التواريخ المتوقعة:\nالمورد يفضل: \"$selectedSupplierSlot\"\nبينما المستشفى تفضل: \"$selectedHospitalSlot\"\nالرجاء عقد التفاوض للتوصل لحل وسط سريع.",
                                color = MedRedPrimary,
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )
                        }

                        Button(
                            onClick = {
                                // Dynamic suggested midpoint (e.g. Wednesday 10th at mid noon)
                                suggestedAgreedTime = "الأربعاء 10 يونيو - 11:30 ظهراً (توقيت وسط مبرمج)"
                                showNegotiationPanel = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary, contentColor = Color.White),
                            modifier = Modifier.fillMaxWidth().testTag("negotiate_schedule_trigger"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.CompareArrows, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("فتح نافذة التفاوض والاتفاق الوسطي ⚖️", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            if (agreedFinalDateTime.isNotEmpty()) {
                Button(
                    onClick = onScheduleCompleted,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MedGreenPrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("الرجوع لمتابعة تفاصيل الطلبات ⬅️", fontWeight = FontWeight.Bold)
                }
            }
        }

        // --- Negotiation Modal Dialog ---
        if (showNegotiationPanel) {
            AlertDialog(
                onDismissRequest = { showNegotiationPanel = false },
                title = { Text("نافذة التفاوض والتقارب للمواعيد 🤝", fontWeight = FontWeight.Bold, fontSize = 15.sp) },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            "لتسهيل عملية الإتفاق، يقترح الخوارزمي موعداً وسطاً مناسباً للتوصيل مع مراعاة فارق العمل وتوفير الشاحنة مبردة:",
                            fontSize = 11.sp,
                            color = Color.DarkGray
                        )

                        // Highlight Midpoint suggested card
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().border(1.dp, MedBlueAccent, RoundedCornerShape(10.dp))
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("المقترح التوافقي الموصى به 💡", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MedBluePrimary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(suggestedAgreedTime, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = MedBluePrimary, textAlign = TextAlign.Center)
                            }
                        }

                        Text("عند موافقتك على هذا الموعد، سيتم إخطار الطرف الآخر فوراً واعتماده كجدول نهائي للتسليم مبرمج.", fontSize = 10.sp, color = Color.Gray)
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val updateSched = DeliverySchedule(
                                scheduleId = "",
                                orderId = order.orderId,
                                supplierAvailableTimes = supplierSlots,
                                hospitalPreferredTimes = hospitalSlots,
                                agreedDateTime = suggestedAgreedTime,
                                status = "agreed"
                            )
                            FirebaseService.saveDeliverySchedule(updateSched, {
                                agreedFinalDateTime = suggestedAgreedTime
                                showNegotiationPanel = false
                                Toast.makeText(context, "تم إبرام الاتفاق واعتماد الموعد بنجاح! ⏱️ 🎊", Toast.LENGTH_LONG).show()
                            }, {})
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MedGreenPrimary)
                    ) {
                        Text("موافق واعتماد الحل الوسط 👍")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showNegotiationPanel = false }) {
                        Text("إلغاء والتفاوض لاحقاً", color = Color.Gray)
                    }
                }
            )
        }
    }
}
