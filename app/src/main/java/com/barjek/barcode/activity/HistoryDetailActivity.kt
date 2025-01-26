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
import com.squareup.picasso.Picasso
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
                        val siswa = JSONObject(req.data)

                        val status = siswa.getString("status")
                        binding.apply {
                            textStatus.text = status
                            textStatus.setBackgroundColor(if (status == "Hadir") getColor(R.color.lightGreen) else getColor(R.color.lightRed))

                            textJam.text = siswa.getString("timestamp")
                            textHari.text = siswa.getString("hari")
                            textTgl.text = siswa.getString("tanggal")

                            textNama.text = siswa.getString("nama")
                            textKelas.text = siswa.getString("kelas")
                            textAbsen.text = siswa.getString("no_absen")
                        }
                        val foto = siswa.getString("foto")
                        Picasso.get()
                            .load(foto)
                            .placeholder(R.drawable.baseline_person_24)
                            .error(R.drawable.baseline_person_24)
                            .into(binding.ivPhoto)
                    }
                }
            }
        }
    }
}