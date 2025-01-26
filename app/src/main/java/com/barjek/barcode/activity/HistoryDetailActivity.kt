package com.barjek.barcode.activity

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.barjek.barcode.R
import com.barjek.barcode.api.APIRequest
import com.barjek.barcode.databinding.ActivityHistoryDetailBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class HistoryDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHistoryDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityHistoryDetailBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val sharedPref = getSharedPreferences("UserPref", MODE_PRIVATE)
        val id_user = sharedPref.getString("ID_USER", "")
        val id_absensi = intent.getStringExtra("id_absensi")

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val req = APIRequest("absensi/$id_user/$id_absensi").execute()

                withContext(Dispatchers.Main) {
                    if (req.code in 200 until 300) {
                        val data = JSONObject(req.data)
                        Log.d("ABSENSI DETAIL", data.toString())
                    }
                }
            }
        }
    }
}