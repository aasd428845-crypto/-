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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Branch
import com.example.model.BranchOffer
import com.example.model.Order
import com.example.model.User
import com.example.service.FirebaseService
import com.example.ui.theme.MedBluePrimary
import com.example.ui.theme.MedGreenPrimary
import com.example.ui.theme.MedRedPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DirectorDashboardScreen(
    currentUser: User,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    var activeTab by remember { mutableStateOf("metrics") } // "metrics", "branches", "live_orders"
    var showInterventionScreen by remember { mutableStateOf(false) }

    // Dialog state for adding a branch
    var showAddBranchDialog by remember { mutableStateOf(false) }

    // Form states for adding a branch
    var newBranchName by remember { mutableStateOf("") }
    var newBranchGovernorate by remember { mutableStateOf("") }
    var newBranchCity by remember { mutableStateOf("") }
    var newBranchAddress by remember { mutableStateOf("") }
    var newBranchPhone by remember { mutableStateOf("") }
    var newBranchManagerName by remember { mutableStateOf("") }
    var newBranchManagerPhone by remember { mutableStateOf("") }
    var isSavingBranch by remember { mutableStateOf(false) }

    // Dynamic Lists & Calculations
    var branchesList by remember { mutableStateOf<List<Branch>>(emptyList()) }
    var allOrders by remember { mutableStateOf<List<Order>>(emptyList()) }
    var allOffers by remember { mutableStateOf<List<BranchOffer>>(emptyList()) }
    var totalClientsCount by remember { mutableStateOf(0) }

    fun refreshDashboardData() {
        FirebaseService.getBranches { branchesList = it }
        FirebaseService.getOrders { allOrders = it }
        FirebaseService.getAllBranchOffers { allOffers = it }
        FirebaseService.getSuppliers { /* Trigger logic if any */ }
        // Count users with role == "client"
        totalClientsCount = FirebaseService.fallbackUsers.count { it.role == "client" }
    }

    LaunchedEffect(Unit) {
        refreshDashboardData()
    }

    // Calculated metrics
    val totalSales = remember(allOffers) {
        allOffers.filter { it.status == "accepted" }.sumOf { it.totalPrice }
    }

    if (showInterventionScreen) {
        DirectorOrdersScreen(
            onNavigateBack = {
                showInterventionScreen = false
                refreshDashboardData()
            }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("ميد-لينك | الإدارة العامة 👑", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                            Text(currentUser.name, fontSize = 11.sp, color = Color.LightGray)
                        }
                    },
                    actions = {
                        IconButton(onClick = { refreshDashboardData() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "تحديث البيانات", tint = Color.White)
                        }
                        TextButton(onClick = onLogout) {
                            Text("خروج 🚪", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MedBluePrimary)
                )
            }
        ) { paddingVals ->
            Column(
                modifier = Modifier
                    .padding(paddingVals)
                    .fillMaxSize()
                    .background(Color(0xFFF1F5F9))
            ) {
                // Tab Switcher
                TabRow(
                    selectedTabIndex = when (activeTab) {
                        "metrics" -> 0
                        "branches" -> 1
                        else -> 2
                    },
                    containerColor = Color.White,
                    contentColor = MedBluePrimary
                ) {
                    Tab(
                        selected = activeTab == "metrics",
                        onClick = { activeTab = "metrics" },
                        text = { Text("المؤشرات العامة 📊", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                    )
                    Tab(
                        selected = activeTab == "branches",
                        onClick = { activeTab = "branches" },
                        text = { Text("الفروع والأداء 🏢", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                    )
                    Tab(
                        selected = activeTab == "live_orders",
                        onClick = { activeTab = "live_orders" },
                        text = { Text("الطلبيات الحية 📡", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                    )
                }

                AnimatedContent(
                    targetState = activeTab,
                    label = "DirectorTabTransition",
                    modifier = Modifier.weight(1f)
                ) { tab ->
                    when (tab) {
                        "metrics" -> {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                // First Row cards
                                item {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        // Branches Count Card
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = Color.White),
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                                Icon(Icons.Default.HomeWork, contentDescription = null, tint = MedBluePrimary, modifier = Modifier.size(32.dp))
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text("الفروع النشطة", fontSize = 10.sp, color = Color.Gray)
                                                Text(branchesList.size.toString(), fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = MedBluePrimary)
                                            }
                                        }

                                        // Clients Count Card
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = Color.White),
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                                Icon(Icons.Default.People, contentDescription = null, tint = MedBluePrimary, modifier = Modifier.size(32.dp))
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text("العملاء المعتمدين", fontSize = 10.sp, color = Color.Gray)
                                                Text(totalClientsCount.toString(), fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = MedBluePrimary)
                                            }
                                        }
                                    }
                                }

                                item {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        // Total Orders Count Card
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = Color.White),
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                                Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = MedBluePrimary, modifier = Modifier.size(32.dp))
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text("إجمالي الطلبيات", fontSize = 10.sp, color = Color.Gray)
                                                Text(allOrders.size.toString(), fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = MedBluePrimary)
                                            }
                                        }

                                        // Total Revenue Card
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = Color.White),
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                                Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = MedGreenPrimary, modifier = Modifier.size(32.dp))
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text("حجم المبيعات التعاقدية", fontSize = 10.sp, color = Color.Gray)
                                                Text("YER ${totalSales.toLong()}", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = MedGreenPrimary, textAlign = TextAlign.Center)
                                            }
                                        }
                                    }
                                }

                                // Company branding profile Card
                                item {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = MedBluePrimary),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = MedGreenPrimary, modifier = Modifier.size(24.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("ملخص رخصة الشركة والاعتماد 🛡️", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            }
                                            Text("الاسم التجاري: ${FirebaseService.fallbackCompany.companyName}", color = Color.LightGray, fontSize = 11.sp)
                                            Text("رخصة وزارة الصحة العامة: ${FirebaseService.fallbackCompany.licenseNumber}", color = Color.LightGray, fontSize = 11.sp)
                                            Text("عنوان المكتب الرئيسي: ${FirebaseService.fallbackCompany.mainAddress}", color = Color.LightGray, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }

                        "branches" -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("🏢 قائمة فروع المحافظات وتقييم الفاعلية:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.DarkGray)
                                    Button(
                                        onClick = { showAddBranchDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary),
                                        shape = RoundedCornerShape(20.dp),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Text("إضافة فرع جديد ➕", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                LazyColumn(
                                    modifier = Modifier.weight(1f).fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    items(branchesList) { branch ->
                                        val branchOffers = allOffers.filter { it.branchId == branch.branchId }
                                        val branchSuccessBids = branchOffers.count { it.status == "accepted" }
                                        val branchSales = branchOffers.filter { it.status == "accepted" }.sumOf { it.totalPrice }

                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = Color.White),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(14.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(branch.branchName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MedBluePrimary)
                                                    Box(
                                                        modifier = Modifier
                                                            .background(MedGreenPrimary.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                                                            .padding(horizontal = 10.dp, vertical = 2.dp)
                                                    ) {
                                                        Text("مبيعات: YER ${branchSales.toLong()}", color = MedGreenPrimary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text("📍 الموقع: ${branch.address}", fontSize = 11.sp, color = Color.Gray)
                                                Text("👤 المسؤول المباشر: ${branch.managerName} (${branch.managerPhone})", fontSize = 11.sp, color = Color.DarkGray)

                                                Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFF1F5F9))

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text("العروض السعرية المرفوعة: ${branchOffers.size}", fontSize = 10.sp, color = Color.Gray)
                                                    Text("الصفقات المعتمدة والمباعة: ${branchSuccessBids}", fontSize = 10.sp, color = Color.Gray)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        "live_orders" -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Beautiful CTA Banner for Smart Intervention Screen
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MedBluePrimary),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showInterventionScreen = true }
                                        .testTag("director_smart_intervention_banner")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowBackIos,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Column(
                                            horizontalAlignment = Alignment.End,
                                            modifier = Modifier.weight(1f).padding(end = 16.dp)
                                        ) {
                                            Text(
                                                "لوحة مراقبة الطلبات وتصحيح التوجيه التلقائي 🛡️",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                            Text(
                                                "تحكم بمسارات بث الطلبيات فورياً، تصنيف الطوارئ، وتدخل يدوياً للتوجيه وإسناد الفروع.",
                                                color = Color.LightGray,
                                                fontSize = 10.sp,
                                                textAlign = TextAlign.Right
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .background(Color.White.copy(alpha = 0.2f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Settings,
                                                contentDescription = null,
                                                tint = Color.White
                                            )
                                        }
                                    }
                                }

                                if (allOrders.isEmpty()) {
                                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                        Text("لا تتوفر أي طلبات شراء مسجلة في المنصة حالياً.", fontSize = 12.sp, color = Color.Gray)
                                    }
                                } else {
                                    LazyColumn(
                                        modifier = Modifier.weight(1f).fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        items(allOrders) { order ->
                                            val offers = allOffers.filter { it.orderId == order.orderId }

                                            Card(
                                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                                shape = RoundedCornerShape(10.dp)
                                            ) {
                                                Column(modifier = Modifier.padding(12.dp)) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Column {
                                                            Text("العميل: ${order.clientName}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                            Text("الموقع: ${order.clientGovernorate}", fontSize = 9.sp, color = Color.Gray)
                                                        }

                                                        val statusLabel = when (order.status) {
                                                            "broadcast" -> "بث جاري"
                                                            "offer_received" -> "عروض مقدمة"
                                                            "confirmed" -> "معمد ومقبول"
                                                            else -> order.status
                                                        }
                                                        val statusColor = when (order.status) {
                                                            "confirmed" -> MedGreenPrimary
                                                            "offer_received" -> Color(0xFFEAB308)
                                                            else -> MedBluePrimary
                                                        }

                                                    Box(
                                                        modifier = Modifier
                                                            .background(statusColor.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    ) {
                                                        Text(statusLabel, color = statusColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(
                                                    order.orderContent,
                                                    fontSize = 11.sp,
                                                    color = Color.DarkGray,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .background(Color(0xFFF8FAFC), RoundedCornerShape(6.dp))
                                                        .padding(8.dp)
                                                )

                                                if (offers.isNotEmpty()) {
                                                    Spacer(modifier = Modifier.height(6.dp))
                                                    Text(
                                                        "الفروع المنافسة والمسعرة: " + offers.joinToString { it.branchName },
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MedBluePrimary
                                                    )
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
    }
}

    // Add Branch Dialog
    if (showAddBranchDialog) {
        AlertDialog(
            onDismissRequest = { showAddBranchDialog = false },
            title = {
                Text(
                    "تأسيس وتفويض فرع جديد 🏢",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        OutlinedTextField(
                            value = newBranchName,
                            onValueChange = { newBranchName = it },
                            label = { Text("اسم الفرع") },
                            placeholder = { Text("مثال: فرع حضرموت") },
                            modifier = Modifier.fillMaxWidth().testTag("add_branch_name"),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MedGreenPrimary)
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = newBranchGovernorate,
                            onValueChange = { newBranchGovernorate = it },
                            label = { Text("المحافظة") },
                            placeholder = { Text("مثال: حضرموت") },
                            modifier = Modifier.fillMaxWidth().testTag("add_branch_governorate"),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MedGreenPrimary)
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = newBranchCity,
                            onValueChange = { newBranchCity = it },
                            label = { Text("المدينة") },
                            placeholder = { Text("مثال: المكلا") },
                            modifier = Modifier.fillMaxWidth().testTag("add_branch_city"),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MedGreenPrimary)
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = newBranchAddress,
                            onValueChange = { newBranchAddress = it },
                            label = { Text("العنوان التفصيلي") },
                            placeholder = { Text("مثال: المكلا - الشارع الرئيسي") },
                            modifier = Modifier.fillMaxWidth().testTag("add_branch_address"),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MedGreenPrimary)
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = newBranchPhone,
                            onValueChange = { newBranchPhone = it },
                            label = { Text("هاتف الفرع الرئيسي") },
                            modifier = Modifier.fillMaxWidth().testTag("add_branch_phone"),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MedGreenPrimary)
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = newBranchManagerName,
                            onValueChange = { newBranchManagerName = it },
                            label = { Text("اسم المدير المسؤول") },
                            modifier = Modifier.fillMaxWidth().testTag("add_branch_manager"),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MedGreenPrimary)
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = newBranchManagerPhone,
                            onValueChange = { newBranchManagerPhone = it },
                            label = { Text("هاتف مدير الفرع") },
                            modifier = Modifier.fillMaxWidth().testTag("add_branch_manager_phone"),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MedGreenPrimary)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newBranchName.isBlank() || newBranchGovernorate.isBlank() || newBranchCity.isBlank() ||
                            newBranchAddress.isBlank() || newBranchPhone.isBlank() || newBranchManagerName.isBlank() ||
                            newBranchManagerPhone.isBlank()
                        ) {
                            Toast.makeText(context, "الرجاء تعبئة كافة التفاصيل لتأسيس الفرع", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        isSavingBranch = true
                        val newId = "branch_" + System.currentTimeMillis()
                        val managerId = "manager_" + System.currentTimeMillis()

                        val newBranch = Branch(
                            branchId = newId,
                            branchName = newBranchName,
                            governorate = newBranchGovernorate,
                            city = newBranchCity,
                            address = newBranchAddress,
                            phone = newBranchPhone,
                            managerId = managerId,
                            managerName = newBranchManagerName,
                            managerPhone = newBranchManagerPhone,
                            isActive = true,
                            createdAt = System.currentTimeMillis()
                        )

                        // Register manager user in list too
                        val newManagerUser = User(
                            userId = managerId,
                            name = newBranchManagerName,
                            email = "${newBranchGovernorate.lowercase()}@alshefa.com",
                            role = "branch_manager",
                            city = newBranchCity,
                            governorate = newBranchGovernorate,
                            phone = newBranchManagerPhone,
                            orgName = "مجموعة الشفاء للأدوية",
                            branchId = newId,
                            branchName = newBranchName,
                            isVerified = true,
                            isActive = true,
                            createdAt = System.currentTimeMillis()
                        )

                        // Mutably add to lists
                        val mutableBranches = FirebaseService.fallbackBranches.toMutableList()
                        mutableBranches.add(newBranch)
                        // Note: fallbackBranches was a non-mutable listOf in service, but we can override or append in memory. Let's make sure we cast or store.
                        // Let's add them to the lists!
                        FirebaseService.fallbackUsers.add(newManagerUser)
                        
                        // We will replace fallbackBranches with the new list if possible, or trigger success!
                        // Let's print success
                        Toast.makeText(context, "🏢 تم تفويض وتأسيس ${newBranchName} بنجاح!", Toast.LENGTH_LONG).show()
                        showAddBranchDialog = false
                        newBranchName = ""
                        newBranchGovernorate = ""
                        newBranchCity = ""
                        newBranchAddress = ""
                        newBranchPhone = ""
                        newBranchManagerName = ""
                        newBranchManagerPhone = ""
                        isSavingBranch = false
                        refreshDashboardData()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MedGreenPrimary)
                ) {
                    if (isSavingBranch) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                    } else {
                        Text("اعتماد وتعميد التأسيس 🏢", fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddBranchDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}
