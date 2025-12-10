 <!DOCTYPE html>
<html lang="ar" dir="rtl">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>واجهة عصرية | نموذج احترافي</title>
    <!-- استدعاء Tailwind CSS -->
    <script src="https://cdn.tailwindcss.com"></script>
    <!-- استدعاء خط "تجوال" من Google Fonts -->
    <link href="https://fonts.googleapis.com/css2?family=Tajawal:wght@300;400;500;700&display=swap" rel="stylesheet">
    <!-- إعدادات الخط والأنميشن -->
    <style>
        body {
            font-family: 'Tajawal', sans-serif;
            scroll-behavior: smooth;
        }
        .gradient-text {
            background: linear-gradient(to right, #2563eb, #9333ea);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
        }
        .hero-pattern {
            background-color: #f3f4f6;
            background-image: radial-gradient(#d1d5db 1px, transparent 1px);
            background-size: 20px 20px;
        }
        /* أنميشن بسيط للظهور */
        .fade-in-up {
            animation: fadeInUp 0.8s ease-out forwards;
            opacity: 0;
            transform: translateY(20px);
        }
        @keyframes fadeInUp {
            to {
                opacity: 1;
                transform: translateY(0);
            }
        }
        .delay-100 { animation-delay: 0.1s; }
        .delay-200 { animation-delay: 0.2s; }
        .delay-300 { animation-delay: 0.3s; }
    </style>
</head>
<body class="bg-gray-50 text-gray-800">

    <!-- شريط التنقل (Navbar) -->
    <nav class="bg-white/80 backdrop-blur-md fixed w-full z-50 shadow-sm border-b border-gray-100">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div class="flex justify-between items-center h-20">
                <!-- الشعار -->
                <div class="flex-shrink-0 flex items-center gap-2 cursor-pointer">
                    <div class="w-10 h-10 bg-blue-600 rounded-xl flex items-center justify-center text-white font-bold text-xl shadow-lg shadow-blue-200">
                        م
                    </div>
                    <span class="font-bold text-2xl text-gray-800">مشروعي</span>
                </div>
                
                <!-- روابط القائمة للشاشات الكبيرة -->
                <div class="hidden md:flex space-x-8 space-x-reverse items-center">
                    <a href="#home" class="text-gray-600 hover:text-blue-600 font-medium transition duration-300">الرئيسية</a>
                    <a href="#features" class="text-gray-600 hover:text-blue-600 font-medium transition duration-300">المميزات</a>
                    <a href="#about" class="text-gray-600 hover:text-blue-600 font-medium transition duration-300">من نحن</a>
                    <a href="#contact" class="bg-blue-600 text-white px-6 py-2.5 rounded-full font-medium hover:bg-blue-700 transition duration-300 shadow-lg shadow-blue-200 hover:shadow-xl transform hover:-translate-y-0.5">ابدأ الآن</a>
                </div>

                <!-- زر القائمة للجوال -->
                <div class="md:hidden flex items-center">
                    <button id="mobile-menu-btn" class="text-gray-600 hover:text-gray-900 focus:outline-none p-2">
                        <svg class="h-8 w-8" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16"/>
                        </svg>
                    </button>
                </div>
            </div>
        </div>
        
        <!-- قائمة الجوال المنبثقة -->
        <div id="mobile-menu" class="hidden md:hidden bg-white border-t border-gray-100 absolute w-full">
            <div class="px-4 pt-2 pb-6 space-y-2">
                <a href="#home" class="block px-3 py-3 text-base font-medium text-gray-700 hover:text-blue-600 hover:bg-gray-50 rounded-lg">الرئيسية</a>
                <a href="#features" class="block px-3 py-3 text-base font-medium text-gray-700 hover:text-blue-600 hover:bg-gray-50 rounded-lg">المميزات</a>
                <a href="#about" class="block px-3 py-3 text-base font-medium text-gray-700 hover:text-blue-600 hover:bg-gray-50 rounded-lg">من نحن</a>
                <a href="#contact" class="block px-3 py-3 text-base font-medium text-blue-600 bg-blue-50 rounded-lg">تواصل معنا</a>
            </div>
        </div>
    </nav>

    <!-- قسم الهيرو (Hero Section) -->
    <section id="home" class="relative pt-32 pb-20 lg:pt-40 lg:pb-28 overflow-hidden hero-pattern">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 relative">
            <div class="text-center max-w-4xl mx-auto">
                <h1 class="text-4xl md:text-6xl font-extrabold text-gray-900 leading-tight mb-6 fade-in-up">
                    أطلق العنان لإبداعك مع <br/>
                    <span class="gradient-text">واجهة المستقبل</span>
                </h1>
                <p class="text-xl text-gray-600 mb-10 leading-relaxed max-w-2xl mx-auto fade-in-up delay-100">
                    نقدم لك أفضل الحلول التقنية لبناء مشاريعك الرقمية بسرعة وكفاءة عالية. تصميم عصري، أداء سريع، وتجربة مستخدم لا تضاهى.
                </p>
                <div class="flex flex-col sm:flex-row justify-center gap-4 fade-in-up delay-200">
                    <button class="px-8 py-4 bg-blue-600 text-white rounded-full font-bold text-lg hover:bg-blue-700 transition duration-300 shadow-xl shadow-blue-200 transform hover:-translate-y-1">
                        اشترك مجاناً
                    </button>
                    <button class="px-8 py-4 bg-white text-gray-700 border border-gray-200 rounded-full font-bold text-lg hover:bg-gray-50 transition duration-300 flex items-center justify-center gap-2">
                        <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 text-gray-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14.752 11.168l-3.197-2.132A1 1 0 0010 9.87v4.263a1 1 0 001.555.832l3.197-2.132a1 1 0 000-1.664z" />
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                        </svg>
                        شاهد الفيديو
                    </button>
                </div>
            </div>
            
            <!-- صورة توضيحية أو عنصر جمالي -->
            <div class="mt-16 relative mx-auto max-w-5xl fade-in-up delay-300">
                <div class="absolute inset-0 bg-blue-600 blur-3xl opacity-10 rounded-full transform scale-90"></div>
                <div class="relative bg-white rounded-2xl shadow-2xl overflow-hidden border border-gray-100">
                    <!-- محاكاة لنافذة متصفح -->
                    <div class="bg-gray-100 px-4 py-3 border-b flex items-center gap-2">
                        <div class="w-3 h-3 rounded-full bg-red-400"></div>
                        <div class="w-3 h-3 rounded-full bg-yellow-400"></div>
                        <div class="w-3 h-3 rounded-full bg-green-400"></div>
                        <div class="flex-1 text-center text-xs text-gray-400 font-mono">dashboard.app.com</div>
                    </div>
                    <!-- محتوى وهمي -->
                    <div class="p-8 grid grid-cols-1 md:grid-cols-3 gap-6">
                        <div class="h-32 bg-blue-50 rounded-xl animate-pulse"></div>
                        <div class="h-32 bg-purple-50 rounded-xl animate-pulse"></div>
                        <div class="h-32 bg-green-50 rounded-xl animate-pulse"></div>
                        <div class="col-span-1 md:col-span-2 h-48 bg-gray-50 rounded-xl animate-pulse"></div>
                        <div class="h-48 bg-gray-50 rounded-xl animate-pulse"></div>
                    </div>
                </div>
            </div>
        </div>
    </section>

    <!-- قسم المميزات (Features) -->
    <section id="features" class="py-20 bg-white">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div class="text-center mb-16">
                <h2 class="text-base text-blue-600 font-bold tracking-wide uppercase">لماذا تختارنا؟</h2>
                <p class="mt-2 text-3xl font-extrabold text-gray-900 sm:text-4xl">
                    كل ما تحتاجه لتنمية أعمالك
                </p>
                <p class="mt-4 max-w-2xl text-xl text-gray-500 mx-auto">
                    نقدم مجموعة متكاملة من الأدوات والخدمات المصممة خصيصاً لتلبية احتياجاتك التقنية.
                </p>
            </div>

            <div class="grid grid-cols-1 md:grid-cols-3 gap-8">
                <!-- ميزة 1 -->
                <div class="bg-gray-50 rounded-2xl p-8 transition duration-300 hover:shadow-xl hover:-translate-y-1 border border-gray-100 group">
                    <div class="w-14 h-14 bg-blue-100 text-blue-600 rounded-xl flex items-center justify-center mb-6 group-hover:bg-blue-600 group-hover:text-white transition duration-300">
                        <svg class="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z"></path></svg>
                    </div>
                    <h3 class="text-xl font-bold text-gray-900 mb-3">سرعة فائقة</h3>
                    <p class="text-gray-600 leading-relaxed">
                        تم تحسين الكود لضمان أسرع وقت تحميل ممكن، مما يعزز تجربة المستخدم ويرفع ترتيبك في محركات البحث.
                    </p>
                </div>

                <!-- ميزة 2 -->
                <div class="bg-gray-50 rounded-2xl p-8 transition duration-300 hover:shadow-xl hover:-translate-y-1 border border-gray-100 group">
                    <div class="w-14 h-14 bg-purple-100 text-purple-600 rounded-xl flex items-center justify-center mb-6 group-hover:bg-purple-600 group-hover:text-white transition duration-300">
                        <svg class="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"></path></svg>
                    </div>
                    <h3 class="text-xl font-bold text-gray-900 mb-3">حماية متقدمة</h3>
                    <p class="text-gray-600 leading-relaxed">
                        نستخدم أحدث بروتوكولات الأمان لحماية بياناتك وبيانات عملائك من أي تهديدات محتملة.
                    </p>
                </div>

                <!-- ميزة 3 -->
                <div class="bg-gray-50 rounded-2xl p-8 transition duration-300 hover:shadow-xl hover:-translate-y-1 border border-gray-100 group">
                    <div class="w-14 h-14 bg-green-100 text-green-600 rounded-xl flex items-center justify-center mb-6 group-hover:bg-green-600 group-hover:text-white transition duration-300">
                        <svg class="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
                    </div>
                    <h3 class="text-xl font-bold text-gray-900 mb-3">سهولة الاستخدام</h3>
                    <p class="text-gray-600 leading-relaxed">
                        واجهة بديهية وبسيطة تتيح لك إدارة مشروعك بكل سهولة دون الحاجة لخبرة تقنية عميقة.
                    </p>
                </div>
            </div>
        </div>
    </section>

    <!-- قسم الإحصائيات (Stats) -->
    <section class="bg-blue-600 py-16">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div class="grid grid-cols-2 md:grid-cols-4 gap-8 text-center text-white">
                <div>
                    <div class="text-4xl font-bold mb-2">+5000</div>
                    <div class="text-blue-200">عميل سعيد</div>
                </div>
                <div>
                    <div class="text-4xl font-bold mb-2">+120</div>
                    <div class="text-blue-200">مشروع ناجح</div>
                </div>
                <div>
                    <div class="text-4xl font-bold mb-2">%99</div>
                    <div class="text-blue-200">نسبة الرضا</div>
                </div>
                <div>
                    <div class="text-4xl font-bold mb-2">24/7</div>
                    <div class="text-blue-200">دعم فني</div>
                </div>
            </div>
        </div>
    </section>

    <!-- قسم تواصل معنا (Contact) -->
    <section id="contact" class="py-20 bg-gray-50">
        <div class="max-w-3xl mx-auto px-4 sm:px-6 lg:px-8">
            <div class="bg-white rounded-2xl shadow-xl p-8 md:p-12">
                <div class="text-center mb-10">
                    <h2 class="text-3xl font-bold text-gray-900">هل أنت مستعد للبدء؟</h2>
                    <p class="mt-4 text-gray-500">املأ النموذج أدناه وسنتواصل معك في أقرب وقت ممكن.</p>
                </div>
                
                <form class="space-y-6" onsubmit="event.preventDefault(); alert('تم إرسال رسالتك بنجاح! (هذا نموذج تجريبي)');">
                    <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                        <div>
                            <label class="block text-sm font-medium text-gray-700 mb-2">الاسم الكامل</label>
                            <input type="text" class="w-full px-4 py-3 rounded-lg border border-gray-300 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition outline-none" placeholder="محمد أحمد">
                        </div>
                        <div>
                            <label class="block text-sm font-medium text-gray-700 mb-2">البريد الإلكتروني</label>
                            <input type="email" class="w-full px-4 py-3 rounded-lg border border-gray-300 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition outline-none" placeholder="name@example.com">
                        </div>
                    </div>
                    <div>
                        <label class="block text-sm font-medium text-gray-700 mb-2">الرسالة</label>
                        <textarea rows="4" class="w-full px-4 py-3 rounded-lg border border-gray-300 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition outline-none" placeholder="كيف يمكننا مساعدتك؟"></textarea>
                    </div>
                    <button type="submit" class="w-full bg-blue-600 text-white font-bold py-4 rounded-lg hover:bg-blue-700 transition duration-300 shadow-lg shadow-blue-200">
                        إرسال الرسالة
                    </button>
                </form>
            </div>
        </div>
    </section>

    <!-- الفوتر (Footer) -->
    <footer class="bg-gray-900 text-gray-300 py-12 border-t border-gray-800">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div class="grid grid-cols-1 md:grid-cols-4 gap-8 mb-8">
                <div class="col-span-1 md:col-span-2">
                    <div class="flex items-center gap-2 mb-4 text-white">
                         <div class="w-8 h-8 bg-blue-600 rounded-lg flex items-center justify-center font-bold">م</div>
                         <span class="text-xl font-bold">مشروعي</span>
                    </div>
                    <p class="text-gray-400 max-w-sm leading-relaxed">
                        نحن شركة تقنية نسعى لتقديم أفضل الحلول الرقمية لعملائنا حول العالم. نؤمن بأن التصميم الجيد هو مفتاح النجاح.
                    </p>
                </div>
                <div>
                    <h4 class="text-white font-bold mb-4">روابط سريعة</h4>
                    <ul class="space-y-2">
                        <li><a href="#" class="hover:text-blue-400 transition">الرئيسية</a></li>
                        <li><a href="#" class="hover:text-blue-400 transition">عن الشركة</a></li>
                        <li><a href="#" class="hover:text-blue-400 transition">الخدمات</a></li>
                        <li><a href="#" class="hover:text-blue-400 transition">سياسة الخصوصية</a></li>
                    </ul>
                </div>
                <div>
                    <h4 class="text-white font-bold mb-4">تواصل معنا</h4>
                    <ul class="space-y-2">
                        <li>info@example.com</li>
                        <li>+966 50 000 0000</li>
                        <li>الرياض، المملكة العربية السعودية</li>
                    </ul>
                </div>
            </div>
            <div class="border-t border-gray-800 pt-8 flex flex-col md:flex-row justify-between items-center">
                <p>&copy; 2023 مشروعي. جميع الحقوق محفوظة.</p>
                <div class="flex space-x-4 space-x-reverse mt-4 md:mt-0">
                    <a href="#" class="text-gray-400 hover:text-white transition"><svg class="w-6 h-6" fill="currentColor" viewBox="0 0 24 24"><path d="M24 4.557c-.883.392-1.832.656-2.828.775 1.017-.609 1.798-1.574 2.165-2.724-.951.564-2.005.974-3.127 1.195-.897-.957-2.178-1.555-3.594-1.555-3.179 0-5.515 2.966-4.797 6.045-4.091-.205-7.719-2.165-10.148-5.144-1.29 2.213-.669 5.108 1.523 6.574-.806-.026-1.566-.247-2.229-.616-.054 2.281 1.581 4.415 3.949 4.89-.693.188-1.452.232-2.224.084.626 1.956 2.444 3.379 4.6 3.419-2.07 1.623-4.678 2.348-7.29 2.04 2.179 1.397 4.768 2.212 7.548 2.212 9.142 0 14.307-7.721 13.995-14.646.962-.695 1.797-1.562 2.457-2.549z"/></svg></a>
                    <a href="#" class="text-gray-400 hover:text-white transition"><svg class="w-6 h-6" fill="currentColor" viewBox="0 0 24 24"><path d="M12 2.163c3.204 0 3.584.012 4.85.072 3.252.148 4.771 1.691 4.919 4.919.058 1.265.069 1.645.069 4.849 0 3.205-.012 3.584-.069 4.849-.149 3.225-1.664 4.771-4.919 4.919-1.266.058-1.644.07-4.85.07-3.204 0-3.584-.012-4.85-.07-3.26-.149-4.771-1.699-4.919-4.92-.058-1.265-.07-1.644-.07-4.849 0-3.204.013-3.583.07-4.849.149-3.227 1.664-4.771 4.919-4.919 1.266-.057 1.645-.069 4.849-.069zm0-2.163c-3.259 0-3.667.014-4.947.072-4.358.2-6.78 2.618-6.98 6.98-.059 1.281-.073 1.689-.073 4.948 0 3.259.014 3.668.072 4.948.2 4.358 2.618 6.78 6.98 6.98 1.281.058 1.689.072 4.948.072 3.259 0 3.668-.014 4.948-.072 4.354-.2 6.782-2.618 6.979-6.98.059-1.28.073-1.689.073-4.948 0-3.259-.014-3.667-.072-4.947-.196-4.354-2.617-6.78-6.979-6.98-1.281-.059-1.69-.073-4.949-.073zm0 5.838c-3.403 0-6.162 2.759-6.162 6.162s2.759 6.163 6.162 6.163 6.162-2.759 6.162-6.163c0-3.403-2.759-6.162-6.162-6.162zm0 10.162c-2.209 0-4-1.79-4-4 0-2.209 1.791-4 4-4s4 1.791 4 4c0 2.21-1.791 4-4 4zm6.406-11.845c-.796 0-1.441.645-1.441 1.44s.645 1.44 1.441 1.44c.795 0 1.439-.645 1.439-1.44s-.644-1.44-1.439-1.44z"/></svg></a>
                </div>
            </div>
        </div>
    </footer>

    <!-- سكربت بسيط لتشغيل القائمة في الجوال -->
    <script>
        const btn = document.getElementById('mobile-menu-btn');
        const menu = document.getElementById('mobile-menu');

        btn.addEventListener('click', () => {
            menu.classList.toggle('hidden');
        });
    </script>
</body>
</html>
