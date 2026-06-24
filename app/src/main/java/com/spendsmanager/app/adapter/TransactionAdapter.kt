package com.spendsmanager.app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.spendsmanager.app.R
import com.spendsmanager.app.data.Transaction

class TransactionAdapter(
    private val transactions: List<Transaction>,
    private val onDelete: (Transaction) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val card: CardView = view.findViewById(R.id.cardTransaction)
        val icon: TextView = view.findViewById(R.id.txtTxnIcon)
        val category: TextView = view.findViewById(R.id.txtCategory)
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
        h.icon.text = if (isExpense) "↑" else "↓"
        h.category.text = t.category
        h.description.text = t.description
        h.date.text = t.date.replace("-", "/")
        h.amount.text = "${if (isExpense) "-" else "+"}${String.format("%.2f", t.amount)}"
        val color = ContextCompat.getColor(h.itemView.context, if (isExpense) R.color.expense else R.color.income)
        h.amount.setTextColor(color)
        h.card.setOnLongClickListener {
            onDelete(t)
            true
        }
    }
}
