package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DirectorNotification
import com.example.model.User
import com.example.model.UserAddress
import com.example.service.FirebaseService
import com.example.ui.theme.MedBluePrimary
import com.example.ui.theme.MedGreenPrimary
import com.example.ui.theme.MedRedPrimary
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BranchAddressSetupScreen(
    currentUser: User,
    onSetupCompleted: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Form inputs state
    var label by remember { mutableStateOf(currentUser.branchName.ifEmpty() { "الفرع الرئيسي" }) }
    var orgName by remember { mutableStateOf(currentUser.orgName.ifEmpty() { "مجموعة الشفاء للأدوية" }) }
    var phone by remember { mutableStateOf(currentUser.phone.ifEmpty() { "771111112" }) }
    var landmark by remember { mutableStateOf("") }
    
    var selectedGovernorate by remember { mutableStateOf(currentUser.governorate.ifEmpty() { "صنعاء" }) }
    var selectedDistrict by remember { mutableStateOf("") }
    var selectedNeighborhood by remember { mutableStateOf("") }
    var detailedDescription by remember { mutableStateOf("") }

    // Dropdown expanded states
    var govExpanded by remember { mutableStateOf(false) }
    var distExpanded by remember { mutableStateOf(false) }
    var neighExpanded by remember { mutableStateOf(false) }

    // Coordinates state
    var latitude by remember { mutableStateOf(15.3482) }
    var longitude by remember { mutableStateOf(44.2191) }
    var isManualMapSelection by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    // Yemen Governorates, Districts, and Neighborhoods hierarchy
    val hierarchy = remember {
        mapOf(
            "صنعاء" to mapOf(
                "السبعين" to listOf("حي الأصبحي", "حي حدة", "حي بيت بوس", "حي الخمسين"),
                "الصافية" to listOf("حي حارة الصافية", "حي البليلي", "حي التحرير الجنوبي"),
                "شعوب" to listOf("حي الروضة", "حي الحصبة الشمالية", "حي المطار"),
                "الثورة" to listOf("حي الحصبة", "حي الجراف", "حي سواد حنش"),
                "الوحدة" to listOf("حي جولة ريحة", "حي عصر", "حي بغداد")
            ),
            "عدن" to mapOf(
                "خور مكسر" to listOf("حي العروض", "حي الرشيد", "حي السلام", "حي أكتوبر"),
                "كريتر" to listOf("حي قلعة صيرة", "حي العيدروس", "حي السبيل"),
                "المنصورة" to listOf("حي التسعين", "حي الحجاز", "حي الدرين", "حي كابوتا"),
                "المعلا" to listOf("حي الدكة", "حي الشيخ إسحاق", "حي ردفان")
            ),
            "تعز" to mapOf(
                "الحوبان" to listOf("حي المدينة الصناعية", "حي الجند", "حي مفرق ماوية"),
                "القاهرة" to listOf("حي الدحي", "حي النسيرية", "حي المسبح"),
                "المظفر" to listOf("حي باب موسى", "حي النسيرية الغربي", "حي بئر باشا")
            ),
            "الحديدة" to mapOf(
                "الحالي" to listOf("حي غليل", "حي الربصة", "حي الشهداء"),
                "الميناء" to listOf("حي الكورنيش", "حي الشام", "حي اليمن"),
                "الحوك" to listOf("حي الربصة الغربي", "حي الهنود")
            ),
            "حضرموت" to mapOf(
                "المكلا" to listOf("حي الشرج", "حي الديس", "حي فوه", "حي روكب"),
                "سيئون" to listOf("حي السحيل", "حي القرن", "حي مريمة")
            ),
            "إب" to mapOf(
                "الظهار" to listOf("حي المعاين", "حي أبلان", "حي الروضة"),
                "المشنة" to listOf("حي جرافة", "حي الوعرة", "حي القديمة")
            ),
            "ذمار" to mapOf(
                "ذمار" to listOf("حي جامعة ذمار", "حي رداع", "حي الدرب")
            ),
            "مأرب" to mapOf(
                "مأرب" to listOf("حي الروضة", "حي المطار", "حي الشركة")
            )
        )
    }

    // Get districts list based on governorate
    val districts = hierarchy[selectedGovernorate]?.keys?.toList() ?: emptyList()
    
    // Auto reset district when governorate changes
    LaunchedEffect(selectedGovernorate) {
        if (selectedDistrict.isEmpty() || !districts.contains(selectedDistrict)) {
            selectedDistrict = districts.firstOrNull() ?: ""
        }
        val coords = FirebaseService.cityCoordinatesMap[selectedGovernorate]
        if (coords != null) {
            latitude = coords.first
            longitude = coords.second
        }
    }

    // Get neighborhoods list based on district
    val neighborhoods = hierarchy[selectedGovernorate]?.get(selectedDistrict) ?: emptyList()

    // Auto reset neighborhood when district changes
    LaunchedEffect(selectedDistrict) {
        if (selectedNeighborhood.isEmpty() || !neighborhoods.contains(selectedNeighborhood)) {
            selectedNeighborhood = neighborhoods.firstOrNull() ?: ""
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "إعداد موقع الفرع 🗺️",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 18.sp
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MedBluePrimary)
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
                // Welcome Banner Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier.fillMaxWidth().testTag("welcome_banner")
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(MedBluePrimary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Business,
                                contentDescription = null,
                                tint = MedBluePrimary,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "مرحباً يا مدير ${currentUser.name} 👋",
                            fontWeight = FontWeight.Bold,
                            color = Color.DarkGray,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "لإكمال تفعيل حسابك يرجى تحديد موقع فرعك",
                            fontWeight = FontWeight.SemiBold,
                            color = MedBluePrimary,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "هذه البيانات تُستخدم لتوجيه الطلبات لفرعك بدقة وإرسالها للمدير العام.",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // 1. Branch Information Section
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth().testTag("branch_info_card")
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "🏢 معلومات الفرع الأساسية",
                            fontWeight = FontWeight.Bold,
                            color = Color.DarkGray,
                            fontSize = 14.sp
                        )

                        // Location identifier label (pre-filled)
                        OutlinedTextField(
                            value = label,
                            onValueChange = { label = it },
                            label = { Text("اسم تعريفي للموقع") },
                            placeholder = { Text("مثال: فرع صنعاء الرئيسي، مستودع الحصبة") },
                            modifier = Modifier.fillMaxWidth().testTag("branch_setup_label_input"),
                            leadingIcon = { Icon(Icons.Default.Label, contentDescription = null, tint = MedBluePrimary) },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MedBluePrimary)
                        )

                        // Company/Branch Name (pre-filled)
                        OutlinedTextField(
                            value = orgName,
                            onValueChange = { orgName = it },
                            label = { Text("اسم الشركة / الفرع") },
                            modifier = Modifier.fillMaxWidth().testTag("branch_setup_org_input"),
                            leadingIcon = { Icon(Icons.Default.Business, contentDescription = null, tint = MedBluePrimary) },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MedBluePrimary)
                        )

                        // Direct phone
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("رقم هاتف الفرع المباشر") },
                            modifier = Modifier.fillMaxWidth().testTag("branch_setup_phone_input"),
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = MedBluePrimary) },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MedBluePrimary)
                        )

                        // Landmark
                        OutlinedTextField(
                            value = landmark,
                            onValueChange = { landmark = it },
                            label = { Text("أقرب معلم (مسجد / مدرسة / مبنى)") },
                            modifier = Modifier.fillMaxWidth().testTag("branch_setup_landmark_input"),
                            leadingIcon = { Icon(Icons.Default.Place, contentDescription = null, tint = MedBluePrimary) },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MedBluePrimary)
                        )
                    }
                }

                // 2. Geolocation Section
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth().testTag("geolocation_card")
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "🗺️ حدد موقع المستودع/المخزن بدقة",
                            fontWeight = FontWeight.Bold,
                            color = Color.DarkGray,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "سيستخدم النظام هذا الموقع لحساب المسافة وتوجيه الطلبات القريبة لفرعك.",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )

                        // Governorate Dropdown
                        Box(modifier = Modifier.fillMaxWidth()) {
                            ExposedDropdownMenuBox(
                                expanded = govExpanded,
                                onExpandedChange = { govExpanded = !govExpanded }
                            ) {
                                OutlinedTextField(
                                    value = selectedGovernorate,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("المحافظة") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = govExpanded) },
                                    modifier = Modifier.fillMaxWidth().menuAnchor().testTag("branch_setup_gov_dropdown"),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MedBluePrimary)
                                )
                                ExposedDropdownMenu(
                                    expanded = govExpanded,
                                    onDismissRequest = { govExpanded = false }
                                ) {
                                    hierarchy.keys.forEach { gov ->
                                        DropdownMenuItem(
                                            text = { Text(gov) },
                                            onClick = {
                                                selectedGovernorate = gov
                                                govExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // District Dropdown
                        Box(modifier = Modifier.fillMaxWidth()) {
                            ExposedDropdownMenuBox(
                                expanded = distExpanded,
                                onExpandedChange = { distExpanded = !distExpanded }
                            ) {
                                OutlinedTextField(
                                    value = selectedDistrict,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("المديرية / المدينة") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = distExpanded) },
                                    modifier = Modifier.fillMaxWidth().menuAnchor().testTag("branch_setup_dist_dropdown"),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MedBluePrimary)
                                )
                                ExposedDropdownMenu(
                                    expanded = distExpanded,
                                    onDismissRequest = { distExpanded = false }
                                ) {
                                    districts.forEach { dist ->
                                        DropdownMenuItem(
                                            text = { Text(dist) },
                                            onClick = {
                                                selectedDistrict = dist
                                                distExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Neighborhood Dropdown
                        Box(modifier = Modifier.fillMaxWidth()) {
                            ExposedDropdownMenuBox(
                                expanded = neighExpanded,
                                onExpandedChange = { neighExpanded = !neighExpanded }
                            ) {
                                OutlinedTextField(
                                    value = selectedNeighborhood,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("الحي / المنطقة") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = neighExpanded) },
                                    modifier = Modifier.fillMaxWidth().menuAnchor().testTag("branch_setup_neigh_dropdown"),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MedBluePrimary)
                                )
                                ExposedDropdownMenu(
                                    expanded = neighExpanded,
                                    onDismissRequest = { neighExpanded = false }
                                ) {
                                    neighborhoods.forEach { neigh ->
                                        DropdownMenuItem(
                                            text = { Text(neigh) },
                                            onClick = {
                                                selectedNeighborhood = neigh
                                                neighExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // GPS Simulation and Manual pinpoint
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    isManualMapSelection = false
                                    // Simulated GPS fetch with minor jitter
                                    val baseCoords = FirebaseService.cityCoordinatesMap[selectedGovernorate] ?: Pair(15.3482, 44.2191)
                                    val jitterLat = (Random.nextDouble() - 0.5) * 0.01
                                    val jitterLon = (Random.nextDouble() - 0.5) * 0.01
                                    latitude = Math.round((baseCoords.first + jitterLat) * 10000.0) / 10000.0
                                    longitude = Math.round((baseCoords.second + jitterLon) * 10000.0) / 10000.0
                                    Toast.makeText(context, "📍 تم تحديد إحداثيات GPS المستودع بنجاح!", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MedGreenPrimary),
                                modifier = Modifier.weight(1f).height(44.dp).testTag("branch_setup_gps_btn"),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("📍 موقعي الحالي (GPS)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    isManualMapSelection = !isManualMapSelection
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isManualMapSelection) MedRedPrimary else MedBluePrimary
                                ),
                                modifier = Modifier.weight(1f).height(44.dp).testTag("branch_setup_manual_btn"),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    if (isManualMapSelection) "إلغاء التحديد اليدوي" else "تحديد يدوياً (الخريطة)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Map Canvas Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFEFF6FF))
                                .border(2.dp, if (isManualMapSelection) MedGreenPrimary else Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                        ) {
                            Canvas(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .pointerInput(isManualMapSelection) {
                                        if (isManualMapSelection) {
                                            detectTapGestures { offset ->
                                                val pctX = offset.x / size.width
                                                val pctY = offset.y / size.height

                                                val computedLong = 42.0 + (pctX * 10.0)
                                                val computedLat = 18.0 - (pctY * 6.0)

                                                latitude = Math.round(computedLat * 10000.0) / 10000.0
                                                longitude = Math.round(computedLong * 10000.0) / 10000.0
                                            }
                                        }
                                    }
                            ) {
                                val canvasWidth = size.width
                                val canvasHeight = size.height

                                // Draw grids
                                val gridColor = Color(0xFFDBEAFE)
                                for (i in 1..4) {
                                    drawLine(gridColor, Offset(canvasWidth * i / 5f, 0f), Offset(canvasWidth * i / 5f, canvasHeight), strokeWidth = 1f)
                                    drawLine(gridColor, Offset(0f, canvasHeight * i / 5f), Offset(canvasWidth, canvasHeight * i / 5f), strokeWidth = 1f)
                                }

                                // Draw Yemeni city points
                                FirebaseService.cityCoordinatesMap.forEach { (_, coords) ->
                                    val x = ((coords.second - 42.0) / 10.0) * canvasWidth
                                    val y = ((18.0 - coords.first) / 6.0) * canvasHeight
                                    drawCircle(color = Color(0x152563EB), radius = 12f, center = Offset(x.toFloat(), y.toFloat()))
                                    drawCircle(color = Color(0x400F172A), radius = 3f, center = Offset(x.toFloat(), y.toFloat()))
                                }

                                // Draw Pin marker
                                val markerX = ((longitude - 42.0) / 10.0) * canvasWidth
                                val markerY = ((18.0 - latitude) / 6.0) * canvasHeight

                                drawCircle(color = MedGreenPrimary.copy(alpha = 0.3f), radius = 20f, center = Offset(markerX.toFloat(), markerY.toFloat()))
                                drawCircle(color = MedGreenPrimary, radius = 7f, center = Offset(markerX.toFloat(), markerY.toFloat()))
                                drawCircle(color = Color.White, radius = 2.5f, center = Offset(markerX.toFloat(), markerY.toFloat()))
                            }

                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = if (isManualMapSelection) "اضغط على الخريطة لوضع الدبوس 📍" else "انقر 'تحديد يدوياً' للتحكم 🗺️",
                                    fontSize = 10.sp,
                                    color = if (isManualMapSelection) MedGreenPrimary else Color.DarkGray,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Display Coordinates
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF1F5F9), RoundedCornerShape(6.dp))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("خط العرض: $latitude", fontSize = 11.sp, color = Color.DarkGray, fontWeight = FontWeight.Bold)
                            Text("خط الطول: $longitude", fontSize = 11.sp, color = Color.DarkGray, fontWeight = FontWeight.Bold)
                        }

                        // Detailed Address description
                        OutlinedTextField(
                            value = detailedDescription,
                            onValueChange = { detailedDescription = it },
                            label = { Text("وصف تفصيلي للعنوان") },
                            placeholder = { Text("مثال: شارع التسعين - بجانب مستشفى الأمل - مقابل شركة النفط") },
                            modifier = Modifier.fillMaxWidth().testTag("branch_setup_desc_input"),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MedBluePrimary)
                        )
                    }
                }

                // Action Save Button
                Button(
                    onClick = {
                        if (label.isEmpty() || orgName.isEmpty() || phone.isEmpty() || selectedGovernorate.isEmpty() || selectedDistrict.isEmpty() || selectedNeighborhood.isEmpty() || detailedDescription.isEmpty()) {
                            Toast.makeText(context, "⚠️ يرجى ملء جميع الحقول المطلوبة بما في ذلك الوصف التفصيلي!", Toast.LENGTH_LONG).show()
                            return@Button
                        }

                        isSaving = true
                        val fullAddrText = "$selectedGovernorate - $selectedDistrict - $selectedNeighborhood - $detailedDescription"

                        // Create address model
                        val addrId = "addr_" + System.currentTimeMillis()
                        val newAddress = UserAddress(
                            addressId = addrId,
                            userId = currentUser.userId,
                            userType = "branch_manager",
                            label = label,
                            hospitalOrCompanyName = orgName,
                            nearbyLandmark = landmark,
                            governorate = selectedGovernorate,
                            district = selectedDistrict,
                            neighborhood = selectedNeighborhood,
                            fullAddress = fullAddrText,
                            latitude = latitude,
                            longitude = longitude,
                            isDefault = true,
                            createdAt = System.currentTimeMillis()
                        )

                        // 1. Save Address in Firestore
                        FirebaseService.addUserAddress(newAddress) { success, _ ->
                            if (success) {
                                // 2. Update branch location
                                FirebaseService.updateBranchLocation(
                                    branchId = currentUser.branchId,
                                    address = fullAddrText,
                                    lat = latitude,
                                    lng = longitude,
                                    managerPhone = phone
                                ) { branchSuccess ->
                                    // 3. Notify Director
                                    val notification = DirectorNotification(
                                        notificationId = "notif_" + System.currentTimeMillis(),
                                        title = "مدير فرع فعّل حسابه",
                                        message = "${currentUser.name} - ${currentUser.branchName} قام بإعداد موقع الفرع وتفعيل حسابه بنجاح",
                                        orderId = "",
                                        clientId = currentUser.userId,
                                        clientName = currentUser.name,
                                        createdAt = System.currentTimeMillis(),
                                        read = false
                                    )

                                    FirebaseService.notifyDirector(notification) { _ ->
                                        isSaving = false
                                        Toast.makeText(context, "✅ تم تفعيل حسابك وحفظ موقع الفرع بنجاح!", Toast.LENGTH_LONG).show()
                                        onSetupCompleted()
                                    }
                                }
                            } else {
                                isSaving = false
                                Toast.makeText(context, "❌ فشل حفظ العنوان، يرجى المحاولة لاحقاً", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("branch_setup_save_btn"),
                    shape = RoundedCornerShape(10.dp),
                    enabled = !isSaving
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("✅ حفظ وتفعيل الحساب", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}
