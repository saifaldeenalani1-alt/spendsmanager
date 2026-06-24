package com.spendsmanager.app.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.spendsmanager.app.R
import com.spendsmanager.app.adapter.AccountAdapter
import com.spendsmanager.app.data.Account
import com.spendsmanager.app.data.DatabaseHelper
import java.io.File

class HomeActivity : AppCompatActivity() {
    private lateinit var db: DatabaseHelper
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AccountAdapter
    private val accounts = mutableListOf<Account>()
    private val balances = mutableMapOf<Long, Double>()

    private val exportLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri ->
        if (uri != null) exportDatabase(uri)
    }

    private val importLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) importDatabase(uri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        db = DatabaseHelper(this)
        recyclerView = findViewById(R.id.recyclerAccounts)
        adapter = AccountAdapter(accounts, balances,
            onClick = { acc -> openAccount(acc) },
            onLongClick = { acc -> deleteAccount(acc) }
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        findViewById<MaterialButton>(R.id.btnAddAccount).setOnClickListener { addAccount() }
        findViewById<MaterialButton>(R.id.btnReports).setOnClickListener { reports() }
        findViewById<MaterialButton>(R.id.btnExport).setOnClickListener { exportLauncher.launch("spendsmanager_backup.db") }
        findViewById<MaterialButton>(R.id.btnImport).setOnClickListener { importLauncher.launch(arrayOf("application/octet-stream", "application/x-sqlite3")) }
    }

    override fun onResume() {
        super.onResume()
        loadAccounts()
    }

    private fun loadAccounts() {
        accounts.clear()
        accounts.addAll(db.getAccounts())
        balances.clear()
        for (acc in accounts) {
            balances[acc.id] = db.getAccountBalance(acc.id)
        }
        adapter.notifyDataSetChanged()
        findViewById<TextView>(R.id.txtEmpty).visibility = if (accounts.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
        findViewById<TextView>(R.id.txtAccountCount).text = "${accounts.size} حساب"
    }

    private fun openAccount(acc: Account) {
        startActivity(Intent(this, AccountDetailActivity::class.java).apply {
            putExtra("accountId", acc.id)
            putExtra("accountName", acc.name)
        })
    }

    private fun addAccount() {
        startActivity(Intent(this, AddAccountActivity::class.java))
    }

    private fun reports() {
        startActivity(Intent(this, ReportsActivity::class.java))
    }

    private fun deleteAccount(acc: Account) {
        AlertDialog.Builder(this)
            .setTitle("حذف الحساب")
            .setMessage("هل أنت متأكد من حذف \"${acc.name}\"؟\nسيتم حذف جميع المعاملات المرتبطة به.")
            .setPositiveButton("حذف") { _, _ ->
                db.deleteAccount(acc.id)
                loadAccounts()
                Toast.makeText(this, "تم الحذف", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("إلغاء", null)
            .show()
    }

    private fun exportDatabase(uri: Uri) {
        try {
            val data = db.exportDatabase()
            contentResolver.openOutputStream(uri)?.use { it.write(data) }
            Toast.makeText(this, "تم تصدير النسخة الاحتياطية", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "فشل التصدير: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun importDatabase(uri: Uri) {
        AlertDialog.Builder(this)
            .setTitle("استيراد نسخة احتياطية")
            .setMessage("سيتم استبدال جميع البيانات الحالية. هل أنت متأكد؟")
            .setPositiveButton("تأكيد") { _, _ ->
                try {
                    val data = contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return@setPositiveButton
                    db.importDatabase(data)
                    loadAccounts()
                    Toast.makeText(this, "تم استيراد النسخة الاحتياطية", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(this, "فشل الاستيراد: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
            .setNegativeButton("إلغاء", null)
            .show()
    }
}