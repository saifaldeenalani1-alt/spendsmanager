class Transaction {
  final int? id;
  final int accountId;
  final String type; // مصروف / وارد
  final double amount;
  final String category;
  final String description;
  final String date;
  final String createdAt;

  Transaction({
    this.id,
    required this.accountId,
    required this.type,
    required this.amount,
    required this.category,
    this.description = '',
    String? date,
    String? createdAt,
  })  : date = date ?? DateTime.now().toIso8601String().substring(0, 10),
        createdAt = createdAt ?? DateTime.now().toIso8601String();

  Map<String, dynamic> toMap() {
    return {
      if (id != null) 'id': id,
      'account_id': accountId,
      'type': type,
      'amount': amount,
      'category': category,
      'description': description,
      'date': date,
      'created_at': createdAt,
    };
  }

  factory Transaction.fromMap(Map<String, dynamic> map) {
    return Transaction(
      id: map['id'] as int?,
      accountId: map['account_id'] as int,
      type: map['type'] as String,
      amount: (map['amount'] as num).toDouble(),
      category: map['category'] as String? ?? '',
      description: map['description'] as String? ?? '',
      date: map['date'] as String?,
      createdAt: map['created_at'] as String?,
    );
  }
}
