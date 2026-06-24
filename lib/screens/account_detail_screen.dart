import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import '../models/transaction.dart';
import '../database/database_helper.dart';
import 'add_edit_transaction_screen.dart';

class AccountDetailScreen extends StatefulWidget {
  final int accountId;
  final String accountName;

  const AccountDetailScreen({
    super.key,
    required this.accountId,
    required this.accountName,
  });

  @override
  State<AccountDetailScreen> createState() => _AccountDetailScreenState();
}

class _AccountDetailScreenState extends State<AccountDetailScreen> {
  List<Transaction> _transactions = [];
  double _balance = 0;
  bool _loading = true;
  String _filter = 'الكل';

  @override
  void initState() {
    super.initState();
    _loadData();
  }

  Future<void> _loadData() async {
    setState(() => _loading = true);
    final db = DatabaseHelper.instance;
    final transactions = await db.getTransactions(widget.accountId);
    final balance = await db.getAccountBalance(widget.accountId);
    setState(() {
      _transactions = transactions;
      _balance = balance;
      _loading = false;
    });
  }

  List<Transaction> get _filteredTransactions {
    if (_filter == 'الكل') return _transactions;
    return _transactions.where((t) => t.type == _filter).toList();
  }

  double get _totalIncome {
    return _transactions
        .where((t) => t.type == 'وارد')
        .fold(0.0, (sum, t) => sum + t.amount);
  }

  double get _totalExpense {
    return _transactions
        .where((t) => t.type == 'مصروف')
        .fold(0.0, (sum, t) => sum + t.amount);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.accountName),
        actions: [
          PopupMenuButton<String>(
            onSelected: (v) => setState(() => _filter = v),
            itemBuilder: (_) => [
              CheckedPopupMenuItem(
                value: 'الكل',
                checked: _filter == 'الكل',
                child: const Text('الكل'),
              ),
              CheckedPopupMenuItem(
                value: 'مصروف',
                checked: _filter == 'مصروف',
                child: const Text('المصروفات'),
              ),
              CheckedPopupMenuItem(
                value: 'وارد',
                checked: _filter == 'وارد',
                child: const Text('الوارد'),
              ),
            ],
          ),
        ],
      ),
      body: _loading
          ? const Center(child: CircularProgressIndicator())
          : Column(
              children: [
                _buildSummary(),
                const Divider(height: 1),
                Expanded(child: _buildTransactionList()),
              ],
            ),
      floatingActionButton: FloatingActionButton(
        child: const Icon(Icons.add),
        onPressed: () => _addTransaction(),
      ),
    );
  }

  Widget _buildSummary() {
    return Container(
      padding: const EdgeInsets.all(16),
      color: Colors.white,
      child: Row(
        children: [
          Expanded(
            child: Column(
              children: [
                const Text('الرصيد', style: TextStyle(color: Colors.grey, fontSize: 12)),
                const SizedBox(height: 4),
                Text(
                  '${_balance.toStringAsFixed(2)} د.ع',
                  style: TextStyle(
                    fontSize: 22,
                    fontWeight: FontWeight.bold,
                    color: _balance >= 0 ? Colors.green : Colors.red,
                  ),
                ),
              ],
            ),
          ),
          Container(height: 40, width: 1, color: Colors.grey.shade300),
          Expanded(
            child: Column(
              children: [
                const Text('الوارد', style: TextStyle(color: Colors.grey, fontSize: 12)),
                const SizedBox(height: 4),
                Text(
                  '${_totalIncome.toStringAsFixed(2)} د.ع',
                  style: const TextStyle(
                    fontSize: 18,
                    fontWeight: FontWeight.bold,
                    color: Colors.green,
                  ),
                ),
              ],
            ),
          ),
          Container(height: 40, width: 1, color: Colors.grey.shade300),
          Expanded(
            child: Column(
              children: [
                const Text('المصروفات', style: TextStyle(color: Colors.grey, fontSize: 12)),
                const SizedBox(height: 4),
                Text(
                  '${_totalExpense.toStringAsFixed(2)} د.ع',
                  style: const TextStyle(
                    fontSize: 18,
                    fontWeight: FontWeight.bold,
                    color: Colors.red,
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildTransactionList() {
    final filtered = _filteredTransactions;
    if (filtered.isEmpty) {
      return const Center(
        child: Text('لا توجد معاملات', style: TextStyle(color: Colors.grey, fontSize: 16)),
      );
    }
    return RefreshIndicator(
      onRefresh: _loadData,
      child: ListView.builder(
        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
        itemCount: filtered.length,
        itemBuilder: (_, i) {
          final t = filtered[i];
          final dateStr = DateFormat('yyyy/MM/dd').format(DateTime.parse(t.date));
          final isExpense = t.type == 'مصروف';
          return Dismissible(
            key: Key(t.id.toString()),
            direction: DismissDirection.endToStart,
            background: Container(
              alignment: Alignment.centerRight,
              padding: const EdgeInsets.only(right: 20),
              color: Colors.red,
              child: const Icon(Icons.delete, color: Colors.white),
            ),
            onDismissed: (_) async {
              if (t.id != null) {
                await DatabaseHelper.instance.deleteTransaction(t.id!);
                _loadData();
              }
            },
            child: Card(
              margin: const EdgeInsets.only(bottom: 6),
              child: ListTile(
                leading: CircleAvatar(
                  backgroundColor: isExpense ? Colors.red.shade50 : Colors.green.shade50,
                  child: Icon(
                    isExpense ? Icons.arrow_upward : Icons.arrow_downward,
                    color: isExpense ? Colors.red : Colors.green,
                  ),
                ),
                title: Row(
                  children: [
                    if (t.category.isNotEmpty)
                      Container(
                        padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
                        decoration: BoxDecoration(
                          color: Colors.grey.shade100,
                          borderRadius: BorderRadius.circular(12),
                        ),
                        child: Text(t.category, style: const TextStyle(fontSize: 12)),
                      ),
                    if (t.description.isNotEmpty) ...[
                      const SizedBox(width: 6),
                      Expanded(
                        child: Text(t.description,
                            style: const TextStyle(fontSize: 13), overflow: TextOverflow.ellipsis),
                      ),
                    ],
                  ],
                ),
                subtitle: Text(dateStr, style: TextStyle(color: Colors.grey.shade500, fontSize: 12)),
                trailing: Text(
                  '${isExpense ? '-' : '+'}${t.amount.toStringAsFixed(2)}',
                  style: TextStyle(
                    fontSize: 16,
                    fontWeight: FontWeight.bold,
                    color: isExpense ? Colors.red : Colors.green,
                  ),
                ),
              ),
            ),
          );
        },
      ),
    );
  }

  void _addTransaction() async {
    final result = await Navigator.push(
      context,
      MaterialPageRoute(
        builder: (_) => AddEditTransactionScreen(accountId: widget.accountId),
      ),
    );
    if (result == true) _loadData();
  }
}
