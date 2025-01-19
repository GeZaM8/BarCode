package com.barjek.barcode.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.barjek.barcode.R
import com.barjek.barcode.api.APIRequest
import com.barjek.barcode.databinding.ActivityEditProfileBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class EditProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userPref = getSharedPreferences("UserPref", MODE_PRIVATE)
        val id_user = userPref.getString("USER_ID", "")
        val nama = userPref.getString("NAMA", "")
        val absen = userPref.getString("ABSEN", "")
        val kelas = userPref.getString("KELAS", "")
        val nisn = userPref.getString("NISN", "")
        val nis = userPref.getString("NIS", "")
        val email = userPref.getString("EMAIL", "")

        binding.apply {
            inputNama.setText(nama)
            inputAbsen.setText(absen)
            inputKelas.setText(kelas)
            inputNisn.setText(nisn)
            inputNis.setText(nis)
            inputEmail.setText(email)
        }

        binding.btnUpdate.setOnClickListener {
            val nama_update = binding.inputNama.text.toString()
            val absen_update = binding.inputAbsen.text.toString()
            val kelas_update = binding.inputKelas.text.toString()
            val nisn_update = binding.inputNisn.text.toString()
            val nis_update = binding.inputNis.text.toString()
            val email_update = binding.inputEmail.text.toString()
            val data = JSONObject().apply {
                put("id_user", id_user)
                put("nama", nama_update)
                put("kelas", kelas_update)
                put("absen", absen_update)
                put("nis", nis_update)
                put("nisn", nisn_update)
                put("email", email_update)
            }

            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    Log.d("DATA EDIT PROFILE", data.toString())
                    val req = APIRequest("update/siswa", "PATCH", data.toString()).execute()
                    val response = JSONObject(req.data)

                    withContext(Dispatchers.Main) {
                        if (req.code in 200 until 300) {
                            Toast.makeText(this@EditProfileActivity, response.getString("messages"), Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Log.d("ERROR EDIT PROFILE", response.toString())
                            Toast.makeText(this@EditProfileActivity, response.getString("messages"), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }
}