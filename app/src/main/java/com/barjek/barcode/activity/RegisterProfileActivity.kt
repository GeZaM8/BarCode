package com.barjek.barcode.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.barjek.barcode.api.APIRequest
import com.barjek.barcode.database.DatabaseHelper
import com.barjek.barcode.databinding.ActivityRegisterProfileBinding
import com.barjek.barcode.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class RegisterProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityRegisterProfileBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val email = intent.getStringExtra("EMAIL") ?: ""
        val password = intent.getStringExtra("PASSWORD") ?: ""

        binding.inputNama.inputType = android.text.InputType.TYPE_CLASS_TEXT
        binding.inputAbsen.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        binding.inputKelasDropdown.inputType = android.text.InputType.TYPE_CLASS_TEXT
        binding.inputJurusan.inputType = android.text.InputType.TYPE_CLASS_TEXT
        binding.inputNisn.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        binding.inputNis.inputType = android.text.InputType.TYPE_CLASS_NUMBER

        val optionsKelas = mutableListOf<String>()

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val req = APIRequest("kelas").execute()

                withContext(Dispatchers.Main) {
                    if (req.code in 200 until 300) {
                        val data = JSONArray(req.data)
                        (0 until data.length()).forEach { index ->
                            val objectKelas = data.getJSONObject(index)
                            optionsKelas.add(objectKelas.getString("kelas"))
                        }
                        Log.d("KELAS", optionsKelas.toString())

                        val adapter = ArrayAdapter(this@RegisterProfileActivity, android.R.layout.simple_dropdown_item_1line, optionsKelas)
                        binding.inputKelasDropdown.setAdapter(adapter)
                    }
                }
            }
        }

        binding.btnCreate.setOnClickListener {
            val nama = binding.inputNama.text.toString()
            val kelas = binding.inputKelasDropdown.text.toString()
            val jurusan = binding.inputJurusan.text.toString()
            val absen = binding.inputAbsen.text.toString()
            val nis = binding.inputNis.text.toString()
            val nisn = binding.inputNisn.text.toString()

            val data = JSONObject().apply {
                put("email", email)
                put("password", password)
                put("nama", nama)
                put("kelas", kelas)
                put("kode_jurusan", jurusan)
                put("no_absen", absen)
                put("nis", nis)
                put("nisn", nisn)
            }

            when {
                nama.isEmpty() -> binding.inputNama.error = "Nama tidak boleh kosong"
                absen.isEmpty() -> binding.inputAbsen.error = "Nomor absen tidak boleh kosong"
                kelas.isEmpty() -> binding.inputKelasDropdown.error = "Kelas tidak boleh kosong"
                jurusan.isEmpty() -> binding.inputJurusan.error = "Jurusan tidak boleh kosong"
                nisn.isEmpty() -> binding.inputNisn.error = "NISN tidak boleh kosong"
                nis.isEmpty() -> binding.inputNis.error = "NIS tidak boleh kosong"
                else -> {
                    lifecycleScope.launch {
                        withContext(Dispatchers.IO) {
                            val req = APIRequest("register", "POST", data.toString()).execute()
                            val response = JSONObject(req.data)

                            withContext(Dispatchers.Main) {
                                if (req.code in 200 until 300) {
                                    Toast.makeText(this@RegisterProfileActivity, response.getString("messages"), Toast.LENGTH_SHORT).show()
                                    startActivity(Intent(this@RegisterProfileActivity, LoginActivity::class.java))
                                    finish()
                                } else {
                                    Toast.makeText(this@RegisterProfileActivity, "Harap masukkan data anda dengan benar", Toast.LENGTH_SHORT).show()
                                    Toast.makeText(this@RegisterProfileActivity, "Harap hubungi ", Toast.LENGTH_SHORT).show()
                                    Log.d("ERROR REGISTER", response.toString())
                                }
                            }
                        }
                    }
//                    try {
//                        val user = User(
//                            id = userId,
//                            email = email,
//                            password = password,
//                            nama = nama,
//                            kelas = kelas,
//                            jurusan = jurusan,
//                            nis = nis,
//                            nisn = nisn
//                        )
//
//                        val result = dbHelper.insertUser(user)
//                        if (result != -1L) {
//                            Toast.makeText(this, "Registrasi berhasil", Toast.LENGTH_SHORT).show()
//                            startActivity(Intent(this, LoginActivity::class.java)
//                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
//                            finishAffinity()
//                        } else {
//                            Toast.makeText(this, "Registrasi gagal", Toast.LENGTH_SHORT).show()
//                        }
//                    } catch (e: Exception) {
//                        Toast.makeText(this, "Terjadi kesalahan: ${e.message}", Toast.LENGTH_SHORT).show()
//                    }
                }
            }
        }
    }
}