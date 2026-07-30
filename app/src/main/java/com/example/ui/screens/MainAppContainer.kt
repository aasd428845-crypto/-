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
import com.example.ui.screens.AuthScreen

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

    // حالة المستخدم الجديد — يحتاج إضافة عنوان
    var pendingNewUser by remember { mutableStateOf<User?>(null) }

    if (pendingNewUser != null) {
        // مستخدم جديد → شاشة إضافة العنوان الأول مباشرة
        AddAddressScreen(
            currentUser = pendingNewUser!!,
            existingAddress = null,
            onBackClick = {
                // تخطي إضافة العنوان والدخول للتطبيق
                userLoggedIn = pendingNewUser
                pendingNewUser = null
                currentScreenState = "dashboard"
            },
            onSaveSuccess = {
                userLoggedIn = pendingNewUser
                pendingNewUser = null
                currentScreenState = "dashboard"
            }
        )
    } else if (userLoggedIn == null) {
        AuthScreen(
            onAuthSuccess = { user, isNewUser ->
                if (isNewUser && user.role == "client") {
                    pendingNewUser = user
                } else {
                    userLoggedIn = user
                    currentScreenState = "dashboard"
                }
            }
        )
    } else {
        // Logged in. Dispatch based on the new single-company role model
        val loggedUser = userLoggedIn!!

        when (loggedUser.role) {
            "company_director" -> {
                when (directorScreenState) {
                    "catalog_management" -> {
                        DirectorCatalogManagementScreen(
                            currentUser = loggedUser,
                            onNavigateBack = { directorScreenState = "dashboard" }
                        )
                    }
                    "cross_branch_inventory" -> {
                        CrossBranchInventoryScreen(
                            onNavigateBack = { directorScreenState = "dashboard" }
                        )
                    }
                    else -> {
                        DirectorDashboardScreen(
                            currentUser = loggedUser,
                            onLogout = {
                                directorScreenState = "dashboard"
                                userLoggedIn = null
                            },
                            onNavigateToCatalog = { directorScreenState = "catalog_management" },
                            onNavigateToCrossBranchInventory = { directorScreenState = "cross_branch_inventory" }
                        )
                    }
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
