package com.example.service

import android.util.Log
import com.example.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.math.*

object FirebaseService {
    private val auth: FirebaseAuth? by lazy {
        try { FirebaseAuth.getInstance() } catch (e: Exception) { null }
    }
    private val db: FirebaseFirestore? by lazy {
        try { FirebaseFirestore.getInstance() } catch (e: Exception) { null }
    }

    // --- Fallback memory data vectors for fluent Offline Demonstration ---
    val fallbackUsers = mutableListOf<User>(
        User("hosp_1", "مستشفى الثورة بصنعاء", "thawra@yemen.org", "hospital", "صنعاء", "771122334", "هيئة مستشفى الثورة العام"),
        User("hosp_2", "مستشفى الجمهورية بعدن", "jomhoria@yemen.org", "hospital", "عدن", "733344556", "مستشفى الجمهورية التعليمي"),
        User("sup_1", "الشركة العالمية للأدوية", "global@yemen.org", "supplier", "صنعاء", "775566778", "العالمية للأدوية والمستلزمات"),
        User("sup_2", "مجموعة الكندي الدوائية", "alkindi@yemen.org", "supplier", "عدن", "711222333", "الكندي للاستيراد والتوزيع"),
        User("sup_3", "الشركة اليمنية لصناعة الأدوية (يدكو)", "yedco@yemen.org", "supplier", "صنعاء", "777888999", "YEDCO Pharma Ltd")
    )

    val fallbackAddresses = mutableListOf<UserAddress>(
        // Hospitals
        UserAddress("addr_hosp_1", "hosp_1", "hospital", "المقر الرئيسي للمستشفى", "هيئة مستشفى الثورة العام", "جامع الشعب", "صنعاء", "الصافية", "حي الاستاد الرياضي", "صنعاء - الصافية - بجانب ملعب الثورة", 15.3482, 44.2191, true, System.currentTimeMillis()),
        UserAddress("addr_hosp_2", "hosp_2", "hospital", "رئاسة الهيئة الطبية", "مستشفى الجمهورية التعليمي", "ساحة العروض", "عدن", "خور مكسر", "حي الجمهورية", "عدن - خور مكسر - الشارع العام", 12.8021, 45.0312, true, System.currentTimeMillis()),
        // Suppliers
        UserAddress("addr_sup_1", "sup_1", "supplier", "المخازن المركزية", "الشركة العالمية للأدوية", "جولة ريحة", "صنعاء", "الحصبة", "حي الرشيد", "صنعاء - الحصبة - خلف بريد الحصبة", 15.3621, 44.1956, true, System.currentTimeMillis()),
        UserAddress("addr_sup_2", "sup_2", "supplier", "الفرع الجنوبي", "مجموعة الكندي الدوائية", "جولة دار سعد", "عدن", "المنصورة", "حي التسعين", "عدن - المنصورة - شارع التسعين الرئيسي", 12.8425, 44.9854, true, System.currentTimeMillis()),
        UserAddress("addr_sup_3", "sup_3", "supplier", "المصنع والمقر الرئيسي", "الشركة اليمنية لصناعة الأدوية (يدكو)", "جولة آية", "صنعاء", "شعوب", "حي الروضة", "صنعاء - الحصبة الشمالية - حي الروضة", 15.3892, 44.2384, true, System.currentTimeMillis())
    )

    val fallbackMedicines = mutableListOf<Medicine>(
        Medicine("med_1", "Amoxicillin 500mg", "مضادات حيوية", 3.5, 500, "sup_1"),
        Medicine("med_2", "Paracetamol 1000mg IV", "مسكنات ومضادات التهاب", 1.2, 2000, "sup_1"),
        Medicine("med_3", "Insulin Actrapid", "أدوية السكري", 12.0, 150, "sup_2"),
        Medicine("med_4", "Ceftriaxone 1g Injection", "مضادات حيوية", 4.2, 800, "sup_2"),
        Medicine("med_5", "Atorvastatin 20mg", "أدوية القلب والضغط", 8.5, 350, "sup_3")
    )

    val fallbackPriceOffers = mutableListOf<PriceOffer>(
        PriceOffer("offer_1", "broad_1", "sup_1", "الشركة العالمية للأدوية", "med_1", "Amoxicillin 500mg", 3.2, 500, 15.0, "متوفر تسليم فوري في صناديق مطابقة للمواصفات", "pending", "", "", "", 0.0, "", "", System.currentTimeMillis() - 7200000),
        PriceOffer("offer_2", "broad_1", "sup_2", "مجموعة الكندي الدوائية", "med_1", "Amoxicillin 500mg", 3.0, 500, 45.0, "شحن مبرد متوفر بالكامل", "pending", "", "", "", 0.0, "", "", System.currentTimeMillis() - 3600000),
        PriceOffer("offer_3", "broad_2", "sup_3", "الشركة اليمنية لصناعة الأدوية (يدكو)", "med_5", "Atorvastatin 20mg", 8.0, 300, 10.0, "شحن داخلي عبر صنعاء متاح مباشرة", "pending", "", "", "", 0.0, "", "", System.currentTimeMillis())
    )

    val fallbackBankAccounts = mutableListOf<BankAccount>(
        BankAccount("acc_1", "sup_1", "البنك الكريمي للتمويل الأصغر الإسلامي", "3201445566", "الشركة العالمية للأدوية المحدودة", "bank", "", true, System.currentTimeMillis()),
        BankAccount("acc_2", "sup_1", "محفظة كاش - يمن موبايل", "", "صالح علي - مندوب المالية العالمية", "mfs", "775566778", false, System.currentTimeMillis()),
        BankAccount("acc_3", "sup_2", "بنك اليمن والخليج", "100200300", "مجموعة الكندي للأدوية والاستيراد", "bank", "", true, System.currentTimeMillis()),
        BankAccount("acc_4", "sup_2", "محفظة MTN كاش", "", "مجموعة الكندي التجارية", "mfs", "711222333", false, System.currentTimeMillis())
    )

    val fallbackPayments = mutableListOf<Payment>()
    val fallbackDeliveryRequests = mutableListOf<DeliveryRequest>()
    val fallbackDeliverySchedules = mutableListOf<DeliverySchedule>(
        DeliverySchedule("sched_1", "order_1", listOf("2026-06-10T10:00:00", "2026-06-11T14:30:00"), listOf("2026-06-10T12:00:00", "2026-06-12T09:00:00"), "2026-06-10T12:00:00", "agreed", System.currentTimeMillis())
    )
    val fallbackOrders = mutableListOf<Order>(
        Order("order_1", "offer_1", "hosp_1", "sup_1", "Amoxicillin 500mg", 3.2, 500, "self", "paid", "2026-06-10T12:00:00", System.currentTimeMillis())
    )

    // Current Local User Simulation Session
    var currentUserSession: User = fallbackUsers[0] // Default is Hospital Thawra

    // --- Authentication ---
    fun getCurrentUser(onResult: (User?) -> Unit) {
        val fbAuthUser = auth?.currentUser
        if (fbAuthUser != null && db != null) {
            db!!.collection("users").document(fbAuthUser.uid).get()
                .addOnSuccessListener { doc ->
                    val user = doc.toObject(User::class.java)
                    if (user != null) {
                        currentUserSession = user
                        onResult(user)
                    } else {
                        onResult(currentUserSession)
                    }
                }
                .addOnFailureListener {
                    onResult(currentUserSession)
                }
        } else {
            onResult(currentUserSession)
        }
    }

    fun loginUser(email: String, onResult: (User?, String?) -> Unit) {
        val user = fallbackUsers.find { it.email.trim().equals(email.trim(), ignoreCase = true) }
        if (user != null) {
            currentUserSession = user
            onResult(user, null)
        } else {
            // Self-register a mock user based on email type to facilitate effortless testing
            val isHospital = email.contains("hosp") || email.contains("hospital")
            val role = if (isHospital) "hospital" else "supplier"
            val newMockUser = User(
                userId = "user_mock_" + System.currentTimeMillis(),
                name = email.substringBefore("@").replaceFirstChar { it.uppercase() },
                email = email,
                role = role,
                city = "صنعاء",
                phone = "770000000",
                orgName = if (isHospital) "منشأة استشفائية تجريبية" else "مورد خدمات دوائية تجريبي"
            )
            fallbackUsers.add(newMockUser)
            currentUserSession = newMockUser
            onResult(newMockUser, null)
        }
    }

    fun registerUser(user: User, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val exists = fallbackUsers.any { it.email.trim().equals(user.email.trim(), ignoreCase = true) }
        if (exists) {
            onFailure("عذراً، البريد الإلكتروني مسجل مسبقاً")
            return
        }
        val finalUser = user.copy(userId = if (user.userId.isEmpty()) "user_" + System.currentTimeMillis() else user.userId)
        fallbackUsers.add(finalUser)
        currentUserSession = finalUser
        onSuccess()
    }

    // --- Addresses ---
    fun getUserAddresses(userId: String, onResult: (List<UserAddress>) -> Unit) {
        if (db != null) {
            db!!.collection("addresses")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener { snap ->
                    val list = snap.toObjects(UserAddress::class.java)
                    if (list.isEmpty()) {
                        onResult(fallbackAddresses.filter { it.userId == userId })
                    } else {
                        onResult(list)
                    }
                }
                .addOnFailureListener {
                    onResult(fallbackAddresses.filter { it.userId == userId })
                }
        } else {
            onResult(fallbackAddresses.filter { it.userId == userId })
        }
    }

    fun getAllAddresses(onResult: (List<UserAddress>) -> Unit) {
        if (db != null) {
            db!!.collection("addresses")
                .get()
                .addOnSuccessListener { snap ->
                    val list = snap.toObjects(UserAddress::class.java)
                    if (list.isEmpty()) {
                        onResult(fallbackAddresses)
                    } else {
                        onResult(list)
                    }
                }
                .addOnFailureListener {
                    onResult(fallbackAddresses)
                }
        } else {
            onResult(fallbackAddresses)
        }
    }

    fun saveAddress(address: UserAddress, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val id = if (address.addressId.isEmpty()) "addr_" + System.currentTimeMillis() else address.addressId
        val finalAddr = address.copy(addressId = id, createdAt = System.currentTimeMillis())

        if (finalAddr.isDefault) {
            // reset previous defaults
            val size = fallbackAddresses.size
            for (i in 0 until size) {
                if (fallbackAddresses[i].userId == finalAddr.userId) {
                    fallbackAddresses[i] = fallbackAddresses[i].copy(isDefault = false)
                }
            }
        }

        if (db != null) {
            db!!.collection("addresses").document(id).set(finalAddr)
                .addOnSuccessListener { 
                    fallbackAddresses.add(finalAddr)
                    onSuccess() 
                }
                .addOnFailureListener { e -> onFailure(e.localizedMessage ?: "فشل تسجيل العنوان") }
        } else {
            fallbackAddresses.add(finalAddr)
            onSuccess()
        }
    }

    fun setDefaultAddress(userId: String, addressId: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        // Mock Update
        for (i in 0 until fallbackAddresses.size) {
            if (fallbackAddresses[i].userId == userId) {
                val match = fallbackAddresses[i].addressId == addressId
                fallbackAddresses[i] = fallbackAddresses[i].copy(isDefault = match)
            }
        }

        if (db != null) {
            db!!.collection("addresses").document(addressId).update("isDefault", true)
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { e -> onFailure(e.localizedMessage ?: "تحذير: لا يمكن الحفظ") }
        } else {
            onSuccess()
        }
    }

    // --- Bank Accounts ---
    fun getBankAccounts(userId: String, onResult: (List<BankAccount>) -> Unit) {
        if (db != null) {
            db!!.collection("bank_accounts")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener { snap ->
                    val list = snap.toObjects(BankAccount::class.java)
                    if (list.isEmpty()) {
                        onResult(fallbackBankAccounts.filter { it.userId == userId })
                    } else {
                        onResult(list)
                    }
                }
                .addOnFailureListener {
                    onResult(fallbackBankAccounts.filter { it.userId == userId })
                }
        } else {
            onResult(fallbackBankAccounts.filter { it.userId == userId })
        }
    }

    fun saveBankAccount(account: BankAccount, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val id = if (account.accountId.isEmpty()) "acc_" + System.currentTimeMillis() else account.accountId
        val finalAcc = account.copy(accountId = id, createdAt = System.currentTimeMillis())

        if (finalAcc.isDefault) {
            for (i in 0 until fallbackBankAccounts.size) {
                if (fallbackBankAccounts[i].userId == finalAcc.userId) {
                    fallbackBankAccounts[i] = fallbackBankAccounts[i].copy(isDefault = false)
                }
            }
        }

        if (db != null) {
            db!!.collection("bank_accounts").document(id).set(finalAcc)
                .addOnSuccessListener {
                    fallbackBankAccounts.add(finalAcc)
                    onSuccess()
                }
                .addOnFailureListener { e -> onFailure(e.localizedMessage ?: "فشل تسجيل إعداد الحساب") }
        } else {
            fallbackBankAccounts.add(finalAcc)
            onSuccess()
        }
    }

    fun setDefaultBankAccount(userId: String, accountId: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        for (i in 0 until fallbackBankAccounts.size) {
            if (fallbackBankAccounts[i].userId == userId) {
                val match = fallbackBankAccounts[i].accountId == accountId
                fallbackBankAccounts[i] = fallbackBankAccounts[i].copy(isDefault = match)
            }
        }
        onSuccess()
    }

    // --- Medicines & Offers ---
    fun getMedicines(onResult: (List<Medicine>) -> Unit) {
        onResult(fallbackMedicines)
    }

    fun getSuppliers(onResult: (List<User>) -> Unit) {
        onResult(fallbackUsers.filter { it.role == "supplier" })
    }

    fun getPriceOffers(userId: String, onResult: (List<PriceOffer>) -> Unit) {
        val list = fallbackPriceOffers.filter { it.supplierId == userId || currentUserSession.role == "hospital" }
        onResult(list)
    }

    fun updatePriceOfferStatus(offerId: String, newStatus: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val idx = fallbackPriceOffers.indexOfFirst { it.priceOfferId == offerId }
        if (idx != -1) {
            fallbackPriceOffers[idx] = fallbackPriceOffers[idx].copy(status = newStatus)
        }
        onSuccess()
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
        val idx = fallbackPriceOffers.indexOfFirst { it.priceOfferId == offerId }
        if (idx != -1) {
            fallbackPriceOffers[idx] = fallbackPriceOffers[idx].copy(
                status = "accepted",
                deliveryAddressId = address.addressId,
                deliveryAddressLabel = address.label,
                deliveryFullAddress = address.fullAddress,
                distance = distance,
                eta = eta,
                deliveryMethod = deliveryMethod
            )
            // Automatically launch an Order object for tracking
            val offer = fallbackPriceOffers[idx]
            val newOrder = Order(
                orderId = "order_" + offer.priceOfferId,
                priceOfferId = offer.priceOfferId,
                hospitalId = currentUserSession.userId,
                supplierId = offer.supplierId,
                medicineName = offer.medicineName,
                price = offer.price,
                quantity = offer.quantity,
                deliveryMethod = deliveryMethod,
                status = "pending",
                createdAt = System.currentTimeMillis()
            )
            fallbackOrders.add(newOrder)
        }
        onSuccess()
    }

    // --- Payments ---
    fun submitPayment(payment: Payment, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val id = "pay_" + System.currentTimeMillis()
        val finalPay = payment.copy(paymentId = id)
        fallbackPayments.add(finalPay)

        // Automatically update the matching Order's status to "paid"
        val orderIdx = fallbackOrders.indexOfFirst { it.orderId == payment.orderId }
        if (orderIdx != -1) {
            fallbackOrders[orderIdx] = fallbackOrders[orderIdx].copy(status = "paid")
        }

        onSuccess()
    }

    fun getPayments(onResult: (List<Payment>) -> Unit) {
        onResult(fallbackPayments)
    }

    fun updatePaymentStatus(paymentId: String, newStatus: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val idx = fallbackPayments.indexOfFirst { it.paymentId == paymentId }
        if (idx != -1) {
            fallbackPayments[idx] = fallbackPayments[idx].copy(status = newStatus)
        }
        onSuccess()
    }

    // --- Delivery Requests (Platform Shipment) ---
    fun submitDeliveryRequest(request: DeliveryRequest, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val id = "del_" + System.currentTimeMillis()
        val finalReq = request.copy(deliveryId = id, createdAt = System.currentTimeMillis())
        fallbackDeliveryRequests.add(finalReq)
        onSuccess()
    }

    fun getDeliveryRequests(onResult: (List<DeliveryRequest>) -> Unit) {
        onResult(fallbackDeliveryRequests)
    }

    fun assignDeliveryDriver(deliveryId: String, driverName: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val idx = fallbackDeliveryRequests.indexOfFirst { it.deliveryId == deliveryId }
        if (idx != -1) {
            fallbackDeliveryRequests[idx] = fallbackDeliveryRequests[idx].copy(
                status = "assigned",
                adminAssigned = true
            )
        }
        onSuccess()
    }

    // --- Delivery Schedules ---
    fun getDeliverySchedule(orderId: String, onResult: (DeliverySchedule?) -> Unit) {
        val s = fallbackDeliverySchedules.find { it.orderId == orderId }
        onResult(s)
    }

    fun saveDeliverySchedule(schedule: DeliverySchedule, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val sId = if (schedule.scheduleId.isEmpty()) "sched_" + System.currentTimeMillis() else schedule.scheduleId
        val finalSched = schedule.copy(scheduleId = sId, updatedAt = System.currentTimeMillis())

        val idx = fallbackDeliverySchedules.indexOfFirst { it.orderId == schedule.orderId }
        if (idx != -1) {
            fallbackDeliverySchedules[idx] = finalSched
        } else {
            fallbackDeliverySchedules.add(finalSched)
        }

        if (schedule.status == "agreed") {
            val oIdx = fallbackOrders.indexOfFirst { it.orderId == schedule.orderId }
            if (oIdx != -1) {
                fallbackOrders[oIdx] = fallbackOrders[oIdx].copy(
                    deliveryScheduledDate = schedule.agreedDateTime
                )
            }
        }
        onSuccess()
    }

    fun getOrders(onResult: (List<Order>) -> Unit) {
        onResult(fallbackOrders)
    }

    fun updateOrderStatus(orderId: String, newStatus: String) {
        val idx = fallbackOrders.indexOfFirst { it.orderId == orderId }
        if (idx != -1) {
            fallbackOrders[idx] = fallbackOrders[idx].copy(status = newStatus)
        }
    }

    // --- Haversine Mathematics with Fallback Lookup ---
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
        val r = 6371.0 // Radius of the earth in km
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
        // Round to 1 decimal place
        return Math.round(distance * 10.0) / 10.0
    }
}
