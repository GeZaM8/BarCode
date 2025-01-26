package com.barjek.barcode.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.barjek.barcode.api.APIRequest
import com.barjek.barcode.database.DatabaseHelper
import com.barjek.barcode.databinding.ActivityLoginBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            val email = binding.inputEmail.text.toString()
            val password = binding.inputPassword.text.toString()

            val dataPost = JSONObject().apply {
                put("email", email)
                put("password", password)
            }

            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    val req = APIRequest(
                        url = "login",
                        method = "POST",
                        data = dataPost.toString()
                    ).execute()
                    val response = JSONObject(req.data)

                    withContext(Dispatchers.Main) {
                        if (req.code in 200 until 300) {
                            val sharedPref = getSharedPreferences("UserPref", MODE_PRIVATE)
                            with(sharedPref.edit()) {
                                putString("ID_USER", response.getString("id_user"))
                                apply()
                            }
                            Log.d("ID Login", response.getString("id_user"))
                            Toast.makeText(this@LoginActivity, "Login berhasil", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@LoginActivity, HomePageActivity::class.java))
                            finishAffinity()
                        } else {
                            Log.d("Salah", "$response")
                            Toast.makeText(this@LoginActivity, "Email atau Password Salah", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

//            when {
//                email.isEmpty() -> {
//                    binding.inputEmail.error = "Email tidak boleh kosong"
//                }
//                !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
//                    binding.inputEmail.error = "Format email tidak valid"
//                }
//                password.isEmpty() -> {
//                    binding.inputPassword.error = "Password tidak boleh kosong"
//                }
//                else -> {
//                    if (dbHelper.checkUser(email, password)) {
//                        val user = dbHelper.getUserByEmail(email)
//                        if (user != null) {
//                            val sharedPref = getSharedPreferences("UserPref", MODE_PRIVATE)
////                            with(sharedPref.edit()) {
////                                putString("USER_ID", user.id)
////                                putString("EMAIL", user.email)
////                                putString("NAMA", user.nama)
////                                putString("KELAS", user.kelas)
////                                apply()
////                            }
//
//                            Toast.makeText(this, "Login berhasil", Toast.LENGTH_SHORT).show()
//                            startActivity(Intent(this, HomePageActivity::class.java)
//                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
//                            finish()
//                        }
//                    } else {
//                        Toast.makeText(this, "Email atau password salah", Toast.LENGTH_SHORT).show()
//                    }
//                }
//            }
        }

        binding.hrefRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.google.setOnClickListener {
            Toast.makeText(this, "Dalam proses bosquh", Toast.LENGTH_SHORT).show()
        }
    }
}