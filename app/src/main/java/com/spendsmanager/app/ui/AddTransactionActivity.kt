package com.spendsmanager.app.ui

import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.spendsmanager.app.R
import com.spendsmanager.app.data.DatabaseHelper
import com.spendsmanager.app.data.Transaction
import java.util.Calendar

class AddTransactionActivity : AppCompatActivity() {
    private var selectedType = "مصروف"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)
        supportActionBar?.title = "تسجيل معاملة"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val accountId = intent.getLongExtra("accountId", 0)
        val db = DatabaseHelper(this)

        val edtAmount = findViewById<EditText>(R.id.edtAmount)
        val edtDesc = findViewById<EditText>(R.id.edtDescription)
        val btnIncome = findViewById<MaterialButton>(R.id.btnIncome)
        val btnExpense = findViewById<MaterialButton>(R.id.btnExpense)
        val flowCategories = findViewById<LinearLayout>(R.id.flowCategories)
        val cardCategories = findViewById<com.google.android.material.card.MaterialCardView>(R.id.cardCategories)
        val txtDate = findViewById<TextView>(R.id.txtDate)
        val btnSave = findViewById<com.google.android.material.button.MaterialButton>(R.id.btnSaveTransaction)

        val incomeColor = ContextCompat.getColor(this, R.color.income)
        val expenseColor = ContextCompat.getColor(this, R.color.expense)
        val incomeLight = ContextCompat.getColor(this, R.color.incomeLight)
        val expenseLight = ContextCompat.getColor(this, R.color.expenseLight)

        fun updateTypeButtons() {
            val isIncome = selectedType == "وارد"
            btnIncome.setBackgroundColor(if (isIncome) incomeColor else Color.TRANSPARENT)
            btnIncome.setTextColor(if (isIncome) Color.WHITE else incomeColor)
            btnExpense.setBackgroundColor(if (!isIncome) expenseColor else Color.TRANSPARENT)
            btnExpense.setTextColor(if (!isIncome) Color.WHITE else expenseColor)
            cardCategories.visibility = if (!isIncome) android.view.View.VISIBLE else android.view.View.GONE
        }

        btnIncome.setOnClickListener {
            selectedType = "وارد"
            updateTypeButtons()
        }
        btnExpense.setOnClickListener {
            selectedType = "مصروف"
            updateTypeButtons()
        }

        val categories = db.getCategories()
        val catChips = mutableListOf<RadioButton>()
        for ((name, icon) in categories) {
            val chip = RadioButton(this).apply {
                text = "$icon $name"
                tag = name
                setOnClickListener {
                    catChips.forEach { it.isChecked = it == this }
                }
            }
            flowCategories.addView(chip)
            catChips.add(chip)
        }

        updateTypeButtons()

        val cal = Calendar.getInstance()
        val dateStr = String.format("%04d-%02d-%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH))
        txtDate.text = dateStr.replace("-", "/")
        var selectedDate = dateStr

        txtDate.setOnClickListener {
            DatePickerDialog(
                this,
                { _, y, m, d ->
                    selectedDate = String.format("%04d-%02d-%02d", y, m + 1, d)
                    txtDate.text = selectedDate.replace("-", "/")
                },
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        btnSave.setOnClickListener {
            val amount = edtAmount.text.toString().toDoubleOrNull()
            if (amount == null || amount <= 0) {
                edtAmount.error = "الرجاء إدخال مبلغ صحيح"
                return@setOnClickListener
            }
            val cat = catChips.find { it.isChecked }?.tag?.toString() ?: ""
            db.insertTransaction(Transaction(
                accountId = accountId,
                type = selectedType,
                amount = amount,
                category = cat,
                description = edtDesc.text.toString().trim(),
                date = selectedDate
            ))
            Toast.makeText(this, "تم التسجيل", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
