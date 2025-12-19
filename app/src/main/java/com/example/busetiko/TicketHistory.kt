package com.example.busetiko

data class TicketHistory (
    val ticketNo: String,
    val from: String,
    val to: String,
    val route: Int,
    val date: String,
    val price: Double
)