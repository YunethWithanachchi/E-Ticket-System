package com.example.busetiko

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Bundle
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.concurrent.thread

class DBHelper
    (private val context: Context) : SQLiteOpenHelper(context,"Bus_Ticket",null,1) {
    companion object {
        const val TABLE_NAME = "Bus"
        const val SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TABLE_NAME + "(ID TEXT, " + "NO TEXT, ROUTE INTEGER, DROP_POINTS TEXT)"
        const val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + TABLE_NAME

        const val FARE_TABLE = "fare_sections"
        const val SQL_CREATE_FARE_TABLE =
            "CREATE TABLE " + FARE_TABLE + "(SECTION_COUNT INTEGER, FARE DOUBLE)"
        const val SQL_DELETE_FARE_TABLE = "DROP TABLE IF EXISTS " + FARE_TABLE

        const val ROUTE_TABLE = "ROUTE_TABLE"
        const val SQL_CREATE_ROUTE_TABLE_101 =
            "CREATE TABLE " + ROUTE_TABLE + "(ROUTE_NO INTEGER,LOCATION TEXT, SECTION INTEGER)"
        const val SQL_DELETE_ROUTE_TABLE_101 = "DROP TABLE IF EXISTS " + ROUTE_TABLE

        const  val USER_TABLE = "USERS"
        const val SQL_CREATE_USER_TABLE = "CREATE TABLE "+ USER_TABLE+"(USER_ID TEXT PRIMARY KEY, USERNAME TEXT, EMAIL TEXT," +
                "TOURS_TRAVELLED INTEGER,TICKET_BOUGHT INTEGER,WALLET DOUBLE)"
        const val SQL_DELETE_USER_TABLE = "DROP TABLE IF EXISTS "+ USER_TABLE

        const val TICKET_TABLE  = "TICKET"
        const val SQL_CREATE_TICKET_TABLE = "CREATE TABLE "+ TICKET_TABLE+"(TICKET_ID INTEGER PRIMARY KEY AUTOINCREMENT,USER_ID TEXT,BUS_ID TEXT,BUS_NO TEXT," +
                " DATE_TIME TEXT, WHERE_TO TEXT, WHERE_FROM TEXT,ROUTE_NO INTEGER,TICKET_COUNT INTEGER, PRICE DOUBLE,FOREIGN KEY (USER_ID) REFERENCES USERS(USER_ID)," +
                "FOREIGN KEY (BUS_ID) REFERENCES BUS(ID)\n)"
        const val SQL_DELETE_TICKET_TABLE = "DROP TABLE IF EXISTS "+ TICKET_TABLE

    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_ENTRIES)
        db.execSQL(SQL_CREATE_FARE_TABLE)
        db.execSQL(SQL_CREATE_ROUTE_TABLE_101)
        db.execSQL(SQL_CREATE_USER_TABLE)
        db.execSQL(SQL_CREATE_TICKET_TABLE)
        runInsertScript(db, "insert_bus.sql")
        runInsertScript(db, "fareVsSections.sql")
        runInsertScript(db, "routes.sql")
        runInsertScript(db, "tempFile.sql")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(SQL_DELETE_ENTRIES)
        db.execSQL(SQL_DELETE_FARE_TABLE)
        db.execSQL(SQL_DELETE_ROUTE_TABLE_101)
        db.execSQL(SQL_DELETE_USER_TABLE)
        db.execSQL(SQL_DELETE_TICKET_TABLE)
        onCreate(db)
    }

    override fun onConfigure(db: SQLiteDatabase?) {
        super.onConfigure(db)
        if (db != null) {
            db.setForeignKeyConstraintsEnabled(true)
        }
    }

    private fun runInsertScript(db: SQLiteDatabase, fileName: String) {
        db.beginTransaction()
        try {
            val inputStream = context.applicationContext.assets.open(fileName)
            val reader = BufferedReader(InputStreamReader(inputStream))

            var line: String?
            while (reader.readLine().also { line = it } != null) {
                val query = line!!.trim()
                if (query.isNotEmpty()) {
                    try {
                        db.execSQL(query)
                    } catch (e: Exception) {
                        Log.e("DBHelper", "Failed SQL : $query", e)
                    }
                }
            }
            db.setTransactionSuccessful()
            reader.close()
            inputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.endTransaction()
        }
    }

    fun getDropPointsByRoute(route: Int): List<String> {
        val dropPointsList = mutableListOf<String>()
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT DROP_POINTS FROM BUS WHERE ROUTE = ?", arrayOf(route.toString())
        )
        if (cursor.moveToFirst()) {
            val dropPoints = cursor.getString(0)
            dropPointsList.addAll(
                dropPoints.split(",").map { it.trim() }
            )
        }
        cursor.close()
        return dropPointsList
    }

    fun getSectionNo(routeNo: Int, location: String): Int? {
        val db = readableDatabase
        var section: Int? = null
        val cursor = db.rawQuery(
            "SELECT SECTION FROM ROUTE_TABLE WHERE ROUTE_NO = ? AND LOCATION = ?",
            arrayOf(routeNo.toString(), location)
        )
        if (cursor.moveToFirst()) {
            section = cursor.getInt(0)
        }
        cursor.close()
        return section
    }

    fun getFareForSections(sectionDifference: Int): Double? {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT FARE FROM fare_sections WHERE SECTION_COUNT = ?",
            arrayOf(sectionDifference.toString())
        )
        val fare = if (cursor.moveToFirst()) cursor.getDouble(0) else 0.0
        cursor.close()
        return fare
    }

    fun getUserWalletBalance(userId:String):Double{
        var balance = 0.0
        val db = readableDatabase
        Log.d("DB_CHECK", "Fetching wallet for userId: $userId")

        val cursor = db.rawQuery(
            "SELECT WALLET FROM USERS WHERE USER_ID = ?",
            arrayOf(userId)
        )
        Log.d("DB_CHECK", "Rows found: ${cursor.count}")

        if (cursor.moveToFirst()){balance=cursor.getDouble(0)}
        cursor.close()
        return balance
    }

    fun updateUserWallet(userId: String,newBalance:Double){
        val db = writableDatabase

        val values = ContentValues().apply {
            put("WALLET",newBalance)
        }
        db.update("USERS",values,"USER_ID = ?", arrayOf(userId))
    }

    fun addTicket(userId: String,busId: String, busNo: String, date: String, to: String, from: String,
                  route: Int, count: String, amount: Double): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("USER_ID",userId)
            put("BUS_ID",busId)
            put("BUS_NO",busNo)
            put("DATE_TIME",date)
            put("WHERE_TO",to)
            put("WHERE_FROM",from)
            put("ROUTE_NO",route)
            put("TICKET_COUNT",count)
            put("PRICE",amount)
        }
        val result = db.insert(TICKET_TABLE,null,values) // result get the type as a long
        return result!=-1L //an error gives -1 long, so we say to return true if its not -1L
    }

    fun lookActiveTicket(userId: String): Bundle? {
        val db= readableDatabase
        val cursor = db.rawQuery("""SELECT * FROM TICKET WHERE USER_ID = ? 
                ORDER BY DATE_TIME DESC 
                LIMIT 1""".trimIndent(),
                arrayOf(userId)
        ) // """ allows multiline o/w if we use " we have to use ""+"" when we go multiline

        var activeTicketDetails: Bundle?=null
        if (cursor.moveToFirst()){
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val dateString =cursor.getString(cursor.getColumnIndexOrThrow("DATE_TIME"))
            val ticketTimeMillis = formatter.parse(dateString)?.time ?: 0L
            val currentTime = System.currentTimeMillis()

            if (currentTime - ticketTimeMillis <= 60 * 60 * 1000) {
                activeTicketDetails = Bundle().apply {
                    putString("TICKET_ID",
                        cursor.getString(cursor.getColumnIndexOrThrow("TICKET_ID")))
                    putString("BUS_ID",
                        cursor.getString(cursor.getColumnIndexOrThrow("BUS_ID")))
                    putString("BUS_NO",
                        cursor.getString(cursor.getColumnIndexOrThrow("BUS_NO")))
                    putString("FROM_STOP",
                        cursor.getString(cursor.getColumnIndexOrThrow("FROM_STOP")))
                    putString("TO_STOP",
                        cursor.getString(cursor.getColumnIndexOrThrow("TO_STOP")))
                    putDouble("FARE",
                        cursor.getDouble(cursor.getColumnIndexOrThrow("PRICE")))
                    putString("ROUTE_NO",
                        cursor.getString(cursor.getColumnIndexOrThrow("ROUTE_NO")))
                    putString("TICKET_COUNT",
                        cursor.getString(cursor.getColumnIndexOrThrow("TICKET_COUNT")))
                    putString("TICKET_TIME", dateString)
                }
            }
        }
        cursor.close()
        return activeTicketDetails
    }
}
