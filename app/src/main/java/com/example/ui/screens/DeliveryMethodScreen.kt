package com.example.ui.screens

import android.widget.Toast
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
import com.example.model.PriceOffer
import com.example.model.User
import com.example.model.UserAddress
import com.example.service.FirebaseService
import com.example.ui.theme.MedBluePrimary
import com.example.ui.theme.MedGreenPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryMethodScreen(
    currentUser: User,
    priceOffer: PriceOffer,
    onBackClick: () -> Unit,
    onSelfDeliverSelect: (UserAddress, Double, String) -> Unit, // address, distance, eta
    onPlatformDeliverSelect: (UserAddress, Double, String, Double) -> Unit // address, distance, eta, estimatedPrice
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var userAddresses by remember { mutableStateOf<List<UserAddress>>(emptyList()) }
    var selectedAddress by remember { mutableStateOf<UserAddress?>(null) }
    var isLoadingAddresses by remember { mutableStateOf(false) }

    // Selected choice
    var selectedMethod by remember { mutableStateOf("") } // "self" or "platform"

    // Load available addresses for current hospital/user
    fun loadAddresses() {
        isLoadingAddresses = true
        FirebaseService.getUserAddresses(currentUser.userId) { list ->
            userAddresses = list
            selectedAddress = list.find { it.isDefault } ?: list.firstOrNull()
            isLoadingAddresses = false
        }
    }

    LaunchedEffect(Unit) {
        loadAddresses()
    }

    // Supplier coordinates - fetched from DB in the LaunchedEffect
    var supplierLat by remember { mutableStateOf(15.3482) }
    var supplierLng by remember { mutableStateOf(44.2191) }

    // Calculations based on chosen address
    val distance = remember(selectedAddress) {
        if (selectedAddress != null) {
            FirebaseService.calculateDistanceKm(
                supplierLat, supplierLng,
                selectedAddress!!.latitude, selectedAddress!!.longitude
            )
        } else {
            0.0 // default
        }
    }

    // Time estimate based on distance (Yemeni roads: 45 km/h average)
    val eta = remember(distance) {
        val totalHours = distance / 42.0
        val days = (totalHours / 24).toInt()
        val hours = (totalHours % 24).toInt()
        val minutes = ((totalHours - totalHours.toInt()) * 60).toInt()

        if (days > 0) {
            "$days يوم و $hours ساعة"
        } else if (hours > 0) {
            "$hours ساعة و $minutes دقيقة"
        } else {
            "${maxOf(15, minutes)} دقيقة"
        }
    }

    // Estimated Price for Platform Delivery (YER 500 per kilometer with base of YER 2000)
    val platformEstimatedPrice = remember(distance) {
        Math.round((2000.0 + (distance * 600.0)) * 10.0) / 10.0
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تحديد طريقة الشحن والتوصيل 🚚", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp) },
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
            // Price offer brief header
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "الدواء: ${priceOffer.medicineName}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MedBluePrimary
                        )
                        Text(
                            text = "المورد: ${priceOffer.supplierName}",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                    Text(
                        text = "${priceOffer.price * priceOffer.quantity} $",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MedGreenPrimary
                    )
                }
            }

            // Step 1: Destination Address Choose
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("📍 اختر عنوان التوصيل المعتمد", fontWeight = FontWeight.Bold, color = Color.DarkGray, fontSize = 14.sp)
                        TextButton(onClick = {
                            // Let the user know they can add it via account settings easily
                            Toast.makeText(context, "الرجاء مراجعة قسم حسابي لإضافة عناوين جديدة 🗺️", Toast.LENGTH_LONG).show()
                        }) {
                            Text("+ عنوان جديد", color = MedBluePrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }

                    if (isLoadingAddresses) {
                        CircularProgressIndicator(color = MedBluePrimary, modifier = Modifier.size(24.dp).align(Alignment.CenterHorizontally))
                    } else if (userAddresses.isEmpty()) {
                        Text(
                            text = "لا توجد أي عناوين مسجلة لجهة حسابك حتى الآن.\nيمكنك إضافة عنوان سريع لغرض الشحن.",
                            fontSize = 11.sp,
                            color = Color.Red,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)
                        )
                        // Fast generate address backup button
                        Button(
                            onClick = {
                                val quickAddr = UserAddress(
                                    addressId = "addr_quick_1",
                                    userId = currentUser.userId,
                                    userType = currentUser.role,
                                    label = "المقر المالي الحالي",
                                    hospitalOrCompanyName = currentUser.orgName,
                                    nearbyLandmark = "مبنى قريب",
                                    governorate = "صنعاء",
                                    district = "السبعين",
                                    neighborhood = "حي الأصبحي",
                                    fullAddress = "صنعاء، مديرية السبعين، حي الأصبحي",
                                    latitude = 15.3482,
                                    longitude = 44.2191,
                                    isDefault = true
                                )
                                FirebaseService.saveAddress(quickAddr, {
                                    loadAddresses()
                                }, {})
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary, contentColor = Color.White),
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("توليد عنوان تلقائي سريع 🗺️", fontSize = 11.sp)
                        }
                    } else {
                        // Display Addresses list
                        userAddresses.forEach { addr ->
                            val isSelected = selectedAddress?.addressId == addr.addressId
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (isSelected) MedBluePrimary.copy(alpha = 0.08f) else Color(0xFFF1F5F9),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { selectedAddress = addr }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { selectedAddress = addr },
                                        colors = RadioButtonDefaults.colors(selectedColor = MedBluePrimary)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Column {
                                        Text(addr.label, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(addr.fullAddress, fontSize = 10.sp, color = Color.Gray)
                                    }
                                }
                                if (addr.isDefault) {
                                    Box(
                                        modifier = Modifier
                                            .background(MedGreenPrimary.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("رئيسي", color = MedGreenPrimary, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }
            }

            // Calculations Preview
            if (selectedAddress != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("المسافة المقدرة", fontSize = 10.sp, color = Color.Gray)
                            Text("📍 $distance كم", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MedBluePrimary)
                        }
                        Divider(modifier = Modifier.width(1.dp).height(30.dp), color = Color.LightGray)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("فترة النقل التقريبية", fontSize = 10.sp, color = Color.Gray)
                            Text("⏱️ $eta", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MedBluePrimary)
                        }
                    }
                }
            }

            // Step 2: Choose Delivery Option
            Text(
                "🚚 خيارات وتكلفة التوصيل والشحن",
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray,
                fontSize = 14.sp
            )

            // Self Delivery Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedMethod == "self") Color(0xFFF0FDF4) else Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        2.dp,
                        if (selectedMethod == "self") MedGreenPrimary else Color.Transparent,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable { selectedMethod = "self" }
                    .testTag("self_delivery_choice")
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🚚", fontSize = 32.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "سأوصل جغرافياً بنفسي (شحن المورد الخاص)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MedBluePrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "المورد يباشر بتفويض الشاحنات والتسليم حسب كلفته الملحقة في العرض بشكل مباشر.",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                    RadioButton(
                        selected = selectedMethod == "self",
                        onClick = { selectedMethod = "self" },
                        colors = RadioButtonDefaults.colors(selectedColor = MedGreenPrimary)
                    )
                }
            }

            // Platform Delivery Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedMethod == "platform") Color(0xFFF0FDF4) else Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        2.dp,
                        if (selectedMethod == "platform") MedGreenPrimary else Color.Transparent,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable { selectedMethod = "platform" }
                    .testTag("platform_delivery_choice")
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("📦", fontSize = 32.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "توصيل عبر المنصة (الوساطة الدوائية لـ MedLink)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MedBluePrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "تتولى منصة MedLink فرز الشحنة مبردة ونقلها بضمان كفاءة وصحة الدواء والمسؤولة عن سلامته.",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                        if (selectedAddress != null) {
                            Text(
                                "تكلفة التوصيل المبرر التقريبية: YER $platformEstimatedPrice",
                                fontWeight = FontWeight.Bold,
                                color = MedGreenPrimary,
                                fontSize = 11.sp
                            )
                        }
                    }
                    RadioButton(
                        selected = selectedMethod == "platform",
                        onClick = { selectedMethod = "platform" },
                        colors = RadioButtonDefaults.colors(selectedColor = MedGreenPrimary)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Confirm Button
            Button(
                onClick = {
                    val addr = selectedAddress
                    if (addr == null) {
                        Toast.makeText(context, "الرجاء تحديد عنوان وجهة الشحن أولاً ⚠️", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (selectedMethod.isEmpty()) {
                        Toast.makeText(context, "الرجاء اختيار طريقة الشحن (🚚 أو 📦) ⚠️", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    if (selectedMethod == "self") {
                        onSelfDeliverSelect(addr, distance, eta)
                    } else {
                        onPlatformDeliverSelect(addr, distance, eta, platformEstimatedPrice)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("confirm_delivery_method_button"),
                colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary, contentColor = Color.White),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("المتابعة وحفظ خيار الشحن ➡️", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
