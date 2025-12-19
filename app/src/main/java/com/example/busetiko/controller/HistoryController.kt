package com.example.busetiko.controller

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.busetiko.DBHelper
import com.example.busetiko.HistoryAdapter
import com.example.busetiko.R
import com.example.busetiko.SessionManager
import com.example.busetiko.TicketHistory

class HistoryController: AppCompatActivity() {
    private lateinit var rvHistory: RecyclerView
    private lateinit var historyAdapter: HistoryAdapter
    private lateinit var db: DBHelper
    private lateinit var ticketList: MutableList<TicketHistory>
    private lateinit var txtEmptyState:TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.history)

        db = DBHelper(this)
        ticketList = db.getTicketHistory(SessionManager.userId).toMutableList()
        Log.d("HISTORY", "Tickets found: ${ticketList.size}")

        rvHistory = findViewById(R.id.rvHistory)
        txtEmptyState = findViewById(R.id.txtEmptyState)
        rvHistory.layoutManager = LinearLayoutManager(this)

        historyAdapter = HistoryAdapter(ticketList){ticket,position->
            AlertDialog.Builder(this)
                .setTitle("Delete Ticket")
                .setMessage("Are you sure you want to delete this ticket?")
                .setPositiveButton("Yes") { dialog, _ ->
                    // Delete from database
                    db.deleteTicket(ticket.ticketNo)

                    // Remove from adapter list
                    ticketList.removeAt(position)
                    historyAdapter.notifyItemRemoved(position)

                    Toast.makeText(this, "Ticket deleted", Toast.LENGTH_SHORT).show()
                    checkEmptyState()
                    dialog.dismiss()
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

        rvHistory.adapter = historyAdapter

        // Initial empty state check
        checkEmptyState()
    }
    private fun checkEmptyState() {
        if (ticketList.isEmpty()) {
            rvHistory.visibility = RecyclerView.GONE
            txtEmptyState.visibility = TextView.VISIBLE
        } else {
            rvHistory.visibility = RecyclerView.VISIBLE
            txtEmptyState.visibility = TextView.GONE
        }
    }
}