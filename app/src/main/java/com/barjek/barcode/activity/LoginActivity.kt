package com.barjek.barcode.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)

        lifecycleScope.launch {
            withContext(Dispatchers.Main) {
                val req = APIRequest("woi").execute()
                Log.d("DATA TEST", req.data)
            }
        }

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

                    withContext(Dispatchers.Main) {
                        if (req.code in 200 until 300) {
                            val data = JSONObject(req.data)
                            val sharedPref = getSharedPreferences("UserPref", MODE_PRIVATE)
                            with(sharedPref.edit()) {
                                putString("USER_ID", data.getString("id_user"))
                                putString("EMAIL", data.getString("email"))
                                putString("NAMA", data.getString("nama"))
                                putString("KELAS", data.getString("kelas"))
                                putString("NISN", data.getString("nisn"))
                                putString("NIS", data.getString("nis"))
                                putString("JURUSAN", data.getString("nama_jurusan"))
                                putString("ABSEN", data.getString("absen"))

                                putString("ABSEN", data.getString("absen"))
                                putString("ABSEN", data.getString("absen"))
                                putString("ABSEN", data.getString("absen"))
                                apply()
                            }
                            Toast.makeText(this@LoginActivity, "Login berhasil", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@LoginActivity, HomePageActivity::class.java)
                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                            finish()
                        } else {
                            Toast.makeText(this@LoginActivity, req.data, Toast.LENGTH_SHORT).show()
//                            Log.d("Salah", req.data)
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