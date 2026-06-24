package com.spendsmanager.app.adapter

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.spendsmanager.app.R
import com.spendsmanager.app.data.Transaction

class TransactionAdapter(
    private val transactions: List<Transaction>,
    private val onDelete: (Transaction) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val typeIndicator: LinearLayout = view.findViewById(R.id.typeIndicator)
        val icon: TextView = view.findViewById(R.id.txtTxnIcon)
        val description: TextView = view.findViewById(R.id.txtDescription)
        val date: TextView = view.findViewById(R.id.txtDate)
        val amount: TextView = view.findViewById(R.id.txtAmount)
    }

    override fun onCreateViewHolder(p: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(p.context).inflate(R.layout.item_transaction, p, false))
    }

    override fun getItemCount() = transactions.size

    override fun onBindViewHolder(h: ViewHolder, pos: Int) {
        val t = transactions[pos]
        val isExpense = t.type == "مصروف"
        val context = h.itemView.context
        val incomeColor = ContextCompat.getColor(context, R.color.income)
        val expenseColor = ContextCompat.getColor(context, R.color.expense)
        val incomeLight = ContextCompat.getColor(context, R.color.incomeLight)
        val expenseLight = ContextCompat.getColor(context, R.color.expenseLight)
        val color = if (isExpense) expenseColor else incomeColor
        val lightColor = if (isExpense) expenseLight else incomeLight

        h.typeIndicator.setBackgroundColor(color)

        h.icon.text = if (isExpense) "🔻" else "🔺"
        h.icon.setBackgroundColor(lightColor)

        h.description.text = t.description.ifEmpty { if (isExpense) "مصروف" else "وارد" }
        h.date.text = t.date.replace("-", "/")
        h.amount.text = "${if (isExpense) "−" else "+"}${String.format("%.0f", t.amount)}"
        h.amount.setTextColor(color)

        h.itemView.setOnLongClickListener {
            onDelete(t)
            true
        }
    }
}