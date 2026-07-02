package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.model.BankAccount
import com.example.model.User
import com.example.service.FirebaseService
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BankAccountsScreen(
    currentUser: User,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current

    var accountsList by remember { mutableStateOf<List<BankAccount>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    // Dialog triggering states
    var showAddDialog by remember { mutableStateOf(false) }
    var dialogType by remember { mutableStateOf("bank") } // "bank" or "mfs" (mobile)

    // Form inputs
    var bankName by remember { mutableStateOf("") }
    var accountNumber by remember { mutableStateOf("") }
    var accountHolderName by remember { mutableStateOf("") }
    var walletNumber by remember { mutableStateOf("") }
    var isDefault by remember { mutableStateOf(false) }

    val presetBanks = listOf(
        "البنك الكريمي للتمويل الأصغر الإسلامي",
        "بنك اليمن والخليج",
        "البنك الأهلي اليمني",
        "بنك المأمون"
    )
    val presetWallets = listOf(
        "محفظة كاش - يمن موبايل",
        "محفظة MTN كاش"
    )

    fun loadAccounts() {
        isLoading = true
        FirebaseService.getBankAccounts(currentUser.userId) { list ->
            accountsList = list
            isLoading = false
        }
    }

    LaunchedEffect(currentUser.userId) {
        loadAccounts()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("قائمة الحسابات البنكية ومحافظ الاستلام 🏦", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp) },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Dashboard Heading Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MedBluePrimary.copy(alpha = 0.05f), contentColor = OnSurfaceDark),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        "🏦 إعدادات الحساب والمحفظة",
                        fontWeight = FontWeight.Bold,
                        color = MedBluePrimary,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "أضف حساباتك البنكية أو محافظ الكاش الالكترونية المعتمدة في اليمن ليتم إظهارها تلقائياً للمستشفيات عند قبول عروضك والبدء بالسداد المالي المباشر.",
                        fontSize = 11.sp,
                        color = Color.DarkGray
                    )
                }
            }

            // Quick Actions Box
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        dialogType = "bank"
                        bankName = presetBanks[0]
                        accountNumber = ""
                        accountHolderName = currentUser.orgName
                        walletNumber = ""
                        isDefault = false
                        showAddDialog = true
                    },
                    modifier = Modifier.weight(1.5f).height(46.dp).testTag("add_bank_option_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary, contentColor = Color.White),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("إضافة بنك 🏦", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        dialogType = "mfs"
                        bankName = presetWallets[0]
                        accountNumber = ""
                        accountHolderName = currentUser.orgName
                        walletNumber = ""
                        isDefault = false
                        showAddDialog = true
                    },
                    modifier = Modifier.weight(1.5f).height(46.dp).testTag("add_wallet_option_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = MedGreenPrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.PhoneAndroid, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("إضافة محفظة 📱", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Accounts List display
            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MedBluePrimary)
                }
            } else if (accountsList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.CreditCard, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                        Text(
                            "لا توجد أي حسابات سداد مسجلة حتى الآن.",
                            color = Color.Gray,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "اضغط على الأزرار في الأعلى لإضافة حسابك البنكي أو محفظتك لتلقي دفعات المستشفيات.",
                            color = Color.LightGray,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(accountsList) { account ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth().clickable {
                                if (!account.isDefault) {
                                    FirebaseService.setDefaultBankAccount(currentUser.userId, account.accountId, {
                                        loadAccounts()
                                        Toast.makeText(context, "تم تعيين الحساب كافتراضي لاستلام الدفعات ✔", Toast.LENGTH_SHORT).show()
                                    }, {})
                                }
                            }
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(
                                                    if (account.walletType == "bank") Color(0xFFEFF6FF) else Color(0xFFFEF9C3),
                                                    RoundedCornerShape(6.dp)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (account.walletType == "bank") Icons.Default.AccountBalance else Icons.Default.PhoneAndroid,
                                                contentDescription = null,
                                                tint = if (account.walletType == "bank") MedBluePrimary else Color(0xFFCA8A04)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            account.bankName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = MedBluePrimary
                                        )
                                    }

                                    // Default tags
                                    if (account.isDefault) {
                                        Box(
                                            modifier = Modifier
                                                .background(MedGreenPrimary.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text("الحساب الافتراضي الرئيسي", color = MedGreenPrimary, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                        }
                                    } else {
                                        Text("تعيين كافتراضي", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        if (account.walletType == "bank") {
                                            Text("رقم الحساب الحوالة:", fontSize = 10.sp, color = Color.Gray)
                                            Text(account.accountNumber, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                                        } else {
                                            Text("رقم محفظة الجوال المعتمد:", fontSize = 10.sp, color = Color.Gray)
                                            Text(account.walletNumber, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                                        }
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("اسم صاحب الحساب / المستفيد:", fontSize = 10.sp, color = Color.Gray)
                                        Text(account.accountHolderName, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- Add Account Dialog ---
        if (showAddDialog) {
            var inputBankName by remember { mutableStateOf(bankName) }
            var inputAccountNumber by remember { mutableStateOf(accountNumber) }
            var inputHolderName by remember { mutableStateOf(accountHolderName) }
            var inputWalletNumber by remember { mutableStateOf(walletNumber) }
            var inputIsDefault by remember { mutableStateOf(isDefault) }

            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = {
                    Text(
                        if (dialogType == "bank") "إضافة حساب بنكي جديد 🏦" else "إضافة محفظة إلكترونية كاش 📱",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Presets list selection
                        if (dialogType == "bank") {
                            Text("اختر المؤسسة المصرفية:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                presetBanks.take(2).forEach { bank ->
                                    Box(
                                        modifier = Modifier
                                            .border(1.dp, if (inputBankName == bank) MedBluePrimary else Color.LightGray, RoundedCornerShape(10.dp))
                                            .background(if (inputBankName == bank) MedBluePrimary.copy(alpha = 0.08f) else Color.Transparent, RoundedCornerShape(10.dp))
                                            .clickable { inputBankName = bank }
                                            .padding(6.dp)
                                    ) {
                                        Text(bank.replace("البنك ", "").take(8) + "..", fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = inputBankName,
                                onValueChange = { inputBankName = it },
                                label = { Text("اسم البنك") },
                                modifier = Modifier.fillMaxWidth().testTag("add_bank_name_input"),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MedBluePrimary)
                            )

                            OutlinedTextField(
                                value = inputAccountNumber,
                                onValueChange = { inputAccountNumber = it },
                                label = { Text("رقم الحساب البنكي") },
                                modifier = Modifier.fillMaxWidth().testTag("add_bank_account_number"),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MedBluePrimary)
                            )
                        } else {
                            Text("اختر مزود الخدمة الالكترونية:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                presetWallets.forEach { wall ->
                                    Box(
                                        modifier = Modifier
                                            .border(1.dp, if (inputBankName == wall) MedGreenPrimary else Color.LightGray, RoundedCornerShape(10.dp))
                                            .background(if (inputBankName == wall) MedGreenPrimary.copy(alpha = 0.08f) else Color.Transparent, RoundedCornerShape(10.dp))
                                            .clickable { inputBankName = wall }
                                            .padding(6.dp)
                                    ) {
                                        Text(wall.replace("محفظة ", ""), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = inputBankName,
                                onValueChange = { inputBankName = it },
                                label = { Text("نوع المحفظة الكاش") },
                                modifier = Modifier.fillMaxWidth().testTag("add_wallet_name_input"),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MedGreenPrimary)
                            )

                            OutlinedTextField(
                                value = inputWalletNumber,
                                onValueChange = { inputWalletNumber = it },
                                label = { Text("رقم جوال المحفظة الإلكترونية") },
                                modifier = Modifier.fillMaxWidth().testTag("add_wallet_number"),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MedGreenPrimary)
                            )
                        }

                        OutlinedTextField(
                            value = inputHolderName,
                            onValueChange = { inputHolderName = it },
                            label = { Text("اسم المستفيد / صاحب الحساب بالكامل") },
                            modifier = Modifier.fillMaxWidth().testTag("add_account_holder_name"),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MedBluePrimary)
                        )

                        // Set default toggle
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { inputIsDefault = !inputIsDefault }
                                .padding(vertical = 4.dp)
                        ) {
                            Checkbox(
                                checked = inputIsDefault,
                                onCheckedChange = { inputIsDefault = it },
                                colors = CheckboxDefaults.colors(checkedColor = MedGreenPrimary)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("تعيين كطريقة الاستلام الافتراضية للطلبات المنجزة", fontSize = 11.sp, color = Color.DarkGray)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (dialogType == "bank" && (inputBankName.isBlank() || inputAccountNumber.isBlank() || inputHolderName.isBlank())) {
                                Toast.makeText(context, "يرجى تعبئة الحقول الأساسية للحساب البنكي ⚠️", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (dialogType == "mfs" && (inputBankName.isBlank() || inputWalletNumber.isBlank() || inputHolderName.isBlank())) {
                                Toast.makeText(context, "يرجى تعبئة الحقول الأساسية للمحفظة الإلكترونية ⚠️", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val newAccount = BankAccount(
                                accountId = "",
                                userId = currentUser.userId,
                                bankName = inputBankName,
                                accountNumber = inputAccountNumber,
                                accountHolderName = inputHolderName,
                                walletType = dialogType,
                                walletNumber = inputWalletNumber,
                                isDefault = inputIsDefault
                            )

                            FirebaseService.saveBankAccount(newAccount, {
                                showAddDialog = false
                                loadAccounts()
                                Toast.makeText(context, "تم حفظ وسيلة استلام الدفع الجديدة بنجاح! 🎊", Toast.LENGTH_SHORT).show()
                            }, { err ->
                                Toast.makeText(context, "خطأ: $err", Toast.LENGTH_SHORT).show()
                            })
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MedGreenPrimary)
                    ) {
                        Text("إضافة وحفظ الحساب ✔", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text("إلغاء", color = Color.Gray)
                    }
                }
            )
        }
    }
}
