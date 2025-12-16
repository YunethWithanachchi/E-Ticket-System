package com.example.busetiko.controller

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.example.busetiko.DBHelper
import com.example.busetiko.R
import com.example.busetiko.SessionManager


class WalletController :Activity() {
    private lateinit var db: DBHelper
    private lateinit var balanceTxt: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.wallet)

        balanceTxt = findViewById(R.id.balanceTxt)
        db = DBHelper(this)

        val userId = SessionManager.userId
        if (userId==null){
            //user not logged in
            finish()
            return
        }
        val balance = db.getUserWalletBalance("U001")
        balanceTxt.text = balance.toString()

        findViewById<Button>(R.id.rechargeBtn).setOnClickListener{
            paymentGateway()
        }
    }
    private fun paymentGateway(){
        AlertDialog.Builder(this)
            .setTitle("Sorry ${SessionManager.userName} :(")
            .setMessage("We haven't integrated the payment gateway yet.\n We will make it available to you soon!")
            .setPositiveButton("Okay"){_,_->finish()}
            .show()

    }

}