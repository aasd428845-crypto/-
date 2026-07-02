package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.service.FirebaseService
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    currentUser: User,
    cartItems: MutableList<CartItem>,
    onNavigateBack: () -> Unit,
    onCheckoutSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val subtotal = cartItems.sumOf { it.addedPrice * it.quantity }
    var isCheckingOut by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("سلة المشتريات 🛒", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("cart_back")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "رجوع", tint = Color.White)
                    }
                },
                actions = {
                    if (cartItems.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                cartItems.clear()
                                Toast.makeText(context, "تم إفراغ سلة المشتريات بالكامل", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.testTag("cart_clear_all")
                        ) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = "مسح الكل", tint = Color.White)
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MedBluePrimary)
            )
        },
        bottomBar = {
            if (cartItems.isNotEmpty()) {
                Surface(
                    tonalElevation = 8.dp,
                    shadowElevation = 8.dp,
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // تفصيل الحساب المالي المتقدم
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("الإجمالي الفرعي:", color = Color.Gray, fontSize = 13.sp)
                            Text("$subtotal YER", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("الضرائب الطبية المضافة (0%):", color = Color.Gray, fontSize = 12.sp)
                            Text("0 YER", color = Color.Gray, fontSize = 12.sp)
                        }

                        Divider(color = Color.LightGray.copy(alpha = 0.5f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("المبلغ الإجمالي المستحق:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(
                                "$subtotal YER",
                                color = MedBluePrimary,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp
                            )
                        }

                        // معلومات الحساب الائتماني المتاح للعميل
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF0FDF4), RoundedCornerShape(6.dp))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.CreditCard, contentDescription = null, tint = MedGreenPrimary, modifier = Modifier.size(16.dp))
                            Text(
                                text = "السقف الائتماني المتوفر: ${currentUser.clientAccount.creditLimit - currentUser.clientAccount.currentBalance} YER (${currentUser.clientAccount.paymentTerms})",
                                color = MedGreenPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Button(
                            onClick = {
                                if (isCheckingOut) return@Button
                                isCheckingOut = true

                                // بناء أسطر الطلب (Order Lines) لضمان الشحن الجزئي
                                val lines = cartItems.mapIndexed { index, item ->
                                    OrderLine(
                                        lineId = "line_${System.currentTimeMillis()}_$index",
                                        product = item.product,
                                        requestedQty = item.quantity,
                                        shippedQty = 0, // شحن جزئي لم يبدأ بعد
                                        unitPrice = item.addedPrice,
                                        totalPrice = item.addedPrice * item.quantity
                                    )
                                }

                                // إنشاء كائن الطلبية الشامل مع الحقول القديمة للتوافق المطلق
                                val order = Order(
                                    orderId = "order_" + System.currentTimeMillis(),
                                    clientId = currentUser.userId,
                                    orderLines = lines,
                                    orderStatus = OrderStatus.Submitted,
                                    totalAmount = subtotal,
                                    deliveryRouteId = if (currentUser.clientAccount.paymentTerms == PaymentTerms.NET30) "route_main" else "",
                                    createdAt = System.currentTimeMillis(),

                                    // حقول التوافقية الرجعية (Backward Compatibility Layer)
                                    clientName = currentUser.orgName.ifEmpty { currentUser.name },
                                    clientType = currentUser.clientType.ifEmpty { "pharmacy" },
                                    clientGovernorate = currentUser.governorate,
                                    orderContent = lines.joinToString(", ") { "${it.product.commercialName} (x${it.requestedQty})" },
                                    status = "broadcast",
                                    broadcastType = "all",
                                    urgencyLevel = "normal",
                                    medicineName = lines.firstOrNull()?.product?.commercialName ?: "",
                                    price = lines.firstOrNull()?.unitPrice ?: 0.0,
                                    quantity = lines.firstOrNull()?.requestedQty ?: 0
                                )

                                // الحفظ في قاعدة البيانات الافتراضية للفيربيز
                                FirebaseService.submitOrder(
                                    order = order,
                                    onSuccess = {
                                        cartItems.clear()
                                        isCheckingOut = false
                                        Toast.makeText(context, "🎉 تم إرسال طلب الشراء اللوجستي بنجاح إلى الفروع!", Toast.LENGTH_LONG).show()
                                        onCheckoutSuccess()
                                    },
                                    onFailure = { err ->
                                        isCheckingOut = false
                                        Toast.makeText(context, "خطأ أثناء إرسال الطلب: $err", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary, contentColor = OnBrandPrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("checkout_confirm_btn"),
                            shape = RoundedCornerShape(8.dp),
                            enabled = !isCheckingOut
                        ) {
                            if (isCheckingOut) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
                                    Text("تأكيد وإرسال طلب الشراء اللوجستي 🚀", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { paddingVals ->
        if (cartItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .padding(paddingVals)
                    .fillMaxSize()
                    .background(Color(0xFFF8FAFC)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.RemoveShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.LightGray
                    )
                    Text("سلة المشتريات فارغة تماماً", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 14.sp)
                    Text("اذهب إلى كتالوج الأدوية لإضافة احتياجات منشأتك الصحية.", color = Color.Gray, fontSize = 11.sp, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onNavigateBack,
                        colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary, contentColor = OnBrandPrimary),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("الذهاب للكتالوج ➡️", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(paddingVals)
                    .fillMaxSize()
                    .background(Color(0xFFF1F5F9)),
                contentPadding = PaddingValues(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(cartItems) { index, item ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(10.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("cart_item_$index")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    item.product.commercialName,
                                    fontWeight = FontWeight.Bold,
                                    color = MedBluePrimary,
                                    fontSize = 13.sp
                                )
                                Text(
                                    "الاسم العلمي: ${item.product.scientificName}",
                                    color = Color.Gray,
                                    fontSize = 10.sp
                                )
                                Text(
                                    "السعر الفردي: ${item.addedPrice} YER",
                                    color = Color.Gray,
                                    fontSize = 10.sp
                                )
                                Text(
                                    "الإجمالي: ${item.addedPrice * item.quantity} YER",
                                    color = MedGreenPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // زر إنقاص الكمية (-)
                                IconButton(
                                    onClick = {
                                        if (item.quantity > 1) {
                                            cartItems[index] = item.copy(quantity = item.quantity - 1)
                                        } else {
                                            cartItems.removeAt(index)
                                        }
                                    },
                                    modifier = Modifier
                                        .size(28.dp)
                                        .background(Color(0xFFF1F5F9), CircleShape)
                                        .testTag("cart_dec_btn_$index")
                                ) {
                                    Icon(
                                        Icons.Default.Remove,
                                        contentDescription = "إنقاص الكمية",
                                        tint = Color.DarkGray,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }

                                Text(
                                    text = item.quantity.toString(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )

                                // زر زيادة الكمية (+)
                                IconButton(
                                    onClick = {
                                        cartItems[index] = item.copy(quantity = item.quantity + 1)
                                    },
                                    modifier = Modifier
                                        .size(28.dp)
                                        .background(Color(0xFFF1F5F9), CircleShape)
                                        .testTag("cart_inc_btn_$index")
                                ) {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = "زيادة الكمية",
                                        tint = Color.DarkGray,
                                        modifier = Modifier.size(14.dp)
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
