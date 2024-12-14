package com.barjek.barcode.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.barjek.barcode.database.DatabaseHelper
import com.barjek.barcode.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)

        binding.btnLogin.setOnClickListener {
            val email = binding.inputEmail.text.toString()
            val password = binding.inputPassword.text.toString()

            when {
                email.isEmpty() -> {
                    binding.inputEmail.error = "Email tidak boleh kosong"
                }
                !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    binding.inputEmail.error = "Format email tidak valid"
                }
                password.isEmpty() -> {
                    binding.inputPassword.error = "Password tidak boleh kosong"
                }
                else -> {
                    if (dbHelper.checkUser(email, password)) {
                        val user = dbHelper.getUserByEmail(email)
                        if (user != null) {
                            val sharedPref = getSharedPreferences("LoginPref", MODE_PRIVATE)
                            with(sharedPref.edit()) {
                                putString("USER_ID", user.id)
                                putString("EMAIL", user.email)
                                putString("NAMA", user.nama)
                                putString("KELAS", user.kelas)
                                apply()
                            }

                            Toast.makeText(this, "Login berhasil", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, HomePageActivity::class.java)
                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                            finish()
                        }
                    } else {
                        Toast.makeText(this, "Email atau password salah", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        binding.hrefRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.google.setOnClickListener {
            Toast.makeText(this, "Dalam proses bosquh", Toast.LENGTH_SHORT).show()
        }
    }
}