package com.barjek.barcode.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.barjek.barcode.R
import com.barjek.barcode.api.APIRequest
import com.barjek.barcode.database.DatabaseHelper
import com.barjek.barcode.databinding.ActivityRegisterBinding
import com.barjek.barcode.databinding.LayoutAktivasiSiswaBinding
import com.barjek.barcode.databinding.LayoutChooseMoodBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.util.UUID

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var dbHelper: DatabaseHelper
    private var nis = ""
    private var nisn = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val sharedPreferences = getSharedPreferences("MyAppPreferences", MODE_PRIVATE)
        sharedPreferences.edit().putBoolean("isFirstRun", false).apply()
        
        dbHelper = DatabaseHelper(this)

        binding.btnRegister.setOnClickListener {
            if (nis.isEmpty() || nisn.isEmpty() ) {
                inputNisNisnLayout()
            } else {
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

                        val data = JSONObject().apply {
                            put("email", email)
                            put("password", password)
                            put("nis", nis)
                            put("nisn", nisn)
                        }

                        lifecycleScope.launch {
                            withContext(Dispatchers.IO) {
                                val req = APIRequest("aktivasi", "POST", data.toString()).execute()
                                val response = JSONObject(req.data)

                                withContext(Dispatchers.Main) {
                                    if (req.code in 200 until 300) {
                                        Toast.makeText(this@RegisterActivity, "Aktivasi Berhasil", Toast.LENGTH_SHORT).show()
                                        startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                                        finish()
                                    } else {
                                        try {
                                            val error = response.getJSONObject("error")
                                            Toast.makeText(this@RegisterActivity, error.getString("messages"), Toast.LENGTH_SHORT).show()
                                            Log.d("ERROR REGISTER", response.toString())
                                        } catch (e: JSONException) {
                                            Toast.makeText(this@RegisterActivity, "Aktivasi Gagal", Toast.LENGTH_SHORT).show()
                                        }
                                        nis = ""; nisn = ""
                                    }
                                }
                            }
                        }
                    }
                }

//
            }
        }

        binding.hrefLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun inputNisNisnLayout() {
        val customLayout = layoutInflater.inflate(R.layout.layout_aktivasi_siswa, null)
        val bindingView = LayoutAktivasiSiswaBinding.bind(customLayout)

        val input = AlertDialog.Builder(this)
            .setView(customLayout)
            .setOnDismissListener {
                nis = bindingView.inputNis.text.toString().trim()
                nisn = bindingView.inputNisn.text.toString().trim()
            }
            .create()

        input.show()
    }
}