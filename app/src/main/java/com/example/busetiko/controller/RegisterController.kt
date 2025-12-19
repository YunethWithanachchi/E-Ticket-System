package com.example.busetiko.controller

import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.busetiko.DBHelper
import com.example.busetiko.R
import com.example.busetiko.SessionManager


class RegisterController : AppCompatActivity() {

    private lateinit var etFullName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var tvLoginLink: TextView
    private lateinit var dbHelper: DBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register)

        dbHelper = DBHelper(this)

        etFullName = findViewById(R.id.etFullName)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnRegister = findViewById(R.id.btnRegister)
        tvLoginLink = findViewById(R.id.tvLoginLink)

        btnRegister.setOnClickListener { registerUser() }
        tvLoginLink.setOnClickListener { finish() }
    }

    private fun registerUser() {
        val username = etFullName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        // ---------- VALIDATION ----------
        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            toast("Please fill all fields")
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            toast("Enter a valid email address")
            return
        }

        if (password.length < 6) {
            toast("Password must be at least 6 characters")
            return
        }

        // ---------- USER ID GENERATION ----------
        val userId = "U" + System.currentTimeMillis().toString().takeLast(4)

        // ---------- INSERT INTO DATABASE ----------
        val success = dbHelper.insertUser(
            userId = userId,
            username = username,
            email = email,
            password = password,
            toursTravelled = 0,
            ticketsBought = 0,
            wallet = 1000.0
        )

        if (!success) {
            toast("Email already exists")
            return
        }

        toast("Registration successful!")
        finish() // back to login OR navigate to MainActivity if you want
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
