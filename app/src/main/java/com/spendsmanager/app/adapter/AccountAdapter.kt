package com.spendsmanager.app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.spendsmanager.app.R
import com.spendsmanager.app.data.Account

class AccountAdapter(
    private val accounts: List<Account>,
    private val balances: Map<Long, Double>,
    private val onClick: (Account) -> Unit,
    private val onLongClick: (Account) -> Unit
) : RecyclerView.Adapter<AccountAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val card: CardView = view.findViewById(R.id.cardAccount)
        val icon: TextView = view.findViewById(R.id.txtIcon)
        val name: TextView = view.findViewById(R.id.txtName)
        val type: TextView = view.findViewById(R.id.txtType)
        val balance: TextView = view.findViewById(R.id.txtBalance)
        val balanceLabel: TextView = view.findViewById(R.id.txtBalanceLabel)
    }

    override fun onCreateViewHolder(p: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(p.context).inflate(R.layout.item_account, p, false))
    }

    override fun getItemCount() = accounts.size

    override fun onBindViewHolder(h: ViewHolder, pos: Int) {
        val acc = accounts[pos]
        val balance = balances[acc.id] ?: 0.0
        h.icon.text = acc.icon
        h.name.text = acc.name
        h.type.text = if (acc.type == "شخصي") "حساب شخصي" else "مشروع"
        h.balance.text = String.format("%.2f د.ع", balance)
        val color = if (balance >= 0) ContextCompat.getColor(h.itemView.context, R.color.income) else ContextCompat.getColor(h.itemView.context, R.color.expense)
        h.balance.setTextColor(color)
        h.balanceLabel.text = if (balance >= 0) "متاح" else "مديون"
        h.card.setOnClickListener { onClick(acc) }
        h.card.setOnLongClickListener {
            onLongClick(acc)
            true
        }
    }
}
