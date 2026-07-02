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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BankAccount
import com.example.model.BranchManagerProfile
import com.example.service.FirebaseService
import com.example.ui.theme.MedBluePrimary
import com.example.ui.theme.MedGreenPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BranchManagerSetupScreen(
    userId: String,
    onSetupCompleted: () -> Unit
) {
    val context = LocalContext.current
    var currentStep by remember { mutableStateOf(1) } // 1, 2, 3

    // Look up pre-assigned branch info for this manager
    val managerUser = FirebaseService.fallbackUsers.find { it.userId == userId }
    val assignedBranchId = managerUser?.branchId ?: "branch_sanaa"
    val assignedBranch = FirebaseService.fallbackBranches.find { it.branchId == assignedBranchId }

    // Step 1 States: Manager Details
    var fullName by remember { mutableStateOf(managerUser?.name ?: "") }
    var phone by remember { mutableStateOf(managerUser?.phone ?: "") }
    var isIdUploaded by remember { mutableStateOf(false) }

    // Step 2 States: Precise Warehouse Location (Prefilled from branch data)
    var warehouseLat by remember { mutableStateOf(assignedBranch?.latitude ?: 15.3482) }
    var warehouseLng by remember { mutableStateOf(assignedBranch?.longitude ?: 44.2191) }
    var warehouseAddressDesc by remember { mutableStateOf(assignedBranch?.address ?: "") }

    // Step 3 States: Bank Accounts Setup
    var bankName by remember { mutableStateOf("") }
    var accountNumber by remember { mutableStateOf("") }
    var accountHolderName by remember { mutableStateOf("") }
    var paymentWalletType by remember { mutableStateOf("bank") } // bank / mfs
    var addedAccountsList = remember { mutableStateListOf<BankAccount>() }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("إعداد ملف مدير الفرع 💼", fontWeight = FontWeight.Bold, color = Color.White) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MedBluePrimary, titleContentColor = Color.White)
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
            // STEP PROGRESS INDICATOR
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StepBubble(step = 1, activeStep = currentStep, label = "البيانات")
                StepConnector(active = currentStep >= 2)
                StepBubble(step = 2, activeStep = currentStep, label = "المستودع")
                StepConnector(active = currentStep >= 3)
                StepBubble(step = 3, activeStep = currentStep, label = "الحسابات")
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
                        ManagerStep1(
                            fullName = fullName,
                            onNameChange = { fullName = it },
                            phone = phone,
                            onPhoneChange = { phone = it },
                            isIdUploaded = isIdUploaded,
                            onUploadClick = { isIdUploaded = true }
                        )
                    }
                    2 -> {
                        ManagerStep2(
                            branchName = assignedBranch?.branchName ?: "فرع صنعاء الرئيسي",
                            governorate = assignedBranch?.governorate ?: "صنعاء",
                            city = assignedBranch?.city ?: "صنعاء",
                            warehouseAddressDesc = warehouseAddressDesc,
                            onAddressChange = { warehouseAddressDesc = it },
                            lat = warehouseLat,
                            lng = warehouseLng,
                            onFineTuneClick = {
                                // Simulate dragging/fine-tuning pin
                                warehouseLat += (Math.random() - 0.5) * 0.005
                                warehouseLng += (Math.random() - 0.5) * 0.005
                                Toast.makeText(context, "📍 تم ضبط موقع المستودع بدقة على الخريطة", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                    3 -> {
                        ManagerStep3(
                            bankName = bankName,
                            onBankNameChange = { bankName = it },
                            accountNumber = accountNumber,
                            onAccNumChange = { accountNumber = it },
                            accountHolderName = accountHolderName,
                            onHolderChange = { accountHolderName = it },
                            walletType = paymentWalletType,
                            onWalletTypeChange = { paymentWalletType = it },
                            addedAccounts = addedAccountsList,
                            onAddClick = {
                                if (bankName.isBlank() || accountNumber.isBlank() || accountHolderName.isBlank()) {
                                    Toast.makeText(context, "الرجاء تعبئة كافة حقول الحساب البنكي المعتمد", Toast.LENGTH_SHORT).show()
                                } else {
                                    val newAcc = BankAccount(
                                        accountId = "acc_" + System.currentTimeMillis(),
                                        userId = userId,
                                        bankName = bankName,
                                        accountNumber = accountNumber,
                                        accountHolderName = accountHolderName,
                                        walletType = paymentWalletType,
                                        walletNumber = if (paymentWalletType == "mfs") accountNumber else "",
                                        isDefault = addedAccountsList.isEmpty()
                                    )
                                    addedAccountsList.add(newAcc)
                                    // Save mock in database fallback too
                                    FirebaseService.saveBankAccount(newAcc, {}, {})
                                    
                                    // Clear fields
                                    bankName = ""
                                    accountNumber = ""
                                    accountHolderName = ""
                                    Toast.makeText(context, "✅ تم إضافة الحساب بنجاح", Toast.LENGTH_SHORT).show()
                                }
                            }
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
                            .testTag("manager_back_btn")
                    ) {
                        Text("السابق ➡️", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                if (currentStep < 3) {
                    Button(
                        onClick = {
                            if (currentStep == 1) {
                                if (fullName.isBlank() || phone.isBlank()) {
                                    Toast.makeText(context, "الرجاء تعبئة الاسم الكامل ورقم الهاتف", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                if (!isIdUploaded) {
                                    Toast.makeText(context, "يرجى النقر لرفع صورة الهوية الوطنية لتأكيد صفتك الوظيفية", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                            }
                            currentStep++
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MedGreenPrimary),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("manager_next_btn")
                    ) {
                        Text("التالي ⬅️", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = {
                            if (addedAccountsList.isEmpty()) {
                                Toast.makeText(context, "الرجاء إضافة حساب مالي واحد على الأقل لاستلام مدفوعات العملاء للفرع", Toast.LENGTH_LONG).show()
                                return@Button
                            }

                            val profile = BranchManagerProfile(
                                userId = userId,
                                fullName = fullName,
                                phone = phone,
                                nationalIdImageUrl = "https://images.unsplash.com/photo-1580489944761-15a19d654956",
                                warehouseLat = warehouseLat,
                                warehouseLng = warehouseLng,
                                bankAccounts = addedAccountsList.toList(),
                                profileCompleted = true,
                                joinedAt = System.currentTimeMillis()
                            )

                            FirebaseService.setupBranchManagerProfile(profile, {
                                Toast.makeText(context, "🎉 مرحباً بك يا مدير الفرع! تم حفظ بياناتك وتجهيز حسابات الاستقبال الدوائية.", Toast.LENGTH_LONG).show()
                                onSetupCompleted()
                            }, {
                                Toast.makeText(context, "فشل حفظ الملف الشخصي: $it", Toast.LENGTH_SHORT).show()
                            })
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MedGreenPrimary),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("manager_finish_btn")
                    ) {
                        Text("حفظ وإنهاء 🚀", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ManagerStep1(
    fullName: String,
    onNameChange: (String) -> Unit,
    phone: String,
    onPhoneChange: (String) -> Unit,
    isIdUploaded: Boolean,
    onUploadClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                "معلومات مدير الفرع الشخصية 👤",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                "هذه البيانات ضرورية لتفويضك بإدارة مخزون الفرع وتوقيع الفواتير الطبية والرد على المستشفيات.",
                color = Color.LightGray,
                fontSize = 11.sp,
                textAlign = TextAlign.Right,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        item {
            OutlinedTextField(
                value = fullName,
                onValueChange = onNameChange,
                label = { Text("الاسم الكامل لمدير الفرع") },
                placeholder = { Text("مثال: أحمد محمد الحميدي") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("setup_manager_name"),
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
                label = { Text("رقم الهاتف المحمول الوظيفي") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("setup_manager_phone"),
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(2.dp, if (isIdUploaded) MedGreenPrimary else Color.Gray, RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.04f))
                    .clickable { onUploadClick() }
                    .testTag("setup_manager_upload_id"),
                contentAlignment = Alignment.Center
            ) {
                if (isIdUploaded) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.CardMembership, contentDescription = null, tint = MedGreenPrimary, modifier = Modifier.size(36.dp))
                        Column {
                            Text("✅ تم تحميل بطاقتك الشخصية / العائلية", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("انقر هنا للتعديل أو الاستبدال", color = Color.LightGray, fontSize = 10.sp)
                        }
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("📷 ارفع صورة بطاقة الهوية الشخصية أو جواز السفر", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("مطلوب للتحقق الإداري والأمني لمدراء الفروع", color = Color.Gray, fontSize = 9.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun ManagerStep2(
    branchName: String,
    governorate: String,
    city: String,
    warehouseAddressDesc: String,
    onAddressChange: (String) -> Unit,
    lat: Double,
    lng: Double,
    onFineTuneClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                "تحديد موقع مستودع الفرع 📍",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                "قم بضبط وتأكيد الموقع الدقيق لمخازن ومستودعات الفرع لاستخدامها في حساب المسافات وأسعار الشحن المبرد.",
                color = Color.LightGray,
                fontSize = 11.sp,
                textAlign = TextAlign.Right,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MedGreenPrimary.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = MedGreenPrimary)
                    Column {
                        Text("اسم الفرع المعتمد: $branchName", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("المحافظة المخصصة: $governorate | المدينة: $city", color = Color.LightGray, fontSize = 11.sp)
                    }
                }
            }
        }

        item {
            OutlinedTextField(
                value = warehouseAddressDesc,
                onValueChange = onAddressChange,
                label = { Text("الوصف التفصيلي لموقع المستودع") },
                placeholder = { Text("شارع الجمهورية - خلف مخازن الأدوية المركزية للشركة") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = MedGreenPrimary,
                    unfocusedBorderColor = Color.Gray
                )
            )
        }

        // INTERACTIVE MAP FOR WAREHOUSE
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .border(1.dp, Color.Gray, RoundedCornerShape(12.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("خريطة موقع الفرع الرئيسي 🗺️", color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.StoreMallDirectory, contentDescription = null, tint = MedGreenPrimary, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("إحداثيات المستودع المحددة:", color = Color.White, fontSize = 10.sp)
                            Text("Lat: ${String.format("%.4f", lat)}, Lng: ${String.format("%.4f", lng)}", color = MedGreenPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Button(
                        onClick = onFineTuneClick,
                        colors = ButtonDefaults.buttonColors(containerColor = MedGreenPrimary),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(32.dp)
                            .testTag("warehouse_fine_tune_btn"),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("📍 ضبط وإزاحة دقيقة لمكان المستودع", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ManagerStep3(
    bankName: String,
    onBankNameChange: (String) -> Unit,
    accountNumber: String,
    onAccNumChange: (String) -> Unit,
    accountHolderName: String,
    onHolderChange: (String) -> Unit,
    walletType: String,
    onWalletTypeChange: (String) -> Unit,
    addedAccounts: List<BankAccount>,
    onAddClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                "الحسابات البنكية المعتمدة للفرع 💳",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                "أدخل الحسابات المالية (البنوك، الصرافين، محافظ كاش) للفرع التي يستطيع العملاء تسديد مستحقات الأدوية إليها مباشرة.",
                color = Color.LightGray,
                fontSize = 11.sp,
                textAlign = TextAlign.Right,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        // Account Type Selector
        item {
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
                        .background(if (walletType == "bank") MedBluePrimary else Color.Transparent)
                        .clickable { onWalletTypeChange("bank") }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🏦 حساب بنكي", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (walletType == "mfs") MedBluePrimary else Color.Transparent)
                        .clickable { onWalletTypeChange("mfs") }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("📱 محفظة جوال / صراف", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            OutlinedTextField(
                value = bankName,
                onValueChange = onBankNameChange,
                label = { Text(if (walletType == "bank") "اسم البنك" else "اسم محفظة الكاش / شركة الصرافة") },
                placeholder = { Text(if (walletType == "bank") "مثال: بنك اليمن والخليج" else "مثال: محفظة MTN كاش أو الكريمي") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("manager_bank_name_input"),
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
                value = accountNumber,
                onValueChange = onAccNumChange,
                label = { Text(if (walletType == "bank") "رقم الحساب الجاري" else "رقم الحساب / رقم الموبايل للمحفظة") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("manager_account_number_input"),
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
                value = accountHolderName,
                onValueChange = onHolderChange,
                label = { Text("اسم المستفيد الكامل للحساب") },
                placeholder = { Text("مثال: شركة الشفاء للأدوية - فرع صنعاء") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("manager_holder_name_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = MedGreenPrimary,
                    unfocusedBorderColor = Color.Gray
                ),
                singleLine = true
            )
        }

        // Add Account Button
        item {
            Button(
                onClick = onAddClick,
                colors = ButtonDefaults.buttonColors(containerColor = MedGreenPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("manager_add_account_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(6.dp))
                Text("إضافة الحساب المالي للقائمة ➕", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        // Header for added list
        if (addedAccounts.isNotEmpty()) {
            item {
                Text("الحسابات المالية المضافة حالياً للفرع:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }

            items(addedAccounts.size) { index ->
                val acc = addedAccounts[index]
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (acc.walletType == "bank") Icons.Default.AccountBalance else Icons.Default.PhoneAndroid,
                            contentDescription = null,
                            tint = MedGreenPrimary
                        )

                        Column(modifier = Modifier.weight(1f).padding(horizontal = 8.dp), horizontalAlignment = Alignment.End) {
                            Text(acc.bankName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("الرقم: ${acc.accountNumber}", color = Color.LightGray, fontSize = 11.sp)
                            Text("المستفيد: ${acc.accountHolderName}", color = Color.Gray, fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}
