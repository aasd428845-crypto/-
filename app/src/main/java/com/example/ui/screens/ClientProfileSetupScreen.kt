package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ClientProfile
import com.example.service.FirebaseService
import com.example.ui.theme.MedBluePrimary
import com.example.ui.theme.MedGreenPrimary
import com.example.ui.theme.MedRedPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientProfileSetupScreen(
    userId: String,
    onSetupCompleted: () -> Unit
) {
    val context = LocalContext.current
    var currentStep by remember { mutableStateOf(1) } // 1, 2, 3

    // Step 1 States: Institution Info
    var institutionName by remember { mutableStateOf("") }
    var clientType by remember { mutableStateOf("hospital") } // hospital / pharmacy
    var responsiblePerson by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("+967 ") }
    var alternatePhone by remember { mutableStateOf("") }
    var licenseNumber by remember { mutableStateOf("") }
    var isLicenseUploaded by remember { mutableStateOf(false) }

    // Step 2 States: Location Info
    val governorates = listOf("صنعاء", "عدن", "تعز", "الحديدة", "حضرموت", "إب")
    val citiesMap = mapOf(
        "صنعاء" to listOf("الصافية", "السبعين", "شعوب", "الحصبة", "الوحدة"),
        "عدن" to listOf("خور مكسر", "كريتر", "المنصورة", "الشيخ عثمان", "المعلا"),
        "تعز" to listOf("الحوبان", "القاهرة", "المظفر", "صالة"),
        "الحديدة" to listOf("الميناء", "الحوك", "الحالي"),
        "حضرموت" to listOf("المكلا", "الشحر", "سيئون", "تريم"),
        "إب" to listOf("الظهار", "المشنة", "جبلة", "يريم")
    )

    var selectedGovernorate by remember { mutableStateOf("صنعاء") }
    var selectedCity by remember { mutableStateOf("الصافية") }
    var neighborhood by remember { mutableStateOf("") }
    var landmark by remember { mutableStateOf("") }
    var fullAddressDesc by remember { mutableStateOf("") }

    // Mock Map Location (starts at Sanaa center)
    var pinLat by remember { mutableStateOf(15.3482) }
    var pinLng by remember { mutableStateOf(44.2191) }

    // Nearest Branch Calculation based on selection or pin
    var nearestBranchName by remember { mutableStateOf("فرع صنعاء الرئيسي") }
    var nearestBranchId by remember { mutableStateOf("branch_sanaa") }
    var nearestBranchDistance by remember { mutableStateOf(0.0) }

    // Step 3 States: Financial Options
    var preferredPayment by remember { mutableStateOf("تحويل بنكي") }
    var paymentAccount by remember { mutableStateOf("") }

    // Function to calculate distance dynamically
    fun updateNearestBranch(lat: Double, lng: Double) {
        var closestBranch = FirebaseService.fallbackBranches.first()
        var minDistance = Double.MAX_VALUE

        FirebaseService.fallbackBranches.forEach { branch ->
            val dist = FirebaseService.calculateDistanceKm(lat, lng, branch.latitude, branch.longitude)
            if (dist < minDistance) {
                minDistance = dist
                closestBranch = branch
            }
        }
        nearestBranchName = closestBranch.branchName
        nearestBranchId = closestBranch.branchId
        nearestBranchDistance = minDistance
    }

    // Trigger update on governorate/coordinates change
    LaunchedEffect(selectedGovernorate) {
        val coords = FirebaseService.cityCoordinatesMap[selectedGovernorate]
        if (coords != null) {
            pinLat = coords.first
            pinLng = coords.second
            updateNearestBranch(pinLat, pinLng)
        }
        val list = citiesMap[selectedGovernorate]
        if (!list.isNullOrEmpty()) {
            selectedCity = list.first()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("إعداد الملف الطبي الترحيبي 🏥", fontWeight = FontWeight.Bold, color = Color.White) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MedBluePrimary, titleContentColor = Color.White)
            )
        }
    ) { paddingVals ->
        Column(
            modifier = Modifier
                .padding(paddingVals)
                .fillMaxSize()
                .background(Color(0xFF0F172A)) // Aesthetic theme
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // STEP PROGRESS INDICATOR
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StepBubble(step = 1, activeStep = currentStep, label = "المنشأة")
                StepConnector(active = currentStep >= 2)
                StepBubble(step = 2, activeStep = currentStep, label = "الموقع")
                StepConnector(active = currentStep >= 3)
                StepBubble(step = 3, activeStep = currentStep, label = "الدفع")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // STEP CONTENT DISPATCHER
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (currentStep) {
                    1 -> {
                        Step1Layout(
                            institutionName = institutionName,
                            onInstChange = { institutionName = it },
                            clientType = clientType,
                            onTypeChange = { clientType = it },
                            responsiblePerson = responsiblePerson,
                            onRespChange = { responsiblePerson = it },
                            phone = phone,
                            onPhoneChange = { phone = it },
                            alternatePhone = alternatePhone,
                            onAltPhoneChange = { alternatePhone = it },
                            licenseNumber = licenseNumber,
                            onLicenseChange = { licenseNumber = it },
                            isLicenseUploaded = isLicenseUploaded,
                            onUploadClick = { isLicenseUploaded = true }
                        )
                    }
                    2 -> {
                        Step2Layout(
                            governorates = governorates,
                            selectedGovernorate = selectedGovernorate,
                            onGovChange = { selectedGovernorate = it },
                            cities = citiesMap[selectedGovernorate] ?: emptyList(),
                            selectedCity = selectedCity,
                            onCityChange = { selectedCity = it },
                            neighborhood = neighborhood,
                            onNeighChange = { neighborhood = it },
                            landmark = landmark,
                            onLandChange = { landmark = it },
                            fullAddressDesc = fullAddressDesc,
                            onAddrDescChange = { fullAddressDesc = it },
                            pinLat = pinLat,
                            pinLng = pinLng,
                            onGpsClick = {
                                // Simulate GPS request
                                val coords = FirebaseService.cityCoordinatesMap[selectedGovernorate]
                                if (coords != null) {
                                    pinLat = coords.first + (Math.random() - 0.5) * 0.05
                                    pinLng = coords.second + (Math.random() - 0.5) * 0.05
                                    updateNearestBranch(pinLat, pinLng)
                                    Toast.makeText(context, "📍 تم تحديد الإحداثيات بنجاح عبر GPS المحاكي", Toast.LENGTH_SHORT).show()
                                }
                            },
                            nearestBranchName = nearestBranchName,
                            nearestBranchDistance = nearestBranchDistance
                        )
                    }
                    3 -> {
                        Step3Layout(
                            preferredPayment = preferredPayment,
                            onPaymentChange = { preferredPayment = it },
                            paymentAccount = paymentAccount,
                            onAccountChange = { paymentAccount = it }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // STEP NAVIGATION CONTROLS
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
                            .testTag("setup_back_btn")
                    ) {
                        Text("السابق ➡️", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                if (currentStep < 3) {
                    Button(
                        onClick = {
                            if (currentStep == 1) {
                                if (institutionName.isBlank() || responsiblePerson.isBlank() || phone.isBlank()) {
                                    Toast.makeText(context, "يرجى ملء جميع الحقول الإلزامية في الخطوة الأولى", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                if (!isLicenseUploaded) {
                                    Toast.makeText(context, "يرجى النقر لرفع ترخيص وزارة الصحة", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                            }
                            currentStep++
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MedGreenPrimary),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("setup_next_btn")
                    ) {
                        Text("التالي ⬅️", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                } else {
                    // Step 3 Completion / Skip Buttons
                    Column(modifier = Modifier.weight(1f)) {
                        Button(
                            onClick = {
                                saveClientProfileData(
                                    userId = userId,
                                    instName = institutionName,
                                    clType = clientType,
                                    respPerson = responsiblePerson,
                                    ph = phone,
                                    altPh = alternatePhone,
                                    licNo = licenseNumber,
                                    licImg = "https://images.unsplash.com/photo-1576091160550-2173dba999ef",
                                    gov = selectedGovernorate,
                                    city = selectedCity,
                                    neigh = neighborhood,
                                    land = landmark,
                                    fullAddr = if (fullAddressDesc.isNotEmpty()) fullAddressDesc else "$selectedGovernorate - $selectedCity - $neighborhood",
                                    lat = pinLat,
                                    lng = pinLng,
                                    bId = nearestBranchId,
                                    bName = nearestBranchName,
                                    payPref = preferredPayment,
                                    payAcc = paymentAccount,
                                    onComplete = onSetupCompleted,
                                    toastContext = context
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MedGreenPrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("setup_finish_btn")
                        ) {
                            Text("حفظ وإنهاء الملف 🚀", color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        TextButton(
                            onClick = {
                                saveClientProfileData(
                                    userId = userId,
                                    instName = institutionName,
                                    clType = clientType,
                                    respPerson = responsiblePerson,
                                    ph = phone,
                                    altPh = alternatePhone,
                                    licNo = licenseNumber,
                                    licImg = "https://images.unsplash.com/photo-1576091160550-2173dba999ef",
                                    gov = selectedGovernorate,
                                    city = selectedCity,
                                    neigh = neighborhood,
                                    land = landmark,
                                    fullAddr = if (fullAddressDesc.isNotEmpty()) fullAddressDesc else "$selectedGovernorate - $selectedCity - $neighborhood",
                                    lat = pinLat,
                                    lng = pinLng,
                                    bId = nearestBranchId,
                                    bName = nearestBranchName,
                                    payPref = "كاش",
                                    payAcc = "",
                                    onComplete = onSetupCompleted,
                                    toastContext = context
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("setup_skip_step3_btn")
                        ) {
                            Text("تخطي هذه الخطوة الآن ↩", color = Color.LightGray, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

private fun saveClientProfileData(
    userId: String,
    instName: String,
    clType: String,
    respPerson: String,
    ph: String,
    altPh: String,
    licNo: String,
    licImg: String,
    gov: String,
    city: String,
    neigh: String,
    land: String,
    fullAddr: String,
    lat: Double,
    lng: Double,
    bId: String,
    bName: String,
    payPref: String,
    payAcc: String,
    onComplete: () -> Unit,
    toastContext: android.content.Context
) {
    val newProfile = ClientProfile(
        clientId = "client_" + System.currentTimeMillis(),
        userId = userId,
        institutionName = instName,
        clientType = clType,
        responsiblePerson = respPerson,
        phone = ph,
        alternatePhone = altPh,
        licenseNumber = licNo,
        licenseImageUrl = licImg,
        governorate = gov,
        city = city,
        district = city,
        neighborhood = neigh,
        landmark = land,
        fullAddress = fullAddr,
        latitude = lat,
        longitude = lng,
        assignedBranchId = bId,
        assignedBranchName = bName,
        preferredPayment = payPref,
        paymentAccount = payAcc,
        isVerified = false, // starts unverified, pending director approval
        isActive = true,
        profileCompleted = true,
        joinedAt = System.currentTimeMillis()
    )

    FirebaseService.setupClientProfile(newProfile, {
        Toast.makeText(toastContext, "🎉 تم تفعيل ملف منشأتك بنجاح وتم ربطك بـ $bName تلقائياً!", Toast.LENGTH_LONG).show()
        onComplete()
    }, {
        Toast.makeText(toastContext, "فشل حفظ الملف: $it", Toast.LENGTH_SHORT).show()
    })
}

@Composable
fun StepBubble(step: Int, activeStep: Int, label: String) {
    val isDone = activeStep > step
    val isActive = activeStep == step

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(
                    if (isDone) MedGreenPrimary else if (isActive) MedBluePrimary else Color.Gray.copy(
                        alpha = 0.5f
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isDone) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            } else {
                Text(step.toString(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, color = if (isActive) MedGreenPrimary else Color.LightGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun RowScope.StepConnector(active: Boolean) {
    Box(
        modifier = Modifier
            .weight(1f)
            .height(3.dp)
            .padding(horizontal = 8.dp)
            .background(if (active) MedGreenPrimary else Color.Gray.copy(alpha = 0.3f))
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Step1Layout(
    institutionName: String,
    onInstChange: (String) -> Unit,
    clientType: String,
    onTypeChange: (String) -> Unit,
    responsiblePerson: String,
    onRespChange: (String) -> Unit,
    phone: String,
    onPhoneChange: (String) -> Unit,
    alternatePhone: String,
    onAltPhoneChange: (String) -> Unit,
    licenseNumber: String,
    onLicenseChange: (String) -> Unit,
    isLicenseUploaded: Boolean,
    onUploadClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                "معلومات منشأتك الطبية 🏢",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                "هذه البيانات تساعدنا في التحقق السريع من تصاريح منشأتك وربط طلبياتك بشكل دقيق وقانوني.",
                color = Color.LightGray,
                fontSize = 11.sp,
                textAlign = TextAlign.Right,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )
        }

        item {
            // Customer Type selector buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (clientType == "hospital") MedBluePrimary else Color.Transparent)
                        .clickable { onTypeChange("hospital") }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🏥 مستشفى أو مركز طبي", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (clientType == "pharmacy") MedBluePrimary else Color.Transparent)
                        .clickable { onTypeChange("pharmacy") }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("💊 صيدلية أو مستودع", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            OutlinedTextField(
                value = institutionName,
                onValueChange = onInstChange,
                label = { Text("الاسم الرسمي للمنشأة الطبية") },
                placeholder = { Text("مثال: مستشفى الثورة العام / صيدلية النور المركزية") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("setup_inst_name_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = MedGreenPrimary,
                    unfocusedBorderColor = Color.Gray
                ),
                singleLine = true
            )
        }

        item {
            OutlinedTextField(
                value = responsiblePerson,
                onValueChange = onRespChange,
                label = { Text("اسم المسؤول عن الطلبات") },
                placeholder = { Text("مثال: مدير الصيدلية / رئيس قسم المشتريات") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("setup_resp_person_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = MedGreenPrimary,
                    unfocusedBorderColor = Color.Gray
                ),
                singleLine = true
            )
        }

        item {
            OutlinedTextField(
                value = phone,
                onValueChange = onPhoneChange,
                label = { Text("رقم الهاتف الرئيسي") },
                placeholder = { Text("+967 77xxxxxxx") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("setup_phone_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = MedGreenPrimary,
                    unfocusedBorderColor = Color.Gray
                ),
                singleLine = true
            )
        }

        item {
            OutlinedTextField(
                value = alternatePhone,
                onValueChange = onAltPhoneChange,
                label = { Text("رقم هاتف بديل (اختياري)") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = MedGreenPrimary,
                    unfocusedBorderColor = Color.Gray
                ),
                singleLine = true
            )
        }

        item {
            OutlinedTextField(
                value = licenseNumber,
                onValueChange = onLicenseChange,
                label = { Text("رقم الترخيص الصحي من وزارة الصحة") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("setup_license_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = MedGreenPrimary,
                    unfocusedBorderColor = Color.Gray
                ),
                singleLine = true
            )
        }

        item {
            // Upload License Document Preview Mock
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(2.dp, if (isLicenseUploaded) MedGreenPrimary else Color.Gray, RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.04f))
                    .clickable { onUploadClick() }
                    .testTag("setup_upload_license_btn"),
                contentAlignment = Alignment.Center
            ) {
                if (isLicenseUploaded) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = MedGreenPrimary, modifier = Modifier.size(32.dp))
                        Column {
                            Text("✅ تم إرفاق صورة السجل / الترخيص بنجاح", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("انقر لتغيير المستند المرفق", color = Color.LightGray, fontSize = 10.sp)
                        }
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("📤 ارفع صورة الترخيص الطبي أو السجل التجاري", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("الحد الأقصى للملف 5 ميجابايت (PNG / PDF)", color = Color.Gray, fontSize = 9.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun Step2Layout(
    governorates: List<String>,
    selectedGovernorate: String,
    onGovChange: (String) -> Unit,
    cities: List<String>,
    selectedCity: String,
    onCityChange: (String) -> Unit,
    neighborhood: String,
    onNeighChange: (String) -> Unit,
    landmark: String,
    onLandChange: (String) -> Unit,
    fullAddressDesc: String,
    onAddrDescChange: (String) -> Unit,
    pinLat: Double,
    pinLng: Double,
    onGpsClick: () -> Unit,
    nearestBranchName: String,
    nearestBranchDistance: Double
) {
    var expandedGov by remember { mutableStateOf(false) }
    var expandedCity by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                "حدد موقع منشأتك بدقة 📍",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                "تحديد موقعك الجغرافي يربطك بالفرع الأنسب للمجموعة تلقائياً لتسريع التوصيل والحفاظ على جودة الدواء.",
                color = Color.LightGray,
                fontSize = 11.sp,
                textAlign = TextAlign.Right,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )
        }

        // Governorates Dropdown
        item {
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = selectedGovernorate,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("المحافظة") },
                    trailingIcon = {
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier.clickable { expandedGov = true },
                            tint = Color.White
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expandedGov = true }
                        .testTag("dropdown_governorate"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = MedGreenPrimary,
                        unfocusedBorderColor = Color.Gray
                    )
                )

                DropdownMenu(
                    expanded = expandedGov,
                    onDismissRequest = { expandedGov = false },
                    modifier = Modifier.background(Color(0xFF1E293B))
                ) {
                    governorates.forEach { gov ->
                        DropdownMenuItem(
                            text = { Text(gov, color = Color.White) },
                            onClick = {
                                onGovChange(gov)
                                expandedGov = false
                            }
                        )
                    }
                }
            }
        }

        // City Dropdown (Dependent)
        item {
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = selectedCity,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("المديرية / المدينة") },
                    trailingIcon = {
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier.clickable { expandedCity = true },
                            tint = Color.White
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expandedCity = true }
                        .testTag("dropdown_city"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = MedGreenPrimary,
                        unfocusedBorderColor = Color.Gray
                    )
                )

                DropdownMenu(
                    expanded = expandedCity,
                    onDismissRequest = { expandedCity = false },
                    modifier = Modifier.background(Color(0xFF1E293B))
                ) {
                    cities.forEach { city ->
                        DropdownMenuItem(
                            text = { Text(city, color = Color.White) },
                            onClick = {
                                onCityChange(city)
                                expandedCity = false
                            }
                        )
                    }
                }
            }
        }

        item {
            OutlinedTextField(
                value = neighborhood,
                onValueChange = onNeighChange,
                label = { Text("الحي / المنطقة") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = MedGreenPrimary,
                    unfocusedBorderColor = Color.Gray
                ),
                singleLine = true
            )
        }

        item {
            OutlinedTextField(
                value = landmark,
                onValueChange = onLandChange,
                label = { Text("أقرب معلم مشهور") },
                placeholder = { Text("مثال: بجانب مستشفى الثورة / خلف مدرسة النور") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = MedGreenPrimary,
                    unfocusedBorderColor = Color.Gray
                ),
                singleLine = true
            )
        }

        item {
            OutlinedTextField(
                value = fullAddressDesc,
                onValueChange = onAddrDescChange,
                label = { Text("وصف تفصيلي كامل للعنوان") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = MedGreenPrimary,
                    unfocusedBorderColor = Color.Gray
                )
            )
        }

        // MOCK GOOGLE MAPS INTERACTIVE CARD
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .border(1.dp, Color.Gray, RoundedCornerShape(12.dp))
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Draw custom decorative lines mimicking roads and zones
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("خريطة التغطية الجغرافية 🗺️", color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("GPS المحاكي", color = MedGreenPrimary, fontSize = 10.sp)
                        }

                        // Center Map Pin simulation graphics
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp)
                                .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Place, contentDescription = null, tint = MedRedPrimary, modifier = Modifier.size(36.dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("إحداثيات منشأتك المعتمدة:", color = Color.White, fontSize = 10.sp)
                                Text("Lat: ${String.format("%.4f", pinLat)}, Lng: ${String.format("%.4f", pinLng)}", color = MedGreenPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // GPS Trigger button
                        Button(
                            onClick = onGpsClick,
                            colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary, contentColor = Color.White),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(32.dp)
                                .testTag("gps_locate_btn"),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("📍 تحديد موقعي تلقائياً (GPS)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Nearest Branch Display Card
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MedGreenPrimary.copy(alpha = 0.12f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MedGreenPrimary, RoundedCornerShape(12.dp))
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.AltRoute, contentDescription = null, tint = MedGreenPrimary)
                        Text("الربط الجغرافي الذكي المخصص ⚡", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "تم تحديد موقعك في محافظة ($selectedGovernorate)",
                        color = Color.LightGray,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "أقرب فرع لك: $nearestBranchName - على بُعد (${String.format("%.1f", nearestBranchDistance)}) كم",
                        color = MedGreenPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun Step3Layout(
    preferredPayment: String,
    onPaymentChange: (String) -> Unit,
    paymentAccount: String,
    onAccountChange: (String) -> Unit
) {
    val paymentOptions = listOf("تحويل بنكي", "كاش", "MTN كاش", "يمن موبايل كاش")

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "إعدادات الحساب وطريقة الدفع 💳 (اختياري)",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Right,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            "حدد طريقة الدفع المفضلة لديك لتسريع مطابقة طلبات الشراء والعروض المقدمة لك من الفروع المختلفة.",
            color = Color.LightGray,
            fontSize = 11.sp,
            textAlign = TextAlign.Right,
            modifier = Modifier.fillMaxWidth()
        )

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                paymentOptions.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (preferredPayment == option) MedBluePrimary.copy(alpha = 0.2f) else Color.Transparent
                            )
                            .clickable { onPaymentChange(option) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        RadioButton(
                            selected = (preferredPayment == option),
                            onClick = { onPaymentChange(option) },
                            colors = RadioButtonDefaults.colors(selectedColor = MedGreenPrimary, unselectedColor = Color.Gray)
                        )
                        Text(option, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        if (preferredPayment != "كاش") {
            OutlinedTextField(
                value = paymentAccount,
                onValueChange = onAccountChange,
                label = { Text(if (preferredPayment == "تحويل بنكي") "رقم الحساب البنكي المعتمد" else "رقم محفظة الجوال") },
                placeholder = { Text("مثال: 3201445566 أو 777xxxxxx") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("setup_payment_account_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = MedGreenPrimary,
                    unfocusedBorderColor = Color.Gray
                ),
                singleLine = true
            )
        }
    }
}
