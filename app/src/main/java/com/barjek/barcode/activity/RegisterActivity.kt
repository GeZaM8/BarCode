package com.barjek.barcode.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.barjek.barcode.database.DatabaseHelper
import com.barjek.barcode.databinding.ActivityRegisterBinding
import java.util.UUID

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        
        dbHelper = DatabaseHelper(this)

        binding.btnRegister.setOnClickListener {
            val email = binding.inputEmail.text.toString()
            val password = binding.inputPassword.text.toString()
            val confirmPassword = binding.inputCPassword.text.toString()

            when {
                email.isEmpty() -> {
                    binding.inputEmail.error = "Email tidak boleh kosong"
                }
                password.isEmpty() -> {
                    binding.inputPassword.error = "Password tidak boleh kosong"
                }
                password != confirmPassword -> {
                    binding.inputCPassword.error = "Password tidak cocok"
                }
                else -> {
                    val intent = Intent(this, RegisterProfileActivity::class.java).apply {
                        putExtra("EMAIL", email)
                        putExtra("PASSWORD", password)
                    }
                    startActivity(intent)
                }
            }
        }

        binding.hrefLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}