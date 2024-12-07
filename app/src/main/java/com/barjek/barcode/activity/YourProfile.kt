package com.barjek.barcode.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.barjek.barcode.R
import com.barjek.barcode.database.DatabaseHelper

class YourProfile : AppCompatActivity() {
    private lateinit var dbHelper: DatabaseHelper
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_your_profile)
        
        dbHelper = DatabaseHelper(this)

        val tvName = findViewById<TextView>(R.id.tvName)
        val tvClass = findViewById<TextView>(R.id.tvClass)
        val tvNISN = findViewById<TextView>(R.id.tvNISN)
        val tvNIS = findViewById<TextView>(R.id.tvNIS)
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val btnLogout = findViewById<Button>(R.id.btnLogout)


        val sharedPref = getSharedPreferences("UserPref", Context.MODE_PRIVATE)
        val userEmail = sharedPref.getString("email", "") ?: ""

        val user = dbHelper.getUserByEmail(userEmail)

        user?.let {
            tvName.text = it.nama
            tvClass.text = "${it.kelas} ${it.jurusan}"
            tvNISN.text = it.nisn
            tvNIS.text = it.nis
        }
        
        // Handle button clicks
        btnBack.setOnClickListener {
            finish()
        }
        
        btnLogout.setOnClickListener {
            // Clear SharedPreferences
            sharedPref.edit().clear().apply()
            
            // Redirect ke Login
            startActivity(Intent(this, LoginActivity::class.java))
            finishAffinity()
        }
    }
}