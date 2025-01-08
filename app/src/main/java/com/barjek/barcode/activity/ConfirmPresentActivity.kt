package com.barjek.barcode.activity

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.barjek.barcode.database.DatabaseHelper
import com.barjek.barcode.databinding.ActivityConfirmPresentBinding
import com.barjek.barcode.model.Presence
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class ConfirmPresentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityConfirmPresentBinding
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityConfirmPresentBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)

        val mood = intent.getStringExtra("mood") ?: "Neutral"
        val reason = intent.getStringExtra("reason") ?: ""

        binding.inputMood.hint = mood

        val sharedPref = getSharedPreferences("UserPref", Context.MODE_PRIVATE)
        val userEmail = sharedPref.getString("EMAIL", "") ?: ""

        val user = dbHelper.getUserByEmail(userEmail)

        user?.let {
            binding.apply {
                inputNama.setText(it.nama)
                inputKelas.setText(it.kelas)
                inputJurusan.setText(it.jurusan)
                inputNama.isEnabled = false
                inputKelas.isEnabled = false
                inputJurusan.isEnabled = false
            }
        }

        binding.btnKirim.setOnClickListener {
            val today = SimpleDateFormat("EEEE-yyyy-MM-dd", Locale.getDefault()).format(Date())
            
            user?.let { currentUser ->
                if (dbHelper.checkTodayPresence(currentUser.id, today)) {
                    Toast.makeText(this, "Anda sudah melakukan presensi hari ini", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val presence = Presence(
                    hadirId = UUID.randomUUID().toString(),
                    userId = currentUser.id,
                    date = today,
                    status = "Hadir",
                    timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                        .format(Date()),
                    location = binding.inputLocation.text.toString(),
                    mood = mood,
                    reason = reason
                )

                if (dbHelper.insertPresence(presence) > 0) {
                    Toast.makeText(this, "Presensi berhasil", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Gagal melakukan presensi", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}