package com.barjek.barcode.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.barjek.barcode.R
import com.barjek.barcode.databinding.ActivityConfirmPresentBinding

class ConfirmPresentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityConfirmPresentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityConfirmPresentBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }
}