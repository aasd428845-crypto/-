package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.model.User
import com.example.service.FirebaseService
import com.example.service.SupabaseClientProvider
import com.example.ui.theme.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.LocationServices
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.gotrue.providers.builtin.IDToken
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onAuthSuccess: (User, Boolean) -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }

    // Login states
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }

    // Signup states
    var orgName by remember { mutableStateOf("") }
    var ownerName by remember { mutableStateOf("") }
    var clientType by remember { mutableStateOf("hospital") }
    var phoneInput by remember { mutableStateOf("") }
    var facilityPhone by remember { mutableStateOf("") }
    var governorate by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("") }
    var nearestLandmark by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }
    var signupEmail by remember { mutableStateOf("") }
    var signupPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var governorateExpanded by remember { mutableStateOf(false) }

    var isProgressing by remember { mutableStateOf(false) }
    var isLocationLoading by remember { mutableStateOf(false) }
    var dbError by remember { mutableStateOf<String?>(null) }
    var isGoogleLoading by remember { mutableStateOf(false) }

    val supabase = SupabaseClientProvider.client
    val governorates = listOf(
        "صنعاء", "عدن", "تعز", "الحديدة", "إب", "الضالع", "ذمار", "المهرة", "حضرموت",
        "حجة", "عمران", "البيضاء", "الجوف", "صعدة", "لحج", "ريمة", "أبين", "مأرب", "سقطرى"
    )
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    suspend fun fetchUserByUid(uid: String): User? {
        return try {
            withContext(Dispatchers.IO) {
                supabase.postgrest["users"]
                    .select { filter { eq("id", uid) } }
                    .decodeList<User>()
                    .firstOrNull()
            }
        } catch (e: Exception) { null }
    }

    suspend fun processAuth(uid: String, isNewUser: Boolean) {
        val user = fetchUserByUid(uid)
        if (user == null) {
            dbError = "لم يتم العثور على المستخدم في قاعدة البيانات. تواصل مع الدعم."
            return
        }
        if (user.role == "company_director") {
            try { supabase.auth.signOut() } catch (_: Exception) {}
            dbError = "لوحة المدير العام متاحة فقط عبر تطبيق الويب، هذا التطبيق مخصص لمدراء الفروع والسائقين والعملاء"
            return
        }
        if (user.role == "client") {
            FirebaseService.getClientProfile(user.userId) { profile ->
                onAuthSuccess(user, isNewUser || !(profile?.profileCompleted ?: false))
            }
        } else {
            onAuthSuccess(user, isNewUser)
        }
    }

    // Native Google Sign-In setup
    val googleWebClientId = "448796262930-jpq327km8kb081b8ikff1fjipjrqolf5.apps.googleusercontent.com"
    val googleSignInClient = remember {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(googleWebClientId)
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso)
    }
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken
            if (idToken == null) {
                dbError = "فشل الحصول على رمز Google — تحقق من إعداد Web Client ID"
                isGoogleLoading = false
                return@rememberLauncherForActivityResult
            }
            MainScope().launch {
                try {
                    supabase.auth.signInWith(IDToken) {
                        this.idToken = idToken
                        provider = io.github.jan.supabase.gotrue.providers.Google
                    }
                    val session = supabase.auth.currentSessionOrNull()
                    val uid = session?.user?.id
                    if (uid == null) {
                        dbError = "فشل الحصول على جلسة المستخدم بعد Google"
                        isGoogleLoading = false
                        return@launch
                    }
                    val existingUser = fetchUserByUid(uid)
                    if (existingUser != null) {
                        if (existingUser.role == "company_director") {
                            try { supabase.auth.signOut() } catch (_: Exception) {}
                            dbError = "لوحة المدير العام متاحة فقط عبر تطبيق الويب"
                        } else {
                            processAuth(uid, false)
                        }
                    } else {
                        // الصف موجود بالجدول بفضل trigger handle_new_user — نحدّث الاسم والإيميل فقط
                        val userEmail = account?.email ?: ""
                        val userName = account?.displayName ?: ""
                        try {
                            withContext(Dispatchers.IO) {
                                supabase.postgrest["users"].update(
                                    buildJsonObject {
                                        put("name", userName)
                                        put("email", userEmail)
                                    }
                                ) {
                                    filter { eq("id", uid) }
                                }
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("SUPABASE_DEBUG", "Google user update failed: ${e.message}")
                            // لا نوقف تدفق تسجيل الدخول — المستخدم موجود أصلاً
                        }
                        // اقرأ المستخدم بعد التحديث
                        var googleUser = fetchUserByUid(uid)
                        if (googleUser == null) {
                            delay(800)
                            googleUser = fetchUserByUid(uid)
                        }
                        if (googleUser != null) {
                            val needsSetup = !(googleUser.orgName.isNotBlank() && googleUser.governorate.isNotBlank())
                            onAuthSuccess(googleUser, needsSetup)
                        } else {
                            // مستخدم مؤقت كملاذ أخير إذا فشلت القراءة
                            val tempUser = com.example.model.User(
                                userId = uid,
                                email = userEmail,
                                name = userName,
                                role = "client",
                                clientType = "pharmacy"
                            )
                            onAuthSuccess(tempUser, true)
                        }
                    }
                } catch (e: Exception) {
                    dbError = "خطأ في تسجيل الدخول بـ Google: ${e.message}"
                } finally {
                    isGoogleLoading = false
                }
            }
        } catch (e: ApiException) {
            isGoogleLoading = false
            dbError = when (e.statusCode) {
                12501 -> "تم إلغاء تسجيل الدخول بـ Google"
                10 -> "خطأ في إعداد Google (DEVELOPER_ERROR): تأكد من تسجيل SHA-1 وpackage name في Google Cloud Console"
                7 -> "لا يوجد اتصال بالإنترنت"
                12500 -> "فشل تسجيل الدخول بـ Google — تحقق من إعداد OAuth في Google Cloud Console"
                else -> "فشل تسجيل الدخول بـ Google (كود الخطأ: ${e.statusCode})"
            }
        }
    }

    fun requestCurrentLocation() {
        val permission = Manifest.permission.ACCESS_FINE_LOCATION
        if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "يرجى منح إذن الموقع لتحديد الموقع الحالي", Toast.LENGTH_SHORT).show()
            return
        }
        isLocationLoading = true
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            isLocationLoading = false
            if (location != null) {
                latitude = location.latitude
                longitude = location.longitude
                Toast.makeText(context, "تم حفظ الموقع الحالي مؤقتاً", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "تعذر الحصول على الموقع الحالي", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { e ->
            isLocationLoading = false
            Toast.makeText(context, "فشل تحديد الموقع: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Email/Password submit
    fun performAuth() {
        val em = (if (selectedTab == 0) emailInput else signupEmail).trim()
        val pw = if (selectedTab == 0) passwordInput else signupPassword
        val trimmedOrgName = orgName.trim()
        val trimmedOwnerName = ownerName.trim()
        val trimmedFacilityPhone = facilityPhone.trim()
        val trimmedGovernorate = governorate.trim()
        val trimmedDistrict = district.trim()
        val trimmedNearestLandmark = nearestLandmark.trim()
        val trimmedPhoneInput = phoneInput.trim()
        if (em.isBlank() || pw.isBlank()) {
            Toast.makeText(context, "الرجاء تعبئة كافة الحقول المطلوبة", Toast.LENGTH_SHORT).show()
            return
        }
        if (selectedTab == 1) {
            if (trimmedOrgName.isBlank()) {
                Toast.makeText(context, "الرجاء إدخال اسم المنشأة", Toast.LENGTH_SHORT).show(); return
            }
            if (pw.length < 6) {
                Toast.makeText(context, "كلمة المرور يجب أن تكون 6 أحرف على الأقل", Toast.LENGTH_SHORT).show(); return
            }
            if (pw != confirmPassword) {
                Toast.makeText(context, "كلمة المرور وتأكيدها غير متطابقين!", Toast.LENGTH_SHORT).show(); return
            }
        }
        isProgressing = true; dbError = null
        MainScope().launch {
            try {
                if (selectedTab == 0) {
                    // === LOGIN ===
                    supabase.auth.signInWith(Email) {
                        email = em; password = pw
                    }
                    val uid = supabase.auth.currentSessionOrNull()?.user?.id
                    if (uid == null) { dbError = "فشل تسجيل الدخول"; isProgressing = false; return@launch }
                    processAuth(uid, false)
                } else {
                    // === SIGNUP ===
                    val result = supabase.auth.signUpWith(Email) {
                        email = em; password = pw
                        data = buildJsonObject {
                            put("role", "client")
                            put("org_name", trimmedOrgName)
                            put("owner_name", trimmedOwnerName)
                            put("client_type", clientType)
                            put("phone", trimmedPhoneInput)
                            put("facility_phone", trimmedFacilityPhone)
                            put("governorate", trimmedGovernorate)
                            put("district", trimmedDistrict)
                            put("nearest_landmark", trimmedNearestLandmark)
                            put("latitude", latitude ?: 0.0)
                            put("longitude", longitude ?: 0.0)
                            putJsonObject("facility_info") {
                                put("type", clientType)
                                put("name", trimmedOrgName)
                                put("owner_name", trimmedOwnerName)
                                put("facility_phone", trimmedFacilityPhone)
                                put("governorate", trimmedGovernorate)
                                put("district", trimmedDistrict)
                                put("nearest_landmark", trimmedNearestLandmark)
                            }
                        }
                    }
                    val session = supabase.auth.currentSessionOrNull()
                    if (session != null) {
                        processAuth(session.user!!.id, true)
                    } else {
                        Toast.makeText(context, "تم إنشاء الحساب! تحقق من بريدك لتأكيد الحساب ثم سجل الدخول.", Toast.LENGTH_LONG).show()
                        isProgressing = false; selectedTab = 0
                    }
                }
                isProgressing = false
            } catch (e: Exception) {
                isProgressing = false
                dbError = e.message?.let {
                    when {
                        it.contains("Invalid login") -> "البريد أو كلمة المرور غير صحيحة"
                        it.contains("already registered") -> "البريد الإلكتروني مسجل مسبقاً"
                        it.contains("email") && it.contains("confirm") -> "تحقق من بريدك الإلكتروني لتأكيد الحساب"
                        else -> "خطأ: $it"
                    }
                } ?: "حدث خطأ غير متوقع"
            }
        }
    }

    // Google Sign-In — native account picker (no browser)
    fun performGoogleSignIn() {
        isGoogleLoading = true
        dbError = null
        // Sign out first to always show account picker
        googleSignInClient.signOut().addOnCompleteListener {
            googleSignInLauncher.launch(googleSignInClient.signInIntent)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceLight)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
            .testTag("auth_container"),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Error Banner
        if (dbError != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3F3)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("⚠️", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(dbError!!, color = Color(0xFFDC2626), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                }
            }
        }

        // Logo Badge
        Box(
            modifier = Modifier.size(80.dp).background(Color.White, CircleShape).border(2.dp, BrandPrimary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.MedicalServices, null, tint = BrandPrimary, modifier = Modifier.size(40.dp))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("ميد-لينك اليمن | MedLink Yemen 🏥", color = OnSurfaceDark, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, textAlign = TextAlign.Center)
        Text("البوابة الموحدة لسلسلة الإمداد الدوائي والمشتريات الطبية", color = TextSecondaryGray, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 4.dp, bottom = 24.dp))

        // Tabs
        Row(
            modifier = Modifier.fillMaxWidth().background(Color(0xFFE2E8F0), RoundedCornerShape(12.dp)).padding(4.dp)
        ) {
            Box(
                modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp))
                    .background(if (selectedTab == 0) BrandPrimary else Color.Transparent)
                    .clickable { selectedTab = 0 }.padding(vertical = 12.dp).testTag("tab_login"),
                contentAlignment = Alignment.Center
            ) {
                Text("تسجيل الدخول", color = if (selectedTab == 0) OnBrandPrimary else OnSurfaceDark, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Box(
                modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp))
                    .background(if (selectedTab == 1) BrandPrimary else Color.Transparent)
                    .clickable { selectedTab = 1 }.padding(vertical = 12.dp).testTag("tab_signup"),
                contentAlignment = Alignment.Center
            ) {
                Text("إنشاء حساب جديد", color = if (selectedTab == 1) OnBrandPrimary else OnSurfaceDark, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        // Form Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (selectedTab == 1) {
                    // Client type selector
                    Text("حدد نوع منشأتك:", color = OnSurfaceDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        listOf("hospital" to "🏥 مستشفى أو مركز", "pharmacy" to "💊 صيدلية أو مستودع").forEach { (type, label) ->
                            Box(
                                modifier = Modifier.weight(1f).clip(RoundedCornerShape(10.dp))
                                    .border(2.dp, if (clientType == type) BrandPrimary else Color(0xFFCBD5E1), RoundedCornerShape(10.dp))
                                    .background(if (clientType == type) BrandPrimary.copy(alpha = 0.08f) else Color.White)
                                    .clickable { clientType = type }.padding(vertical = 14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(label.take(2), fontSize = 24.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(label.drop(2), color = OnSurfaceDark, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    // Org Name
                    OutlinedTextField(
                        value = orgName, onValueChange = { orgName = it },
                        label = { Text("اسم المنشأة") },
                        leadingIcon = { Icon(Icons.Default.Business, null, tint = TextSecondaryGray) },
                        modifier = Modifier.fillMaxWidth().testTag("auth_org_name"),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandPrimary, focusedLabelColor = BrandPrimary),
                        singleLine = true
                    )

                    // Owner Name
                    OutlinedTextField(
                        value = ownerName, onValueChange = { ownerName = it },
                        label = { Text("اسم صاحب/مديرة المنشأة") },
                        leadingIcon = { Icon(Icons.Default.Person, null, tint = TextSecondaryGray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandPrimary, focusedLabelColor = BrandPrimary),
                        singleLine = true
                    )

                    // Facility Phone
                    OutlinedTextField(
                        value = facilityPhone, onValueChange = { facilityPhone = it },
                        label = { Text("هاتف المنشأة") },
                        leadingIcon = { Icon(Icons.Default.Phone, null, tint = TextSecondaryGray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandPrimary, focusedLabelColor = BrandPrimary),
                        singleLine = true
                    )

                    // Governorate Dropdown
                    ExposedDropdownMenuBox(
                        expanded = governorateExpanded,
                        onExpandedChange = { governorateExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = governorate,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("المحافظة") },
                            leadingIcon = { Icon(Icons.Default.LocationCity, null, tint = TextSecondaryGray) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = governorateExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandPrimary, focusedLabelColor = BrandPrimary),
                            singleLine = true
                        )
                        ExposedDropdownMenu(
                            expanded = governorateExpanded,
                            onDismissRequest = { governorateExpanded = false }
                        ) {
                            governorates.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        governorate = option
                                        governorateExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // District
                    OutlinedTextField(
                        value = district, onValueChange = { district = it },
                        label = { Text("المديرية") },
                        leadingIcon = { Icon(Icons.Default.Map, null, tint = TextSecondaryGray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandPrimary, focusedLabelColor = BrandPrimary),
                        singleLine = true
                    )

                    // Landmark
                    OutlinedTextField(
                        value = nearestLandmark, onValueChange = { nearestLandmark = it },
                        label = { Text("الحي أو أقرب معلم") },
                        leadingIcon = { Icon(Icons.Default.Place, null, tint = TextSecondaryGray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandPrimary, focusedLabelColor = BrandPrimary),
                        singleLine = true
                    )

                    Button(
                        onClick = { requestCurrentLocation() },
                        enabled = !isLocationLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary, contentColor = OnBrandPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isLocationLoading) {
                            CircularProgressIndicator(color = OnBrandPrimary, modifier = Modifier.size(20.dp))
                        } else {
                            Text("📍 تحديد الموقع الجغرافي الحالي", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Personal Phone
                    OutlinedTextField(
                        value = phoneInput, onValueChange = { phoneInput = it },
                        label = { Text("رقم الهاتف") },
                        leadingIcon = { Icon(Icons.Default.Phone, null, tint = TextSecondaryGray) },
                        modifier = Modifier.fillMaxWidth().testTag("auth_phone"),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandPrimary, focusedLabelColor = BrandPrimary),
                        singleLine = true
                    )
                }

                // Email
                OutlinedTextField(
                    value = if (selectedTab == 0) emailInput else signupEmail,
                    onValueChange = { if (selectedTab == 0) emailInput = it else signupEmail = it },
                    label = { Text("البريد الإلكتروني") },
                    leadingIcon = { Icon(Icons.Default.Email, null, tint = TextSecondaryGray) },
                    modifier = Modifier.fillMaxWidth().testTag("auth_email_field"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = OnSurfaceDark, unfocusedTextColor = OnSurfaceDark,
                        focusedLabelColor = BrandPrimary, unfocusedLabelColor = TextSecondaryGray,
                        focusedBorderColor = BrandPrimary, unfocusedBorderColor = Color(0xFFCBD5E1)
                    ),
                    singleLine = true
                )

                // Password
                OutlinedTextField(
                    value = if (selectedTab == 0) passwordInput else signupPassword,
                    onValueChange = { if (selectedTab == 0) passwordInput = it else signupPassword = it },
                    label = { Text("كلمة المرور") },
                    leadingIcon = { Icon(Icons.Default.Lock, null, tint = TextSecondaryGray) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth().testTag("auth_password_field"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = OnSurfaceDark, unfocusedTextColor = OnSurfaceDark,
                        focusedLabelColor = BrandPrimary, unfocusedLabelColor = TextSecondaryGray,
                        focusedBorderColor = BrandPrimary, unfocusedBorderColor = Color(0xFFCBD5E1)
                    ),
                    singleLine = true
                )

                if (selectedTab == 1) {
                    OutlinedTextField(
                        value = confirmPassword, onValueChange = { confirmPassword = it },
                        label = { Text("تأكيد كلمة المرور") },
                        leadingIcon = { Icon(Icons.Default.LockClock, null, tint = TextSecondaryGray) },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth().testTag("auth_confirm_password"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = OnSurfaceDark, unfocusedTextColor = OnSurfaceDark,
                            focusedLabelColor = BrandPrimary, unfocusedLabelColor = TextSecondaryGray,
                            focusedBorderColor = BrandPrimary, unfocusedBorderColor = Color(0xFFCBD5E1)
                        ),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Submit Button
                Button(
                    onClick = { performAuth() },
                    enabled = !isProgressing,
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary, contentColor = OnBrandPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(50.dp).testTag("auth_submit_btn")
                ) {
                    if (isProgressing) {
                        CircularProgressIndicator(color = OnBrandPrimary, modifier = Modifier.size(24.dp))
                    } else {
                        Text(
                            if (selectedTab == 0) "تسجيل الدخول 🚪" else "إنشاء الحساب 🚀",
                            fontSize = 14.sp, fontWeight = FontWeight.Bold, color = OnBrandPrimary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Google Sign-In Button
        OutlinedButton(
            onClick = { performGoogleSignIn() },
            enabled = !isGoogleLoading && !isProgressing,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(50.dp).testTag("google_signin_btn"),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
        ) {
            if (isGoogleLoading) {
                CircularProgressIndicator(color = BrandPrimary, modifier = Modifier.size(24.dp))
            } else {
                Text("🔵 المتابعة عبر Google", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = OnSurfaceDark)
            }
        }
    }
}
