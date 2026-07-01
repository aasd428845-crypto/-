package com.example.model

import kotlinx.serialization.Serializable

// ==========================================
// 🏢 النظام التعريفي والتنظيمي للمؤسسة والشركاء (Corporate & Identity)
// ==========================================

/**
 * يمثل الهيكل المالي والقانوني للشركة الأم المالكة لسلسلة التوزيع.
 * يتضمن التراخيص والتفاصيل التجارية الكلية.
 */
@Serializable
data class CompanyInfo(
    val companyId: String = "main_company",
    val companyName: String = "",
    val companyNameEn: String = "",
    val logoUrl: String = "",
    val phone: String = "",
    val email: String = "",
    val mainAddress: String = "",
    val foundedYear: String = "",
    val licenseNumber: String = "",
    val totalBranches: Int = 0,
    val isActive: Boolean = true
)

/**
 * الفروع ومراكز التوزيع والمستودعات اللوجستية الإقليمية لتوريد الأدوية.
 * تم تحسينه ليشمل مسارات التوصيل اللوجستي المغطاة بواسطة كل فرع.
 */
@Serializable
data class Branch(
    val branchId: String = "",
    val branchName: String = "",
    val governorate: String = "",
    val city: String = "",
    val address: String = "",
    val phone: String = "",
    val managerId: String = "",
    val managerName: String = "",
    val managerPhone: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val isActive: Boolean = true,
    val createdAt: Long = 0L,

    // ⚡ المتطلبات الجديدة: مسارات التوصيل اللوجستية المسندة للفرع لتغطية الصيدليات والمستشفيات جغرافياً
    val assignedRoutes: List<String> = emptyList()
)

/**
 * المستخدم النهائي في تطبيق B2B (صيدلي، مدير مستشفى، مدير فرع، أو مدير عام الشفاء).
 * تم تحديثه ليشمل نوع المنشأة الطبي الصارم، رقم الترخيص الصيدلي، والحساب الائتماني والمالي الشامل.
 */
@Serializable
data class User(
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "", // "client", "branch_manager", "company_director"
    val clientType: String = "", // "hospital" أو "pharmacy" للعملاء فقط (للتوافق الرجعي)
    val city: String = "",
    val governorate: String = "",
    val phone: String = "",
    val orgName: String = "",
    val branchId: String = "", // لمدراء الفروع فقط
    val branchName: String = "", // اسم الفرع
    val isVerified: Boolean = false,
    val isActive: Boolean = true,
    val profileImageUrl: String = "",
    val createdAt: Long = 0L,

    // ⚡ المتطلبات الجديدة: تصنيف المنشأة كـ Enum رسمي لأغراض الرقابة وتوريد الأصناف المقيدة والباردة
    val facilityType: FacilityType = FacilityType.PHARMACY,
    
    // ⚡ رقم الترخيص الصيدلي والمهني الصادر من الهيئة العليا للأدوية لضمان قانونية العمليات التجارية
    val licenseNumber: String = "",
    
    // ⚡ الحساب الائتماني والمالي الذي يربط العميل بحدود الديون والآجال وميزان المراجعة
    val clientAccount: ClientAccount = ClientAccount()
)

/**
 * نوع المنشأة الطبية الشريكة في منظومة التوريد B2B.
 * يسهم في تحديد شروط الدفع وقوانين بيع الأدوية المقيدة أو الباردة.
 */
enum class FacilityType {
    HOSPITAL,     // مستشفى حكومي أو خاص 🏥
    PHARMACY,     // صيدلية تجارية فرعية 💊
    CLINIC,       // عيادة تخصصية أو مركز طبي مصغر
    DISTRIBUTOR   // موزع جملة فرعي أو وكيل إقليمي
}

/**
 * العناوين والمستودعات الجغرافية الخاصة بالعملاء لاستلام الشحنات الدوائية الحساسة.
 */
@Serializable
data class UserAddress(
    val addressId: String = "",
    val userId: String = "",
    val userType: String = "", // "hospital" or "supplier"
    val label: String = "", // e.g. "المقر الرئيسي"
    val hospitalOrCompanyName: String = "",
    val nearbyLandmark: String = "", // معلم بارز
    val governorate: String = "", // المحافظة
    val district: String = "", // المديرية
    val neighborhood: String = "", // الحي
    val fullAddress: String = "",
    val latitude: Double = 15.3482,
    val longitude: Double = 44.2191,
    val isDefault: Boolean = false,
    val createdAt: Long = 0L
)


// ==========================================
// 💊 نظام الأصناف والمستحضرات الطبية والدوائية (Pharma Product System)
// ==========================================

/**
 * الأشكال الصيدلانية وطريقة أخذ الجرعات الدوائية للمستحضرات.
 */
enum class DosageForm {
    TABLET,       // أقراص فموية
    CAPSULE,      // كبسولات جيلاتينية
    INJECTION,    // حقن أمبولات أو فيال (وريدي/عضلي)
    SYRUP,        // شراب سائل فموي
    OINTMENT,     // مرهم جلدي موضعي
    CREAM,        // كريم ترطيبي طبي
    SUSPENSION,   // معلق دوائي سائل يحتاج رج
    INHALER       // بخاخ طبي استنشاقي للرئة
}

/**
 * حالة التوافر ومراقبة تذبذب المخزون للأدوية والمستلزمات الطبية.
 */
enum class InventoryStatus {
    AVAILABLE,       // متوفر للشراء الفوري والتسليم اللوجستي السريع
    LOW_STOCK,       // مخزون حرج (تنبيه تلقائي لإعادة التوريد والطلب)
    OUT_OF_STOCK,    // نفدت الكمية بالكامل من مستودعات الفرع
    EXPECTED_SOON    // غير متوفر حالياً ولكنه قيد الشحن الخارجي وقريب الوصول
}

/**
 * يمثل الصنف الدوائي والمستحضر الطبي بكافة تفاصيله الرقابية واللوجستية وفق معايير B2B الدولية لشركات الأدوية.
 */
@Serializable
data class PharmaProduct(
    val productId: String = "", // المعرف الفريد للمنتج
    val sku: String = "", // كود وحدة حفظ المخزون (Stock Keeping Unit)
    val ndcCode: String = "", // كود الدواء الوطني القياسي المعتمد عالمياً (National Drug Code)
    val commercialName: String = "", // الاسم التجاري باللغتين (مثال: بنادول اكسترا)
    val scientificName: String = "", // الاسم العلمي والمادة الفعالة (مثال: باراسيتامول)
    val manufacturer: String = "", // الشركة الطبية المصنعة (مثال: GSK)
    val dosageForm: DosageForm = DosageForm.TABLET, // الشكل الصيدلاني للمستحضر
    val strength: String = "", // التركيز الكيميائي والجرعة (مثال: 500 ملغ)
    val isColdChain: Boolean = false, // هل يتطلب سلسلة تبريد ونقل مبرد دقيق (2-8 درجة مئوية)؟
    val isControlledSubstance: Boolean = false, // هل هو دواء مراقَب وخاضع لرقابة مكافحة المخدرات والوزارة؟
    val unitType: String = "Box", // نوع تعبئة الوحدة الصيدلانية (Box, Ampoule, Bottle, Vial)
    val unitsPerBox: Int = 1, // عدد القطع أو الشرائط أو الأمبولات داخل الصندوق الواحد
    val price: Double = 0.0, // سعر البيع الأساسي المعتمد للصيدليات والمستشفيات
    val description: String = "" // الوصف والتحذيرات الدوائية المرافقة
)

/**
 * التصنيفات العلاجية والصيدلانية للأدوية (مثل: المضادات الحيوية، مسكنات الآلام، أدوية السكري).
 */
@Serializable
data class Category(
    val categoryId: String = "",
    val nameAr: String = "", // الاسم بالعربية (المضادات الحيوية)
    val nameEn: String = "", // الاسم بالإنجليزية (Antibiotics)
    val description: String = "",
    val isActive: Boolean = true
)

/**
 * نموذج التوافق الرجعي القديم مع كلاس Medicine للتأكد من عدم حدوث أي انهيار برمجي في التطبيق الحالي.
 */
@Serializable
data class Medicine(
    val medicineId: String = "",
    val name: String = "",
    val category: String = "",
    val price: Double = 0.0,
    val quantity: Int = 0,
    val supplierId: String = ""
)


// ==========================================
// 🛒 نظام المشتريات والطلبات والمناقصات (Cart & Order System)
// ==========================================

/**
 * يمثل عنصراً داخل سلة الشراء المؤسسية الخاصة بالصيدلية أو المستشفى.
 */
@Serializable
data class CartItem(
    val product: PharmaProduct = PharmaProduct(), // المستحضر الدوائي الشامل
    val quantity: Int = 0, // الكمية الكلية المطلوبة للتوريد
    val addedPrice: Double = 0.0 // السعر المحدد لحظة الإضافة للسلة لتفادي تغيرات السوق اللحظية
)

/**
 * يمثل بنوداً وتفاصيل كمية وسعرية منفردة داخل طلبيات الشراء الدوائية.
 * يراعي بنية التوريد واللوجستيات B2B التي تتيح الشحن الجزئي.
 */
@Serializable
data class OrderLine(
    val lineId: String = "", // كود البند الفريد
    val product: PharmaProduct = PharmaProduct(), // المستحضر الدوائي المسعر
    val requestedQty: Int = 0, // الكمية التي طلبها العميل في الأصل
    val shippedQty: Int = 0, // الكمية التي تم تجهيزها وشحنها وتأكيد استلامها فعلياً (تسمح بالشحن الجزئي)
    val unitPrice: Double = 0.0, // سعر القطعة الفعلي المتفق عليه
    val totalPrice: Double = 0.0 // الإجمالي المالي للبند الحالي (الالكمية المشحونة * سعر الوحدة)
)

/**
 * الحالات الصارمة لدورة حياة طلبيات الأدوية وتجهيزها في مستودعات B2B للأدوية.
 */
@Serializable
sealed class OrderStatus {
    @Serializable
    data object Draft : OrderStatus() // مسودة قيد المراجعة بواسطة الصيدلي ولم ترسل بعد
    
    @Serializable
    data object Submitted : OrderStatus() // تم إرسال الطلب وإتاحته للفروع والمستودعات للمزايدة والتسعير
    
    @Serializable
    data object Allocated : OrderStatus() // تم تخصيص وحجز المنتجات والكميات بنجاح من مستودعات الفروع
    
    @Serializable
    data object PartiallyShipped : OrderStatus() // تم شحن وتوريد جزء من الكميات المطلوبة نظراً لندرة الأصناف
    
    @Serializable
    data object Invoiced : OrderStatus() // تمت الفوترة وإصدار المطالبة المالية رسمياً للعميل للمراجعة والتسديد
    
    @Serializable
    data object Delivered : OrderStatus() // تم تسليم الشحنة وتوقيع إيصال الاستلام ومطابقة درجات الحرارة
}

/**
 * الطلبية الطبية الرسمية الشاملة للرعاية الصحية والتوريد والمزايدات اللحظية.
 * تم تصميمها بحيث تدعم مسار البيانات الجديد بكفاءة متناهية، وتحتفظ بكافة الحقول القديمة لضمان توافقية النظام المطلقة مع واجهات المستخدم.
 */
@Serializable
data class Order(
    // 🏢 الهيكل المؤسسي الحديث (B2B Enterprise Mode)
    val orderId: String = "", // معرف الطلب الفريد
    val clientId: String = "", // معرف المنشأة الطبية (العميل)
    val orderLines: List<OrderLine> = emptyList(), // الأسطر والبنود الدوائية التفصيلية (تدعم الشحن الجزئي)
    val orderStatus: OrderStatus = OrderStatus.Submitted, // دورة حياة الطلب عبر Sealed Class
    val totalAmount: Double = 0.0, // إجمالي المبلغ المالي المستحق للطلب
    val deliveryRouteId: String = "", // كود المسار اللوجستي للتوصيل والشاحنة المخصصة
    val createdAt: Long = 0L, // تاريخ إنشاء وتثبيت الطلب

    // 🔄 حقول التوافقية الرجعية مع الكود القديم (Backward Compatibility Layer - لتفادي انهيار الـ UI)
    val clientName: String = "",
    val clientType: String = "", // hospital / pharmacy
    val clientGovernorate: String = "",
    val orderContent: String = "", // الوصف النصي المجمع للاحتياج
    val attachments: List<String> = emptyList(),
    val urgencyLevel: String = "normal", // normal, high, critical
    val broadcastType: String = "all", // all, nearby, selected
    val targetBranches: List<String> = emptyList(),
    val status: String = "broadcast", // broadcast, offer_received, negotiating, confirmed, delivered
    val priceOfferId: String = "",
    val hospitalId: String = "",
    val supplierId: String = "",
    val medicineName: String = "",
    val price: Double = 0.0,
    val quantity: Int = 0,
    val deliveryMethod: String = "", // "self" or "platform"
    val deliveryScheduledDate: String = ""
)


// ==========================================
// 💵 النظام المالي والمحاسبي والائتماني للشركات (B2B Financial System)
// ==========================================

/**
 * حالة سداد الفاتورة المالية الصادرة للعميل.
 */
enum class PaymentStatus {
    UNPAID,         // غير مدفوعة ومستحقة السداد بالكامل
    PARTIALLY_PAID, // تم سداد دفعة مالية جزئية والباقي معلق بالائتمان والمقاصة
    PAID            // تمت التسوية المالية الكاملة وأغلقت الفاتورة بنجاح
}

/**
 * الفاتورة المالية والضريبية الرسمية المرتبطة بطلبيات الشراء والتوريد المؤسسي للأدوية.
 */
@Serializable
data class Invoice(
    val invoiceId: String = "", // المعرف الفريد للفاتورة المالية
    val orderId: String = "", // كود الطلبية القانونية المرتبطة بهذه الفاتورة
    val totalAmount: Double = 0.0, // الإجمالي المالي المطلوب بعد الضرائب والخصومات
    val dueDate: Long = 0L, // تاريخ استحقاق السداد المالي (Timestamp)
    val paymentStatus: PaymentStatus = PaymentStatus.UNPAID, // الحالة الراهنة للتسديد والدفع
    val taxAmount: Double = 0.0, // مقدار الضريبة الطبية والمجتمعية المضافة للفاتورة
    val discountAmount: Double = 0.0, // الخصم التجاري الممنوح للمؤسسة الطبية
    val billingAddress: String = "", // عنوان إرسال الفواتير والمطالبات القانوني
    val issuedAt: Long = 0L // تاريخ ووقت إصدار وتثبيت الفاتورة
)

/**
 * فترات وآجال شروط الدفع والائتمان المسموح بها للمنشآت الطبية المتعاقدة.
 */
enum class PaymentTerms {
    NET30,             // السداد الكامل خلال 30 يوماً كأقصى حد من تاريخ الفاتورة
    NET60,             // السداد الكامل خلال 60 يوماً كأقصى حد من تاريخ الفاتورة
    CASH_ON_DELIVERY,  // التسوية النقدية المباشرة فور تسلم الطلب والكميات
    PREPAID            // الدفع والتخليص المالي المسبق قبل مغادرة البضاعة للمستودع
}

/**
 * الحساب المالي والائتماني المتقدم المخصص لكل عميل مؤسسي شريك (B2B Account Ledger).
 * يراقب السقف الائتماني المعتمد والديون والميزانية المستمرة لمنع تضخم مديونيات الفروع والعملاء.
 */
@Serializable
data class ClientAccount(
    val creditLimit: Double = 1000000.0, // السقف الائتماني الأقصى بالعملة المحلية (مثال: مليون ريال يمني)
    val currentBalance: Double = 0.0, // الرصيد الحالي للعميل (المستحقات المعلقة على الصيدلية/المستشفى)
    val paymentTerms: PaymentTerms = PaymentTerms.CASH_ON_DELIVERY, // شروط السداد المعتمدة والمبرمة بالاتفاقية
    val currency: String = "YER", // العملة الرسمية المعتمدة للحساب والمقاصة (YER أو USD)
    val lastPaymentDate: Long = 0L, // تاريخ سداد آخر دفعة مالية ناجحة من العميل
    val isActive: Boolean = true // هل الحساب الائتماني والتعاقد ساري وصالح للتشغيل والتوريد الآجل؟
)


// ==========================================
// 🤝 العروض والمناقصات والعطاءات (Bidding & Offers)
// ==========================================

/**
 * عرض مالي ولوجستي بديل (أو كلاس PriceOffer السابق) لضمان توافق عمليات التقديم والشراء.
 */
@Serializable
data class PriceOffer(
    val priceOfferId: String = "",
    val broadcastId: String = "",
    val supplierId: String = "",
    val supplierName: String = "",
    val medicineId: String = "",
    val medicineName: String = "",
    val price: Double = 0.0,
    val quantity: Int = 0,
    val shippingCost: Double = 0.0,
    val notes: String = "",
    val status: String = "pending", // pending, accepted, rejected, negotiating
    val deliveryAddressId: String = "",
    val deliveryAddressLabel: String = "",
    val deliveryFullAddress: String = "",
    val distance: Double = 0.0,
    val eta: String = "",
    val deliveryMethod: String = "", // "self" or "platform"
    val createdAt: Long = 0L
)

/**
 * العطاء والمسعرة المالية المقدمة من فروع ومستودعات "الشفاء" للرد على احتياجات العملاء.
 */
@Serializable
data class BranchOffer(
    val offerId: String = "",
    val orderId: String = "",
    val branchId: String = "",
    val branchName: String = "",
    val managerId: String = "",
    val managerName: String = "",
    val offerDetails: String = "",
    val totalPrice: Double = 0.0,
    val currency: String = "YER",
    val deliveryDays: Int = 0,
    val shippingCost: Double = 0.0,
    val paymentTerms: String = "",
    val attachmentUrl: String = "",
    val notes: String = "",
    val status: String = "pending", // pending/accepted/rejected/negotiating
    val createdAt: Long = 0L
)


// ==========================================
// 🏦 الحسابات والبيانات البنكية وحركات الدفع الإلكتروني (Financial Transactions)
// ==========================================

@Serializable
data class Payment(
    val paymentId: String = "",
    val orderId: String = "",
    val hospitalId: String = "",
    val hospitalName: String = "",
    val supplierId: String = "",
    val supplierName: String = "",
    val amount: Double = 0.0,
    val currency: String = "YER", // YER or USD
    val paymentMethod: String = "", // e.g. "البنك الكريمي", "كاش يمن موبايل"
    val receiptUrl: String = "", // إيصال التحويل المالي أو صورة السداد
    val receiptNote: String = "",
    val status: String = "pending", // pending, confirmed, rejected
    val adminVisible: Boolean = true,
    val commissionAmount: Double = 0.0,
    val commissionRate: Double = 0.05,
    val commissionStatus: String = "pending"
)

@Serializable
data class BankAccount(
    val accountId: String = "",
    val userId: String = "",
    val bankName: String = "",
    val accountNumber: String = "",
    val accountHolderName: String = "",
    val walletType: String = "bank", // "bank", "mfs"
    val walletNumber: String = "",
    val isDefault: Boolean = false,
    val createdAt: Long = 0L
)


// ==========================================
// 🚚 الإمداد اللوجستي والنقل والتوجيه (Logistics & Distribution Routing)
// ==========================================

@Serializable
data class DeliveryRequest(
    val deliveryId: String = "",
    val orderId: String = "",
    val hospitalId: String = "",
    val supplierId: String = "",
    val pickupAddress: String = "",
    val pickupLat: Double = 15.3482,
    val pickupLng: Double = 44.2191,
    val deliveryAddress: String = "",
    val deliveryLat: Double = 15.3482,
    val deliveryLng: Double = 44.2191,
    val distance: Double = 0.0,
    val estimatedPrice: Double = 0.0,
    val urgencyLevel: String = "normal", // normal, high, critical
    val packageSize: String = "medium", // small, medium, large
    val packageImageUrl: String = "",
    val status: String = "pending", // pending, assigned, picked, delivered
    val adminAssigned: Boolean = false,
    val createdAt: Long = 0L
)

@Serializable
data class DeliverySchedule(
    val scheduleId: String = "",
    val orderId: String = "",
    val supplierAvailableTimes: List<String> = emptyList(),
    val hospitalPreferredTimes: List<String> = emptyList(),
    val agreedDateTime: String = "",
    val status: String = "negotiating", // negotiating, agreed
    val updatedAt: Long = 0L
)

@Serializable
data class OrderRouting(
    val orderId: String = "",
    val clientId: String = "",
    val targetBranches: List<String> = emptyList(),
    val routingType: String = "", // "all", "nearby", "selected"
    val routingReason: String = "",
    val createdAt: Long = 0L
)


// ==========================================
// 👤 ملفات التعريف والبيانات الشخصية لشركاء المنصة (Users & Profiles)
// ==========================================

@Serializable
data class ClientProfile(
    val clientId: String = "",
    val userId: String = "",
    val institutionName: String = "",
    val clientType: String = "", // hospital / pharmacy
    val responsiblePerson: String = "",
    val phone: String = "",
    val alternatePhone: String = "",
    val licenseNumber: String = "",
    val licenseImageUrl: String = "",
    val governorate: String = "",
    val city: String = "",
    val district: String = "",
    val neighborhood: String = "",
    val landmark: String = "",
    val fullAddress: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val assignedBranchId: String = "",
    val assignedBranchName: String = "",
    val preferredPayment: String = "",
    val paymentAccount: String = "",
    val isVerified: Boolean = false,
    val isActive: Boolean = true,
    val profileCompleted: Boolean = false,
    val joinedAt: Long = 0L,
    val lastOrderAt: Long = 0L,
    val totalOrders: Int = 0,
    val totalSpent: Double = 0.0
)

@Serializable
data class BranchManagerProfile(
    val userId: String = "",
    val fullName: String = "",
    val phone: String = "",
    val nationalIdImageUrl: String = "",
    val warehouseLat: Double = 0.0,
    val warehouseLng: Double = 0.0,
    val bankAccounts: List<BankAccount> = emptyList(),
    val profileCompleted: Boolean = false,
    val joinedAt: Long = 0L
)


// ==========================================
// 🔔 نظام الإشعارات والتنبيهات الإدارية (Administrative Notifications)
// ==========================================

@Serializable
data class DirectorNotification(
    val notificationId: String = "",
    val title: String = "",
    val message: String = "",
    val orderId: String = "",
    val clientId: String = "",
    val clientName: String = "",
    val createdAt: Long = 0L,
    val read: Boolean = false
)
