package com.example.busetiko.controller

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.busetiko.DBHelper
import com.example.busetiko.HistoryAdapter
import com.example.busetiko.R
import com.example.busetiko.SessionManager

class HistoryController: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.history)

        //DB -> List -> RecyclerView ->adapter
        val db = DBHelper(this)
        val ticketList = db.getTicketHistory(SessionManager.userId)
        Log.d("HISTORY", "Tickets found: ${ticketList.size}")

        val rvHistory = findViewById<RecyclerView>(R.id.rvHistory)
        rvHistory.layoutManager = LinearLayoutManager(this)

        rvHistory.adapter = HistoryAdapter(ticketList)
    }
}