package com.barjek.barcode.activity

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.barjek.barcode.R
import com.barjek.barcode.api.APIRequest
import com.barjek.barcode.databinding.ActivityEditProfileBinding
import com.barjek.barcode.geofence.GeofenceHelper
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayOutputStream

class EditProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditProfileBinding

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

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            finish()
        }

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
                            inputNisn.setText(siswa.getString("nisn"))
                            inputNis.setText(siswa.getString("nis"))
                            inputEmail.setText(siswa.getString("email"))
                        }
                        photoName = siswa.getString("foto")
                        Picasso.get()
                            .load(photoName)
                            .placeholder(R.drawable.baseline_person_24)
                            .error(R.drawable.baseline_person_24)
                            .into(binding.ivPhoto)
                    }
                }
            }
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
                put("foto", photoName)
            }

            val bitmap = (binding.ivPhoto.drawable as BitmapDrawable).bitmap
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
            val photoDataByte = byteArrayOutputStream.toByteArray()

            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    Log.d("DATA EDIT PROFILE", "${data}, image: $photoDataByte")
                    val req = APIRequest("update/siswa", "POST", data.toString(), photoDataByte).execute()
                    Log.d("DATA REQ EDIT", req.data)
                    val response = JSONObject(req.data)

                    withContext(Dispatchers.Main) {
                        if (req.code in 200 until 300) {
                            Toast.makeText(this@EditProfileActivity, response.getString("messages"), Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Log.d("ERROR EDIT PROFILE", response.toString())
                            Toast.makeText(this@EditProfileActivity, "Gagal Update Profile", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
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
}