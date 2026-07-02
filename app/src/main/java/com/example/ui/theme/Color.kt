package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Semantic Color Palette
val BrandPrimary = Color(0xFF2563EB)       // أزرق أساسي للأزرار والعناصر الرئيسية
val OnBrandPrimary = Color(0xFFFFFFFF)     // أبيض دائماً فوق الأزرق الأساسي

val SuccessGreen = Color(0xFF10B981)       // نجاح / قبول / أموال
val OnSuccessGreen = Color(0xFFFFFFFF)

val ErrorRed = Color(0xFFEF4444)           // رفض / خطر
val OnErrorRed = Color(0xFFFFFFFF)

val WarningAmber = Color(0xFFD97706)       // تنبيه / مخزون منخفض
val OnWarningAmber = Color(0xFFFFFFFF)

val SurfaceLight = Color(0xFFF8FAFC)       // خلفية عامة فاتحة
val OnSurfaceDark = Color(0xFF0F172A)      // نص غامق فوق الخلفيات الفاتحة فقط

val TextSecondaryGray = Color(0xFF64748B)  // نص ثانوي/وصفي

// Backward Compatibility Definitions
val MedBluePrimary = BrandPrimary
val MedBlueAccent = BrandPrimary
val MedGreenPrimary = SuccessGreen
val MedRedPrimary = ErrorRed
