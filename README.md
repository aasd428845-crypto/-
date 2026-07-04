# MedLink Yemen 🏥

تطبيق Android لإدارة فروع الأدوية والمستلزمات الطبية.

---

## 🔨 بناء الـ APK عبر GitHub Actions (بدون جهاز كمبيوتر)

### الخطوات:

1. **ارفع المشروع إلى مستودعك على GitHub**
2. اذهب إلى تبويب **Actions** في المستودع
3. اختر workflow باسم **Build APK**
4. اضغط على **Run workflow** ← **Run workflow**
5. انتظر انتهاء البناء (حوالي 5-10 دقائق)
6. بعد الانتهاء، اضغط على اسم الـ Run ← ابحث عن قسم **Artifacts**
7. حمّل ملف `MedLinkYemen-debug-apk`

> الـ APK يُبنى تلقائياً أيضاً عند كل `push` إلى الفرع الرئيسي.

---

## 📋 متطلبات البناء

- JDK 17
- Gradle 9.3.1
- Android compileSdk 34 / minSdk 24

## 🛠️ بناء محلي (إن توفر جهاز)

**المتطلبات:** [Android Studio](https://developer.android.com/studio)

1. افتح المشروع في Android Studio
2. شغّل التطبيق على محاكي أو جهاز حقيقي

---

## 🗂️ هيكل المشروع

```
app/
└── src/main/
    ├── java/com/example/
    │   ├── MainActivity.kt
    │   ├── model/         # نماذج البيانات
    │   ├── service/       # Firebase وخدمات البيانات
    │   └── ui/screens/    # شاشات التطبيق (Compose)
    └── res/               # الموارد
```
