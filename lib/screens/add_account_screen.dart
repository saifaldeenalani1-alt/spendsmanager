import 'package:flutter/material.dart';
import '../models/account.dart';
import '../database/database_helper.dart';

class AddAccountScreen extends StatefulWidget {
  const AddAccountScreen({super.key});

  @override
  State<AddAccountScreen> createState() => _AddAccountScreenState();
}

class _AddAccountScreenState extends State<AddAccountScreen> {
  final _formKey = GlobalKey<FormState>();
  final _nameController = TextEditingController();
  final _balanceController = TextEditingController();
  String _type = 'شخصي';
  String _icon = '📁';
  bool _saving = false;

  final _icons = ['📁', '🏠', '🚗', '💼', '🏪', '💰', '🎓', '🏥', '🛒', '📊', '🏗️', '🌾'];

  @override
  void dispose() {
    _nameController.dispose();
    _balanceController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('حساب جديد')),
      body: Form(
        key: _formKey,
        child: ListView(
          padding: const EdgeInsets.all(20),
          children: [
            Text('اختر الأيقونة', style: TextStyle(color: Colors.grey.shade600)),
            const SizedBox(height: 8),
            SizedBox(
              height: 50,
              child: ListView.separated(
                scrollDirection: Axis.horizontal,
                itemCount: _icons.length,
                separatorBuilder: (_, __) => const SizedBox(width: 8),
                itemBuilder: (_, i) {
                  final selected = _icon == _icons[i];
                  return GestureDetector(
                    onTap: () => setState(() => _icon = _icons[i]),
                    child: Container(
                      padding: const EdgeInsets.all(8),
                      decoration: BoxDecoration(
                        color: selected ? Colors.blue.shade50 : Colors.grey.shade100,
                        borderRadius: BorderRadius.circular(12),
                        border: Border.all(
                          color: selected ? Colors.blue : Colors.transparent,
                          width: 2,
                        ),
                      ),
                      child: Text(_icons[i], style: const TextStyle(fontSize: 28)),
                    ),
                  );
                },
              ),
            ),
            const SizedBox(height: 24),
            TextFormField(
              controller: _nameController,
              decoration: const InputDecoration(
                labelText: 'اسم الحساب',
                border: OutlineInputBorder(),
                hintText: 'مثال: مصروفات البيت',
              ),
              validator: (v) => v == null || v.trim().isEmpty ? 'الرجاء إدخال اسم الحساب' : null,
            ),
            const SizedBox(height: 16),
            DropdownButtonFormField<String>(
              value: _type,
              decoration: const InputDecoration(
                labelText: 'نوع الحساب',
                border: OutlineInputBorder(),
              ),
              items: const [
                DropdownMenuItem(value: 'شخصي', child: Text('شخصي')),
                DropdownMenuItem(value: 'مشروع', child: Text('مشروع')),
              ],
              onChanged: (v) => setState(() => _type = v ?? 'شخصي'),
            ),
            const SizedBox(height: 16),
            TextFormField(
              controller: _balanceController,
              decoration: const InputDecoration(
                labelText: 'الرصيد الابتدائي',
                border: OutlineInputBorder(),
                hintText: '0',
              ),
              keyboardType: TextInputType.number,
            ),
            const SizedBox(height: 32),
            SizedBox(
              height: 50,
              child: ElevatedButton(
                onPressed: _saving ? null : _save,
                child: _saving
                    ? const SizedBox(
                        width: 24,
                        height: 24,
                        child: CircularProgressIndicator(strokeWidth: 2),
                      )
                    : const Text('حفظ الحساب', style: TextStyle(fontSize: 16)),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Future<void> _save() async {
    if (!_formKey.currentState!.validate()) return;
    setState(() => _saving = true);
    final balance = double.tryParse(_balanceController.text) ?? 0.0;
    final account = Account(
      name: _nameController.text.trim(),
      type: _type,
      initialBalance: balance,
      icon: _icon,
    );
    await DatabaseHelper.instance.insertAccount(account);
    if (mounted) Navigator.pop(context, true);
  }
}
