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
import androidx.constraintlayout.motion.widget.MotionLayout
import com.barjek.barcode.activity.CameraActivity
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

    private var isActivityStarted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        dbHelper = DatabaseHelper(this)
        supportActionBar?.hide()

        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val sharedPreferences = getSharedPreferences("MyAppPreferences", MODE_PRIVATE)
        val isFirstRun = sharedPreferences.getBoolean("isFirstRun", true)

        val login = getSharedPreferences("UserPref", MODE_PRIVATE)
        val id = login.getString("ID_USER", "")

        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        val intent = when(isFirstRun) {
            true -> {
                sharedPreferences.edit().putBoolean("isFirstRun", false).apply()
                Intent(this, StartActivity::class.java)
            }
            else -> {
                if (id.isNullOrBlank()) {
                    Intent(this, LoginActivity::class.java)
                } else {
//                    if (dbHelper.checkTodayPresence(user.id, today))
                        Intent(this, HomePageActivity::class.java)
//                    else Intent(this, CameraActivity::class.java)
                }
            }
        }

        binding.main.setTransitionListener(object: MotionLayout.TransitionListener {
            override fun onTransitionStarted(
                motionLayout: MotionLayout?,
                startId: Int,
                endId: Int
            ) {}

            override fun onTransitionChange(
                motionLayout: MotionLayout?,
                startId: Int,
                endId: Int,
                progress: Float
            ) {}

            override fun onTransitionCompleted(motionLayout: MotionLayout?, currentId: Int) {
                if (!isActivityStarted) {
                    isActivityStarted = true
                    Handler(Looper.getMainLooper()).postDelayed({
                        startActivity(intent)
                        finish()
                    }, 3000L)
                }
            }

            override fun onTransitionTrigger(
                motionLayout: MotionLayout?,
                triggerId: Int,
                positive: Boolean,
                progress: Float
            ) {}

        })
    }
}