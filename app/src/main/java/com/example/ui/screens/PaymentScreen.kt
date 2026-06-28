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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Payment
import com.example.model.PriceOffer
import com.example.model.User
import com.example.model.BankAccount
import com.example.service.FirebaseService
import com.example.ui.theme.MedBluePrimary
import com.example.ui.theme.MedGreenPrimary
import com.example.ui.theme.MedRedPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    currentUser: User,
    priceOffer: PriceOffer,
    onBackClick: () -> Unit,
    onPaymentSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Load supplier's accounts
    var supplierBankAccounts by remember { mutableStateOf<List<BankAccount>>(emptyList()) }
    var selectedAccount by remember { mutableStateOf<BankAccount?>(null) }
    var isLoadingAccounts by remember { mutableStateOf(false) }

    // Inputs
    var transactionId by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var currencyChosen by remember { mutableStateOf("YER") } // YER or USD

    // File attachments state (Simulated)
    var attachedFileName by remember { mutableStateOf<String?>(null) }
    var attachedFileType by remember { mutableStateOf<String?>(null) } // "image" or "pdf"

    // Sound FCM notification visual dialog
    var showFcmNotificationSim by remember { mutableStateOf<String?>(null) }

    // Constants
    val totalInUSD = priceOffer.price * priceOffer.quantity + priceOffer.shippingCost
    // Standard exchange currency rate in Yemen (YER ~ 530 for Aden is different but let's assume standard YER 600 or YER 1500 for unified calculations)
    val conversionRate = 1600.0 // 1600 YER per Dollar
    val totalInYER = Math.round(totalInUSD * conversionRate * 10.0) / 10.0

    val activeAmountCalculated = if (currencyChosen == "YER") totalInYER else totalInUSD

    // Load supplier accounts
    LaunchedEffect(priceOffer.supplierId) {
        isLoadingAccounts = true
        FirebaseService.getBankAccounts(priceOffer.supplierId) { list ->
            supplierBankAccounts = list
            selectedAccount = list.find { it.isDefault } ?: list.firstOrNull()
            isLoadingAccounts = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("المستحقات وحوالة الدفع المباشر 💳", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MedBluePrimary)
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF8FAFC))
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Facture/Invoice summary Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("📄 تفاصيل الفاتورة الطبية", fontWeight = FontWeight.Bold, color = MedBluePrimary, fontSize = 14.sp)
                        Divider(color = Color(0xFFF1F5F9))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("الصنف الطبي:", fontSize = 12.sp, color = Color.Gray)
                            Text(priceOffer.medicineName, fontWeight = FontWeight.Medium, fontSize = 12.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("الكمية المطلوبة:", fontSize = 12.sp, color = Color.Gray)
                            Text("${priceOffer.quantity} وحدة", fontWeight = FontWeight.Medium, fontSize = 12.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("سعر الوحدة:", fontSize = 12.sp, color = Color.Gray)
                            Text("${priceOffer.price} $", fontWeight = FontWeight.Medium, fontSize = 12.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("كلفة الشحن والتوصيل:", fontSize = 12.sp, color = Color.Gray)
                            Text("${priceOffer.shippingCost} $", fontWeight = FontWeight.Medium, fontSize = 12.sp)
                        }
                        Divider(color = Color(0xFFF1F5F9))

                        // Switch Currency YER / USD
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("العملة المفضلة للتسديد:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.DarkGray)
                            Row(
                                modifier = Modifier
                                    .background(Color(0xFFF1F5F9), RoundedCornerShape(20.dp))
                                    .padding(2.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (currencyChosen == "YER") MedBluePrimary else Color.Transparent,
                                            RoundedCornerShape(18.dp)
                                        )
                                        .clickable { currencyChosen = "YER" }
                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text("ريال يمني YER", color = if (currencyChosen == "YER") Color.White else Color.DarkGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (currencyChosen == "USD") MedBluePrimary else Color.Transparent,
                                            RoundedCornerShape(18.dp)
                                        )
                                        .clickable { currencyChosen = "USD" }
                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text("دولار أمريكي USD", color = if (currencyChosen == "USD") Color.White else Color.DarkGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Total sum display
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF0FDF4), RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("إجمالي قيمة الفاتورة المستحقة للتحويل:", fontWeight = FontWeight.Bold, color = MedGreenPrimary, fontSize = 12.sp)
                            Text(
                                text = if (currencyChosen == "YER") "$totalInYER ريال يمني" else "$totalInUSD دولار أمريكي",
                                fontWeight = FontWeight.ExtraBold,
                                color = MedGreenPrimary,
                                fontSize = 16.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                // Step 2: Supplier's Bank Accounts Display
                Text("🏦 معلومات حسابات المورد البنكية والالكترونية المعتمدة", fontWeight = FontWeight.Bold, color = Color.DarkGray, fontSize = 13.sp)

                if (isLoadingAccounts) {
                    CircularProgressIndicator(color = MedBluePrimary, modifier = Modifier.size(24.dp).align(Alignment.CenterHorizontally))
                } else if (supplierBankAccounts.isEmpty()) {
                    // Pre-fill some defaults if list is empty representing common yemeni providers
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFEF2F2), RoundedCornerShape(8.dp))
                            .border(1.dp, MedRedPrimary.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            "المورد لم يقم بإدراج أي حسابات بنكية مسبقاً في ملفه الشخصي بعد. يرجى إرشاده لإضافتها لضمان السداد.",
                            fontSize = 11.sp,
                            color = MedRedPrimary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    supplierBankAccounts.forEach { account ->
                        val isSelected = selectedAccount?.accountId == account.accountId
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0xFFEFF6FF) else Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    1.5.dp,
                                    if (isSelected) MedBlueAccent else Color.Transparent,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { selectedAccount = account }
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Dynamic logo icon based on type (Bank vs Mobile cash)
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(
                                            if (account.walletType == "bank") Color(0xFFE0F2FE) else Color(0xFFFEF9C3),
                                            RoundedCornerShape(8.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (account.walletType == "bank") Icons.Default.AccountBalance else Icons.Default.PhoneAndroid,
                                        contentDescription = null,
                                        tint = if (account.walletType == "bank") MedBlueAccent else Color(0xFFCA8A04)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = account.bankName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color.DarkGray
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    if (account.walletType == "bank") {
                                        Text("رقم الحساب: ${account.accountNumber}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MedBluePrimary)
                                        Text("المستفيد: ${account.accountHolderName}", fontSize = 10.sp, color = Color.Gray)
                                    } else {
                                        Text("رقم الجوال: ${account.walletNumber}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MedBluePrimary)
                                        Text("المستفيد: ${account.accountHolderName}", fontSize = 10.sp, color = Color.Gray)
                                    }
                                }

                                RadioButton(
                                    selected = isSelected,
                                    onClick = { selectedAccount = account },
                                    colors = RadioButtonDefaults.colors(selectedColor = MedBlueAccent)
                                )
                            }
                        }
                    }
                }

                // Step 3: Transaction proof inputs
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
                        Text("📤 معلومات وتأكيد الحوالة البنكية المباشرة", fontWeight = FontWeight.Bold, color = Color.DarkGray, fontSize = 13.sp)

                        // Operation reference number
                        OutlinedTextField(
                            value = transactionId,
                            onValueChange = { transactionId = it },
                            label = { Text("رقم العملية / الحوالة البنكية") },
                            modifier = Modifier.fillMaxWidth().testTag("transaction_id_input"),
                            leadingIcon = { Icon(Icons.Default.ConfirmationNumber, contentDescription = null, tint = MedBluePrimary) },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MedBluePrimary)
                        )

                        // Note
                        OutlinedTextField(
                            value = note,
                            onValueChange = { note = it },
                            label = { Text("ملاحظة إضافية للمورد") },
                            modifier = Modifier.fillMaxWidth().testTag("receipt_note_input"),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MedBluePrimary)
                        )

                        // Upload receipt buttons
                        Text("📸 إيصال أو مستند التحويل المالي (JPG or PDF)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    attachedFileName = "IMG_RECEIPT_" + System.currentTimeMillis() + ".jpeg"
                                    attachedFileType = "image"
                                    Toast.makeText(context, "📸 تم التقاط وإرفاق صورة إيصال التحويل المالي!", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9)),
                                modifier = Modifier.weight(1f).height(44.dp).testTag("capture_image_button"),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = MedBluePrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("صورة الإيصال (Screenshot)", fontSize = 9.sp, color = MedBluePrimary, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    attachedFileName = "DOC_RECEIPT_" + System.currentTimeMillis() + ".pdf"
                                    attachedFileType = "pdf"
                                    Toast.makeText(context, "📄 تم رفع مستند الـ PDF للحوالة بنجاح!", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9)),
                                modifier = Modifier.weight(1f).height(44.dp).testTag("upload_pdf_button"),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = MedBluePrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("رفع إيصال PDF", fontSize = 10.sp, color = MedBluePrimary, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Attach preview badge
                        if (attachedFileName != null) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF0FDF4), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (attachedFileType == "pdf") Icons.Default.PictureAsPdf else Icons.Default.Image,
                                        contentDescription = null,
                                        tint = MedGreenPrimary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(attachedFileName!!, fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                }
                                IconButton(onClick = {
                                    attachedFileName = null
                                    attachedFileType = null
                                }) {
                                    Icon(Icons.Default.Close, contentDescription = "Delete attachment", tint = MedRedPrimary)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Submit Payment Button
                Button(
                    onClick = {
                        val acc = selectedAccount
                        if (acc == null) {
                            Toast.makeText(context, "عذراً، يرجى اختيار حساب المورد السداد إليه أولاً ⚠️", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (attachedFileName == null) {
                            Toast.makeText(context, "الرجاء إرفاق صورة الإيصال أو مستند السداد لتأكيد صحة المعاملة ⚠️", Toast.LENGTH_LONG).show()
                            return@Button
                        }

                        // Build Payment configuration object
                        val paymentObject = Payment(
                            paymentId = "",
                            orderId = "order_" + priceOffer.priceOfferId,
                            hospitalId = currentUser.userId,
                            hospitalName = currentUser.name,
                            supplierId = priceOffer.supplierId,
                            supplierName = priceOffer.supplierName,
                            amount = activeAmountCalculated,
                            currency = currencyChosen,
                            paymentMethod = acc.bankName,
                            receiptUrl = attachedFileName ?: "simulated_attached_file",
                            receiptNote = note,
                            status = "pending",
                            adminVisible = true,
                            commissionAmount = activeAmountCalculated * 0.05,
                            commissionRate = 0.05,
                            commissionStatus = "pending"
                        )

                        FirebaseService.submitPayment(paymentObject, {
                            // Trigger FCM audio notification panel simulation
                            showFcmNotificationSim = currentUser.name
                        }, { errMsg ->
                            Toast.makeText(context, "فشل تسجيل الدائنية: $errMsg", Toast.LENGTH_SHORT).show()
                        })
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("submit_payment_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = MedGreenPrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("إرسال إيصال السداد فوراً للمورد ✔", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }

            // --- FCM Notification Dialogue Simulation Overlay ---
            if (showFcmNotificationSim != null) {
                androidx.compose.ui.window.Dialog(
                    onDismissRequest = {
                        showFcmNotificationSim = null
                        onPaymentSuccess()
                    }
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .border(2.dp, MedGreenPrimary, RoundedCornerShape(16.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Audio / Sound Pulse graphic
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .background(MedGreenPrimary.copy(alpha = 0.15f), RoundedCornerShape(32.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = null,
                                    tint = MedGreenPrimary,
                                    modifier = Modifier.size(36.dp)
                                )
                            }

                            Text(
                                "🔔 إشعار FCM مُرسل ومسموع!",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 16.sp
                            )

                            // Signal Text
                            Box(
                                modifier = Modifier
                                    .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                    .padding(12.dp)
                            ) {
                                Text(
                                    "🔊 [تنبيه صوتي مفعل للهاتف المحمول للمورد]\n\"تم استلام إشعار بتحويل مالي من $showFcmNotificationSim - تحقق من حسابك\"",
                                    color = MedGreenPrimary,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Text(
                                "تم حفظ بيانات الحوالة بنجاح، وربطها بالطلب. سيصل موظفي المالية لدى المورد إشعار بوجود تحويل مالي معلق لمطابقته.",
                                fontSize = 11.sp,
                                color = Color.LightGray,
                                textAlign = TextAlign.Center
                            )

                            Button(
                                onClick = {
                                    showFcmNotificationSim = null
                                    onPaymentSuccess()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MedGreenPrimary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("حسناً، الانتقال للمتابعة 👍", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
