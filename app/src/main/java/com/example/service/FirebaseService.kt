package com.example.service

import android.util.Log
import com.example.model.*
import kotlin.math.*
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object FirebaseService {
    private val scope = CoroutineScope(Dispatchers.IO)

    var currentUserSession: User? = null

    var lastDatabaseError: String? = null

    // --- Authentication ---
    fun getCurrentUser(onResult: (User?) -> Unit) {
        scope.launch {
            try {
                val sessionUser = SupabaseClientProvider.client.auth.currentUserOrNull()
                if (sessionUser != null) {
                    val user = withContext(Dispatchers.IO) {
                        SupabaseClientProvider.client.postgrest["users"]
                            .select {
                                filter {
                                    eq("id", sessionUser.id)
                                }
                            }.decodeList<User>().firstOrNull()
                    }
                    if (user != null) {
                        currentUserSession = user
                        withContext(Dispatchers.Main) { onResult(user) }
                    } else {
                        withContext(Dispatchers.Main) { onResult(currentUserSession) }
                    }
                } else {
                    withContext(Dispatchers.Main) { onResult(currentUserSession) }
                }
            } catch (e: Exception) {
                Log.e("SUPABASE_DEBUG", "getCurrentUser failed: ${e.message} | ${e.stackTraceToString()}")
                lastDatabaseError = "getCurrentUser: ${e.message}"
                withContext(Dispatchers.Main) { onResult(currentUserSession) }
            }
        }
    }

    fun loginUser(email: String, password: String, onResult: (User?, String?) -> Unit) {
        scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    SupabaseClientProvider.client.auth.signInWith(Email) {
                        this.email = email.trim()
                        this.password = password
                    }
                }
                val uid = SupabaseClientProvider.client.auth.currentUserOrNull()?.id
                if (uid == null) {
                    withContext(Dispatchers.Main) { onResult(null, "فشل تسجيل الدخول") }
                    return@launch
                }
                val user = withContext(Dispatchers.IO) {
                    SupabaseClientProvider.client.postgrest["users"]
                        .select { filter { eq("id", uid) } }
                        .decodeSingleOrNull<User>()
                }
                currentUserSession = user
                withContext(Dispatchers.Main) {
                    onResult(user, if (user == null) "تعذر إيجاد بيانات الحساب في قاعدة البيانات" else null)
                }
            } catch (e: Exception) {
                Log.e("SUPABASE_DEBUG", "loginUser failed: ${e.message} | ${e.stackTraceToString()}")
                withContext(Dispatchers.Main) { onResult(null, "بيانات الدخول غير صحيحة") }
            }
        }
    }

    fun registerUser(
        email: String,
        password: String,
        name: String,
        role: String,
        extraFields: Map<String, String> = emptyMap(),
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    SupabaseClientProvider.client.auth.signUpWith(Email) {
                        this.email = email.trim()
                        this.password = password
                        data = buildJsonObject {
                            put("name", name)
                            put("role", role)
                            extraFields.forEach { (k, v) -> put(k, v) }
                        }
                    }
                }
                // لا تُدرج أي صف يدوياً بجدول users — الـ trigger handle_new_user بقاعدة البيانات
                // ينشئ الصف تلقائياً فور نجاح signUpWith أعلاه.
                withContext(Dispatchers.Main) { onSuccess() }
            } catch (e: Exception) {
                Log.e("SUPABASE_DEBUG", "registerUser failed: ${e.message} | ${e.stackTraceToString()}")
                withContext(Dispatchers.Main) { onFailure(e.message ?: "فشل إنشاء الحساب") }
            }
        }
    }

    // --- Addresses ---
    fun getUserAddresses(userId: String, onResult: (List<UserAddress>) -> Unit) {
        scope.launch {
            try {
                val list = SupabaseClientProvider.client.postgrest["addresses"]
                    .select {
                        filter {
                            eq("user_id", userId)
                        }
                    }.decodeList<UserAddress>()
                withContext(Dispatchers.Main) {
                    onResult(list)
                }
            } catch (e: Exception) {
                Log.e("SUPABASE_DEBUG", "getUserAddresses failed: ${e.message} | ${e.stackTraceToString()}")
                withContext(Dispatchers.Main) {
                    onResult(emptyList())
                }
            }
        }
    }

    fun getAllAddresses(onResult: (List<UserAddress>) -> Unit) {
        scope.launch {
            try {
                val list = SupabaseClientProvider.client.postgrest["addresses"]
                    .select()
                    .decodeList<UserAddress>()
                withContext(Dispatchers.Main) {
                    onResult(list)
                }
            } catch (e: Exception) {
                Log.e("SUPABASE_DEBUG", "getAllAddresses failed: ${e.message} | ${e.stackTraceToString()}")
                withContext(Dispatchers.Main) {
                    onResult(emptyList())
                }
            }
        }
    }

    fun saveAddress(address: UserAddress, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val id = if (address.addressId.isEmpty()) "addr_" + System.currentTimeMillis() else address.addressId
        val finalAddr = address.copy(addressId = id, createdAt = System.currentTimeMillis())

        scope.launch {
            try {
                if (finalAddr.isDefault) {
                    try {
                        SupabaseClientProvider.client.postgrest["addresses"]
                            .update({
                                set("is_default", false)
                            }) {
                                filter {
                                    eq("user_id", finalAddr.userId)
                                }
                            }
                    } catch (ex: Exception) {
                        Log.e("SUPABASE_DEBUG", "saveAddress resetting defaults warning: ${ex.message}")
                    }
                }

                SupabaseClientProvider.client.postgrest["addresses"].upsert(finalAddr)

                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            } catch (e: Exception) {
                Log.e("SUPABASE_DEBUG", "saveAddress failed: ${e.message} | ${e.stackTraceToString()}")
                lastDatabaseError = "saveAddress: ${e.message}"
                withContext(Dispatchers.Main) {
                    onFailure("خطأ في حفظ العنوان: ${e.message}")
                }
            }
        }
    }

    fun setDefaultAddress(userId: String, addressId: String, onResult: (Boolean) -> Unit) {
        scope.launch {
            try {
                SupabaseClientProvider.client.postgrest["addresses"]
                    .update({
                        set("is_default", false)
                    }) {
                        filter {
                            eq("user_id", userId)
                        }
                    }

                SupabaseClientProvider.client.postgrest["addresses"]
                    .update({
                        set("is_default", true)
                    }) {
                        filter {
                            eq("id", addressId)
                        }
                    }

                withContext(Dispatchers.Main) {
                    onResult(true)
                }
            } catch (e: Exception) {
                Log.e("SUPABASE_DEBUG", "setDefaultAddress failed: ${e.message} | ${e.stackTraceToString()}")
                withContext(Dispatchers.Main) {
                    onResult(false)
                }
            }
        }
    }

    fun addUserAddress(address: UserAddress, onResult: (Boolean, String?) -> Unit) {
        val id = if (address.addressId.isEmpty()) "addr_" + System.currentTimeMillis() else address.addressId
        val finalAddr = address.copy(addressId = id, createdAt = System.currentTimeMillis())

        scope.launch {
            try {
                if (finalAddr.isDefault) {
                    try {
                        SupabaseClientProvider.client.postgrest["addresses"]
                            .update({
                                set("is_default", false)
                            }) {
                                filter {
                                    eq("user_id", finalAddr.userId)
                                }
                            }
                    } catch (ex: Exception) {
                        Log.e("SUPABASE_DEBUG", "addUserAddress resetting defaults warning: ${ex.message}")
                    }
                }

                SupabaseClientProvider.client.postgrest["addresses"].upsert(finalAddr)

                withContext(Dispatchers.Main) {
                    onResult(true, id)
                }
            } catch (e: Exception) {
                Log.e("SUPABASE_DEBUG", "addUserAddress failed: ${e.message} | ${e.stackTraceToString()}")
                lastDatabaseError = "addUserAddress: ${e.message}"
                withContext(Dispatchers.Main) {
                    onResult(false, "خطأ في إضافة العنوان: ${e.message}")
                }
            }
        }
    }

    fun updateUserAddress(address: UserAddress, onResult: (Boolean) -> Unit) {
        scope.launch {
            try {
                if (address.isDefault) {
                    try {
                        SupabaseClientProvider.client.postgrest["addresses"]
                            .update({
                                set("is_default", false)
                            }) {
                                filter {
                                    eq("user_id", address.userId)
                                }
                            }
                    } catch (ex: Exception) {
                        Log.e("SUPABASE_DEBUG", "updateUserAddress resetting defaults warning: ${ex.message}")
                    }
                }

                SupabaseClientProvider.client.postgrest["addresses"].upsert(address)

                withContext(Dispatchers.Main) {
                    onResult(true)
                }
            } catch (e: Exception) {
                Log.e("SUPABASE_DEBUG", "updateUserAddress failed: ${e.message} | ${e.stackTraceToString()}")
                withContext(Dispatchers.Main) {
                    onResult(false)
                }
            }
        }
    }

    fun deleteUserAddress(addressId: String, onResult: (Boolean) -> Unit) {
        scope.launch {
            try {
                SupabaseClientProvider.client.postgrest["addresses"]
                    .delete {
                        filter {
                            eq("id", addressId)
                        }
                    }
                withContext(Dispatchers.Main) {
                    onResult(true)
                }
            } catch (e: Exception) {
                Log.e("SUPABASE_DEBUG", "deleteUserAddress failed: ${e.message} | ${e.stackTraceToString()}")
                withContext(Dispatchers.Main) {
                    onResult(false)
                }
            }
        }
    }

    // --- Bank Accounts ---
    fun getBankAccounts(userId: String, onResult: (List<BankAccount>) -> Unit) {
        scope.launch {
            try {
                val list = SupabaseClientProvider.client.postgrest["bank_accounts"]
                    .select {
                        filter {
                            eq("user_id", userId)
                        }
                    }.decodeList<BankAccount>()
                withContext(Dispatchers.Main) {
                    onResult(list)
                }
            } catch (e: Exception) {
                Log.e("SUPABASE_DEBUG", "getBankAccounts failed: ${e.message} | ${e.stackTraceToString()}")
                withContext(Dispatchers.Main) {
                    onResult(emptyList())
                }
            }
        }
    }

    fun saveBankAccount(account: BankAccount, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val id = if (account.accountId.isEmpty()) "acc_" + System.currentTimeMillis() else account.accountId
        val finalAcc = account.copy(accountId = id, createdAt = System.currentTimeMillis())

        scope.launch {
            try {
                if (finalAcc.isDefault) {
                    try {
                        SupabaseClientProvider.client.postgrest["bank_accounts"]
                            .update({
                                set("is_default", false)
                            }) {
                                filter {
                                    eq("user_id", finalAcc.userId)
                                }
                            }
                    } catch (ex: Exception) {
                        Log.e("SUPABASE_DEBUG", "saveBankAccount resetting defaults warning: ${ex.message}")
                    }
                }

                SupabaseClientProvider.client.postgrest["bank_accounts"].upsert(finalAcc)

                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            } catch (e: Exception) {
                Log.e("SUPABASE_DEBUG", "saveBankAccount failed: ${e.message} | ${e.stackTraceToString()}")
                lastDatabaseError = "saveBankAccount: ${e.message}"
                withContext(Dispatchers.Main) {
                    onFailure("خطأ في حفظ الحساب البنكي: ${e.message}")
                }
            }
        }
    }

    fun setDefaultBankAccount(userId: String, accountId: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        scope.launch {
            try {
                SupabaseClientProvider.client.postgrest["bank_accounts"]
                    .update({
                        set("is_default", false)
                    }) {
                        filter {
                            eq("user_id", userId)
                        }
                    }

                SupabaseClientProvider.client.postgrest["bank_accounts"]
                    .update({
                        set("is_default", true)
                    }) {
                        filter {
                            eq("id", accountId)
                        }
                    }

                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            } catch (e: Exception) {
                Log.e("SUPABASE_DEBUG", "setDefaultBankAccount failed: ${e.message} | ${e.stackTraceToString()}")
                withContext(Dispatchers.Main) {
                    onFailure("خطأ في تعيين الحساب الافتراضي: ${e.message}")
                }
            }
        }
    }

    // --- Medicines & Offers ---
    fun getMedicines(onResult: (List<Medicine>) -> Unit) {
        scope.launch {
            try {
                val list = SupabaseClientProvider.client.postgrest["medicines"]
                    .select()
                    .decodeList<Medicine>()
                withContext(Dispatchers.Main) {
                    onResult(list)
                }
            } catch (e: Exception) {
                Log.e("SUPABASE_DEBUG", "getMedicines failed: ${e.message} | ${e.stackTraceToString()}")
                withContext(Dispatchers.Main) {
                    onResult(emptyList())
                }
            }
        }
    }

    fun getSuppliers(onResult: (List<User>) -> Unit) {
        scope.launch {
            try {
                val list = SupabaseClientProvider.client.postgrest["users"]
                    .select {
                        filter {
                            eq("role", "supplier")
                        }
                    }.decodeList<User>()
                withContext(Dispatchers.Main) {
                    onResult(list)
                }
            } catch (e: Exception) {
                Log.e("SUPABASE_DEBUG", "getSuppliers failed: ${e.message} | ${e.stackTraceToString()}")
                withContext(Dispatchers.Main) {
                    onResult(emptyList())
                }
            }
        }
    }

    fun getPriceOffers(userId: String, onResult: (List<PriceOffer>) -> Unit) {
        scope.launch {
            try {
                val list = SupabaseClientProvider.client.postgrest["price_offers"]
                    .select {
                        filter {
                            eq("supplier_id", userId)
                        }
                    }.decodeList<PriceOffer>()
                withContext(Dispatchers.Main) {
                    onResult(list)
                }
            } catch (e: Exception) {
                Log.e("SUPABASE_DEBUG", "getPriceOffers failed: ${e.message} | ${e.stackTraceToString()}")
                withContext(Dispatchers.Main) {
                    onResult(emptyList())
                }
            }
        }
    }

    fun updatePriceOfferStatus(offerId: String, newStatus: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        scope.launch {
            try {
                SupabaseClientProvider.client.postgrest["price_offers"]
                    .update({
                        set("status", newStatus)
                    }) {
                        filter {
                            eq("id", offerId)
                        }
                    }
                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            } catch (e: Exception) {
                Log.e("SUPABASE_DEBUG", "updatePriceOfferStatus failed: ${e.message} | ${e.stackTraceToString()}")
                withContext(Dispatchers.Main) {
                    onFailure("خطأ في تحديث حالة العرض: ${e.message}")
                }
            }
        }
    }

    fun updatePriceOfferWithDelivery(
        offerId: String,
        address: UserAddress,
        distance: Double,
        eta: String,
        deliveryMethod: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        scope.launch {
            try {
                SupabaseClientProvider.client.postgrest["price_offers"]
                    .update({
                        set("status", "accepted")
                        set("delivery_address_id", address.addressId)
                        set("delivery_address_label", address.label)
                        set("delivery_full_address", address.fullAddress)
                        set("distance", distance)
                        set("eta", eta)
                        set("delivery_method", deliveryMethod)
                    }) {
                        filter {
                            eq("id", offerId)
                        }
                    }

                try {
                    val offerList = SupabaseClientProvider.client.postgrest["price_offers"]
                        .select {
                            filter { eq("id", offerId) }
                        }.decodeList<PriceOffer>()
                    val offer = offerList.firstOrNull()
                    if (offer != null) {
                        val newOrder = Order(
                            orderId = "order_${offer.priceOfferId}",
                            priceOfferId = offer.priceOfferId,
                            hospitalId = currentUserSession?.userId ?: "",
                            supplierId = offer.supplierId,
                            medicineName = offer.medicineName,
                            price = offer.price,
                            quantity = offer.quantity,
                            deliveryMethod = deliveryMethod,
                            status = "pending",
                            createdAt = System.currentTimeMillis()
                        )
                        SupabaseClientProvider.client.postgrest["orders"].upsert(newOrder)
                    }
                } catch (ex: Exception) {
                    Log.e("SUPABASE_DEBUG", "updatePriceOfferWithDelivery order creation warning: ${ex.message}")
                }

                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            } catch (e: Exception) {
                Log.e("SUPABASE_DEBUG", "updatePriceOfferWithDelivery failed: ${e.message} | ${e.stackTraceToString()}")
                withContext(Dispatchers.Main) {
                    onFailure("خطأ في تحديث عرض السعر: ${e.message}")
                }
            }
        }
    }

    // --- Payments ---
    fun submitPayment(payment: Payment, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        scope.launch {
            try {
                val id = "pay_" + System.currentTimeMillis()
                val finalPay = payment.copy(paymentId = id)
                SupabaseClientProvider.client.postgrest["payments"].upsert(finalPay)

                try {
                    SupabaseClientProvider.client.postgrest["orders"]
                        .update({
                            set("status", "paid")
                        }) {
                            filter {
                                eq("id", payment.orderId)
                            }
                        }
                } catch (ex: Exception) {
                    Log.e("SUPABASE_DEBUG", "submitPayment order update warning: ${ex.message}")
                }

                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            } catch (e: Exception) {
                Log.e("SUPABASE_DEBUG", "submitPayment failed: ${e.message} | ${e.stackTraceToString()}")
                withContext(Dispatchers.Main) {
                    onFailure("خطأ في إرسال الدفع: ${e.message}")
                }
            }
        }
    }

    fun getPayments(onResult: (List<Payment>) -> Unit) {
        scope.launch {
            try {
                val list = SupabaseClientProvider.client.postgrest["payments"]
                    .select()
                    .decodeList<Payment>()
                withContext(Dispatchers.Main) {
                    onResult(list)
                }
            } catch (e: Exception) {
                Log.e("SUPABASE_DEBUG", "getPayments failed: ${e.message} | ${e.stackTraceToString()}")
                withContext(Dispatchers.Main) {
                    onResult(emptyList())
                }
            }
        }
    }

    fun updatePaymentStatus(paymentId: String, newStatus: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        scope.launch {
            try {
                SupabaseClientProvider.client.postgrest["payments"]
                    .update({
                        set("status", newStatus)
                    }) {
                        filter {
                            eq("id", paymentId)
                        }
                    }
                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            } catch (e: Exception) {
                Log.e("SUPABASE_DEBUG", "updatePaymentStatus failed: ${e.message} | ${e.stackTraceToString()}")
                withContext(Dispatchers.Main) {
                    onFailure("خطأ في تحديث حالة الدفع: ${e.message}")
                }
            }
        }
    }

    // --- Delivery Requests (Platform Shipment) ---
    fun submitDeliveryRequest(request: DeliveryRequest, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        scope.launch {
            try {
                val id = "del_" + System.currentTimeMillis()
                val finalReq = request.copy(deliveryId = id, createdAt = System.currentTimeMillis())
                SupabaseClientProvider.client.postgrest["delivery_requests"].upsert(finalReq)
                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            } catch (e: Exception) {
                Log.e("SUPABASE_DEBUG", "submitDeliveryRequest failed: ${e.message} | ${e.stackTraceToString()}")
                withContext(Dispatchers.Main) {
                    onFailure("خطأ في إرسال طلب التوصيل: ${e.message}")
                }
            }
        }
    }

    fun getDeliveryRequests(onResult: (List<DeliveryRequest>) -> Unit) {
        scope.launch {
            try {
                val list = SupabaseClientProvider.client.postgrest["delivery_requests"]
                    .select()
                    .decodeList<DeliveryRequest>()
                withContext(Dispatchers.Main) {
                    onResult(list)
                }
            } catch (e: Exception) {
                Log.e("SUPABASE_DEBUG", "getDeliveryRequests failed: ${e.message} | ${e.stackTraceToString()}")
                withContext(Dispatchers.Main) {
                    onResult(emptyList())
                }
            }
        }
    }

    fun assignDeliveryDriver(deliveryId: String, driverName: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        scope.launch {
            try {
                SupabaseClientProvider.client.postgrest["delivery_requests"]
                    .update({
                        set("status", "assigned")
                        set("admin_assigned", true)
                    }) {
                        filter {
                            eq("id", deliveryId)
                        }
                    }
                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            } catch (e: Exception) {
                Log.e("SUPABASE_DEBUG", "assignDeliveryDriver failed: ${e.message} | ${e.stackTraceToString()}")
                withContext(Dispatchers.Main) {
                    onFailure("خطأ في تعيين سائق التوصيل: ${e.message}")
                }
            }
        }
    }

    // --- Delivery Schedules ---
    fun getDeliverySchedule(orderId: String, onResult: (DeliverySchedule?) -> Unit) {
        scope.launch {
            try {
                val list = SupabaseClientProvider.client.postgrest["delivery_schedules"]
                    .select {
                        filter {
                            eq("order_id", orderId)
                        }
                    }.decodeList<DeliverySchedule>()
                withContext(Dispatchers.Main) {
                    onResult(list.firstOrNull())
                }
            } catch (e: Exception) {
                Log.e("SUPABASE_DEBUG", "getDeliverySchedule failed: ${e.message} | ${e.stackTraceToString()}")
                withContext(Dispatchers.Main) {
                    onResult(null)
                }
            }
        }
    }

    fun saveDeliverySchedule(schedule: DeliverySchedule, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        scope.launch {
            try {
                val sId = if (schedule.scheduleId.isEmpty()) "sched_" + System.currentTimeMillis() else schedule.scheduleId
                val finalSched = schedule.copy(scheduleId = sId, updatedAt = System.currentTimeMillis())
                SupabaseClientProvider.client.postgrest["delivery_schedules"].upsert(finalSched)

                if (schedule.status == "agreed") {
                    try {
                        SupabaseClientProvider.client.postgrest["orders"]
                            .update({
                                set("scheduled_delivery_date", schedule.agreedDateTime)
                            }) {
                                filter {
                                    eq("id", schedule.orderId)
                                }
                            }
                    } catch (ex: Exception) {
                        Log.e("SUPABASE_DEBUG", "saveDeliverySchedule order update warning: ${ex.message}")
                    }
                }
                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            } catch (e: Exception) {
                Log.e("SUPABASE_DEBUG", "saveDeliverySchedule failed: ${e.message} | ${e.stackTraceToString()}")
                withContext(Dispatchers.Main) {
                    onFailure("خطأ في حفظ جدول التوصيل: ${e.message}")
                }
            }
        }
    }

    fun getOrders(onResult: (List<Order>) -> Unit) {
        scope.launch {
            try {
                val list = SupabaseClientProvider.client.postgrest["orders"]
                    .select()
                    .decodeList<Order>()
                withContext(Dispatchers.Main) {
                    onResult(list)
                }
            } catch (e: Exception) {
                Log.e("SUPABASE_DEBUG", "getOrders failed: ${e.message} | ${e.stackTraceToString()}")
                withContext(Dispatchers.Main) {
                    onResult(emptyList())
                }
            }
        }
    }

    fun updateOrderStatus(orderId: String, newStatus: String) {
        scope.launch {
            try {
                SupabaseClientProvider.client.postgrest["orders"]
                    .update({
                        set("status", newStatus)
                    }) {
                        filter {
                            eq("id", orderId)
                        }
                    }
            } catch (e: Exception) {
                Log.e("SUPABASE_DEBUG", "updateOrderStatus failed: ${e.message} | ${e.stackTraceToString()}")
            }
        }
    }

    fun getCompanyInfo(onResult: (CompanyInfo) -> Unit) {
        scope.launch {
            try {
                val list = SupabaseClientProvider.client.postgrest["company_info"]
                    .select()
                    .decodeList<CompanyInfo>()
                withContext(Dispatchers.Main) {
                    val info = list.firstOrNull()
                    if (info != null) {
                        onResult(info)
                    } else {
                        onResult(CompanyInfo())
                    }
                }
            } catch (e: Exception) {
                Log.e("SUPABASE_DEBUG", "getCompanyInfo failed: ${e.message} | ${e.stackTraceToString()}")
                withContext(Dispatchers.Main) {
                    onResult(CompanyInfo())
                }
            }
        }
    }

    fun getBranches(onResult: (List<Branch>) -> Unit) {
        scope.launch {
            try {
                val list = SupabaseClientProvider.client.postgrest["branches"]
                    .select()
                    .decodeList<Branch>()
                withContext(Dispatchers.Main) {
                    onResult(list)
                }
            } catch (e: Exception) {
                Log.e("SUPABASE_DEBUG", "getBranches failed: ${e.message} | ${e.stackTraceToString()}")
                withContext(Dispatchers.Main) {
                    onResult(emptyList())
                }
            }
        }
    }

    fun submitOrder(order: Order, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val finalOrder = order.copy(
            orderId = if (order.orderId.isEmpty()) "order_" + System.currentTimeMillis() else order.orderId,
            createdAt = if (order.createdAt == 0L) System.currentTimeMillis() else order.createdAt
        )
        scope.launch {
            try {
                SupabaseClientProvider.client.postgrest["orders"].upsert(finalOrder)
                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            } catch (e: Exception) {
                Log.e("SUPABASE_DEBUG", "submitOrder failed: ${e.message} | ${e.stackTraceToString()}")
                withContext(Dispatchers.Main) {
                    onFailure("خطأ في إرسال الطلب: ${e.message}")
                }
            }
        }
    }

    fun getBranchOffersForOrder(orderId: String, onResult: (List<BranchOffer>) -> Unit) {
        scope.launch {
            try {
                val list = SupabaseClientProvider.client.postgrest["branch_offers"]
                    .select {
                        filter {
                            eq("order_id", orderId)
                        }
                    }.decodeList<BranchOffer>()
                withContext(Dispatchers.Main) {
                    onResult(list)
                }
            } catch (e: Exception) {
                Log.e("SUPABASE_DEBUG", "getBranchOffersForOrder failed: ${e.message} | ${e.stackTraceToString()}")
                withContext(Dispatchers.Main) {
                    onResult(emptyList())
                }
            }
        }
    }

    fun getAllBranchOffers(onResult: (List<BranchOffer>) -> Unit) {
        scope.launch {
            try {
                val list = SupabaseClientProvider.client.postgrest["branch_offers"]
                    .select()
                    .decodeList<BranchOffer>()
                withContext(Dispatchers.Main) {
                    onResult(list)
                }
            } catch (e: Exception) {
                Log.e("SUPABASE_DEBUG", "getAllBranchOffers failed: ${e.message} | ${e.stackTraceToString()}")
                withContext(Dispatchers.Main) {
                    onResult(emptyList())
                }
            }
        }
    }

    fun submitBranchOffer(offer: BranchOffer, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val finalOffer = offer.copy(
            offerId = if (offer.offerId.isEmpty()) "offer_" + System.currentTimeMillis() else offer.offerId,
            createdAt = if (offer.createdAt == 0L) System.currentTimeMillis() else offer.createdAt
        )
        scope.launch {
            try {
                SupabaseClientProvider.client.postgrest["branch_offers"].upsert(finalOffer)

                try {
                    SupabaseClientProvider.client.postgrest["orders"]
                        .update({
                            set("status", "offer_received")
                        }) {
                            filter {
                                eq("id", offer.orderId)
                            }
                        }
                } catch (ex: Exception) {
                    Log.e("SUPABASE_DEBUG", "submitBranchOffer order update warning: ${ex.message}")
                }

                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            } catch (e: Exception) {
                Log.e("SUPABASE_DEBUG", "submitBranchOffer failed: ${e.message} | ${e.stackTraceToString()}")
                withContext(Dispatchers.Main) {
                    onFailure("خطأ في إرسال عرض الفرع: ${e.message}")
                }
            }
        }
    }

    fun updateBranchOfferStatus(offerId: String, newStatus: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        scope.launch {
            try {
                SupabaseClientProvider.client.postgrest["branch_offers"]
                    .update({
                        set("status", newStatus)
                    }) {
                        filter {
                            eq("id", offerId)
                        }
                    }

                if (newStatus == "accepted") {
                    try {
                        val offerList = SupabaseClientProvider.client.postgrest["branch_offers"]
                            .select {
                                filter { eq("id", offerId) }
                            }.decodeList<BranchOffer>()
                        val offer = offerList.firstOrNull()
                        if (offer != null) {
                            SupabaseClientProvider.client.postgrest["orders"]
                                .update({
                                    set("status", "confirmed")
                                }) {
                                    filter {
                                        eq("id", offer.orderId)
                                    }
                                }
                        }
                    } catch (ex: Exception) {
                        Log.e("SUPABASE_DEBUG", "updateBranchOfferStatus order update warning: ${ex.message}")
                    }
                }

                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            } catch (e: Exception) {
                Log.e("SUPABASE_DEBUG", "updateBranchOfferStatus failed: ${e.message} | ${e.stackTraceToString()}")
                withContext(Dispatchers.Main) {
                    onFailure("خطأ في تحديث حالة عرض الفرع: ${e.message}")
                }
            }
        }
    }

    fun getBranchOrders(branchId: String, onResult: (List<Order>) -> Unit) {
        scope.launch {
            try {
                val list = SupabaseClientProvider.client.postgrest["orders"]
                    .select()
                    .decodeList<Order>()
                val filtered = list.filter { it.targetBranches.contains(branchId) || it.targetBranches.isEmpty() }
                withContext(Dispatchers.Main) {
                    onResult(filtered)
                }
            } catch (e: Exception) {
                Log.e("SUPABASE_DEBUG", "getBranchOrders failed: ${e.message} | ${e.stackTraceToString()}")
                withContext(Dispatchers.Main) {
                    onResult(emptyList())
                }
            }
        }
    }

    fun sendBranchOffer(offer: BranchOffer, onResult: (Boolean) -> Unit) {
        submitBranchOffer(offer, { onResult(true) }, { onResult(false) })
    }

    fun acceptBranchOffer(orderId: String, offerId: String, onResult: (Boolean) -> Unit) {
        updateBranchOfferStatus(offerId, "accepted", { onResult(true) }, { onResult(false) })
    }

    fun rejectBranchOffer(offerId: String, reason: String, onResult: (Boolean) -> Unit) {
        scope.launch {
            try {
                SupabaseClientProvider.client.postgrest["branch_offers"]
                    .update({
                        set("status", "rejected")
                        set("notes", "سبب الرفض: $reason")
                    }) {
                        filter {
                            eq("id", offerId)
                        }
                    }
                withContext(Dispatchers.Main) {
                    onResult(true)
                }
            } catch (e: Exception) {
                Log.e("SUPABASE_DEBUG", "rejectBranchOffer failed: ${e.message} | ${e.stackTraceToString()}")
                withContext(Dispatchers.Main) {
                    onResult(false)
                }
            }
        }
    }

    fun negotiateBranchOffer(offerId: String, message: String, onResult: (Boolean) -> Unit) {
        scope.launch {
            try {
                val current = SupabaseClientProvider.client.postgrest["branch_offers"]
                    .select {
                        filter { eq("id", offerId) }
                    }.decodeList<BranchOffer>()
                val currentNotes = current.firstOrNull()?.notes ?: ""
                val updatedNotes = if (currentNotes.isEmpty()) "💬 تفاوض العميل: $message" else "$currentNotes\n💬 تفاوض العميل: $message"
                SupabaseClientProvider.client.postgrest["branch_offers"]
                    .update({
                        set("status", "negotiating")
                        set("notes", updatedNotes)
                    }) {
                        filter {
                            eq("id", offerId)
                        }
                    }
                withContext(Dispatchers.Main) {
                    onResult(true)
                }
            } catch (e: Exception) {
                Log.e("SUPABASE_DEBUG", "negotiateBranchOffer failed: ${e.message} | ${e.stackTraceToString()}")
                withContext(Dispatchers.Main) {
                    onResult(false)
                }
            }
        }
    }

    // --- Haversine Mathematics ---
    val cityCoordinatesMap = mapOf(
        "صنعاء" to Pair(15.3482, 44.2191),
        "عدن" to Pair(12.8021, 45.0312),
        "تعز" to Pair(13.5783, 44.0135),
        "الحديدة" to Pair(14.7978, 42.9544),
        "حضرموت" to Pair(14.5358, 49.1235),
        "إب" to Pair(13.9716, 44.1725),
        "ذمار" to Pair(14.5425, 44.4012),
        "مأرب" to Pair(15.4625, 45.3241)
    )

    fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val latDistance = Math.toRadians(lat2 - lat1)
        val lonDistance = Math.toRadians(lon2 - lon1)
        val a = sin(latDistance / 2) * sin(latDistance / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(lonDistance / 2) * sin(lonDistance / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        var distance = r * c
        if (distance.isNaN()) {
            return 0.0
        }
        return Math.round(distance * 10.0) / 10.0
    }

    // --- SMART ONBOARDING & ROUTING SERVICES ---

    fun setupClientProfile(profile: ClientProfile, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val finalProfile = profile.copy(
            clientId = if (profile.clientId.isEmpty()) "client_" + System.currentTimeMillis() else profile.clientId,
            joinedAt = System.currentTimeMillis(),
            profileCompleted = true
        )

        scope.launch {
            try {
                SupabaseClientProvider.client.postgrest["client_profiles"].upsert(finalProfile)

                try {
                    SupabaseClientProvider.client.postgrest["users"]
                        .update({
                            set("is_verified", finalProfile.isVerified)
                            set("org_name", finalProfile.institutionName)
                            set("phone", finalProfile.phone)
                            set("city", finalProfile.city)
                            set("governorate", finalProfile.governorate)
                        }) {
                            filter {
                                eq("id", profile.userId)
                            }
                        }
                } catch (ex: Exception) {
                    Log.e("SUPABASE_DEBUG", "setupClientProfile user update warning: ${ex.message}")
                }

                try {
                    val notification = DirectorNotification(
                        notificationId = "notif_" + System.currentTimeMillis(),
                        title = "انضمام عميل جديد",
                        message = "انضم عميل جديد: ${finalProfile.institutionName} في محافظة ${finalProfile.governorate}",
                        clientId = finalProfile.clientId,
                        clientName = finalProfile.institutionName,
                        createdAt = System.currentTimeMillis()
                    )
                    SupabaseClientProvider.client.postgrest["director_notifications"].upsert(notification)
                } catch (ex: Exception) {
                    Log.e("SUPABASE_DEBUG", "setupClientProfile notification warning: ${ex.message}")
                }

                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            } catch (e: Exception) {
                Log.e("SUPABASE_DEBUG", "setupClientProfile failed: ${e.message} | ${e.stackTraceToString()}")
                withContext(Dispatchers.Main) {
                    onFailure("خطأ في إعداد ملف العميل: ${e.message}")
                }
            }
        }
    }

    fun setupBranchManagerProfile(profile: BranchManagerProfile, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val finalProfile = profile.copy(
            profileCompleted = true,
            joinedAt = System.currentTimeMillis()
        )

        scope.launch {
            try {
                SupabaseClientProvider.client.postgrest["branch_manager_profiles"].upsert(finalProfile)

                try {
                    SupabaseClientProvider.client.postgrest["users"]
                        .update({
                            set("phone", finalProfile.phone)
                            set("name", finalProfile.fullName)
                        }) {
                            filter {
                                eq("id", profile.userId)
                            }
                        }
                } catch (ex: Exception) {
                    Log.e("SUPABASE_DEBUG", "setupBranchManagerProfile user update warning: ${ex.message}")
                }

                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            } catch (e: Exception) {
                Log.e("SUPABASE_DEBUG", "setupBranchManagerProfile failed: ${e.message} | ${e.stackTraceToString()}")
                withContext(Dispatchers.Main) {
                    onFailure("خطأ في إعداد ملف مدير الفرع: ${e.message}")
                }
            }
        }
    }

    fun smartRouteOrder(
        order: Order,
        clientGovernorate: String,
        clientLat: Double,
        clientLng: Double,
        broadcastType: String,
        selectedBranchIds: List<String> = emptyList(),
        onResult: (List<Branch>) -> Unit
    ) {
        scope.launch {
            try {
                val allBranches = SupabaseClientProvider.client.postgrest["branches"]
                    .select()
                    .decodeList<Branch>()

                val activeBranches = allBranches.filter { it.isActive }
                val routedBranches = when (broadcastType) {
                    "all" -> activeBranches
                    "nearby" -> {
                        val withDist = activeBranches.map { branch ->
                            val d = calculateDistanceKm(clientLat, clientLng, branch.latitude, branch.longitude)
                            branch to d
                        }
                        val withinRadius = withDist.filter { it.second <= 200.0 }
                        if (withinRadius.isEmpty()) {
                            val closest = withDist.minByOrNull { it.second }
                            if (closest != null) listOf(closest.first) else emptyList()
                        } else {
                            withinRadius.sortedBy { it.second }.map { it.first }
                        }
                    }
                    "selected" -> {
                        activeBranches.filter { selectedBranchIds.contains(it.branchId) }
                    }
                    else -> activeBranches
                }

                val targetBranchIds = routedBranches.map { it.branchId }
                val finalOrder = order.copy(
                    orderId = if (order.orderId.isEmpty()) "order_" + System.currentTimeMillis() else order.orderId,
                    targetBranches = targetBranchIds,
                    status = "broadcast",
                    createdAt = System.currentTimeMillis()
                )

                try {
                    SupabaseClientProvider.client.postgrest["orders"].upsert(finalOrder)
                } catch (ex: Exception) {
                    Log.e("SUPABASE_DEBUG", "smartRouteOrder order insert warning: ${ex.message}")
                }

                try {
                    val routing = OrderRouting(
                        orderId = finalOrder.orderId,
                        clientId = finalOrder.clientId,
                        targetBranches = targetBranchIds,
                        routingType = broadcastType,
                        routingReason = "توجيه تلقائي طبقاً للموقع الجغرافي ونصف قطر التغطية",
                        createdAt = System.currentTimeMillis()
                    )
                    SupabaseClientProvider.client.postgrest["order_routings"].upsert(routing)
                } catch (ex: Exception) {
                    Log.e("SUPABASE_DEBUG", "smartRouteOrder routing save warning: ${ex.message}")
                }

                try {
                    val notification = DirectorNotification(
                        notificationId = "notif_" + System.currentTimeMillis(),
                        title = "طلب جديد موجه",
                        message = "تم إنشاء طلب جديد برقم ${finalOrder.orderId} من العميل ${finalOrder.clientName} وموجه لـ ${targetBranchIds.size} فرع.",
                        orderId = finalOrder.orderId,
                        clientId = finalOrder.clientId,
                        clientName = finalOrder.clientName,
                        createdAt = System.currentTimeMillis()
                    )
                    SupabaseClientProvider.client.postgrest["director_notifications"].upsert(notification)
                } catch (ex: Exception) {
                    Log.e("SUPABASE_DEBUG", "smartRouteOrder notification warning: ${ex.message}")
                }

                withContext(Dispatchers.Main) {
                    onResult(routedBranches)
                }
            } catch (e: Exception) {
                Log.e("SUPABASE_DEBUG", "smartRouteOrder failed: ${e.message} | ${e.stackTraceToString()}")
                withContext(Dispatchers.Main) {
                    onResult(emptyList())
                }
            }
        }
    }

    fun sendOrderToDirector(notification: DirectorNotification, onSuccess: () -> Unit) {
        scope.launch {
            try {
                val finalNotification = if (notification.notificationId.isEmpty()) {
                    notification.copy(
                        notificationId = "notif_" + System.currentTimeMillis(),
                        createdAt = System.currentTimeMillis()
                    )
                } else {
                    notification
                }
                SupabaseClientProvider.client.postgrest["director_notifications"].upsert(finalNotification)
                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            } catch (e: Exception) {
                Log.e("SUPABASE_DEBUG", "sendOrderToDirector failed: ${e.message} | ${e.stackTraceToString()}")
                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            }
        }
    }

    fun getClientProfile(userId: String, onResult: (ClientProfile?) -> Unit) {
        scope.launch {
            try {
                val list = SupabaseClientProvider.client.postgrest["client_profiles"]
                    .select {
                        filter {
                            eq("user_id", userId)
                        }
                    }.decodeList<ClientProfile>()
                withContext(Dispatchers.Main) {
                    onResult(list.firstOrNull())
                }
            } catch (e: Exception) {
                Log.e("SUPABASE_DEBUG", "getClientProfile failed: ${e.message} | ${e.stackTraceToString()}")
                withContext(Dispatchers.Main) {
                    onResult(null)
                }
            }
        }
    }

    fun verifyClient(clientId: String, approve: Boolean, rejectReason: String = "", onSuccess: () -> Unit) {
        scope.launch {
            try {
                SupabaseClientProvider.client.postgrest["client_profiles"]
                    .update({
                        set("is_verified", approve)
                        set("is_active", approve)
                    }) {
                        filter {
                            eq("client_id", clientId)
                        }
                    }

                try {
                    val profileList = SupabaseClientProvider.client.postgrest["client_profiles"]
                        .select { filter { eq("client_id", clientId) } }
                        .decodeList<ClientProfile>()
                    val profile = profileList.firstOrNull()
                    if (profile != null) {
                        SupabaseClientProvider.client.postgrest["users"]
                            .update({
                                set("is_verified", approve)
                                set("is_active", approve)
                            }) {
                                filter {
                                    eq("id", profile.userId)
                                }
                            }
                    }
                } catch (ex: Exception) {
                    Log.e("SUPABASE_DEBUG", "verifyClient user update warning: ${ex.message}")
                }

                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            } catch (e: Exception) {
                Log.e("SUPABASE_DEBUG", "verifyClient failed: ${e.message} | ${e.stackTraceToString()}")
                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            }
        }
    }

    fun getDirectorOrdersFeed(onResult: (List<Order>) -> Unit) {
        scope.launch {
            try {
                val list = SupabaseClientProvider.client.postgrest["orders"]
                    .select()
                    .decodeList<Order>()
                withContext(Dispatchers.Main) {
                    onResult(list)
                }
            } catch (e: Exception) {
                Log.e("SUPABASE_DEBUG", "getDirectorOrdersFeed failed: ${e.message} | ${e.stackTraceToString()}")
                withContext(Dispatchers.Main) {
                    onResult(emptyList())
                }
            }
        }
    }

    fun getBranchDistance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        return calculateDistanceKm(lat1, lng1, lat2, lng2)
    }

    fun updateBranchLocation(
        branchId: String,
        address: String,
        lat: Double,
        lng: Double,
        managerPhone: String,
        onResult: (Boolean) -> Unit
    ) {
        scope.launch {
            try {
                SupabaseClientProvider.client.postgrest["branches"]
                    .update({
                        set("address", address)
                        set("latitude", lat)
                        set("longitude", lng)
                        set("manager_phone", managerPhone)
                    }) {
                        filter {
                            eq("id", branchId)
                        }
                    }
                withContext(Dispatchers.Main) {
                    onResult(true)
                }
            } catch (e: Exception) {
                Log.e("SUPABASE_DEBUG", "updateBranchLocation failed: ${e.message} | ${e.stackTraceToString()}")
                withContext(Dispatchers.Main) {
                    onResult(false)
                }
            }
        }
    }

    fun notifyDirector(
        notification: DirectorNotification,
        onResult: (Boolean) -> Unit
    ) {
        scope.launch {
            try {
                val finalNotification = if (notification.notificationId.isEmpty()) {
                    notification.copy(
                        notificationId = "notif_" + System.currentTimeMillis(),
                        createdAt = System.currentTimeMillis()
                    )
                } else {
                    notification
                }
                SupabaseClientProvider.client.postgrest["director_notifications"].upsert(finalNotification)
                withContext(Dispatchers.Main) {
                    onResult(true)
                }
            } catch (e: Exception) {
                Log.e("SUPABASE_DEBUG", "notifyDirector failed: ${e.message} | ${e.stackTraceToString()}")
                withContext(Dispatchers.Main) {
                    onResult(false)
                }
            }
        }
    }

    fun hasUserAddress(
        userId: String,
        onResult: (Boolean) -> Unit
    ) {
        scope.launch {
            try {
                val list = SupabaseClientProvider.client.postgrest["addresses"]
                    .select {
                        filter {
                            eq("user_id", userId)
                        }
                    }.decodeList<UserAddress>()
                withContext(Dispatchers.Main) {
                    onResult(list.isNotEmpty())
                }
            } catch (e: Exception) {
                Log.e("SUPABASE_DEBUG", "hasUserAddress failed: ${e.message} | ${e.stackTraceToString()}")
                withContext(Dispatchers.Main) {
                    onResult(false)
                }
            }
        }
    }

    fun getUserById(userId: String, onResult: (User?) -> Unit) {
        scope.launch {
            try {
                val user = SupabaseClientProvider.client.postgrest["users"]
                    .select {
                        filter {
                            eq("id", userId)
                        }
                    }.decodeSingle<User>()
                withContext(Dispatchers.Main) {
                    onResult(user)
                }
            } catch (e: Exception) {
                Log.e("SUPABASE_DEBUG", "getUserById failed: ${e.message} | ${e.stackTraceToString()}")
                withContext(Dispatchers.Main) {
                    onResult(null)
                }
            }
        }
    }

    fun allocateAndInvoiceOrder(
        orderId: String,
        updatedLines: List<OrderLine>,
        newStatus: OrderStatus,
        invoice: Invoice,
        clientId: String,
        scheduledDeliveryDate: Long,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val statusString = when (newStatus) {
            is OrderStatus.Allocated -> "allocated"
            is OrderStatus.PartiallyShipped -> "partially_shipped"
            is OrderStatus.Invoiced -> "invoiced"
            else -> "allocated"
        }

        scope.launch {
            try {
                SupabaseClientProvider.client.postgrest["orders"]
                    .update({
                        set("order_lines", updatedLines)
                        set("status", statusString)
                        set("total_amount", invoice.totalAmount)
                        set("scheduled_delivery_date", scheduledDeliveryDate)
                    }) {
                        filter {
                            eq("id", orderId)
                        }
                    }

                SupabaseClientProvider.client.postgrest["invoices"].upsert(invoice)

                try {
                    val user = SupabaseClientProvider.client.postgrest["users"]
                        .select {
                            filter {
                                eq("id", clientId)
                            }
                        }.decodeSingle<User>()
                    val currentAcc = user.clientAccount
                    val newBalance = currentAcc.currentBalance + invoice.totalAmount
                    val updatedUser = user.copy(
                        clientAccount = currentAcc.copy(currentBalance = newBalance)
                    )
                    SupabaseClientProvider.client.postgrest["users"].update(
                        buildJsonObject {
                            put("client_account", kotlinx.serialization.json.Json.encodeToJsonElement(
                                ClientAccount.serializer(), updatedUser.clientAccount
                            ))
                        }
                    ) {
                        filter { eq("id", clientId) }
                    }
                } catch (ex: Exception) {
                    Log.e("SUPABASE_DEBUG", "allocateAndInvoiceOrder user update warning: ${ex.message}")
                }

                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            } catch (e: Exception) {
                Log.e("SUPABASE_DEBUG", "allocateAndInvoiceOrder failed: ${e.message} | ${e.stackTraceToString()}")
                withContext(Dispatchers.Main) {
                    onFailure("خطأ في تخصيص الطلب وإنشاء الفاتورة: ${e.message}")
                }
            }
        }
    }

    fun getInvoices(onResult: (List<Invoice>) -> Unit) {
        scope.launch {
            try {
                val list = SupabaseClientProvider.client.postgrest["invoices"]
                    .select()
                    .decodeList<Invoice>()
                withContext(Dispatchers.Main) {
                    onResult(list)
                }
            } catch (e: Exception) {
                Log.e("SUPABASE_DEBUG", "getInvoices failed: ${e.message} | ${e.stackTraceToString()}")
                withContext(Dispatchers.Main) {
                    onResult(emptyList())
                }
            }
        }
    }

    fun getClientInvoices(clientId: String, onResult: (List<Invoice>) -> Unit) {
        getOrders { allOrders ->
            val clientOrderIds = allOrders.filter { it.clientId == clientId }.map { it.orderId }.toSet()
            getInvoices { allInvoices ->
                val filtered = allInvoices.filter { it.orderId in clientOrderIds }
                onResult(filtered)
            }
        }
    }

    fun getClientAccountStatus(clientId: String, onResult: (ClientAccount?) -> Unit) {
        getUserById(clientId) { user ->
            onResult(user?.clientAccount)
        }
    }

    fun getPharmaProducts(onResult: (List<PharmaProduct>) -> Unit) {
        scope.launch {
            try {
                val list = SupabaseClientProvider.client.postgrest["products"]
                    .select()
                    .decodeList<PharmaProduct>()
                withContext(Dispatchers.Main) {
                    onResult(list)
                }
            } catch (e: Exception) {
                Log.e("SUPABASE_DEBUG", "getPharmaProducts failed: ${e.message} | ${e.stackTraceToString()}")
                withContext(Dispatchers.Main) {
                    onResult(emptyList())
                }
            }
        }
    }

    fun savePharmaProduct(product: PharmaProduct, onResult: (Boolean) -> Unit) {
        val finalProduct = if (product.productId.isBlank()) {
            product.copy(productId = "prod_" + System.currentTimeMillis())
        } else {
            product
        }

        scope.launch {
            try {
                SupabaseClientProvider.client.postgrest["products"].upsert(finalProduct)
                withContext(Dispatchers.Main) {
                    onResult(true)
                }
            } catch (e: Exception) {
                Log.e("SUPABASE_DEBUG", "savePharmaProduct failed: ${e.message} | ${e.stackTraceToString()}")
                withContext(Dispatchers.Main) {
                    onResult(false)
                }
            }
        }
    }

    fun deletePharmaProduct(productId: String, onResult: (Boolean) -> Unit) {
        scope.launch {
            try {
                SupabaseClientProvider.client.postgrest["products"]
                    .delete {
                        filter {
                            eq("id", productId)
                        }
                    }
                withContext(Dispatchers.Main) {
                    onResult(true)
                }
            } catch (e: Exception) {
                Log.e("SUPABASE_DEBUG", "deletePharmaProduct failed: ${e.message} | ${e.stackTraceToString()}")
                withContext(Dispatchers.Main) {
                    onResult(false)
                }
            }
        }
    }

    fun getWarehouseInventory(branchId: String, onResult: (List<WarehouseInventoryItem>) -> Unit) {
        getPharmaProducts { centralProducts ->
            val activeProducts = centralProducts.filter { it.isActive }
            
            scope.launch {
                try {
                    val inventoryItems = SupabaseClientProvider.client.postgrest["warehouse_inventory"]
                        .select {
                            filter {
                                eq("branch_id", branchId)
                            }
                        }.decodeList<WarehouseInventoryItem>()
                    val inventoryMap = inventoryItems.associateBy { it.sku }

                    val mergedList = activeProducts.map { product ->
                        val existing = inventoryMap[product.sku]
                        WarehouseInventoryItem(
                            sku = product.sku,
                            name = product.commercialName,
                            dosageForm = product.dosageForm.name,
                            availableQuantity = existing?.availableQuantity ?: 0,
                            expiryDate = existing?.expiryDate ?: "2028-12-31",
                            branchId = branchId
                        )
                    }
                    withContext(Dispatchers.Main) {
                        onResult(mergedList)
                    }
                } catch (e: Exception) {
                    Log.e("SUPABASE_DEBUG", "getWarehouseInventory failed: ${e.message} | ${e.stackTraceToString()}")
                    withContext(Dispatchers.Main) {
                        onResult(emptyList())
                    }
                }
            }
        }
    }

    fun updateInventoryQuantity(sku: String, addedQty: Int) {
        updateInventoryQuantity("branch_sanaa", sku, addedQty, "") {}
    }

    fun updateInventoryQuantity(branchId: String, sku: String, addedQty: Int, expiryDate: String, onResult: (Boolean) -> Unit) {
        getPharmaProducts { products ->
            val product = products.find { it.sku == sku }
            val name = product?.commercialName ?: "مستحضر طبي"
            val dosageStr = product?.dosageForm?.name ?: "TABLET"

            scope.launch {
                try {
                    val list = SupabaseClientProvider.client.postgrest["warehouse_inventory"]
                        .select {
                            filter {
                                eq("branch_id", branchId)
                                eq("sku", sku)
                            }
                        }.decodeList<WarehouseInventoryItem>()
                    val currentItem = list.firstOrNull()
                    val currentQty = currentItem?.availableQuantity ?: 0
                    val newQty = (currentQty + addedQty).coerceAtLeast(0)
                    val finalExpiry = if (expiryDate.isNotEmpty()) expiryDate else (currentItem?.expiryDate ?: "2028-12-31")

                    val updatedItem = WarehouseInventoryItem(
                        sku = sku,
                        name = name,
                        dosageForm = dosageStr,
                        availableQuantity = newQty,
                        expiryDate = finalExpiry,
                        branchId = branchId
                    )

                    SupabaseClientProvider.client.postgrest["warehouse_inventory"].upsert(updatedItem)

                    withContext(Dispatchers.Main) {
                        onResult(true)
                    }
                } catch (e: Exception) {
                    Log.e("SUPABASE_DEBUG", "updateInventoryQuantity failed: ${e.message} | ${e.stackTraceToString()}")
                    withContext(Dispatchers.Main) {
                        onResult(false)
                    }
                }
            }
        }
    }

    // --- Promotional Offers ---
    fun createOffer(offer: PromotionalOffer, onResult: (Boolean) -> Unit) {
        val finalOffer = if (offer.offerId.isBlank()) {
            offer.copy(offerId = "offer_" + System.currentTimeMillis())
        } else {
            offer
        }
        scope.launch {
            try {
                SupabaseClientProvider.client.postgrest["promotional_offers"].upsert(finalOffer)
                withContext(Dispatchers.Main) {
                    onResult(true)
                }
            } catch (e: Exception) {
                Log.e("SUPABASE_DEBUG", "createOffer failed: ${e.message} | ${e.stackTraceToString()}")
                withContext(Dispatchers.Main) {
                    onResult(false)
                }
            }
        }
    }

    fun updateOffer(offer: PromotionalOffer, onResult: (Boolean) -> Unit) {
        scope.launch {
            try {
                SupabaseClientProvider.client.postgrest["promotional_offers"].upsert(offer)
                withContext(Dispatchers.Main) {
                    onResult(true)
                }
            } catch (e: Exception) {
                Log.e("SUPABASE_DEBUG", "updateOffer failed: ${e.message} | ${e.stackTraceToString()}")
                withContext(Dispatchers.Main) {
                    onResult(false)
                }
            }
        }
    }

    fun deleteOffer(offerId: String, onResult: (Boolean) -> Unit) {
        scope.launch {
            try {
                SupabaseClientProvider.client.postgrest["promotional_offers"]
                    .delete {
                        filter {
                            eq("id", offerId)
                        }
                    }
                withContext(Dispatchers.Main) {
                    onResult(true)
                }
            } catch (e: Exception) {
                Log.e("SUPABASE_DEBUG", "deleteOffer failed: ${e.message} | ${e.stackTraceToString()}")
                withContext(Dispatchers.Main) {
                    onResult(false)
                }
            }
        }
    }

    fun getActiveOffers(governorate: String, onResult: (List<PromotionalOffer>) -> Unit) {
        scope.launch {
            try {
                val list = SupabaseClientProvider.client.postgrest["promotional_offers"]
                    .select {
                        filter {
                            eq("is_active", true)
                        }
                    }.decodeList<PromotionalOffer>()
                withContext(Dispatchers.Main) {
                    val currentTime = System.currentTimeMillis()
                    val filtered = list.filter { offer ->
                        val isDateValid = currentTime >= offer.startDate && currentTime <= offer.endDate
                        val isGovValid = offer.targetGovernorate.isBlank() || offer.targetGovernorate == governorate
                        isDateValid && isGovValid
                    }
                    onResult(filtered)
                }
            } catch (e: Exception) {
                Log.e("SUPABASE_DEBUG", "getActiveOffers failed: ${e.message} | ${e.stackTraceToString()}")
                withContext(Dispatchers.Main) {
                    onResult(emptyList())
                }
            }
        }
    }

    // --- Order Transfer Functions ---
    fun transferFullOrder(orderId: String, newBranchId: String, onResult: (Boolean) -> Unit) {
        scope.launch {
            try {
                val order = SupabaseClientProvider.client.postgrest["orders"]
                    .select {
                        filter {
                            eq("id", orderId)
                        }
                    }.decodeSingle<Order>()
                val updatedOrder = order.copy(targetBranches = listOf(newBranchId))
                SupabaseClientProvider.client.postgrest["orders"].upsert(updatedOrder)
                withContext(Dispatchers.Main) {
                    onResult(true)
                }
            } catch (e: Exception) {
                Log.e("SUPABASE_DEBUG", "transferFullOrder failed: ${e.message} | ${e.stackTraceToString()}")
                withContext(Dispatchers.Main) {
                    onResult(false)
                }
            }
        }
    }

    fun transferPartialOrder(orderId: String, lineSkus: List<String>, newBranchId: String, onResult: (Boolean) -> Unit) {
        fun processOrderTransfer(originalOrder: Order, onDone: (Boolean) -> Unit) {
            val linesToTransfer = originalOrder.orderLines.filter { it.product.sku in lineSkus }
            val linesToKeep = originalOrder.orderLines.filter { it.product.sku !in lineSkus }
            
            if (linesToTransfer.isEmpty()) {
                onDone(false)
                return
            }
            
            val newSubOrderId = "${originalOrder.orderId}_sub_${System.currentTimeMillis()}"
            val subOrderTotal = linesToTransfer.sumOf { it.totalPrice }
            val updatedOriginalTotal = linesToKeep.sumOf { it.totalPrice }
            
            val subOrder = originalOrder.copy(
                orderId = newSubOrderId,
                parentOrderId = originalOrder.orderId,
                orderLines = linesToTransfer,
                totalAmount = subOrderTotal,
                targetBranches = listOf(newBranchId),
                createdAt = System.currentTimeMillis()
            )
            
            val updatedOriginalOrder = originalOrder.copy(
                orderLines = linesToKeep,
                totalAmount = updatedOriginalTotal
            )
            
            scope.launch {
                try {
                    SupabaseClientProvider.client.postgrest["orders"].upsert(updatedOriginalOrder)
                    SupabaseClientProvider.client.postgrest["orders"].upsert(subOrder)
                    withContext(Dispatchers.Main) {
                        onDone(true)
                    }
                } catch (e: Exception) {
                    Log.e("SUPABASE_DEBUG", "transferPartialOrder failed: ${e.message} | ${e.stackTraceToString()}")
                    withContext(Dispatchers.Main) {
                        onDone(false)
                    }
                }
            }
        }

        scope.launch {
            try {
                val order = SupabaseClientProvider.client.postgrest["orders"]
                    .select {
                        filter {
                            eq("id", orderId)
                        }
                    }.decodeSingle<Order>()
                withContext(Dispatchers.Main) {
                    processOrderTransfer(order, onResult)
                }
            } catch (e: Exception) {
                Log.e("SUPABASE_DEBUG", "transferPartialOrder select failed: ${e.message} | ${e.stackTraceToString()}")
                withContext(Dispatchers.Main) {
                    onResult(false)
                }
            }
        }
    }

    fun smartTransferOrder(orderId: String, partialOnly: Boolean, onResult: (Boolean, String?) -> Unit) {
        scope.launch {
            try {
                val order = SupabaseClientProvider.client.postgrest["orders"]
                    .select {
                        filter {
                            eq("id", orderId)
                        }
                    }.decodeSingle<Order>()

                val currentBranchId = order.targetBranches.firstOrNull() ?: ""
                
                getUserAddresses(order.clientId) { addresses ->
                    val clientAddr = addresses.find { it.isDefault } ?: addresses.firstOrNull()
                    val clientLat = clientAddr?.latitude ?: 15.3482
                    val clientLng = clientAddr?.longitude ?: 44.2191
                    
                    getBranches { allBranches ->
                        val otherActiveBranches = allBranches.filter { it.isActive && it.branchId != currentBranchId }
                        if (otherActiveBranches.isEmpty()) {
                            onResult(false, "لا توجد فروع أخرى نشطة متاحة للتحويل إليها")
                            return@getBranches
                        }
                        
                        if (partialOnly) {
                            getWarehouseInventory(currentBranchId) { currentInv ->
                                val currentInvMap = currentInv.associateBy { it.sku }
                                val shortageLines = order.orderLines.filter { line ->
                                    val available = currentInvMap[line.product.sku]?.availableQuantity ?: 0
                                    available < line.requestedQty
                                }
                                
                                if (shortageLines.isEmpty()) {
                                    onResult(false, "كل المواد متوفرة بالفعل في هذا الفرع")
                                    return@getWarehouseInventory
                                }
                                
                                val eligibleBranches = mutableListOf<Pair<Branch, Double>>()
                                var completed = 0
                                
                                otherActiveBranches.forEach { branch ->
                                    getWarehouseInventory(branch.branchId) { branchInv ->
                                        val branchInvMap = branchInv.associateBy { it.sku }
                                        val hasSufficient = shortageLines.all { line ->
                                            val available = branchInvMap[line.product.sku]?.availableQuantity ?: 0
                                            available >= line.requestedQty
                                        }
                                        if (hasSufficient) {
                                            val dist = calculateDistanceKm(clientLat, clientLng, branch.latitude, branch.longitude)
                                            eligibleBranches.add(branch to dist)
                                        }
                                        completed++
                                        if (completed == otherActiveBranches.size) {
                                            if (eligibleBranches.isEmpty()) {
                                                onResult(false, "لا يوجد فرع بديل متوفر لهذه الأصناف حالياً")
                                            } else {
                                                val closest = eligibleBranches.minBy { it.second }.first
                                                val lineSkus = shortageLines.map { it.product.sku }
                                                transferPartialOrder(orderId, lineSkus, closest.branchId) { success ->
                                                    if (success) {
                                                        onResult(true, closest.branchName)
                                                    } else {
                                                        onResult(false, "فشل في إتمام عملية التحويل الجزئي")
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            val eligibleBranches = mutableListOf<Pair<Branch, Double>>()
                            var completed = 0
                            
                            otherActiveBranches.forEach { branch ->
                                getWarehouseInventory(branch.branchId) { branchInv ->
                                    val branchInvMap = branchInv.associateBy { it.sku }
                                    val hasSufficient = order.orderLines.all { line ->
                                        val available = branchInvMap[line.product.sku]?.availableQuantity ?: 0
                                        available >= line.requestedQty
                                    }
                                    if (hasSufficient) {
                                        val dist = calculateDistanceKm(clientLat, clientLng, branch.latitude, branch.longitude)
                                        eligibleBranches.add(branch to dist)
                                    }
                                    completed++
                                    if (completed == otherActiveBranches.size) {
                                        if (eligibleBranches.isEmpty()) {
                                            onResult(false, "لا يوجد فرع بديل متوفر لهذه الأصناف حالياً")
                                        } else {
                                            val closest = eligibleBranches.minBy { it.second }.first
                                            transferFullOrder(orderId, closest.branchId) { success ->
                                                if (success) {
                                                    onResult(true, closest.branchName)
                                                } else {
                                                    onResult(false, "فشل في إتمام عملية تحويل الطلب بالكامل")
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("SUPABASE_DEBUG", "smartTransferOrder failed: ${e.message} | ${e.stackTraceToString()}")
                withContext(Dispatchers.Main) {
                    onResult(false, "خطأ في البحث عن الطلب: ${e.message}")
                }
            }
        }
    }
}
