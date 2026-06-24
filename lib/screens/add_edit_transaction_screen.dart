import 'package:flutter/material.dart';
import '../models/transaction.dart';
import '../models/expense_category.dart';
import '../database/database_helper.dart';

class AddEditTransactionScreen extends StatefulWidget {
  final int accountId;

  const AddEditTransactionScreen({super.key, required this.accountId});

  @override
  State<AddEditTransactionScreen> createState() => _AddEditTransactionScreenState();
}

class _AddEditTransactionScreenState extends State<AddEditTransactionScreen> {
  final _formKey = GlobalKey<FormState>();
  final _amountController = TextEditingController();
  final _descController = TextEditingController();
  String _type = 'مصروف';
  String _category = '';
  DateTime _selectedDate = DateTime.now();
  List<ExpenseCategory> _categories = [];
  bool _saving = false;

  @override
  void initState() {
    super.initState();
    _loadCategories();
  }

  Future<void> _loadCategories() async {
    final cats = await DatabaseHelper.instance.getCategories();
    setState(() => _categories = cats);
  }

  @override
  void dispose() {
    _amountController.dispose();
    _descController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(_type == 'مصروف' ? 'تسجيل مصروف' : 'تسجيل وارد')),
      body: Form(
        key: _formKey,
        child: ListView(
          padding: const EdgeInsets.all(20),
          children: [
            Row(
              children: [
                Expanded(
                  child: _typeButton('مصروف', Icons.arrow_upward, Colors.red),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: _typeButton('وارد', Icons.arrow_downward, Colors.green),
                ),
              ],
            ),
            const SizedBox(height: 24),
            TextFormField(
              controller: _amountController,
              decoration: const InputDecoration(
                labelText: 'المبلغ',
                border: OutlineInputBorder(),
                prefixText: 'د.ع  ',
              ),
              keyboardType: TextInputType.number,
              validator: (v) {
                if (v == null || v.trim().isEmpty) return 'الرجاء إدخال المبلغ';
                final amount = double.tryParse(v);
                if (amount == null || amount <= 0) return 'الرجاء إدخال مبلغ صحيح';
                return null;
              },
            ),
            const SizedBox(height: 16),
            if (_type == 'مصروف')
              _buildCategorySelector(),
            if (_type == 'مصروف' && _category.isNotEmpty)
              Padding(
                padding: const EdgeInsets.only(bottom: 16),
                child: Text('التصنيف المختار: $_category',
                    style: TextStyle(color: Colors.grey.shade600)),
              ),
            TextFormField(
              controller: _descController,
              decoration: const InputDecoration(
                labelText: 'الوصف (اختياري)',
                border: OutlineInputBorder(),
                hintText: 'تفاصيل المعاملة...',
              ),
              maxLines: 2,
            ),
            const SizedBox(height: 16),
            ListTile(
              leading: const Icon(Icons.calendar_today),
              title: Text(
                'التاريخ: ${_selectedDate.year}/${_selectedDate.month.toString().padLeft(2, '0')}/${_selectedDate.day.toString().padLeft(2, '0')}',
              ),
              trailing: const Icon(Icons.edit_calendar),
              onTap: _pickDate,
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(8),
                side: BorderSide(color: Colors.grey.shade300),
              ),
            ),
            const SizedBox(height: 32),
            SizedBox(
              height: 50,
              child: ElevatedButton(
                onPressed: _saving ? null : _save,
                style: ElevatedButton.styleFrom(
                  backgroundColor: _type == 'مصروف' ? Colors.red : Colors.green,
                  foregroundColor: Colors.white,
                ),
                child: _saving
                    ? const SizedBox(
                        width: 24,
                        height: 24,
                        child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white),
                      )
                    : Text('${_type == 'مصروف' ? 'تسجيل مصروف' : 'تسجيل وارد'}',
                        style: const TextStyle(fontSize: 16)),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _typeButton(String type, IconData icon, Color color) {
    final selected = _type == type;
    return GestureDetector(
      onTap: () => setState(() => _type = type),
      child: Container(
        padding: const EdgeInsets.symmetric(vertical: 16),
        decoration: BoxDecoration(
          color: selected ? color.withOpacity(0.1) : Colors.grey.shade100,
          borderRadius: BorderRadius.circular(12),
          border: Border.all(
            color: selected ? color : Colors.transparent,
            width: 2,
          ),
        ),
        child: Column(
          children: [
            Icon(icon, color: selected ? color : Colors.grey, size: 32),
            const SizedBox(height: 8),
            Text(
              type == 'مصروف' ? 'مصروف' : 'وارد',
              style: TextStyle(
                fontWeight: FontWeight.bold,
                color: selected ? color : Colors.grey,
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildCategorySelector() {
    if (_categories.isEmpty) return const SizedBox.shrink();
    return Wrap(
      spacing: 8,
      runSpacing: 8,
      children: _categories.map((cat) {
        final selected = _category == cat.name;
        return FilterChip(
          label: Text('${cat.icon} ${cat.name}'),
          selected: selected,
          onSelected: (v) => setState(() => _category = v ? cat.name : ''),
          selectedColor: Colors.blue.shade50,
          checkmarkColor: Colors.blue,
        );
      }).toList(),
    );
  }

  Future<void> _pickDate() async {
    final picked = await showDatePicker(
      context: context,
      initialDate: _selectedDate,
      firstDate: DateTime(2020),
      lastDate: DateTime(2030),
      locale: const Locale('ar'),
    );
    if (picked != null) setState(() => _selectedDate = picked);
  }

  Future<void> _save() async {
    if (!_formKey.currentState!.validate()) return;
    setState(() => _saving = true);
    final transaction = Transaction(
      accountId: widget.accountId,
      type: _type,
      amount: double.parse(_amountController.text.trim()),
      category: _category,
      description: _descController.text.trim(),
      date: _selectedDate.toIso8601String().substring(0, 10),
    );
    await DatabaseHelper.instance.insertTransaction(transaction);
    if (mounted) Navigator.pop(context, true);
  }
}
