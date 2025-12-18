package com.example.busetiko

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.example.busetiko.controller.HistoryController
import com.example.busetiko.controller.ProfileController
import com.example.busetiko.controller.TicketController
import com.example.busetiko.controller.WalletController
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // we tell by findViewById to look for a type "Button" having the particular id. Android has all ids saved in R.id
        val ticketBtn = findViewById<Button>(R.id.ticketBtn)
        val historyBtn = findViewById<Button>(R.id.historyBtn)
        val walletBtn = findViewById<Button>(R.id.walletBtn)
        val profileBtn = findViewById<Button>(R.id.profileBtn)
        val scanBtn = findViewById<FloatingActionButton>(R.id.ScanButton)

        ticketBtn.setOnClickListener {
            val ticketBundle = DBHelper(this).lookActiveTicket(SessionManager.userId)
            if (ticketBundle==null) {
                Toast.makeText(this, "No active ticket. Please scan QR.", Toast.LENGTH_SHORT).show()
            }else{
                activeTicketAvailable(ticketBundle)
            }
        }

        historyBtn.setOnClickListener {
            val intent = Intent(this,HistoryController::class.java)
            startActivity(intent)
        }

        walletBtn.setOnClickListener {
            val intent = Intent(this, WalletController::class.java)
            startActivity(intent)        }

        profileBtn.setOnClickListener {
            val intent = Intent(this,ProfileController::class.java)
            startActivity(intent)
        }

        scanBtn.setOnClickListener {
            val intent = Intent(this,ScanQrActivity::class.java)
            startActivity(intent)
        }
    }

    private fun activeTicketAvailable(ticketBundle: Bundle) {
        val intent = Intent(this,TicketController::class.java)
        intent.putExtra("ticketDetails",ticketBundle)
        intent.putExtra("isScanned",false)
        startActivity(intent)
    }

}