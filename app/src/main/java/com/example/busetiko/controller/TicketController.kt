package com.example.busetiko.controller

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import com.example.busetiko.DBHelper
import com.example.busetiko.R
import com.example.busetiko.SessionManager

class TicketController : Activity() {

    private lateinit var fromSpinner: Spinner
    private lateinit var toSpinner: Spinner
    private lateinit var ticketCount: Spinner
    private lateinit var paybtn:Button
    private lateinit var fareTxt:TextView
    private lateinit var db: DBHelper

    private lateinit var busId:String
    private lateinit var busNo:String
    private lateinit var route:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ticket)
        paybtn = findViewById<Button>(R.id.payBtn)
        fareTxt = findViewById<TextView>(R.id.amountTxt)

        busId = intent.getStringExtra("BUS_ID")?:""
        busNo = intent.getStringExtra("BUS_NO")?:""
        route = (intent.getStringExtra("ROUTE")?:"")
        findViewById<TextView>(R.id.busIdTxt).setText(busId)
        findViewById<TextView>(R.id.busNoTxt).setText(busNo)
        findViewById<TextView>(R.id.routeTxt).setText(route)


        db = DBHelper(this)
        val routeNo = route.toInt()
        val dropPoints = db.getDropPointsByRoute(routeNo)

        val numbers = (1..10).toList()

        fromSpinner = findViewById(R.id.fromSpinner)
        toSpinner = findViewById(R.id.toSpinner)
        ticketCount = findViewById(R.id.ticketSpinner)

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            dropPoints
        )
        val adapter2 = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            numbers
        )

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        fromSpinner.adapter = adapter
        toSpinner.adapter = adapter
        ticketCount.adapter = adapter2

        //Setting Listeners to the dropdowns

        fromSpinner.onItemSelectedListener = object :AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                showFare()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        toSpinner.onItemSelectedListener = object :AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                showFare()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        ticketCount.onItemSelectedListener =object :AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                showFare()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        paybtn.setOnClickListener{
            val amount = fareTxt.text.toString().toDouble()?:0.0
            if (amount==0.0){
                Toast.makeText(this,"Invalid Destinations",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            AlertDialog.Builder(this)
                .setTitle("Confirm Payment")
                .setMessage("Do you want to pay LKR $amount?")
                .setPositiveButton("Confirm"){_,_->processPayment(amount)}
                .setNegativeButton("Cancel",null)
                .show()
        }
    }
    fun showFare(){
        val from = fromSpinner.selectedItem?.toString()?:return
        val to = toSpinner.selectedItem?.toString()?:return
        val count = ticketCount.selectedItem.toString().toIntOrNull()

        val fare = (count?.let { calculateFare(route.toInt(),from,to)?.times(it) })
        fareTxt.text = String.format("%.2f",fare)
    }
    fun calculateFare(routeNo: Int, from: String, to: String): Double? {
        val fromSectionNo = db.getSectionNo(routeNo,from)
        val toSectionNo = db.getSectionNo(routeNo,to)

        if (fromSectionNo!=null && toSectionNo!=null) {
            val sectionDifference = kotlin.math.abs(toSectionNo-fromSectionNo)
            return db.getFareForSections(sectionDifference)
        }
        return null
    }

    private fun processPayment(amount:Double){
        val balance = db.getUserWalletBalance(SessionManager.userId.toString())
        if (amount<=balance){
            //enough money
            val newBalance = balance-amount
            db.updateUserWallet(SessionManager.userId.toString(),newBalance)
            showSuccessDialog(newBalance);
        }else{
            //Insufficient Balance
            showInsufficientBalanceDialog(balance)
        }
    }

    private fun showInsufficientBalanceDialog(balance: Double) {
        AlertDialog.Builder(this)
            .setTitle("Insufficient Balance")
            .setMessage("Your balance is LKR $balance.\nPlease recharge your wallet.")
            .setPositiveButton("Wallet"){_,_->
                val intent=Intent(this,WalletController::class.java)
                startActivity(intent)
            }
            .setNegativeButton("Cancel",null)
            .show()
    }

    private fun showSuccessDialog(newBalance: Double) {
        AlertDialog.Builder(this)
            .setTitle("Payment Successful")
            .setMessage("Ticket booked successfully.\nRemaining balance : LKR $newBalance")
            .setPositiveButton("OK"){_,_->finish()}
            .show()
    }


}