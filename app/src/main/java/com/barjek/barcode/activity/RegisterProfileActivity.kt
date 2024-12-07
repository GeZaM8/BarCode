package com.barjek.barcode.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.barjek.barcode.database.DatabaseHelper
import com.barjek.barcode.databinding.ActivityRegisterProfileBinding
import com.barjek.barcode.model.User

class RegisterProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterProfileBinding
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityRegisterProfileBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)

        val userId = intent.getStringExtra("USER_ID") ?: ""
        val email = intent.getStringExtra("EMAIL") ?: ""
        val password = intent.getStringExtra("PASSWORD") ?: ""

        binding.inputNama.inputType = android.text.InputType.TYPE_CLASS_TEXT
        binding.inputAbsen.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        binding.inputKelas.inputType = android.text.InputType.TYPE_CLASS_TEXT
        binding.inputJurusan.inputType = android.text.InputType.TYPE_CLASS_TEXT
        binding.inputNisn.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        binding.inputNis.inputType = android.text.InputType.TYPE_CLASS_NUMBER

        binding.btnCreate.setOnClickListener {
            val nama = binding.inputNama.text.toString()
            val absen = binding.inputAbsen.text.toString()
            val kelas = binding.inputKelas.text.toString()
            val jurusan = binding.inputJurusan.text.toString()
            val nisn = binding.inputNisn.text.toString()
            val nis = binding.inputNis.text.toString()

            when {
                nama.isEmpty() -> binding.inputNama.error = "Nama tidak boleh kosong"
                absen.isEmpty() -> binding.inputAbsen.error = "Nomor absen tidak boleh kosong"
                kelas.isEmpty() -> binding.inputKelas.error = "Kelas tidak boleh kosong"
                jurusan.isEmpty() -> binding.inputJurusan.error = "Jurusan tidak boleh kosong"
                nisn.isEmpty() -> binding.inputNisn.error = "NISN tidak boleh kosong"
                nis.isEmpty() -> binding.inputNis.error = "NIS tidak boleh kosong"
                else -> {
                    try {
                        val user = User(
                            id = userId,
                            email = email,
                            password = password,
                            nama = nama,
                            kelas = kelas,
                            jurusan = jurusan,
                            nis = nis,
                            nisn = nisn
                        )

                        val result = dbHelper.insertUser(user)
                        if (result != -1L) {
                            Toast.makeText(this, "Registrasi berhasil", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, LoginActivity::class.java)
                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                            finishAffinity()
                        } else {
                            Toast.makeText(this, "Registrasi gagal", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this, "Terjadi kesalahan: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}