package com.example.busetiko.controller

import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import com.example.busetiko.DBHelper
import com.example.busetiko.R
import com.example.busetiko.SessionManager


class WalletController :Activity() {
    private var db = DBHelper(this)
    private var balance = db.getUserWalletBalance(SessionManager.userId.toString())
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.wallet)
        findViewById<TextView>(R.id.balanceTxt).text = balance.toString()
    }

}