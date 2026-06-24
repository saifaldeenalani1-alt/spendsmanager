package com.spendsmanager.app.data

data class Account(
    var id: Long = 0,
    var name: String = "",
    var type: String = "شخصي",
    var initialBalance: Double = 0.0,
    var icon: String = "📁",
    var createdAt: String = ""
)
