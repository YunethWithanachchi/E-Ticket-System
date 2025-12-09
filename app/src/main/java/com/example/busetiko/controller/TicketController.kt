package com.example.busetiko.controller

import android.app.Activity
import android.os.Bundle
import com.example.busetiko.R

class TicketController : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ticket)

        val busId = "B001"
        val busNo = "NA - 2323"
        val busRoute = "100"
    }

}