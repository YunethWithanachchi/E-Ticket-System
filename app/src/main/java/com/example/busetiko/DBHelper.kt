package com.example.busetiko

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.io.BufferedReader
import java.io.InputStreamReader

class DBHelper
    (private val context: Context) : SQLiteOpenHelper(context,"Bus_Ticket",null,1)
{
    companion object{
        const val TABLE_NAME = "Bus"
        const val SQL_CREATE_ENTRIES = "CREATE TABLE " + TABLE_NAME + "(ID TEXT, " + "NO TEXT, ROUTE INT, DROP_POINTS TEXT)"
        const val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + TABLE_NAME

        const val FARE_TABLE = "Fare"
        const val SQL_CREATE_FARE_TABLE = "CREATE TABLE " + FARE_TABLE+"(SECTION_COUNT INT, FARE DOUBLE)"
        const val SQL_DELETE_FARE_TABLE = "DROP TABLE IF EXISTS "+ FARE_TABLE

        const val ROUTE_TABLE = "ROUTE_101"
        const val SQL_CREATE_ROUTE_TABLE_101 = "CREATE TABLE " + ROUTE_TABLE+"(ROUTE_NO INT,LOCATION TEXT, SECTION INT)"
        const val SQL_DELETE_ROUTE_TABLE_101 = "DROP TABLE IF EXISTS "+ ROUTE_TABLE
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_ENTRIES)
        db.execSQL(SQL_CREATE_FARE_TABLE)
        db.execSQL(SQL_CREATE_ROUTE_TABLE_101)
        runInsertScript(db,"insert_bus.sql")
        runInsertScript(db,"fareVsSections.sql")
        runInsertScript(db,"routes.sql")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(SQL_DELETE_ENTRIES)
        db.execSQL(SQL_DELETE_FARE_TABLE)
        db.execSQL(SQL_DELETE_ROUTE_TABLE_101)
        onCreate(db)
    }

    private fun runInsertScript(db: SQLiteDatabase,fileName : String){
        try{
            val inputStream = context.applicationContext.assets.open(fileName)
            val reader = BufferedReader(InputStreamReader(inputStream))

            var line: String?
            while (reader.readLine().also { line = it }!= null){
                val query = line!!.trim()
                if (query.isNotEmpty()){
                    db.execSQL(query)
                }
            }
            reader.close()
            inputStream.close()
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    fun getDropPointsByRoute(route:Int):List<String>{
        val dropPointsList = mutableListOf<String>()
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT DROP_POINTS FROM BUS WHERE ROUTE = ?", arrayOf(route.toString())
        )
        if (cursor.moveToFirst()){
            val dropPoints = cursor.getString(0)
            dropPointsList.addAll(
                dropPoints.split(",").map { it.trim() }
            )
        }
        cursor.close()
        db.close()
        return dropPointsList
    }
}