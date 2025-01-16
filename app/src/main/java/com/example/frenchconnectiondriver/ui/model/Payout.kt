package com.example.frenchconnectiondriver.ui.model

data class Payout(
    val amount :Int = 0,
    val completionTimeStamp :Long = 0,
    val driverId :String = "",
    val orderId :String = "",
    val status :String = "",
    val type :String = "",
    val id :String = ""
)
