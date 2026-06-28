package com.example.model

import kotlinx.serialization.Serializable

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
    val createdAt: Long = 0L
)

@Serializable
data class User(
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "", // "client", "branch_manager", "company_director"
    val clientType: String = "", // "hospital" أو "pharmacy" للعملاء فقط
    val city: String = "",
    val governorate: String = "",
    val phone: String = "",
    val orgName: String = "",
    val branchId: String = "", // لمدراء الفروع فقط
    val branchName: String = "", // اسم الفرع
    val isVerified: Boolean = false,
    val isActive: Boolean = true,
    val profileImageUrl: String = "",
    val createdAt: Long = 0L
)

@Serializable
data class UserAddress(
    val addressId: String = "",
    val userId: String = "",
    val userType: String = "", // "hospital" or "supplier"
    val label: String = "", // e.g. "المقر الرئيسي"
    val hospitalOrCompanyName: String = "",
    val nearbyLandmark: String = "", // landmark
    val governorate: String = "", // المحافظة
    val district: String = "", // المديرية
    val neighborhood: String = "", // الحي
    val fullAddress: String = "",
    val latitude: Double = 15.3482,
    val longitude: Double = 44.2191,
    val isDefault: Boolean = false,
    val createdAt: Long = 0L
)

@Serializable
data class Medicine(
    val medicineId: String = "",
    val name: String = "",
    val category: String = "",
    val price: Double = 0.0,
    val quantity: Int = 0,
    val supplierId: String = ""
)

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
    val status: String = "pending", // "pending", "accepted", "rejected", "negotiating"
    val deliveryAddressId: String = "",
    val deliveryAddressLabel: String = "",
    val deliveryFullAddress: String = "",
    val distance: Double = 0.0,
    val eta: String = "",
    val deliveryMethod: String = "", // "self" or "platform"
    val createdAt: Long = 0L
)

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
    val receiptUrl: String = "", // URL/Path to receipt image or doc
    val receiptNote: String = "",
    val status: String = "pending", // "pending", "confirmed", "rejected"
    val adminVisible: Boolean = true,
    val commissionAmount: Double = 0.0,
    val commissionRate: Double = 0.05, // e.g. 5% platform fee
    val commissionStatus: String = "pending" // "pending" or "paid"
)

@Serializable
data class BankAccount(
    val accountId: String = "",
    val userId: String = "",
    val bankName: String = "", // Bank or Wallet name
    val accountNumber: String = "",
    val accountHolderName: String = "",
    val walletType: String = "bank", // "bank", "mfs" (mobile financial service like mtn / yemen mobile)
    val walletNumber: String = "", // Phone number linked to wallet
    val isDefault: Boolean = false,
    val createdAt: Long = 0L
)

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
    val urgencyLevel: String = "normal", // "normal", "high", "critical"
    val packageSize: String = "medium", // "small", "medium", "large"
    val packageImageUrl: String = "",
    val status: String = "pending", // "pending", "assigned", "picked", "delivered"
    val adminAssigned: Boolean = false,
    val createdAt: Long = 0L
)

@Serializable
data class DeliverySchedule(
    val scheduleId: String = "",
    val orderId: String = "",
    val supplierAvailableTimes: List<String> = emptyList(), // Date-times available
    val hospitalPreferredTimes: List<String> = emptyList(), // Date-times preferred
    val agreedDateTime: String = "",
    val status: String = "negotiating", // "negotiating" or "agreed"
    val updatedAt: Long = 0L
)

@Serializable
data class Order(
    val orderId: String = "",
    val clientId: String = "",
    val clientName: String = "",
    val clientType: String = "", // hospital/pharmacy
    val clientGovernorate: String = "",
    val orderContent: String = "", // وصف الطلب
    val attachments: List<String> = emptyList(),
    val urgencyLevel: String = "normal",
    val broadcastType: String = "all", // all/nearby/selected
    val targetBranches: List<String> = emptyList(),
    val status: String = "broadcast", // broadcast/offer_received/negotiating/confirmed/delivered
    val createdAt: Long = 0L,

    // Backward compatibility fields:
    val priceOfferId: String = "",
    val hospitalId: String = "",
    val supplierId: String = "",
    val medicineName: String = "",
    val price: Double = 0.0,
    val quantity: Int = 0,
    val deliveryMethod: String = "", // "self" or "platform"
    val deliveryScheduledDate: String = ""
)

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
    val assignedBranchId: String = "", // الفرع المخصص تلقائياً
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

@Serializable
data class OrderRouting(
    val orderId: String = "",
    val clientId: String = "",
    val targetBranches: List<String> = emptyList(),
    val routingType: String = "", // "all", "nearby", "selected"
    val routingReason: String = "",
    val createdAt: Long = 0L
)

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
