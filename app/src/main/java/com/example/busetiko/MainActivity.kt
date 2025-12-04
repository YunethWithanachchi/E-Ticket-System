package com.example.busetiko

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
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
            val intent = Intent(this,TicketController::class.java)
            startActivity(intent)
        }

        historyBtn.setOnClickListener {
            Toast.makeText(this, "History Clicked", Toast.LENGTH_SHORT).show()
        }

        walletBtn.setOnClickListener {
            val intent = Intent(this,WalletController::class.java)
            startActivity(intent)        }

        profileBtn.setOnClickListener {
            Toast.makeText(this, "Profile Clicked", Toast.LENGTH_SHORT).show()
        }

        scanBtn.setOnClickListener {
            Toast.makeText(this, "Scan Clicked", Toast.LENGTH_SHORT).show()
        }
    }

}