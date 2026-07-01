package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.User
import com.example.model.UserAddress
import com.example.service.FirebaseService
import com.example.ui.theme.MedBluePrimary
import com.example.ui.theme.MedGreenPrimary
import com.example.ui.theme.MedRedPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BranchAddressesScreen(
    currentUser: User,
    onNavigateBack: () -> Unit,
    onAddNewAddress: () -> Unit,
    onEditAddress: (UserAddress) -> Unit
) {
    val context = LocalContext.current
    var addressesList by remember { mutableStateOf<List<UserAddress>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Dialog state for delete confirmation
    var addressToDelete by remember { mutableStateOf<UserAddress?>(null) }

    // Fetch addresses on launch and when refreshed
    fun loadAddresses() {
        isLoading = true
        FirebaseService.getUserAddresses(currentUser.userId) { list ->
            addressesList = list.sortedByDescending { it.isDefault }
            isLoading = false
        }
    }

    LaunchedEffect(currentUser.userId) {
        loadAddresses()
    }

    // Force RTL local layout direction for Arabic experience
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "مواقع الفرع والمستودعات",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "رجوع",
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        TextButton(
                            onClick = onAddNewAddress,
                            modifier = Modifier.testTag("branch_add_address_top_btn")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "إضافة موقع",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MedBluePrimary)
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color(0xFFF8FAFC))
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MedBluePrimary
                    )
                } else if (addressesList.isEmpty()) {
                    // Empty State Screen
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warehouse,
                            contentDescription = null,
                            tint = Color.LightGray,
                            modifier = Modifier.size(100.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "لم تقم بإضافة أي موقع بعد",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.DarkGray,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "يمكنك إضافة أكثر من موقع لفروعك الفرعية أو مستودعاتك لتوجيه الطلبات وتسريع الأعمال.",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = onAddNewAddress,
                            colors = ButtonDefaults.buttonColors(containerColor = MedBluePrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("branch_add_first_address_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "إضافة أول موقع",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                } else {
                    // Addresses List State
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Small Helper Tip
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                            shape = RoundedCornerShape(0.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = MedBluePrimary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "يمكنك إضافة أكثر من موقع لفروعك الفرعية أو مستودعاتك وجعل أحدها كعنوان افتراضي.",
                                    fontSize = 12.sp,
                                    color = Color(0xFF1E40AF),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(addressesList, key = { it.addressId }) { address ->
                                BranchAddressCard(
                                    address = address,
                                    onEdit = { onEditAddress(address) },
                                    onSetDefault = {
                                        FirebaseService.setDefaultAddress(currentUser.userId, address.addressId) { success ->
                                            if (success) {
                                                // Sync defaults also inside branch table if we want the default location to be synced
                                                FirebaseService.updateBranchLocation(
                                                    branchId = currentUser.branchId,
                                                    address = address.fullAddress,
                                                    lat = address.latitude,
                                                    lng = address.longitude,
                                                    managerPhone = currentUser.phone
                                                ) { _ ->
                                                    Toast.makeText(context, "تم تعيين هذا العنوان كافتراضي للمستودعات", Toast.LENGTH_SHORT).show()
                                                    loadAddresses()
                                                }
                                            } else {
                                                Toast.makeText(context, "فشل تعيين العنوان الافتراضي", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    },
                                    onDelete = {
                                        addressToDelete = address
                                    }
                                )
                            }
                        }
                    }
                }

                // Delete Confirmation Dialog
                addressToDelete?.let { address ->
                    AlertDialog(
                        onDismissRequest = { addressToDelete = null },
                        title = {
                            Text(
                                text = "تأكيد الحذف",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        text = {
                            Text(
                                text = "هل أنت متأكد من حذف موقع الفرع \"${address.label}\"؟ لا يمكن التراجع عن هذا الإجراء.",
                                fontSize = 14.sp,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    val id = address.addressId
                                    addressToDelete = null
                                    FirebaseService.deleteUserAddress(id) { success ->
                                        if (success) {
                                            Toast.makeText(context, "تم حذف موقع الفرع بنجاح", Toast.LENGTH_SHORT).show()
                                            loadAddresses()
                                        } else {
                                            Toast.makeText(context, "فشل حذف موقع الفرع", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MedRedPrimary)
                            ) {
                                Text("حذف وتأكيد", fontWeight = FontWeight.Bold)
                            }
                        },
                        dismissButton = {
                            OutlinedButton(onClick = { addressToDelete = null }) {
                                Text("إلغاء", color = Color.Gray)
                            }
                        },
                        containerColor = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun BranchAddressCard(
    address: UserAddress,
    onEdit: () -> Unit,
    onSetDefault: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Card Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warehouse,
                        contentDescription = null,
                        tint = MedBluePrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = address.label,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.DarkGray
                    )
                }

                if (address.isDefault) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MedGreenPrimary.copy(alpha = 0.12f))
                            .border(1.dp, MedGreenPrimary, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "الافتراضي المعتمد",
                            color = MedGreenPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Divider(color = Color(0xFFF1F5F9), thickness = 1.dp)

            // Address Details
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Text(text = "🏢 ", fontSize = 14.sp)
                    Text(
                        text = "الشركة / الفرع: ",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = address.hospitalOrCompanyName,
                        fontSize = 13.sp,
                        color = Color.DarkGray
                    )
                }

                Row(verticalAlignment = Alignment.Top) {
                    Text(text = "📍 ", fontSize = 14.sp)
                    Text(
                        text = "الموقع: ",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                    val locationText = listOfNotNull(
                        address.governorate.takeIf { it.isNotEmpty() },
                        address.district.takeIf { it.isNotEmpty() },
                        address.neighborhood.takeIf { it.isNotEmpty() }
                    ).joinToString(" ⁃ ")
                    Text(
                        text = locationText,
                        fontSize = 13.sp,
                        color = Color.DarkGray
                    )
                }

                if (address.nearbyLandmark.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.Top) {
                        Text(text = "🗺️ ", fontSize = 14.sp)
                        Text(
                            text = "المعلم القريب: ",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = address.nearbyLandmark,
                            fontSize = 13.sp,
                            color = Color.DarkGray
                        )
                    }
                }

                Row(verticalAlignment = Alignment.Top) {
                    Text(text = "📝 ", fontSize = 14.sp)
                    Text(
                        text = "العنوان التفصيلي: ",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = address.fullAddress,
                        fontSize = 13.sp,
                        color = Color.DarkGray,
                        modifier = Modifier.weight(1f)
                    )
                }

                // GPS coordinates indicator
                Row(verticalAlignment = Alignment.Top) {
                    Text(text = "🌐 ", fontSize = 14.sp)
                    Text(
                        text = "الإحداثيات الجغرافية: ",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "${address.latitude}, ${address.longitude}",
                        fontSize = 13.sp,
                        color = Color.DarkGray
                    )
                }
            }

            Divider(color = Color(0xFFF1F5F9), thickness = 1.dp)

            // Action Buttons Row (3 horizontal buttons)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Edit Button
                OutlinedButton(
                    onClick = onEdit,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MedBluePrimary),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MedBluePrimary.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "تعديل",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "تعديل", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                // Default Button (only shown if isDefault is false)
                if (!address.isDefault) {
                    OutlinedButton(
                        onClick = onSetDefault,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MedGreenPrimary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MedGreenPrimary.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "افتراضي",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "افتراضي", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Delete Button
                OutlinedButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MedRedPrimary),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MedRedPrimary.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "حذف",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "حذف", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
