class Account {
  final int? id;
  final String name;
  final String type; // شخصي / مشروع
  final double initialBalance;
  final String createdAt;
  final String icon;

  Account({
    this.id,
    required this.name,
    required this.type,
    this.initialBalance = 0.0,
    String? createdAt,
    this.icon = '📁',
  }) : createdAt = createdAt ?? DateTime.now().toIso8601String();

  Map<String, dynamic> toMap() {
    return {
      if (id != null) 'id': id,
      'name': name,
      'type': type,
      'initial_balance': initialBalance,
      'created_at': createdAt,
      'icon': icon,
    };
  }

  factory Account.fromMap(Map<String, dynamic> map) {
    return Account(
      id: map['id'] as int?,
      name: map['name'] as String,
      type: map['type'] as String,
      initialBalance: (map['initial_balance'] as num?)?.toDouble() ?? 0.0,
      createdAt: map['created_at'] as String?,
      icon: map['icon'] as String? ?? '📁',
    );
  }

  Account copyWith({
    int? id,
    String? name,
    String? type,
    double? initialBalance,
    String? createdAt,
    String? icon,
  }) {
    return Account(
      id: id ?? this.id,
      name: name ?? this.name,
      type: type ?? this.type,
      initialBalance: initialBalance ?? this.initialBalance,
      createdAt: createdAt ?? this.createdAt,
      icon: icon ?? this.icon,
    );
  }
}
