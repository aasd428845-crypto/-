package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.User
import com.example.ui.theme.*

private data class KpiItem(
    val title: String,
    val value: String,
    val icon: ImageVector,
    val color: Color,
    val bgColor: Color,
    val testTag: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DirectorDashboardScreen(
    currentUser: User,
    onLogout: () -> Unit,
    onNavigateToCatalog: () -> Unit,
    onNavigateToCrossBranchInventory: () -> Unit
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "ميد-لينك | الإدارة العامة 👑",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                            Text(
                                text = "المدير العام: ${currentUser.name}",
                                fontSize = 11.sp,
                                color = Color.LightGray
                            )
                        }
                    },
                    actions = {
                        TextButton(
                            onClick = onNavigateToCrossBranchInventory,
                            modifier = Modifier.testTag("navigate_to_cross_branch_inventory_btn")
                        ) {
                            Text(
                                text = "المخزون الموحد 📦",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(
                            onClick = onNavigateToCatalog,
                            modifier = Modifier.testTag("navigate_to_catalog_btn")
                        ) {
                            Text(
                                text = "إدارة الكتالوج 💊",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(
                            onClick = onLogout,
                            modifier = Modifier.testTag("logout_button")
                        ) {
                            Text(
                                text = "خروج 🚪",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MedBluePrimary, titleContentColor = Color.White)
                )
            }
        ) { paddingVals ->
            LazyColumn(
                modifier = Modifier
                    .padding(paddingVals)
                    .fillMaxSize()
                    .background(Color(0xFFF8FAFC))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Dashboard Welcome Header
                item {
                    Column {
                        Text(
                            text = "لوحة المراقبة والمؤشرات التنفيذية 📊",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0F172A)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "نظرة شمولية فورية على المبيعات، المدفوعات، وحالة المخزون الاستراتيجي.",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                // Quick Catalog Access Banner
                item {
                    Card(
                        onClick = onNavigateToCatalog,
                        colors = CardDefaults.cardColors(containerColor = BrandPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("catalog_access_banner_card")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    "إدارة كتالوج الأدوية الموحد 💊",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = OnBrandPrimary
                                )
                                Text(
                                    "إضافة وتعديل وحذف الأصناف والأسعار المركزية لجميع الفروع",
                                    fontSize = 11.sp,
                                    color = OnBrandPrimary.copy(alpha = 0.8f)
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = null,
                                tint = OnBrandPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Quick Cross-Branch Inventory Access Banner
                item {
                    Card(
                        onClick = onNavigateToCrossBranchInventory,
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0284C7)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("cross_branch_inventory_banner_card")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    "مراقبة مخزون الفروع الموحد 📦",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color.White
                                )
                                Text(
                                    "عرض ومراقبة كميات الأدوية في جميع الفروع وكشف العجز الفوري",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // 1. KPI Grid Card Grid (LazyVerticalGrid with 2 columns)
                item {
                    val kpis = emptyList<KpiItem>()

                    if (kpis.isEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "جاري تحميل المؤشرات...",
                                    fontSize = 13.sp,
                                    color = Color(0xFF94A3B8),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            userScrollEnabled = false
                        ) {
                            items(kpis.size) { index ->
                                val kpi = kpis[index]
                                Card(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .testTag(kpi.testTag),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, kpi.color.copy(alpha = 0.15f))
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(kpi.bgColor.copy(alpha = 0.3f))
                                            .padding(14.dp),
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(kpi.bgColor, shape = RoundedCornerShape(10.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = kpi.icon,
                                                contentDescription = null,
                                                tint = kpi.color,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Column {
                                            Text(
                                                text = kpi.title,
                                                fontSize = 11.sp,
                                                color = Color(0xFF64748B),
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                text = kpi.value,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = kpi.color,
                                                modifier = Modifier.padding(top = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Separator Header
                item {
                    Text(
                        text = "نظام التنبيهات العاجلة والمراقبة 🚨",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                // 2. Alerts Section: Critical Stock (⚠️ مخزون حرج)
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("alert_critical_stock_card"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = MedRedPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "⚠️ مخزون حرج",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MedRedPrimary
                                )
                            }

                            val criticalMeds = emptyList<String>()

                            if (criticalMeds.isEmpty()) {
                                Text(
                                    text = "لا توجد تنبيهات مخزون حرج حالياً",
                                    fontSize = 12.sp,
                                    color = Color(0xFF94A3B8),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    textAlign = TextAlign.Center
                                )
                            } else {
                                criticalMeds.forEach { medName ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFFFEF2F2), RoundedCornerShape(8.dp))
                                            .border(1.dp, Color(0xFFFEE2E2), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = medName,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1E293B)
                                        )
                                        Text(
                                            text = "الكمية المتبقية: 5 كراتين",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MedRedPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. Alerts Section: Defaulting Clients (🔴 عملاء متعثرون)
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("alert_defaulting_clients_card"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Error,
                                    contentDescription = null,
                                    tint = Color(0xFFEA580C),
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "🔴 عملاء متعثرون",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFEA580C)
                                )
                            }

                            val defaultingPharmacies = emptyList<String>()

                            if (defaultingPharmacies.isEmpty()) {
                                Text(
                                    text = "لا يوجد عملاء متعثرون حالياً",
                                    fontSize = 12.sp,
                                    color = Color(0xFF94A3B8),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    textAlign = TextAlign.Center
                                )
                            } else {
                                defaultingPharmacies.forEach { pharmacyName ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFFFFF7ED), RoundedCornerShape(8.dp))
                                            .border(1.dp, Color(0xFFFFEDD5), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = pharmacyName,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1E293B)
                                        )
                                        Text(
                                            text = "تجاوز السقف الائتماني بنسبة 95%",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFEA580C)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
