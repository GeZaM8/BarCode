package com.barjek.barcode.activity

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.barjek.barcode.api.APIRequest
import com.barjek.barcode.database.DatabaseHelper
import com.barjek.barcode.databinding.ActivityConfirmPresentBinding
import com.barjek.barcode.model.Presence
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
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
        val qrcode = intent.getStringExtra("qrcode") ?: ""

        binding.inputMood.hint = mood

        val sharedPref = getSharedPreferences("UserPref", Context.MODE_PRIVATE)
        val id = sharedPref.getString("ID_USER", "0")

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 6)
            set(Calendar.MINUTE, 30)
        }
        val waktuAkhir = calendar.timeInMillis

        val waktuSekarang = System.currentTimeMillis()
        Log.d("WAKTU", "Sekarang: $waktuSekarang, Akhir: $waktuAkhir")

        val status = if (waktuSekarang > waktuAkhir) "Terlambat" else "Hadir"
        val userPref = getSharedPreferences("UserPref", MODE_PRIVATE)
        val id_user = userPref.getString("ID_USER", "")

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val req = APIRequest("siswa/$id_user", "GET").execute()

                withContext(Dispatchers.Main) {
                    if (req.code in 200 until 300) {
                        val siswa = JSONObject(req.data)

                        binding.apply {
                            inputNama.setText(siswa.getString("nama"))
                            inputAbsen.setText(siswa.getString("no_absen"))
                            inputKelas.setText(siswa.getString("kelas"))
                            inputJurusan.setText(siswa.getString("kode_jurusan"))
                            inputKelas.isEnabled = false
                            inputJurusan.isEnabled = false
                        }
                    }
                }
            }
        }

        binding.btnKirim.setOnClickListener {
            val dataUserJSON = JSONObject().apply {
                put("id_user", id)
                put("status", status)
                put("mood", mood)
                put("reason", reason)
                put("qrcode", qrcode)
            }

            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    val req = APIRequest("absensi", "POST", dataUserJSON.toString()).execute()
                    val responseJSON = JSONObject(req.data)
                    withContext(Dispatchers.Main) {
                        if (req.code in 200 until 300) {
                            val message = responseJSON.getString("messages")
                            Toast.makeText(this@ConfirmPresentActivity, message, Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            val errorMessage = responseJSON.getJSONObject("messages").getString("error")
                            Log.d("RESPONSEJSON", responseJSON.toString())
                            Toast.makeText(this@ConfirmPresentActivity, errorMessage, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            
//            user?.let { currentUser ->
//                if (dbHelper.checkTodayPresence(currentUser.id, today)) {
//                    Toast.makeText(this, "Anda sudah melakukan presensi hari ini", Toast.LENGTH_SHORT).show()
//                    return@setOnClickListener
//                }
//
//                val presence = Presence(
//                    hadirId = UUID.randomUUID().toString(),
//                    userId = currentUser.id,
//                    date = today,
//                    status = "Hadir",
//                    timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
//                        .format(Date()),
//                    location = binding.inputLocation.text.toString(),
//                    mood = mood,
//                    reason = reason
//                )
//
//                if (dbHelper.insertPresence(presence) > 0) {
//                    Toast.makeText(this, "Presensi berhasil", Toast.LENGTH_SHORT).show()
//                    finish()
//                } else {
//                    Toast.makeText(this, "Gagal melakukan presensi", Toast.LENGTH_SHORT).show()
//                }
//            }
        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
//        super.onBackPressed()
    }
}