package com.example.busetiko.controller

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.busetiko.DBHelper
import com.example.busetiko.R
import com.example.busetiko.SessionManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TicketController : Activity() {

    private lateinit var fromSpinner: Spinner
    private lateinit var toSpinner: Spinner
    private lateinit var ticketCount: Spinner
    private lateinit var paybtn:Button
    private lateinit var busIdUI:TextView
    private lateinit var busNoUI:TextView
    private lateinit var routeUI:TextView
    private lateinit var fareTxt:TextView
    private lateinit var db: DBHelper

    private lateinit var busId:String
    private lateinit var busNo:String
    private lateinit var route:String
    private lateinit var to:String
    private lateinit var from:String
    private lateinit var count:String

    private var isScanned = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ticket)
        paybtn  = findViewById(R.id.payBtn)
        fareTxt = findViewById(R.id.amountTxt)
        busIdUI = findViewById(R.id.busIdTxt)
        busNoUI = findViewById(R.id.busNoTxt)
        routeUI = findViewById(R.id.routeTxt)
        fromSpinner = findViewById(R.id.fromSpinner)
        toSpinner = findViewById(R.id.toSpinner)
        ticketCount = findViewById(R.id.ticketSpinner)
        db = DBHelper(this)

        isScanned = intent.getBooleanExtra("isScanned",false)

        if (isScanned){newTicket()}else{loadTicket()}
    }
    private fun newTicket(){

        busId = intent.getStringExtra("BUS_ID")?:""
        busNo = intent.getStringExtra("BUS_NO")?:""
        route = (intent.getStringExtra("ROUTE")?:"")
        busIdUI.text = busId
        busNoUI.text = busNo
        routeUI.text = route

        val routeNo = route.toInt()
        val dropPoints = db.getDropPointsByRoute(routeNo)

        val numbers = (1..10).toList()

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
            val amount = fareTxt.text.toString().toDoubleOrNull()?:0.0
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
    private fun loadTicket(){
        val extras= intent.extras //intent.extras refer to Bundle
        val ticketDetails = extras?.getBundle("ticketDetails")
        //Extras are used when getting data that has been passed to the UI from another
        //the thing we have done here is we have taken the bundle(Key-Value Container)
        //that was passed from the previous UI and retrieved its contents below
        if (ticketDetails!=null){
            busId =ticketDetails.getString("BUS_ID")?:""
            busNo =ticketDetails.getString("BUS_NO")?:""
            route =ticketDetails.getString("ROUTE_NO")?:""
            count = ticketDetails.getString("TICKET_COUNT")?:""
            fareTxt.text = String.format("%.2f", ticketDetails.getDouble("FARE"))

            busIdUI.text = busId
            busNoUI.text = busNo
            routeUI.text = route

            fromSpinner.adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                listOf(ticketDetails.getString("FROM_STOP") ?: "")
            ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

            toSpinner.adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                listOf(ticketDetails.getString("TO_STOP") ?: "")
            ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

            ticketCount.adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                listOf(ticketDetails.getString("TICKET_COUNT") ?: "")
            ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

            fromSpinner.isEnabled = false
            toSpinner.isEnabled = false
            ticketCount.isEnabled = false

            paybtn.text = "Paid"
            paybtn.isEnabled = false
            //paybtn.background = ContextCompat.getDrawable(this, R.drawable.paid_button_bg)
            paybtn.setTextColor(Color.GREEN)

        }


    }
    fun showFare(){
        if (paybtn.text == "Paid") return
        from = fromSpinner.selectedItem?.toString() ?: return
        to = toSpinner.selectedItem?.toString() ?: return
        count = ticketCount.selectedItem?.toString() ?: "1"

        val fareValue = calculateFare(route.toInt(), from, to) ?: 0.0
        val totalFare = fareValue * count.toInt()
        fareTxt.text = String.format("%.2f", totalFare)
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
        val balance = db.getUserWalletBalance(SessionManager.userId)
        if (amount<=balance){
            //enough money
            val newBalance = balance-amount

            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val date = formatter.format(Date())

            val result = db.addTicket(SessionManager.userId,busId,busNo,date,to,from,route.toInt(),count,amount)
            if (result){
                db.updateUserWallet(SessionManager.userId,newBalance)
                db.updateUserStat(count.toInt(),SessionManager.userId)
                showSuccessDialog(newBalance);
            }
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

