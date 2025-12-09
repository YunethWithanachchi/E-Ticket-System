package com.example.busetiko.controller

import android.app.Activity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.example.busetiko.DBHelper
import com.example.busetiko.R

class TicketController : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ticket)

        val busId = "B001"
        val busNo = "NA - 2323"
        val busRoute = 101

        val db = DBHelper(this)
        val dropPoints = db.getDropPointsByRoute(busRoute)

        val fromSpinner = findViewById<Spinner>(R.id.fromSpinner)
        val toSpinner = findViewById<Spinner>(R.id.toSpinner)

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            dropPoints
        )

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        fromSpinner.adapter = adapter
        toSpinner.adapter = adapter
    }

}