package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ClientProfile
import com.example.service.FirebaseService
import com.example.ui.theme.MedBluePrimary
import com.example.ui.theme.MedGreenPrimary
import com.example.ui.theme.MedRedPrimary
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientDigitalCard(
    userId: String,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var clientProfile by remember { mutableStateOf<ClientProfile?>(null) }

    LaunchedEffect(userId) {
        FirebaseService.getClientProfile(userId) { profile ->
            clientProfile = profile
        }
    }

    // Default template profile in case loading
    val activeProfile = clientProfile ?: ClientProfile(
        clientId = "YM-CL-10254",
        userId = userId,
        institutionName = "مستشفى الثورة العام النموذجي",
        clientType = "hospital",
        responsiblePerson = "أحمد محمد الحيمي",
        phone = "+967 771234567",
        governorate = "صنعاء",
        city = "صنعاء",
        assignedBranchName = "فرع صنعاء الرئيسي",
        isVerified = true,
        joinedAt = System.currentTimeMillis() - (86400000L * 15) // 15 days ago
    )

    // Pulse animation for QR/Badge highlight
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alphaPulse by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha_pulse"
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("بطاقة العضوية الرقمية الطبية 🛡️", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("digital_card_back")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MedBluePrimary)
            )
        }
    ) { paddingVals ->
        Column(
            modifier = Modifier
                .padding(paddingVals)
                .fillMaxSize()
                .background(Color(0xFF0F172A))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "بطاقة الهوية الرقمية المعتمدة للمشتريات والإمداد",
                color = Color.LightGray,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // GORGEOUS GLASSMORPHIC DIGITAL CARD
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .border(
                        1.dp,
                        Brush.linearGradient(listOf(Color.White.copy(alpha = 0.3f), Color.Transparent)),
                        RoundedCornerShape(20.dp)
                    )
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF1E293B),
                                Color(0xFF0F172A)
                            )
                        )
                    )
                    .padding(20.dp)
                    .testTag("digital_card_canvas")
            ) {
                // Background watermarks/elements
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .align(Alignment.BottomStart)
                        .graphicsLayer(alpha = 0.04f)
                        .background(Color.White, CircleShape)
                )

                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Upper header row: Logo & verification
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Verification Status Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (activeProfile.isVerified) MedGreenPrimary.copy(alpha = 0.2f)
                                    else MedRedPrimary.copy(alpha = 0.2f)
                                )
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (activeProfile.isVerified) MedGreenPrimary else MedRedPrimary)
                                        .graphicsLayer(alpha = alphaPulse)
                                )
                                Text(
                                    text = if (activeProfile.isVerified) "عضوية معتمدة 🛡️" else "قيد المراجعة ⏳",
                                    color = if (activeProfile.isVerified) MedGreenPrimary else MedRedPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // App/Group Identity
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "مجموعة الشفاء للأدوية",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Text(
                                "MEDLINK YEMEN",
                                color = MedGreenPrimary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Middle details row: Hospital / Pharmacy Name
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left - Pulsing security QR code simulation
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(65.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White)
                                    .border(1.dp, MedGreenPrimary, RoundedCornerShape(8.dp))
                                    .padding(4.dp)
                            ) {
                                // QR code visual mock drawing using clean CSS/Compose boxes
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.SpaceEvenly,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        Box(modifier = Modifier.size(16.dp).border(2.dp, Color.Black).background(Color.White))
                                        Box(modifier = Modifier.size(16.dp).border(2.dp, Color.Black).background(Color.White))
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        Box(modifier = Modifier.size(16.dp).border(2.dp, Color.Black).background(Color.White))
                                        Box(modifier = Modifier.size(8.dp).background(Color.Black))
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "مسح أمني 🔎",
                                color = Color.LightGray,
                                fontSize = 8.sp,
                                textAlign = TextAlign.Center
                            )
                        }

                        // Right - Customer Info
                        Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                            Text(
                                text = activeProfile.institutionName,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                maxLines = 1,
                                textAlign = TextAlign.Right
                            )
                            Text(
                                text = "نوع العضوية: ${if (activeProfile.clientType == "hospital") "مستشفى طبي معتمد" else "صيدلية / مستودع دوائي"}",
                                color = Color.LightGray,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Right
                            )
                            Text(
                                text = "رقم العضوية: #${activeProfile.clientId}",
                                color = MedGreenPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }

                    Divider(color = Color.White.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 4.dp))

                    // Lower row: Branch & date
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val formattedDate = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date(activeProfile.joinedAt))
                        Text(
                            text = "انضم في: $formattedDate",
                            color = Color.Gray,
                            fontSize = 10.sp
                        )

                        Text(
                            text = "الفرع المغذي: ${activeProfile.assignedBranchName}",
                            color = Color.LightGray,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Action Buttons
            Button(
                onClick = {
                    Toast.makeText(context, "📤 جاري تحضير ملف البطاقة الطبية الرقمية المعتمدة ومشاركتها كـ PDF...", Toast.LENGTH_LONG).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MedGreenPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("share_digital_card_btn")
            ) {
                Icon(Icons.Default.Share, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("مشاركة البطاقة الطبية المعتمدة 📤", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    Toast.makeText(context, "📥 تم حفظ رمز الاستجابة السريعة (QR) في ألبوم الصور للاستخدام غير المتصل", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("download_qr_btn")
            ) {
                Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("تنزيل رمز الاستجابة السريعة (QR) 📥", color = Color.White, fontSize = 13.sp)
            }
        }
    }
}
