package com.barjek.barcode

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.barjek.barcode.databinding.ActivityLoginBinding

class Login_Activity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityLoginBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }
}