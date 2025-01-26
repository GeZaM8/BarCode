package com.barjek.barcode.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.barjek.barcode.api.APIRequest
import com.barjek.barcode.database.DatabaseHelper
import com.barjek.barcode.databinding.ActivityConfirmPresentBinding
import com.barjek.barcode.model.Presence
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

class ConfirmPresentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityConfirmPresentBinding
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityConfirmPresentBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions())
        { permissions ->
            val cameraGranted = permissions[Manifest.permission.CAMERA] ?: false
            val storageGranted = permissions[Manifest.permission.CAMERA] ?: false

            if (cameraGranted && storageGranted) pickFromGallery()
        }

        binding.ivPhoto.setOnClickListener {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            )
        }

        val mood = intent.getStringExtra("mood") ?: "Neutral"
        val reason = intent.getStringExtra("reason") ?: ""
        val qrcode = intent.getStringExtra("qrcode") ?: ""

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
                            inputKelasDropdown.setText(siswa.getString("id_kelas"))
                            inputJurusan.setText(siswa.getString("kode_jurusan"))
                            binding.inputMood.setText(mood)
                        }
                    }
                }
            }
        }

        binding.btnKirim.setOnClickListener {
            val dataUserJSON = JSONObject().apply {
                put("id_user", id_user)
                put("status", status)
                put("mood", mood)
                put("reason", reason)
                put("qrcode", qrcode)
                put("foto", photoName)
            }

            val bitmap = (binding.ivPhoto.drawable as BitmapDrawable).bitmap
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
            val photoDataByte = byteArrayOutputStream.toByteArray()

            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    val req = APIRequest("absensi", "POST", dataUserJSON.toString(), photoDataByte).execute()
                    val responseJSON = JSONObject(req.data)
                    withContext(Dispatchers.Main) {
                        if (req.code in 200 until 300) {
                            Toast.makeText(
                                this@ConfirmPresentActivity,
                                "Anda berhasil absensi",
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()
                        } else {
                            Log.d("RESPONSEJSON", responseJSON.toString())
                            Toast.makeText(
                                this@ConfirmPresentActivity,
                                "Terjadi Kesalahan",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }
    }

    private lateinit var photoName: String
    val cropImageLauncher = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            val uri: Uri? = result.uriContent
            photoName = uri?.lastPathSegment.toString()
            Picasso.get().load(uri).into(binding.ivPhoto)
        } else {
            val error = result.error
            error?.printStackTrace()
        }
    }

    private fun pickFromGallery() {
        val cropImageOptions = CropImageOptions().apply {
            aspectRatioX = 1
            aspectRatioY = 1
            fixAspectRatio = true
        }
        cropImageLauncher.launch(CropImageContractOptions(null, cropImageOptions))
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
//        super.onBackPressed()
    }
}