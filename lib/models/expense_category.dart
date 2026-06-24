class ExpenseCategory {
  final int? id;
  final String name;
  final String icon;

  ExpenseCategory({this.id, required this.name, this.icon = '📌'});

  Map<String, dynamic> toMap() {
    return {
      if (id != null) 'id': id,
      'name': name,
      'icon': icon,
    };
  }

  factory ExpenseCategory.fromMap(Map<String, dynamic> map) {
    return ExpenseCategory(
      id: map['id'] as int?,
      name: map['name'] as String,
      icon: map['icon'] as String? ?? '📌',
    );
  }

  static List<ExpenseCategory> defaults() {
    return [
      ExpenseCategory(name: 'طعام', icon: '🍽️'),
      ExpenseCategory(name: 'مواصلات', icon: '🚗'),
      ExpenseCategory(name: 'فواتير', icon: '💡'),
      ExpenseCategory(name: 'إيجار', icon: '🏠'),
      ExpenseCategory(name: 'صحة', icon: '💊'),
      ExpenseCategory(name: 'ترفيه', icon: '🎮'),
      ExpenseCategory(name: 'ملابس', icon: '👕'),
      ExpenseCategory(name: 'تعليم', icon: '📚'),
      ExpenseCategory(name: 'اتصالات', icon: '📱'),
      ExpenseCategory(name: 'أخرى', icon: '📌'),
    ];
  }
}
