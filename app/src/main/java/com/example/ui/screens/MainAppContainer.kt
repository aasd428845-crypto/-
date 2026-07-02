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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.service.FirebaseService
import com.example.ui.theme.MedBluePrimary
import com.example.ui.theme.MedGreenPrimary
import com.example.ui.theme.MedRedPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContainer() {
    val context = LocalContext.current

    // Session States
    var userLoggedIn by remember { mutableStateOf<User?>(null) }
    var currentScreenState by remember { mutableStateOf("login") } // "login", "dashboard", "add_address", "delivery_method", "payment", "bank_accounts", "schedules"
    var directorScreenState by remember { mutableStateOf("dashboard") } // "dashboard", "catalog_management"

    // Temporary storage for item checkout
    var activeCheckoutOffer by remember { mutableStateOf<PriceOffer?>(null) }
    var activeCheckoutOrder by remember { mutableStateOf<Order?>(null) }

    // Search query states
    var searchQuery by remember { mutableStateOf("") }
    var userAddressesList by remember { mutableStateOf<List<UserAddress>>(emptyList()) }
    var selectedDefaultAddress by remember { mutableStateOf<UserAddress?>(null) }

    // Dialog state for fast testing instructions
    var showExplanationHelper by remember { mutableStateOf(true) }

    // Fetch and populate database arrays
    fun refreshCurrentUserData() {
        val user = userLoggedIn
        if (user != null) {
            FirebaseService.getUserAddresses(user.userId) { list ->
                userAddressesList = list
                selectedDefaultAddress = list.find { it.isDefault } ?: list.firstOrNull()
            }
        }
    }

    LaunchedEffect(userLoggedIn) {
        refreshCurrentUserData()
    }

    if (userLoggedIn == null) {
        // --- 🟢 Login / Welcome Screen 🟢 ---
        var emailInput by remember { mutableStateOf("") }
        var isProgressing by remember { mutableStateOf(false) }

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("ميد-لينك اليمن | MedLink Yemen 🏥", fontWeight = FontWeight.ExtraBold, color = Color.White) },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MedBluePrimary, titleContentColor = Color.White)
                )
            }
        ) { paddingVals ->
            Column(
                modifier = Modifier
                    .padding(paddingVals)
                    .fillMaxSize()
                    .background(Color(0xFF0F172A)) // Aesthetic dark cosmic theme
                    .padding(20.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Branded Logo Card
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(MedBluePrimary, CircleShape)
                        .border(3.dp, MedGreenPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MedicalServices,
                        contentDescription = "Logo",
                        tint = MedGreenPrimary,
                        modifier = Modifier.size(50.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "مِنصة الإمداد والوساطة الدوائية الذكية",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "رابط آمن ومباشر بين المستشفيات والشركات الموردة للأدوية في اليمن لتخطيط الدفع واللوجستيات المبردة",
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )

                Spacer(modifier = Modifier.height(30.dp))

                // Standard email input
                OutlinedTextField(
                    value = emailInput,
                    onValueChange = { emailInput = it },
                    label = { Text("أدخل بريدك الإلكتروني للتجربة") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("login_email_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = MedGreenPrimary,
                        unfocusedLabelColor = Color.LightGray,
                        focusedBorderColor = MedGreenPrimary,
                        unfocusedBorderColor = Color.Gray
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        if (emailInput.isBlank()) {
                            Toast.makeText(context, "الرجاء كتابة البريد الإلكتروني أو اختيار حساب تجريبي بالأسفل 👇", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isProgressing = true
                        FirebaseService.loginUser(emailInput) { user, _ ->
                            userLoggedIn = user
                            currentScreenState = "dashboard"
                            isProgressing = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MedGreenPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("login_submit_btn"),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (isProgressing) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("الدخول للمنصة ➡️", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text("👇 أو تسجيل الدخول السريع كأحد الأطراف للتجربة:", color = Color.LightGray, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(8.dp))

                // Quick Login Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            emailInput = "thawra@hospital.com"
                            FirebaseService.loginUser(emailInput) { user, _ ->
                                userLoggedIn = user
                                currentScreenState = "dashboard"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary, contentColor = Color.White),
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .testTag("login_as_client_btn"),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("🏥 مستشفى الثورة (عميل)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            emailInput = "sanaa@alshefa.com"
                            FirebaseService.loginUser(emailInput) { user, _ ->
                                userLoggedIn = user
                                currentScreenState = "dashboard"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary, contentColor = Color.White),
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .testTag("login_as_branch_manager_btn"),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("💼 مدير فرع صنعاء", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            emailInput = "director@alshefa.com"
                            FirebaseService.loginUser(emailInput) { user, _ ->
                                userLoggedIn = user
                                currentScreenState = "dashboard"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .testTag("login_as_director_btn"),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("👑 المدير العام", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    } else {
        // Logged in. Dispatch based on the new single-company role model
        val loggedUser = userLoggedIn!!

        when (loggedUser.role) {
            "company_director" -> {
                if (directorScreenState == "catalog_management") {
                    DirectorCatalogManagementScreen(
                        currentUser = loggedUser,
                        onNavigateBack = { directorScreenState = "dashboard" }
                    )
                } else {
                    DirectorDashboardScreen(
                        currentUser = loggedUser,
                        onLogout = {
                            directorScreenState = "dashboard"
                            userLoggedIn = null
                        },
                        onNavigateToCatalog = { directorScreenState = "catalog_management" }
                    )
                }
            }
            "branch_manager" -> {
                var branchSetupChecked by remember { mutableStateOf(false) }
                var needsAddressSetup by remember { mutableStateOf(false) }

                LaunchedEffect(loggedUser.userId) {
                    FirebaseService.getUserAddresses(loggedUser.userId) { addresses ->
                        needsAddressSetup = addresses.isEmpty()
                        branchSetupChecked = true
                    }
                }

                if (!branchSetupChecked) {
                    // شاشة تحميل بسيطة
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MedBluePrimary)
                    }
                } else if (needsAddressSetup) {
                    BranchAddressSetupScreen(
                        currentUser = loggedUser,
                        onSetupCompleted = { needsAddressSetup = false }
                    )
                } else {
                    BranchManagerScreen(
                        currentUser = loggedUser,
                        onLogout = { userLoggedIn = null }
                    )
                }
            }
            "client" -> {
                ClientScreen(
                    currentUser = loggedUser,
                    onLogout = { userLoggedIn = null }
                )
            }
            else -> {
                ClientScreen(
                    currentUser = loggedUser,
                    onLogout = { userLoggedIn = null }
                )
            }
        }
    }

    // --- Guide Modal Dialog on startup ---
    if (showExplanationHelper) {
        AlertDialog(
            onDismissRequest = { showExplanationHelper = false },
            title = {
                Text(
                    "بوابة تجربة مجموعة الشفاء الدوائية 🏥 🏢",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "يسهل تطبيق MedLink Yemen الربط بين المستشفيات والفروع لتأمين سلاسل الإمداد الدوائي:",
                        fontSize = 11.sp,
                        color = Color.DarkGray
                    )
                    Text(
                        "1️⃣ بث طلب الشراء (ClientScreen): قيام العميل ببث الاحتياج الدوائي للكل، الفروع القريبة، أو فرع محدد.",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                    Text(
                        "2️⃣ تسعير فوري (BranchManagerScreen): تسعير وتوفير بدائل الأدوية، توفير شروط السداد ومصاريف الشحن من الفروع مباشرة.",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                    Text(
                        "3️⃣ الإدارة والتحكم (DirectorDashboardScreen): لوحة تحكم متكاملة للمدير العام لمتابعة مؤشرات الأداء، المبيعات وتأسيس الفروع الجديدة.",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showExplanationHelper = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary, contentColor = Color.White)
                ) {
                    Text("البدء بالتجربة الحالية 👍", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
