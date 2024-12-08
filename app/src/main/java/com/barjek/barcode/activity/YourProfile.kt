package com.barjek.barcode.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.barjek.barcode.database.DatabaseHelper
import com.barjek.barcode.databinding.ActivityYourProfileBinding

class YourProfile : AppCompatActivity() {
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var binding: ActivityYourProfileBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityYourProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        dbHelper = DatabaseHelper(this)

        val sharedPref = getSharedPreferences("UserPref", Context.MODE_PRIVATE)
        val userEmail = sharedPref.getString("email", "") ?: ""

        val user = dbHelper.getUserByEmail(userEmail)

        user?.let {
            binding.tvName.text = it.nama
            binding.tvClass.text = "${it.kelas} ${it.jurusan}"
            binding.tvNISN.text = it.nisn
            binding.tvNIS.text = it.nis
        }

        binding.btnBack.setOnClickListener {
            finish()
        }
        
        binding.btnLogout.setOnClickListener {
            sharedPref.edit().clear().apply()
            startActivity(Intent(this, LoginActivity::class.java))
            finishAffinity()
        }
    }
}