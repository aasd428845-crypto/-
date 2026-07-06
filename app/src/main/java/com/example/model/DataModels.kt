package com.example.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

// ==========================================
// 🏢 النظام التعريفي والتنظيمي للمؤسسة والشركاء (Corporate & Identity)
// ==========================================

/**
 * يمثل الهيكل المالي والقانوني للشركة الأم المالكة لسلسلة التوزيع.
 * يتضمن التراخيص والتفاصيل التجارية الكلية.
 */
@Serializable
data class CompanyInfo(
    @SerialName("id") val companyId: String = "main_company",
    @SerialName("company_name") val companyName: String = "",
    @SerialName("company_name_en") val companyNameEn: String = "",
    @SerialName("logo_url") val logoUrl: String = "",
    val phone: String = "",
    val email: String = "",
    @SerialName("main_address") val mainAddress: String = "",
    @SerialName("founded_year") val foundedYear: String = "",
    @SerialName("license_number") val licenseNumber: String = "",
    @SerialName("total_branches") val totalBranches: Int = 0,
    @SerialName("is_active") val isActive: Boolean = true
)

/**
 * الفروع ومراكز التوزيع والمستودعات اللوجستية الإقليمية لتوريد الأدوية.
 * تم تحسينه ليشمل مسارات التوصيل اللوجستي المغطاة بواسطة كل فرع.
 */
@Serializable
data class Branch(
    @SerialName("id") val branchId: String = "",
    @SerialName("branch_name") val branchName: String = "",
    val governorate: String = "",
    val city: String = "",
    val address: String = "",
    val phone: String = "",
    @SerialName("manager_id") val managerId: String = "",
    @SerialName("manager_name") val managerName: String = "",
    @SerialName("manager_phone") val managerPhone: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("created_at") val createdAt: Long = 0L,

    // ⚡ المتطلبات الجديدة: مسارات التوصيل اللوجستية المسندة للفرع لتغطية الصيدليات والمستشفيات جغرافياً
    @SerialName("assigned_routes") val assignedRoutes: List<String> = emptyList()
)

/**
 * المستخدم النهائي في تطبيق B2B (صيدلي، مدير مستشفى، مدير فرع، أو مدير عام الشفاء).
 * تم تحديثه ليشمل نوع المنشأة الطبي الصارم، رقم الترخيص الصيدلي، والحساب الائتماني والمالي الشامل.
 */
@Serializable
data class User(
    @SerialName("id") val userId: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "", // "client", "branch_manager", "company_director"
    @SerialName("client_type") val clientType: String = "", // "hospital" أو "pharmacy" للعملاء فقط (للتوافق الرجعي)
    val city: String = "",
    val governorate: String = "",
    val phone: String = "",
    @SerialName("org_name") val orgName: String = "",
    @SerialName("branch_id") val branchId: String = "", // لمدراء الفروع فقط
    @SerialName("branch_name") val branchName: String = "", // اسم الفرع
    @SerialName("is_verified") val isVerified: Boolean = false,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("profile_image_url") val profileImageUrl: String = "",
    @SerialName("created_at") val createdAt: Long = 0L,

    // ⚡ المتطلبات الجديدة: تصنيف المنشأة كـ Enum رسمي لأغراض الرقابة وتوريد الأصناف المقيدة والباردة
    @SerialName("facility_type") val facilityType: FacilityType = FacilityType.PHARMACY,
    
    // ⚡ رقم الترخيص الصيدلي والمهني الصادر من الهيئة العليا للأدوية لضمان قانونية العمليات التجارية
    @SerialName("license_number") val licenseNumber: String = "",
    
    // ⚡ الحساب الائتماني والمالي الذي يربط العميل بحدود الديون والآجال وميزان المراجعة
    @SerialName("client_account") val clientAccount: ClientAccount = ClientAccount()
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
    @SerialName("id") val addressId: String = "",
    @SerialName("user_id") val userId: String = "",
    @SerialName("user_type") val userType: String = "", // "hospital" or "supplier"
    val label: String = "", // e.g. "المقر الرئيسي"
    @SerialName("hospital_or_company_name") val hospitalOrCompanyName: String = "",
    @SerialName("nearby_landmark") val nearbyLandmark: String = "", // معلم بارز
    val governorate: String = "", // المحافظة
    val district: String = "", // المديرية
    val neighborhood: String = "", // الحي
    @SerialName("full_address") val fullAddress: String = "",
    val latitude: Double = 15.3482,
    val longitude: Double = 44.2191,
    @SerialName("is_default") val isDefault: Boolean = false,
    @SerialName("created_at") val createdAt: Long = 0L
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
    @SerialName("id") val productId: String = "", // المعرف الفريد للمنتج
    val sku: String = "", // كود وحدة حفظ المخزون (Stock Keeping Unit)
    @SerialName("ndc_code") val ndcCode: String = "", // كود الدواء الوطني القياسي المعتمد عالمياً (National Drug Code)
    @SerialName("commercial_name") val commercialName: String = "", // الاسم التجاري باللغتين (مثال: بنادول اكسترا)
    @SerialName("scientific_name") val scientificName: String = "", // الاسم العلمي والمادة الفعالة (مثال: باراسيتامول)
    val manufacturer: String = "", // الشركة الطبية المصنعة (مثال: GSK)
    @SerialName("dosage_form") val dosageForm: DosageForm = DosageForm.TABLET, // الشكل الصيدلاني للمستحضر
    val strength: String = "", // التركيز الكيميائي والجرعة (مثال: 500 ملغ)
    @SerialName("is_cold_chain") val isColdChain: Boolean = false, // هل يتطلب سلسلة تبريد ونقل مبرد دقيق (2-8 درجة مئوية)؟
    @SerialName("is_controlled_substance") val isControlledSubstance: Boolean = false, // هل هو دواء مراقَب وخاضع لرقابة مكافحة المخدرات والوزارة؟
    @SerialName("unit_type") val unitType: String = "Box", // نوع تعبئة الوحدة الصيدلانية (Box, Ampoule, Bottle, Vial)
    @SerialName("units_per_box") val unitsPerBox: Int = 1, // عدد القطع أو الشرائط أو الأمبولات داخل الصندوق الواحد
    val price: Double = 0.0, // سعر البيع الأساسي المعتمد للصيدليات والمستشفيات
    val description: String = "", // الوصف والتحذيرات الدوائية المرافقة
    @SerialName("is_active") val isActive: Boolean = true
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
    @SerialName("id") val lineId: String = "", // كود البند الفريد
    val product: PharmaProduct = PharmaProduct(), // المستحضر الدوائي المسعر
    @SerialName("requested_qty") val requestedQty: Int = 0, // الكمية التي طلبها العميل في الأصل
    @SerialName("shipped_qty") val shippedQty: Int = 0, // الكمية التي تم تجهيزها وشحنها وتأكيد استلامها فعلياً (تسمح بالشحن الجزئي)
    @SerialName("unit_price") val unitPrice: Double = 0.0, // سعر القطعة الفعلي المتفق عليه
    @SerialName("total_price") val totalPrice: Double = 0.0 // الإجمالي المالي للبند الحالي (الالكمية المشحونة * سعر الوحدة)
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
    @SerialName("id") val orderId: String = "", // معرف الطلب الفريد
    @SerialName("client_id") val clientId: String = "", // معرف المنشأة الطبية (العميل)
    @SerialName("order_lines") val orderLines: List<OrderLine> = emptyList(), // الأسطر والبنود الدوائية التفصيلية (تدعم الشحن الجزئي)
    @SerialName("order_status") val orderStatus: OrderStatus = OrderStatus.Submitted, // دورة حياة الطلب عبر Sealed Class
    @SerialName("total_amount") val totalAmount: Double = 0.0, // إجمالي المبلغ المالي المستحق للطلب
    @SerialName("delivery_route_id") val deliveryRouteId: String = "", // كود المسار اللوجستي للتوصيل والشاحنة المخصصة
    @SerialName("created_at") val createdAt: Long = 0L, // تاريخ إنشاء وتثبيت الطلب

    // 🔄 حقول التوافقية الرجعية مع الكود القديم (Backward Compatibility Layer - لتفادي انهيار الـ UI)
    @SerialName("client_name") val clientName: String = "",
    @SerialName("client_type") val clientType: String = "", // hospital / pharmacy
    @SerialName("client_governorate") val clientGovernorate: String = "",
    @SerialName("order_content") val orderContent: String = "", // الوصف النصي المجمع للاحتياج
    val attachments: List<String> = emptyList(),
    @SerialName("urgency_level") val urgencyLevel: String = "normal", // normal, high, critical
    @SerialName("broadcast_type") val broadcastType: String = "all", // all, nearby, selected
    @SerialName("target_branches") val targetBranches: List<String> = emptyList(),
    val status: String = "broadcast", // broadcast, offer_received, negotiating, confirmed, delivered
    @SerialName("price_offer_id") val priceOfferId: String = "",
    @SerialName("hospital_id") val hospitalId: String = "",
    @SerialName("supplier_id") val supplierId: String = "",
    @SerialName("medicine_name") val medicineName: String = "",
    val price: Double = 0.0,
    val quantity: Int = 0,
    @SerialName("delivery_method") val deliveryMethod: String = "", // "self" or "platform"
    @SerialName("delivery_scheduled_date") val deliveryScheduledDate: String = "",
    @SerialName("parent_order_id") val parentOrderId: String = "",       // فارغ في الطلب الأصلي، يشير لمعرف الطلب الأصلي في أي "طلب فرعي" ناتج عن تحويل جزئي
    @SerialName("scheduled_delivery_date") val scheduledDeliveryDate: Long = 0L // يُحدَّد لاحقاً في الجزء ج
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
    @SerialName("id") val invoiceId: String = "", // المعرف الفريد للفاتورة المالية
    @SerialName("order_id") val orderId: String = "", // كود الطلبية القانونية المرتبطة بهذه الفاتورة
    @SerialName("total_amount") val totalAmount: Double = 0.0, // الإجمالي المالي المطلوب بعد الضرائب والخصومات
    @SerialName("due_date") val dueDate: Long = 0L, // تاريخ استحقاق السداد المالي (Timestamp)
    @SerialName("payment_status") val paymentStatus: PaymentStatus = PaymentStatus.UNPAID, // الحالة الراهنة للتسديد والدفع
    @SerialName("tax_amount") val taxAmount: Double = 0.0, // مقدار الضريبة الطبية والمجتمعية المضافة للفاتورة
    @SerialName("discount_amount") val discountAmount: Double = 0.0, // الخصم التجاري الممنوح للمؤسسة الطبية
    @SerialName("billing_address") val billingAddress: String = "", // عنوان إرسال الفواتير والمطالبات القانوني
    @SerialName("issued_at") val issuedAt: Long = 0L // تاريخ ووقت إصدار وتثبيت الفاتورة
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
    @SerialName("credit_limit") val creditLimit: Double = 1000000.0, // السقف الائتماني الأقصى بالعملة المحلية (مثال: مليون ريال يمني)
    @SerialName("current_balance") val currentBalance: Double = 0.0, // الرصيد الحالي للعميل (المستحقات المعلقة على الصيدلية/المستشفى)
    @SerialName("payment_terms") val paymentTerms: PaymentTerms = PaymentTerms.CASH_ON_DELIVERY, // شروط السداد المعتمدة والمبرمة بالاتفاقية
    val currency: String = "YER", // العملة الرسمية المعتمدة للحساب والمقاصة (YER أو USD)
    @SerialName("last_payment_date") val lastPaymentDate: Long = 0L, // تاريخ سداد آخر دفعة مالية ناجحة من العميل
    @SerialName("is_active") val isActive: Boolean = true // هل الحساب الائتماني والتعاقد ساري وصالح للتشغيل والتوريد الآجل؟
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
    @SerialName("id") val offerId: String = "",
    @SerialName("order_id") val orderId: String = "",
    @SerialName("branch_id") val branchId: String = "",
    @SerialName("branch_name") val branchName: String = "",
    @SerialName("manager_id") val managerId: String = "",
    @SerialName("manager_name") val managerName: String = "",
    @SerialName("offer_details") val offerDetails: String = "",
    @SerialName("total_price") val totalPrice: Double = 0.0,
    val currency: String = "YER",
    @SerialName("delivery_days") val deliveryDays: Int = 0,
    @SerialName("shipping_cost") val shippingCost: Double = 0.0,
    @SerialName("payment_terms") val paymentTerms: String = "",
    @SerialName("attachment_url") val attachmentUrl: String = "",
    val notes: String = "",
    val status: String = "pending", // pending/accepted/rejected/negotiating
    @SerialName("created_at") val createdAt: Long = 0L
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

@Serializable
data class WarehouseInventoryItem(
    val sku: String = "",
    val name: String = "",
    @SerialName("dosage_form") val dosageForm: String = "",
    @SerialName("available_quantity") val availableQuantity: Int = 0,
    @SerialName("expiry_date") val expiryDate: String = "",
    @SerialName("branch_id") val branchId: String = ""
)

// ==========================================
// 📣 نظام العروض الترويجية الذكية (Promotional Offers)
// ==========================================

@Serializable
data class PromotionalOffer(
    @SerialName("id") val offerId: String = "",
    @SerialName("product_id") val productId: String = "",       // يربط بـ PharmaProduct.productId
    @SerialName("product_name") val productName: String = "",     // نسخة مخزّنة للعرض السريع بدون join
    val title: String = "",
    val description: String = "",
    @SerialName("discount_percent") val discountPercent: Double = 0.0,   // مثال: 15.0 يعني خصم 15%
    @SerialName("special_price") val specialPrice: Double = 0.0,      // اختياري: سعر خاص مباشر بدل النسبة (استخدم الأكبر أولوية إن وُجد)
    @SerialName("start_date") val startDate: Long = 0L,
    @SerialName("end_date") val endDate: Long = 0L,
    @SerialName("target_governorate") val targetGovernorate: String = "",  // فارغ = يشمل كل المحافظات
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("created_at") val createdAt: Long = 0L
)


