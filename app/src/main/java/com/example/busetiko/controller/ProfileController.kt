package com.example.busetiko.controller

import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import com.example.busetiko.DBHelper
import com.example.busetiko.R
import com.example.busetiko.SessionManager

class ProfileController : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile)

        val db =DBHelper(this)

        val idtxt = findViewById<TextView>(R.id.idTxt)
        val nametxt = findViewById<TextView>(R.id.nameTxt)
        val triptxt = findViewById<TextView>(R.id.triptxt2)
        val tickettxt = findViewById<TextView>(R.id.tickettxt2)

        idtxt.text = SessionManager.userId
        nametxt.text = SessionManager.userName
        triptxt.text = db.getToursTravelled(SessionManager.userId).toString()
        tickettxt.text = db.getTicketBought(SessionManager.userId).toString()
    }
}