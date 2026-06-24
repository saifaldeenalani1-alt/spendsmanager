# مدير المصروفات

تطبيق Android لتسجيل المصروفات والمدفوعات اليومية مع حسابات متعددة.

## الميزات
- حسابات متعددة (شخصية + مشاريع)
- تسجيل المصروفات والوارد
- تصنيف المصروفات
- تقارير شهرية
- تخزين محلي باستخدام SQLite
- واجهة عربية كاملة

## بناء APK
```bash
./gradlew assembleDebug
```

## هيكل المشروع
```
app/
├── src/main/java/com/spendsmanager/app/
│   ├── data/        # DatabaseHelper + model classes
│   ├── ui/          # Activities
│   └── adapter/     # RecyclerView adapters
└── src/main/res/
    ├── layout/      # XML layouts
    ├── values/      # Strings, colors, themes
    └── drawable/    # Icons and shapes
```
