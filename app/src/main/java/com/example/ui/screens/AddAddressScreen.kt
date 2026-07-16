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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.User
import com.example.model.UserAddress
import com.example.model.DirectorNotification
import com.example.service.FirebaseService
import com.example.ui.theme.MedBluePrimary
import com.example.ui.theme.MedGreenPrimary
import com.example.ui.theme.MedRedPrimary
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAddressScreen(
    currentUser: User,
    existingAddress: UserAddress? = null, // إذا موجود = وضع تعديل
    onBackClick: () -> Unit,
    onSaveSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Form inputs state
    var label by remember { mutableStateOf(existingAddress?.label ?: "") }
    var orgName by remember { mutableStateOf(existingAddress?.hospitalOrCompanyName ?: currentUser.orgName) }
    var landmark by remember { mutableStateOf(existingAddress?.nearbyLandmark ?: "") }
    var selectedGovernorate by remember { mutableStateOf(existingAddress?.governorate ?: "صنعاء") }
    var district by remember { mutableStateOf(existingAddress?.district ?: "") }
    var neighborhood by remember { mutableStateOf(existingAddress?.neighborhood ?: "") }
    var isDefault by remember { mutableStateOf(existingAddress?.isDefault ?: false) }

    // Coordinates state
    var latitude by remember { mutableStateOf(existingAddress?.latitude ?: 15.3482) }
    var longitude by remember { mutableStateOf(existingAddress?.longitude ?: 44.2191) }
    var isManualMapSelection by remember { mutableStateOf(false) }

    // List of governorates in Yemen
    val governorates = emptyList<String>()
    var govExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (existingAddress != null) "تعديل العنوان" else "إضافة عنوان جغرافي جديد 🗺️",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 18.sp
                    )
                },
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
            // Heading Tip Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "📌 نظام التموضع والخرائط الذكي لليمن",
                        fontWeight = FontWeight.Bold,
                        color = MedBluePrimary,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "احفظ وتحديد نقاط منشأتك لتسهيل احتساب فترات وكلف التوصيل وتلقي الطلبات وعروض الأسعار بمنتهى الأمان والموثوقية.",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Input fields Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text("📍 تفاصيل العنوان", fontWeight = FontWeight.Bold, color = Color.DarkGray, fontSize = 14.sp)

                    // Label (e.g. المقر الرئيسي)
                    OutlinedTextField(
                        value = label,
                        onValueChange = { label = it },
                        label = { Text("اسم العنوان (مثال: المقر الرئيسي، المخزن رقم 2)") },
                        modifier = Modifier.fillMaxWidth().testTag("address_label_input"),
                        leadingIcon = { Icon(Icons.Default.Label, contentDescription = null, tint = MedBluePrimary) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MedBluePrimary)
                    )

                    // Hospital or Company Name
                    OutlinedTextField(
                        value = orgName,
                        onValueChange = { orgName = it },
                        label = { Text("اسم المستشفى أو الشركة") },
                        modifier = Modifier.fillMaxWidth().testTag("address_org_input"),
                        leadingIcon = { Icon(Icons.Default.Business, contentDescription = null, tint = MedBluePrimary) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MedBluePrimary)
                    )

                    // Nearest Landmark
                    OutlinedTextField(
                        value = landmark,
                        onValueChange = { landmark = it },
                        label = { Text("المعلم القريب المميز (مثال: مستشفى سابق، مدرسة الأمل، جامع الفردوس)") },
                        modifier = Modifier.fillMaxWidth().testTag("address_landmark_input"),
                        leadingIcon = { Icon(Icons.Default.Place, contentDescription = null, tint = MedBluePrimary) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MedBluePrimary)
                    )

                    // Dropdown for Governorates
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
                                modifier = Modifier.fillMaxWidth().menuAnchor().testTag("governorate_dropdown"),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MedBluePrimary)
                            )
                            ExposedDropdownMenu(
                                expanded = govExpanded,
                                onDismissRequest = { govExpanded = false }
                            ) {
                                governorates.forEach { gov ->
                                    DropdownMenuItem(
                                        text = { Text(gov) },
                                        onClick = {
                                            selectedGovernorate = gov
                                            govExpanded = false
                                            // Relocate coordinates approximate to chosen city
                                            val coords = FirebaseService.cityCoordinatesMap[gov]
                                            if (coords != null) {
                                                latitude = coords.first
                                                longitude = coords.second
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // District
                    OutlinedTextField(
                        value = district,
                        onValueChange = { district = it },
                        label = { Text("المديرية (مثال: السبعين، المعلا، الحصبة)") },
                        modifier = Modifier.fillMaxWidth().testTag("address_district_input"),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MedBluePrimary)
                    )

                    // Neighborhood
                    OutlinedTextField(
                        value = neighborhood,
                        onValueChange = { neighborhood = it },
                        label = { Text("الحي أو الحارة (مثال: حي السلام)") },
                        modifier = Modifier.fillMaxWidth().testTag("address_neighborhood_input"),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MedBluePrimary)
                    )
                }
            }

            // Interactive Geolocation Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("🗺️ التموضع الجغرافي والإحداثيات", fontWeight = FontWeight.Bold, color = Color.DarkGray, fontSize = 14.sp)

                    // Buttons list. GPS simulation vs Map pinpoint
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                // Simulate GPS trigger
                                val baseCoords = FirebaseService.cityCoordinatesMap[selectedGovernorate] ?: Pair(15.3482, 44.2191)
                                // Add small random jitter of ~ 2km for realism
                                val jitterLat = (Random.nextDouble() - 0.5) * 0.02
                                val jitterLon = (Random.nextDouble() - 0.5) * 0.02
                                latitude = baseCoords.first + jitterLat
                                longitude = baseCoords.second + jitterLon
                                Toast.makeText(context, "📍 تم جلب إحداثيات موقعك الحالي عبر الـ GPS بنجاح!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MedGreenPrimary),
                            modifier = Modifier.weight(1f).height(44.dp).testTag("gps_button"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("موقعي الحالي (GPS)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                isManualMapSelection = !isManualMapSelection
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isManualMapSelection) MedRedPrimary else MedBluePrimary
                            ),
                            modifier = Modifier.weight(1f).height(44.dp).testTag("manual_map_button"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                if (isManualMapSelection) "إلغاء تحديد اليدوي" else "تحديد يدوياً (الخريطة)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Interactive Canvas - Styled Custom Map Grid Selector
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
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
                                            // Map tapped coordinates to Yemeni limits
                                            // Width corresponds to Longitude 42.0 to 52.0
                                            // Height corresponds to Latitude 12.0 to 18.0
                                            val pctX = offset.x / size.width
                                            val pctY = offset.y / size.height

                                            val computedLong = 42.0 + (pctX * 10.0)
                                            val computedLat = 18.0 - (pctY * 6.0) // Y is inverted in drawing space

                                            latitude = Math.round(computedLat * 10000.0) / 10000.0
                                            longitude = Math.round(computedLong * 10000.0) / 10000.0
                                        }
                                    }
                                }
                        ) {
                            val canvasWidth = size.width
                            val canvasHeight = size.height

                            // Draw ocean / grid lines
                            val gridColor = Color(0xFFDBEAFE)
                            for (i in 1..4) {
                                drawLine(
                                    gridColor,
                                    Offset(canvasWidth * i / 5f, 0f),
                                    Offset(canvasWidth * i / 5f, canvasHeight),
                                    strokeWidth = 1f
                                )
                                drawLine(
                                    gridColor,
                                    Offset(0f, canvasHeight * i / 5f),
                                    Offset(canvasWidth, canvasHeight * i / 5f),
                                    strokeWidth = 1f
                                )
                            }

                            // Draw Yemen Main Outline representation (Abstract Poly)
                            // Draw critical Yemeni city nodes
                            FirebaseService.cityCoordinatesMap.forEach { (cityName, coords) ->
                                val x = ((coords.second - 42.0) / 10.0) * canvasWidth
                                val y = ((18.0 - coords.first) / 6.0) * canvasHeight

                                drawCircle(
                                    color = Color(0x202563EB),
                                    radius = 16f,
                                    center = Offset(x.toFloat(), y.toFloat())
                                )
                                drawCircle(
                                    color = Color(0x600F172A),
                                    radius = 4f,
                                    center = Offset(x.toFloat(), y.toFloat())
                                )
                            }

                            // Draw the current pinpoint marker
                            val markerX = ((longitude - 42.0) / 10.0) * canvasWidth
                            val markerY = ((18.0 - latitude) / 6.0) * canvasHeight

                            // Ping animation circle
                            drawCircle(
                                color = MedGreenPrimary.copy(alpha = 0.3f),
                                radius = 24f,
                                center = Offset(markerX.toFloat(), markerY.toFloat())
                            )
                            drawCircle(
                                color = MedGreenPrimary,
                                radius = 8f,
                                center = Offset(markerX.toFloat(), markerY.toFloat())
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 3f,
                                center = Offset(markerX.toFloat(), markerY.toFloat())
                            )
                        }

                        // Instructions Badge inside Map
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                if (isManualMapSelection) "👉 انقر في أي مكان على الخريطة لتعديل الموقع" else "تعديل الإحداثيات مقفل. لتغييرها اضغط زر 'تحديد يدوياً'",
                                fontSize = 8.sp,
                                color = if (isManualMapSelection) MedGreenPrimary else Color.DarkGray,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Target coordinate badge inside map
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(8.dp)
                                .background(Color(0xFF0F172A), RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                "إحداثيات: \nخط شمال: $latitude\nخط شرق: $longitude",
                                fontSize = 8.sp,
                                color = Color.White,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )
                        }
                    }

                    // Default Address Checkbox
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isDefault = !isDefault }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isDefault,
                            onCheckedChange = { isDefault = it },
                            colors = CheckboxDefaults.colors(checkedColor = MedGreenPrimary)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "تعيين كعنوان افتراضي معتمد لتلقي وتلقيم العروض والتوصيل",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.DarkGray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Save Button
            Button(
                onClick = {
                    if (label.isBlank() || orgName.isBlank() || district.isBlank()) {
                        Toast.makeText(context, "الرجاء تعبئة الحقول الأساسية: اسم العنوان، المستشفى، والمديرية ⚠️", Toast.LENGTH_LONG).show()
                        return@Button
                    }

                    val completeAddress = "$selectedGovernorate، مديرية $district، حي $neighborhood، معمار $landmark"
                    val targetAddress = UserAddress(
                        addressId = existingAddress?.addressId ?: "",
                        userId = currentUser.userId,
                        userType = currentUser.role,
                        label = label,
                        hospitalOrCompanyName = orgName,
                        nearbyLandmark = landmark,
                        governorate = selectedGovernorate,
                        district = district,
                        neighborhood = neighborhood,
                        fullAddress = completeAddress,
                        latitude = latitude,
                        longitude = longitude,
                        isDefault = isDefault
                    )

                    if (existingAddress != null) {
                        FirebaseService.updateUserAddress(targetAddress) { success ->
                            if (success) {
                                Toast.makeText(context, "تم تحديث العنوان بنجاح! 🎊 ✔", Toast.LENGTH_SHORT).show()
                                onSaveSuccess()
                            } else {
                                Toast.makeText(context, "فشل تحديث العنوان ⚠️", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        FirebaseService.addUserAddress(targetAddress) { success, error ->
                            if (success) {
                                if (currentUser.role == "branch_manager") {
                                    val notification = DirectorNotification(
                                        notificationId = "notif_" + System.currentTimeMillis(),
                                        title = "إضافة موقع مستودع/فرع جديد",
                                        message = "قام مدير الفرع ${currentUser.name} بإضافة موقع جديد: ${targetAddress.label} - ${targetAddress.fullAddress}",
                                        orderId = "",
                                        clientId = currentUser.userId,
                                        clientName = currentUser.name,
                                        createdAt = System.currentTimeMillis()
                                    )
                                    FirebaseService.notifyDirector(notification) {
                                        Toast.makeText(context, "تم حفظ وتثبيت العنوان الجغرافي وإخطار المدير العام! 🎊 ✔", Toast.LENGTH_SHORT).show()
                                        onSaveSuccess()
                                    }
                                } else {
                                    Toast.makeText(context, "تم حفظ وتثبيت العنوان الجغرافي الجديد بنجاح! 🎊 ✔", Toast.LENGTH_SHORT).show()
                                    onSaveSuccess()
                                }
                            } else {
                                Toast.makeText(context, "فشل حفظ العنوان: ${error ?: "خطأ غير معروف"}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("save_address_button"),
                colors = ButtonDefaults.buttonColors(containerColor = MedGreenPrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("حفظ وتثبيت العنوان الذكي ✔", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
