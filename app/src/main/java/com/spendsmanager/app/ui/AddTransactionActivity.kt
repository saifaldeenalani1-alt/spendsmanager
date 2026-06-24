package com.spendsmanager.app.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.spendsmanager.app.R
import com.spendsmanager.app.data.DatabaseHelper
import com.spendsmanager.app.data.Transaction
import java.util.Calendar

class AddTransactionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)
        supportActionBar?.title = "تسجيل معاملة"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val accountId = intent.getLongExtra("accountId", 0)
        val db = DatabaseHelper(this)

        val edtAmount = findViewById<EditText>(R.id.edtAmount)
        val edtDesc = findViewById<EditText>(R.id.edtDescription)
        val spinnerType = findViewById<Spinner>(R.id.spinnerType)
        val flowCategories = findViewById<LinearLayout>(R.id.flowCategories)
        val txtDate = findViewById<TextView>(R.id.txtDate)
        val btnSave = findViewById<Button>(R.id.btnSaveTransaction)

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

        var selectedType = "مصروف"
        spinnerType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: android.view.View?, pos: Int, id: Long) {
                selectedType = if (pos == 0) "مصروف" else "وارد"
                flowCategories.visibility = if (selectedType == "مصروف") android.view.View.VISIBLE else android.view.View.GONE
            }
            override fun onNothingSelected(p: AdapterView<*>?) {}
        }

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
