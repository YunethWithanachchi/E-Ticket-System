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
    }


    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_ENTRIES)
        runInsertScript(db,"insert_bus.sql")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(SQL_DELETE_ENTRIES)
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