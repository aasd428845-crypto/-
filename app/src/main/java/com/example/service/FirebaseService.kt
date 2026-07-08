package com.example.service

import android.util.Log
import com.example.model.*
import kotlin.math.*
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object FirebaseService {
    private val scope = CoroutineScope(Dispatchers.IO)

    // --- Fallback memory data vectors for fluent Offline Demonstration ---
    val fallbackCompany = CompanyInfo(
        companyId = "main_company",
        companyName = "مجموعة الشفاء للأدوية والمستلزمات الطبية",
        companyNameEn = "Al-Shefa Medical Group",
        phone = "+967 1 234567",
        email = "info@alshefa-yemen.com",
        mainAddress = "صنعاء - شارع حدة - بجانب وزارة الصحة",
        licenseNumber = "MOH-2020-0045",
        totalBranches = 6
    )

    val fallbackBranches = mutableListOf(
        Branch("branch_sanaa", "فرع صنعاء الرئيسي", "صنعاء", "صنعاء", "صنعاء - الحصبة", "771111111", "manager_1", "أحمد محمد الحميدي", "771111112", 15.3482, 44.2191, true),
        Branch("branch_aden", "فرع عدن", "عدن", "عدن", "عدن - خورمكسر", "733222222", "manager_2", "فيصل علي البكري", "733222223", 12.8021, 45.0312, true),
        Branch("branch_taiz", "فرع تعز", "تعز", "تعز", "تعز - الحوبان", "711333333", "manager_3", "محمد عبدالله السلامي", "711333334", 13.5784, 44.0217, true),
        Branch("branch_hadramout", "فرع حضرموت", "حضرموت", "المكلا", "المكلا - شارع البحر", "701444444", "manager_4", "خالد عمر الكثيري", "701444445", 14.5403, 49.1293, true),
        Branch("branch_hodeidah", "فرع الحديدة", "الحديدة", "الحديدة", "الحديدة - شارع علي عبدالمغني", "775555555", "manager_5", "عبدالرحمن يحيى الزيدي", "775555556", 14.7978, 42.9547, true),
        Branch("branch_ibb", "فرع إب", "إب", "إب", "إب - شارع الجمهورية", "770666666", "manager_6", "سالم حسن الأنسي", "770666667", 13.9764, 44.1773, true)
    )

    val fallbackUsers = mutableListOf<User>(
        User("director_1", "المدير العام - سعيد ناصر", "director@alshefa.com", "company_director", "", "صنعاء", "صنعاء", "777000001", "مجموعة الشفاء للأدوية"),
        User("manager_1", "أحمد محمد الحميدي", "sanaa@alshefa.com", "branch_manager", "", "صنعاء", "صنعاء", "771111112", "مجموعة الشفاء", "branch_sanaa", "فرع صنعاء الرئيسي", true, true),
        User("manager_2", "فيصل علي البكري", "aden@alshefa.com", "branch_manager", "", "عدن", "عدن", "733222223", "مجموعة الشفاء", "branch_aden", "فرع عدن", true, true),
        User("client_1", "مستشفى الثورة العام", "thawra@hospital.com", "client", "hospital", "صنعاء", "صنعاء", "771122334", "مستشفى الثورة"),
        User("client_2", "صيدلية النور المركزية", "alnoor@pharmacy.com", "client", "pharmacy", "عدن", "عدن", "733344556", "صيدلية النور")
    )

    val fallbackBranchOffers = mutableListOf<BranchOffer>(
        BranchOffer(
            offerId = "offer_sanaa_1",
            orderId = "order_sample_1",
            branchId = "branch_sanaa",
            branchName = "فرع صنعاء الرئيسي",
            managerId = "manager_1",
            managerName = "أحمد محمد الحميدي",
            offerDetails = "علب أموكسيسيلين متوفرة بالكامل مع تسليم خلال يومين",
            totalPrice = 120000.0,
            currency = "YER",
            deliveryDays = 2,
            shippingCost = 5000.0,
            paymentTerms = "سداد نقدي عند الاستلام أو تحويل كريمي",
            notes = "عرض خاص بمستشفى الثورة العام",
            status = "pending",
            createdAt = System.currentTimeMillis() - 7200000
        ),
        BranchOffer(
            offerId = "offer_sample_2_sanaa",
            orderId = "order_sample_2",
            branchId = "branch_sanaa",
            branchName = "فرع صنعاء الرئيسي",
            managerId = "manager_1",
            managerName = "أحمد محمد الحميدي",
            offerDetails = "مجموعة كاملة من خافضات الحرارة متوفرة الآن في مستودعاتنا",
            totalPrice = 45000.0,
            currency = "YER",
            deliveryDays = 3,
            shippingCost = 4000.0,
            paymentTerms = "الدفع نقداً أو كريمي عند الاستلام",
            notes = "توصيل سريع مع تغليف حراري ممتاز للأدوية",
            status = "pending",
            createdAt = System.currentTimeMillis() - 1800000
        ),
        BranchOffer(
            offerId = "offer_sample_2_aden",
            orderId = "order_sample_2",
            branchId = "branch_aden",
            branchName = "فرع عدن",
            managerId = "manager_2",
            managerName = "فيصل علي البكري",
            offerDetails = "شراب كف وخافض حرارة متوفر بالكامل تسليم فوري في عدن",
            totalPrice = 42000.0,
            currency = "YER",
            deliveryDays = 1,
            shippingCost = 1500.0,
            paymentTerms = "فوري كاش",
            notes = "الأقرب لعنوانكم في خورمكسر، التوصيل في نفس اليوم",
            status = "pending",
            createdAt = System.currentTimeMillis() - 900000
        )
    )

    val fallbackAddresses = mutableListOf<UserAddress>(
        // Hospitals
        UserAddress("addr_hosp_1", "hosp_1", "hospital", "المقر الرئيسي للمستشفى", "هيئة مستشفى الثورة العام", "جامع الشعب", "صنعاء", "الصافية", "حي الاستاد الرياضي", "صنعاء - الصافية - بجانب ملعب الثورة", 15.3482, 44.2191, true, System.currentTimeMillis()),
        UserAddress("addr_hosp_2", "hosp_2", "hospital", "رئاسة الهيئة الطبية", "مستشفى الجمهورية التعليمي", "ساحة العروض", "عدن", "خور مكسر", "حي الجمهورية", "عدن - خور مكسر - الشارع العام", 12.8021, 45.0312, true, System.currentTimeMillis()),
        // Suppliers
        UserAddress("addr_sup_1", "sup_1", "supplier", "المخازن المركزية", "الشركة العالمية للأدوية", "جولة ريحة", "صنعاء", "الحصبة", "حي الرشيد", "صنعاء - الحصبة - خلف بريد الحصبة", 15.3621, 44.1956, true, System.currentTimeMillis()),
        UserAddress("addr_sup_2", "sup_2", "supplier", "الفرع الجنوبي", "مجموعة الكندي الدوائية", "جولة دار سعد", "عدن", "المنصورة", "حي التسعين", "عدن - المنصورة - شارع التسعين الرئيسي", 12.8425, 44.9854, true, System.currentTimeMillis()),
        UserAddress("addr_sup_3", "sup_3", "supplier", "المصنع والمقر الرئيسي", "الشركة اليمنية لصناعة الأدوية (يدكو)", "جولة آية", "صنعاء", "شعوب", "حي الروضة", "صنعاء - الحصبة الشمالية - حي الروضة", 15.3892, 44.2384, true, System.currentTimeMillis()),
        // Branch Managers
        UserAddress("addr_mgr_1", "manager_1", "branch_manager", "فرع صنعاء الرئيسي", "مجموعة الشفاء", "باب اليمن", "صنعاء", "صنعاء القديمة", "حي الجامع الكبير", "صنعاء القديمة - بجانب باب اليمن", 15.3482, 44.2191, true, System.currentTimeMillis()),
        UserAddress("addr_mgr_2", "manager_2", "branch_manager", "فرع عدن", "مجموعة الشفاء", "قلعة صيرة", "عدن", "كريتر", "حي صيرة", "عدن - كريتر - بجانب القلعة", 12.8021, 45.0312, true, System.currentTimeMillis())
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
        Order(
            orderId = "order_1",
            priceOfferId = "offer_1",
            hospitalId = "hosp_1",
            supplierId = "sup_1",
            medicineName = "Amoxicillin 500mg",
            price = 3.2,
            quantity = 500,
            deliveryMethod = "self",
            status = "paid",
            deliveryScheduledDate = "2026-06-10T12:00:00",
            createdAt = System.currentTimeMillis()
        ),
        Order(
            orderId = "order_sample_1",
            clientId = "profile_client_1",
            clientName = "مستشفى الثورة العام",
            clientType = "hospital",
            clientGovernorate = "صنعاء",
            orderContent = "طلب عاجل لـ 500 فيال سيفالوسبورين و 200 علبة أنسولين لانتوس",
            attachments = listOf(),
            urgencyLevel = "critical",
            broadcastType = "all",
            targetBranches = listOf("branch_sanaa", "branch_aden"),
            status = "broadcast",
            createdAt = System.currentTimeMillis() - 3600000
        ),
        Order(
            orderId = "order_sample_2",
            clientId = "profile_client_2",
            clientName = "صيدلية النور المركزية",
            clientType = "pharmacy",
            clientGovernorate = "عدن",
            orderContent = "مجموعة خافض حرارة للأطفال وشراب كف دواء سعال",
            attachments = listOf(),
            urgencyLevel = "normal",
            broadcastType = "nearby",
            targetBranches = listOf("branch_aden", "branch_sanaa"),
            status = "offer_received",
            createdAt = System.currentTimeMillis() - 7200000
        )
    )

    val fallbackClientProfiles = mutableListOf<ClientProfile>(
        ClientProfile(
            clientId = "profile_client_1",
            userId = "client_1",
            institutionName = "مستشفى الثورة العام",
            clientType = "hospital",
            responsiblePerson = "د. جمال الشامي",
            phone = "771122334",
            alternatePhone = "771122335",
            licenseNumber = "MOH-HOSP-991",
            licenseImageUrl = "https://images.unsplash.com/photo-1576091160550-2173dba999ef",
            governorate = "صنعاء",
            city = "صنعاء",
            district = "الصافية",
            neighborhood = "حي الاستاد",
            landmark = "ملعب الثورة",
            fullAddress = "صنعاء - الصافية - بجانب ملعب الثورة",
            latitude = 15.3482,
            longitude = 44.2191,
            assignedBranchId = "branch_sanaa",
            assignedBranchName = "فرع صنعاء الرئيسي",
            preferredPayment = "تحويل بنكي",
            paymentAccount = "الكريمي - 320111",
            isVerified = true,
            isActive = true,
            profileCompleted = true,
            joinedAt = System.currentTimeMillis() - 86400000 * 10
        ),
        ClientProfile(
            clientId = "profile_client_2",
            userId = "client_2",
            institutionName = "صيدلية النور المركزية",
            clientType = "pharmacy",
            responsiblePerson = "صيدلي أحمد العليمي",
            phone = "733344556",
            alternatePhone = "",
            licenseNumber = "MOH-PHAR-442",
            licenseImageUrl = "https://images.unsplash.com/photo-1576091160550-2173dba999ef",
            governorate = "عدن",
            city = "عدن",
            district = "خور مكسر",
            neighborhood = "حي الجمهورية",
            landmark = "ساحة العروض",
            fullAddress = "عدن - خور مكسر - الشارع العام",
            latitude = 12.8021,
            longitude = 45.0312,
            assignedBranchId = "branch_aden",
            assignedBranchName = "فرع عدن",
            preferredPayment = "كاش",
            paymentAccount = "",
            isVerified = true,
            isActive = true,
            profileCompleted = true,
            joinedAt = System.currentTimeMillis() - 86400000 * 5
        )
    )
    val fallbackBranchManagerProfiles = mutableListOf<BranchManagerProfile>(
        BranchManagerProfile(
            userId = "manager_1",
            fullName = "أحمد محمد الحميدي",
            phone = "771111112",
            nationalIdImageUrl = "https://images.unsplash.com/photo-1580489944761-15a19d654956",
            warehouseLat = 15.3482,
            warehouseLng = 44.2191,
            profileCompleted = true,
            joinedAt = System.currentTimeMillis() - 86400000 * 30
        )
    )
    val fallbackOrderRoutings = mutableListOf<OrderRouting>()
    val fallbackDirectorNotifications = mutableListOf<DirectorNotification>()

    // Current Local User Simulation Session
    var currentUserSession: User? = null

    // Last database error for UI diagnosis
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

    fun loginUser(email: String, onResult: (User?, String?) -> Unit) {
        scope.launch {
            try {
                val user = withContext(Dispatchers.IO) {
                    SupabaseClientProvider.client.postgrest["users"]
                        .select {
                            filter {
                                eq("email", email.trim())
                            }
                        }.decodeList<User>().firstOrNull()
                }
                if (user != null) {
                    currentUserSession = user
                    // also attempt to sign in with auth if possible, but lookup is sufficient
                    withContext(Dispatchers.Main) { onResult(user, null) }
                } else {
                    val isClient = email.contains("hosp") || email.contains("hospital") || email.contains("pharmacy") || email.contains("client")
                    val isDirector = email.contains("director") || email.contains("admin")
                    val role = when {
                        isDirector -> "company_director"
                        isClient -> "client"
                        else -> "branch_manager"
                    }
                    val newId = "user_mock_" + System.currentTimeMillis()
                    val newMockUser = User(
                        userId = newId,
                        name = email.substringBefore("@").replaceFirstChar { it.uppercase() },
                        email = email,
                        role = role,
                        clientType = if (email.contains("pharmacy")) "pharmacy" else "hospital",
                        city = "صنعاء",
                        phone = "770000000",
                        orgName = if (isClient) "منشأة تجريبية للعميل" else "مجموعة الشفاء للأدوية",
                        branchId = if (role == "branch_manager") "branch_sanaa" else "",
                        branchName = if (role == "branch_manager") "فرع صنعاء الرئيسي" else ""
                    )
                    withContext(Dispatchers.IO) {
                        SupabaseClientProvider.client.postgrest["users"].insert(newMockUser)
                    }
                    fallbackUsers.add(newMockUser)
                    currentUserSession = newMockUser
                    withContext(Dispatchers.Main) { onResult(newMockUser, null) }
                }
            } catch (e: Exception) {
                Log.e("SUPABASE_DEBUG", "loginUser failed: ${e.message} | ${e.stackTraceToString()}")
                lastDatabaseError = "loginUser: ${e.message}"
                withContext(Dispatchers.Main) { onResult(null, "خطأ في الاتصال بقاعدة البيانات: ${e.message}") }
            }
        }
    }

    fun registerUser(user: User, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        scope.launch {
            try {
                val exists = withContext(Dispatchers.IO) {
                    SupabaseClientProvider.client.postgrest["users"]
                        .select {
                            filter {
                                eq("email", user.email.trim())
                            }
                        }.decodeList<User>().isNotEmpty()
                }
                if (exists) {
                    withContext(Dispatchers.Main) { onFailure("عذراً، البريد الإلكتروني مسجل مسبقاً") }
                    return@launch
                }
                
                val finalUserId = if (user.userId.isEmpty()) "user_" + System.currentTimeMillis() else user.userId
                val finalUser = user.copy(userId = finalUserId)
                
                withContext(Dispatchers.IO) {
                    SupabaseClientProvider.client.postgrest["users"].insert(finalUser)
                }
                
                try {
                    withContext(Dispatchers.IO) {
                        SupabaseClientProvider.client.auth.signUpWith(Email) {
                            email = finalUser.email
                            password = "default_password_123"
                            data = buildJsonObject {
                                put("role", finalUser.role)
                                put("name", finalUser.name)
                            }
                        }
                    }
                } catch (authEx: Exception) {
                    Log.e("SUPABASE_DEBUG", "Auth signUpWith failed (non-blocking): ${authEx.message}")
                }
                
                currentUserSession = finalUser
                if (!fallbackUsers.any { it.email.equals(finalUser.email, true) }) {
                    fallbackUsers.add(finalUser)
                }
                withContext(Dispatchers.Main) { onSuccess() }
            } catch (e: Exception) {
                Log.e("SUPABASE_DEBUG", "registerUser failed: ${e.message} | ${e.stackTraceToString()}")
                withContext(Dispatchers.Main) { onFailure("خطأ في الاتصال بقاعدة البيانات: ${e.message}") }
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
                    if (list.isEmpty()) {
                        onResult(fallbackAddresses.filter { it.userId == userId })
                    } else {
                        onResult(list)
                    }
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
                    if (list.isEmpty()) {
                        onResult(fallbackAddresses)
                    } else {
                        onResult(list)
                    }
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

        if (finalAddr.isDefault) {
            // reset previous defaults
            val size = fallbackAddresses.size
            for (i in 0 until size) {
                if (fallbackAddresses[i].userId == finalAddr.userId) {
                    fallbackAddresses[i] = fallbackAddresses[i].copy(isDefault = false)
                }
            }
        }

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

                // Sync local state
                val idx = fallbackAddresses.indexOfFirst { it.addressId == id }
                if (idx != -1) {
                    fallbackAddresses[idx] = finalAddr
                } else {
                    fallbackAddresses.add(finalAddr)
                }

                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            } catch (e: Exception) {
                Log.e("SUPABASE_DEBUG", "saveAddress failed: ${e.message} | ${e.stackTraceToString()}")
                val idx = fallbackAddresses.indexOfFirst { it.addressId == id }
                if (idx != -1) {
                    fallbackAddresses[idx] = finalAddr
                } else {
                    fallbackAddresses.add(finalAddr)
                }
                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            }
        }
    }

    fun setDefaultAddress(userId: String, addressId: String, onResult: (Boolean) -> Unit) {
        // Mock Update
        for (i in 0 until fallbackAddresses.size) {
            if (fallbackAddresses[i].userId == userId) {
                val match = fallbackAddresses[i].addressId == addressId
                fallbackAddresses[i] = fallbackAddresses[i].copy(isDefault = match)
            }
        }

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
                    onResult(true)
                }
            }
        }
    }

    fun addUserAddress(address: UserAddress, onResult: (Boolean, String?) -> Unit) {
        val id = if (address.addressId.isEmpty()) "addr_" + System.currentTimeMillis() else address.addressId
        val finalAddr = address.copy(addressId = id, createdAt = System.currentTimeMillis())

        if (finalAddr.isDefault) {
            val size = fallbackAddresses.size
            for (i in 0 until size) {
                if (fallbackAddresses[i].userId == finalAddr.userId) {
                    fallbackAddresses[i] = fallbackAddresses[i].copy(isDefault = false)
                }
            }
        }

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

                val idx = fallbackAddresses.indexOfFirst { it.addressId == id }
                if (idx != -1) {
                    fallbackAddresses[idx] = finalAddr
                } else {
                    fallbackAddresses.add(finalAddr)
                }

                withContext(Dispatchers.Main) {
                    onResult(true, id)
                }
            } catch (e: Exception) {
                Log.e("SUPABASE_DEBUG", "addUserAddress failed: ${e.message} | ${e.stackTraceToString()}")
                val idx = fallbackAddresses.indexOfFirst { it.addressId == id }
                if (idx != -1) {
                    fallbackAddresses[idx] = finalAddr
                } else {
                    fallbackAddresses.add(finalAddr)
                }
                withContext(Dispatchers.Main) {
                    onResult(true, id)
                }
            }
        }
    }

    fun updateUserAddress(address: UserAddress, onResult: (Boolean) -> Unit) {
        val id = address.addressId
        if (address.isDefault) {
            val size = fallbackAddresses.size
            for (i in 0 until size) {
                if (fallbackAddresses[i].userId == address.userId) {
                    fallbackAddresses[i] = fallbackAddresses[i].copy(isDefault = false)
                }
            }
        }

        val idx = fallbackAddresses.indexOfFirst { it.addressId == id }
        if (idx != -1) {
            fallbackAddresses[idx] = address
        } else {
            fallbackAddresses.add(address)
        }

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
                    onResult(true)
                }
            }
        }
    }

    fun deleteUserAddress(addressId: String, onResult: (Boolean) -> Unit) {
        val idx = fallbackAddresses.indexOfFirst { it.addressId == addressId }
        if (idx != -1) {
            fallbackAddresses.removeAt(idx)
        }

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
                    onResult(true)
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
                    if (list.isEmpty()) {
                        onResult(fallbackBankAccounts.filter { it.userId == userId })
                    } else {
                        onResult(list)
                    }
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

        if (finalAcc.isDefault) {
            for (i in 0 until fallbackBankAccounts.size) {
                if (fallbackBankAccounts[i].userId == finalAcc.userId) {
                    fallbackBankAccounts[i] = fallbackBankAccounts[i].copy(isDefault = false)
                }
            }
        }

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

                // Sync local state
                val idx = fallbackBankAccounts.indexOfFirst { it.accountId == id }
                if (idx != -1) {
                    fallbackBankAccounts[idx] = finalAcc
                } else {
                    fallbackBankAccounts.add(finalAcc)
                }

                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            } catch (e: Exception) {
                Log.e("SUPABASE_DEBUG", "saveBankAccount failed: ${e.message} | ${e.stackTraceToString()}")
                val idx = fallbackBankAccounts.indexOfFirst { it.accountId == id }
                if (idx != -1) {
                    fallbackBankAccounts[idx] = finalAcc
                } else {
                    fallbackBankAccounts.add(finalAcc)
                }
                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            }
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
        val list = fallbackPriceOffers.filter { it.supplierId == userId || currentUserSession?.role == "hospital" }
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
                hospitalId = currentUserSession?.userId ?: "",
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

    fun getCompanyInfo(onResult: (CompanyInfo) -> Unit) {
        onResult(fallbackCompany)
    }

    fun getBranches(onResult: (List<Branch>) -> Unit) {
        onResult(fallbackBranches)
    }

    fun submitOrder(order: Order, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val finalOrder = order.copy(
            orderId = if (order.orderId.isEmpty()) "order_" + System.currentTimeMillis() else order.orderId,
            createdAt = if (order.createdAt == 0L) System.currentTimeMillis() else order.createdAt
        )
        fallbackOrders.add(finalOrder)
        onSuccess()
    }

    fun getBranchOffersForOrder(orderId: String, onResult: (List<BranchOffer>) -> Unit) {
        onResult(fallbackBranchOffers.filter { it.orderId == orderId })
    }

    fun getAllBranchOffers(onResult: (List<BranchOffer>) -> Unit) {
        onResult(fallbackBranchOffers)
    }

    fun submitBranchOffer(offer: BranchOffer, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val finalOffer = offer.copy(
            offerId = if (offer.offerId.isEmpty()) "offer_" + System.currentTimeMillis() else offer.offerId,
            createdAt = if (offer.createdAt == 0L) System.currentTimeMillis() else offer.createdAt
        )
        fallbackBranchOffers.add(finalOffer)
        // Automatically update order status to indicate offer received
        val oIdx = fallbackOrders.indexOfFirst { it.orderId == offer.orderId }
        if (oIdx != -1) {
            fallbackOrders[oIdx] = fallbackOrders[oIdx].copy(status = "offer_received")
        }
        onSuccess()
    }

    fun updateBranchOfferStatus(offerId: String, newStatus: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val idx = fallbackBranchOffers.indexOfFirst { it.offerId == offerId }
        if (idx != -1) {
            fallbackBranchOffers[idx] = fallbackBranchOffers[idx].copy(status = newStatus)
            if (newStatus == "accepted") {
                // Confirm the order as well
                val oIdx = fallbackOrders.indexOfFirst { it.orderId == fallbackBranchOffers[idx].orderId }
                if (oIdx != -1) {
                    fallbackOrders[oIdx] = fallbackOrders[oIdx].copy(status = "confirmed")
                }
            }
        }
        onSuccess()
    }

    fun getBranchOrders(branchId: String, onResult: (List<Order>) -> Unit) {
        scope.launch {
            try {
                val list = SupabaseClientProvider.client.postgrest["orders"]
                    .select()
                    .decodeList<Order>()
                val filtered = list.filter { it.targetBranches.contains(branchId) || it.targetBranches.isEmpty() }
                withContext(Dispatchers.Main) {
                    if (filtered.isEmpty()) {
                        onResult(fallbackOrders.filter { it.targetBranches.contains(branchId) || it.targetBranches.isEmpty() })
                    } else {
                        onResult(filtered)
                    }
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
        val idx = fallbackBranchOffers.indexOfFirst { it.offerId == offerId }
        if (idx != -1) {
            fallbackBranchOffers[idx] = fallbackBranchOffers[idx].copy(status = "rejected", notes = "سبب الرفض: $reason")
        }
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
                    onResult(true)
                }
            }
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

    // --- SMART ONBOARDING & ROUTING SERVICES ---

    fun setupClientProfile(profile: ClientProfile, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val finalProfile = profile.copy(
            clientId = if (profile.clientId.isEmpty()) "client_" + System.currentTimeMillis() else profile.clientId,
            joinedAt = System.currentTimeMillis(),
            profileCompleted = true
        )
        val idx = fallbackClientProfiles.indexOfFirst { it.userId == profile.userId }
        if (idx != -1) {
            fallbackClientProfiles[idx] = finalProfile
        } else {
            fallbackClientProfiles.add(finalProfile)
        }

        // Also update User profile in fallbackUsers to profileCompleted
        val userIdx = fallbackUsers.indexOfFirst { it.userId == profile.userId }
        if (userIdx != -1) {
            fallbackUsers[userIdx] = fallbackUsers[userIdx].copy(
                isVerified = finalProfile.isVerified,
                orgName = finalProfile.institutionName,
                phone = finalProfile.phone,
                city = finalProfile.city,
                governorate = finalProfile.governorate
            )
        }

        // Send Notification to Director
        val notification = DirectorNotification(
            notificationId = "notif_" + System.currentTimeMillis(),
            title = "انضمام عميل جديد",
            message = "انضم عميل جديد: ${finalProfile.institutionName} في محافظة ${finalProfile.governorate}",
            clientId = finalProfile.clientId,
            clientName = finalProfile.institutionName,
            createdAt = System.currentTimeMillis()
        )
        fallbackDirectorNotifications.add(notification)

        onSuccess()
    }

    fun setupBranchManagerProfile(profile: BranchManagerProfile, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val finalProfile = profile.copy(
            profileCompleted = true,
            joinedAt = System.currentTimeMillis()
        )
        val idx = fallbackBranchManagerProfiles.indexOfFirst { it.userId == profile.userId }
        if (idx != -1) {
            fallbackBranchManagerProfiles[idx] = finalProfile
        } else {
            fallbackBranchManagerProfiles.add(finalProfile)
        }

        // Also update User profile in fallbackUsers
        val userIdx = fallbackUsers.indexOfFirst { it.userId == profile.userId }
        if (userIdx != -1) {
            fallbackUsers[userIdx] = fallbackUsers[userIdx].copy(
                phone = finalProfile.phone,
                name = finalProfile.fullName
            )
        }
        onSuccess()
    }

    fun smartRouteOrder(
        order: Order,
        clientGovernorate: String,
        clientLat: Double,
        clientLng: Double,
        broadcastType: String, // all / nearby / selected
        selectedBranchIds: List<String> = emptyList(),
        onResult: (List<Branch>) -> Unit
    ) {
        val activeBranches = fallbackBranches.filter { it.isActive }
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
        fallbackOrders.add(finalOrder)

        // Save routing log
        val routing = OrderRouting(
            orderId = finalOrder.orderId,
            clientId = finalOrder.clientId,
            targetBranches = targetBranchIds,
            routingType = broadcastType,
            routingReason = "توجيه تلقائي طبقاً للموقع الجغرافي ونصف قطر التغطية",
            createdAt = System.currentTimeMillis()
        )
        fallbackOrderRoutings.add(routing)

        // Notification to General Director
        val notification = DirectorNotification(
            notificationId = "notif_" + System.currentTimeMillis(),
            title = "طلب جديد موجه",
            message = "تم إنشاء طلب جديد برقم ${finalOrder.orderId} من العميل ${finalOrder.clientName} وموجه لـ ${targetBranchIds.size} فرع.",
            orderId = finalOrder.orderId,
            clientId = finalOrder.clientId,
            clientName = finalOrder.clientName,
            createdAt = System.currentTimeMillis()
        )
        fallbackDirectorNotifications.add(notification)

        onResult(routedBranches)
    }

    fun sendOrderToDirector(notification: DirectorNotification, onSuccess: () -> Unit) {
        fallbackDirectorNotifications.add(notification)
        onSuccess()
    }

    fun getClientProfile(userId: String, onResult: (ClientProfile?) -> Unit) {
        val p = fallbackClientProfiles.find { it.userId == userId }
        onResult(p)
    }

    fun verifyClient(clientId: String, approve: Boolean, rejectReason: String = "", onSuccess: () -> Unit) {
        val idx = fallbackClientProfiles.indexOfFirst { it.clientId == clientId }
        if (idx != -1) {
            val updated = fallbackClientProfiles[idx].copy(
                isVerified = approve,
                isActive = approve
            )
            fallbackClientProfiles[idx] = updated
            // also update fallbackUsers list
            val userIdx = fallbackUsers.indexOfFirst { it.userId == updated.userId }
            if (userIdx != -1) {
                fallbackUsers[userIdx] = fallbackUsers[userIdx].copy(
                    isVerified = approve,
                    isActive = approve
                )
            }
        }
        onSuccess()
    }

    fun getDirectorOrdersFeed(onResult: (List<Order>) -> Unit) {
        onResult(fallbackOrders)
    }

    fun getBranchDistance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        return calculateDistanceKm(lat1, lng1, lat2, lng2)
    }

    // تحديث موقع الفرع في مجموعة branches
    fun updateBranchLocation(
        branchId: String,
        address: String,
        lat: Double,
        lng: Double,
        managerPhone: String,
        onResult: (Boolean) -> Unit
    ) {
        val idx = fallbackBranches.indexOfFirst { it.branchId == branchId }
        if (idx != -1) {
            val updated = fallbackBranches[idx].copy(
                address = address,
                latitude = lat,
                longitude = lng,
                managerPhone = managerPhone
            )
            fallbackBranches[idx] = updated
        }
        
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
                    onResult(true)
                }
            }
        }
    }

    // إرسال إشعار للمدير العام
    fun notifyDirector(
        notification: DirectorNotification,
        onResult: (Boolean) -> Unit
    ) {
        val finalNotification = if (notification.notificationId.isEmpty()) {
            notification.copy(
                notificationId = "notif_" + System.currentTimeMillis(),
                createdAt = System.currentTimeMillis()
            )
        } else {
            notification
        }
        
        fallbackDirectorNotifications.add(finalNotification)
        
        scope.launch {
            try {
                SupabaseClientProvider.client.postgrest["director_notifications"].upsert(finalNotification)
                withContext(Dispatchers.Main) {
                    onResult(true)
                }
            } catch (e: Exception) {
                Log.e("SUPABASE_DEBUG", "notifyDirector failed: ${e.message} | ${e.stackTraceToString()}")
                withContext(Dispatchers.Main) {
                    onResult(true)
                }
            }
        }
    }

    // التحقق من وجود عنوان للمستخدم
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
                    if (list.isEmpty()) {
                        val hasFallback = fallbackAddresses.any { it.userId == userId }
                        onResult(hasFallback)
                    } else {
                        onResult(true)
                    }
                }
            } catch (e: Exception) {
                Log.e("SUPABASE_DEBUG", "hasUserAddress failed: ${e.message} | ${e.stackTraceToString()}")
                withContext(Dispatchers.Main) {
                    onResult(false)
                }
            }
        }
    }

    val fallbackInvoices = mutableListOf<Invoice>()

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
        val orderIdx = fallbackOrders.indexOfFirst { it.orderId == orderId }
        val statusString = when (newStatus) {
            is OrderStatus.Allocated -> "allocated"
            is OrderStatus.PartiallyShipped -> "partially_shipped"
            is OrderStatus.Invoiced -> "invoiced"
            else -> "allocated"
        }

        if (orderIdx != -1) {
            val originalOrder = fallbackOrders[orderIdx]
            fallbackOrders[orderIdx] = originalOrder.copy(
                orderLines = updatedLines,
                orderStatus = newStatus,
                status = statusString,
                totalAmount = invoice.totalAmount,
                scheduledDeliveryDate = scheduledDeliveryDate
            )
        }

        fallbackInvoices.add(invoice)

        val userIdx = fallbackUsers.indexOfFirst { it.userId == clientId }
        if (userIdx != -1) {
            val user = fallbackUsers[userIdx]
            val currentAcc = user.clientAccount
            val newBalance = currentAcc.currentBalance + invoice.totalAmount
            fallbackUsers[userIdx] = user.copy(
                clientAccount = currentAcc.copy(currentBalance = newBalance)
            )
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
                    SupabaseClientProvider.client.postgrest["users"].upsert(updatedUser)
                } catch (ex: Exception) {
                    Log.e("SUPABASE_DEBUG", "allocateAndInvoiceOrder user update warning: ${ex.message}")
                }

                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            } catch (e: Exception) {
                Log.e("SUPABASE_DEBUG", "allocateAndInvoiceOrder failed: ${e.message} | ${e.stackTraceToString()}")
                withContext(Dispatchers.Main) {
                    onSuccess()
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
                    if (list.isEmpty()) {
                        onResult(fallbackInvoices)
                    } else {
                        onResult(list)
                    }
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

    val fallbackPharmaProducts = mutableListOf<PharmaProduct>(
        PharmaProduct(
            productId = "prod_1",
            sku = "AMX-500-CAP",
            ndcCode = "0007-4112-20",
            commercialName = "أموكسيسيلين 500 ملجم (أمبيسيل)",
            scientificName = "Amoxicillin Trihydrate",
            manufacturer = "الشركة اليمنية لصناعة الأدوية (يدكو)",
            dosageForm = DosageForm.CAPSULE,
            strength = "500 mg",
            isColdChain = false,
            isControlledSubstance = false,
            unitType = "صندوق (3 شريط)",
            unitsPerBox = 30,
            price = 1500.0,
            description = "مضاد حيوي واسع الطيف لعلاج الالتهابات البكتيرية الحادة."
        ),
        PharmaProduct(
            productId = "prod_2",
            sku = "INS-ACT-INJ",
            ndcCode = "0169-1834-11",
            commercialName = "إنسولين أكتRapid مبرد 💉",
            scientificName = "Insulin Human (rDNA)",
            manufacturer = "Novo Nordisk",
            dosageForm = DosageForm.INJECTION,
            strength = "100 IU/ml",
            isColdChain = true,
            isControlledSubstance = false,
            unitType = "فيال (Vial)",
            unitsPerBox = 1,
            price = 8500.0,
            description = "إنسولين سريع المفعول للتحكم بالسكري. يحفظ في درجة حرارة 2-8 مئوية."
        ),
        PharmaProduct(
            productId = "prod_3",
            sku = "FEN-50-INJ",
            ndcCode = "50458-038-10",
            commercialName = "فنتانيل حقن مخدرة ⚠️ (مقيد)",
            scientificName = "Fentanyl Citrate",
            manufacturer = "Janssen-Cilag",
            dosageForm = DosageForm.INJECTION,
            strength = "50 mcg/ml",
            isColdChain = false,
            isControlledSubstance = true,
            unitType = "أمبولة",
            unitsPerBox = 5,
            price = 12000.0,
            description = "مسكن ألم أفيوني قوي جداً للعمليات الجراحية. خاضع للرقابة الصارمة."
        ),
        PharmaProduct(
            productId = "prod_4",
            sku = "PAR-500-TAB",
            ndcCode = "0012-4001-50",
            commercialName = "باراسيتامول 500 ملجم الشفاء",
            scientificName = "Paracetamol",
            manufacturer = "مجموعة الشفاء الدوائية",
            dosageForm = DosageForm.TABLET,
            strength = "500 mg",
            isColdChain = false,
            isControlledSubstance = false,
            unitType = "كرتون (10 شريط)",
            unitsPerBox = 100,
            price = 800.0,
            description = "خافض حرارة ومسكن للآلام الخفيفة والمتوسطة."
        ),
        PharmaProduct(
            productId = "prod_5",
            sku = "CEF-1G-INJ",
            ndcCode = "0781-3204-95",
            commercialName = "سيف ترياكسون 1 جرام حقن",
            scientificName = "Ceftriaxone Sodium",
            manufacturer = "Sandoz",
            dosageForm = DosageForm.INJECTION,
            strength = "1 g",
            isColdChain = false,
            isControlledSubstance = false,
            unitType = "فيال + مذيب",
            unitsPerBox = 1,
            price = 3500.0,
            description = "مضاد حيوي قوي من الجيل الثالث للسيفالوسبورينات."
        ),
        PharmaProduct(
            productId = "prod_6",
            sku = "ATO-20-TAB",
            ndcCode = "0071-0156-23",
            commercialName = "أتورفاستاتين 20 ملجم (ليبيتور)",
            scientificName = "Atorvastatin Calcium",
            manufacturer = "Pfizer",
            dosageForm = DosageForm.TABLET,
            strength = "20 mg",
            isColdChain = false,
            isControlledSubstance = false,
            unitType = "شريطين",
            unitsPerBox = 28,
            price = 4200.0,
            description = "خافض للكوليسترول والدهون الثلاثية للوقاية من أمراض القلب."
        ),
        PharmaProduct(
            productId = "prod_7",
            sku = "ATO-10-TAB",
            ndcCode = "0071-0155-23",
            commercialName = "أتورفاستاتين 10 ملجم (منتهي)",
            scientificName = "Atorvastatin Calcium",
            manufacturer = "Pfizer",
            dosageForm = DosageForm.TABLET,
            strength = "10 mg",
            isColdChain = false,
            isControlledSubstance = false,
            unitType = "شريطين",
            unitsPerBox = 28,
            price = 3900.0,
            description = "مخزون نافد تماماً من المستودعات حالياً."
        ),
        PharmaProduct(
            productId = "prod_8",
            sku = "VEN-100-INH",
            ndcCode = "0173-0321-88",
            commercialName = "فنتولين بخاخ للربو 🌬️",
            scientificName = "Salbutamol Inhaler",
            manufacturer = "GlaxoSmithKline",
            dosageForm = DosageForm.INHALER,
            strength = "100 mcg",
            isColdChain = false,
            isControlledSubstance = false,
            unitType = "جهاز استنشاق",
            unitsPerBox = 1,
            price = 5000.0,
            description = "موقع للقصبات الهوائية سريع المفعول لنوبات الربو وضيق التنفس."
        ),
        PharmaProduct(
            productId = "prod_9",
            sku = "AUG-312-SYR",
            ndcCode = "0173-0315-10",
            commercialName = "أوجمنتين شراب معلق للأطفال",
            scientificName = "Amoxicillin / Clavulanate",
            manufacturer = "GSK",
            dosageForm = DosageForm.SYRUP,
            strength = "312 mg / 5ml",
            isColdChain = false,
            isControlledSubstance = false,
            unitType = "زجاجة معلق سائل",
            unitsPerBox = 1,
            price = 4800.0,
            description = "مضاد حيوي مركب للأطفال لعلاج التهاب اللوزتين والأذن الوسطى."
        )
    )

    fun getPharmaProducts(onResult: (List<PharmaProduct>) -> Unit) {
        scope.launch {
            try {
                val list = SupabaseClientProvider.client.postgrest["products"]
                    .select()
                    .decodeList<PharmaProduct>()
                withContext(Dispatchers.Main) {
                    if (list.isNotEmpty()) {
                        onResult(list)
                    } else {
                        // Seed Supabase if empty
                        fallbackPharmaProducts.forEach { prod ->
                            try {
                                SupabaseClientProvider.client.postgrest["products"].upsert(prod)
                            } catch (ex: Exception) {
                                Log.e("SUPABASE_DEBUG", "getPharmaProducts seeding prod ${prod.productId} warning: ${ex.message}")
                            }
                        }
                        onResult(fallbackPharmaProducts)
                    }
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
        
        // Update local memory list
        val index = fallbackPharmaProducts.indexOfFirst { it.productId == finalProduct.productId }
        if (index != -1) {
            fallbackPharmaProducts[index] = finalProduct
        } else {
            fallbackPharmaProducts.add(finalProduct)
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
                    onResult(true)
                }
            }
        }
    }

    fun deletePharmaProduct(productId: String, onResult: (Boolean) -> Unit) {
        fallbackPharmaProducts.removeAll { it.productId == productId }
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
                    onResult(true)
                }
            }
        }
    }

    val mockWarehouseInventory = mutableListOf<WarehouseInventoryItem>(
        WarehouseInventoryItem("AMX-500-CAP", "أموكسيسيلين 500 ملجم (أمبيسيل)", "كبسولات (Capsule)", 120, "2028-09-30", "branch_sanaa"),
        WarehouseInventoryItem("INS-ACT-INJ", "إنسولين أكتRapid مبرد 💉", "حقن (Injection)", 3, "2027-04-15", "branch_sanaa"),
        WarehouseInventoryItem("FEN-50-INJ", "فنتانيل حقن مخدرة ⚠️ (مقيد)", "حقن (Injection)", 15, "2028-06-20", "branch_sanaa"),
        WarehouseInventoryItem("PAR-500-TAB", "باراسيتامول 500 ملجم الشفاء", "أقراص (Tablet)", 45, "2028-05-12", "branch_sanaa"),
        WarehouseInventoryItem("CEF-1G-INJ", "سيف ترياكسون 1 جرام حقن", "حقن (Injection)", 25, "2027-08-25", "branch_sanaa"),
        WarehouseInventoryItem("ATO-20-TAB", "أتورفاستاتين 20 ملجم (ليبيتور)", "أقراص (Tablet)", 28, "2028-02-18", "branch_sanaa"),
        WarehouseInventoryItem("ATO-10-TAB", "أتورفاستاتين 10 ملجم (منتهي)", "أقراص (Tablet)", 0, "2026-12-31", "branch_sanaa"),
        WarehouseInventoryItem("VEN-100-INH", "فنتولين بخاخ للربو 🌬️", "بخاخ ربو (Inhaler)", 14, "2028-10-05", "branch_sanaa"),
        WarehouseInventoryItem("AUG-312-SYR", "أوجمنتين شراب معلق للأطفال", "شراب (Syrup)", 8, "2027-11-20", "branch_sanaa")
    )

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

                    // Update local memory fallback list
                    val index = mockWarehouseInventory.indexOfFirst { it.sku == sku && it.branchId == branchId }
                    if (index != -1) {
                        mockWarehouseInventory[index] = updatedItem
                    } else {
                        mockWarehouseInventory.add(updatedItem)
                    }
                    withContext(Dispatchers.Main) {
                        onResult(true)
                    }
                } catch (e: Exception) {
                    Log.e("SUPABASE_DEBUG", "updateInventoryQuantity failed: ${e.message} | ${e.stackTraceToString()}")
                    // Fallback to updating memory and return true
                    val index = mockWarehouseInventory.indexOfFirst { it.sku == sku && it.branchId == branchId }
                    val currentItem = if (index != -1) mockWarehouseInventory[index] else null
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

                    if (index != -1) {
                        mockWarehouseInventory[index] = updatedItem
                    } else {
                        mockWarehouseInventory.add(updatedItem)
                    }
                    withContext(Dispatchers.Main) {
                        onResult(true)
                    }
                }
            }
        }
    }

    // --- Promotional Offers Mock Data & Supabase Integration ---
    val fallbackPromotionalOffers = mutableListOf<PromotionalOffer>(
        PromotionalOffer(
            offerId = "offer_1",
            productId = "prod_1",
            productName = "أموكسيسيلين 500 ملجم (أمبيسيل)",
            title = "خصم الشفاء الخاص",
            description = "عرض خاص بمناسبة فصل الصيف على مضاد يدكو الشهير",
            discountPercent = 15.0,
            specialPrice = 1275.0,
            startDate = System.currentTimeMillis() - 86400000L * 2, // منذ يومين
            endDate = System.currentTimeMillis() + 86400000L * 10,   // لـ 10 أيام قادمة
            targetGovernorate = "", // الكل
            isActive = true,
            createdAt = System.currentTimeMillis()
        ),
        PromotionalOffer(
            offerId = "offer_2",
            productId = "prod_2",
            productName = "إنسولين أكتRapid مبرد 💉",
            title = "عرض العاصمة الخاص",
            description = "سعر مخفض للمؤسسات الصحية في محافظة صنعاء",
            discountPercent = 10.0,
            specialPrice = 7650.0,
            startDate = System.currentTimeMillis() - 86400000L,
            endDate = System.currentTimeMillis() + 86400000L * 5,
            targetGovernorate = "صنعاء", // مخصص لصنعاء
            isActive = true,
            createdAt = System.currentTimeMillis()
        )
    )

    fun createOffer(offer: PromotionalOffer, onResult: (Boolean) -> Unit) {
        val finalOffer = if (offer.offerId.isBlank()) {
            offer.copy(offerId = "offer_" + System.currentTimeMillis())
        } else {
            offer
        }
        fallbackPromotionalOffers.add(finalOffer)
        scope.launch {
            try {
                SupabaseClientProvider.client.postgrest["promotional_offers"].upsert(finalOffer)
                withContext(Dispatchers.Main) {
                    onResult(true)
                }
            } catch (e: Exception) {
                Log.e("SUPABASE_DEBUG", "createOffer failed: ${e.message} | ${e.stackTraceToString()}")
                withContext(Dispatchers.Main) {
                    onResult(true)
                }
            }
        }
    }

    fun updateOffer(offer: PromotionalOffer, onResult: (Boolean) -> Unit) {
        val index = fallbackPromotionalOffers.indexOfFirst { it.offerId == offer.offerId }
        if (index != -1) {
            fallbackPromotionalOffers[index] = offer
        } else {
            fallbackPromotionalOffers.add(offer)
        }
        scope.launch {
            try {
                SupabaseClientProvider.client.postgrest["promotional_offers"].upsert(offer)
                withContext(Dispatchers.Main) {
                    onResult(true)
                }
            } catch (e: Exception) {
                Log.e("SUPABASE_DEBUG", "updateOffer failed: ${e.message} | ${e.stackTraceToString()}")
                withContext(Dispatchers.Main) {
                    onResult(true)
                }
            }
        }
    }

    fun deleteOffer(offerId: String, onResult: (Boolean) -> Unit) {
        fallbackPromotionalOffers.removeAll { it.offerId == offerId }
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
                    onResult(true)
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
                    if (list.isNotEmpty()) {
                        val currentTime = System.currentTimeMillis()
                        val filtered = list.filter { offer ->
                            val isDateValid = currentTime >= offer.startDate && currentTime <= offer.endDate
                            val isGovValid = offer.targetGovernorate.isBlank() || offer.targetGovernorate == governorate
                            isDateValid && isGovValid
                        }
                        onResult(filtered)
                    } else {
                        // Seed Supabase if empty
                        fallbackPromotionalOffers.forEach { offer ->
                            try {
                                SupabaseClientProvider.client.postgrest["promotional_offers"].upsert(offer)
                            } catch (ex: Exception) {
                                Log.e("SUPABASE_DEBUG", "getActiveOffers seeding offer ${offer.offerId} warning: ${ex.message}")
                            }
                        }
                        val currentTime = System.currentTimeMillis()
                        val filtered = fallbackPromotionalOffers.filter { offer ->
                            val isDateValid = currentTime >= offer.startDate && currentTime <= offer.endDate
                            val isGovValid = offer.targetGovernorate.isBlank() || offer.targetGovernorate == governorate
                            offer.isActive && isDateValid && isGovValid
                        }
                        onResult(filtered)
                    }
                }
            } catch (e: Exception) {
                Log.e("SUPABASE_DEBUG", "getActiveOffers failed: ${e.message} | ${e.stackTraceToString()}")
                withContext(Dispatchers.Main) {
                    onResult(emptyList())
                }
            }
        }
    }

    // تحويل يدوي أو تلقائي لكامل الطلب: يعيد توجيه targetBranches بالكامل لفرع جديد، الطلب يختفي من قائمة الفرع الحالي ويظهر عند الفرع الجديد فوراً
    fun transferFullOrder(orderId: String, newBranchId: String, onResult: (Boolean) -> Unit) {
        val idx = fallbackOrders.indexOfFirst { it.orderId == orderId }
        if (idx != -1) {
            val originalOrder = fallbackOrders[idx]
            val updatedOrder = originalOrder.copy(targetBranches = listOf(newBranchId))
            fallbackOrders[idx] = updatedOrder
            
            scope.launch {
                try {
                    SupabaseClientProvider.client.postgrest["orders"].upsert(updatedOrder)
                    withContext(Dispatchers.Main) {
                        onResult(true)
                    }
                } catch (e: Exception) {
                    Log.e("SUPABASE_DEBUG", "transferFullOrder failed: ${e.message} | ${e.stackTraceToString()}")
                    withContext(Dispatchers.Main) {
                        onResult(true)
                    }
                }
            }
        } else {
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
                    fallbackOrders.add(updatedOrder)
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
    }

    // تحويل جزئي (يدوي أو تلقائي): ينشئ طلباً فرعياً جديداً يحوي فقط الأصناف المحوَّلة (orderLines مطابقة)، بـ targetBranches = [newBranchId] و parentOrderId = orderId الأصلي، ويحذف تلك الأصناف تحديداً من orderLines في الطلب الأصلي (يبقى الطلب الأصلي بباقي الأصناف فقط عند الفرع الحالي)
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
            
            // Update local memory list
            val idx = fallbackOrders.indexOfFirst { it.orderId == originalOrder.orderId }
            if (idx != -1) {
                fallbackOrders[idx] = updatedOriginalOrder
            }
            fallbackOrders.add(subOrder)
            
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
                        onDone(true)
                    }
                }
            }
        }

        val idx = fallbackOrders.indexOfFirst { it.orderId == orderId }
        if (idx != -1) {
            processOrderTransfer(fallbackOrders[idx], onResult)
        } else {
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
    }

    // الخيار الذكي: يحسب أقرب فرع (باستخدام calculateDistanceKm الموجودة أصلاً) من بين كل الفروع عدا الفرع الحالي، الذي لديه مخزون كافٍ (من warehouse_inventory) لكل الأصناف المطلوبة (أو الأصناف الناقصة فقط حسب الوضع)، ويستدعي الدالة المناسبة أعلاه تلقائياً. إن لم يوجد أي فرع مطابق، أرجع خطأ واضحاً "لا يوجد فرع بديل متوفر لهذه الأصناف حالياً" ليتعامل معه مدير الفرع يدوياً.
    fun smartTransferOrder(orderId: String, partialOnly: Boolean, onResult: (Boolean, String?) -> Unit) {
        val oIdx = fallbackOrders.indexOfFirst { it.orderId == orderId }
        if (oIdx == -1) {
            onResult(false, "لا يوجد فرع بديل متوفر لهذه الأصناف حالياً")
            return
        }
        val order = fallbackOrders[oIdx]
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
                        // Identify items where current branch has insufficient stock
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
                    // Full transfer
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
    }
}

