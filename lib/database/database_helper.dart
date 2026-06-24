import 'package:sqflite/sqflite.dart';
import 'package:path/path.dart';
import '../models/account.dart';
import '../models/transaction.dart';
import '../models/expense_category.dart';

class DatabaseHelper {
  static Database? _database;
  static final DatabaseHelper instance = DatabaseHelper._();

  DatabaseHelper._();

  Future<Database> get database async {
    if (_database != null) return _database!;
    _database = await _initDatabase();
    return _database!;
  }

  Future<Database> _initDatabase() async {
    final dbPath = await getDatabasesPath();
    final path = join(dbPath, 'madear_almasroofat.db');
    return await openDatabase(
      path,
      version: 1,
      onCreate: _onCreate,
    );
  }

  Future<void> _onCreate(Database db, int version) async {
    await db.execute('''
      CREATE TABLE accounts (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        name TEXT NOT NULL,
        type TEXT NOT NULL,
        initial_balance REAL DEFAULT 0,
        created_at TEXT NOT NULL,
        icon TEXT DEFAULT '📁'
      )
    ''');

    await db.execute('''
      CREATE TABLE transactions (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        account_id INTEGER NOT NULL,
        type TEXT NOT NULL,
        amount REAL NOT NULL,
        category TEXT DEFAULT '',
        description TEXT DEFAULT '',
        date TEXT NOT NULL,
        created_at TEXT NOT NULL,
        FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE
      )
    ''');

    await db.execute('''
      CREATE TABLE categories (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        name TEXT NOT NULL,
        icon TEXT DEFAULT '📌'
      )
    ''');

    for (final cat in ExpenseCategory.defaults()) {
      await db.insert('categories', cat.toMap());
    }
  }

  Future<int> insertAccount(Account account) async {
    final db = await database;
    return await db.insert('accounts', account.toMap());
  }

  Future<List<Account>> getAccounts() async {
    final db = await database;
    final maps = await db.query('accounts', orderBy: 'created_at DESC');
    return maps.map((map) => Account.fromMap(map)).toList();
  }

  Future<int> updateAccount(Account account) async {
    final db = await database;
    return await db.update(
      'accounts',
      account.toMap(),
      where: 'id = ?',
      whereArgs: [account.id],
    );
  }

  Future<int> deleteAccount(int id) async {
    final db = await database;
    await db.delete('transactions', where: 'account_id = ?', whereArgs: [id]);
    return await db.delete('accounts', where: 'id = ?', whereArgs: [id]);
  }

  Future<int> insertTransaction(Transaction transaction) async {
    final db = await database;
    return await db.insert('transactions', transaction.toMap());
  }

  Future<List<Transaction>> getTransactions(int accountId, {int? limit}) async {
    final db = await database;
    final maps = await db.query(
      'transactions',
      where: 'account_id = ?',
      whereArgs: [accountId],
      orderBy: 'date DESC, id DESC',
      limit: limit,
    );
    return maps.map((map) => Transaction.fromMap(map)).toList();
  }

  Future<List<Transaction>> getTransactionsByDateRange(
    int accountId, String startDate, String endDate) async {
    final db = await database;
    final maps = await db.query(
      'transactions',
      where: 'account_id = ? AND date >= ? AND date <= ?',
      whereArgs: [accountId, startDate, endDate],
      orderBy: 'date DESC, id DESC',
    );
    return maps.map((map) => Transaction.fromMap(map)).toList();
  }

  Future<List<Transaction>> getAllTransactionsByMonth(
      String startDate, String endDate) async {
    final db = await database;
    final maps = await db.query(
      'transactions',
      where: 'date >= ? AND date <= ?',
      whereArgs: [startDate, endDate],
      orderBy: 'date DESC, id DESC',
    );
    return maps.map((map) => Transaction.fromMap(map)).toList();
  }

  Future<double> getAccountBalance(int accountId) async {
    final db = await database;
    final accountMaps = await db.query(
      'accounts',
      where: 'id = ?',
      whereArgs: [accountId],
    );
    if (accountMaps.isEmpty) return 0.0;
    final account = Account.fromMap(accountMaps.first);

    final incomeResult = await db.rawQuery(
      'SELECT COALESCE(SUM(amount), 0) as total FROM transactions WHERE account_id = ? AND type = ?',
      [accountId, 'وارد'],
    );
    final expenseResult = await db.rawQuery(
      'SELECT COALESCE(SUM(amount), 0) as total FROM transactions WHERE account_id = ? AND type = ?',
      [accountId, 'مصروف'],
    );

    final income = (incomeResult.first['total'] as num?)?.toDouble() ?? 0.0;
    final expense = (expenseResult.first['total'] as num?)?.toDouble() ?? 0.0;
    return account.initialBalance + income - expense;
  }

  Future<int> deleteTransaction(int id) async {
    final db = await database;
    return await db.delete('transactions', where: 'id = ?', whereArgs: [id]);
  }

  Future<List<ExpenseCategory>> getCategories() async {
    final db = await database;
    final maps = await db.query('categories');
    return maps.map((map) => ExpenseCategory.fromMap(map)).toList();
  }
}
