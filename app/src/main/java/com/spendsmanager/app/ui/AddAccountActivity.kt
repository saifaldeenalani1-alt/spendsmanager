package com.spendsmanager.app.ui

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.spendsmanager.app.R
import com.spendsmanager.app.data.Account
import com.spendsmanager.app.data.DatabaseHelper

class AddAccountActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_account)
        supportActionBar?.title = "حساب جديد"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val db = DatabaseHelper(this)
        val edtName = findViewById<EditText>(R.id.edtAccountName)
        val edtBalance = findViewById<EditText>(R.id.edtInitialBalance)
        val spinnerType = findViewById<Spinner>(R.id.spinnerType)

        spinnerType.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listOf("شخصي", "مشروع"))

        findViewById<Button>(R.id.btnSaveAccount).setOnClickListener {
            val name = edtName.text.toString().trim()
            if (name.isEmpty()) {
                edtName.error = "الرجاء إدخال اسم"
                return@setOnClickListener
            }
            val balance = edtBalance.text.toString().toDoubleOrNull() ?: 0.0
            db.insertAccount(Account(
                name = name,
                type = spinnerType.selectedItem.toString(),
                initialBalance = balance
            ))
            Toast.makeText(this, "تم حفظ الحساب", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
