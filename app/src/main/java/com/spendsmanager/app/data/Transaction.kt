package com.spendsmanager.app.data

data class Transaction(
    var id: Long = 0,
    var accountId: Long = 0,
    var type: String = "مصروف",
    var amount: Double = 0.0,
    var description: String = "",
    var date: String = ""
)
