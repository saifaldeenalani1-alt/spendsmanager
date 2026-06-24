package com.spendsmanager.app.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {
    private val appContext = context.applicationContext

    companion object {
        private const val DB_NAME = "spendsmanager.db"
        private const val DB_VERSION = 2
        private const val TABLE_ACCOUNTS = "accounts"
        private const val TABLE_TRANSACTIONS = "transactions"
        private const val TABLE_CATEGORIES = "categories"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE $TABLE_ACCOUNTS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                type TEXT NOT NULL,
                initial_balance REAL DEFAULT 0,
                icon TEXT DEFAULT '📁',
                created_at TEXT NOT NULL
            )
        """)
        db.execSQL("""
            CREATE TABLE $TABLE_TRANSACTIONS (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                account_id INTEGER NOT NULL,
                type TEXT NOT NULL,
                amount REAL NOT NULL,
                description TEXT DEFAULT '',
                date TEXT NOT NULL,
                FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE
            )
        """)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVer: Int, newVer: Int) {
        if (oldVer < 2) {
            db.execSQL("DROP TABLE IF EXISTS $TABLE_CATEGORIES")
            db.execSQL("CREATE TABLE transactions_v2 (id INTEGER PRIMARY KEY AUTOINCREMENT, account_id INTEGER NOT NULL, type TEXT NOT NULL, amount REAL NOT NULL, description TEXT DEFAULT '', date TEXT NOT NULL)")
            db.execSQL("INSERT INTO transactions_v2 (id, account_id, type, amount, description, date) SELECT id, account_id, type, amount, IFNULL(description, ''), date FROM $TABLE_TRANSACTIONS")
            db.execSQL("DROP TABLE $TABLE_TRANSACTIONS")
            db.execSQL("ALTER TABLE transactions_v2 RENAME TO $TABLE_TRANSACTIONS")
        }
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    private fun now(): String = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(Date())

    fun insertAccount(acc: Account): Long {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put("name", acc.name)
            put("type", acc.type)
            put("initial_balance", acc.initialBalance)
            put("icon", acc.icon)
            put("created_at", now())
        }
        return db.insert(TABLE_ACCOUNTS, null, cv)
    }

    fun getAccounts(): List<Account> {
        val db = readableDatabase
        val cursor = db.query(TABLE_ACCOUNTS, null, null, null, null, null, "created_at DESC")
        val list = mutableListOf<Account>()
        while (cursor.moveToNext()) {
            list.add(Account(
                id = cursor.getLong(0),
                name = cursor.getString(1),
                type = cursor.getString(2),
                initialBalance = cursor.getDouble(3),
                icon = cursor.getString(4),
                createdAt = cursor.getString(5)
            ))
        }
        cursor.close()
        return list
    }

    fun deleteAccount(id: Long) {
        val db = writableDatabase
        db.delete(TABLE_TRANSACTIONS, "account_id = ?", arrayOf(id.toString()))
        db.delete(TABLE_ACCOUNTS, "id = ?", arrayOf(id.toString()))
    }

    fun insertTransaction(t: Transaction): Long {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put("account_id", t.accountId)
            put("type", t.type)
            put("amount", t.amount)
            put("description", t.description)
            put("date", t.date)
        }
        return db.insert(TABLE_TRANSACTIONS, null, cv)
    }

    fun getTransactions(accountId: Long): List<Transaction> {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_TRANSACTIONS, null,
            "account_id = ?", arrayOf(accountId.toString()),
            null, null, "date DESC, id DESC"
        )
        val list = mutableListOf<Transaction>()
        while (cursor.moveToNext()) {
            list.add(Transaction(
                id = cursor.getLong(0),
                accountId = cursor.getLong(1),
                type = cursor.getString(2),
                amount = cursor.getDouble(3),
                description = cursor.getString(4),
                date = cursor.getString(5)
            ))
        }
        cursor.close()
        return list
    }

    fun getAccountBalance(accountId: Long): Double {
        val db = readableDatabase
        val cursor = db.query(TABLE_ACCOUNTS, null, "id = ?", arrayOf(accountId.toString()), null, null, null)
        var initialBalance = 0.0
        if (cursor.moveToFirst()) initialBalance = cursor.getDouble(3)
        cursor.close()

        var income = 0.0
        var expense = 0.0

        val incCur = db.rawQuery(
            "SELECT COALESCE(SUM(amount),0) FROM $TABLE_TRANSACTIONS WHERE account_id = ? AND type = 'وارد'",
            arrayOf(accountId.toString())
        )
        if (incCur.moveToFirst()) income = incCur.getDouble(0)
        incCur.close()

        val expCur = db.rawQuery(
            "SELECT COALESCE(SUM(amount),0) FROM $TABLE_TRANSACTIONS WHERE account_id = ? AND type = 'مصروف'",
            arrayOf(accountId.toString())
        )
        if (expCur.moveToFirst()) expense = expCur.getDouble(0)
        expCur.close()

        return initialBalance + income - expense
    }

    fun deleteTransaction(id: Long) {
        writableDatabase.delete(TABLE_TRANSACTIONS, "id = ?", arrayOf(id.toString()))
    }

    fun getTotalIncomeForMonth(yearMonth: String): Double {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT COALESCE(SUM(amount),0) FROM $TABLE_TRANSACTIONS WHERE type='وارد' AND date LIKE ?",
            arrayOf("$yearMonth%")
        )
        var total = 0.0
        if (cursor.moveToFirst()) total = cursor.getDouble(0)
        cursor.close()
        return total
    }

    fun getTotalExpenseForMonth(yearMonth: String): Double {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT COALESCE(SUM(amount),0) FROM $TABLE_TRANSACTIONS WHERE type='مصروف' AND date LIKE ?",
            arrayOf("$yearMonth%")
        )
        var total = 0.0
        if (cursor.moveToFirst()) total = cursor.getDouble(0)
        cursor.close()
        return total
    }

    fun getAllTransactionsForMonth(yearMonth: String): List<Transaction> {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_TRANSACTIONS, null,
            "date LIKE ?", arrayOf("$yearMonth%"),
            null, null, "date DESC, id DESC"
        )
        val list = mutableListOf<Transaction>()
        while (cursor.moveToNext()) {
            list.add(Transaction(
                id = cursor.getLong(0),
                accountId = cursor.getLong(1),
                type = cursor.getString(2),
                amount = cursor.getDouble(3),
                description = cursor.getString(4),
                date = cursor.getString(5)
            ))
        }
        cursor.close()
        return list
    }

    fun getAccountName(id: Long): String {
        val db = readableDatabase
        val cursor = db.query(TABLE_ACCOUNTS, arrayOf("name"), "id = ?", arrayOf(id.toString()), null, null, null)
        var name = ""
        if (cursor.moveToFirst()) name = cursor.getString(0)
        cursor.close()
        return name
    }

    fun exportDatabase(): ByteArray {
        val db = readableDatabase
        val path = db.path
        return java.io.File(path).readBytes()
    }

    fun importDatabase(data: ByteArray) {
        close()
        appContext.deleteDatabase(DB_NAME)
        val path = appContext.getDatabasePath(DB_NAME).absolutePath
        java.io.File(path).writeBytes(data)
    }
}