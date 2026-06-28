package com.example.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "", // "hospital", "supplier", "admin"
    val city: String = "صنعاء",
    val phone: String = "",
    val orgName: String = ""
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
    val priceOfferId: String = "",
    val hospitalId: String = "",
    val supplierId: String = "",
    val medicineName: String = "",
    val price: Double = 0.0,
    val quantity: Int = 0,
    val deliveryMethod: String = "", // "self" or "platform"
    val status: String = "pending", // "pending", "paid", "shipping", "delivered"
    val deliveryScheduledDate: String = "",
    val createdAt: Long = 0L
)
