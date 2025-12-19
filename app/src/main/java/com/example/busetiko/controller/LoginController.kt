package com.example.busetiko.controller

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.busetiko.DBHelper
import com.example.busetiko.MainActivity
import com.example.busetiko.R
import com.example.busetiko.SessionManager


class LoginController : AppCompatActivity() {

    private lateinit var UserName: EditText
    private lateinit var Password: EditText
    private lateinit var dbHelper: DBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        dbHelper = DBHelper(this)

        UserName = findViewById(R.id.editTextLoginUsername)
        Password = findViewById(R.id.editTextLoginPassword)
    }

    fun newUser(view: View) {
        startActivity(Intent(this, RegisterController::class.java))
    }

    fun onLoginClick(view: View) {
        val userName = UserName.text.toString().trim()
        val password = Password.text.toString().trim()

        if (userName.isEmpty() || password.isEmpty()) {
            toast("Please enter username and password")
            return
        }

        val user = dbHelper.getUserByUserNameAndPassword(userName, password)

        if (user != null) {
            // ---- SAVE TO SESSION ----
            SessionManager.userId = user.first
            SessionManager.userName = user.second
            SessionManager.email = user.third

            toast("Login successful")

            // ---- GO TO MAIN ----
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            toast("Invalid email or password")
        }
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
