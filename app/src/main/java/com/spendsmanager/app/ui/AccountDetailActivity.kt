package com.spendsmanager.app.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.spendsmanager.app.R
import com.spendsmanager.app.adapter.TransactionAdapter
import com.spendsmanager.app.data.DatabaseHelper
import com.spendsmanager.app.data.Transaction

class AccountDetailActivity : AppCompatActivity() {
    private lateinit var db: DatabaseHelper
    private lateinit var adapter: TransactionAdapter
    private val transactions = mutableListOf<Transaction>()
    private var accountId: Long = 0

    private val addTransactionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK) loadData()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_detail)
        db = DatabaseHelper(this)
        accountId = intent.getLongExtra("accountId", 0)
        val accountName = intent.getStringExtra("accountName") ?: ""
        supportActionBar?.title = accountName
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerTransactions)
        adapter = TransactionAdapter(transactions, onDelete = { t ->
            db.deleteTransaction(t.id)
            loadData()
            Toast.makeText(this, "تم الحذف", Toast.LENGTH_SHORT).show()
        })
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        findViewById<MaterialButton>(R.id.btnAddTransaction).setOnClickListener {
            addTransactionLauncher.launch(Intent(this, AddTransactionActivity::class.java).apply {
                putExtra("accountId", accountId)
            })
        }
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun loadData() {
        transactions.clear()
        transactions.addAll(db.getTransactions(accountId))
        adapter.notifyDataSetChanged()
        val balance = db.getAccountBalance(accountId)
        val totalIncome = transactions.filter { it.type == "وارد" }.sumOf { it.amount }
        val totalExpense = transactions.filter { it.type == "مصروف" }.sumOf { it.amount }
        findViewById<TextView>(R.id.txtBalance).text = String.format("%.0f د.ع", balance)
        findViewById<TextView>(R.id.txtIncome).text = String.format("%.0f د.ع", totalIncome)
        findViewById<TextView>(R.id.txtExpense).text = String.format("%.0f د.ع", totalExpense)
        findViewById<LinearLayout>(R.id.txtEmpty).visibility = if (transactions.isEmpty()) View.VISIBLE else View.GONE
    }
}