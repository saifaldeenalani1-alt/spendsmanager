import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import '../database/database_helper.dart';
import '../models/account.dart';
import '../models/transaction.dart';

class ReportsScreen extends StatefulWidget {
  const ReportsScreen({super.key});

  @override
  State<ReportsScreen> createState() => _ReportsScreenState();
}

class _ReportsScreenState extends State<ReportsScreen> {
  DateTime _selectedMonth = DateTime.now();
  List<Account> _accounts = [];
  List<Transaction> _transactions = [];
  bool _loading = true;

  @override
  void initState() {
    super.initState();
    _loadData();
  }

  Future<void> _loadData() async {
    setState(() => _loading = true);
    final db = DatabaseHelper.instance;
    final accounts = await db.getAccounts();
    final startDate = DateFormat('yyyy-MM-dd').format(DateTime(_selectedMonth.year, _selectedMonth.month, 1));
    final endDate = DateFormat('yyyy-MM-dd').format(DateTime(_selectedMonth.year, _selectedMonth.month + 1, 0));
    final transactions = await db.getAllTransactionsByMonth(startDate, endDate);
    setState(() {
      _accounts = accounts;
      _transactions = transactions;
      _loading = false;
    });
  }

  double get _totalIncome => _transactions.where((t) => t.type == 'وارد').fold(0.0, (s, t) => s + t.amount);
  double get _totalExpense => _transactions.where((t) => t.type == 'مصروف').fold(0.0, (s, t) => s + t.amount);

  Map<String, double> get _categoryTotals {
    final map = <String, double>{};
    for (final t in _transactions.where((t) => t.type == 'مصروف' && t.category.isNotEmpty)) {
      map[t.category] = (map[t.category] ?? 0) + t.amount;
    }
    return map;
  }

  Map<int, double> get _accountExpenses {
    final map = <int, double>{};
    for (final t in _transactions.where((t) => t.type == 'مصروف')) {
      map[t.accountId] = (map[t.accountId] ?? 0) + t.amount;
    }
    return map;
  }

  String _accountName(int id) {
    return _accounts.firstWhere((a) => a.id == id, orElse: () => Account(name: 'مشترك', type: '', icon: '')).name;
  }

  @override
  Widget build(BuildContext context) {
    final monthStr = DateFormat('yyyy/MM').format(_selectedMonth);
    return Scaffold(
      appBar: AppBar(title: Text('التقارير - $monthStr')),
      body: _loading
          ? const Center(child: CircularProgressIndicator())
          : RefreshIndicator(
              onRefresh: _loadData,
              child: ListView(
                padding: const EdgeInsets.all(16),
                children: [
                  Center(
                    child: Row(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        IconButton(
                          icon: const Icon(Icons.chevron_right),
                          onPressed: () {
                            setState(() => _selectedMonth = DateTime(_selectedMonth.year, _selectedMonth.month - 1));
                            _loadData();
                          },
                        ),
                        Text(monthStr, style: const TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
                        IconButton(
                          icon: const Icon(Icons.chevron_left),
                          onPressed: () {
                            setState(() => _selectedMonth = DateTime(_selectedMonth.year, _selectedMonth.month + 1));
                            _loadData();
                          },
                        ),
                      ],
                    ),
                  ),
                  const SizedBox(height: 16),
                  _buildSummaryCards(),
                  const SizedBox(height: 16),
                  if (_categoryTotals.isNotEmpty) ...[
                    const Text('المصروفات حسب التصنيف',
                        style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
                    const SizedBox(height: 8),
                    ..._categoryTotals.entries.map((e) => _buildBar(e.key, e.value, _totalExpense)),
                    const SizedBox(height: 16),
                  ],
                  if (_accountExpenses.isNotEmpty) ...[
                    const Text('المصروفات حسب الحساب',
                        style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
                    const SizedBox(height: 8),
                    ..._accountExpenses.entries.map((e) => _buildBar(
                          _accountName(e.key),
                          e.value,
                          _totalExpense,
                        )),
                  ],
                  if (_transactions.isEmpty)
                    const Padding(
                      padding: EdgeInsets.only(top: 40),
                      child: Center(
                        child: Text('لا توجد معاملات لهذا الشهر',
                            style: TextStyle(color: Colors.grey, fontSize: 16)),
                      ),
                    ),
                ],
              ),
            ),
    );
  }

  Widget _buildSummaryCards() {
    final balance = _totalIncome - _totalExpense;
    return Row(
      children: [
        _summaryCard('الوارد', _totalIncome, Colors.green),
        const SizedBox(width: 8),
        _summaryCard('المصروفات', _totalExpense, Colors.red),
        const SizedBox(width: 8),
        _summaryCard('الصافي', balance, balance >= 0 ? Colors.blue : Colors.orange),
      ],
    );
  }

  Widget _summaryCard(String label, double amount, Color color) {
    return Expanded(
      child: Card(
        color: color.withOpacity(0.1),
        child: Padding(
          padding: const EdgeInsets.all(12),
          child: Column(
            children: [
              Text(label, style: TextStyle(color: color, fontWeight: FontWeight.bold)),
              const SizedBox(height: 4),
              Text('${amount.toStringAsFixed(0)} د.ع',
                  style: TextStyle(fontWeight: FontWeight.bold, color: color, fontSize: 16)),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildBar(String label, double amount, double total) {
    final ratio = total > 0 ? amount / total : 0.0;
    return Padding(
      padding: const EdgeInsets.only(bottom: 8),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text(label, style: const TextStyle(fontWeight: FontWeight.w500)),
              Text('${amount.toStringAsFixed(2)} د.ع (${(ratio * 100).toStringAsFixed(1)}%)',
                  style: TextStyle(color: Colors.grey.shade600, fontSize: 12)),
            ],
          ),
          const SizedBox(height: 4),
          ClipRRect(
            borderRadius: BorderRadius.circular(4),
            child: LinearProgressIndicator(
              value: ratio,
              backgroundColor: Colors.grey.shade200,
              color: Colors.blue,
              minHeight: 8,
            ),
          ),
        ],
      ),
    );
  }
}
