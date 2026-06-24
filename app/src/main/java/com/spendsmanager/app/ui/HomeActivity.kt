package com.spendsmanager.app.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.spendsmanager.app.R
import com.spendsmanager.app.adapter.AccountAdapter
import com.spendsmanager.app.data.Account
import com.spendsmanager.app.data.DatabaseHelper

class HomeActivity : AppCompatActivity() {
    private lateinit var db: DatabaseHelper
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AccountAdapter
    private val accounts = mutableListOf<Account>()
    private val balances = mutableMapOf<Long, Double>()

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
        findViewById<Button>(R.id.btnAddAccount).setOnClickListener { addAccount() }
        findViewById<Button>(R.id.btnReports).setOnClickListener { reports() }
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
}
