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
import com.example.ui.theme.MedBluePrimary
import com.example.ui.theme.MedGreenPrimary
import com.example.ui.theme.MedRedPrimary

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A)) // Elegant Cosmic Dark
            .padding(24.dp)
            .testTag("auth_container"),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Upper Medical App Badge / Identity
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(MedBluePrimary, CircleShape)
                .border(2.dp, MedGreenPrimary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.MedicalServices,
                contentDescription = null,
                tint = MedGreenPrimary,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "ميد-لينك اليمن | MedLink Yemen 🏥",
            color = Color.White,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 18.sp,
            textAlign = TextAlign.Center
        )
        Text(
            text = "البوابة الموحدة لسلسلة الإمداد الدوائي والمشتريات الطبية",
            color = Color.LightGray,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
        )

        // Custom High-Quality Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (selectedTab == 0) MedBluePrimary else Color.Transparent)
                    .clickable { selectedTab = 0 }
                    .padding(vertical = 12.dp)
                    .testTag("tab_login"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "تسجيل الدخول",
                    color = if (selectedTab == 0) Color.White else Color.LightGray,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (selectedTab == 1) MedBluePrimary else Color.Transparent)
                    .clickable { selectedTab = 1 }
                    .padding(vertical = 12.dp)
                    .testTag("tab_signup"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "إنشاء حساب جديد",
                    color = if (selectedTab == 1) Color.White else Color.LightGray,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Card containing dynamic input form
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (selectedTab == 1) {
                    // ACCOUNT TYPE SELECTOR WITH LARGE MODERN BUTTONS
                    Text(
                        "حدد نوع منشأتك أولاً:",
                        color = Color.White,
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
                                    if (clientType == "hospital") MedGreenPrimary else Color.Gray,
                                    RoundedCornerShape(10.dp)
                                )
                                .background(
                                    if (clientType == "hospital") MedGreenPrimary.copy(alpha = 0.15f) else Color.Transparent
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
                                    color = Color.White,
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
                                    if (clientType == "pharmacy") MedGreenPrimary else Color.Gray,
                                    RoundedCornerShape(10.dp)
                                )
                                .background(
                                    if (clientType == "pharmacy") MedGreenPrimary.copy(alpha = 0.15f) else Color.Transparent
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
                                    color = Color.White,
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
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color.LightGray) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_email_field"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = MedGreenPrimary,
                        unfocusedLabelColor = Color.LightGray,
                        focusedBorderColor = MedGreenPrimary,
                        unfocusedBorderColor = Color.Gray
                    ),
                    singleLine = true
                )

                // Password Field
                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = { passwordInput = it },
                    label = { Text("كلمة المرور") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color.LightGray) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_password_field"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = MedGreenPrimary,
                        unfocusedLabelColor = Color.LightGray,
                        focusedBorderColor = MedGreenPrimary,
                        unfocusedBorderColor = Color.Gray
                    ),
                    singleLine = true
                )

                if (selectedTab == 1) {
                    // Confirm Password Field
                    OutlinedTextField(
                        value = confirmPasswordInput,
                        onValueChange = { confirmPasswordInput = it },
                        label = { Text("تأكيد كلمة المرور") },
                        leadingIcon = { Icon(Icons.Default.LockClock, contentDescription = null, tint = Color.LightGray) },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_confirm_password_field"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = MedGreenPrimary,
                            unfocusedLabelColor = Color.LightGray,
                            focusedBorderColor = MedGreenPrimary,
                            unfocusedBorderColor = Color.Gray
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
                                Toast.makeText(context, "🎉 تم إنشاء الحساب بنجاح! تفضل بإكمال ملفك الشخصي.", Toast.LENGTH_LONG).show()
                                onAuthSuccess(newUser, true) // True specifies new user (needs setup)
                            }, { errorMsg ->
                                isProgressing = false
                                Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                            })

                        } else {
                            // Login Flow
                            isProgressing = true
                            FirebaseService.loginUser(emailInput) { user, error ->
                                isProgressing = false
                                if (user != null) {
                                    Toast.makeText(context, "أهلاً بك مجدداً: ${user.name}", Toast.LENGTH_SHORT).show()
                                    // Check if this user has already completed setup profile in Client/Branch managers
                                    if (user.role == "client") {
                                        FirebaseService.getClientProfile(user.userId) { profile ->
                                            val complete = profile?.profileCompleted ?: false
                                            onAuthSuccess(user, !complete)
                                        }
                                    } else if (user.role == "branch_manager") {
                                        val managerProfile = FirebaseService.fallbackBranchManagerProfiles.find { it.userId == user.userId }
                                        val complete = managerProfile?.profileCompleted ?: false
                                        onAuthSuccess(user, !complete)
                                    } else {
                                        // Directors go directly
                                        onAuthSuccess(user, false)
                                    }
                                } else {
                                    Toast.makeText(context, error ?: "البريد الإلكتروني غير مسجل", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MedGreenPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("auth_submit_btn")
                ) {
                    if (isProgressing) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text(
                            if (selectedTab == 0) "تسجيل الدخول 🚪" else "إنشاء الحساب وبدء التأهيل 🚀",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Demo Accounts Shortcut Notice
        Text(
            "💡 للتجربة السريعة للأنواع المختلفة، اكتب أحد الحسابات التالية:",
            color = Color.Gray,
            fontSize = 10.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "العميل: thawra@hospital.com\nالفرع: sanaa@alshefa.com\nالمدير: director@alshefa.com",
            color = Color.LightGray,
            fontSize = 10.sp,
            textAlign = TextAlign.Center,
            lineHeight = 16.sp
        )
    }
}
