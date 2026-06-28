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
import com.example.model.Medicine
import com.example.model.Order
import com.example.service.FirebaseService
import com.example.ui.theme.MedBluePrimary
import com.example.ui.theme.MedGreenPrimary
import com.example.ui.theme.MedRedPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewOrderScreen(
    userId: String,
    onNavigateBack: () -> Unit,
    onTrackOrderStatus: () -> Unit
) {
    val context = LocalContext.current

    // Load Client Profile
    var clientProfile by remember { mutableStateOf<ClientProfile?>(null) }
    LaunchedEffect(userId) {
        FirebaseService.getClientProfile(userId) { profile ->
            clientProfile = profile
        }
    }

    val activeProfile = clientProfile ?: ClientProfile(
        institutionName = "المستشفى التجريبي للمطابقة",
        clientType = "hospital",
        responsiblePerson = "د. علي الحمادي",
        phone = "771122334",
        governorate = "صنعاء",
        city = "صنعاء",
        latitude = 15.3482,
        longitude = 44.2191
    )

    var currentStep by remember { mutableStateOf(1) } // 1 to 5
    var isSubmitted by remember { mutableStateOf(false) }
    var generatedOrderId by remember { mutableStateOf("") }
    var submittedBranchCount by remember { mutableStateOf(0) }

    // --- STEP 1 STATES: ORDER TYPES ---
    val orderTypesList = listOf("💊 أدوية ومستحضرات", "🔬 محاليل مختبرات", "🏥 مستلزمات طبية", "📦 أخرى")
    val selectedOrderTypes = remember { mutableStateListOf<String>() }
    var otherTypeText by remember { mutableStateOf("") }

    // --- STEP 2 STATES: CONTENT ENGINES ---
    var contentTabIdx by remember { mutableStateOf(0) } // 0: Free writing, 1: Catalog, 2: Upload
    var manualWritingText by remember { mutableStateOf("") }
    var selectedCatalogMedicines = remember { mutableStateListOf<Pair<Medicine, Int>>() } // medicine to qty
    var attachedFileNames = remember { mutableStateListOf<String>() }

    // --- STEP 3 STATES: URGENCY ---
    var selectedUrgency by remember { mutableStateOf("normal") } // "critical", "high", "normal"

    // --- STEP 4 STATES: TARGET BRANCHES ---
    var branchRouteOption by remember { mutableStateOf("nearby") } // "all", "nearby", "selected"
    val selectedBranchIdsForOrder = remember { mutableStateListOf<String>() }

    // Nearest branch math
    val userLat = activeProfile.latitude
    val userLng = activeProfile.longitude
    var closestBranchName by remember { mutableStateOf("فرع صنعاء الرئيسي") }
    var closestBranchDistance by remember { mutableStateOf(0.0) }

    LaunchedEffect(userLat, userLng) {
        var minDistance = Double.MAX_VALUE
        var closest = FirebaseService.fallbackBranches.first()
        FirebaseService.fallbackBranches.forEach { b ->
            val d = FirebaseService.calculateDistanceKm(userLat, userLng, b.latitude, b.longitude)
            if (d < minDistance) {
                minDistance = d
                closest = b
            }
        }
        closestBranchName = closest.branchName
        closestBranchDistance = minDistance
    }

    if (isSubmitted) {
        // High-fidelity success screen
        CheckoutSuccessLayout(
            orderId = generatedOrderId,
            branchCount = submittedBranchCount,
            onTrackOrder = onTrackOrderStatus,
            onNewOrder = {
                // Reset everything
                selectedOrderTypes.clear()
                otherTypeText = ""
                manualWritingText = ""
                selectedCatalogMedicines.clear()
                attachedFileNames.clear()
                selectedUrgency = "normal"
                branchRouteOption = "nearby"
                selectedBranchIdsForOrder.clear()
                generatedOrderId = ""
                submittedBranchCount = 0
                isSubmitted = false
                currentStep = 1
            }
        )
    } else {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("طلب شراء وإمداد ذكي 📝", fontWeight = FontWeight.Bold, color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("new_order_back")) {
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
                // VISIBLE PROGRESS STEPPER
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StepBubble(step = 1, activeStep = currentStep, label = "النوع")
                    StepConnector(active = currentStep >= 2)
                    StepBubble(step = 2, activeStep = currentStep, label = "المحتوى")
                    StepConnector(active = currentStep >= 3)
                    StepBubble(step = 3, activeStep = currentStep, label = "الاستعجال")
                    StepConnector(active = currentStep >= 4)
                    StepBubble(step = 4, activeStep = currentStep, label = "الربط")
                    StepConnector(active = currentStep >= 5)
                    StepBubble(step = 5, activeStep = currentStep, label = "المراجعة")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // CURRENT STEP LAYOUT CONTENT
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    when (currentStep) {
                        1 -> OrderStep1Layout(
                            types = orderTypesList,
                            selectedTypes = selectedOrderTypes,
                            otherText = otherTypeText,
                            onOtherTextChange = { otherTypeText = it },
                            onToggleType = { type ->
                                if (selectedOrderTypes.contains(type)) {
                                    selectedOrderTypes.remove(type)
                                } else {
                                    selectedOrderTypes.add(type)
                                }
                            }
                        )
                        2 -> OrderStep2Layout(
                            tabIdx = contentTabIdx,
                            onTabChange = { contentTabIdx = it },
                            manualText = manualWritingText,
                            onManualTextChange = { manualWritingText = it },
                            catalogItems = selectedCatalogMedicines,
                            onAddCatalogItem = { med, qty ->
                                val existingIdx = selectedCatalogMedicines.indexOfFirst { it.first.medicineId == med.medicineId }
                                if (existingIdx != -1) {
                                    val currentPair = selectedCatalogMedicines[existingIdx]
                                    selectedCatalogMedicines[existingIdx] = Pair(currentPair.first, currentPair.second + qty)
                                } else {
                                    selectedCatalogMedicines.add(Pair(med, qty))
                                }
                            },
                            onRemoveCatalogItem = { idx -> selectedCatalogMedicines.removeAt(idx) },
                            attachedFiles = attachedFileNames,
                            onAttachFile = { attachedFileNames.add(it) }
                        )
                        3 -> OrderStep3Layout(
                            urgency = selectedUrgency,
                            onUrgencyChange = { selectedUrgency = it }
                        )
                        4 -> OrderStep4Layout(
                            routeOption = branchRouteOption,
                            onRouteOptionChange = {
                                branchRouteOption = it
                                if (it != "selected") {
                                    selectedBranchIdsForOrder.clear()
                                }
                            },
                            selectedBranchIds = selectedBranchIdsForOrder,
                            onToggleBranch = { id ->
                                if (selectedBranchIdsForOrder.contains(id)) {
                                    selectedBranchIdsForOrder.remove(id)
                                } else {
                                    selectedBranchIdsForOrder.add(id)
                                }
                            },
                            clientGovernorate = activeProfile.governorate,
                            closestBranch = closestBranchName,
                            closestDistance = closestBranchDistance,
                            clientLat = userLat,
                            clientLng = userLng
                        )
                        5 -> OrderStep5Layout(
                            selectedTypes = selectedOrderTypes,
                            otherText = otherTypeText,
                            manualText = manualWritingText,
                            catalogItems = selectedCatalogMedicines,
                            attachedFiles = attachedFileNames,
                            urgency = selectedUrgency,
                            routeOption = branchRouteOption,
                            selectedBranchIds = selectedBranchIdsForOrder,
                            profile = activeProfile
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // STEPS NAVIGATION ACTIONS
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (currentStep > 1) {
                        Button(
                            onClick = { currentStep-- },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .padding(end = 8.dp)
                                .testTag("order_back_btn")
                        ) {
                            Text("السابق ➡️", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    Button(
                        onClick = {
                            if (currentStep < 5) {
                                // Validation before moving forward
                                if (currentStep == 1 && selectedOrderTypes.isEmpty()) {
                                    Toast.makeText(context, "الرجاء اختيار نوع طلب واحد على الأقل", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                if (currentStep == 2) {
                                    val hasManual = manualWritingText.trim().isNotEmpty()
                                    val hasCatalog = selectedCatalogMedicines.isNotEmpty()
                                    val hasFiles = attachedFileNames.isNotEmpty()
                                    if (!hasManual && !hasCatalog && !hasFiles) {
                                        Toast.makeText(context, "الرجاء كتابة محتوى أو إضافة دواء أو إرفاق ملف للطلب", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                }
                                if (currentStep == 4 && branchRouteOption == "selected" && selectedBranchIdsForOrder.isEmpty()) {
                                    Toast.makeText(context, "الرجاء اختيار فرع واحد على الأقل لاستقبال الطلب", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                currentStep++
                            } else {
                                // SUBMIT ORDER NOW
                                val refNum = "YM-2026-" + (10000..99999).random()
                                val compiledContent = buildString {
                                    if (manualWritingText.trim().isNotEmpty()) {
                                        append("📝 وصف الطلب: $manualWritingText\n\n")
                                    }
                                    if (selectedCatalogMedicines.isNotEmpty()) {
                                        append("📋 أدوية من الكتالوج:\n")
                                        selectedCatalogMedicines.forEach { (med, qty) ->
                                            append("- ${med.name} (${med.category}) | العدد: $qty\n")
                                        }
                                        append("\n")
                                    }
                                    if (attachedFileNames.isNotEmpty()) {
                                        append("📎 الملفات المرفقة: ${attachedFileNames.joinToString(", ")}")
                                    }
                                }

                                val finalOrder = Order(
                                    orderId = refNum,
                                    clientId = activeProfile.clientId,
                                    clientName = activeProfile.institutionName,
                                    clientType = activeProfile.clientType,
                                    clientGovernorate = activeProfile.governorate,
                                    orderContent = compiledContent,
                                    attachments = attachedFileNames.toList(),
                                    urgencyLevel = selectedUrgency,
                                    broadcastType = branchRouteOption,
                                    targetBranches = selectedBranchIdsForOrder.toList(),
                                    status = "broadcast",
                                    createdAt = System.currentTimeMillis()
                                )

                                // Call the smart routing algorithm
                                FirebaseService.smartRouteOrder(
                                    order = finalOrder,
                                    clientGovernorate = activeProfile.governorate,
                                    clientLat = userLat,
                                    clientLng = userLng,
                                    broadcastType = branchRouteOption,
                                    selectedBranchIds = selectedBranchIdsForOrder.toList()
                                ) { targets ->
                                    generatedOrderId = refNum
                                    submittedBranchCount = targets.size
                                    isSubmitted = true
                                    Toast.makeText(context, "🚀 تم إرسال وبث طلب الشراء بنجاح للفروع!", Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MedGreenPrimary),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("order_submit_btn")
                    ) {
                        Text(
                            if (currentStep == 5) "🚀 إرسال الطلب الآن" else "التالي ⬅️",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OrderStep1Layout(
    types: List<String>,
    selectedTypes: List<String>,
    otherText: String,
    onOtherTextChange: (String) -> Unit,
    onToggleType: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                "حدد تصنيفات المنتجات المطلوبة 📝",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                "يمكنك اختيار تصنيف واحد أو أكثر في نفس طلب الشراء ليتم فلترتها بشكل ذكي للفرع المناسب.",
                color = Color.LightGray,
                fontSize = 11.sp,
                textAlign = TextAlign.Right,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        item {
            // Grid 2x2 of cards
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Box 1
                    TypeSelectionCard(
                        title = types[0],
                        isSelected = selectedTypes.contains(types[0]),
                        onClick = { onToggleType(types[0]) },
                        modifier = Modifier.weight(1f)
                    )
                    // Box 2
                    TypeSelectionCard(
                        title = types[1],
                        isSelected = selectedTypes.contains(types[1]),
                        onClick = { onToggleType(types[1]) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Box 3
                    TypeSelectionCard(
                        title = types[2],
                        isSelected = selectedTypes.contains(types[2]),
                        onClick = { onToggleType(types[2]) },
                        modifier = Modifier.weight(1f)
                    )
                    // Box 4
                    TypeSelectionCard(
                        title = types[3],
                        isSelected = selectedTypes.contains(types[3]),
                        onClick = { onToggleType(types[3]) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        if (selectedTypes.contains(types[3])) {
            item {
                OutlinedTextField(
                    value = otherText,
                    onValueChange = onOtherTextChange,
                    label = { Text("يرجى تحديد التصنيف الآخر المطلوب") },
                    placeholder = { Text("أجهزة طبية، موازين حرارة، إلخ...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("other_type_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = MedGreenPrimary,
                        unfocusedBorderColor = Color.Gray
                    )
                )
            }
        }
    }
}

@Composable
fun TypeSelectionCard(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(100.dp)
            .clickable { onClick() }
            .border(
                2.dp,
                if (isSelected) MedGreenPrimary else Color.Gray.copy(alpha = 0.5f),
                RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MedGreenPrimary.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.04f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun OrderStep2Layout(
    tabIdx: Int,
    onTabChange: (Int) -> Unit,
    manualText: String,
    onManualTextChange: (String) -> Unit,
    catalogItems: List<Pair<Medicine, Int>>,
    onAddCatalogItem: (Medicine, Int) -> Unit,
    onRemoveCatalogItem: (Int) -> Unit,
    attachedFiles: List<String>,
    onAttachFile: (String) -> Unit
) {
    val tabs = listOf("✏️ كتابة حرة", "📋 الكتالوج الموحد", "📎 إرفاق ملف")
    var catalogSearchQuery by remember { mutableStateOf("") }
    var matchingMedicines by remember { mutableStateOf<List<Medicine>>(emptyList()) }

    LaunchedEffect(catalogSearchQuery) {
        FirebaseService.getMedicines { allMeds ->
            matchingMedicines = if (catalogSearchQuery.isBlank()) {
                allMeds
            } else {
                allMeds.filter { it.name.contains(catalogSearchQuery, ignoreCase = true) || it.category.contains(catalogSearchQuery) }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "تفاصيل ومحتوى الطلب الدوائي 💊",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Right,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            "يمكنك دمج الكتابة الحرة مع اختيار الأدوية من الكتالوج أو تحميل وصفات مصورة في نفس الطلب المعتمد.",
            color = Color.LightGray,
            fontSize = 11.sp,
            textAlign = TextAlign.Right,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        // Tab Navigation for content inputs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                .padding(3.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (tabIdx == index) MedBluePrimary else Color.Transparent)
                        .clickable { onTabChange(index) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        title,
                        color = if (tabIdx == index) Color.White else Color.LightGray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Sub-layout renderer based on active tab
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (tabIdx) {
                0 -> {
                    // Manual free text
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("اكتب احتياجك الدوائي بالتفصيل (اسم الصنف، العيار، الكمية):", color = Color.White, fontSize = 12.sp)
                        OutlinedTextField(
                            value = manualText,
                            onValueChange = onManualTextChange,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .testTag("order_manual_writing_field"),
                            placeholder = { Text("مثال: \nأموكسيسيلين 500 ملغ - 20 كرتون \nباراسيتامول 1000 ملغ وريدي - 10 كرتون") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = MedGreenPrimary,
                                unfocusedBorderColor = Color.Gray
                            )
                        )
                    }
                }
                1 -> {
                    // Catalog select
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = catalogSearchQuery,
                            onValueChange = { catalogSearchQuery = it },
                            label = { Text("بحث في الكتالوج الدوائي الموحد 🔍") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = MedGreenPrimary,
                                unfocusedBorderColor = Color.Gray
                            )
                        )

                        // List of medicines to add
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(matchingMedicines) { med ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Button(
                                        onClick = { onAddCatalogItem(med, 10) },
                                        colors = ButtonDefaults.buttonColors(containerColor = MedGreenPrimary),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Text("إضافة (10) ➕", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(med.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        Text(med.category, color = Color.LightGray, fontSize = 9.sp)
                                    }
                                }
                            }
                        }

                        // Selected List
                        if (catalogItems.isNotEmpty()) {
                            Text("الأصناف المختارة للطلب الحركي:", color = MedGreenPrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(catalogItems.size) { index ->
                                    val pair = catalogItems[index]
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(6.dp))
                                            .padding(6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(
                                            onClick = { onRemoveCatalogItem(index) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = null, tint = MedRedPrimary)
                                        }

                                        Text(
                                            "${pair.first.name} | الكمية: ${pair.second}",
                                            color = Color.White,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // Upload files
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("أرفق روشتات مصورة أو ملفات طلبيات Excel/Word:", color = Color.White, fontSize = 12.sp, textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth())

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, Color.Gray, RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.04f))
                                .clickable {
                                    val simName = "Order_Doc_" + (10..99).random() + ".xlsx"
                                    onAttachFile(simName)
                                }
                                .testTag("order_attach_file_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.AttachFile, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("انقر لمحاكاة إرفاق ملف طلبيات 📁", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("يتم قبول صور الوصفات، ملفات Excel، نصوص Word", color = Color.Gray, fontSize = 9.sp)
                            }
                        }

                        if (attachedFiles.isNotEmpty()) {
                            Text("الملفات المرفقة بنجاح للطلب:", color = MedGreenPrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp, textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth())
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(attachedFiles) { name ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(6.dp))
                                            .padding(6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Verified, contentDescription = null, tint = MedGreenPrimary, modifier = Modifier.size(16.dp))
                                        Text(name, color = Color.White, fontSize = 11.sp)
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

@Composable
fun OrderStep3Layout(
    urgency: String,
    onUrgencyChange: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "مستوى استعجال الطلبية وأولوية الإمداد 🔴",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Right,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            "اختر مستوى الاستعجال المطلوب ليتم فلترتها وترتيبها في لوحة تحكم مدراء الفروع ولتنسيق سيارات النقل المبرد المناسبة.",
            color = Color.LightGray,
            fontSize = 11.sp,
            textAlign = TextAlign.Right,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        // Critical Card
        UrgencyOptionCard(
            title = "🔴 حرج جداً (خلال 24 ساعة فقط)",
            desc = "مخصص لحالات الطوارئ القصوى، العناية المركزة، نقص الأكسجين والعمليات الطارئة.",
            isSelected = (urgency == "critical"),
            onClick = { onUrgencyChange("critical") }
        )

        // High Card
        UrgencyOptionCard(
            title = "🟡 مستعجل (خلال 3 أيام)",
            desc = "طلبات الشراء الاعتيادية التي قاربت مخازن المنشأة على النفاد منها لتأمين الرعاية المباشرة.",
            isSelected = (urgency == "high"),
            onClick = { onUrgencyChange("high") }
        )

        // Normal Card
        UrgencyOptionCard(
            title = "🟢 عادي (خلال أسبوع)",
            desc = "لتغذية مخازن الأدوية الروتينية والحصص الدورية المجدولة شهرياً أو فصلياً.",
            isSelected = (urgency == "normal"),
            onClick = { onUrgencyChange("normal") }
        )
    }
}

@Composable
fun UrgencyOptionCard(
    title: String,
    desc: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(
                2.dp,
                if (isSelected) MedGreenPrimary else Color.Transparent,
                RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MedGreenPrimary.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.05f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(desc, color = Color.LightGray, fontSize = 11.sp, textAlign = TextAlign.Right)
        }
    }
}

@Composable
fun OrderStep4Layout(
    routeOption: String,
    onRouteOptionChange: (String) -> Unit,
    selectedBranchIds: List<String>,
    onToggleBranch: (String) -> Unit,
    clientGovernorate: String,
    closestBranch: String,
    closestDistance: Double,
    clientLat: Double,
    clientLng: Double
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                "قنوات التوجيه والربط مع فروع الشفاء 📢",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                "حدد الفروع التي ترغب ببث الطلب إليها للحصول على عروض أسعار منافسة.",
                color = Color.LightGray,
                fontSize = 11.sp,
                textAlign = TextAlign.Right,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }

        // Info Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MedGreenPrimary.copy(alpha = 0.12f)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.End) {
                    Text("موقع منشأتك المعتمد: محافظة ($clientGovernorate)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("أقرب فرع مخزني مبرد لك: فرع ($closestBranch) على بُعد (${String.format("%.1f", closestDistance)}) كم", color = MedGreenPrimary, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }

        // Option 1: All
        item {
            RouteOptionCard(
                title = "📢 إرسال لجميع الفروع الستة للمجموعة",
                desc = "يتم بث الاحتياج لكافة الفروع لضمان أوسع منافسة وتوفير البدايل بأسعار تنافسية ممتازة.",
                isSelected = (routeOption == "all"),
                onClick = { onRouteOptionChange("all") }
            )
        }

        // Option 2: Nearby
        item {
            RouteOptionCard(
                title = "📍 إرسال للفروع القريبة من موقعي (توجيه تلقائي)",
                desc = "يبث للخوارزمية الذكية الفروع في محيط 200 كم لسرعة فائقة وسلسلة تبريد دوائية مؤمنة بالكامل.",
                isSelected = (routeOption == "nearby"),
                onClick = { onRouteOptionChange("nearby") }
            )
        }

        // Option 3: Selected
        item {
            RouteOptionCard(
                title = "🎯 اختيار فروع محددة يدوياً",
                desc = "تصفح الفروع الستة النشطة للمجموعة وحدد الفروع التي ترغب بتوصيل العروض منها.",
                isSelected = (routeOption == "selected"),
                onClick = { onRouteOptionChange("selected") }
            )
        }

        if (routeOption == "selected") {
            item {
                Text("حدد الفروع المستهدفة للطلب من القائمة 👇:", color = MedGreenPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }

            items(FirebaseService.fallbackBranches) { branch ->
                val dist = FirebaseService.calculateDistanceKm(clientLat, clientLng, branch.latitude, branch.longitude)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.04f))
                        .clickable { onToggleBranch(branch.branchId) }
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = selectedBranchIds.contains(branch.branchId),
                        onCheckedChange = { onToggleBranch(branch.branchId) },
                        colors = CheckboxDefaults.colors(checkedColor = MedGreenPrimary)
                    )

                    Column(horizontalAlignment = Alignment.End) {
                        Text(branch.branchName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("محافظة: ${branch.governorate} | المسافة: ${String.format("%.1f", dist)} كم", color = Color.LightGray, fontSize = 10.sp)
                        Text("⭐ التقييم: 4.8/5 | الصفقات المكتملة: 150+", color = Color.Gray, fontSize = 9.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun RouteOptionCard(
    title: String,
    desc: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(2.dp, if (isSelected) MedGreenPrimary else Color.Transparent, RoundedCornerShape(10.dp)),
        colors = CardDefaults.cardColors(containerColor = if (isSelected) MedGreenPrimary.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.End) {
            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(desc, color = Color.LightGray, fontSize = 11.sp, textAlign = TextAlign.Right)
        }
    }
}

@Composable
fun OrderStep5Layout(
    selectedTypes: List<String>,
    otherText: String,
    manualText: String,
    catalogItems: List<Pair<Medicine, Int>>,
    attachedFiles: List<String>,
    urgency: String,
    routeOption: String,
    selectedBranchIds: List<String>,
    profile: ClientProfile
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "مراجعة تفاصيل طلب الشراء والمستلم 📋",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                "يرجى مراجعة ملخص الطلب وعنوان التوصيل وبيانات المسؤول قبل إتمام البث الذكي.",
                color = Color.LightGray,
                fontSize = 11.sp,
                textAlign = TextAlign.Right,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }

        // Order Summary Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.End) {
                    Text("📦 ملخص محتوى الطلبية", color = MedGreenPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Divider(color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp))

                    Text("• التصنيفات المطلوبة: ${selectedTypes.joinToString(", ")} ${if (otherText.isNotBlank()) "($otherText)" else ""}", color = Color.White, fontSize = 11.sp)
                    
                    if (manualText.isNotBlank()) {
                        Text("• الوصف الحر: $manualText", color = Color.LightGray, fontSize = 11.sp, maxLines = 3)
                    }

                    if (catalogItems.isNotEmpty()) {
                        Text("• أصناف الكتالوج المحددة: ${catalogItems.size} صنف طبي", color = Color.White, fontSize = 11.sp)
                    }

                    if (attachedFiles.isNotEmpty()) {
                        Text("• الملفات المرفقة: ${attachedFiles.joinToString(", ")}", color = Color.White, fontSize = 11.sp)
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "• مستوى الاستعجال: ${if (urgency == "critical") "🔴 حرج جداً" else if (urgency == "high") "🟡 مستعجل" else "🟢 عادي"}",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "• التوجيه المستهدف: ${if (routeOption == "all") "📢 جميع الفروع" else if (routeOption == "nearby") "📍 الفروع القريبة" else "🎯 فروع محددة (${selectedBranchIds.size} فرع)"}",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Masked Client Profile Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MedBluePrimary.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.End) {
                    Text("👤 بيانات جهة الطلب (معتمدة وموثقة)", color = MedGreenPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Divider(color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp))

                    Text("اسم المنشأة: ${profile.institutionName}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("العنوان: محافظة ${profile.governorate} | ${profile.city} | ${profile.neighborhood}", color = Color.LightGray, fontSize = 11.sp)

                    // Partially hidden responsible person and phone as requested
                    val maskedPerson = if (profile.responsiblePerson.length > 3) {
                        profile.responsiblePerson.take(3) + "***"
                    } else {
                        profile.responsiblePerson + "***"
                    }
                    val maskedPhone = if (profile.phone.length > 5) {
                        profile.phone.take(5) + "***" + profile.phone.takeLast(2)
                    } else {
                        profile.phone + "***"
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text("المسؤول المشتريات: $maskedPerson (مخفي جزئياً لأمان المعاملات)", color = Color.LightGray, fontSize = 11.sp)
                    Text("رقم هاتف الاتصال: $maskedPhone", color = Color.LightGray, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun CheckoutSuccessLayout(
    orderId: String,
    branchCount: Int,
    onTrackOrder: () -> Unit,
    onNewOrder: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Checking Animated Green Checkmark
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(MedGreenPrimary.copy(alpha = 0.15f), CircleShape)
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MedGreenPrimary,
                modifier = Modifier.size(80.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "تم إرسال طلبك بنجاح! 🎉",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "الرقم المرجعي الفريد للطلب: #$orderId",
            color = MedGreenPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "تم بث طلب الشراء بنجاح إلى ($branchCount) من فروع مجموعة الشفاء المؤهلة جغرافياً. ستتلقى إشعارات فورية بعروض الأسعار وتوفر البدائل في وقت قصير جداً.",
            color = Color.LightGray,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onTrackOrder,
            colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("track_order_success_btn")
        ) {
            Text("متابعة حالة الطلب والعروض 🔍", color = Color.White, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onNewOrder,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("create_another_order_btn")
        ) {
            Text("إرسال طلب شراء آخر 📝", fontWeight = FontWeight.Bold)
        }
    }
}
