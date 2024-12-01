package com.barjek.barcode.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.barjek.barcode.databinding.ActivityRegisterProfileBinding

class RegisterProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityRegisterProfileBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.btnCreate.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}