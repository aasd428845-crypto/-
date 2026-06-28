package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MedBluePrimary
import com.example.ui.theme.MedGreenPrimary

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OnboardingScreen(
    onFinished: () -> Unit
) {
    var currentSlide by remember { mutableStateOf(0) }
    val totalSlides = 4

    // Modern medical deep blue/indigo gradient background
    val bgGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0D1B2A),
            Color(0xFF1B263B),
            Color(0xFF415A77)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGradient)
            .padding(24.dp)
            .testTag("onboarding_container")
    ) {
        // Skip Button at Top Right
        if (currentSlide < totalSlides - 1) {
            TextButton(
                onClick = onFinished,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .testTag("onboarding_skip_btn")
            ) {
                Text(
                    text = "تخطي ↩",
                    color = Color.LightGray,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Animated Content for slides
        Box(
            modifier = Modifier
                .fillMaxSize()
                .wrapContentHeight(Alignment.CenterVertically)
                .align(Alignment.Center)
        ) {
            AnimatedContent(
                targetState = currentSlide,
                transitionSpec = {
                    fadeIn() + slideInHorizontally { it } with fadeOut() + slideOutHorizontally { -it }
                },
                label = "slide_transition"
            ) { slide ->
                when (slide) {
                    0 -> OnboardingSlide1()
                    1 -> OnboardingSlide2()
                    2 -> OnboardingSlide3()
                    3 -> OnboardingSlide4(onFinished)
                }
            }
        }

        // Bottom Controls (Dots and Buttons)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Slide Indicators (Dots)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until totalSlides) {
                    Box(
                        modifier = Modifier
                            .size(if (i == currentSlide) 14.dp else 8.dp)
                            .clip(CircleShape)
                            .background(if (i == currentSlide) MedGreenPrimary else Color.Gray)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Navigation buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentSlide > 0) {
                    OutlinedButton(
                        onClick = { currentSlide-- },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.linearGradient(listOf(Color.White, Color.LightGray))),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(50.dp)
                            .weight(1f)
                            .testTag("onboarding_prev_btn")
                    ) {
                        Text("السابق ➡️", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.width(16.dp))
                }

                if (currentSlide < totalSlides - 1) {
                    Button(
                        onClick = { currentSlide++ },
                        colors = ButtonDefaults.buttonColors(containerColor = MedGreenPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(50.dp)
                            .weight(1f)
                            .testTag("onboarding_next_btn")
                    ) {
                        Text("التالي ⬅️", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingSlide1() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Large medical service icon with animated/pulsing style
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(Color.White.copy(alpha = 0.12f), CircleShape)
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.LocalHospital,
                contentDescription = "Medical Group Logo",
                tint = MedGreenPrimary,
                modifier = Modifier.size(70.dp)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "مجموعة الشفاء للأدوية والمستلزمات الطبية",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            lineHeight = 32.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "شريكك الموثوق في الإمداد الدوائي منذ 1995",
            color = Color.LightGray,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(30.dp))

        // Stat Badges Row
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Text("المؤشرات الإحصائية للمجموعة 📈", color = MedGreenPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                StatItem(title = "6 فروع", desc = "في كافة المحافظات")
                StatItem(title = "500+ عميل", desc = "مستشفى وصيدلية")
                StatItem(title = "10k+ صنف", desc = "دواء ومستلزم")
            }
        }
    }
}

@Composable
fun OnboardingSlide2() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(Color.White.copy(alpha = 0.12f), CircleShape)
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.LocalShipping,
                contentDescription = "Delivery Icon",
                tint = MedGreenPrimary,
                modifier = Modifier.size(70.dp)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "إمداد سريع وآمن",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "نوصل طلبك من أقرب فرع لموقعك خلال 24 ساعة مع ضمان سلسلة التبريد الكاملة وحماية فاعلية الأدوية.",
            color = Color.LightGray,
            fontSize = 15.sp,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
    }
}

@Composable
fun OnboardingSlide3() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(Color.White.copy(alpha = 0.12f), CircleShape)
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Assignment,
                contentDescription = "Price Offer Icon",
                tint = MedGreenPrimary,
                modifier = Modifier.size(70.dp)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "أفضل الأسعار والجودة",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "قارن العروض المباشرة من فروعنا المتعددة واختر الأنسب لميزانيتك، مع ضمان الجودة الكاملة والترخيص الرسمي من وزارة الصحة العامة والسكان.",
            color = Color.LightGray,
            fontSize = 15.sp,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
    }
}

@Composable
fun OnboardingSlide4(onStartClicked: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(Color.White.copy(alpha = 0.12f), CircleShape)
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = "Security Icon",
                tint = MedGreenPrimary,
                modifier = Modifier.size(70.dp)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "شراكة موثوقة وآمنة",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "جميع المعاملات والمدفوعات محمية وموثقة بسجلات واضحة، مع توفير دعم فني طبي مخصص على مدار الساعة لمتابعة توريد الطلبيات بدون أي تعقيد.",
            color = Color.LightGray,
            fontSize = 15.sp,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(modifier = Modifier.height(36.dp))

        Button(
            onClick = onStartClicked,
            colors = ButtonDefaults.buttonColors(containerColor = MedGreenPrimary),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("onboarding_start_now_btn")
        ) {
            Text(
                text = "ابدأ الآن 🚀",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun StatItem(title: String, desc: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = desc,
            color = Color.LightGray,
            fontSize = 10.sp,
            textAlign = TextAlign.Center
        )
    }
}
