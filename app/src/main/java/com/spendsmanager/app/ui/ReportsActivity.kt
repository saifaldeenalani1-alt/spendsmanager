package com.spendsmanager.app.ui

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.spendsmanager.app.R
import com.spendsmanager.app.data.DatabaseHelper
import java.util.Calendar

class ReportsActivity : AppCompatActivity() {
    private lateinit var db: DatabaseHelper
    private var year = Calendar.getInstance().get(Calendar.YEAR)
    private var month = Calendar.getInstance().get(Calendar.MONTH) + 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reports)
        supportActionBar?.title = "التقارير"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        db = DatabaseHelper(this)
        findViewById<Button>(R.id.btnPrevMonth).setOnClickListener {
            month--
            if (month < 1) { month = 12; year-- }
            loadReport()
        }
        findViewById<Button>(R.id.btnNextMonth).setOnClickListener {
            month++
            if (month > 12) { month = 1; year++ }
            loadReport()
        }
        loadReport()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun loadReport() {
        val ym = String.format("%04d-%02d", year, month)
        findViewById<TextView>(R.id.txtMonthTitle).text = "$year/${month.toString().padStart(2, '0')}"

        val income = db.getTotalIncomeForMonth(ym)
        val expense = db.getTotalExpenseForMonth(ym)
        val balance = income - expense

        findViewById<TextView>(R.id.txtTotalIncome).text = String.format("%.0f د.ع", income)
        findViewById<TextView>(R.id.txtTotalExpense).text = String.format("%.0f د.ع", expense)
        findViewById<TextView>(R.id.txtNetBalance).text = String.format("%.0f د.ع", balance)

        val transactions = db.getAllTransactionsForMonth(ym)
        val txnLayout = findViewById<LinearLayout>(R.id.layoutTransactions)
        txnLayout.removeAllViews()

        if (transactions.isEmpty()) {
            txnLayout.addView(TextView(this).apply {
                text = "لا توجد معاملات لهذا الشهر"
                textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                setPadding(0, 16, 0, 16)
                setTextColor(ContextCompat.getColor(this@ReportsActivity, R.color.textSecondary))
            })
        }
        for (t in transactions) {
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(8, 8, 8, 8)
            }
            val accName = db.getAccountName(t.accountId)
            val sign = if (t.type == "مصروف") "-" else "+"
            row.addView(TextView(this).apply {
                text = "${t.date} | $accName"
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                textSize = 13f
            })
            row.addView(TextView(this).apply {
                text = "$sign${String.format("%.0f", t.amount)}"
                setTextColor(ContextCompat.getColor(this@ReportsActivity, if (t.type == "مصروف") R.color.expense else R.color.income))
                textSize = 14f
                setTypeface(null, android.graphics.Typeface.BOLD)
            })
            txnLayout.addView(row)
        }
    }
}