import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../models/account.dart';
import '../database/database_helper.dart';
import 'account_detail_screen.dart';
import 'add_account_screen.dart';
import 'reports_screen.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  List<Account> _accounts = [];
  Map<int, double> _balances = {};
  bool _loading = true;

  @override
  void initState() {
    super.initState();
    _loadAccounts();
  }

  Future<void> _loadAccounts() async {
    setState(() => _loading = true);
    final db = DatabaseHelper.instance;
    final accounts = await db.getAccounts();
    final balances = <int, double>{};
    for (final acc in accounts) {
      if (acc.id != null) {
        balances[acc.id!] = await db.getAccountBalance(acc.id!);
      }
    }
    setState(() {
      _accounts = accounts;
      _balances = balances;
      _loading = false;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('مدير المصروفات'),
        actions: [
          IconButton(
            icon: const Icon(Icons.bar_chart),
            onPressed: () {
              Navigator.push(
                context,
                MaterialPageRoute(builder: (_) => const ReportsScreen()),
              );
            },
          ),
        ],
      ),
      body: _loading
          ? const Center(child: CircularProgressIndicator())
          : _accounts.isEmpty
              ? Center(
                  child: Column(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      const Text('لا توجد حسابات بعد',
                          style: TextStyle(fontSize: 18, color: Colors.grey)),
                      const SizedBox(height: 16),
                      ElevatedButton.icon(
                        onPressed: () => _addAccount(),
                        icon: const Icon(Icons.add),
                        label: const Text('أضف حسابك الأول'),
                      ),
                    ],
                  ),
                )
              : RefreshIndicator(
                  onRefresh: _loadAccounts,
                  child: ListView.builder(
                    padding: const EdgeInsets.all(12),
                    itemCount: _accounts.length,
                    itemBuilder: (context, index) {
                      final acc = _accounts[index];
                      final balance = _balances[acc.id] ?? 0.0;
                      final isPersonal = acc.type == 'شخصي';
                      return Card(
                        margin: const EdgeInsets.only(bottom: 10),
                        child: ListTile(
                          leading: CircleAvatar(
                            backgroundColor:
                                isPersonal ? Colors.green.shade100 : Colors.blue.shade100,
                            child: Text(acc.icon, style: const TextStyle(fontSize: 24)),
                          ),
                          title: Text(acc.name,
                              style: const TextStyle(fontWeight: FontWeight.bold)),
                          subtitle: Text(
                            acc.type == 'شخصي' ? 'حساب شخصي' : 'مشروع',
                            style: TextStyle(color: Colors.grey.shade600, fontSize: 12),
                          ),
                          trailing: Column(
                            mainAxisAlignment: MainAxisAlignment.center,
                            crossAxisAlignment: CrossAxisAlignment.end,
                            children: [
                              Text(
                                '${balance.toStringAsFixed(2)} د.ع',
                                style: TextStyle(
                                  fontWeight: FontWeight.bold,
                                  fontSize: 16,
                                  color: balance >= 0 ? Colors.green : Colors.red,
                                ),
                              ),
                              Text(
                                balance >= 0 ? 'متاح' : 'مديون',
                                style: TextStyle(
                                    color: Colors.grey.shade500, fontSize: 11),
                              ),
                            ],
                          ),
                          onTap: () async {
                            await Navigator.push(
                              context,
                              MaterialPageRoute(
                                builder: (_) =>
                                    AccountDetailScreen(accountId: acc.id!, accountName: acc.name),
                              ),
                            );
                            _loadAccounts();
                          },
                          onLongPress: () => _deleteAccount(acc),
                        ),
                      );
                    },
                  ),
                ),
      floatingActionButton: FloatingActionButton(
        onPressed: _addAccount,
        child: const Icon(Icons.add),
      ),
    );
  }

  void _addAccount() async {
    final result = await Navigator.push(
      context,
      MaterialPageRoute(builder: (_) => const AddAccountScreen()),
    );
    if (result == true) _loadAccounts();
  }

  void _deleteAccount(Account acc) async {
    final confirm = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('حذف الحساب'),
        content: Text('هل أنت متأكد من حذف "${acc.name}"؟\nسيتم حذف جميع المعاملات المرتبطة به.'),
        actions: [
          TextButton(onPressed: () => Navigator.pop(ctx, false), child: const Text('إلغاء')),
          TextButton(
            onPressed: () => Navigator.pop(ctx, true),
            child: const Text('حذف', style: TextStyle(color: Colors.red)),
          ),
        ],
      ),
    );
    if (confirm == true && acc.id != null) {
      await DatabaseHelper.instance.deleteAccount(acc.id!);
      _loadAccounts();
    }
  }
}
