package com.barjek.barcode

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.barjek.barcode.activity.HomePageActivity
import com.barjek.barcode.activity.LoginActivity
import com.barjek.barcode.activity.NoInsideActivity
import com.barjek.barcode.activity.StartActivity
import com.barjek.barcode.database.DatabaseHelper
import com.barjek.barcode.databinding.ActivitySplashScreenBinding
import com.barjek.barcode.model.User
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@SuppressLint("CustomSplashScreen")
class SplashScreen : AppCompatActivity() {
    private lateinit var binding: ActivitySplashScreenBinding
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        dbHelper = DatabaseHelper(this)
        supportActionBar?.hide()

        super.onCreate(savedInstanceState)
        setContentView(binding.root)

//        val sharedPreferences = getSharedPreferences("MyAppPreferences", MODE_PRIVATE)
//        val isFirstRun = sharedPreferences.getBoolean("isFirstRun", true)
//
//        val login = getSharedPreferences("UserPref", MODE_PRIVATE)
//        val email = login.getString("EMAIL", "") ?: ""
//
//        val user = dbHelper.getUserByEmail(email) ?: User()
//
//        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
//
//        val intent = when(isFirstRun) {
//            true -> {
//                sharedPreferences.edit().putBoolean("isFirstRun", false).apply()
//                Intent(this, StartActivity::class.java)
//            }
//            else -> {
//                if (user.id.isBlank()) {
//                    Intent(this, LoginActivity::class.java)
//                } else {
//                    if (dbHelper.checkTodayPresence(user.id, today)) Intent(this, HomePageActivity::class.java)
//                    else Intent(this, NoInsideActivity::class.java)
//                }
//            }
//        }
//        Handler(Looper.getMainLooper()).postDelayed({
//            startActivity(intent)
//            finish()
//        }, 3000L)
    }
}