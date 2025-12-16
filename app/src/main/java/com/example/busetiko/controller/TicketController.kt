package com.example.busetiko.controller

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import com.example.busetiko.DBHelper
import com.example.busetiko.R

class TicketController : Activity() {

    private lateinit var fromSpinner: Spinner
    private lateinit var toSpinner: Spinner
    private lateinit var ticketCount: Spinner
    private lateinit var db: DBHelper

    private lateinit var busId:String
    private lateinit var busNo:String
    private lateinit var route:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ticket)

        busId = intent.getStringExtra("BUS_ID")?:""
        busNo = intent.getStringExtra("BUS_NO")?:""
        route = intent.getStringExtra("ROUTE")?:""
        findViewById<TextView>(R.id.busIdTxt).setText(busId)
        findViewById<TextView>(R.id.busNoTxt).setText(busNo)
        findViewById<TextView>(R.id.routeTxt).setText(route)


        db = DBHelper(this)
        val dropPoints = db.getDropPointsByRoute(route.toInt())

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
    }
    fun showFare(){
        val from = fromSpinner.selectedItem?.toString()?:return
        val to = toSpinner.selectedItem?.toString()?:return
        val count = ticketCount.selectedItem.toString().toIntOrNull()

        val fare = (count?.let { calculateFare(route.toInt(),from,to)?.times(it) })
        val fareTxt = findViewById<TextView>(R.id.amountTxt)
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


}