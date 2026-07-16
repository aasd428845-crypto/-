package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.User
import com.example.service.FirebaseService
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onAuthSuccess: (User, Boolean) -> Unit // (user, isNewUser)
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) } // 0: Login, 1: Signup

    // Input States
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var confirmPasswordInput by remember { mutableStateOf("") }
    var clientType by remember { mutableStateOf("hospital") } // "hospital" or "pharmacy"
    var isProgressing by remember { mutableStateOf(false) }
    var dbError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceLight) // Clean, enterprise-grade light background
            .padding(24.dp)
            .testTag("auth_container"),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ⚠️ Error Banner
        if (dbError != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3F3)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("⚠️", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = dbError!!,
                        color = Color(0xFFDC2626),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        // Upper Medical App Badge / Identity
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(Color.White, CircleShape)
                .border(2.dp, BrandPrimary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.MedicalServices,
                contentDescription = null,
                tint = BrandPrimary,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "ميد-لينك اليمن | MedLink Yemen 🏥",
            color = OnSurfaceDark,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 18.sp,
            textAlign = TextAlign.Center
        )
        Text(
            text = "البوابة الموحدة لسلسلة الإمداد الدوائي والمشتريات الطبية",
            color = TextSecondaryGray,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
        )

        // Custom High-Quality Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (selectedTab == 0) BrandPrimary else Color.Transparent)
                    .clickable { selectedTab = 0 }
                    .padding(vertical = 12.dp)
                    .testTag("tab_login"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "تسجيل الدخول",
                    color = if (selectedTab == 0) OnBrandPrimary else OnSurfaceDark,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (selectedTab == 1) BrandPrimary else Color.Transparent)
                    .clickable { selectedTab = 1 }
                    .padding(vertical = 12.dp)
                    .testTag("tab_signup"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "إنشاء حساب جديد",
                    color = if (selectedTab == 1) OnBrandPrimary else OnSurfaceDark,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Card containing dynamic input form
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (selectedTab == 1) {
                    // ACCOUNT TYPE SELECTOR WITH LARGE MODERN BUTTONS
                    Text(
                        "حدد نوع منشأتك أولاً:",
                        color = OnSurfaceDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Hospital Select Button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .border(
                                    2.dp,
                                    if (clientType == "hospital") BrandPrimary else Color(0xFFCBD5E1),
                                    RoundedCornerShape(10.dp)
                                )
                                .background(
                                    if (clientType == "hospital") BrandPrimary.copy(alpha = 0.08f) else Color.White
                                )
                                .clickable { clientType = "hospital" }
                                .padding(vertical = 14.dp)
                                .testTag("select_hospital_type"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🏥", fontSize = 24.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "مستشفى أو مركز",
                                    color = OnSurfaceDark,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Pharmacy Select Button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .border(
                                    2.dp,
                                    if (clientType == "pharmacy") BrandPrimary else Color(0xFFCBD5E1),
                                    RoundedCornerShape(10.dp)
                                )
                                .background(
                                    if (clientType == "pharmacy") BrandPrimary.copy(alpha = 0.08f) else Color.White
                                )
                                .clickable { clientType = "pharmacy" }
                                .padding(vertical = 14.dp)
                                .testTag("select_pharmacy_type"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("💊", fontSize = 24.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "صيدلية أو مستودع",
                                    color = OnSurfaceDark,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                // Email Field
                OutlinedTextField(
                    value = emailInput,
                    onValueChange = { emailInput = it },
                    label = { Text("البريد الإلكتروني") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = TextSecondaryGray) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_email_field"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = OnSurfaceDark,
                        unfocusedTextColor = OnSurfaceDark,
                        focusedLabelColor = BrandPrimary,
                        unfocusedLabelColor = TextSecondaryGray,
                        focusedBorderColor = BrandPrimary,
                        unfocusedBorderColor = Color(0xFFCBD5E1)
                    ),
                    singleLine = true
                )

                // Password Field
                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = { passwordInput = it },
                    label = { Text("كلمة المرور") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = TextSecondaryGray) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_password_field"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = OnSurfaceDark,
                        unfocusedTextColor = OnSurfaceDark,
                        focusedLabelColor = BrandPrimary,
                        unfocusedLabelColor = TextSecondaryGray,
                        focusedBorderColor = BrandPrimary,
                        unfocusedBorderColor = Color(0xFFCBD5E1)
                    ),
                    singleLine = true
                )

                if (selectedTab == 1) {
                    // Confirm Password Field
                    OutlinedTextField(
                        value = confirmPasswordInput,
                        onValueChange = { confirmPasswordInput = it },
                        label = { Text("تأكيد كلمة المرور") },
                        leadingIcon = { Icon(Icons.Default.LockClock, contentDescription = null, tint = TextSecondaryGray) },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_confirm_password_field"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = OnSurfaceDark,
                            unfocusedTextColor = OnSurfaceDark,
                            focusedLabelColor = BrandPrimary,
                            unfocusedLabelColor = TextSecondaryGray,
                            focusedBorderColor = BrandPrimary,
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        ),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Primary Action Button
                Button(
                    onClick = {
                        if (emailInput.isBlank() || passwordInput.isBlank()) {
                            Toast.makeText(context, "الرجاء تعبئة كافة الحقول المطلوبة", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        if (selectedTab == 1) {
                            if (passwordInput != confirmPasswordInput) {
                                Toast.makeText(context, "كلمة المرور وتأكيدها غير متطابقين!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (passwordInput.length < 6) {
                                Toast.makeText(context, "كلمة المرور يجب أن تكون 6 أحرف على الأقل", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            // Sign Up Flow
                            isProgressing = true
                            val newUser = User(
                                userId = "user_" + System.currentTimeMillis(),
                                name = emailInput.substringBefore("@"),
                                email = emailInput,
                                role = "client", // New users registered via customer signup are clients
                                clientType = clientType,
                                orgName = if (clientType == "hospital") "مستشفى طبي جديد" else "صيدلية جديدة",
                                governorate = "",
                                city = "",
                                isVerified = false,
                                isActive = true,
                                createdAt = System.currentTimeMillis()
                            )

                            FirebaseService.registerUser(newUser, {
                                isProgressing = false
                                dbError = null
                                Toast.makeText(context, "🎉 تم إنشاء الحساب بنجاح! تفضل بإكمال ملفك الشخصي.", Toast.LENGTH_LONG).show()
                                onAuthSuccess(newUser, true) // True specifies new user (needs setup)
                            }, { errorMsg ->
                                isProgressing = false
                                if (errorMsg.contains("قاعدة البيانات")) {
                                    dbError = errorMsg
                                } else {
                                    Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                                }
                            })

                        } else {
                            // Login Flow
                            isProgressing = true
                            dbError = null
                            FirebaseService.loginUser(emailInput) { user, error ->
                                isProgressing = false
                                if (user != null) {
                                    dbError = null
                                    Toast.makeText(context, "أهلاً بك مجدداً: ${user.name}", Toast.LENGTH_SHORT).show()
                                    // Check if this user has already completed setup profile in Client/Branch managers
                                    if (user.role == "client") {
                                        FirebaseService.getClientProfile(user.userId) { profile ->
                                            val complete = profile?.profileCompleted ?: false
                                            onAuthSuccess(user, !complete)
                                        }
                                    } else if (user.role == "branch_manager") {
                                        val managerProfile = null
                                        val complete = managerProfile?.profileCompleted ?: false
                                        onAuthSuccess(user, !complete)
                                    } else {
                                        // Directors go directly
                                        onAuthSuccess(user, false)
                                    }
                                } else {
                                    val errorMsg = error ?: "البريد الإلكتروني غير مسجل"
                                    if (errorMsg.contains("قاعدة البيانات")) {
                                        dbError = errorMsg
                                    } else {
                                        Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary, contentColor = OnBrandPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("auth_submit_btn")
                ) {
                    if (isProgressing) {
                        CircularProgressIndicator(color = OnBrandPrimary, modifier = Modifier.size(24.dp))
                    } else {
                        Text(
                            if (selectedTab == 0) "تسجيل الدخول 🚪" else "إنشاء الحساب وبدء التأهيل 🚀",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnBrandPrimary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Demo Accounts Shortcut Notice
        Text(
            "💡 للتجربة السريعة للأنواع المختلفة، اكتب أحد الحسابات التالية:",
            color = TextSecondaryGray,
            fontSize = 11.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "العميل: thawra@hospital.com\nالفرع: sanaa@alshefa.com\nالمدير: director@alshefa.com",
            color = OnSurfaceDark.copy(alpha = 0.8f),
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            lineHeight = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
